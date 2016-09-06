import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Created by Karthik, Sujith on 1/29/16.
 */
public class FareCompareReducerMean extends Reducer<Text, FloatWritable, Text, FloatWritable> {

    @Override
    public void reduce(Text key, Iterable<FloatWritable> values, Context context) throws IOException, InterruptedException {

        float count = 0.0f;
        float sum = 0.0f;

        for (FloatWritable value : values) {
            count++;
            sum += value.get();
        }

        float avg = sum/count;
        BigDecimal bd = new BigDecimal(avg);

        // rounding the average to two decimal places
        bd = bd.setScale(2,BigDecimal.ROUND_HALF_UP);
        avg = bd.floatValue();

        context.write(key, new FloatWritable(avg));

    }

}
