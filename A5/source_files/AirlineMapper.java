import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
/**
 *
 * Created by sujith, karthik on 2/20/16.
 * AirlineMapper class reads the dataset and creates an AirlineDetails by performing sanity
 * Writes carrier, year and origin/destination as the key and the FlightDetails object as the
 * value to the context
 */

public class AirlineMapper extends Mapper<LongWritable, Text, Text, FlightDetails> {
    @Override
    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

        String line = value.toString();
        AirlineDetails details = AirlineDetails.createAirlineObject(line);
        if (details == null) return;

        String carrierAndYear = details.carrier+","+details.year;
        FlightDetails flightDetailsArr = createFlightObject(details,true);
        FlightDetails flightDetailsDep = createFlightObject(details,false);
        if (flightDetailsArr != null && flightDetailsDep != null) {
            // Writing to context once as origin and then as destination
            context.write(new Text(carrierAndYear+","+details.origin.trim()), flightDetailsArr);
            context.write(new Text(carrierAndYear+","+details.destination.trim()), flightDetailsDep);
        }
    }

    /***
     *
     * @param time
     * @return time in milliseconds
     */
    private static long getMilliSecsFromTime(int time) {
        int hoursPart = time / 100 ;
        int minutesPart = time % 100;
        long finalTime = (hoursPart * 60) + minutesPart;
        return finalTime * 60000;
    }

    /***
     *
     * @param flightDate
     * @return the epoch time
     */
    private static long getEpochTime(String flightDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        try {
            date = formatter.parse(flightDate);
        } catch (ParseException e) {
            System.err.println("Exception while getting epoch time: "+e);
            return 0L;
        }
        return date.getTime();
    }

    /***
     * Converts arrival and departure times to epoch times and checks for day rollovers
     * @param details
     * @param type
     * @return the FlightDetails object
     */
    private static FlightDetails createFlightObject(AirlineDetails details, Boolean type) {

        if (details == null || !details.isSane()) return null;

        /* Converting the values from the dataset to milliseconds and calculating the diff */
        long scheduledDepartureInMs = getMilliSecsFromTime(details.crsDepTime);
        long actualDepartureInMs = getMilliSecsFromTime(details.depTime);
        long scheduledArrivalInMs = getMilliSecsFromTime(details.crsArrTime);
        long actualArrivalInMs = getMilliSecsFromTime(details.arrTime);
        long scheduledElapsedInMs = details.crsElapsedTime * 60000;
        long actualElapsedInMs = details.actualElapsedTime * 60000;

        long scheduledDiff = scheduledArrivalInMs - scheduledDepartureInMs - scheduledElapsedInMs;
        long actualDiff = actualArrivalInMs - actualDepartureInMs - actualElapsedInMs;
        Boolean dayRollOver = false;
        long SIX_HOURS = -360*60000;
        // Day rollover is set to true if the difference between the times calculated above is less than negative six hours
        if (scheduledDiff <= SIX_HOURS || actualDiff <= SIX_HOURS) dayRollOver = true;

        long flightDateEpoch = getEpochTime(details.dateStr);
        if (flightDateEpoch == 0L) return null;
        long scheduledDepartureInEpoch = flightDateEpoch + scheduledDepartureInMs;
        long actualDepartureInEpoch = flightDateEpoch + actualDepartureInMs;
        long scheduledArrivalInEpoch = flightDateEpoch + scheduledArrivalInMs;
        long actualArrivalInEpoch = flightDateEpoch + actualArrivalInMs;
        // If day rollover occurs, add epoch time of one day to both scheduled and actual arrival
        if (dayRollOver) {
            long ONE_DAY = 24*60*60000;
            scheduledArrivalInEpoch += ONE_DAY;
            actualArrivalInEpoch += ONE_DAY;
        }
        if (type)  return new FlightDetails(true,scheduledArrivalInEpoch,actualArrivalInEpoch,details.cancelledSht);
        return new FlightDetails(false,scheduledDepartureInEpoch,actualDepartureInEpoch,details.cancelledSht );
    }
}
