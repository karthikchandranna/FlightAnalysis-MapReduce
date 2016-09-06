/**
 * @classname: AirlineConnectionReducer
 *
 * @author Ruinan,Naveen,Sujith,Karthik
 * @description Reducer outputs the intermediate city as key and ratio of missed connections to connections as value
 * (Key,Value) => (<INTERMEDIATE-CITY>,<FRACTION OF MISSED CONN>)
 *
 */

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class AirlineRoutingReducer extends Reducer<Text, CustomWritable, Text, Text> {
	public boolean DEV_MODE = false;
	@Override
	public void reduce(Text Key, Iterable<CustomWritable> Values, Context context) throws IOException, InterruptedException {
		ArrayList<CustomWritableComparable> flightTable = new ArrayList<CustomWritableComparable>();
		//adding the list of values to comparable to sort
		for (CustomWritable Value : Values){
			flightTable.add(new CustomWritableComparable(Value.getCityName(), Value.getFlightType(), Value.getScheduleTime(), Value.getActualTime(),
					Value.getYear(),Value.getMonth(),Value.getDayOfMonth(),Value.getActualElapsedTime(),Value.getDistance(),Value.getDistanceGroup(),Value.getCarrier()));
		}
		Collections.sort(flightTable);

		//find the connections
		int flightI=0;
		int missedConnCount = 0;
		int connectionCount = 0;
		for(CustomWritableComparable flight: flightTable){
			//check for departure flights
			int flightII=flightI+1;
			if(flightTable.get(flightI).flightType==0){
				flightI++;
				continue;
			}
			//Check arrival flights for the conditions <= 60 and >= 30 minutes based on scheduled time.
			while(flightII >= 0 && flightII < flightTable.size() && (flightTable.get(flightII).scheduleTime - flightTable.get(flightI).scheduleTime) <= (60)){
				if((flightTable.get(flightII).flightType == 0) && (flightTable.get(flightII).scheduleTime - flightTable.get(flightI).scheduleTime) >= (30) && flightTable.get(flightI).carrier.equals(flightTable.get(flightII).carrier)){
					connectionCount++;
					if((flightTable.get(flightII).flightType == 0) && (flightTable.get(flightII).actualTime - flightTable.get(flightI).actualTime) < (30)){
						missedConnCount++;
					}
				}
				flightII++;
			}
			flightI++;
		}
		context.write(Key, new Text(String.format("%.2f", (float)missedConnCount/connectionCount)));

	}
}


