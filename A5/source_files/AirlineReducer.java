import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by sujith,karthik on 2/20/16.
 * AirlineReducer class calculates the number of connections and missed connections
 */

public class AirlineReducer extends Reducer<Text, FlightDetails, Text, Text> {

    @Override
    public void reduce(Text key, Iterable<FlightDetails> values, Context context) throws IOException, InterruptedException {
        List<FlightDetails> flightDetailsList = new ArrayList<>();
        // creating a array list of all the flight details. Since we want to iterate 2 times.
        for (FlightDetails fDetails :values) {
            FlightDetails fDetailsNew = new FlightDetails(fDetails.type,fDetails.scheduledTime,fDetails.actualTime,fDetails.cancelled);
            flightDetailsList.add(fDetailsNew);
        }
        // sort the list based on scheduledTime
        Collections.sort(flightDetailsList);
        context.write(key,getCounts(flightDetailsList));
    }

    /***
     * This method calculates the number of connections and missed connections
     * Checking only for flights within a six hour window to optimize for performance
     * @param flightDetailsList
     * @return
     */
    private static Text getCounts(List<FlightDetails> flightDetailsList) {
        long connections = 0L;
        long missedConnections = 0L;
        int numberOfFlights = flightDetailsList.size();
        for (FlightDetails f : flightDetailsList) {
            int otherIndex = flightDetailsList.indexOf(f);
            // skip this iteration if its not the first flight
            if(!f.type) continue;
            //check if flight index is less than number of flights. Also only check for flights within the next 6 hours
            while(otherIndex<numberOfFlights && ((flightDetailsList.get(otherIndex).scheduledTime - f.scheduledTime) <= (6*60*60000))) {
                // check if other flight is the second flight
                if (!flightDetailsList.get(otherIndex).type) {
                    //calculate scheduled layover between the flights
                    long layOver = (flightDetailsList.get(otherIndex).scheduledTime - f.scheduledTime);
                    //check if scheduled layover is > 30 mins
                    if (layOver >= (30 * 60000)) {
                        // this is a connecting flight
                        connections += 1;
                        //calculate the actual layover between the flights
                        long actualLayOver = flightDetailsList.get(otherIndex).actualTime - f.actualTime;
                        //check if the first flight is cancelled or if the actual layover is less than 30 mins
                        if (f.cancelled == 1 || actualLayOver < (30 * 60000))
                            // this is a missed connection
                            missedConnections += 1;
                    }
                }
                otherIndex+=1;
            }
        }
        String counts = String.valueOf(connections) + "," + String.valueOf(missedConnections);
        return new Text(counts);
    }
}


