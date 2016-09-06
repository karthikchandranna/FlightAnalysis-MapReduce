import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *  AirlineWeka class is used to predict airline delays using random forest algorithm
 *  provide by Weka
 *
 * Created by sujith, karthik on 3/5/16.
 *
 */
public class AirlineWeka {

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.err.println("Provide correct number of command line arguments");
            System.exit(1);
        }
        Configuration conf = new Configuration();
        // Set the text output separator from the reducer to comma
        conf.set("mapreduce.output.textoutputformat.separator", ",");

        // First MapReduce job generates the model
        Job job = Job.getInstance(conf, "Airline Weka");
        job.setJarByClass(AirlineWeka.class);
        job.setMapperClass(AirlineWekaMapper.class);
        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(AirlineCompositeKey.class);
        job.setReducerClass(AirlineWekaReducer.class);
        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        if (!job.waitForCompletion(true)) {
            System.out.println("First MR job failed");
            System.exit(-1);
        }

        // Second MapReduce job tests the model
        Job job2 = Job.getInstance(conf, "Airline Weka MR 2");
        job2.setJarByClass(AirlineWeka.class);
        job2.setMapperClass(AirlineWekaMapper2.class);
        job2.setMapOutputKeyClass(IntWritable.class);
        job2.setMapOutputValueClass(AirlineCompositeKey.class);
        job2.setReducerClass(AirlineWekaReducer2.class);
        job2.setOutputKeyClass(Text.class);
        job2.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job2, new Path(args[2]));
        FileOutputFormat.setOutputPath(job2, new Path(args[3]));
        System.exit(job2.waitForCompletion(true)? 0 : 1);
    }

    /*
    // Mapper class of the first MapReduce job
    // Reads the train dataset, creates an AirlineDetails object after removing fields that are not needed
    // Writes the month(key) and the AirlineCompositeKey object (value) to the context.
     */
    static class AirlineWekaMapper extends Mapper<LongWritable, Text, IntWritable, AirlineCompositeKey> {

        @Override
        public void map(LongWritable key, Text value, org.apache.hadoop.mapreduce.Mapper.Context context) throws IOException, InterruptedException {
            String line = value.toString();
            AirlineDetails flightDetails = AirlineDetails.createObject(line);
            // If there is a parse exception or if the flight is insane, return
            if (flightDetails == null || !flightDetails.isSane()) return;
            context.write(new IntWritable(flightDetails.month),new AirlineCompositeKey(flightDetails.getFactors()));
        }
    }

    /*
    // Reducer class of the first MapReduce job
    // Generates the model for the given training dataset
     */
    static class AirlineWekaReducer extends Reducer<IntWritable, AirlineCompositeKey, IntWritable, IntWritable> {

        @Override
        public void reduce(IntWritable key, Iterable<AirlineCompositeKey> values, Context context) throws IOException, InterruptedException {

            // Build attributes for the model
            ArrayList<Attribute> attributes = buildAttributes();

            // Create an Instances object and assign the declared attributes to it
            Instances dataset = new Instances(key.toString(),attributes,0);

            // Use the iterable on AirlineCompositeKey and create the dataset
            for (AirlineCompositeKey value : values) {
                Instance instance = createInstance(value, attributes);
                // Add the created instance to the dataset
                if (instance != null) dataset.add(instance);
            }

            // Set the class index of the dataset to the isDelayed attribute
            dataset.setClassIndex(dataset.numAttributes()-1);

            // Create a Random forest object and set the number of trees
            RandomForest rf = new RandomForest();
            rf.setNumTrees(Constants.NUM_TREES);

            try {
                // Build the random forest for the dataset
                rf.buildClassifier(dataset);
            } catch (Exception e) {
                System.err.println("Exception during build classifier: "+e.getMessage());
            }
            try {
                // Get the filesystem object from the configuration of the context
                Configuration conf = context.getConfiguration();
                FileSystem fs = FileSystem.get(conf);

                // Write the reducer output to a file on the HDFS
                FSDataOutputStream out = fs.create(new Path(key.toString()+".model"));
                ObjectOutputStream oStream = new ObjectOutputStream(out);
                oStream.writeObject(rf);
                oStream.flush();
                oStream.close();
            } catch (Exception e) {
                System.err.println("Exception during save model: "+e.getMessage());
            }
            context.write(key, key);
        }

    }

    /*
    // Mapper class for the second mapreduce job
    // Reads the test dataset and removes fields that are not needed
     */

    static class AirlineWekaMapper2 extends Mapper<LongWritable, Text, IntWritable, AirlineCompositeKey> {

        @Override
        public void map(LongWritable key, Text testValue, org.apache.hadoop.mapreduce.Mapper.Context context) throws IOException, InterruptedException {

            String line = testValue.toString();

            // Create an AirlineFlightDetails object
            AirlineDetails testFlightDetails = AirlineDetails.createTestObject(line);

            if (testFlightDetails == null) return;

            // Writes the month(key) and the stringified AirlineDetails object (value) to the context
            context.write(new IntWritable(testFlightDetails.month),new AirlineCompositeKey(testFlightDetails.getFactors()));

        }

    }

    /*
    // Reducer for the second MR job
     */
    static class AirlineWekaReducer2 extends Reducer<IntWritable, AirlineCompositeKey, Text, Text> {

        @Override
        public void reduce(IntWritable key, Iterable<AirlineCompositeKey> values, Context context) throws IOException, InterruptedException {

            // Build attributes for the model
            ArrayList<Attribute> attributes = buildAttributes();
            Instances testDataset = new Instances(key.toString(),attributes,0);
            for (AirlineCompositeKey value : values) {
                Instance instance = createInstance(value, attributes);
                if (instance != null) testDataset.add(instance);
            }

            // set the class index to the isDelayed attribute
            testDataset.setClassIndex(testDataset.numAttributes()-1);

            try {
                // Get the filesystem object from the configuration
                Configuration conf = context.getConfiguration();
                FileSystem fs = FileSystem.get(conf);

                // Read the model file from the filesystem
                FSDataInputStream in = fs.open(new Path(key.toString()+".model"));
                ObjectInputStream objectInputStream = new ObjectInputStream(in);
                RandomForest rf = (RandomForest) objectInputStream.readObject();
                objectInputStream.close();

                // Classify each instance and write the result to the context
                for (int i=0;i<testDataset.numInstances();i++) {
                    double pred = rf.classifyInstance(testDataset.instance(i));
                    String contextKey = (int)testDataset.instance(i).value(Constants.FLNO)
                            + "_" + getFormattedDate((int) testDataset.instance(i).value(Constants.DATE))
                            + "_" + getFormattedTime((int)testDataset.instance(i).value(Constants.CRSDEPTIME));
                    String contextValue = testDataset.classAttribute().value((int) pred);
                    context.write(new Text(contextKey),new Text(contextValue));
                }

            } catch (Exception e) {
                System.err.println("Exception during save model: "+e.getMessage());
            }

        }

        private String getFormattedTime(int value) {
            int hours = value/60;
            int minutes = value%60;
            int hLen = String.valueOf(hours).length();
            int mLen = String.valueOf(minutes).length();
            String hStr = String.valueOf(hours);
            String mStr = String.valueOf(minutes);

            if (hLen < 2) {
                hStr = "0"+hStr;
            }
            if (mLen < 2) {
                mStr = "0"+mStr;
            }

            String finalTime = hStr+mStr;
            if (finalTime.equals("2400")) finalTime = "0000";

            return finalTime;

        }

        /***
         * Get the formatted date from the integer
         * @param value integer date to be formatted
         * @return formatted string representing the date
         */
        private static String getFormattedDate(int value) {
            String date = String.valueOf(value);
            String year = date.substring(0,4);
            String month = date.substring(4,6);
            String day = date.substring(6);
            return (year+"-"+month+"-"+day);
        }
    }

    /***
     * Builds the 18 attributes required for the instance
     * @return List of attribute objects
     */
    private static ArrayList<Attribute> buildAttributes() {

        ArrayList<Attribute> attrs = new ArrayList<>();
        attrs.add(new Attribute("year"));
        attrs.add(new Attribute("quarter"));
        attrs.add(new Attribute("month"));
        attrs.add(new Attribute("dayOfMonth"));
        attrs.add(new Attribute("dayOfWeek"));
        attrs.add(new Attribute("date"));
        attrs.add(new Attribute("flno"));
        attrs.add(new Attribute("originId"));
        attrs.add(new Attribute("destId"));
        attrs.add(new Attribute("crsArrTime"));
        attrs.add(new Attribute("crsDepTime"));
        attrs.add(new Attribute("crsElapTime"));

        ArrayList<String> carrierNames = new ArrayList<>();
        carrierNames.addAll(Arrays.asList("9E","AA","AS","B6","DL","US","UA","VX","WN",
                "YV","EV","F9","FL","HA","MQ","OO","NK","CO","HP","NW","TW"));
        attrs.add(new Attribute("carrier",carrierNames));

        attrs.add(new Attribute("isFromChicago"));
        attrs.add(new Attribute("isHoliday"));
        attrs.add(new Attribute("isPopularSrcDst"));
        attrs.add(new Attribute("isBusyDay"));
        attrs.add(new Attribute("distance"));

        ArrayList<String> isDel = new ArrayList<>();
        isDel.add("TRUE");
        isDel.add("FALSE");
        attrs.add(new Attribute("isDelayed",isDel));

        return attrs;
    }

    /***
     *
     * @param airlineCompositeKey AirlineCompositeKey object having the necessary fields
     * @param attrs list of Attribute objects
     * @return
     */
    private static Instance createInstance(AirlineCompositeKey airlineCompositeKey, List<Attribute> attrs) {
        Instance inst = new DenseInstance(Constants.NUM_FACTORS);
        inst.setValue(attrs.get(Constants.YEAR), airlineCompositeKey.year);
        inst.setValue(attrs.get(Constants.QUARTER), airlineCompositeKey.quarter);
        inst.setValue(attrs.get(Constants.MONTH), airlineCompositeKey.month);
        inst.setValue(attrs.get(Constants.DAYOFMONTH), airlineCompositeKey.day);
        inst.setValue(attrs.get(Constants.DAYOFWEEK), airlineCompositeKey.dayOfWeek);
        inst.setValue(attrs.get(Constants.DATE), airlineCompositeKey.date);
        inst.setValue(attrs.get(Constants.FLNO), airlineCompositeKey.flno);
        inst.setValue(attrs.get(Constants.ORGID), airlineCompositeKey.orgid);
        inst.setValue(attrs.get(Constants.DESTID), airlineCompositeKey.destid);
        inst.setValue(attrs.get(Constants.CRSARRTIME), airlineCompositeKey.crsArrTime);
        inst.setValue(attrs.get(Constants.CRSDEPTIME), airlineCompositeKey.crsDepTime);
        inst.setValue(attrs.get(Constants.CRSELAPTIME), airlineCompositeKey.crsElapTime);

        inst.setValue(attrs.get(Constants.ISFROMCHICAGO), airlineCompositeKey.isFromChicago);
        inst.setValue(attrs.get(Constants.ISHOLIDAY), airlineCompositeKey.isHoliday);
        inst.setValue(attrs.get(Constants.ISPOPULARSRCDEST), airlineCompositeKey.isPopularSrcDest);
        inst.setValue(attrs.get(Constants.ISBUSYDAY), airlineCompositeKey.isBusyDay);
        inst.setValue(attrs.get(Constants.DISTANCE), airlineCompositeKey.distance);

        try {
            inst.setValue(attrs.get(Constants.CARRIER), airlineCompositeKey.carrier);
            // If isDelayed is neither true or false in the dataset, do not set it in the instance
            if (airlineCompositeKey.isDelayed.equals("TRUE") || airlineCompositeKey.isDelayed.equals("FALSE")) {
                inst.setValue(attrs.get(Constants.ISDELAYED), airlineCompositeKey.isDelayed);
            }
            return inst;
        } catch(Exception e) {
            return null;
        }
    }

}
