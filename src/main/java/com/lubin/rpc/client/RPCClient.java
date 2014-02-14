package com.lubin.rpc.client;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import com.lubin.rpc.server.example.IHelloWord;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;



public class RPCClient {

	static EventLoopGroup eventLoopGroup = new NioEventLoopGroup(2);
	static AtomicLong seqNumGenerator = new AtomicLong(0);
	
	public  static long getNextSequentNumber(){
		return seqNumGenerator.getAndAdd(1);
	}
	
	
	
    public static void main(String[] args) throws Exception {

    	String host ="127.0.0.1";
    	int port = 9090;
    	 Bootstrap b = new Bootstrap();
         b.group(eventLoopGroup)
          .channel(NioSocketChannel.class)
          .handler(new RPCClientInitializer());

         // Make a new connection.
         Channel ch = b.connect(host, port).sync().channel();

         // Get the handler instance to initiate the request.
         ObjectProxyHandler handler =
             ch.pipeline().get(ObjectProxyHandler.class);
         
         IHelloWord client = ObjectProxy.createObjectProxy(IHelloWord.class, handler);
         System.out.print(client.hello("hello world!"));

    }

}
