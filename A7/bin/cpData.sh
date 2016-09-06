#!/bin/bash

CLUSTER_ID=$1
ips=$(aws emr list-instances --instance-group-types "CORE" --cluster-id $CLUSTER_ID | ./bin/nodeips.py)

rm -Rf models
mkdir -p models
for i in $ips
	do
		scp  -r -i ../"MAPR.pem" hadoop@$i:/home/hadoop/r_t/ ./models/
done
