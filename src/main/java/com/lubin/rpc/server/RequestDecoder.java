package com.lubin.rpc.server;

import com.lubin.rpc.server.http.message.Request;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;

public class RequestDecoder extends SimpleChannelInboundHandler<HttpObject> {

	private long orderNumber;

	public RequestDecoder() {
		// Do not autorelease HttpObject since
		// it is passed through
		super(false);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HttpObject httpObject)
			throws Exception {

		DecoderResult result = httpObject.getDecoderResult();
		if (!result.isSuccess()) {
			throw new BadRequestException(result.cause());
		}

		if (httpObject instanceof HttpRequest) {
			HttpRequest httpRequest = (HttpRequest) httpObject;
			ctx.fireChannelRead(new Request(httpRequest, orderNumber));
			orderNumber += 1;
		}
	}
}
