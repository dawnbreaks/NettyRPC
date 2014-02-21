package com.lubin.rpc.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.Method;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.lubin.rpc.client.proxy.BaseObjectProxy;
import com.lubin.rpc.protocol.RPCContext;
import com.lubin.rpc.protocol.Request;
import com.lubin.rpc.protocol.Response;
import com.lubin.rpc.thread.BetterExecutorService;



public class DefaultHandler extends SimpleChannelInboundHandler<RPCContext> {


	public static void submit(Runnable task){
		if(threadPool != null){
			synchronized (BaseObjectProxy.class) {
				if(threadPool!=null){
					LinkedBlockingDeque<Runnable> linkedBlockingDeque = new LinkedBlockingDeque<Runnable>();
					ThreadPoolExecutor executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 600L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
					threadPool = new BetterExecutorService(linkedBlockingDeque, executor,"Client async thread pool",BaseObjectProxy.getConfig().getInt("server.asyncThreadPoolSize"));
				}
			}
		}
		
		threadPool.submit(task);
	}
	
	private static BetterExecutorService threadPool;
	
	
	public DefaultHandler() {
		super(false);
	}

	@Override
	protected void channelRead0(final ChannelHandlerContext ctx, final RPCContext rpcContext) throws Exception {
		if(RPCServer.getConfig().getBoolean("server.async")){
			DefaultHandler.submit(new Runnable(){
				@Override
				public void run() {
					processRequest(ctx,rpcContext);
				}
				
			});
		}else{
			processRequest(ctx,rpcContext);
		}
	}	

	public void processRequest(ChannelHandlerContext ctx, RPCContext rpcContext){
		
		try{
			
			Request req = rpcContext.getRequest();
			Response res= new Response();
			
			//copy properties
			res.setSeqNum(req.getSeqNum());
			res.setVersion(req.getVersion());
			res.setType(req.getType());
			res.setObjName(req.getObjName());
			res.setFuncName(req.getFuncName());
			
			Class[] parameterTypes = new Class[req.getArgs().length];
			int i=0;
			for(Object arg : req.getArgs()){
				parameterTypes[i++] = arg.getClass();
			}
			

			Object obj= RPCServer.getObject(req.getObjName());
			Class clazz= obj.getClass();
			Method func = clazz.getMethod(req.getFuncName(), parameterTypes);
			Object result= func.invoke(obj, req.getArgs());
			
			res.setResult(result);
			res.setStatus(Constants.RPCStatus.ok);
			res.setMsg("ok");

			rpcContext.setResponse(res);
			ctx.write(rpcContext);
		} catch (Exception e) {

			Request req = rpcContext.getRequest();
			Response res= new Response();
			//copy properties
			res.setSeqNum(req.getSeqNum());
			res.setVersion(req.getVersion());
			res.setType(req.getType());
			res.setObjName(req.getObjName());
			res.setFuncName(req.getFuncName());
			
			//pass exception message to client
			res.setStatus(Constants.RPCStatus.exception);
			res.setMsg("excepton="+e.getClass().getSimpleName()+"|msg="+e.getMessage());
			
			rpcContext.setResponse(res);
			ctx.writeAndFlush(rpcContext);
		}
		
	}
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		// TODO(adolgarev): cancel submitted tasks,
		// that works only for not in progress tasks
		// if (future != null && !future.isDone()) {
		// future.cancel(true);
		// }
	}

}
