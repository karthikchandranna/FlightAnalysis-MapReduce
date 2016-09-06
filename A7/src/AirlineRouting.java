/***
 * *@classname: AirlineRouting
 *
 * @author Ruinan, Naveen, Karthik, Sujith
 * @description Main class, kickstarts two MR jobs
 * The first MR job creates a model from the history data
 * The second MR job gives the connections from the test data
 */

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class AirlineRouting extends Configured implements Tool {
	public static void main(String[] args) throws Exception {
		if (args.length != 4) {
			System.err.println("Wrong Syntax: FlightPrediction <input path> <input path>");
			System.exit(-1);
		}
		ToolRunner.run(new Configuration(), new AirlineRouting(), args);
	}
	
	@Override
	public int run(String[] args) throws Exception {

        Configuration conf = getConf();
        conf.set("mapreduce.output.textoutputformat.separator", ",");

        // First MR job
        Job job = new Job(conf);
        job.setJarByClass(AirlineRouting.class);
        job.setJobName("Airline Connection Train");
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        job.setMapperClass(AirlineRoutingMapper.class);
        job.setReducerClass(AirlineRoutingReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(CustomWritable.class);
        job.waitForCompletion(true);

        // Second MR Job
        Job job1 = new Job(conf);
        job1.setJarByClass(AirlineRouting.class);
        job1.setJobName("Airline Connection Test");
        FileInputFormat.addInputPath(job1, new Path(args[2]));
        FileOutputFormat.setOutputPath(job1, new Path(args[3]));
        job1.setMapperClass(AirlineRoutingMapper1.class);
        job1.setReducerClass(AirlineRoutingReducer1.class);
        job1.setOutputKeyClass(Text.class);
        job1.setOutputValueClass(CustomWritable.class);
        System.exit(job1.waitForCompletion(true) ? 0: 1);
        return 0;
	}
	
}
