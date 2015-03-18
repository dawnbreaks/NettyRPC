package com.lubin.rpc.client;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

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
    static {
        // initiate SLF4J Logger Factory setting
        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
    }
    private Config conf;
    
	private BetterExecutorService threadPool;

	EventLoopGroup eventLoopGroup;
	
	static RPCClient instane;;
	public RPCClient(){
	    conf = ConfigFactory.load();
	    eventLoopGroup = new NioEventLoopGroup(conf.getInt("client.ioThreadNum"));
	    
	    LinkedBlockingDeque<Runnable> linkedBlockingDeque = new LinkedBlockingDeque<>();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 600L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
        threadPool = new BetterExecutorService(linkedBlockingDeque, executor,"Client async thread pool", getConfig().getInt("client.asyncThreadPoolSize"));
        
//        enableServiceDiscovery = RPCServer.getConfig().getBoolean("server.enableServiceDiscovery");
	
	}
	
	public static RPCClient getInstance(){
	    if(instane == null){
            synchronized (RPCClient.class) {
                if(instane == null){
                    instane= new RPCClient();
                }
            }
        }
        return instane;
	}

	
	public static <T> ObjProxyBuilder<T> proxyBuilder(Class<T> clazz){
	   return new ObjProxyBuilder<T>(clazz);
	}
	
	public static class ObjProxyBuilder<T> {
	    private Class<T> clazz;
        private String host;
        private int port;
        private List<InetSocketAddress> serverNodes;
        private boolean enableRegistry;
	    public ObjProxyBuilder(Class<T> clazz) {
            this.clazz = clazz;
        }
        public ObjProxyBuilder<T> withServerNode(String host, int port){
	        this.host = host;
	        this.port = port;
	        return this;
	    }
	    public ObjProxyBuilder<T> withServerNodes(List<InetSocketAddress> serverNodes){
            this.serverNodes = serverNodes;
            return this;
        }
	    public ObjProxyBuilder<T> enableRegistry(){
            this.enableRegistry = true;
            return this;
        }
	    
	    public T build(){
	        this.clazz = clazz;
	        if(this.enableRegistry){
	            T t = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { this.clazz }, new ObjectProxy<T>(this.clazz));
	            return t;
	        }else if( serverNodes != null ){
	            T t = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] {clazz}, new ObjectProxy<T>(serverNodes, clazz));
	            return t;
	        }else if( host !=null && port > 0){
	            ArrayList<InetSocketAddress> serverNodes = new ArrayList<InetSocketAddress>();
	            serverNodes.add(new InetSocketAddress(host, port));
	            T t = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz }, new ObjectProxy<T>(serverNodes, clazz));
	            return t;
	        }else{
	            T t = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz }, new ObjectProxy<T>(RPCClient.getInstance().loadServerListFromConf(clazz), clazz));
                return t;
	        }
	    }
	    
	    public IAsyncObjectProxy buildAsyncObjPrx(){
            if(this.enableRegistry){
                return new ObjectProxy<T>(clazz);
            }else if( serverNodes != null ){
                return new ObjectProxy<T>(serverNodes, clazz);
            }else if( host !=null && port > 0){
                ArrayList<InetSocketAddress> serverList = new ArrayList<InetSocketAddress>();
                serverList.add(new InetSocketAddress(host, port));
                return new ObjectProxy<T>(serverList, clazz);
            }else{
                return new ObjectProxy<T>(RPCClient.getInstance().loadServerListFromConf(clazz), clazz);
            }
        }
	}
	
//	public <T> IAsyncObjectProxy  createAsyncObjPrx(ArrayList<InetSocketAddress> serverList, Class<T> clazz) {
//		return new ObjectProxy<T>(serverList, clazz);
//	}
//	
//	public <T> IAsyncObjectProxy  createAsyncObjPrx(String host, int port, Class<T> clazz) {
//		ArrayList<InetSocketAddress> serverList = new ArrayList<InetSocketAddress>();
//		serverList.add(new InetSocketAddress(host, port));
//		return new ObjectProxy<T>(serverList, clazz);
//	}
//	
//	public <T> IAsyncObjectProxy  createAsyncObjPrx(Class<T> clazz) {
//		return new ObjectProxy<T>(loadServerListFromConf(clazz), clazz);
//	}
//	
//	public <T> IAsyncObjectProxy  createAsyncObjPrxFromZk(Class<T> clazz) {
//        return new ObjectProxy<T>(clazz);
//    }
	
	// 
//  public <T> T createObjectProxy(String host, int port, Class<T> clazz){
//      ArrayList<InetSocketAddress> serverList = new ArrayList<InetSocketAddress>();
//      serverList.add(new InetSocketAddress(host, port));
//      T t = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz }, new ObjectProxy<T>(serverList, clazz));
//      return t;
//  }
//
//  public <T> T  createObjectProxy(ArrayList<InetSocketAddress> serverList, Class<T> clazz) {
//      T t = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] {clazz},new ObjectProxy<T>(serverList, clazz));
//      return t;
//  }
//  
//  public <T> T createObjectProxy(Class<T> clazz){
//      T t = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz }, new ObjectProxy<T>(loadServerListFromConf(clazz), clazz));
//      return t;
//  }
//
//  public <T> T createObjectProxyFromZK(Class<T> clazz){
//        T t = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz }, new ObjectProxy<T>(clazz));
//        return t;
//    }
	
	public <T> ArrayList<InetSocketAddress> loadServerListFromConf(Class<T> clazz){
		ArrayList<InetSocketAddress> serverList = new ArrayList<InetSocketAddress>();
		List<? extends ConfigObject> objConfList = getConfig().getObjectList("client.objects");
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
	
	public void submit(Runnable task){
		if(threadPool == null){
			synchronized (BaseObjectProxy.class) {
				if(threadPool== null){
					
				}
			}
		}
		
		threadPool.submit(task);
	}


	public Config getConfig(){
		return conf;
	}

	public EventLoopGroup getEventLoopGroup() {
		return eventLoopGroup;
	}
	
	public void shutdown(){
		eventLoopGroup.shutdownGracefully();
		threadPool.shutdown();
	}
}


