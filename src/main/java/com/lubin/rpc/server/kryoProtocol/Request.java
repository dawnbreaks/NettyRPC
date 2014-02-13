package com.lubin.rpc.server.kryoProtocol;

import java.util.ArrayList;

public class Request {
	int seq;
	int version;
	char type;  //0 normal, 1oneway ,2 async
	String objName;
	String funcName;
	ArrayList<Object> args;
}
