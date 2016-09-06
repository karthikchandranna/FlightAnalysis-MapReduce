/**
 * @classname: AirlineRoutingMapper1
 *
 * @author Ruinan, Naveen, Karthik, Sujith
 * @description Mapper writes to context twice - once with the key as the carrier,origin and the value as flight details
 * and the second time with key as the carrier,destinationn and the value as flight details
 * (key,value) => 
 * (<CARRIER,INTERMEDIATE-CITY>,<DESTINATION/ORIGIN_CITY,FLAG,SCHEDULED-DEPARTURE/ARRIVAL-TIME,ACTUAL-DEPARTURE/ARRIVAL-TIME,DISTANCE,
 * DISTANCE GROUP,CARRIER>)
 *
 */


import java.io.IOException;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import java.util.Calendar;

public class AirlineRoutingMapper1 extends Mapper<LongWritable, Text, Text, CustomWritable> {
	@Override
	public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

		AirlineParser1 fParser = new AirlineParser1();
		if (!fParser.map(value.toString())) return;
		
		//0 From Intermediate to Dst
        long scheduleTimeOrigin = convertTimeToMilliseconds(fParser, fParser.CRSDepTime);
		Text keyOut = new Text(fParser.Carrier+","+fParser.Origin);
		CustomWritable valueOut1 = new CustomWritable(fParser.Origin, fParser.Destination, fParser.flightNum, 0,
                scheduleTimeOrigin,	fParser.Year,fParser.Month,fParser.DayOfMonth, fParser.CRSElapsedTime);
		context.write(keyOut, valueOut1);

		//1 From Origin to Intermediate
        long scheduleTimeDestination = convertTimeToMilliseconds(fParser, fParser.CRSArrTime);
		keyOut.set(fParser.Carrier + "," + fParser.Destination);
		CustomWritable valueOut2 = new CustomWritable(fParser.Origin, fParser.Destination, fParser.flightNum, 1,
                scheduleTimeDestination, fParser.Year,fParser.Month,fParser.DayOfMonth, fParser.CRSElapsedTime);
		context.write(keyOut, valueOut2);
	}

	/***
	 * Calculating time in milliseconds for an Airline
	 * @param fParser
	 * @param time
	 * @return
	 */
	private long convertTimeToMilliseconds(AirlineParser1 fParser, int time) {
		Calendar cal = Calendar.getInstance();
		cal.set(fParser.Year, fParser.Month-1, fParser.DayOfMonth, time/100, time%100, 0);
		return cal.getTimeInMillis()/(1000*60);
	}
}
