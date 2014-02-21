package com.lubin.rpc.example;

import java.util.ArrayList;
import java.util.List;

import com.lubin.rpc.example.obj.HelloWorldObj;
import com.lubin.rpc.server.RPCServer;
import com.lubin.rpc.server.ServerConfig;

public class HelloServer {
	public static void main(String[] args) throws Exception {
		List<Object> objList = new ArrayList<Object>();
		objList.add(new HelloWorldObj());
		new RPCServer(new ServerConfig(objList)).run();
	}
}
