package com.lubin.rpc.server.http.message;

import io.netty.handler.codec.http.HttpRequest;

public class Request {

	private final HttpRequest httpRequest;

	private final long orderNumber;

	public Request(HttpRequest httpRequest, long orderNumber) {
		this.httpRequest = httpRequest;
		this.orderNumber = orderNumber;
	}

	public long getOrderNumber() {
		return orderNumber;
	}

	public HttpRequest getHttpRequest() {
		return httpRequest;
	}
}
