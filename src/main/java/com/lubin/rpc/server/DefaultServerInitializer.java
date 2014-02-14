package com.lubin.rpc.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import com.lubin.rpc.server.kryoProtocol.RequestDecoder;
import com.lubin.rpc.server.kryoProtocol.ResponseEncoder;

public class DefaultServerInitializer extends ChannelInitializer<SocketChannel> {

	private final ServerConfig conf;

	private final EventExecutorGroup executor;

	public DefaultServerInitializer(ServerConfig conf) {
		this.conf = conf;
		this.executor = new DefaultEventExecutorGroup(
				conf.getTaskThreadPoolSize());
	}

	@Override
	public void initChannel(SocketChannel ch) throws Exception {
		// Create a default pipeline implementation
		final ChannelPipeline p = ch.pipeline();

		p.addLast("decoder", new RequestDecoder());

		p.addLast("encoder", new ResponseEncoder());

		p.addLast("handler", new DefaultHandler(executor));
		
		p.addLast("httpExceptionHandler", new DefaultExceptionHandler());
	}
}
