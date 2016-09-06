
INPUT = input
OUTPUT = mr_output_a4_ks
OUTPUT1 = mr_output_a4_ks_1
FINAL_OUTPUT = mr_final_output_a4_ks
FINAL_OUTPUT1 = mr_final_output_a4_ks_1

BUCKET_PATH_INPUT = [input folder on s3]
BUCKET_PATH_OUTPUT =  [output folder on s3]
BUCKET_PATH_FINAL_OUTPUT =  [final output folder on s3]
BUCKET_PATH_JAR =  [jar on s3]
CLUSTER_ID = [enter cluster id]
LOGS = [log file path]
JAR1 = mr1.jar
JARLINK = mrLink.jar
JAR2 = mr2.jar

pseudo200:
	hadoop jar mr1.jar input/data $(OUTPUT)
	hadoop fs -get $(OUTPUT) .
	java -jar mrLink.jar 200 $(OUTPUT)
	hadoop fs -put cheapest_airline.txt .
	hadoop jar mr2.jar input/data $(FINAL_OUTPUT) cheapest_airline.txt
	hadoop fs -getmerge $(FINAL_OUTPUT) results.txt
	
pseudo1:
	hadoop jar mr1.jar input/data $(OUTPUT1)
	hadoop fs -get $(OUTPUT1) .
	java -jar mrLink.jar 1 $(OUTPUT1)
	hadoop fs -put cheapest_airline.txt .
	hadoop jar mr2.jar input/data $(FINAL_OUTPUT1) cheapest_airline.txt
	hadoop fs -getmerge $(FINAL_OUTPUT1) results_1.txt

# To Run AWS, 
cluster:
	aws emr create-cluster --cluster_name <enter name of cluster> --release-label emr-4.3.0  --service-role EMR_DefaultRole --ec2-attributes InstanceProfile=EMR_EC2_DefaultRole --instance-groups InstanceGroupType=MASTER,InstanceCount=1,InstanceType=m3.xlarge InstanceGroupType=CORE,InstanceCount=10,InstanceType=m3.xlarge	

step:
	aws emr add-steps --$(CLUSTER_ID) --steps Type=CUSTOM_JAR,Name=$(JAR1),ActionOnFailure=CONTINUE,Jar=$(BUCKET_PATH_JAR),Args=$(BUCKET_PATH_INPUT),$(BUCKET_PATH_OUTPUT)
	aws emr add-steps --$(CLUSTER_ID) --steps Type=CUSTOM_JAR,Name=$(JARLINK),ActionOnFailure=CONTINUE,Jar=$(BUCKET_PATH_JAR),Args=200,$(BUCKET_PATH_OUTPUT)
	aws emr add-steps --$(CLUSTER_ID) --steps Type=CUSTOM_JAR,Name=$(JAR2),ActionOnFailure=CONTINUE,Jar=$(BUCKET_PATH_JAR),Args=$(BUCKET_PATH_INPUT),$(BUCKET_PATH_FINAL_OUTPUT),cheapest_airline.txt

cloud:
	aws s3 sync $(BUCKET_PATH_FINAL_OUTPUT) output
	