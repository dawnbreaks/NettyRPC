package com.lubin.rpc.server.exception;

import com.lubin.rpc.protocol.RPCContext;

public class BadRequestException extends Exception {

	public RPCContext context;
	private static final long serialVersionUID = 8166629097983704842L;

	public BadRequestException(RPCContext context, Throwable cause) {
		super(cause);
		this.context = context;
	}
}
