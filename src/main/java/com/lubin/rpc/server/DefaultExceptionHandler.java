package com.lubin.rpc.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lubin.rpc.server.exception.BadRequestException;
import com.lubin.rpc.server.kryoProtocol.Request;
import com.lubin.rpc.server.kryoProtocol.Response;

public class DefaultExceptionHandler extends ChannelInboundHandlerAdapter {

	private final Logger logger = LoggerFactory
			.getLogger(DefaultExceptionHandler.class);

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		
		logger.error("Exception caught", cause);

		if(cause instanceof BadRequestException){	//application layer exception.
			BadRequestException exc = (BadRequestException)cause;
			Request req = exc.context.req;
			Response res= new Response();
			//copy properties
			res.seq = req.seq;
			res.version = req.version;
			res.type = req.type;
			res.objName = req.objName;
			res.funcName = req.funcName;
			
			//pass exception message to client
			res.status = Constants.RPCStatus.exception;
			res.msg = exc.getMessage();
			
			ctx.writeAndFlush(res);
		}else{	// unknow error
			ctx.close();
		}
	}
}
