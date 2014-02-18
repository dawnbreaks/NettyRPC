package com.lubin.rpc.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;

import com.lubin.rpc.protocol.RPCContext;
import com.lubin.rpc.protocol.Request;
import com.lubin.rpc.server.Constants;

public class ObjectProxy<T> implements InvocationHandler {

	private Class<T> clazz;
	private DefaultClientHandler handler;

	public void setClazz(Class<T> clazz) {
		this.clazz = clazz;
	}

	public Class<T> getClazz() {
		return clazz;
	}
	
	public ObjectProxy(DefaultClientHandler handler, Class<T> clazz){
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
		   req.setSeqNum(handler.getNextSequentNumber());
		   req.setObjName(clazz.getSimpleName());
		   req.setFuncName(method.getName());
		   req.setArgs(args);
		   
		   req.setType(Constants.RPCType.normal);

		   RPCContext rpcCtx = new RPCContext();
		   rpcCtx.setRequest(req);
		   
		   RPCFuture rpcFuture = handler.doRPC(rpcCtx);
		   return rpcFuture.get(3000, TimeUnit.MILLISECONDS);

	}
	
	
	@SuppressWarnings("unchecked")
	public static <T> T createObjectProxy(Class<T> clazz, DefaultClientHandler handler){
		
		T t = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[] {clazz},new ObjectProxy<T>(handler,clazz));
		return t;
	}


	
}
