package com.lubin.rpc.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.EventExecutorGroup;

import com.lubin.rpc.server.kryoProtocol.RPCContext;
import com.lubin.rpc.server.kryoProtocol.Request;
import com.lubin.rpc.server.kryoProtocol.Response;


public class DefaultHandler extends SimpleChannelInboundHandler<RPCContext> {

	private final EventExecutorGroup executor;

	public DefaultHandler(EventExecutorGroup executor) {
		super(false);
		this.executor = executor;
	}

	@Override
	protected void channelRead0(final ChannelHandlerContext ctx, final RPCContext rpcContext) throws Exception {
		Request req = rpcContext.req;
		
		Response res= new Response();
		//copy properties
		res.seq = req.seq;
		res.version = req.version;
		res.type = req.type;
		res.objName = req.objName;
		res.funcName = req.funcName;
		
		rpcContext.res = res;
		
		ctx.write(rpcContext);
		//todo...
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
