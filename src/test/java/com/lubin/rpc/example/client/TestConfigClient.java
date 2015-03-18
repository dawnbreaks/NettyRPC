package com.lubin.rpc.example.client;

import java.util.concurrent.atomic.AtomicLong;

import com.lubin.rpc.client.RPCClient;
import com.lubin.rpc.example.obj.IHelloWordObj;

public class TestConfigClient {

	public static void main(String []args) throws InterruptedException{

		final AtomicLong totalTimeCosted = new AtomicLong(0);
		int threadNum = 1;
		final int requestNum = 100000;
		Thread[] threads = new Thread[threadNum];

		for (int i = 0; i < threadNum; i++) {
			threads[i] = new Thread(new Runnable() {
				@Override
				public void run() {
					IHelloWordObj client = RPCClient.proxyBuilder(IHelloWordObj.class).build();
					long start = System.currentTimeMillis();
					for (int i = 0; i < requestNum; i++) {
						String result = client.hello("hello world!" + i);
						if (!result.equals("hello world!" + i))
							System.out.print("error=" + result);
					}
					long time = System.currentTimeMillis() - start;
					long old = totalTimeCosted.get();
					while (!totalTimeCosted.compareAndSet(old, old + time)) {
						old = totalTimeCosted.get();
					}
				}
			});
			threads[i].start();
		}

		for (int i = 0; i < threads.length; i++)
			threads[i].join();

		System.out.println("total time costed:" + totalTimeCosted.get()	+ "|req/s=" + requestNum * threadNum / (double) (totalTimeCosted.get() / 1000));

		RPCClient.getInstance().getEventLoopGroup().shutdownGracefully();
	}
}
