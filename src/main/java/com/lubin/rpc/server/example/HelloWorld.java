package com.lubin.rpc.server.example;

public class HelloWorld implements IHelloWord {

	@Override
	public String hello(String msg) {
		return msg;
	}

	@Override
	public String test(int i, String s, long l) {
		return i+s+l;
	}
}
