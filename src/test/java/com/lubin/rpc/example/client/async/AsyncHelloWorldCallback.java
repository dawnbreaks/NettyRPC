package com.lubin.rpc.example.client.async;

import com.lubin.rpc.client.proxy.AsyncRPCCallback;

public class AsyncHelloWorldCallback implements AsyncRPCCallback {

	String expect;
	
	public AsyncHelloWorldCallback(String msg){
		this.expect = msg;
	}

	@Override
	public void fail(Exception e) {
		System.out.println("fail"+e.getMessage());
	}

	@Override
	public void success(Object result) {
//		System.out.print(result);
		if(!this.expect.equals(result))
			System.out.println("AsyncCallback got error|expect="+expect+"|got="+result);
	}

}
