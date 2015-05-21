package com.lubin.rpc.example.server;

import com.lubin.rpc.server.RPCServer;

public class HelloWorldServer {

	public static void main(String[] args) throws Exception {
		new RPCServer().run();
	}
}
