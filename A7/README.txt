README

Required Software

1) Python 2.x (if not installed please install)
2) Hadoop 2.5.x and higher
3) Able to run Bash script
4) AWS CLI version 1.10.x and higher

Configurations

1) AWS CLI version:
Make sure you have AWS CLI version greater than 1.10, if you version is below as mentioned please perform following steps to upgrade to CLI version.

$ curl "https://s3.amazonaws.com/aws-cli/awscli-bundle.zip" -o "awscli-bundle.zip"
$ unzip awscli-bundle.zip
$ sudo ./awscli-bundle/install -i /usr/local/aws -b /usr/local/bin/aws

2) AWS CLI configured to output JSON:
Make sure aws output format is set to JSON, since we use custom python script to track the progress of MR job on cluster. 

Aws configure command should output this.

$aws configure
AWS Access Key ID [****************5FXQ]:
AWS Secret Access Key [****************c+Wz]:
Default region name [us-east-1]:
Default output format [json]:

If Default output format is not in json, run $aws configure command and type json when you encounter "Default output format [text/xml]:"
Default output format [text]:json

Running the MR job on AWS

Step 1: Compile source code
Requirements: Set path variables for the local hadoop like $HADOOP and $PATH
Change in Makefile: none
Cmd: make Build -- will build the class files and generate Jar file which will be uploaded to AWS.


Step 2: Create Cluster
Requirements: AWS CLI version 1.10.x and higher
Changes in Makefile:
 a) provide name of the bucket through the constant “YourBucketName”
 b) update the complete path of the secret key file saved as .pem in Makefile to the constant “PATHTOKEY”
 c) Update only the filename of the secret key saved as .pem in Makefile to the constant “Key”.
Cmd: make Cluster -- will create the cluster on AWS and start provisioning.

Configuration Change for running on Master node.
----------------------------------------------------------------
Change the security policy for Master node, to login and run spark program.
Requirements: Follow the configuration setting to enable master node to login and run spark.
Go to the aws console click on the EC2 link form the main dashboard you will be directed to EC2 dashboard. 
On the left section Network and security group section click on "Security Group"
Select the master nodes and click on Inbound tab below and click on edit.
Edit inbound rules window -->Click on Add Rule ? select SSH ? select Source Anywhere.
Click save

Step 3: Running MR Job
Requirements: none
Change in Makefile: copy the cluster id to makefile constant "CLUSTER_ID"
Cmd: make Step -- this will run the mapreduce job on the created cluster. wait till the cluster completes the job. Status of the job can be seen on the shell screen. Once the MR job is done, make Step will login into the master node of the cluster and run the spark script to do the prediction. You can find result in the console, when spark finishes its job. The whole process takes about 6 mins on 2 nodes cluster so don’t terminate the console while the job is running.

NOTE: DO NOT TERMINATE CLUSTER until you see the result from spark console.

Sample output: 

Duration in hours: 32599
Number of missed connections: 525
Total duration after penalty in hours: 85099

Now you can TERMINATE the cluster
