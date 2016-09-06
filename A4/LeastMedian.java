import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Author(s): Karthik, Sujith
 */
public class LeastMedian extends Configured implements Tool {
    public int run(String[] args) throws Exception {

        if (args.length != 3) {
            System.err.println("Provide correct number of command line arguments");
            System.exit(1);
        }

        Configuration conf = new Configuration();
        BufferedReader br = new BufferedReader(new FileReader(args[2]));
        String cheapestAirline = null;

        String line;

        if ((line = br.readLine()) != null) {
            cheapestAirline = line;
        }

        // Passing parameter to mapper
        conf.set("cheapestCarrier", cheapestAirline);

        Job job = Job.getInstance(conf, "Least Median");
        job.setMapperClass(LeastMedianMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(FloatWritable.class);
        job.setReducerClass(LeastMedianReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(FloatWritable.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        return job.waitForCompletion(true) ? 0 : 1;
    }
    public static void main(String[] args) throws Exception {
        System.exit(ToolRunner.run(new LeastMedian(), args));
    }

}
