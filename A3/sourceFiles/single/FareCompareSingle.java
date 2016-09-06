
/**
 *
 * Created by Karthik, Sujith on 1/13/2016.
 */

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.zip.GZIPInputStream;

import com.opencsv.CSVReader;

public class FareCompareSingle {

    public static void main(String[] args) throws Exception {

        Long startTime = System.currentTimeMillis();
        File folder = new File(args[0]);
        String type = args[1];
        int inputCount = Integer.parseInt(args[2]);
        List<Integer> validNumberOfFiles = Arrays.asList(5,10,15,20,25);

        if (!validNumberOfFiles.contains(inputCount)) {
            System.err.println("Please enter a valid count of number of input files");
            System.exit(1);
        }

        if (!(type.equals("mean")|| type.equals("median"))) {
            System.err.println("Please enter mean or median as the second argument");
            System.exit(1);
        }
        File[] files = folder.listFiles();
        HashMap<String, List<Float>> nameWithAllPrices = new HashMap<>();
        int i=0;
        for (File file : files) {
            if(i<inputCount) {
                FileInputStream fileInputStream = new FileInputStream(file.getAbsolutePath());
                GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
                InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream);
                BufferedReader br = new BufferedReader(inputStreamReader);

                CSVReader reader = new CSVReader(br);
                String[] line = null;

                reader.readNext(); // Ignore the header line

                while ((line = reader.readNext()) != null) {

                    if (isCorruptLine(line) || !isSanityPass(line)) {
                        continue;
                    }


                    List<Float> listOfPrices;
                    String carrierMonth = line[6] + line[2];
                    String price = line[109];
                    if (nameWithAllPrices.containsKey(carrierMonth)) {
                        listOfPrices = nameWithAllPrices.get(carrierMonth);
                    } else {
                        listOfPrices = new ArrayList<>();
                    }
                    listOfPrices.add(Float.parseFloat(price));
                    nameWithAllPrices.put(carrierMonth, listOfPrices);
                }
                i++;
            }
            else
                break;
        }

        for (String key : nameWithAllPrices.keySet()) {
            List<Float> listOfPrices = nameWithAllPrices.get(key);
            float value = 0.0f;
            if(type.equals("median"))
                value = getMedian(listOfPrices);
            else
                value = getAvgPrice(listOfPrices);
            String carrier = key.substring(0,2);
            String month = key.substring(2);
            DecimalFormat df = new DecimalFormat("#.##");
            System.out.println(carrier+" "+month+" "+df.format(value));
        }

        Long timeTaken = (System.currentTimeMillis()-startTime)/1000;

        String timeFileName = "timeFile_single_"+type+".txt";
        File timeFile = new File(timeFileName);

        if (!timeFile.exists()) {
            timeFile.createNewFile();
        }

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(timeFileName, true), "utf-8"))) {
            writer.write(timeTaken.toString());
            if (inputCount!=25) {
                writer.write(",");
            }
        }
    }

    private static float getAvgPrice(List<Float> listOfPrices) {
        Float sum = 0.0f;
        if (!listOfPrices.isEmpty()) {
            for (Float price : listOfPrices) {
                sum += price;
            }
            return sum / listOfPrices.size();
        }
        return 0.0f;

    }

    private static float getMedian(List<Float> prices) {
        Collections.sort(prices);
        Float median = 0.0f;

        if (prices.size() % 2 == 0)
            //average of middle 2 elements if the list is even
            median = ((float) prices.get(prices.size() / 2) + (float) prices.get(prices.size() / 2 - 1)) / 2;
        else
            //taking the median when the list is odd
            median = (float) prices.get(prices.size() / 2);
        return median;

    }

    private static boolean isSanityPass(String[] line) {

        try {
            double crsArrTime = Double.parseDouble(line[40]);
            double crsDepTime = Double.parseDouble(line[29]);
            double crsElapsedTime = Double.parseDouble(line[50]);
            double timeZone = (crsArrTime - crsDepTime - crsElapsedTime);
            int airportId = Integer.parseInt(line[11]);
            int airportSequenceId = Integer.parseInt(line[12]);
            int cityMarketId = Integer.parseInt(line[13]);
            int stateFips = Integer.parseInt(line[17]);
            int wac = Integer.parseInt(line[19]);
            String origin = line[14];
            String destination = line[23];
            String cityName = line[15];
            String stateName = line[18];
            boolean cancelled = Boolean.parseBoolean(line[47]);

            double arrTime = Double.parseDouble(line[41]);
            double depTime = Double.parseDouble(line[30]);
            double actualElapsedTime = Double.parseDouble(line[51]);
            double arrDelay = Double.parseDouble(line[42]);

            double arrDelayMins;
            if (arrDelay < 0.0) {
                arrDelayMins = 0.0;
            } else {
                arrDelayMins = arrDelay;
            }
            boolean arrDel15 = Boolean.parseBoolean(line[44]);

            if (crsArrTime == 0.0 || crsDepTime == 0.0) {
                return false;
            }

            if (airportId <= 0.0 || airportSequenceId <= 0.0 || cityMarketId <= 0.0 || stateFips <= 0.0 || wac <= 0.0) {
                return false;
            }
            if (origin.isEmpty() || destination.isEmpty() || cityName.isEmpty() || stateName.isEmpty()) {
                return false;
            }
            if (cancelled) {
                if ((arrTime - depTime - actualElapsedTime - timeZone) != 0.0) {
                    return false;
                }
                if (arrDelay > 0.0) {
                    if (arrDelay != arrDelayMins) {
                        return false;
                    }
                }
                if (arrDelay < 0.0) {
                    if (arrDelayMins != 0) {
                        return false;
                    }
                }
                if (arrDelayMins >= 15.0) {
                    if (!arrDel15) {
                        return false;
                    }
                }
                return true;
            }
        } catch (NumberFormatException e) {
            return true;
        }

        return true;
    }

    private static boolean isCorruptLine(String[] line) {
        if (line.length != 110) {
            return true;
        }
        return false;
    }

}

