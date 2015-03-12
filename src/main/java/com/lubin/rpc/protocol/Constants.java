package com.lubin.rpc.protocol;

public class Constants {
	
    public static int headerLen = 5;
    
	public interface RPCStatus {
		char ok = 0;
		char exception = 1;
		char unknownError = 2;
	}
	
	public interface RPCType {
	    byte normal = 0;
	    byte oneway = 1;
	    byte async = 2;
	}
	
	public interface MessageType {
        byte request = 0;
        byte response = 1;
    }
	
	public interface RPCSerializer {
		byte kryo = 0;
		byte json = 1;
		byte msgpack = 2;
		byte bson = 3;
	}
}
