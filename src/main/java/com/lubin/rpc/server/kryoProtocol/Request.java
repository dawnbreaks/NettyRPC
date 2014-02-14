package com.lubin.rpc.server.kryoProtocol;

import java.util.ArrayList;

public class Request {
//	public int len;
	public int seq;
	public int version;
	public char type;  //0 normal, 1oneway ,2 async
	public String objName;
	public String funcName;
	
	//input
	public ArrayList<Object> args;
}
