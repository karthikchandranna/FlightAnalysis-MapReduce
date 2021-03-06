import org.apache.avro.generic.GenericData;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

/**
 *
 * Created by Karthik, Sujith on 1/29/16.
 */
public class FareCompareReducerMedian extends Reducer<Text, FloatWritable, Text, FloatWritable> {

    @Override
    public void reduce(Text key, Iterable<FloatWritable> values, Context context) throws IOException, InterruptedException {

        List prices = new ArrayList<Float>();

        for (FloatWritable value : values) {
            prices.add(value.get());
        }

        Collections.sort(prices);

        float median;

        if (prices.size() % 2 == 0)
            //average of middle 2 elements if the list is even
            median = ((float) prices.get(prices.size() / 2) + (float) prices.get(prices.size() / 2 - 1)) / 2;
        else
            //taking the median when the list is odd
            median = (float) prices.get(prices.size() / 2);

        BigDecimal bd = new BigDecimal(median);

        // rounding the average to two decimal places
        bd = bd.setScale(2,BigDecimal.ROUND_HALF_UP);
        median = bd.floatValue();

        context.write(key, new FloatWritable(median));

    }

}
