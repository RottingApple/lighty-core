#!/bin/sh

#start controller
cd /lighty-cluster-app-14.0.0-SNAPSHOT
java -ms128m -mx128m -XX:MaxMetaspaceSize=128m -jar lighty-cluster-app-14.0.0-SNAPSHOT.jar -n 0 -k
