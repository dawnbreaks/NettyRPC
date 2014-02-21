package com.lubin.rpc.client.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.lubin.rpc.client.DefaultClientHandler;
import com.lubin.rpc.client.RPCFuture;
import com.lubin.rpc.protocol.RPCContext;
import com.lubin.rpc.protocol.Request;
import com.lubin.rpc.server.Constants;

public class ObjectProxy<T> extends BaseObjectProxy<T> implements InvocationHandler {

	public ObjectProxy(String host, int port, Class<T> clazz) {
		super(host, port, clazz);
	}

	public ObjectProxy(ArrayList<InetSocketAddress> servers, Class<T> clazz){
		super(servers, clazz);
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

		   DefaultClientHandler handler = chooseHandler();
		   
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
	
	
	public static <T> T createObjectProxy(String host, int port, Class<T> clazz){
		T t = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] {clazz},new ObjectProxy<T>(host, port, clazz));
		return t;
	}

	public static  <T> T  createObjectProxy(ArrayList<InetSocketAddress> serverList, Class<T> clazz) {
		T t = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] {clazz},new ObjectProxy<T>(serverList, clazz));
		return t;
	}
}
