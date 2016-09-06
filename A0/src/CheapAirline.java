import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import com.opencsv.CSVReader;

public class CheapAirline {

	public static void main(String[] args) throws IOException,
			FileNotFoundException {
		if (args.length != 1) {
			System.err.println("Usage: CheapAirline <input path>");
			System.exit(-1);
		}
		FileInputStream fis = new FileInputStream(args[0]);// "C:\\Users\\hp\\Downloads\\323.csv.gz"
		GZIPInputStream gis = new GZIPInputStream(fis);
		InputStreamReader isr = new InputStreamReader(gis);
		BufferedReader br = new BufferedReader(isr);
		CSVReader reader = new CSVReader(br);
		String[] nextLine;
		int lines = 0;
		int corruptLines = 0;
		HashMap<String, List<Double>> pricesMap = new HashMap<String, List<Double>>();
		while ((nextLine = reader.readNext()) != null) {
			lines++;
			String carrier;
			Double price;

			if (nextLine.length == 110) {
				if (sanityTest(nextLine)) {
					// extract and put to map
					carrier = nextLine[8];
					price = Double.parseDouble(nextLine[109]);
					if (pricesMap.containsKey(carrier)) {
						List<Double> value = pricesMap.get(carrier);
						value.add(price);
						pricesMap.put(carrier, value);
					} else {
						List<Double> value = new ArrayList<Double>();
						value.add(price);
						pricesMap.put(carrier, value);
					}

				} else {
					corruptLines++;
				}

			} else {
				corruptLines++;
			}

		}
		reader.close();
		System.out.println("K: " + corruptLines);
		System.out.println("F: " + (lines - corruptLines));		
		Map<String, Double> avgPriceMap = new HashMap<String, Double>();
		for (String key : pricesMap.keySet()) {
			List<Double> prices = pricesMap.get(key);
			Double pricesSum = 0.0;
			for (Double price : prices) {
				pricesSum += price;
			}
			avgPriceMap.put(key, pricesSum / prices.size());
		}
		Map<String, Double> sortedMap = sortByComparator(avgPriceMap);
		for (String key : sortedMap.keySet()) {
			Double avgPrice = sortedMap.get(key);
			System.out.println(key + " " + avgPrice);
		}
	}

	private static Map<String, Double> sortByComparator(
			Map<String, Double> unsortMap) {
		// Convert Map to List
		List<HashMap.Entry<String, Double>> list = new LinkedList<HashMap.Entry<String, Double>>(
				unsortMap.entrySet());

		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
			public int compare(Map.Entry<String, Double> o1,
					Map.Entry<String, Double> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		// Convert sorted map back to a Map
		Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
		for (Iterator<Map.Entry<String, Double>> it = list.iterator(); it
				.hasNext();) {
			Map.Entry<String, Double> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

	private static boolean sanityTest(String[] line) {

		int crsArrTime = Integer.parseInt(line[40]);
		crsArrTime = (crsArrTime%100) + (crsArrTime/100)*60;
		int crsDepTime = Integer.parseInt(line[29]);
		crsDepTime = (crsDepTime%100) + (crsDepTime/100)*60;		
		if (crsArrTime != 0 && crsDepTime != 0) {
			int timezone = crsArrTime - crsDepTime - Integer.parseInt(line[50]);
			if (timezone % 60 == 0) {
				if (Integer.parseInt(line[11]) > 0
						&& Integer.parseInt(line[12]) > 0
						&& Integer.parseInt(line[13]) > 0
						&& Integer.parseInt(line[17]) > 0
						&& Integer.parseInt(line[19]) > 0
						&& Integer.parseInt(line[20]) > 0
						&& Integer.parseInt(line[21]) > 0
						&& Integer.parseInt(line[22]) > 0
						&& Integer.parseInt(line[26]) > 0
						&& Integer.parseInt(line[28]) > 0) {

					if ((line[14] != null && !line[14].isEmpty())
							&& (line[15] != null && !line[15].isEmpty())
							&& (line[16] != null && !line[16].isEmpty())
							&& (line[18] != null && !line[18].isEmpty())
							&& (line[23] != null && !line[23].isEmpty())
							&& (line[24] != null && !line[24].isEmpty())
							&& (line[25] != null && !line[25].isEmpty())
							&& (line[27] != null && !line[27].isEmpty())) {
						if (line[47].equals("0")) {
							int arrTime = Integer.parseInt(line[41]);
							arrTime = (arrTime%100) + (arrTime/100)*60;
							int depTime = Integer.parseInt(line[30]);
							depTime = (depTime%100) + (depTime/100)*60;
							int elapTime = Integer.parseInt(line[51]);
							int time = arrTime - depTime - elapTime
									- timezone;
							if (time == 0) {
								double arrDelay = Double.parseDouble(line[42]);
								double arrDelayMin = Double
										.parseDouble(line[43]);
								if (arrDelay < 0) {
									if (arrDelayMin == 0) {
										return true;
									} else
										return false;
								}

								else if (arrDelay > 0) {
									if (arrDelay == arrDelayMin)
										return true;
									else
										return false;
								}

								if (arrDelayMin >= 15) {
									if (Double.parseDouble(line[44]) == 1)
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
		return false;
	}
}
