package com.lubin.rpc.client;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.lubin.rpc.client.proxy.BaseObjectProxy;
import com.lubin.rpc.protocol.RPCContext;


public class DefaultClientHandler extends SimpleChannelInboundHandler<RPCContext>{
	
	private final Logger logger = LoggerFactory.getLogger(BaseObjectProxy.class);
    
    private ConcurrentHashMap<Long, RPCFuture> pendingRPC = new ConcurrentHashMap<Long, RPCFuture>();

    private AtomicLong seqNumGenerator = new AtomicLong(0);
		
	public long getNextSequentNumber(){
		return seqNumGenerator.getAndAdd(1);
	}
	
    private volatile Channel channel;
    
    private BaseObjectProxy  objProxy;

	private SocketAddress remotePeer;
	
	public SocketAddress getRemotePeer(){
		return remotePeer;
	}
 
	public Channel getChannel(){
        return channel;
    }
	
	public DefaultClientHandler(BaseObjectProxy objProxy) {
		this.objProxy = objProxy;
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext arg0, RPCContext rpcCtx)
			throws Exception {

		RPCFuture rpcFuture = pendingRPC.get(rpcCtx.getResponse().getSeqNum());
		
		if(rpcFuture != null){
			pendingRPC.remove(rpcCtx.getResponse().getSeqNum());
			rpcFuture.done(rpcCtx.getResponse());
		}
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		super.channelRegistered(ctx);
		channel = ctx.channel();
		
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		this.remotePeer = channel.remoteAddress();
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		super.exceptionCaught(ctx, cause);
        logger.warn("Unexpected exception from downstream.", cause);
        ctx.close();
        //will result in duplicated connections
//        objProxy.doReconnect(ctx.channel(), remotePeer);
	}
	
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		objProxy.reconnect(ctx.channel(), remotePeer);
	}
	
	
	public void close(){
	    channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }
	
	public RPCFuture doRPC(RPCContext rpcCtx){
		RPCFuture rpcFuture = new RPCFuture(rpcCtx, this);
		pendingRPC.put(rpcCtx.getRequest().getSeqNum(), rpcFuture);
		channel.writeAndFlush(rpcCtx);
		return rpcFuture;
	}
	
	public void doNotify(RPCContext rpcCtx){
		channel.writeAndFlush(rpcCtx);
	}
}
