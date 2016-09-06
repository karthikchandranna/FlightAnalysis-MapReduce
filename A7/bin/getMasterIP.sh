#!/bin/bash

CLUSTER_ID=$1
ips=$(aws emr list-instances --instance-group-types "MASTER" --cluster-id $CLUSTER_ID | ./nodeips.py)
echo $ips
