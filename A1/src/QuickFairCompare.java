import com.opencsv.CSVReader;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

/**
 * Created by SujithNarayan on 1/22/2016.
 */

class FareCompareParallel implements Runnable {

    public static AtomicInteger totalLines = new AtomicInteger();
    public static AtomicInteger numberOfCorruptLines = new AtomicInteger();
    private String fileName;

    public static ConcurrentHashMap<String,List<Float>> nameWithAllPrices = new ConcurrentHashMap<>();

    public static Thread t;

    public static List<Thread> threads = new ArrayList<Thread>();


    FareCompareParallel(String fileName, int count) throws InterruptedException {
        this.fileName = fileName;
        count++;
        String threadName = "Thread-"+count;
        t = new Thread(this,threadName);    // Create the new thread
        threads.add(t);
        t.start();  // Start the new thread
    }

    @Override
    public void run() {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(this.fileName);
        } catch (FileNotFoundException e) {
            System.out.println("File input stream error");
        }
        GZIPInputStream gzipInputStream = null;
        try {
            gzipInputStream = new GZIPInputStream(fileInputStream);
        } catch (IOException e) {
            System.out.println("IO EXCEPTION in gzip");
        }
        InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream);
        BufferedReader br = new BufferedReader(inputStreamReader);


        CSVReader reader = new CSVReader(br);
        String[] line = null;

        try {
            reader.readNext(); // Ignore the header line
        } catch (IOException e) {
            System.out.println("IO EXCEPTION in reading header");
        }

        try {
            while ((line = reader.readNext()) != null) {

                totalLines.incrementAndGet();

                if (isCorruptLine(line) || !isSanityPass(line)) {
                    numberOfCorruptLines.incrementAndGet();
                    continue;
                }

                List<Float> listOfPrices;
                try {
                    String carrierMonth = line[6]+line[2];
                    String price = line[109];
                    String year = line[0];
                    String month = line[2];

                    if (nameWithAllPrices.containsKey(carrierMonth)) {
                        listOfPrices = nameWithAllPrices.get(carrierMonth);
                    }
                    else {
                        listOfPrices = new ArrayList<>();
                    }
                    listOfPrices.add(Float.parseFloat(price));
                    nameWithAllPrices.put(carrierMonth, listOfPrices);
                } catch(ArrayIndexOutOfBoundsException e) {
                    continue;
                }


            }

        } catch (Exception e) {
            System.out.println("IO EXCEPTION in reading body line");
        }
    }

    private static boolean isSanityPass(String[] line) {

        try {
            int crsArrTime = Integer.parseInt(line[40]);
            int crsArrTimeHoursPart = crsArrTime / 100 ;
            int crsArrTimeMinutesPart = crsArrTime % 100;

            int finalCrsArrTime = (crsArrTimeHoursPart * 60) + crsArrTimeMinutesPart;

            int crsDepTime = Integer.parseInt(line[40]);
            int crsDepTimeHoursPart = crsDepTime / 100 ;
            int crsDepTimeMinutesPart = crsDepTime % 100;

            int finalcrsDepTime = (crsDepTimeHoursPart * 60) + crsDepTimeMinutesPart;

            int crsElapsedTime = Integer.parseInt(line[50]);

            int timeZone = (finalCrsArrTime - finalcrsDepTime - crsElapsedTime);

            int originAirportId = Integer.parseInt(line[11]);
            int originAirportSequenceId = Integer.parseInt(line[12]);
            int originCityMarketId = Integer.parseInt(line[13]);
            String origin = line[14];
            String originCityName = line[15];
            int originStateFips = Integer.parseInt(line[17]);
            String originStateName = line[18];
            int originWac = Integer.parseInt(line[19]);

            int destinationAirportId = Integer.parseInt(line[20]);
            int destinationAirportSequenceId = Integer.parseInt(line[21]);
            int destinationCityMarketId = Integer.parseInt(line[22]);
            String destination = line[23];
            String destinationCityName = line[24];
            int destinationStateFips = Integer.parseInt(line[26]);
            String destinationStateName = line[27];
            int destinationWac = Integer.parseInt(line[28]);


            boolean cancelled = Boolean.parseBoolean(line[47]);

            int arrTime = Integer.parseInt(line[41]);
            int arrTimeHoursPart = arrTime / 100 ;
            int arrTimeMinutesPart = arrTime % 100;

            int finalArrTime = (arrTimeHoursPart * 60) + arrTimeMinutesPart;

            int depTime = Integer.parseInt(line[30]);
            int depTimeHoursPart = depTime / 100 ;
            int depTimeMinutesPart = depTime % 100;

            int finalDepTime = (depTimeHoursPart * 60) + depTimeMinutesPart;


            int actualElapsedTime = Integer.parseInt(line[51]);

            int arrDelay = Integer.parseInt(line[42]);

            double arrDelayMins;
            if (arrDelay < 0) {
                arrDelayMins = 0;
            }
            else {
                arrDelayMins = arrDelay;
            }
            boolean arrDel15 = Boolean.parseBoolean(line[44]);

            if (crsArrTime == 0 || crsDepTime == 0) {
                return false;
            }

            if (timeZone % 60 != 0) {
                return false;
            }

            if (originAirportId <= 0 || originAirportSequenceId <= 0 || originAirportSequenceId <= 0 || originStateFips <= 0 || originWac <= 0) {
                return false;
            }
            if (destinationAirportId <= 0 || destinationAirportSequenceId <= 0 || destinationAirportSequenceId <= 0 || destinationStateFips <= 0 || destinationWac <= 0) {
                return false;
            }
            if (origin.isEmpty() || destination.isEmpty() || originCityName.isEmpty() || originStateName.isEmpty() ||
                    destinationCityName.isEmpty() || destinationStateName.isEmpty()) {
                return false;
            }
            if (!cancelled) {
                if ((finalArrTime - finalDepTime - actualElapsedTime - timeZone) != 0) {
                    return false;
                }
                if (arrDelay > 0) {
                    if (arrDelay != arrDelayMins) {
                        return false;
                    }
                }
                if (arrDelay < 0) {
                    if (arrDelayMins != 0) {
                        return false;
                    }
                }
                if (arrDelayMins >= 15) {
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
        if (line.length != 110 || Double.parseDouble(line[109]) > 999999) {
            return true;
        }
        return false;
    }

}

public class QuickFairCompare {
    public static void main(String[] args) throws IOException, InterruptedException {

        // Check command line arguments

        Long startTime = System.currentTimeMillis();

        if (args.length != 3) {
            System.out.println("Supply correct number of command line arguments");
            System.exit(-1);
        }

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

        System.out.println("Executing...");

//        File fileObj = new File(args[0]);
        int count = 0;

        // Get the list of files and call constructors for each
        for (File f : folder.listFiles()) {
            if (count < inputCount) {
                new FareCompareParallel(f.getAbsolutePath(),count);
                count++;
            }
            else {
                break;
            }
        }

        // Wait for all threads to complete execution
        for (Thread thread: FareCompareParallel.threads) thread.join();


        HashMap<String, List<Float>> nameWithPricesNonConcurrent = new HashMap<>();

        // Hashmap having airline names as keys and a list of prices as values
        nameWithPricesNonConcurrent.putAll(FareCompareParallel.nameWithAllPrices);

        for (String key: nameWithPricesNonConcurrent.keySet()) {
            float value;
            if(type.equals("median"))
                value = getMedian(nameWithPricesNonConcurrent.get(key));
            else
                value = getAvgPrice(nameWithPricesNonConcurrent.get(key));
            //double avgPrice = getAvgPrice(nameWithPricesNonConcurrent.get(key));
            String carrier = key.substring(0,2);
            String month = key.substring(2);
            DecimalFormat df = new DecimalFormat("#.##");
            System.out.println(carrier+" "+month+" "+df.format(value));
        }

        Long timeTaken = (System.currentTimeMillis()-startTime)/1000;

        String timeFileName = "timeFile_multi_"+type+".txt";
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
            listOfPrices.removeAll(Collections.singleton(null));
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
        if (!prices.isEmpty()) {
            prices.removeAll(Collections.singleton(null));
        }
        if (prices.size() % 2 == 0)
            //average of middle 2 elements if the list is even
            median = ((float) prices.get(prices.size() / 2) + (float) prices.get(prices.size() / 2 - 1)) / 2;
        else
            //taking the median when the list is odd
            median = (float) prices.get(prices.size() / 2);
        return median;

    }

}