#!/bin/bash
nohup java -cp ./dependency/*:./*  com.lubin.rpc.example.server.HelloWorldServer  2>&1 > /dev/null  &
