package com.lubin.rpc.server.http.message;

import io.netty.handler.codec.http.HttpResponse;

public class FullEncodedResponse {

	private final Request request;

	private final HttpResponse httpResponse;

	public FullEncodedResponse(Request request, HttpResponse httpResponse) {
		this.request = request;
		this.httpResponse = httpResponse;
	}

	public Request getRequest() {
		return request;
	}

	public HttpResponse getHttpResponse() {
		return httpResponse;
	}
}
