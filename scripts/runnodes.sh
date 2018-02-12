#!/bin/bash
#~/corda/gradlew clean
#~/corda/gradlew deployNodes

#~/corda/build/nodes/runnodes


cd /home/ubuntu/FundsDLT/build/nodes/$1
screen -d -m java -jar corda.jar && screen -d -m java -jar corda-webserver.jar

exit 0
