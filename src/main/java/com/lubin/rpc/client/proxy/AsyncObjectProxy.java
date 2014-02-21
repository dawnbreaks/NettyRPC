package com.lubin.rpc.client.proxy;

import java.net.InetSocketAddress;
import java.util.ArrayList;

import com.lubin.rpc.client.DefaultClientHandler;
import com.lubin.rpc.client.RPCFuture;
import com.lubin.rpc.protocol.RPCContext;
import com.lubin.rpc.protocol.Request;
import com.lubin.rpc.server.Constants;

public class AsyncObjectProxy<T> extends BaseObjectProxy<T>{

	public AsyncObjectProxy(String host, int port, Class<T> clazz) {
		super(host, port, clazz);
	}
	
	public AsyncObjectProxy(ArrayList<InetSocketAddress> servers, Class<T> clazz){
		super(servers, clazz);
	}

	public RPCFuture call(String funcName, Object[] args, AsyncRPCCallback callback){
		
		   Request req = new Request();
		   DefaultClientHandler handler = chooseHandler();
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
