package com.lubin.rpc.protocol;

import java.util.HashMap;

public class RPCContext {
	private Request request;
	private Response response;
	private HashMap<String,Object> attributes = new HashMap<String,Object>();
	
	public Request getRequest() {
		return request;
	}
	public void setRequest(Request request) {
		this.request = request;
	}
	public Response getResponse() {
		return response;
	}
	public void setResponse(Response response) {
		this.response = response;
	}
	
	
	public void setAttribute(String key,Object value){
		attributes.put(key, value);
	}
	
	public Object getAttribute(String key){
		return attributes.get(key);
	}
}
