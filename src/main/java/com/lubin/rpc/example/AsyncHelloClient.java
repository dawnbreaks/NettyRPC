package com.lubin.rpc.example;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.lubin.rpc.client.AsyncObjectProxy;
import com.lubin.rpc.client.RPCClient;
import com.lubin.rpc.client.RPCFuture;

public class AsyncHelloClient {
	

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
							 AsyncObjectProxy<IHelloWordObj> client = RPCClient.createAsyncObjProxyInstance(host, port, IHelloWordObj.class);
							 long start = System.currentTimeMillis();
					         for(int i=0;i<requestNum;i++){
					         	RPCFuture result = client.call("hello", new Object[]{"hello world!"}, new AsyncHelloWorldCallback("hello world!"));
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
         
         
         
		 AsyncObjectProxy<IHelloWordObj> client = RPCClient.createAsyncObjProxyInstance(host, port, IHelloWordObj.class);
		 long start = System.currentTimeMillis();
		 
		 RPCFuture helloFuture = client.call("hello", new Object[]{"hello world!"}, new AsyncHelloWorldCallback("hello world!"));
		 RPCFuture testFuture = client.call("test", new Object[]{1,"hello world!",2L}, new AsyncHelloWorldCallback("hello world!"));
		 
		 System.out.print(helloFuture.get(3000, TimeUnit.MILLISECONDS));
		 System.out.print(testFuture.get(3000, TimeUnit.MILLISECONDS));
		 
		 
//         RPCClient.getEventLoopGroup().shutdownGracefully();
    }
}
