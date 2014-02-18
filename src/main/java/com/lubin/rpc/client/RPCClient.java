package com.lubin.rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;



public class RPCClient {

	private static EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);
	
	public static EventLoopGroup getEventLoopGroup(){
		return eventLoopGroup;
	}
	
	
	public static <T> T createObjProxyInstance(String host, int port,Class<T> clazz) throws InterruptedException{
		
	 	 Bootstrap b = new Bootstrap();
	 	 b.group(RPCClient.getEventLoopGroup()).channel(NioSocketChannel.class).handler(new RPCClientInitializer());
		 Channel ch = b.connect(host, port).sync().channel();

         //Get the handler instance to initiate the request.
         DefaultClientHandler handler = ch.pipeline().get(DefaultClientHandler.class);
         
         //create proxy
         return ObjectProxy.createObjectProxy(clazz, handler);
	}
	
	public static <T> AsyncObjectProxy<T> createAsyncObjProxyInstance(String host, int port,Class<T> clazz) throws InterruptedException{
		
	 	 Bootstrap b = new Bootstrap();
	 	 b.group(RPCClient.getEventLoopGroup()).channel(NioSocketChannel.class).handler(new RPCClientInitializer());
		 Channel ch = b.connect(host, port).sync().channel();

        //Get the handler instance to initiate the request.
        DefaultClientHandler handler = ch.pipeline().get(DefaultClientHandler.class);
        
        //create async proxy
        return new AsyncObjectProxy<T>(handler,clazz);
	}
}
