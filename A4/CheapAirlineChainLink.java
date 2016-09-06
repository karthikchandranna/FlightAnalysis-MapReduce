import java.io.*;
import java.util.*;

/**
 *
 * Created by sujith , karthik on 2/13/16.
 *
 * This class reads the reducer output from the first MR job and computes the cheapest airline
 */
public class CheapAirlineChainLink {
    public static void main(String[] args) throws IOException {

        // TreeMap is used to maintain the natural ordering of keys
        HashMap<String, TreeMap<Double,String>> yearWiseHash = new HashMap<>();


        // N value : 1 or 200
        String NStr = args[0];
        Double N = Double.parseDouble(NStr);

        // Read the combined reducer op file
        File dir = new File(args[1]);
        File[] files = dir.listFiles();

        System.out.println(files[0].getName());
        for (int i = 0; i < files.length; i++) {

            if (!files[i].getName().startsWith("part-r")) {
                continue;
            }

            BufferedReader br = new BufferedReader(new FileReader(files[i].getAbsolutePath()));

            String reducerOpLine;

            while ((reducerOpLine = br.readLine()) != null) {
                String[] reducerOpLineSplit = reducerOpLine.split("\\s+");

                // Get the airline code and year from the key
                String carrierInfo = reducerOpLineSplit[0];
                String airline = carrierInfo.split(",")[0];
                String year = carrierInfo.split(",")[1];

                // Get the slope and intercept from the value
                String slopeAndIntercept = reducerOpLineSplit[1];
                String[] slopeAndInterceptSplit = slopeAndIntercept.split(",");
                String interceptStr = slopeAndInterceptSplit[0];
                String slopeStr = slopeAndInterceptSplit[1];

                Double intercept = Double.parseDouble(interceptStr);
                Double slope = Double.parseDouble(slopeStr);


                Double calculatedValue = intercept + (slope * N);


                if (yearWiseHash.containsKey(year)) {
                    TreeMap<Double, String> innerHash = yearWiseHash.get(year);
                    innerHash.put(calculatedValue, airline);
                    yearWiseHash.put(year, innerHash);
                } else {
                    TreeMap<Double, String> airlineAndN = new TreeMap<>();
                    airlineAndN.put(calculatedValue, airline);
                    yearWiseHash.put(year, airlineAndN);
                }
            }
        }

        Map<String, Integer> results = new HashMap<>();

        for(Map.Entry<String, TreeMap<Double,String>> entry : yearWiseHash.entrySet()){
            TreeMap<Double,String> innerMap = entry.getValue();
            String cheapAirline = innerMap.firstEntry().getValue();
            if(results.containsKey(cheapAirline)){
                Integer wins = results.get(cheapAirline);
                wins+=1;
                results.put(cheapAirline,wins);
            }
            else{
                results.put(cheapAirline,1);
            }
        }

        Map<String, Integer> sortedResults = sortByComparator(results);

        String cheapestAirline = sortedResults.entrySet().iterator().next().getKey();

        PrintWriter writer = new PrintWriter("cheapest_airline.txt", "UTF-8");
        writer.print(cheapestAirline);
        writer.close();

    }

    private static Map<String, Integer> sortByComparator(Map<String, Integer> unsortMap) {

        // Convert Map to List
        List<Map.Entry<String, Integer>> list =
                new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());

        // Sort list with comparator, to compare the Map values
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        // Convert sorted map back to a Map
        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Iterator<Map.Entry<String, Integer>> it = list.iterator(); it.hasNext();) {
            Map.Entry<String, Integer> entry = it.next();
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }
}
