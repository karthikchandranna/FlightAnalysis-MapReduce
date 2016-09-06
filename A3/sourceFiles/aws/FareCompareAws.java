import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

/**
 *
 * Created by sujith, karthik on 2/6/16.
 */
public class FareCompareAws extends Configured implements Tool {
    static final Logger log = Logger.getLogger(FareCompareAws.class);

    public static void main(String[] args) throws Exception {

        Long startTime = System.currentTimeMillis();
        if (ToolRunner.run(new FareCompareAws(), args) == 0) {
            Long timeTaken = (System.currentTimeMillis() - startTime)/1000;
            log.debug("Time taken: "+timeTaken);
            System.exit(0);
        }
        System.exit(1);

    }

    public int run(String[] args) throws Exception {

        if (args.length != 3) {
            System.err.println("Usage: Number of arguments must be 3");
            return 1;
        }

        Job job = Job.getInstance(getConf(), "Fare Compare Aws");
        job.setJarByClass(FareCompareAws.class);
        job.setJobName("Fare Compare Aws Job");

        String type = args[2];

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        // Set the mapper class
        job.setMapperClass(FareCompareMapper.class);

        // Set the reducer class
        switch(type){
            case "mean" : job.setReducerClass(FareCompareReducerMean.class);
                break;
            case "median" : job.setReducerClass(FareCompareReducerMedian.class);
                break;
            case "fastMedian" : job.setReducerClass(FareCompareReducerFastMedian.class);
                break;
            default : System.err.println("Usage: 3rd argument should be either mean, median or fastMedian");
                return 1;
        }

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(FloatWritable.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(FloatWritable.class);

        job.setNumReduceTasks(1);

        return job.waitForCompletion(true) ? 0 : 1;

    }

}
