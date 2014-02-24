package com.lubin.rpc.protocol;

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
	
	public interface RPCSerializer {
		char kryo = 0;
		char json = 1;
		char msgpack = 2;
		char bson = 3;
	}
}
