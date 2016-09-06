import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * <h1>Java Utility</h1>
 * The Java program is used to sanitize the airline data from the given dataset to find the cheapest airline and 
 * further calculates its weekly median price for all the years.
 *
 * @author: Sujith, Karthik, Naveen, Ruinan
 * @date: March 25, 2016
 */
public class AirlineDetails {

    int year;
    int crsArrTime;
    int crsArrTimeHoursPart;
    int crsArrTimeMinutesPart;
    int finalCrsArrTime;
    int crsDepTime;
    int crsDepTimeHoursPart;
    int crsDepTimeMinutesPart;
    int finalcrsDepTime;
    int crsElapsedTime;
    int timeZone;

    int originAirportId;
    int originAirportSequenceId;
    int originCityMarketId;
    String origin;
    String originCityName;
    int originStateFips;
    String originStateName;
    int originWac;

    int destinationAirportId;
    int destinationAirportSequenceId;
    int destinationCityMarketId;
    String destination;
    String destinationCityName;
    int destinationStateFips;
    String destinationStateName;
    int destinationWac;

    boolean cancelled;
    int arrTime;
    int arrTimeHoursPart;
    int arrTimeMinutesPart;
    int finalArrTime;
    int depTime;
    int depTimeHoursPart;
    int depTimeMinutesPart;
    int finalDepTime;
    int actualElapsedTime;
    int arrDelay;
    int arrDelayMins;
    int arrDel15;
    double avgPrice;
    int quarter;
    int month;
    int day;
    int dayOfWeek;
    int date;
    String carrier;
    String tailNum;
    int originStateId;
    int destinationStateId;
    boolean divert;
    int airTime;
    int depDelay;
    int distance;
    int distanceGroup;
    int flightNumber;

    
    
    /**
     * @name: validate
     * @description: validate the data based on the requirements specified which 
     * indicates that this is sane record. 
     * @param: String[] line -- record containing details of each flight.
     * @return: boolean -- indicating valid or invalid record.
     */
    public static boolean validate(String[] line) {
        try {
            AirlineDetails details = new AirlineDetails();
            if (line.length != 110) return false;
            double avgPrice = Double.parseDouble(line[109]);
            if (avgPrice >= 100000) return false;
            details.avgPrice = avgPrice;
            details.year = (int) Float.parseFloat(line[0]);
            details.quarter = (int) Float.parseFloat(line[1]);
            details.month = (int) Float.parseFloat(line[2]);
            details.day = (int) Float.parseFloat(line[3]);
            details.dayOfWeek = (int) Float.parseFloat(line[4]);
            String s1 = line[5].replaceAll("-","");
            details.date = Integer.parseInt(s1);
            details.carrier = line[8];
            details.crsArrTime = (int) Float.parseFloat(line[40]);  
            details.crsArrTimeHoursPart = details.crsArrTime / 100;
            details.crsArrTimeMinutesPart = details.crsArrTime % 100;
            details.finalCrsArrTime = (details.crsArrTimeHoursPart * 60) + details.crsArrTimeMinutesPart;
            details.crsDepTime = (int) Float.parseFloat(line[29]); 
            details.crsDepTimeHoursPart = details.crsDepTime / 100;
            details.crsDepTimeMinutesPart = details.crsDepTime % 100;
            details.finalcrsDepTime = (details.crsDepTimeHoursPart * 60) + details.crsDepTimeMinutesPart;
            details.crsElapsedTime = (int) Float.parseFloat(line[50]);
            details.timeZone = (details.finalCrsArrTime - details.finalcrsDepTime - details.crsElapsedTime);
            details.originAirportId = (int) Float.parseFloat(line[11]);
            details.originAirportSequenceId = (int) Float.parseFloat(line[12]);
            details.originCityMarketId = (int) Float.parseFloat(line[13]);
            details.origin = line[14];
            details.originCityName = line[15];
            details.originStateName = line[18];
            details.originStateId = (int) Float.parseFloat(line[17]);
            details.originStateFips = (int) Float.parseFloat(line[17]);
            details.originWac = (int) Float.parseFloat(line[19]);
            details.destinationAirportId = (int) Float.parseFloat(line[20]);
            details.destinationAirportSequenceId = (int) Float.parseFloat(line[21]);
            details.destinationCityMarketId = (int) Float.parseFloat(line[22]);
            details.destination = line[23];
            details.destinationCityName = line[24];
            details.destinationStateName = line[27];
            details.destinationStateId = (int) Float.parseFloat(line[26]);
            details.destinationStateFips = (int) Float.parseFloat(line[26]);
            details.destinationWac = (int) Float.parseFloat(line[28]);
            details.cancelled = Boolean.parseBoolean(line[47]);
            details.divert = Boolean.parseBoolean(line[49]);
            details.arrTime = (int) Float.parseFloat(line[41]); 
            details.arrTimeHoursPart = details.arrTime / 100;
            details.arrTimeMinutesPart = details.arrTime % 100;
            details.finalArrTime = (details.arrTimeHoursPart * 60) + details.arrTimeMinutesPart;
            details.depTime = (int) Float.parseFloat(line[30]); 
            details.depTimeHoursPart = details.depTime / 100;
            details.depTimeMinutesPart = details.depTime % 100;
            details.finalDepTime = (details.depTimeHoursPart * 60) + details.depTimeMinutesPart;
            details.actualElapsedTime = (int) Float.parseFloat(line[51]);
            details.airTime = (int) Float.parseFloat(line[52]);
            details.arrDelay = (int) Float.parseFloat(line[42]);
            details.depDelay = (int) Float.parseFloat(line[31]);

            if (details.arrDelay < 0) {
                details.arrDelayMins = 0;
            } else {
                details.arrDelayMins = (int)Float.parseFloat(line[43]);
            }
            details.arrDel15 = (int)Float.parseFloat(line[44]);
            return isSane(details);
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * @name: isSane
     * @description: Perform all the sanity checks for each flight data.
     * @param: AirlineDetails details -- object containing details of each flight.
     * @return: boolean -- indicating valid or invalid record.
     */
    public static boolean isSane(AirlineDetails details) {

        if (details.crsArrTime == 0 || details.crsDepTime == 0) return false;

        if (details.timeZone % 60 != 0) return false;

        if (details.originAirportId <= 0 || details.originAirportSequenceId <= 0 || details.originCityMarketId <=0
                || details.originStateFips <= 0 || details.originWac <= 0) return false;

        if (details.destinationAirportId <= 0 || details.destinationAirportSequenceId <= 0 || details.destinationCityMarketId <= 0
                || details.destinationStateFips <= 0 || details.destinationWac <= 0) return false;

        if (details.origin.isEmpty() || details.destination.isEmpty() || details.originCityName.isEmpty() || details.originStateName.isEmpty() || details.destinationCityName.isEmpty() || details.destinationStateName.isEmpty()) return false;

        if (!details.cancelled) {
            int calcTime = details.finalArrTime - details.finalDepTime - details.actualElapsedTime - details.timeZone;
            if (calcTime != 0) return false;

            if (details.arrDelay > 0) {
                if (details.arrDelay != details.arrDelayMins) return false;
            }
            if (details.arrDelay < 0) {
                if (details.arrDelayMins != 0) return false;
            }
            if (details.arrDelayMins >= 15) {
                if (details.arrDel15 != 1) return false;
            }
        }

        return true;
    }

    /**
     * @name: getWeekOfYear
     * @description: Based on the date given this function returns the week number.
     * @param: String date -- string containing the date field in yyyy-MM-dd format.
     * @return: int -- integer representing the week number to which the date belong. 
     */
    public static int getWeekOfYear(String date) throws ParseException {
        String format = "yyyy-MM-dd";
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        Date fligtDate = dateFormat.parse(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fligtDate);
        // Get the week of the year from the calendar
        return calendar.get(Calendar.WEEK_OF_YEAR);

    }
}
