package com.lubin.rpc.server.kryoProtocol;

import java.util.ArrayList;

public class Response {
	int seq;
	int version;
	char type;  //0 normal, 1oneway ,2 async
	char status;//0 ok , 1 error
	String objName;
	String funcName;
	Object results;
}
