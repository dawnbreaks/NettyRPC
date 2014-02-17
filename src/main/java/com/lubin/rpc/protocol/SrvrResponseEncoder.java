package com.lubin.rpc.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;


public class SrvrResponseEncoder extends ChannelOutboundHandlerAdapter {

//	protected void encode(ChannelHandlerContext ctx, RPCContext rpcContext, ByteBuf out) throws Exception {
//		byte[] bytes = KryoSerializer.write(rpcContext.getRes());
//		out.writeInt(bytes.length);
//		out.writeBytes(bytes);
//	}
	
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    	
    	if(msg instanceof RPCContext){
    		RPCContext rpcContext = (RPCContext)msg;
    		byte[] bytes = KryoSerializer.write(rpcContext.getResponse());
    		
    		ByteBuf byteBuf = ctx.alloc().buffer(4+bytes.length);
    		byteBuf.writeInt(bytes.length);
    		byteBuf.writeBytes(bytes);
    		ctx.writeAndFlush(byteBuf, promise); 
    	}
    }
}
