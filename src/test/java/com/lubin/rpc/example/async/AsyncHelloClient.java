package com.lubin.rpc.example.async;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.lubin.rpc.client.RPCClient;
import com.lubin.rpc.client.RPCFuture;
import com.lubin.rpc.client.proxy.IAsyncObjectProxy;
import com.lubin.rpc.example.obj.IHelloWordObj;

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

				 	 IAsyncObjectProxy client = RPCClient.createAsyncObjPrx(host, port, IHelloWordObj.class);
					 long start = System.currentTimeMillis();
					 for(int i=0;i<requestNum;i++){
					 	RPCFuture result = client.call("hello", new Object[]{"hello world!"+i}, new AsyncHelloWorldCallback("hello world!"+i));
					  }
					 totalTimeCosted.addAndGet(System.currentTimeMillis() - start);
				}
        	 });
        	 threads[i].start();
         }
         
         for(int i=0; i<threads.length;i++)
        	 threads[i].join();

         System.out.println("total time costed:"+totalTimeCosted.get()+"|req/s="+requestNum*threadNum/(double)(totalTimeCosted.get()/1000));

		 IAsyncObjectProxy client = RPCClient.createAsyncObjPrx(host, port, IHelloWordObj.class);
	
		 
		 RPCFuture helloFuture = client.call("hello", new Object[]{"hello world!"});
		 RPCFuture testFuture = client.call("test", new Object[]{1,"hello world!",2L});
		 
		 System.out.println(helloFuture.get(3000, TimeUnit.MILLISECONDS));
		 System.out.println(testFuture.get(3000, TimeUnit.MILLISECONDS));
		 
		 
//         RPCClient.getEventLoopGroup().shutdownGracefully();
    }
}
