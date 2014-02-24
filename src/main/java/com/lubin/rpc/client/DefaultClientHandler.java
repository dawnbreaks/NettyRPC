package com.lubin.rpc.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lubin.rpc.client.proxy.AsyncRPCCallback;
import com.lubin.rpc.client.proxy.BaseObjectProxy;
import com.lubin.rpc.protocol.RPCContext;


public class DefaultClientHandler extends SimpleChannelInboundHandler<RPCContext>{
	
    private static final Logger logger = Logger.getLogger(DefaultClientHandler.class.getName());
    
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
        logger.log( Level.WARNING, "Unexpected exception from downstream.", cause);
        ctx.close();
        objProxy.doReconnect(ctx.channel(), remotePeer);
	}
	
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		objProxy.doReconnect(ctx.channel(), remotePeer);
	}
	

	
	public RPCFuture doRPC(RPCContext rpcCtx){
		return doRPC(rpcCtx,null);
	}
	
	
	public RPCFuture doRPC(RPCContext rpcCtx, AsyncRPCCallback callback){
		RPCFuture rpcFuture = new RPCFuture(rpcCtx, this, callback);
		pendingRPC.put(rpcCtx.getRequest().getSeqNum(), rpcFuture);
		channel.writeAndFlush(rpcCtx);
		return rpcFuture;
	}
	
	public void doNotify(RPCContext rpcCtx){
		channel.writeAndFlush(rpcCtx);
	}
}
