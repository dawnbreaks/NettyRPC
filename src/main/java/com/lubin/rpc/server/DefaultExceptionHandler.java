package com.lubin.rpc.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lubin.rpc.protocol.Request;
import com.lubin.rpc.protocol.Response;
import com.lubin.rpc.server.exception.BadRequestException;

public class DefaultExceptionHandler extends ChannelInboundHandlerAdapter {

	private final Logger logger = LoggerFactory
			.getLogger(DefaultExceptionHandler.class);

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		
		logger.error("Exception caught", cause);

		if(cause instanceof BadRequestException){	//application layer exception.
			BadRequestException exc = (BadRequestException)cause;
			Request req = exc.context.getRequest();
			Response res= new Response();
			//copy properties
			res.setSeqNum(req.getSeqNum());
			res.setVersion(req.getVersion());
			res.setType(req.getType());
			res.setObjName(req.getObjName());
			res.setFuncName(req.getFuncName());
			
			//pass exception message to client
			res.setStatus(Constants.RPCStatus.exception);
			res.setMsg(exc.getMessage());
			
			ctx.writeAndFlush(res);
		}else{	// unknow error
			ctx.close();
		}
	}
}
