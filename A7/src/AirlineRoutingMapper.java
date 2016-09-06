/**
 * @classname: AirlineRoutingMapper
 *
 * @author Ruinan, Naveen, Karthik, Sujith
 * @description Mapper writes to context twice - once with the key as the origin and the value as flight details
 * and the second time with key as the destinationn and the value as flight details
 * (Key,Value) => 
 * (<INTERMEDIATE-CITY>,<DESTINATION/ORIGIN_CITY,FLAG,SCHEDULED-DEPARTURE/ARRIVAL-TIME,ACTUAL-DEPARTURE/ARRIVAL-TIME,DISTANCE,
 * DISTANCE GROUP,CARRIER>)
 *
 */


import java.io.IOException;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import java.util.Calendar;

public class AirlineRoutingMapper extends Mapper<LongWritable, Text, Text, CustomWritable> {
    @Override
    public void map(LongWritable Key, Text Value, Context context) throws IOException, InterruptedException {

        AirlineParser fParser = new AirlineParser();
        if (!fParser.map(Value.toString())) return;

        //0 From Intermediate to Dst
        long scheduleTimeOrigin = convertTimeToMilliseconds(fParser, fParser.CRSDepTime);
        long actualTimeOrigin = convertTimeToMilliseconds(fParser, fParser.DepTime);
        CustomWritable ValueOut1 = new CustomWritable(fParser.Destination, 0, scheduleTimeOrigin,
                actualTimeOrigin,fParser.Year,fParser.Month,fParser.DayOfMonth,
                fParser.ActualElapsedTime,fParser.Distance,fParser.DistanceGroup,fParser.Carrier);
        context.write(new Text(fParser.Origin), ValueOut1);

        //1 From Origin to Intermediate
        long scheduleTimeDest = convertTimeToMilliseconds(fParser, fParser.CRSArrTime);
        long actualTimeDest = convertTimeToMilliseconds(fParser, fParser.ArrTime);
        CustomWritable ValueOut2 = new CustomWritable(fParser.Origin, 1, scheduleTimeDest,
                actualTimeDest,fParser.Year,fParser.Month,fParser.DayOfMonth,
                fParser.ActualElapsedTime,fParser.Distance,fParser.DistanceGroup,fParser.Carrier);
        context.write(new Text(fParser.Destination), ValueOut2);
    }

    /***
     * Calculating time in milliseconds for an Airline
     * @param fParser
     * @param time
     * @return
     */
    private long convertTimeToMilliseconds(AirlineParser fParser, int time) {
        Calendar cal = Calendar.getInstance();
        cal.set(fParser.Year, fParser.Month-1, fParser.DayOfMonth, time/100, time%100, 0);
        return cal.getTimeInMillis()/(1000*60);
    }
}
