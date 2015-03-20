package com.lubin.rpc.example;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import com.lubin.rpc.client.RPCClient;
import com.lubin.rpc.client.proxy.AsyncRPCCallback;
import com.lubin.rpc.client.proxy.IAsyncObjectProxy;
import com.lubin.rpc.example.obj.IHelloWordObj;

public class Benchmark {


    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
        final String host = "127.0.0.1";
        final int port = 9090;

        ArrayList<InetSocketAddress> serverNodeList =new ArrayList<InetSocketAddress>();
        serverNodeList.add(new InetSocketAddress(host, port));
        serverNodeList.add(new InetSocketAddress(host, port));
        serverNodeList.add(new InetSocketAddress(host, port));
        serverNodeList.add(new InetSocketAddress(host, port));
        serverNodeList.add(new InetSocketAddress(host, port));

        final IAsyncObjectProxy asyncClient = RPCClient.proxyBuilder(IHelloWordObj.class).withServerNodes(serverNodeList).buildAsyncObjPrx();
        final IHelloWordObj syncClient = RPCClient.proxyBuilder(IHelloWordObj.class).withServerNodes(serverNodeList).build();

        int threadNum = 1;
        final int requestNum = 10*10000;
        Thread[] threads = new Thread[threadNum];

        //benchmark for sync call
        final AtomicLong totalTimeCosted = new AtomicLong(0);
        for(int i =0;i< threadNum;i++){    
            threads[i] = new Thread(new Runnable(){
                @Override
                public void run() {
                    long start = System.currentTimeMillis();
                    for (int i = 0; i < requestNum; i++) {
                        String result = syncClient.hello("hello world!" + i);
                        if (!result.equals("hello world!" + i))
                            System.out.print("error=" + result);
                    }
                    totalTimeCosted.addAndGet(System.currentTimeMillis() - start);
                }
            });
            threads[i].start();
        }
        for(int i=0; i<threads.length;i++){
            threads[i].join();
        }
        System.out.println("sync call|total time costed:" + totalTimeCosted.get() + "|req/s=" + requestNum * threadNum / (double) (totalTimeCosted.get() / 1000));


//        //benchmark for async call
//        final AsyncHelloWorldCallback callback = new AsyncHelloWorldCallback(requestNum*threadNum);
//        for (int i = 0; i < threadNum; i++) {
//            threads[i] = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    for (int i = 0; i < requestNum; i++) {
//                        asyncClient.call("hello",  "hello world!" + i ).addCallback(callback);
//                    }
//                }
//            });
//            threads[i].start();
//        }
//        for (int i = 0; i < threads.length; i++){
//            threads[i].join();
//        }
//
//
//        synchronized (Benchmark.class) {
//            Benchmark.class.wait();
//        }

        System.out.print("shutdownGracefully");
        RPCClient.getInstance().shutdown();
    }


    public static class AsyncHelloWorldCallback implements AsyncRPCCallback {

        AtomicLong requestCount = new AtomicLong(0);
        private long requestNum;
        private long startTime;

        public AsyncHelloWorldCallback(long requestNum){
            this.requestNum = requestNum;
            this.startTime = System.currentTimeMillis();
        }

        @Override
        public void fail(Exception e) {
            System.out.println("fail"+e.getMessage());
        }

        @Override
        public void success(Object result) {
            if(requestNum ==requestCount.addAndGet(1)){
                long timeCosted= System.currentTimeMillis() - startTime;
                System.out.println("total time costed:" + timeCosted + "|total requests="+requestNum+"|req/s=" + requestNum  / (double) (timeCosted / 1000));
                synchronized (Benchmark.class) {
                    Benchmark.class.notify();
                }
            }
        }
    }

}
