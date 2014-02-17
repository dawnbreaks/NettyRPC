package com.lubin.rpc.client;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lubin.rpc.protocol.RPCContext;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;


public class ObjectProxyHandler extends SimpleChannelInboundHandler<RPCContext>{
	
    private static final Logger logger = Logger.getLogger(ObjectProxyHandler.class.getName());
    
    Lock lock = new ReentrantLock();
    
    private ConcurrentHashMap<Long, Condition> pendingRPCThread = new ConcurrentHashMap<Long, Condition>();
    private ConcurrentHashMap<Long, RPCContext> pendingRPCCtx = new ConcurrentHashMap<Long, RPCContext>();
    
    private AtomicLong seqNumGenerator = new AtomicLong(0);
		
	public long getNextSequentNumber(){
		return seqNumGenerator.getAndAdd(1);
	}
	
    private volatile Channel channel;
    
    public ObjectProxyHandler(){
    	
    }
	@Override
	protected void channelRead0(ChannelHandlerContext arg0, RPCContext rpcCtx)
			throws Exception {
		
		Condition condition = pendingRPCThread.get(rpcCtx.getResponse().getSeqNum());
		RPCContext oriRpcCtx = pendingRPCCtx.get(rpcCtx.getResponse().getSeqNum());
		oriRpcCtx.setResponse(rpcCtx.getResponse());
		
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
			if(channel==null){
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
		try{
			lock.lock();
			Condition con = lock.newCondition();
			pendingRPCThread.put(rpcCtx.getRequest().getSeqNum(), con);
			pendingRPCCtx.put(rpcCtx.getRequest().getSeqNum(), rpcCtx);
			channel.writeAndFlush(rpcCtx);
			boolean success = con.await(3000, TimeUnit.MILLISECONDS);
			if(!success){
				pendingRPCThread.remove(rpcCtx.getRequest().getSeqNum());
				pendingRPCCtx.remove(rpcCtx.getRequest().getSeqNum());
				throw new RuntimeException("Timeout exception|objName="+rpcCtx.getRequest().getObjName()+"|funcName="+rpcCtx.getRequest().getFuncName());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally{
			lock.unlock();
		}

	}

}
