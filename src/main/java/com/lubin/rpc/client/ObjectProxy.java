package com.lubin.rpc.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import com.lubin.rpc.server.kryoProtocol.RPCContext;
import com.lubin.rpc.server.kryoProtocol.Request;

public class ObjectProxy<T> implements InvocationHandler {
	

	private Class<T> clazz;
	private ObjectProxyHandler handler;

	
	


	public void setClazz(Class<T> clazz) {
		this.clazz = clazz;
	}

	public Class<T> getClazz() {
		return clazz;
	}
	
	public ObjectProxy(ObjectProxyHandler handler, Class<T> clazz){
		this.handler = handler;
		this.clazz = clazz;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		   if(Object.class  == method.getDeclaringClass()) {
		       String name = method.getName();
		       if("equals".equals(name)) {
		           return proxy == args[0];
		       } else if("hashCode".equals(name)) {
		           return System.identityHashCode(proxy);
		       } else if("toString".equals(name)) {
		           return proxy.getClass().getName() + "@" +
		               Integer.toHexString(System.identityHashCode(proxy)) +
		               ", with InvocationHandler " + this;
		       } else {
		           throw new IllegalStateException(String.valueOf(method));
		       }
		   }

		   Request req = new Request();
		   req.setSeqNum(RPCClient.getNextSequentNumber());
		   req.setObjName(clazz.getSimpleName());
		   req.setFuncName(method.getName());
		  
		   req.setArgs(args);
		   //todo..
		   
		   RPCContext rpcCtx = new RPCContext();
		   rpcCtx.setReq(req);
		   
		   handler.doRPC(rpcCtx);
		   return rpcCtx.getRes().getResult();
	}
	
	
	@SuppressWarnings("unchecked")
	public static <T> T createObjectProxy(Class<T> clazz, ObjectProxyHandler handler){
		
		T t = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[] {clazz},new ObjectProxy<T>(handler,clazz));
		return t;
		
	}


	
}
