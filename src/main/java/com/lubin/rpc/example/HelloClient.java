package com.lubin.rpc.example;

import java.util.concurrent.atomic.AtomicLong;

import com.lubin.rpc.client.RPCClient;

public class HelloClient {
	

    public static void main(String[] args) throws Exception {

    	 final String host ="127.0.0.1";//192.168.0.51  127.0.0.1
    	 final int port = 9090;

         final AtomicLong totalTimeCosted = new AtomicLong(0);
         
         int threadNum = 1;
         final int requestNum = 100000;
         Thread[] threads = new Thread[threadNum];
         for(int i =0;i< threadNum;i++){
        	 
        	 threads[i] = new Thread(new Runnable(){
        		 
				@Override
				public void run() {
					
			         // Make a new connection.
					try {

							 IHelloWordObj client = RPCClient.createObjProxyInstance(host, port, IHelloWordObj.class);
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
         
         RPCClient.getEventLoopGroup().shutdownGracefully();
    }
}
