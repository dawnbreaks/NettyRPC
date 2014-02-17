package com.lubin.rpc.client;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lubin.rpc.server.kryoProtocol.RPCContext;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;


public class ObjectProxyHandler extends SimpleChannelInboundHandler<RPCContext>{
	
    private static final Logger logger = Logger.getLogger(ObjectProxyHandler.class.getName());
    
    static  Lock lock = new ReentrantLock();
    
    static ConcurrentHashMap<Long, Condition> pendingRPCThread = new ConcurrentHashMap<Long, Condition>();
    static ConcurrentHashMap<Long, RPCContext> pendingRPCCtx = new ConcurrentHashMap<Long, RPCContext>();
    
    private volatile Channel channel;
    
    public ObjectProxyHandler(){
    	
    }
	@Override
	protected void channelRead0(ChannelHandlerContext arg0, RPCContext rpcCtx)
			throws Exception {
		
		Condition condition = pendingRPCThread.get(rpcCtx.getRes().getSeqNum());
		RPCContext oriRpcCtx = pendingRPCCtx.get(rpcCtx.getRes().getSeqNum());
		oriRpcCtx.setRes(rpcCtx.getRes());
		if(condition!=null && oriRpcCtx !=null){
			try{
				lock.lock();
				condition.signal();
			}finally{
				lock.unlock();
			}
		}

	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		super.channelRegistered(ctx);
		channel = ctx.channel();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		super.exceptionCaught(ctx, cause);
        logger.log( Level.WARNING, "Unexpected exception from downstream.", cause);
        ctx.close();
	}
	
	
	public void doRPC(RPCContext rpcCtx){
		
		//ugly,  todo ......
		try {
			if(channel!=null)
				Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
		try{
			lock.lock();
			Condition con = lock.newCondition();
			pendingRPCThread.put(rpcCtx.getReq().getSeqNum(), con);
			pendingRPCCtx.put(rpcCtx.getReq().getSeqNum(), rpcCtx);
			channel.writeAndFlush(rpcCtx);
			boolean notTimeout = con.await(3000, TimeUnit.MILLISECONDS);
			if(!notTimeout)
				throw new RuntimeException("time outexception");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally{
			lock.unlock();
		}

	}

}
