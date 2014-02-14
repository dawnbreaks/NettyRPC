package com.lubin.rpc.server.http;

import static io.netty.handler.codec.http.HttpMethod.POST;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.lubin.rpc.server.http.message.FullDecodedRequest;
import com.lubin.rpc.server.http.message.Request;


public class FormPayloadDecoder extends SimpleChannelInboundHandler<Request> {

	public FormPayloadDecoder() {
		super(false);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Request request)
			throws IOException, InstantiationException, IllegalAccessException {

		HttpRequest httpRequest = request.getHttpRequest();
		Map<String, List<String>> requestParameters;

		QueryStringDecoder queryStringDecoder = new QueryStringDecoder(
				httpRequest.getUri());
		requestParameters = queryStringDecoder.parameters();

		if (httpRequest.getMethod() == POST) {
			// Add POST parameters
			HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(
					new DefaultHttpDataFactory(false), httpRequest);
			try {
				while (decoder.hasNext()) {
					InterfaceHttpData httpData = decoder.next();
					if (httpData.getHttpDataType() == HttpDataType.Attribute) {
						Attribute attribute = (Attribute) httpData;
						if (!requestParameters.containsKey(attribute.getName())) {
							requestParameters.put(attribute.getName(),
									new LinkedList<String>());
						}
						requestParameters.get(attribute.getName()).add(
								attribute.getValue());
						attribute.release();
					}
				}
			} catch (HttpPostRequestDecoder.EndOfDataDecoderException ex) {
				// Exception when the body is fully decoded, even if there
				// is still data
			}

			decoder.destroy();
		}

		Values values = new Values();
		for (Entry<String, List<String>> entry : requestParameters.entrySet()) {
			String key = entry.getKey();
			List<String> value = entry.getValue();
			if (value.size() == 1)
				values.put(key, value.get(0));
			else
				values.putStringList(key, value);
		}

		FullDecodedRequest decodedRequest = new FullDecodedRequest(request,
				values);

		ctx.fireChannelRead(decodedRequest);
	}
}
