
package com.lubin.tcpproxy;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ProxyBackendHandler extends ChannelInboundHandlerAdapter {

    private final Channel inboundChannel;
	private ProxyFrontendHandler proxyFrondtendHandle;

    public ProxyBackendHandler(ProxyFrontendHandler proxyFrondtendHandle) {
        this.inboundChannel = proxyFrondtendHandle.getInboundChannel();
        this.proxyFrondtendHandle=proxyFrondtendHandle;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    	proxyFrondtendHandle.outBoundReady();
        System.out.println("ProxyBackendHandler.channelActive");
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
    	 System.out.println("ProxyBackendHandler.channelRead"+msg);
        inboundChannel.writeAndFlush(msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ProxyFrontendHandler.closeOnFlush(inboundChannel);
        System.out.println("ProxyBackendHandler.channelInactive");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ProxyFrontendHandler.closeOnFlush(ctx.channel());
    }
}