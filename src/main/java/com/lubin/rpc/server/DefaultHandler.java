package com.lubin.rpc.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lubin.rpc.protocol.Constants;
import com.lubin.rpc.protocol.RPCContext;
import com.lubin.rpc.protocol.Request;
import com.lubin.rpc.protocol.Response;



public class DefaultHandler extends SimpleChannelInboundHandler<RPCContext> {

	private final Logger logger = LoggerFactory.getLogger(DefaultHandler.class);
	
	public DefaultHandler() {
		super(false);
	}

	@Override
	protected void channelRead0(final ChannelHandlerContext ctx, final RPCContext rpcContext) throws Exception {
		if(RPCServer.getConfig().getBoolean("server.async")){
			RPCServer.submit(new Runnable(){
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
		Request req = rpcContext.getRequest();
		Response res= new Response();
		
		//copy properties
		res.setSeqNum(req.getSeqNum());
		res.setVersion(req.getVersion());
		res.setType(req.getType());
		res.setSerializer(req.getSerializer());
		res.setObjName(req.getObjName());
		res.setFuncName(req.getFuncName());

		try{
		    
			Object[] args = req.getArgs();
			Class[] argTypes = new Class[args.length];
			String methodKey ="";
			for(int i=0;i<args.length;i++){
				argTypes[i] = args[i].getClass();
				methodKey+=argTypes[i].getSimpleName();
			}
	
			Object obj= RPCServer.getObject(req.getObjName());
			Class clazz= obj.getClass();
			Method func = clazz.getMethod(req.getFuncName(), argTypes);
			Object result= func.invoke(obj, req.getArgs());
			
			if(req.getType() != Constants.RPCType.oneway){
				res.setResult(result);
				res.setStatus(Constants.RPCStatus.ok);
				res.setMsg("ok");

				rpcContext.setResponse(res);
				ctx.writeAndFlush(rpcContext);
			}
			
		} catch (Exception e) {
			
			//pass exception message to client
			if(req.getType() != Constants.RPCType.oneway){
				res.setStatus(Constants.RPCStatus.exception);
				res.setMsg("excepton="+e.getClass().getSimpleName()+"|msg="+e.getMessage());
				
				rpcContext.setResponse(res);
				ctx.writeAndFlush(rpcContext);
			}
			
			logger.error("DefaultHandler|processRequest got error",e);
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
