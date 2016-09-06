DIR = karthik_sujith
INPUT = all
OUTPUT = output_make

BUCKET_PATH_INPUT = [input folder on s3]
BUCKET_PATH_OUTPUT =  [output folder on s3]
BUCKET_PATH_JAR =  [jar on s3]
CLUSTER_ID = [enter cluster id]
LOGS = [log file path]

JARFILE_SINGLE = Karthik_Sujith_A3.jar
JARFILE_MULTI = Karthik_Sujith_A3_Multi.jar
JARFILE_PSEUDO = karthik_sujith.jar
JARFILE_AWS = karthik_sujith_aws.jar

single:
	java -jar $(JARFILE_SINGLE) $(INPUT) mean 5
	java -jar $(JARFILE_SINGLE) $(INPUT) mean 10
	java -jar $(JARFILE_SINGLE) $(INPUT) mean 15
	java -jar $(JARFILE_SINGLE) $(INPUT) mean 20
	java -jar $(JARFILE_SINGLE) $(INPUT) mean 25
	java -jar $(JARFILE_SINGLE) $(INPUT) median 5
	java -jar $(JARFILE_SINGLE) $(INPUT) median 10
	java -jar $(JARFILE_SINGLE) $(INPUT) median 15
	java -jar $(JARFILE_SINGLE) $(INPUT) median 20
	java -jar $(JARFILE_SINGLE) $(INPUT) median 25

multi:
	java -jar $(JARFILE_MULTI) $(INPUT) mean 5
	java -jar $(JARFILE_MULTI) $(INPUT) mean 10
	java -jar $(JARFILE_MULTI) $(INPUT) mean 15
	java -jar $(JARFILE_MULTI) $(INPUT) mean 20
	java -jar $(JARFILE_MULTI) $(INPUT) mean 25
	java -jar $(JARFILE_MULTI) $(INPUT) median 5
	java -jar $(JARFILE_MULTI) $(INPUT) median 10
	java -jar $(JARFILE_MULTI) $(INPUT) median 15
	java -jar $(JARFILE_MULTI) $(INPUT) median 20
	java -jar $(JARFILE_MULTI) $(INPUT) median 25

	


pseudo:
	hadoop fs -mkdir $(DIR)
	hadoop fs -put $(INPUT) $(DIR)
	hadoop jar $(JARFILE_PSEUDO) $(DIR)/all $(OUTPUT) mean 5
	hadoop jar $(JARFILE_PSEUDO) $(DIR)/all $(OUTPUT) mean 10
	hadoop jar $(JARFILE_PSEUDO) $(DIR)/all $(OUTPUT) mean 15
	hadoop jar $(JARFILE_PSEUDO) $(DIR)/all $(OUTPUT) mean 20
	hadoop jar $(JARFILE_PSEUDO) $(DIR)/all $(OUTPUT) mean 25
	hadoop jar $(JARFILE_PSEUDO) $(DIR)/all $(OUTPUT) median 5
	hadoop jar $(JARFILE_PSEUDO) $(DIR)/all $(OUTPUT) median 10
	hadoop jar $(JARFILE_PSEUDO) $(DIR)/all $(OUTPUT) median 15
	hadoop jar $(JARFILE_PSEUDO) $(DIR)/all $(OUTPUT) median 20
	hadoop jar $(JARFILE_PSEUDO) $(DIR)/all $(OUTPUT) median 25
	hadoop jar $(JARFILE_PSEUDO) $(DIR)/all $(OUTPUT) fastMedian 5
	hadoop jar $(JARFILE_PSEUDO) $(DIR)/all $(OUTPUT) fastMedian 10
	hadoop jar $(JARFILE_PSEUDO) $(DIR)/all $(OUTPUT) fastMedian 15
	hadoop jar $(JARFILE_PSEUDO) $(DIR)/all $(OUTPUT) fastMedian 20
	hadoop jar $(JARFILE_PSEUDO) $(DIR)/all $(OUTPUT) fastMedian 25


# To Run AWS, 
cluster:
	aws emr create-cluster --cluster_name <enter name of cluster> --release-label emr-4.3.0  --service-role EMR_DefaultRole --ec2-attributes InstanceProfile=EMR_EC2_DefaultRole --instance-groups InstanceGroupType=MASTER,InstanceCount=1,InstanceType=m3.xlarge InstanceGroupType=CORE,InstanceCount=10,InstanceType=m3.xlarge	

step:
	aws emr add-steps --$(CLUSTER_ID) --steps Type=CUSTOM_JAR,Name=$(APP),ActionOnFailure=CONTINUE,Jar=$(BUCKET_PATH_JAR),Args=$(BUCKET_PATH_INPUT),$(BUCKET_PATH_OUTPUT)

cloud:
	aws s3 sync $(BUCKET_PATH_OUTPUT) output






