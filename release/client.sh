#!/bin/bash
nohup java -cp ./dependency/*:./*  com.lubin.rpc.example.Benchmark   2>&1 > /dev/null  &
