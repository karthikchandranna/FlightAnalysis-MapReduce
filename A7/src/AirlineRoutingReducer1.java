/**
 * @classname: AirlineConnectionReducer
 *
 * @author Ruinan,Naveen,Sujith,Karthik
 * @description Reducer outputs the date as key and connection details as the value
 *
 * (key,value) => (<DATE>,<Connection Details>)
 *
 */

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class AirlineRoutingReducer1 extends Reducer<Text, CustomWritable, Text, Text> {
	public boolean DEV_MODE = false;
	@Override
	public void reduce(Text key, Iterable<CustomWritable> values, Context context) throws IOException, InterruptedException {
		ArrayList<CustomWritableComparable1> flightTable = new ArrayList<CustomWritableComparable1>();
		//adding the list of values to comparable to sort
		for (CustomWritable value : values){
			flightTable.add(new CustomWritableComparable1(value.getAirportO(), value.getAirportD(),
					value.getFlightType(), value.getFlightNum(), value.getScheduleTime(),
					value.getYear(),value.getMonth(),value.getDayOfMonth(),	value.getCRSElapsedTime()));
		}
		Collections.sort(flightTable);

		//find the connections 
		int flightI=0;
		for(CustomWritableComparable1 flight: flightTable){
			//check for departure flights
			int flightII=flightI+1;
			if(flightTable.get(flightI).flightType==0){
				flightI++;
				continue;
			}
			//Check arrival flights for the conditions <= 60 and >= 30 minutes based on scheduled time.
			while(flightII >= 0 && flightII < flightTable.size() && (flightTable.get(flightII).scheduleTime - flightTable.get(flightI).scheduleTime) <= (60)){
				if((flightTable.get(flightII).flightType == 0) && (flightTable.get(flightII).scheduleTime -
								flightTable.get(flightI).scheduleTime) >= (30)){

					context.write(new Text(flightTable.get(flightI).year+","+flightTable.get(flightI).month+","
							+flightTable.get(flightI).dayOfMonth),
							new Text(flightTable.get(flightI).airportO+","+flightTable.get(flightI).airportD+","
									+flightTable.get(flightII).airportD+","+(flightTable.get(flightI).ElapsedTime
									+flightTable.get(flightII).ElapsedTime)+","	+flightTable.get(flightI).flightNum+","
									+flightTable.get(flightII).flightNum));
				}
				flightII++;
			}
			flightI++;
		}
	}
}


