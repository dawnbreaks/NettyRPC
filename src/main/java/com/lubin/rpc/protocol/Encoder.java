package com.lubin.rpc.protocol;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;


public class Encoder extends ChannelOutboundHandlerAdapter {
	
	private static final ObjectMapper objectMapper = new ObjectMapper();

	static {
		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	}
	
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    	
    	if(msg instanceof RPCContext){
    		RPCContext rpcContext = (RPCContext)msg;
    		Object objToEncode;
    		char serializer;
    		if(rpcContext.getResponse() !=null){
    			objToEncode = rpcContext.getResponse();
    			serializer = rpcContext.getResponse().getSerializer();
    		}else{
    			objToEncode = rpcContext.getRequest();
    			serializer = rpcContext.getRequest().getSerializer();
    		}
    		
    		byte[] bytes;
    		if(serializer == Constants.RPCSerializer.kryo){
    			bytes = KryoSerializer.write(objToEncode);
    		}else if(serializer == Constants.RPCSerializer.json){
    			bytes = objectMapper.writeValueAsBytes(objToEncode);
    		}else{
    			bytes = KryoSerializer.write(objToEncode);
    		}
    		
    		ByteBuf byteBuf = ctx.alloc().buffer(6+bytes.length);
    		//header
    		byteBuf.writeInt(bytes.length);
    		byteBuf.writeChar(serializer);
    		//body
    		byteBuf.writeBytes(bytes);
    		ctx.writeAndFlush(byteBuf, promise); 
    	}
    }
}
