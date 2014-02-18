package com.lubin.rpc.client;

public interface AsyncRPCCallback {
	
	public void success(Object result);
	
	public void fail(Exception e);
	
}
