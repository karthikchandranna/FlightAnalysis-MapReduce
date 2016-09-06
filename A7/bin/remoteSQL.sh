#!/bin/bash


CLUSTER_ID=$1
PATH_TO_KEY=$2
BUCKET=$3
IP=`./getMasterIP.sh $CLUSTER_ID`
scp  -r -i $PATH_TO_KEY ./ec2-shell.sh ec2-user@$IP:/home/ec2-user/
scp  -r -i $PATH_TO_KEY ../scala/AirlineRouting.scala ec2-user@$IP:/home/ec2-user/
ssh -i $PATH_TO_KEY ec2-user@$IP "./ec2-shell.sh $BUCKET"


