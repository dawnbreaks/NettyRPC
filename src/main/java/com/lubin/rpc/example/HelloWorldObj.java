package com.lubin.rpc.example;

public class HelloWorldObj implements IHelloWordObj {

	@Override
	public String hello(String msg) {
//		System.out.print("msg"+msg);
		return msg;
	}

	@Override
	public String test(int i, String s, long l) {
		System.out.print("test:"+i+s+l);
		return i+s+l;
	}
}
