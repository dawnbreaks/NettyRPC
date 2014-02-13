package com.lubin.rpc.server;

import com.lubin.rpc.server.ServerConfig;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

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


		p.addLast("httpDecoder", new HttpRequestDecoder());

		p.addLast("httpAggregator",
				new HttpObjectAggregator(conf.getClientMaxBodySize()));
		p.addLast("httpDecoderAux", new RequestDecoder());
		p.addLast("httpEncoder", new HttpResponseEncoder());

		p.addLast("httpEncoderAux", new ResponseEncoder());

		p.addLast("handler", new DefaultHandler(executor));
		p.addLast("httpExceptionHandler", new DefaultExceptionHandler());
	}
}
