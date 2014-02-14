package com.lubin.rpc.server;

public class Constants {
	
	public interface RPCStatus {
		int ok = 0;
		int exception = 1;
		int unknownError = 2;
	}
	
	public interface RPCType {
		int normal = 0;
		int oneway = 1;
		int async = 2;
	}
}
