package com.lubin.rpc.server.kryoProtocol;

import java.util.HashMap;

public class RPCContext {
	private Request req;
	private Response res;
	private HashMap<String,Object> attributes = new HashMap<String,Object>();
	
	
	public Request getReq() {
		return req;
	}
	public void setReq(Request req) {
		this.req = req;
	}
	public Response getRes() {
		return res;
	}
	public void setRes(Response res) {
		this.res = res;
	}
	
	
	public void setAttribute(String key,Object value){
		attributes.put(key, value);
	}
	
	public Object getAttribute(String key){
		return attributes.get(key);
	}
}
