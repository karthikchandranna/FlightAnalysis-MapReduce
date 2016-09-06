Author: Sujith Narayan Rudrapatna Prakash, Naveen Aswathanarayana, Ruinan Hu and Karthik Chandranna
Email: aswathanarayana.n@husky.neu.edu
Date created: March 27, 2016
Format used: A7 Readme Mehta_Khan_Sharma_Raje

NOTE: The make file runs the program for both values of N, 1 and 200 so the execution time seen is combined execution time.

DESCRIPTION
The program ranks carriers and plots the evolution of prices of the least expensive carrier over time.

1. LIST OF FILES and FOLDERS PROVIDED:	
	1.  A A8_CheapestAirline.gz file. Unzip this folder. The folder unpacks it to a A8_CheapestAirline folder. This folder contains the following files
	2   README.txt 
	3.  Makefile 
	4.  A8_Report.pdf 
    5.  AirlineDetails.java
    6.  CheapestAirline.scala 
    7.  build.sbt
    8.  sparkConfig.json
    9.  bin (nodeips.py, step.py, Step.sh, stepid.py)
 
	
3. SYSTEM SPECIFICATION & REQUIREMENTS:
    1. Ubuntu 15.XX 64-bit, 8GB RAM
    2. Java 1.7.X
    3. sbt 
      Linux: http://www.scala-sbt.org/release/docs/Installing-sbt-on-Linux.html
      Mac: http://www.scala-sbt.org/release/docs/Installing-sbt-on-Mac.html
    4. Python 2.x (if not installed please install)
    5. Able to run Bash script
    6. AWS CLI version 1.10.x and higher
    7. Apache Hadoop v.2.6.3 (http://www-us.apache.org/dist/hadoop/common/hadoop-2.6.3/hadoop-2.6.3.tar.gz)
    8. Scala v.2.10.6 (http://www.scala-lang.org/download/2.10.6.html)
    9. Apache Spark v.1.6.0 (http://www-eu.apache.org/dist/spark/spark-1.6.0/spark-1.6.0-bin-hadoop2.6.tgz)


4. CONFIGURATIONS REQUIRED

    4.1 AWS CLI version:
        Make sure you have AWS CLI version greater than 1.10, if you version is below as mentioned please perform following steps to upgrade to CLI version.

        $ curl "https://s3.amazonaws.com/aws-cli/awscli-bundle.zip" -o "awscli-bundle.zip"
        $ unzip awscli-bundle.zip
        $ sudo ./awscli-bundle/install -i /usr/local/aws -b /usr/local/bin/aws

    4.2 AWS CLI configured to output JSON:
        Make sure aws output format is set to JSON, since we use custom python script to track the progress of MR job on cluster. 

        Aws configure command should output this.

        $aws configure
        AWS Access Key ID [****************5FXQ]:
        AWS Secret Access Key [****************c+Wz]:
        Default region name [us-east-1]:
        Default output format [json]:

        If Default output format is not in json, run $aws configure command and type json when you encounter "Default output format [text/xml]:"
        Default output format [text]:json

    4.3 AWS CLI configured to region us-east-1

5. CHANGES IN MAKEFILE
    5.1 Change - YourBucketName => to the name you want for you bucket
    5.2 Change - Key => Update only the filename of the secret key saved as .pem in Makefile to the constant “Key”.
    5.3 Change - PATHTOKEY => update the complete path of the secret key file saved as .pem in Makefile to the constant “PATHTOKEY”
    5.4 Change - CLUSTER_ID => copy the cluster id after running the make cluster command to makefile constant "CLUSTER_ID"

6. STEPS TO BUILD
    6.1 make build //to build the spark jar using sbt.

7. STEPS TO RUN 
	7.1 For Running in AWS Cloud:
        NOTE: Please make sure you create the Bucket in the same zone as the EMR machine.Otherwise, it'll fail
        7.1.1 make cluster    // create Bucket
		7.1.2 make step       // runs the AWS Pipeline		

8. OUTPUT
	8.1 For Cloud: Two files are generated output_1 and output_200, these files has the weekly median price for cheapest airline carriers.