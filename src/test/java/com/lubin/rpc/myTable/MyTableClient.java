package com.lubin.rpc.myTable;

import static org.fusesource.leveldbjni.JniDBFactory.bytes;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

import com.lubin.myTable.obj.IMyTable;
import com.lubin.rpc.client.RPCClient;

public class MyTableClient {
	

    public static void main(String[] args) throws Exception {

    	 final String host ="127.0.0.1";//192.168.0.51  127.0.0.1
    	 final int port = 9090;

         final AtomicLong totalTimeCosted = new AtomicLong(0);
         int threadNum = 1;
         final int requestNum = 50000;
         Thread[] threads = new Thread[threadNum];
         
         for(int i =0;i< threadNum;i++){	
        	 threads[i] = new Thread(new Runnable(){
			 @Override
			 public void run() {
				 
				 IMyTable client = RPCClient.proxyBuilder(IMyTable.class).withServerNode(host, port).build();
					long start = System.currentTimeMillis();
					for (int i = 0; i < requestNum; i++) {
						client.put(bytes("hello world!" +i), bytes("hello world!" + i));
					}
					totalTimeCosted.addAndGet(System.currentTimeMillis() - start);
				}
        	 });
        	 threads[i].start();
         }
         
         for(int i=0; i<threads.length;i++)
        	 threads[i].join();

		System.out.println("total time costed:" + totalTimeCosted.get()	+ "|req/s=" + requestNum * threadNum / (double) (totalTimeCosted.get() / 1000));


		ArrayList<InetSocketAddress> serverList = new ArrayList<InetSocketAddress>();
		serverList.add(new InetSocketAddress("127.0.0.1", 9090));
		serverList.add(new InetSocketAddress("127.0.0.1", 9091));

		IMyTable client = RPCClient.proxyBuilder(IMyTable.class).withServerNodes(serverList).build();

		RPCClient.getInstance().getEventLoopGroup().shutdownGracefully();
    }
}
