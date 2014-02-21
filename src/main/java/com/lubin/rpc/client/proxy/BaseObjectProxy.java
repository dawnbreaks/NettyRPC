package com.lubin.rpc.client.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import com.lubin.rpc.client.DefaultClientHandler;
import com.lubin.rpc.client.RPCClientInitializer;
import com.lubin.rpc.thread.BetterExecutorService;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class BaseObjectProxy<T> {


	protected static final Logger logger = Logger.getLogger(BaseObjectProxy.class.getName());
	 
	protected Class<T> clazz;
	
	protected CopyOnWriteArrayList<DefaultClientHandler> handlers = new CopyOnWriteArrayList<DefaultClientHandler>();
	
	private AtomicInteger roundRobin = new AtomicInteger(0);

	static EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);
	
	private static Config conf = ConfigFactory.load();
	
	private int reconnInterval = ConfigFactory.load().getInt("client.reconnInterval");  //1 second
	
	public static Config getConfig(){
		return conf;
	}

	public void setClazz(Class<T> clazz) {
		this.clazz = clazz;
	}

	public Class<T> getClazz() {
		return clazz;
	}
	

	public BaseObjectProxy(final String host, final int port, Class<T> clazz) {
		 this.clazz = clazz;
		
	 	 Bootstrap b = new Bootstrap();
	 	 b.group(BaseObjectProxy.getEventLoopGroup())
	 	  .channel(NioSocketChannel.class)
	 	  .handler(new RPCClientInitializer(this));
	 	 
		 ChannelFuture channelFuture = b.connect(host, port);
		 
		 channelFuture.addListener(new ChannelFutureListener(){
			@Override
			public void operationComplete(final ChannelFuture channelFuture) throws Exception {
				if(!channelFuture.isSuccess()){
					final SocketAddress remotePeer = new InetSocketAddress(host,port);
					doReconnect(channelFuture.channel(), remotePeer );
				}else{
					handlers.add(channelFuture.channel().pipeline().get(DefaultClientHandler.class));
				}
				
				
			}
		 });
		 
		 try {
			 channelFuture.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public BaseObjectProxy(ArrayList<InetSocketAddress> servers, Class<T> clazz) {
		 this.clazz = clazz;
		 
		 for(final InetSocketAddress server : servers){
		 	 Bootstrap b = new Bootstrap();
		 	 b.group(BaseObjectProxy.getEventLoopGroup())
		 	  .channel(NioSocketChannel.class)
		 	  .handler(new RPCClientInitializer(this));
		 	 
			 ChannelFuture channelFuture = b.connect(server);
			 
			 channelFuture.addListener(new ChannelFutureListener(){
				@Override
				public void operationComplete(final ChannelFuture channelFuture) throws Exception {
					if(!channelFuture.isSuccess()){
						doReconnect(channelFuture.channel(), server );
					}else{
						handlers.add(channelFuture.channel().pipeline().get(DefaultClientHandler.class));
					}
				}
			 });
			 
			 try {
				 channelFuture.await();
			} catch (InterruptedException e) {
				System.out.println("unable to connect to server|host="+server.getHostString()+"|port="+server.getPort());
				e.printStackTrace();
			}
		 }
	}

	public void doReconnect(final Channel channel,final SocketAddress remotePeer) {
		
		handlers.remove(channel.pipeline().get(DefaultClientHandler.class));
		channel.eventLoop().schedule(new Runnable(){
			@Override
			public void run() {
				try {
				 	 Bootstrap b = new Bootstrap();
				 	 b.group(BaseObjectProxy.getEventLoopGroup()).channel(NioSocketChannel.class).handler(new RPCClientInitializer(BaseObjectProxy.this));
				 	 
					 ChannelFuture channelFuture = b.connect(remotePeer);
					 
					 channelFuture.addListener(new ChannelFutureListener(){
						@Override
						public void operationComplete(final ChannelFuture channelFuture) throws Exception {
							if(!channelFuture.isSuccess()){
								System.out.println("doReconnect failed:server="+remotePeer.toString());
								doReconnect(channelFuture.channel(), remotePeer );
							}else{
								DefaultClientHandler handler = channelFuture.channel().pipeline().get(DefaultClientHandler.class);
								handlers.add(handler);
							}
						}
					 });
	
				} catch (Exception e) {
					System.out.println("doReconnect got exception"+e.getMessage());
					doReconnect(channel, remotePeer);
				}
			}
		}, reconnInterval, TimeUnit.MILLISECONDS);
	}
	
	DefaultClientHandler chooseHandler(){
		
		CopyOnWriteArrayList<DefaultClientHandler> handlers = (CopyOnWriteArrayList<DefaultClientHandler>) this.handlers.clone();
		int size = handlers.size();
		if(size <= 0){
			throw new RuntimeException("Cann't connect any servers!");
		}
		int index = (roundRobin.getAndAdd(1) + size)%size;
		return handlers.get(index);
	}

	public static EventLoopGroup getEventLoopGroup(){
		return eventLoopGroup;
	}
	

	public static void submit(Runnable task){
		if(threadPool == null){
			synchronized (BaseObjectProxy.class) {
				if(threadPool== null){
					LinkedBlockingDeque<Runnable> linkedBlockingDeque = new LinkedBlockingDeque<Runnable>();
					ThreadPoolExecutor executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 600L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
					threadPool = new BetterExecutorService(linkedBlockingDeque, executor,"Client async thread pool",BaseObjectProxy.getConfig().getInt("client.asyncThreadPoolSize"));
				}
			}
		}
		
		threadPool.submit(task);
	}
	
	private static BetterExecutorService threadPool;
}
