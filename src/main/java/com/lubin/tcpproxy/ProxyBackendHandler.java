
package com.lubin.tcpproxy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ProxyBackendHandler extends ChannelInboundHandlerAdapter {

	private ProxyFrontendHandler proxyFrondtendHandle;

    public ProxyBackendHandler(ProxyFrontendHandler proxyFrondtendHandle) {
        this.proxyFrondtendHandle=proxyFrondtendHandle;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    	proxyFrondtendHandle.outBoundChannelReady();
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
    	proxyFrondtendHandle.getInboundChannel().writeAndFlush(msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ProxyFrontendHandler.closeOnFlush(proxyFrondtendHandle.getInboundChannel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ProxyFrontendHandler.closeOnFlush(ctx.channel());
    }
}