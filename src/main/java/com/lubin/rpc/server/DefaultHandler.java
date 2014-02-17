package com.lubin.rpc.server;

import java.lang.reflect.Method;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.EventExecutorGroup;

import com.lubin.rpc.protocol.RPCContext;
import com.lubin.rpc.protocol.Request;
import com.lubin.rpc.protocol.Response;


public class DefaultHandler extends SimpleChannelInboundHandler<RPCContext> {

	private final EventExecutorGroup executor;

	public DefaultHandler(EventExecutorGroup executor) {
		super(false);
		this.executor = executor;
	}

	@Override
	protected void channelRead0(final ChannelHandlerContext ctx, final RPCContext rpcContext) throws Exception {
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
