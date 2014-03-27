
package com.lubin.tcpproxy;

import java.net.InetSocketAddress;
import java.util.LinkedList;

import com.lubin.tcpproxy.TcpProxyServer.ProxyHost;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;

public class ProxyFrontendHandler extends ChannelInboundHandlerAdapter {

  
	private LinkedList<Object> buffer = new LinkedList<Object> ();

    private volatile Channel outboundChannel;
    
    private volatile Channel inboundChannel;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    	inboundChannel = ctx.channel();

        InetSocketAddress localAddress = (InetSocketAddress) inboundChannel.localAddress();
        int port = localAddress.getPort();
        ProxyHost outboundRemoteHost = TcpProxyServer.getProxyHosts().get(port);
  
        // Start the connection attempt.
        Bootstrap b = new Bootstrap();
        b.group(inboundChannel.eventLoop())
        	.channel(ctx.channel().getClass())
        	.handler(new BackendInitializer(this))
        	.option(ChannelOption.SO_BACKLOG, TcpProxyServer.getConfig().getInt("tcpProxyServer.SO_BACKLOG"))
        	.option(ChannelOption.SO_REUSEADDR, true)
        	.option(ChannelOption.SO_TIMEOUT, TcpProxyServer.getConfig().getInt("tcpProxyServer.SO_TIMEOUT"))
        	.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TcpProxyServer.getConfig().getInt("tcpProxyServer.CONNECT_TIMEOUT_MILLIS"))
//        	.option(ChannelOption.AUTO_READ, false)
        	.option(ChannelOption.SO_KEEPALIVE, true);
        
//         .option(ChannelOption.AUTO_READ, false);
        ChannelFuture f = b.connect(outboundRemoteHost.getRemoteHost(), outboundRemoteHost.getRemotePort());
        outboundChannel = f.channel();
    }

    public Channel getInboundChannel() {
		return inboundChannel;
	}

	@Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
		
    	synchronized (buffer) {
    		if(outboundChannel.isActive()){
        		for(Object ojb : buffer){
        			outboundChannel.writeAndFlush(ojb);
        		}
        		buffer.clear();
        		outboundChannel.writeAndFlush(msg);
        	}else{
        		buffer.add(msg);
        	}
		}
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (outboundChannel != null) {
            closeOnFlush(outboundChannel);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        closeOnFlush(ctx.channel());
    }

    /**
     * Closes the specified channel after all queued write requests are flushed.
     */
    static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

	public void outBoundChannelReady() {
		synchronized (buffer) {
			if(outboundChannel.isActive()){
        		for(Object ojb : buffer){
        			outboundChannel.writeAndFlush(ojb);
        		}
        		buffer.clear();
        	}
		}
	}
}