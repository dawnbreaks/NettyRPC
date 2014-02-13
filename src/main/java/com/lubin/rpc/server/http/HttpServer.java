package com.lubin.rpc.server.http;

import com.lubin.rpc.server.ServerConfig;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class HttpServer {

	private final ServerConfig conf;

	public HttpServer(ServerConfig conf) {
		this.conf = conf;
	}

	public void run() throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(new DefaultServerInitializer(conf))
					.option(ChannelOption.SO_BACKLOG, conf.getBacklog())
					.option(ChannelOption.SO_REUSEADDR, true);

			Channel ch = b.bind(conf.getPort()).sync().channel();
			ch.closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	public static void main(String[] args) throws Exception {
		new HttpServer(new ServerConfig()).run();
	}
}
