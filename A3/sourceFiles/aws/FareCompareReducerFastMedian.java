import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by Karthik, Sujith on 1/29/16.
 */
public class FareCompareReducerFastMedian extends Reducer<Text, FloatWritable, Text, FloatWritable> {

    private static Float[] pricesArray;

    @Override
    public void reduce(Text key, Iterable<FloatWritable> values, Context context) throws IOException, InterruptedException {

        List<Float> pricesList = new ArrayList<>();
        for (FloatWritable value : values) {
            pricesList.add(value.get());
        }

        pricesArray = new Float[pricesList.size()];

        int i = 0;
        for (float val : pricesList) {
            pricesArray[i++] = val;
        }

        float fastMedian = selection_algorithm(1,pricesArray.length-1,pricesArray.length/2);

        context.write(key, new FloatWritable(fastMedian));

    }

    int partitions(int low,int high)
    {
        int p=low;
        int r=high;
        float x=pricesArray[r];
        int i=p-1;
        float temp;
        for(int j=p;j<=r-1;j++)
        {
            if (pricesArray[j]<=x)
            {

                i=i+1;
                temp = pricesArray[i];
                pricesArray[i] = pricesArray[j];
                pricesArray[j] = temp;

            }
        }
        temp = pricesArray[i+1];
        pricesArray[i+1] = pricesArray[r];
        pricesArray[r] = temp;

        return i+1;
    }

    float selection_algorithm(int left,int right,int kth)
    {
        for(;;)
        {
            int pivotIndex=partitions(left,right);          //Select the Pivot Between Left and Right
            int len=pivotIndex-left+1;

            if(kth==len)
                return pricesArray[pivotIndex];

            else if(kth<len)
                right=pivotIndex-1;

            else
            {
                kth=kth-len;
                left=pivotIndex+1;
            }
        }
    }

}
