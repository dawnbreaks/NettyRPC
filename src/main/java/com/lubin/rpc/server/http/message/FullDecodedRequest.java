package com.lubin.rpc.server.http.message;

import com.lubin.rpc.server.http.Values;

import io.netty.handler.codec.http.QueryStringDecoder;

public class FullDecodedRequest {

	private final Request request;

	private final Values values;

	public FullDecodedRequest(Request request, Values values) {
		this.request = request;
		this.values = values;
	}

	public Request getRequest() {
		return request;
	}

	public Values getValues() {
		return values;
	}
	
	public String getPath() {
		QueryStringDecoder queryStringDecoder = new QueryStringDecoder(
				request.getHttpRequest().getUri());
		return queryStringDecoder.path();
	}
}
