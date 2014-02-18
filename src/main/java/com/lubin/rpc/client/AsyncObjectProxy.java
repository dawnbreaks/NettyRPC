package com.lubin.rpc.client;

import com.lubin.rpc.protocol.RPCContext;
import com.lubin.rpc.protocol.Request;
import com.lubin.rpc.server.Constants;

public class AsyncObjectProxy<T> {
	
	private Class<T> clazz;
	private DefaultClientHandler handler;

	public void setClazz(Class<T> clazz) {
		this.clazz = clazz;
	}

	public Class<T> getClazz() {
		return clazz;
	}
	
	public AsyncObjectProxy(DefaultClientHandler handler, Class<T> clazz){
		this.handler = handler;
		this.clazz = clazz;
	}
	
	public RPCFuture call(String funcName, Object[] args, AsyncRPCCallback callback){
		
		   Request req = new Request();
		   req.setSeqNum(handler.getNextSequentNumber());
		   req.setObjName(clazz.getSimpleName());
		   req.setFuncName(funcName);
		   req.setArgs(args);
		   
		   //set rpc type
		   req.setType(Constants.RPCType.async);

		   RPCContext rpcCtx = new RPCContext();
		   rpcCtx.setRequest(req);
		   
		   RPCFuture rpcFuture = handler.doRPC(rpcCtx,callback);
		   return rpcFuture;
	}

}
