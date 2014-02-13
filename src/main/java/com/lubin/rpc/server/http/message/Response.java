package com.lubin.rpc.server.http.message;


public class Response {

	private Request request;

	private Object payload;

	public Response(Request request, Object payload) {
		this.request = request;
		this.payload = payload;
	}

	public Request getRequest() {
		return request;
	}

	public Object getPayload() {
		return payload;
	}
}
