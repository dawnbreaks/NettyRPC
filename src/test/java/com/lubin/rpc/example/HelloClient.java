package com.lubin.rpc.example;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

import com.lubin.rpc.client.RPCClient;
import com.lubin.rpc.example.obj.IHelloWordObj;
import com.lubin.rpc.example.obj.IHelloWordObj.HellMsg;
import com.lubin.rpc.example.obj.IHelloWordObj.Msg;

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
				 
					IHelloWordObj client = RPCClient.createObjectProxy(host, port, IHelloWordObj.class);
					long start = System.currentTimeMillis();
					for (int i = 0; i < requestNum; i++) {
						String result = client.hello("hello world!" + i);
						if (!result.equals("hello world!" + i))
							System.out.print("error=" + result);
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

		IHelloWordObj client = RPCClient.createObjectProxy(serverList, IHelloWordObj.class);

		HellMsg msg =new HellMsg();
		msg.setI(1);
		msg.setL(2L);
		msg.setS("hello1");
		msg.setMsg(new Msg());
		msg.getMsg().setI(2);
		msg.getMsg().setL(3L);
		msg.getMsg().setS("hello2");
		
		System.out.println("test server list:" + client.testMst(msg));
		Msg res = client.testMst(msg);
		if(!(res.getI() == msg.getI() + msg.getMsg().getI()) 
			|| !(res.getL() == msg.getL() + msg.getMsg().getL())
			||!(res.getS() .equals(msg.getS() + msg.getMsg().getS())))
		{
			System.out.println("tesg Msg got error!");
		}

		RPCClient.getEventLoopGroup().shutdownGracefully();
    }
}
