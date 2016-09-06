import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * Author(s): Karthik, Sujith
 * Main class - kickstarts the MapReduce jobs
 */
public class AirlineMain extends Configured implements Tool {
    public int run(String[] args) throws Exception {

        if (args.length < 2) {
            System.err.println("Provide correct number of command line arguments");
            System.exit(1);
        }
        Configuration conf = new Configuration();
        conf.set("mapreduce.output.textoutputformat.separator",",");
        Job job = Job.getInstance(conf, "Airline Main");
        job.setJarByClass(AirlineMain.class);
        job.setMapperClass(AirlineMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(FlightDetails.class);
        job.setReducerClass(AirlineReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        return job.waitForCompletion(true) ? 0 : 1;
    }
    public static void main(String[] args) throws Exception {
        System.exit(ToolRunner.run(new AirlineMain(), args));
    }
}

