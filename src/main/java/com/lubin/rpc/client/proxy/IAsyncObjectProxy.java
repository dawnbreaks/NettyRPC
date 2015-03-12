package com.lubin.rpc.client.proxy;

import com.lubin.rpc.client.RPCFuture;

public interface IAsyncObjectProxy {
//	public RPCFuture call(AsyncRPCCallback callback, String funcName, Object... args);
	public RPCFuture call(String funcName, Object... args);
	void notify(String funcName, Object[] args);
}
