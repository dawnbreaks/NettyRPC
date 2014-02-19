package com.lubin.rpc.client;

public interface Reconnectable {
	void reconnect(DefaultClientHandler newHandler);
}
