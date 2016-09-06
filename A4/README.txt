README:

The submission tar file contains the following:

1) JAR file of the pseudo-distributed implementation of the application
2) A Makefile that contains the targets to run the above application.
3) A report that has the R Script and the R Graph that compares the execution times of the above implementations.
4) Source files


Execution Instructions:
Step 1: Change Directory (cd) to the path of the Makefile.

Step 2: hadoop jar mr1.jar <path to dataset> <output directory>
		This executes the first mapreduce program. The arguments are the path to dataset and the output directory. The output of the reducer will contain the carrier, year, slope and intercepts.

Step 3: hadoop fs -get <same output directory> .
		This gets the output directory to the local path

Step 4: java -jar mrLink.jar 200 <same output directory>
		This processes the reducer output and finds the cheapest airline 
		
Step 5: hadoop fs -put cheapest_airline.txt .
		Upload the file containing the cheapest airline to hadoop.
		
	
Step 6: hadoop jar mr2.jar input/data $(FINAL_OUTPUT) cheapest_airline.txt
		File containing the cheapest airline is used by hadoop to generate weekly medians

Step 7: hadoop fs -getmerge $(FINAL_OUTPUT) results.txt
		All the result files are merged
		

Alternatively, you can run the "make pseudo" from the location of the Makefile.