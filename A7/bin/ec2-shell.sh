#!/bin/bash

BUCKET_NAME=$1

rm -Rf aws_output
rm -f history
rm -f test

aws s3 cp s3://$BUCKET_NAME/output/history aws_output/history/ --recursive
aws s3 cp s3://$BUCKET_NAME/output/test aws_output/test/ --recursive
for i in `ls ./aws_output/test/part*`; do cat $i >>test; done
for i in `ls ./aws_output/history/part*`; do cat $i >>history; done
aws s3 cp ./history s3://$BUCKET_NAME/
aws s3 cp ./test s3://$BUCKET_NAME/

rm -f 04req10k.csv.gz
rm -f 04req10k.csv
aws s3 cp s3://mrclassvitek/07request/04req10k.csv.gz ./
gzip -d 04req10k.csv.gz
aws s3 cp 04req10k.csv s3://$BUCKET_NAME/

rm -f 04missed.csv.gz
rm -f 04missed.csv
aws s3 cp s3://mrclassvitek/a7validate/04missed.csv.gz ./
gzip -d 04missed.csv.gz
aws s3 cp 04missed.csv s3://$BUCKET_NAME/

sed "s/"A7Naveen"/"$BUCKET_NAME"/g" ./AirlineRouting.scala > ./AirlineRouting1.scala

cat AirlineRouting1.scala

sudo spark-shell --packages com.databricks:spark-csv_2.11:1.4.0 -i AirlineRouting1.scala
