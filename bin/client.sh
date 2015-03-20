#!/bin/bash

currentDir=`readlink -m  $(dirname $0)`
targetDir=$currentDir/../target

jvmOptions="-server -XX:PermSize=24M -XX:MaxPermSize=64m -Xms128m -Xmx448m -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=70 -XX:NewRatio=3"
classPath=$targetDir/dependency/*:$targetDir/classes:$targetDir/test-classes
echo "starting benchmark testing....."
nohup java $jvmOptions -cp $classPath  com.lubin.rpc.example.Benchmark  2>&1 >> ./Benchmark.log  &
echo "Done"
