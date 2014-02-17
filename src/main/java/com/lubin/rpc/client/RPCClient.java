package com.lubin.rpc.client;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import com.lubin.rpc.example.IHelloWord;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;



public class RPCClient {

	static EventLoopGroup eventLoopGroup = new NioEventLoopGroup(1);
	static AtomicLong seqNumGenerator = new AtomicLong(0);
	
	public  static long getNextSequentNumber(){
		return seqNumGenerator.getAndAdd(1);
	}
	
	

    public static void main(String[] args) throws Exception {

    	 final String host ="127.0.0.1";//192.168.0.51  127.0.0.1
    	 final int port = 9090;



         
         final AtomicLong totalTimeCosted = new AtomicLong(0);
         
         int threadNum = 2;
         final int requestNum = 100000;
         Thread[] threads = new Thread[threadNum];
         for(int i =0;i< threadNum;i++){
        	 
        	 threads[i] = new Thread(new Runnable(){
        		 
				@Override
				public void run() {
					
			         // Make a new connection.
					try {
				    	 	 final Bootstrap b = new Bootstrap();
				    	 	 b.group(eventLoopGroup).channel(NioSocketChannel.class).handler(new RPCClientInitializer());
							 Channel ch = b.connect(host, port).sync().channel();
		
					         //Get the handler instance to initiate the request.
					         ObjectProxyHandler handler = ch.pipeline().get(ObjectProxyHandler.class);
					         final IHelloWord client = ObjectProxy.createObjectProxy(IHelloWord.class, handler);
					         
							 long start = System.currentTimeMillis();
					         for(int i=0;i<requestNum;i++){
					         	String result = client.hello("hello world!");
					         	if(!result.equals("hello world!"))
					         		System.out.print("error="+result);
					          }
					         long time=System.currentTimeMillis()- start;
					         long old = totalTimeCosted.get();
					         while(!totalTimeCosted.compareAndSet(old, old + time )){
					        	 old = totalTimeCosted.get();
					         }
			         
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
        	 });
        	 threads[i].start();
         }
         
         for(int i=0; i<threads.length;i++)
        	 threads[i].join();

         System.out.println("total time costed:"+totalTimeCosted.get()+"|req/s="+requestNum*threadNum/(double)(totalTimeCosted.get()/1000));
         
         eventLoopGroup.shutdownGracefully();
    }

}
