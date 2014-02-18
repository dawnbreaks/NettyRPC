package com.lubin.rpc.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import com.lubin.rpc.protocol.SrvrRequestDecoder;
import com.lubin.rpc.protocol.SrvrResponseEncoder;

public class DefaultServerInitializer extends ChannelInitializer<SocketChannel> {

	private ServerConfig conf;

	public DefaultServerInitializer(ServerConfig conf) {
		this.conf = conf;
	}

	@Override
	public void initChannel(SocketChannel ch) throws Exception {
		// Create a default pipeline implementation
		final ChannelPipeline p = ch.pipeline();

		p.addLast("decoder", new SrvrRequestDecoder());

		p.addLast("encoder", new SrvrResponseEncoder());

		p.addLast("handler", new DefaultHandler(conf));
		
		p.addLast("httpExceptionHandler", new DefaultExceptionHandler());
	}
}
