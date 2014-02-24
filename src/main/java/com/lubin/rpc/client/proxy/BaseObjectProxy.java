package com.lubin.rpc.client.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import com.lubin.rpc.client.DefaultClientHandler;
import com.lubin.rpc.client.RPCClient;
import com.lubin.rpc.client.RPCClientInitializer;
import com.lubin.rpc.protocol.Constants;
import com.lubin.rpc.protocol.RPCContext;
import com.lubin.rpc.protocol.Request;

public class BaseObjectProxy<T> {


	protected static final Logger logger = Logger.getLogger(BaseObjectProxy.class.getName());
	 
	protected Class<T> clazz;
	
	protected CopyOnWriteArrayList<DefaultClientHandler> handlers = new CopyOnWriteArrayList<DefaultClientHandler>();
	
	private AtomicInteger roundRobin = new AtomicInteger(0);

	public void setClazz(Class<T> clazz) {
		this.clazz = clazz;
	}

	public Class<T> getClazz() {
		return clazz;
	}
	

	public BaseObjectProxy(ArrayList<InetSocketAddress> servers, Class<T> clazz) {
		 this.clazz = clazz;
		 
		 for(final InetSocketAddress server : servers){
		 	 Bootstrap b = new Bootstrap();
		 	 b.group(RPCClient.getEventLoopGroup())
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
				 	 b.group(RPCClient.getEventLoopGroup()).channel(NioSocketChannel.class).handler(new RPCClientInitializer(BaseObjectProxy.this));
				 	 
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
		}, RPCClient.getConfig().getInt("client.reconnInterval"), TimeUnit.MILLISECONDS);
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

	RPCContext createRequest(String funcName, Object[] args, long seqNum, char type) {
		try{
			Request req = new Request();
			req.setSeqNum(seqNum);
			req.setObjName(clazz.getSimpleName());
			req.setFuncName(funcName);
			req.setSerializer((char) RPCClient.getConfig().getInt("client.serializer"));
			req.setArgs(args);
			   
			Class[] parameterTypes = new Class[args.length];
			for(int i=0; i<args.length;i++){
				parameterTypes[i] = args[i].getClass();
			}
		   
		    Method method = clazz.getMethod(funcName, parameterTypes);
		    if( method.getReturnType().equals(Void.TYPE) && Constants.RPCType.oneway == type){
			   req.setType(Constants.RPCType.oneway);
		    }else if( method.getReturnType().equals(Void.TYPE) && Constants.RPCType.normal == type){
		    	req.setType(Constants.RPCType.oneway);
		    }else if( method.getReturnType().equals(Void.TYPE) && Constants.RPCType.async == type){
		    	new RuntimeException("this method will not return, please use notify() to call this method.");
		    }else{
		    	req.setType(type);
		    }
		    
		    RPCContext rpcCtx = new RPCContext();
			rpcCtx.setRequest(req);
		    return rpcCtx ;
		}catch (Exception e) {
			throw new RuntimeException("BaseObjectProxy.createRequest got exception|",e);
		}
	}

}
