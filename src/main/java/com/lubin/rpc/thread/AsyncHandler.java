package com.lubin.rpc.thread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lubin.rpc.server.RPCServer;

public class AsyncHandler 
{
	private ExecutorService threadPool = null;	

	private final Logger logger = LoggerFactory.getLogger(AsyncHandler.class);

	static AsyncHandler instance=null;
	
	public static AsyncHandler getInstance(){
		if(instance==null){
			synchronized (AsyncHandler.class){
				if(instance==null){
					instance = new AsyncHandler();
				}
			}
		}
		return instance;
	}
	
	//constructor
	private AsyncHandler(){
		BlockingQueue<Runnable> linkedBlockingDeque = new LinkedBlockingDeque<Runnable>(100000);
		ExecutorService threadpool = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 600L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
		threadPool = new BetterExecutorService(linkedBlockingDeque, threadpool,"AsyncHandler", 2);
	}

	public void submit(Runnable task){
		threadPool.submit(task);
	}
}
