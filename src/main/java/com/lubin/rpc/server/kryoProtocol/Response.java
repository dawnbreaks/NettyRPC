package com.lubin.rpc.server.kryoProtocol;

import java.util.ArrayList;

public class Response {
//	public int len;
	public int seq;
	public int version;
	public char type;  //0 normal, 1oneway, 2 async
	public String objName;
	public String funcName;
	
	//output
	public Object results;
	public char status;//0 ok , 1 error, 2 unknown error
	public String msg;
}
