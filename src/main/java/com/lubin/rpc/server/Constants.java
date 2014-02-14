package com.lubin.rpc.server;

public class Constants {
	
	public interface RPCStatus {
		char ok = 0;
		char exception = 1;
		char unknownError = 2;
	}
	
	public interface RPCType {
		char normal = 0;
		char oneway = 1;
		char async = 2;
	}
}
