/**
 *
 * Created by Karthik and Sujith on 2/10/16.
 */

import org.apache.commons.lang.text.StrTokenizer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class LeastMedianMapper extends Mapper<LongWritable, Text, Text, FloatWritable> {
    @Override
    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

        Configuration conf = context.getConfiguration();

        // Get the parameter from main
        String cheapestCarrier = conf.get("cheapestCarrier");
        String line = value.toString();
        StrTokenizer t = new StrTokenizer(line, ',','"');

        t.setIgnoreEmptyTokens(false);
        String[] columns = t.getTokenArray();

        // Filtering out the other carriers and retaining the cheapest carrier
        if (columns[8].equals(cheapestCarrier) && columns.length == 110 && Double.parseDouble(columns[109]) < 100000 && sanityTest(columns)) {

            String date = columns[5];
            int weekOfYear;
            try {
                weekOfYear = getWeekOfYear(date);
            } catch (ParseException e) {
                return;
            }

            DecimalFormat decimals = new DecimalFormat("00");
            //key would be airline code ,year and week of year
            String yearAndWeek = columns[0]+decimals.format(weekOfYear);


            float priceDollars = Float.parseFloat(columns[109]);

            context.write(new Text(yearAndWeek), new FloatWritable(priceDollars));
        }
    }

    private int getWeekOfYear(String date) throws ParseException {
        String format = "yyyy-MM-dd";
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        Date fligtDate = dateFormat.parse(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fligtDate);
        // Get the week of the year from the calendar
        return calendar.get(Calendar.WEEK_OF_YEAR);

    }


    private static boolean sanityTest(String[] line) {

        try {
            int crsArrTime = Integer.parseInt(line[40]);
            //converting to mins
            crsArrTime = (crsArrTime % 100) + (crsArrTime / 100) * 60;
            int crsDepTime = Integer.parseInt(line[29]);
            //converting to mins
            crsDepTime = (crsDepTime % 100) + (crsDepTime / 100) * 60;
            //check if CRS arrival and departure times are not 0
            if (crsArrTime != 0 && crsDepTime != 0) {
                int timezone = crsArrTime - crsDepTime - Integer.parseInt(line[50]);
                //check if timezone is a multiple of 60
                if (timezone % 60 == 0) {
                    if (Integer.parseInt(line[11]) > 0              //ORIGIN_AIRPORT_ID
                            && Integer.parseInt(line[12]) > 0       //ORIGIN_AIRPORT_SEQ_ID
                            && Integer.parseInt(line[13]) > 0       //ORIGIN_CITY_MARKET_ID
                            && Integer.parseInt(line[17]) > 0       //ORIGIN_STATE_FIPS
                            && Integer.parseInt(line[19]) > 0       //ORIGIN_WAC
                            && Integer.parseInt(line[20]) > 0       //DEST_AIRPORT_ID
                            && Integer.parseInt(line[21]) > 0       //DEST_AIRPORT_SEQ_ID
                            && Integer.parseInt(line[22]) > 0       //DEST_CITY_MARKET_ID
                            && Integer.parseInt(line[26]) > 0       //DEST_STATE_FIPS
                            && Integer.parseInt(line[28]) > 0) {    //DEST_WAC

                        if (!line[14].isEmpty()                     //ORIGIN
                                && !line[15].isEmpty()              //ORIGIN_CITY_NAME
                                && !line[16].isEmpty()              //ORIGIN_STATE_ABR
                                && !line[18].isEmpty()              //ORIGIN_STATE_NM
                                && !line[23].isEmpty()              //DEST
                                && !line[24].isEmpty()              //DEST_CITY_NAME
                                && !line[25].isEmpty()              //DEST_STATE_ABR
                                && !line[27].isEmpty()) {           //DEST_STATE_NM
                            if (Boolean.parseBoolean(line[47])) {                   //CANCELLED
                                int arrTime = Integer.parseInt(line[41]);           //ARR_TIME
                                arrTime = (arrTime % 100) + (arrTime / 100) * 60;
                                int depTime = Integer.parseInt(line[30]);           //DEP_TIME
                                depTime = (depTime % 100) + (depTime / 100) * 60;
                                int elapTime = Integer.parseInt(line[51]);          //ACTUAL_ELAPSED_TIME
                                int time = arrTime - depTime - elapTime
                                        - timezone;
                                if (time == 0) {
                                    double arrDelay = Double.parseDouble(line[42]); //ARR_DELAY
                                    double arrDelayMin = Double
                                            .parseDouble(line[43]);                 //ARR_DELAY_NEW
                                    if (arrDelay < 0) {
                                        if (arrDelayMin == 0) {
                                            return true;
                                        } else
                                            return false;
                                    } else if (arrDelay > 0) {
                                        if (arrDelay == arrDelayMin)
                                            return true;
                                        else
                                            return false;
                                    }

                                    if (arrDelayMin >= 15) {
                                        if (Double.parseDouble(line[44]) == 1)      //ARR_DEL15
                                            return true;
                                        else
                                            return false;
                                    }
                                }
                            } else
                                return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

}
