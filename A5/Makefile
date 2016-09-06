AWS_ACCESS_KEY=[your access key]
AWS_SECRET_KEY=[your secret key]

CLUSTER_NAME = [cluster name]
BUCKET = [your bucket name]
OUTPUT_DIR = [output directory]

aws:
	aws s3 cp a5.jar s3://$(BUCKET)
	aws emr create-cluster  --name $(CLUSTER_NAME) \
	  --ami-version 3.10.0  \
	  --instance-groups InstanceGroupType=MASTER,InstanceCount=1,InstanceType=m3.xlarge \
	             	InstanceGroupType=CORE,InstanceCount=10,InstanceType=m3.xlarge \
	  --steps Type=CUSTOM_JAR,Name="CLI Test JAR Step",ActionOnFailure=TERMINATE_CLUSTER,\
Jar=s3://$(BUCKET)/a5.jar,\
Args=[s3://$(BUCKET)/input/all,\
s3://$(BUCKET)/$(OUTPUT_DIR),\
-fsroot=s3://$(BUCKET)/,\
-awskeyid=$(AWS_ACCESS_KEY),\
-awskey=$(AWS_SECRET_KEY)] \
	--auto-terminate \
	--log-uri s3://$(BUCKET)/logs \
	--service-role EMR_DefaultRole \
	--ec2-attributes InstanceProfile=EMR_EC2_DefaultRole,AvailabilityZone=us-east-1c \
	--enable-debugging


result:
	rm -rf $(OUTPUT_DIR)
	mkdir $(OUTPUT_DIR)  
	aws s3 cp s3://$(BUCKET)/$(OUTPUT_DIR) $(OUTPUT_DIR) --recursive
	javac AirlineResultsParse.java
	java AirlineResultsParse $(OUTPUT_DIR)
