Author: Sujith Narayan Rudrapatna Prakash, Naveen Aswathanarayana, Ruinan Hu and Karthik Chandranna
Email: aswathanarayana.n@husky.neu.edu
Date created: April 9, 2016
Format used: A7 Readme Mehta_Khan_Sharma_Raje

Note: This version runs on eclipse IDE like pseudo-distributed mode with reading all the files from S3.

DESCRIPTION
The program ranks sorts the given climate data.

1. LIST OF FILES and FOLDERS PROVIDED:	
	1. Job.java
	2. JobSort.java
	3. JobTemperature.java 
	4. SortNodeMaster.java
	5. SortNodeServer.java
	6. SortNodeServer1.java
	7. WeatherDetails.java 
	8. bin-- Contains all the files required to create Client and SortNodes.
		
	
2. SYSTEM SPECIFICATION & REQUIREMENTS:
    1. Ubuntu 15.XX 64-bit, 8GB RAM
    2. Java 1.7.X
    3. Eclipse IDE
    4. Python 2.x (if not installed please install)
    5. Able to run Bash script
    6. AWS CLI version 1.10.x and higher
    7. AWS Eclipse toolkit (https://aws.amazon.com/eclipse/)
	8. Add these external jars to the project folder.
		1. aws-java-sdk-* all jars
		2. com.fasterxml.jackson.* all jars 
		3. commons-lang-2.6.jar
		4. guava-11.0.2.jar
		5. httpclient-*, httpcore-* all jars
		6. jackson-annotations-* jackson-core-* jackson-databind-* jackson-jaxrs-* jackson-mapper-asl-* jackson-xc-* all jars
		7. joda-time-*
		8. log4j-*
		9. org-apache-commons.logging-*
		
3. CONFIGURATIONS REQUIRED
    3.1 AWS Eclipse toolkit:
        Make sure you install AWS Eclipse toolkit for eclipse IDE.
		Detailed instruction is provided in the link (https://aws.amazon.com/eclipse)

4. STEPS TO RUN 
	4.1 Load all the src files into eclipse
	4.2 Add all the required external jar files mentioned below.	

5. OUTPUT
	5.1 Output will be displayed on the console of the eclipse.