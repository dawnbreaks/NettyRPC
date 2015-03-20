#!/bin/bash

pid=`ps -ef|grep java|grep RPCServer|awk '{print $2}'`
if [ "x$pid" != "x" ];
then
    echo  "killing service process." 
    kill  -9 $pid
    sleep  4
fi

currentDir=`readlink -m  $(dirname $0)`
targetDir=$currentDir/../target

jvmOptions="-server -XX:PermSize=24M -XX:MaxPermSize=64m -Xms128m -Xmx448m -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=70 -XX:NewRatio=3"
classPath=$targetDir/dependency/*:$targetDir/classes:$targetDir/test-classes
echo "starting service....."
nohup java $jvmOptions -cp $classPath  com.lubin.rpc.server.RPCServer  2>&1 >> ./service.log  &
echo "done"
