YourBucketName = assignmentnaveen9
Key = 'sujith'
PATHTOKEY = /home/naveenaswa/Downloads/sujith.pem
CLUSTER_ID = j-2RZRDLTSU33A5


BUCKET_PATH_JAR = s3://$(YourBucketName)/cheapestairline_2.10-1.0.jar
BUCKET_PATH_INPUT = s3://mrclassvitek/data/ 
BUCKET_PATH_OUTPUT = s3://$(YourBucketName)/output


clean:
	rm -rf project target

build: 
	sbt package

cluster:
	aws s3 mb s3://$(YourBucketName)
	aws s3 cp sparkConfig.json s3://$(YourBucketName)/ --grants full=uri=http://acs.amazonaws.com/groups/global/AllUsers
	aws emr create-cluster --release-label emr-4.3.0  --service-role EMR_DefaultRole --ec2-attributes '{"InstanceProfile":"EMR_EC2_DefaultRole","KeyName":"$(Key)"}' \
        --instance-groups InstanceGroupType=MASTER,InstanceCount=1,InstanceType=m3.xlarge \
         InstanceGroupType=CORE,InstanceCount=10,InstanceType=m3.xlarge \
	--configurations https://s3.amazonaws.com/${YourBucketName}/sparkConfig.json \
	--applications Name=spark Name=Hadoop \
	--region us-east-1  --no-auto-terminate --name A08 \
        --log-uri 's3://$(YourBucketName)/elasticmapreduce/'

step:
	rm -Rf 200
	rm -Rf 1
	mkdir 200
	mkdir 1
	aws s3 cp ./target/scala-2.10/cheapestairline_2.10-1.0.jar s3://$(YourBucketName)/
	aws s3 rm $(BUCKET_PATH_OUTPUT) --recursive
	cd ./bin && ./Step.sh $(CLUSTER_ID) $(BUCKET_PATH_JAR) $(BUCKET_PATH_INPUT) $(BUCKET_PATH_OUTPUT) 200
	aws s3 cp $(BUCKET_PATH_OUTPUT)/ ./200/ --recursive
	aws s3 rm $(BUCKET_PATH_OUTPUT) --recursive
	cd ./bin && ./Step.sh $(CLUSTER_ID) $(BUCKET_PATH_JAR) $(BUCKET_PATH_INPUT) $(BUCKET_PATH_OUTPUT) 1
	aws s3 cp $(BUCKET_PATH_OUTPUT)/ ./1/ --recursive
	rm -Rf output_200
	rm -Rf output_1	
	for i in `ls 200/part*`; do cat $$i >> output_200; done;
	for i in `ls 1/part*`; do cat $$i >> output_1; done;

