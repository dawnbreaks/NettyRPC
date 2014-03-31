#!/bin/bash
nohup java -cp ./dependency/*:./*   com.lubin.tcpproxy.TcpProxyServer  2>&1 > /dev/null  &
