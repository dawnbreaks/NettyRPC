package com.lubin.rpc.server.kryoProtocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.ByteBuffer;
import java.util.List;

public class RequestDecoder extends ByteToMessageDecoder  {
	
	private int packetLength = 0;
	
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) { 
    	
    	if(packetLength ==0 && in.readableBytes() < 4)	{
    		return; 
    	}else if(packetLength ==0 && in.readableBytes() >= 4){
	   	     packetLength =  in.readInt();
    	}
 
        //  we have got the length of the package
        if (packetLength > 0) {
            if (in.readableBytes() < packetLength) {
                return;
            } else {

		        int startOffset = in.readerIndex();
		   
		        ByteBuffer buffer = in.nioBuffer(startOffset, packetLength);			//todo
		        Request req = (Request) KryoSerializer.read(buffer.array());
		        RPCContext context = new RPCContext();
		        context.req = req;
		        out.add(context);
		        in.skipBytes(packetLength);
		        
		        packetLength = 0;
		        return;
            }
        }

        if(packetLength <=0){ //Unrecoverable error, have to close connection.
        	ctx.close();
        }

    }


}
