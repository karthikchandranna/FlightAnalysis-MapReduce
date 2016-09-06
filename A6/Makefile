#Enter the values before running
AWS_ACCESS_KEY=[your aws access key]
AWS_SECRET_KEY=[your aws secret key]

CLUSTER_NAME = [Cluster name]
BUCKET = [Bucket name]
OUTPUT_DIR = [output directory]
OUTPUT_DIR_FINAL = [output directory for second reducer]
PATH_TO_VALIDATION_FILE = [given validation file]

aws:
	aws s3 cp job.jar s3://$(BUCKET)
	aws emr create-cluster  --name $(CLUSTER_NAME) \
	  --ami-version 3.10.0  \
	  --instance-groups InstanceGroupType=MASTER,InstanceCount=1,InstanceType=m3.xlarge \
	             	InstanceGroupType=CORE,InstanceCount=10,InstanceType=m3.xlarge \
	  --steps Type=CUSTOM_JAR,Name="MR_A6_Custom_Step",ActionOnFailure=TERMINATE_CLUSTER,\
Jar=s3://$(BUCKET)/job.jar,\
Args=[s3://$(BUCKET)/input/forest_data,\
s3://$(BUCKET)/$(OUTPUT_DIR),\
s3://$(BUCKET)/a6test,\
s3://$(BUCKET)/$(OUTPUT_DIR_FINAL),\
-fsroot=s3://$(BUCKET)/,\
-awskeyid=$(AWS_ACCESS_KEY),\
-awskey=$(AWS_SECRET_KEY)] \
	--auto-terminate \
	--log-uri s3://$(BUCKET)/logs \
	--service-role EMR_DefaultRole \
	--ec2-attributes InstanceProfile=EMR_EC2_DefaultRole,AvailabilityZone=us-east-1c \
	--enable-debugging


result:
	rm -rf $(OUTPUT_DIR_FINAL)
	mkdir $(OUTPUT_DIR_FINAL)  
	aws s3 sync s3://$(BUCKET)/$(OUTPUT_DIR_FINAL) $(OUTPUT_DIR_FINAL)
	cat p* > delay_predictions.csv
	javac Validator.java
	java -Xms1024m -Xmx2048m Validator delay_predictions.csv $(PATH_TO_VALIDATION_FILE)
