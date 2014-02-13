package com.lubin.rpc.server.http;

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

		// Uncomment the following line if you want HTTPS
		// SSLEngine engine =
		// SecureChatSslContextFactory.getServerContext().createSSLEngine();
		// engine.setUseClientMode(false);
		// p.addLast("ssl", new SslHandler(engine));
		p.addLast("httpDecoder", new HttpRequestDecoder());
		// Don't want to handle HttpChunks
		p.addLast("httpAggregator",
				new HttpObjectAggregator(conf.getClientMaxBodySize()));
		p.addLast("httpDecoderAux", new RequestDecoder());
		p.addLast("httpEncoder", new HttpResponseEncoder());
		// Automatic content compression
//		p.addLast("httpDeflater", new HttpContentCompressor());
		p.addLast("httpEncoderAux", new ResponseEncoder());
		p.addLast("httpPayloadEncoder", new JacksonJsonResponseEncoder());
		p.addLast("httpPayloadDecoder", new FormPayloadDecoder());
		p.addLast("handler", new DefaultHandler(executor));
		p.addLast("httpExceptionHandler", new DefaultExceptionHandler());
	}
}
