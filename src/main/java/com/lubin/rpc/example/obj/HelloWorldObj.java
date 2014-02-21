package com.lubin.rpc.example.obj;

public class HelloWorldObj implements IHelloWordObj {

	@Override
	public String hello(String msg) {
//		System.out.print("msg"+msg);
		return msg;
	}

	@Override
	public String test(Integer i, String s, Long l) {
		System.out.print("test:"+i+s+l);
		return i+s+l;
	}
}
