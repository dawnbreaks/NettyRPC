package com.lubin.rpc.example;

import com.lubin.rpc.client.AsyncRPCCallback;

public class AsyncHelloWorldCallback implements AsyncRPCCallback {

	String expect;
	
	public AsyncHelloWorldCallback(String msg){
		this.expect = msg;
	}

	@Override
	public void fail(Exception e) {
		System.out.print("fail"+e.getMessage());
	}

	@Override
	public void success(Object result) {
//		System.out.print(result);
		if(!this.expect.equals(result))
			System.out.print("AsyncCallback got error|expect="+expect+"|got="+result);
	}

}
