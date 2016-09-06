#!/usr/bin/env python
import sys
import json

result = json.load(sys.stdin)
#print result
stepid =  result['StepIds'][0]
print stepid
	
