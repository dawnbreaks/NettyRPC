package com.lubin.myTable.client;

import com.lubin.myTable.obj.IMyTable;
import com.lubin.rpc.client.RPCClient;

public class MyTableClient {
	
	static IMyTable instance;
	
	public static IMyTable getInstance(){
		if (instance == null){
			synchronized (MyTableClient.class){
				if (instance == null){
//					 final String host ="127.0.0.1";//192.168.0.51  127.0.0.1
//			    	 final int port = 9090;
			    	 instance = RPCClient.proxyBuilder(IMyTable.class).build();
				}
			}
		}
		return instance;
	}
}
