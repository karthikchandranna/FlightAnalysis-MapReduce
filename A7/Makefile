YourBucketName = 'rnksA7'
Key = 'sujith'
PATHTOKEY = /home/sujith/Downloads/sujith.pem

CLUSTER_ID = j-3J82KB492S3TY
BUCKET_PATH_JAR = s3://$(YourBucketName)/AirlineRouting.jar
BUCKET_PATH_INPUT = s3://mrclassvitek/a7history/ 
BUCKET_PATH_OUTPUT = s3://$(YourBucketName)/output/history
BUCKET_PATH_INPUT1 = s3://mrclassvitek/a7test/
BUCKET_PATH_OUTPUT1 = s3://$(YourBucketName)/output/test

JCC = javac
JFLAGS = -g -classpath ./lib/opencsv-3.6.jar:`yarn classpath`:./target/
SRC = ./src/
TARGET = -d ./target/

Build:  AirlineRouting.class
	cd ./target && jar -cvf $(basename $(<F)).jar *.class
	cd ./target && jar -uvf $(basename $(<F)).jar -C ../lib/ com
	cd ./target && jar -uvf $(basename $(<F)).jar -C ../lib/ org
	cd ./target && rm -f *.class

AirlineParser.class:
	$(JCC) $(JFLAGS) $(TARGET) $(SRC)AirlineParser.java

AirlineParser1.class:
	$(JCC) $(JFLAGS) $(TARGET) $(SRC)AirlineParser1.java

CustomWritable.class: $(SRC)CustomWritable.java
	$(JCC) $(JFLAGS) $(TARGET) $(SRC)$(<F)

CustomWritableComparable.class: $(SRC)CustomWritableComparable.java
	$(JCC) $(JFLAGS) $(TARGET) $(SRC)$(<F)

CustomWritableComparable1.class: $(SRC)CustomWritableComparable1.java
	$(JCC) $(JFLAGS) $(TARGET) $(SRC)$(<F)

AirlineRoutingMapper.class: $(SRC)AirlineRoutingMapper.java CustomWritable.class AirlineParser.class
	$(JCC) $(JFLAGS) $(TARGET) $(SRC)$(<F)

AirlineRoutingMapper1.class: $(SRC)AirlineRoutingMapper1.java CustomWritable.class AirlineParser1.class
	$(JCC) $(JFLAGS) $(TARGET) $(SRC)$(<F)

AirlineRoutingReducer.class: $(SRC)AirlineRoutingReducer.java CustomWritable.class CustomWritableComparable.class
	$(JCC) $(JFLAGS) $(TARGET) $(SRC)$(<F)

AirlineRoutingReducer1.class: $(SRC)AirlineRoutingReducer1.java CustomWritable.class CustomWritableComparable1.class
	$(JCC) $(JFLAGS) $(TARGET) $(SRC)$(<F)

AirlineRouting.class: $(SRC)AirlineRouting.java AirlineRoutingMapper.class AirlineRoutingReducer.class AirlineRoutingMapper1.class AirlineRoutingReducer1.class
	$(JCC) $(JFLAGS) $(TARGET) $(SRC)$(<F)


StopH:
	stop-dfs.sh --config `pwd`/conf/
	stop-yarn.sh --config `pwd`/conf/
	mr-jobhistory-daemon.sh stop historyserver
	jps
StartH:
	start-dfs.sh --config `pwd`/conf/
	start-yarn.sh --config `pwd`/conf/
	mr-jobhistory-daemon.sh start historyserver
	jps
Prepare:
	hdfs dfs -rm -R -f hdfs://localhost/A07/
	hdfs dfs -mkdir hdfs://localhost/A07/
	hdfs dfs -mkdir hdfs://localhost/A07/data/
	hdfs dfs -mkdir hdfs://localhost/A07/data1/
	hdfs dfs -copyFromLocal ../TestCases/A05/a5/*  hdfs://localhost/A07/data/
	hdfs dfs -copyFromLocal ../TestCases/A07/a7test/*  hdfs://localhost/A07/data1/
Format:
	hdfs namenode -format
Run:
	hadoop dfs -rm -R -f hdfs://localhost/A07/output
	hadoop dfs -rm -R -f hdfs://localhost/A07/output1
	hadoop --config ./conf jar ./target/AirlineRouting.jar AirlineRouting /A07/data/ /A07/output /A07/data1/ /A07/output1
	#hadoop --config ./conf jar ./target/FlightPrice.jar FlightPrice /A04/data/55.csv.gz /A04/output
	rm -Rf output
	rm -Rf output1
	hadoop dfs -copyToLocal hdfs://localhost/A07/output ./
	hadoop dfs -copyToLocal hdfs://localhost/A07/output1 ./

Cluster:
	aws s3 mb s3://$(YourBucketName)
	aws emr create-cluster --release-label emr-4.3.0  --service-role EMR_DefaultRole --ec2-attributes '{"InstanceProfile":"EMR_EC2_DefaultRole","KeyName":"$(Key)"}' \
        --instance-groups InstanceGroupType=MASTER,InstanceCount=1,InstanceType=m3.xlarge \
         InstanceGroupType=CORE,InstanceCount=2,InstanceType=m3.xlarge \
	--applications Name=spark Name=Hadoop \
	--region us-east-1  --no-auto-terminate --name A07 \
        --log-uri 's3://$(YourBucketName)/elasticmapreduce/'

Step:
	aws s3 cp ./target/AirlineRouting.jar s3://$(YourBucketName)/
	aws s3 rm s3://$(YourBucketName)/output --recursive
	cd ./bin && ./Step.sh $(CLUSTER_ID) $(BUCKET_PATH_JAR) $(BUCKET_PATH_INPUT) $(BUCKET_PATH_OUTPUT) $(BUCKET_PATH_INPUT1) $(BUCKET_PATH_OUTPUT1)
	cd bin && ./remoteSQL.sh $(CLUSTER_ID) $(PATHTOKEY) $(YourBucketName)
	#rm -Rf aws_output
	#aws s3 cp s3://$(YourBucketName)/output/history ./aws_output/history/ --recursive
	#aws s3 cp s3://$(YourBucketName)/output/test ./aws_output/test/ --recursive
