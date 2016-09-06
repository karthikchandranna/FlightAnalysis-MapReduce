#!/bin/bash

typeOfEC2="m1.medium"
keyName="MAPR"
yourBucketName="HUA09"
keyPath="/home/radioer/CS6240/MAPR.pem"

numberOfNodes=$1

if [ $2 == "c" ] || [ $2 == "a" ] ; then
	#Check whether the two security groups is exist, if not create one
	aws ec2 describe-security-groups --group-names {"mapreduceFrameworkSlave","mapreduceFrameworkMaster"} 2>/dev/null
	if [ $? == "255" ]; then
		secGroupIDM=$(aws ec2 create-security-group --group-name mapreduceFrameworkMaster --description "mapreduceFramework master security group" | ./sid.py)
		aws ec2 authorize-security-group-ingress --group-name mapreduceFrameworkMaster --protocol tcp --port 22 --cidr 0.0.0.0/0

		secGroupIDS=$(aws ec2 create-security-group --group-name mapreduceFrameworkSlave --description "mapreduceFramework slave security group" | ./sid.py)
		aws ec2 authorize-security-group-ingress --group-name mapreduceFrameworkSlave --protocol tcp --port 0-65535 --source-group $secGroupIDM 

		aws ec2 authorize-security-group-ingress --group-name mapreduceFrameworkMaster --protocol tcp --port 0-65535 --source-group $secGroupIDM
	fi

		aws emr create-cluster --release-label emr-4.4.0  --service-role EMR_DefaultRole \
			--ec2-attributes "{\"InstanceProfile\":\"EMR_EC2_DefaultRole\", \"KeyName\":\"$keyName\", \
		\"EmrManagedMasterSecurityGroup\":\"$secGroupIDM\", \
		\"EmrManagedSlaveSecurityGroup\":\"$secGroupIDS\"}" \
		--instance-groups InstanceGroupType=MASTER,InstanceCount=1,InstanceType=$typeOfEC2 \
		InstanceGroupType=CORE,InstanceCount=$numberOfNodes,InstanceType=$typeOfEC2 \
		--region us-east-1  --no-auto-terminate --name A09 \
		--log-uri 's3://$(yourBucketName)/elasticmapreduce/' | ./cid.py > cid
        clusterID=`cat cid`

        while true; do
                state=$(aws emr describe-cluster --cluster-id `cat cid` | ./cluster.py)
                if [ $state == "WAITING" ]; then
                        break
                fi
                echo "WAITING"
                sleep 5
        done

fi

if [ $2 == "a" ] || [ $2 == "s" ] ; then
	clusterID=`cat cid`

	masterIP=`aws emr list-instances --instance-group-types "MASTER" --cluster-id $clusterID | ./nodeips.py`
	masterIPP=`aws emr list-instances --instance-group-types "MASTER" --cluster-id $clusterID | ./nodeipps.py`

	declare -a nodeIP
	ips=$(aws emr list-instances --instance-group-types "CORE" --cluster-id $clusterID | ./nodeips.py)
	ii=1
	for i in $ips; do
		nodeIP[$ii]=$i
		((ii++))
	done

	#ssh  -i $keyPath ec2-user@$masterIPP 'ls' 
	rm -f nodelist
	touch nodelist
	echo $masterIP >> nodelist
	for i in `seq 1 $numberOfNodes`; do
		echo -e "${nodeIP[$i]}" >> nodelist
	done
fi

if [ $2 == "e" ]; then
	aws emr terminate-clusters --cluster-ids `cat cid`	
fi
