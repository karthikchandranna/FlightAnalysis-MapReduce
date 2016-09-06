#!/usr/bin/env python
import sys
import json

result = json.load(sys.stdin)
#print result
State =  result['Steps'][0]['Status']['State']
print State
	
