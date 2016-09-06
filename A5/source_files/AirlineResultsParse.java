import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * AirlineResultsParse combines all the reducer outputs and aggregates
 * connections and missed connections per carrier per year.
 * Created by Sujith, Karthik on 2/20/2016.
 */
public class AirlineResultsParse {
    public static void main(String[] args) throws IOException {
        HashMap<String, List<Long>> finalHash = new HashMap<>();
        File dir = new File(args[0]);
        File[] files = dir.listFiles();
        for (int i=0; i< files.length; i++) {
            if (!files[i].getName().startsWith("part-r")) continue;
            // Reading the file where the reducer output is stored
            BufferedReader br = new BufferedReader(new FileReader(files[i].getAbsolutePath()));
            String line;
            while ((line = br.readLine()) != null) {
                String[] lineArr = line.split(",");
                String carrierCode = lineArr[0];
                String year = lineArr[1];
                long connections = Long.parseLong(lineArr[3]);
                long missedConnections = Long.parseLong(lineArr[4]);
                // Key for the final hash is combination of carrierCode and year
                String carrierCodeAndYear = carrierCode + "," + year;
                if (finalHash.containsKey(carrierCodeAndYear)) {
                    // If hash contains the carrierCode and year, update the connections and missed connections
                    List<Long> connectionInfoList = finalHash.get(carrierCodeAndYear);
                    long updatedConnections = connectionInfoList.get(0) + connections;
                    long updatedMissedConnections = connectionInfoList.get(1) + missedConnections;
                    finalHash.put(carrierCodeAndYear, Arrays.asList(updatedConnections, updatedMissedConnections));
                }
                else finalHash.put(carrierCodeAndYear, Arrays.asList(connections, missedConnections));
            }
        }
        StringBuilder fileContent = new StringBuilder();
        // Printing the final output to a results file
        for (String key : finalHash.keySet()) {
            fileContent.append(key.split(",")[0]+",");
            fileContent.append(key.split(",")[1] + ",");
            fileContent.append(finalHash.get(key).get(0) + ",");
            fileContent.append(finalHash.get(key).get(1));
            fileContent.append("\n");
        }
        File file = new File("results.csv");
        if (!file.exists()) file.createNewFile();
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(fileContent.toString());
        bw.close();
    }
}
