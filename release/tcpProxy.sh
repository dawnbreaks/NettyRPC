#/bin/bash
pid=`ps -ef|grep java|grep tcpproxy|awk '{print $2}'`
if [ "x$pid" != "x" ];
then
    echo  "killing service process." 
    kill  -9 $pid
    sleep  4
fi

echo "starting service....."
nohup java -cp ./dependency/*:./*   com.lubin.tcpproxy.TcpProxyServer  2>&1 >> ./tcpProxy.log  &
