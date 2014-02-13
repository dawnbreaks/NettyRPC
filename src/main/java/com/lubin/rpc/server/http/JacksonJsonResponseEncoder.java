package com.lubin.rpc.server.http;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.CharsetUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.lubin.rpc.server.http.message.FullEncodedResponse;
import com.lubin.rpc.server.http.message.Response;

public class JacksonJsonResponseEncoder extends ChannelOutboundHandlerAdapter {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	static {
		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg,
			ChannelPromise promise) throws Exception {
		if (msg instanceof HttpResponse) {
			super.write(ctx, msg, promise);
			return;
		}

		Response response = (Response) msg;

		String res;
		try {
			res = objectMapper.writeValueAsString(response.getPayload());
		} catch (Exception ex) {
			ctx.fireExceptionCaught(ex);
			return;
		}

		// Build the response object
		FullHttpResponse httpResponse = new DefaultFullHttpResponse(HTTP_1_1,
				OK, Unpooled.copiedBuffer(res, CharsetUtil.UTF_8));

		httpResponse.headers().set(CONTENT_TYPE, "application/json");
		httpResponse.headers().set(CONTENT_LENGTH,
				httpResponse.content().readableBytes());

		FullEncodedResponse encodedResponse = new FullEncodedResponse(
				response.getRequest(), httpResponse);

		// Write the response
		ctx.write(encodedResponse, promise);
	}
}
