package com.lubin.rpc.client;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

import com.lubin.rpc.client.proxy.AsyncRPCCallback;
import com.lubin.rpc.protocol.RPCContext;
import com.lubin.rpc.protocol.Response;
import com.lubin.rpc.server.Constants;



public class RPCFuture implements Future<Object>{

    private Sync sync;
	private RPCContext rpcCtx;

	private AsyncRPCCallback callback;

	private DefaultClientHandler handler;
    
    static class Sync extends AbstractQueuedSynchronizer {
  
		private static final long serialVersionUID = 1L;
		
		//future status
		private final int done = 1;
		private final int pending = 0;

		protected boolean tryAcquire(int acquires) {
            return getState()==done?true:false;
        }

        protected  boolean tryRelease(int releases) {
            if (getState() == pending) {
                if (compareAndSetState(pending, 1)) {
                    return true;
                }
            }
			return false;
        }
        
        public boolean isDone(){
        	getState();
			return getState()==done;
        }
    }



	//Constructor
	public RPCFuture (RPCContext rpcCtx,DefaultClientHandler handler, AsyncRPCCallback callback){
		this.sync = new Sync();
		this.rpcCtx = rpcCtx;
		this.handler = handler;
		this.callback = callback;
	}
	
	//Constructor
	public RPCFuture (RPCContext rpcCtx,DefaultClientHandler handler){
		this.sync = new Sync();
		this.rpcCtx = rpcCtx;
		this.handler = handler;
		this.callback = null;
	}
	
	@Override
	public boolean isDone() {
		return sync.isDone();
	}
	
	@Override
	public Object get() throws InterruptedException, ExecutionException {
		sync.acquire(-1);
		return processResponse();
	}


	@Override
	public Object get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		boolean success = sync.tryAcquireNanos(-1, unit.toNanos(timeout));

		if(success){
			return processResponse();
		}else{
			throw new RuntimeException("Timeout exception|objName="+rpcCtx.getRequest().getObjName()+"|funcName="+rpcCtx.getRequest().getFuncName());
		}
	}

	@Override
	public boolean isCancelled() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		throw new UnsupportedOperationException();
	}
	
	
	//wake up caller thread or summit task to excute async callback , will be called by event loop thread when received response from Server.
	public void done(Response reponse){
		this.rpcCtx.setResponse(reponse);
		char type = rpcCtx.getRequest().getType();
		if(type== Constants.RPCType.normal){//wake up caller thread 
			sync.release(1);
		}else if(type== Constants.RPCType.async ){//submit task to execute async callback 
			sync.release(1);
			
			if(callback != null){
				RPCClient.submit(new Runnable(){
					@Override
					public void run() {
						Response response = rpcCtx.getResponse();
						char status = response.getStatus();
						if(status == Constants.RPCStatus.ok){
							callback.success(response.getResult());
						}else if(status == Constants.RPCStatus.exception){
							callback.fail(new RuntimeException("Got exception in server|objName="+rpcCtx.getRequest().getObjName()+"|funcName="+rpcCtx.getRequest().getFuncName()+"|server msg="+response.getMsg()));
						}else if(status == Constants.RPCStatus.unknownError){
							callback.fail(new RuntimeException("Got unknown error in server|objName="+rpcCtx.getRequest().getObjName()+"|funcName="+rpcCtx.getRequest().getFuncName()+"|server msg="+response.getMsg()));
						}
					}
				});
			}


		}else if(type== Constants.RPCType.oneway){
			//oneway call wonn't got a response from server.
			
		}
	}

    //call by caller thread to get result
	private Object processResponse() {

		char type = rpcCtx.getRequest().getType();
		
		if(type == Constants.RPCType.normal||type == Constants.RPCType.async){//process response to return result or throw error/exception.
			
			Response response = rpcCtx.getResponse();
			char status = response.getStatus();
			
			if(status == Constants.RPCStatus.exception){
				throw new RuntimeException("Got exception in server|objName="+rpcCtx.getRequest().getObjName()+"|funcName="+rpcCtx.getRequest().getFuncName()+"|server msg="+response.getMsg());
			}else if(status == Constants.RPCStatus.unknownError){
				throw new RuntimeException("Got unknown error in server|objName="+rpcCtx.getRequest().getObjName()+"|funcName="+rpcCtx.getRequest().getFuncName()+"|server msg="+response.getMsg());
			}
		}
		return rpcCtx.getResponse().getResult();
	}
}
