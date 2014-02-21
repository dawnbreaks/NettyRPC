package com.lubin.rpc.example;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

import com.lubin.rpc.client.proxy.BaseObjectProxy;
import com.lubin.rpc.client.proxy.ObjectProxy;
import com.lubin.rpc.example.obj.IHelloWordObj;

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

		         	IHelloWordObj client = ObjectProxy.createObjectProxy(host, port, IHelloWordObj.class);
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
				}
        	 });
        	 threads[i].start();
         }
         
         for(int i=0; i<threads.length;i++)
        	 threads[i].join();

         System.out.println("total time costed:"+totalTimeCosted.get()+"|req/s="+requestNum*threadNum/(double)(totalTimeCosted.get()/1000));
         
         InetSocketAddress server1 = new InetSocketAddress("127.0.0.1",9090);
         InetSocketAddress server2 = new InetSocketAddress("127.0.0.1",9091);

         ArrayList<InetSocketAddress> serverList = new ArrayList<InetSocketAddress>();
         serverList.add(server1);
         serverList.add(server2);
         
         IHelloWordObj client = ObjectProxy.createObjectProxy(serverList, IHelloWordObj.class);
         
         System.out.println("test server list:"+client.hello("test server list11"));

         BaseObjectProxy.getEventLoopGroup().shutdownGracefully();
    }
}
