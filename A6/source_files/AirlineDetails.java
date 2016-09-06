import org.apache.commons.lang.text.StrTokenizer;

import java.util.Arrays;

/**
 * This is the Airline Details Data class used to read from csv files and instantiate
 * AirlineDetails objects and sanitize them
 *
 * Created by sujith, karthik on 3/2/16.
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
    int isLeavingFromChicago;
    int isMostDelayedRoute;
    int isPopularSourceOrDestination;
    int isBusyDay;
    int flightNumber;
    String isDelayed;
    int isHoliday;

    /***
     * This is the workhorse method that instantiates an AirlineDetails object
     * from the training data set with only the necessary fields
     * @param data
     * @return
     */
    public static AirlineDetails createObject(String data) {
        try {
            AirlineDetails details = new AirlineDetails();

            StrTokenizer t = new StrTokenizer(data,',','"');
            t.setIgnoreEmptyTokens(false);
            String[] line = t.getTokenArray();

            if (line.length != 110) return null;

            double avgPrice = Double.parseDouble(line[109]);

            if (avgPrice >= 100000) return null;

            details.avgPrice = avgPrice;

            details.year = (int) Float.parseFloat(line[0]);
            details.quarter = (int) Float.parseFloat(line[1]);
            details.month = (int) Float.parseFloat(line[2]);
            details.day = (int) Float.parseFloat(line[3]);
            details.dayOfWeek = (int) Float.parseFloat(line[4]);
            String s1 = line[5].replaceAll("-","");
            details.date = Integer.parseInt(s1);

            details.carrier = line[8];
            details.tailNum = line[9];

            details.crsArrTime = (int) Float.parseFloat(line[40]); // see Final crs time 
            details.crsArrTimeHoursPart = details.crsArrTime / 100;
            details.crsArrTimeMinutesPart = details.crsArrTime % 100;

            details.finalCrsArrTime = (details.crsArrTimeHoursPart * 60) + details.crsArrTimeMinutesPart;

            details.crsDepTime = (int) Float.parseFloat(line[29]); // see final crs time
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

            details.arrTime = (int) Float.parseFloat(line[41]); // see final arr time
            details.arrTimeHoursPart = details.arrTime / 100;
            details.arrTimeMinutesPart = details.arrTime % 100;

            details.finalArrTime = (details.arrTimeHoursPart * 60) + details.arrTimeMinutesPart;

            details.depTime = (int) Float.parseFloat(line[30]); // see final dep time
            details.depTimeHoursPart = details.depTime / 100;
            details.depTimeMinutesPart = details.depTime % 100;

            details.finalDepTime = (details.depTimeHoursPart * 60) + details.depTimeMinutesPart;


            details.actualElapsedTime = (int) Float.parseFloat(line[51]);

            details.airTime = (int) Float.parseFloat(line[52]);

            details.arrDelay = (int) Float.parseFloat(line[42]);
            details.depDelay = (int) Float.parseFloat(line[31]);


            details.distance = (int) Float.parseFloat(line[54]);
            details.distanceGroup = (int) Float.parseFloat(line[55]);


            details.flightNumber = (int) Float.parseFloat(line[10]);

            if (details.arrDelay < 0) {
                details.arrDelayMins = 0;
            } else {
                details.arrDelayMins = (int)Float.parseFloat(line[43]);
            }
            details.arrDel15 = (int)Float.parseFloat(line[44]);

            // Synthesizing new fields for prediction
            details.isLeavingFromChicago = isLeavingFromChicago(details.originAirportId);
            details.isMostDelayedRoute = isMostDelayedRoute(details.originAirportId, details.destinationAirportId);
            details.isHoliday = isHoliday(line[5]);
            details.isBusyDay = isBusyDay(details.dayOfWeek);
            details.isPopularSourceOrDestination = isPopularSourceOrDestination(details.originAirportId, details.destinationAirportId);

            details.isDelayed = (details.arrDelay > 0)? "TRUE" : "FALSE";


            return details;
        } catch (Exception e) {
            return null;
        }
    }


    /***
     * This is the sanitizer method. It checks if the given airline object is sane
     * @return
     */
    public boolean isSane() {

        if (crsArrTime == 0 || crsDepTime == 0) return false;

        if (timeZone % 60 != 0) return false;

        if (originAirportId <= 0 || originAirportSequenceId <= 0 || originCityMarketId <=0
                || originStateFips <= 0 || originWac <= 0) return false;

        if (destinationAirportId <= 0 || destinationAirportSequenceId <= 0 || destinationCityMarketId <= 0
                || destinationStateFips <= 0 || destinationWac <= 0) return false;

        if (origin.isEmpty() || destination.isEmpty() || originCityName.isEmpty() || originStateName.isEmpty() ||
                destinationCityName.isEmpty() || destinationStateName.isEmpty()) return false;

        if (!cancelled) {
            int calcTime = finalArrTime - finalDepTime - actualElapsedTime - timeZone;
            if (calcTime != 0) return false;

            if (arrDelay > 0) {
                if (arrDelay != arrDelayMins) return false;
            }
            if (arrDelay < 0) {
                if (arrDelayMins != 0) return false;
            }
            if (arrDelayMins >= 15) {
                if (arrDel15 != 1) return false;
            }
        }

        return true;
    }

    /***
     * This method checks if flight leaves from Chicago
     * @param originAirportId
     * @return
     */
    public static int isLeavingFromChicago(int originAirportId) {
        // 13930 is the airport ID of Chicago ORD airport
        if (originAirportId == 13930) return 1;
        return 0;
    }

    /***
     * This method checks if the route is amongst the most frequently delayed routes
     * @param originAirportId
     * @param destinationAirportId
     * @return
     */
    public static int isMostDelayedRoute(int originAirportId, int destinationAirportId) {
        int[] source = {13930,13930,11292,13930,11423,13930,12992,13930,13930,13930,13930,12278,13930,13930,11641,11618,11986,13930,13930,13930,13930,13487,13930,13244,13930};
        int[] destin = {15412,12278,10372,10599,13930,12884,13930,11109,13029,13851,13796,13930,14747,14696,13930,14524,11298,11641,12945,13244,11337,11618,14783,13930,10408};

        for (int i=0; i<source.length; i++) {
            if (source[i] == originAirportId && destin[i] == destinationAirportId) {
                return 1;
            }
        }
        return 0;
    }

    /***
     * Checks if the flight is on a busy day of the week
     * @param dayOfWeek
     * @return
     */
    public static int isBusyDay(int dayOfWeek) {
        if (dayOfWeek == 4 || dayOfWeek == 5 || dayOfWeek == 6 || dayOfWeek == 7) return 1;
        return 0;
    }

    /***
     * Checks if the flight travels on a national holiday
     * @param date
     * @return
     */
    public static int isHoliday(String date) {
        String[] splitDate = date.split("-");
        int month = (int) Float.parseFloat(splitDate[0]);
        int day = (int) Float.parseFloat(splitDate[1]);

        int[] holidays = {1,20,45,48,146,150,154,241,283,311,327,360};

        int daysFromStart = month * 30 + day;

        for (int holiday: holidays) {
            if (Math.abs(daysFromStart - holiday) < 6) return 1;
        }
        return 0;

    }

    /***
     * Checks if the flight leaves from or goes to a popular airport
     * @param originAirportId
     * @param destinationAirportId
     * @return
     */
    public static int isPopularSourceOrDestination(int originAirportId, int destinationAirportId) {
        int[] popularDestinationIds = {10397,12892,13930,11298,12478,11292,14771,11067,12889,14107,12266,13303,14747,11618,13204};
        for (int i=0;i<popularDestinationIds.length;i++) {
            if (originAirportId == popularDestinationIds[i] || destinationAirportId == popularDestinationIds[i]) return 1;
        }
        return 0;
    }

    /***
     * This method stringifies the factors from the object required for prediction
     * @return
     */
    public String getFactors() {
        return  year +
                "," + quarter +
                "," + month +
                "," + day +
                "," + dayOfWeek +
                "," + date +
                "," + flightNumber +
                "," + originAirportId +
                "," + destinationAirportId +
                "," + finalCrsArrTime +
                "," + finalcrsDepTime +
                "," + crsElapsedTime +
                "," + carrier +
                "," + isLeavingFromChicago +
                "," + isHoliday +
                "," + isPopularSourceOrDestination +
                "," + isBusyDay +
                "," + distance +
                "," + isDelayed ;
    }

    /***
     * This is the workhorse method that instantiates an AirlineDetails object
     * from the test dataset with only the
     * necessary fields
     * @param data
     * @return
     */
    public static AirlineDetails createTestObject(String data) {

        try {
            AirlineDetails details = new AirlineDetails();

            StrTokenizer t = new StrTokenizer(data,',','"');
            t.setIgnoreEmptyTokens(false);
            String[] testLine = t.getTokenArray();

            details.year = (int) Float.parseFloat(testLine[1]);
            details.quarter = (int) Float.parseFloat(testLine[2]);
            details.month = (int) Float.parseFloat(testLine[3]);
            details.day = (int) Float.parseFloat(testLine[4]);
            details.dayOfWeek = (int) Float.parseFloat(testLine[5]);
            String s1 = testLine[6].replaceAll("-","");
            details.date = Integer.parseInt(s1);

            details.carrier = testLine[7];
            details.flightNumber = (int) Float.parseFloat(testLine[11]);

            details.originAirportId = (int) Float.parseFloat(testLine[12]); //

            details.destinationAirportId = (int) Float.parseFloat(testLine[21]); //

            details.crsDepTime = (int) Float.parseFloat(testLine[30]); //
            details.crsDepTimeHoursPart = details.crsDepTime / 100;
            details.crsDepTimeMinutesPart = details.crsDepTime % 100;

            details.finalcrsDepTime = (details.crsDepTimeHoursPart * 60) + details.crsDepTimeMinutesPart;

            details.crsArrTime = (int) Float.parseFloat(testLine[41]); //
            details.crsArrTimeHoursPart = details.crsArrTime / 100;
            details.crsArrTimeMinutesPart = details.crsArrTime % 100;

            details.finalCrsArrTime = (details.crsArrTimeHoursPart * 60) + details.crsArrTimeMinutesPart;
            details.crsElapsedTime = (int) Float.parseFloat(testLine[51]); //

            details.distance = (int) Float.parseFloat(testLine[55]); //

            details.isLeavingFromChicago = isLeavingFromChicago(details.originAirportId);
            details.isMostDelayedRoute = isMostDelayedRoute(details.originAirportId, details.destinationAirportId);
            details.isHoliday = isHoliday(testLine[6]);
            details.isBusyDay = isBusyDay(details.dayOfWeek);
            details.isPopularSourceOrDestination = isPopularSourceOrDestination(details.originAirportId, details.destinationAirportId);

            return details;
        } catch (Exception e) {
            System.err.println("Failed to parse the test line!");
            return null;
        }


    }
}
