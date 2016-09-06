
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.*;
import java.util.Arrays;
import java.util.List;


/**
 * Author(s): Karthik, Sujith
 */
public class FareCompare extends Configured implements Tool {

    public static void main(String[] args) throws Exception {
        Long startTime = System.currentTimeMillis();
        if((ToolRunner.run(new FareCompare(), args)) == 0) {
            Long endTime = System.currentTimeMillis();

            // Get the unique combination to append to the filename
            String uniqueCombination = args[1] + "_" + args[2] + "_" + args[3];

            // Run the hadoop merge command to merge result files
            String hadoopMergeCommand = "hadoop fs -getmerge " + uniqueCombination + " " + uniqueCombination + ".txt";
            Process p = Runtime.getRuntime().exec(hadoopMergeCommand);
            p.waitFor();
            String fileName = uniqueCombination +".txt";
            try (BufferedReader br = new BufferedReader(new FileReader(fileName)))
            {
                String sCurrentLine;
                StringBuilder sb = new StringBuilder();

                // Format the result and write it to a separate file
                while ((sCurrentLine = br.readLine()) != null) {
                    String fields[] = sCurrentLine.split("\\s+");
                    String key = fields[0];
                    String value = fields[1];
                    String carrier = key.substring(0,2);
                    String month = key.substring(2);
                    String line = month + " " + carrier + " " + value + "\n";
                    sb.append(line);
                }

                try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream("result_"+uniqueCombination+".txt"), "utf-8"))) {
                    writer.write(sb.toString());
                }

            }
            File f = new File(uniqueCombination+".txt");
            f.delete();



            Long timeTaken = (endTime - startTime) / 1000;

            String timeFileName = "timeFile_pseudo_aws_"+args[2]+".txt";
            File timeFile = new File(timeFileName);

            if (!timeFile.exists()) {
                timeFile.createNewFile();
            }

            try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(timeFileName, true), "utf-8"))) {
                writer.write(timeTaken.toString());
                if (!args[3].equals("25")) {
                    writer.write(",");
                }
            }

            System.exit(0);
        }
        System.exit(1);
    }

    public int run(String[] args) throws Exception {

        if (args.length != 4) {
            System.err.println("Usage: Number of arguments must be 4");
            return 1;
        }

        // Set the Job class, Jar and the job name
        Job job = Job.getInstance(getConf(), "Fare Compare");
        job.setJarByClass(FareCompare.class);
        job.setJobName("Fare Compare Job");

        String type = args[2];
        Integer numberOfFiles = Integer.parseInt(args[3]);

        // Valid input for the number of files
        List<Integer> validNumberOfFiles = Arrays.asList(5,10,15,20,25);

        if (!validNumberOfFiles.contains(numberOfFiles)) {
            System.err.println("Please enter a valid count of number of input files");
            System.exit(1);
        }

        // List the file names in hadoop
        String[] hadoopListFilesCommand = {"/bin/sh","-c","hadoop fs -ls " + args[0] + " | sed '1d;s/  */ /g' | cut -d\\  -f8"};
        Process p = Runtime.getRuntime().exec(hadoopListFilesCommand);
        p.waitFor();

        BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));

        String fileName = "";

        int currentCount = 0;

        while ((fileName = b.readLine()) != null) {
            if (currentCount < numberOfFiles) {
                MultipleInputs.addInputPath(job,new Path(fileName),TextInputFormat.class);
                currentCount++;
            }
            else {
                break;
            }
        }

        String uniqueCombination = args[1] + "_" + args[2] + "_" + args[3];
        FileOutputFormat.setOutputPath(job, new Path(uniqueCombination));

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

        // Set the number of reduce tasks
        job.setNumReduceTasks(1);

        return job.waitForCompletion(true) ? 0 : 1;

    }
}
