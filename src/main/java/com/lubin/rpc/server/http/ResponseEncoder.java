package com.lubin.rpc.server.http;

import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static io.netty.handler.codec.http.HttpHeaders.Names.SET_COOKIE;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.ServerCookieEncoder;
import io.netty.util.ReferenceCountUtil;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.lubin.rpc.server.http.message.FullEncodedResponse;


public class ResponseEncoder extends ChannelOutboundHandlerAdapter {

	private static final String SESSION_COOKIE_NAME = "JSESSIOINID";
	private static final SecureRandom random = new SecureRandom();
	private final Map<Long, HttpResponse> pendingResponses = new HashMap<Long, HttpResponse>();
	private long orderNumber;

	@Override
	public void write(ChannelHandlerContext ctx, Object msg,
			ChannelPromise promise) throws Exception {
		if (!(msg instanceof FullEncodedResponse)) {
			super.write(ctx, msg, promise);
			return;
		}

		FullEncodedResponse encodedResponse = (FullEncodedResponse) msg;

		HttpResponse httpResponse = encodedResponse.getHttpResponse();
		HttpRequest httpRequest = encodedResponse.getRequest().getHttpRequest();

		/*String cookieString = httpRequest.headers().get(COOKIE);
		Boolean hasSessionId = false;
		if (cookieString != null) {
			Set<Cookie> cookies = CookieDecoder.decode(cookieString);
			if (!cookies.isEmpty()) {
				// Reset the cookies if necessary.
				for (Cookie cookie : cookies) {
					if (cookie.getName().equals(SESSION_COOKIE_NAME)) {
						hasSessionId = true;
					}
					httpResponse.headers().add(SET_COOKIE,
							ServerCookieEncoder.encode(cookie));
				}
			}
		}
		if (!hasSessionId) {
			httpResponse.headers().add(
					SET_COOKIE,
					ServerCookieEncoder.encode(SESSION_COOKIE_NAME,
							nextSessionId()));
		}*/

		Boolean keepAlive = isKeepAlive(httpRequest);

		if (keepAlive) {
			// Add keep alive header as per
			// http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
			httpResponse.headers().set(CONNECTION,
					HttpHeaders.Values.KEEP_ALIVE);
		} else {
			httpResponse.headers().set(CONNECTION, HttpHeaders.Values.CLOSE);
		}

                ReferenceCountUtil.release(httpRequest);
                if (encodedResponse.getRequest().getOrderNumber() == orderNumber) {
                    ctx.writeAndFlush(httpResponse, promise);
                    orderNumber += 1;
                }
                else {
                    pendingResponses.put(encodedResponse.getRequest().getOrderNumber(),
                                    httpResponse);
                    sendPending(ctx, promise);
                }
	}

	private void sendPending(ChannelHandlerContext ctx,
			ChannelPromise promise) {
		while (true) {
			HttpResponse response = pendingResponses.remove(orderNumber);
			if (response == null)
				break;
			ctx.write(response, promise);
			orderNumber += 1;
		}
		if (pendingResponses.isEmpty())
			ctx.flush();
	}

	private String nextSessionId() {
		return new BigInteger(130, random).toString(32);
	}
}
