package com.lubin.rpc.server;

public class ServerConfig {

	public Integer getBacklog() {
		return 128;
	}

	public Integer getPort() {
		return 9090;
	}

	public Integer getClientMaxBodySize() {
		return 1048576;
	}

	public Integer getTaskThreadPoolSize() {
		return 32;
	}
}
