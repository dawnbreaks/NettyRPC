package com.lubin.rpc.client;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.lubin.rpc.client.proxy.BaseObjectProxy;
import com.lubin.rpc.client.proxy.IAsyncObjectProxy;
import com.lubin.rpc.client.proxy.ObjectProxy;
import com.lubin.rpc.thread.BetterExecutorService;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;

public class RPCClient {

	private static BetterExecutorService threadPool;
	
	private static Config conf = ConfigFactory.load();

	static EventLoopGroup eventLoopGroup = new NioEventLoopGroup(conf.getInt("client.ioThreadNum"));
	
	
	public static <T> T createObjectProxy(String host, int port, Class<T> clazz){
		ArrayList<InetSocketAddress> serverList = new ArrayList<InetSocketAddress>();
		serverList.add(new InetSocketAddress(host, port));
		T t = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz }, new ObjectProxy<T>(serverList, clazz));
		return t;
	}

	public static  <T> T  createObjectProxy(ArrayList<InetSocketAddress> serverList, Class<T> clazz) {
		T t = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] {clazz},new ObjectProxy<T>(serverList, clazz));
		return t;
	}
	
	public static <T> T createObjectProxy(Class<T> clazz){
		T t = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz }, new ObjectProxy<T>(loadServerList(clazz), clazz));
		return t;
	}

	
	public static <T> IAsyncObjectProxy  createAsyncObjPrx(ArrayList<InetSocketAddress> serverList, Class<T> clazz) {
		return new ObjectProxy<T>(serverList, clazz);
	}
	
	public static <T> IAsyncObjectProxy  createAsyncObjPrx(String host, int port, Class<T> clazz) {
		ArrayList<InetSocketAddress> serverList = new ArrayList<InetSocketAddress>();
		serverList.add(new InetSocketAddress(host, port));
		return new ObjectProxy<T>(serverList, clazz);
	}
	
	public static <T> IAsyncObjectProxy  createAsyncObjPrx(Class<T> clazz) {
		return new ObjectProxy<T>(loadServerList(clazz), clazz);
	}
	
	
	public static <T> ArrayList<InetSocketAddress> loadServerList(Class<T> clazz){
		ArrayList<InetSocketAddress> serverList = new ArrayList<InetSocketAddress>();
		List<? extends ConfigObject> objConfList = RPCClient.getConfig().getObjectList("client.objects");
		for(ConfigObject conf : objConfList){
			Object name = conf.get("name").unwrapped();
			if(name.equals(clazz.getName())){
				String[] servers = ((String)conf.get("servers").unwrapped()).split(" ");
				for(int i=0;i<servers.length;i++){
					String[] ipAndPort = servers[i].split(":");
					serverList.add(new InetSocketAddress(ipAndPort[0],Integer.parseInt(ipAndPort[1])));
				}
			}
		}
		
		if(serverList.isEmpty()){
			throw new RuntimeException("server list is empty, can not find any corresponding client.objects in the conf file.");
		}
		return serverList;
	}
	public static void submit(Runnable task){
		if(threadPool == null){
			synchronized (BaseObjectProxy.class) {
				if(threadPool== null){
					LinkedBlockingDeque<Runnable> linkedBlockingDeque = new LinkedBlockingDeque<Runnable>();
					ThreadPoolExecutor executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 600L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
					threadPool = new BetterExecutorService(linkedBlockingDeque, executor,"Client async thread pool",RPCClient.getConfig().getInt("client.asyncThreadPoolSize"));
				}
			}
		}
		
		threadPool.submit(task);
	}


	public static Config getConfig(){
		return conf;
	}

	public static EventLoopGroup getEventLoopGroup() {
		return eventLoopGroup;
	}
	
	public static void shutdown(){
		eventLoopGroup.shutdownGracefully();
		threadPool.shutdown();
	}
}


