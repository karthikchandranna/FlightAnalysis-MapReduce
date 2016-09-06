import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 *
 * Created by sujith, karthik on 2/10/16.
 */
public class CheapAirlineReducer extends Reducer<Text, Text, Text, Text> {

    @Override
    public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

        SimpleRegression regression = new SimpleRegression();

        for(Text value:values){

            String[] valueArr = value.toString().split("\\|");

            String elapsedTimeString = valueArr[0];
            String priceString = valueArr[1];

            Double elpasedTime = Double.parseDouble(elapsedTimeString);
            Double price = Double.parseDouble(priceString);

            // Adding each data pair to the linear regression computation
            regression.addData(elpasedTime, price);
        }

        // Key remains unchanged, value is the intercept and slope obtained from the linear regression
        context.write(key, new Text(String.valueOf(regression.getIntercept())+","+String.valueOf(regression.getSlope())));

    }
}
