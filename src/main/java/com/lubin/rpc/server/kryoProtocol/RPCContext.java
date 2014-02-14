package com.lubin.rpc.server.kryoProtocol;

import java.util.HashMap;

public class RPCContext {
	public Request req;
	public Response res;
	public HashMap<Object,Object> attributes;
}
