
package com.lubin.tcpproxy;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class FrontendInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
		ch.pipeline().addLast(
                new LoggingHandler(LogLevel.INFO),
                new ProxyFrontendHandler());
    }
}
