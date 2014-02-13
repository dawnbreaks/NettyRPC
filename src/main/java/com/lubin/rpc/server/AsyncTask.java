package com.lubin.rpc.server;

import java.util.concurrent.Callable;

public class AsyncTask implements Callable<Object> {

	private final String path;
	
	private final Values values;



	public AsyncTask(String path, Values values) {
		this.path = path;
		this.values = values;
	}

	@Override
	public Object call() throws Exception {
		Object res = null;


		
		res = "Ok";

		return res;
		// throw new RuntimeException("Hello, Exception!");
	}
}
