package com.lubin.rpc.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class SrvrRequestDecoder extends ByteToMessageDecoder  {
	

	/*
	package = head(length of body)|body(bytes) = 4 + bodyLen
	 */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) { 
    	
    	int packetLength = 0;
    	int bodyLength = 0;
    	int readerIndex=in.readerIndex();
    	
    	if( in.readableBytes() < 4)	{
    		return; 
    	}else if( in.readableBytes() >= 4){
    		bodyLength =  in.getInt(readerIndex);
    		packetLength = 4 +bodyLength ;
    	}
 
        //  we have got the length of the package
        if (packetLength > 4) {
            if (in.readableBytes() < packetLength) {
                return;
            } else {

		        byte[] body = new byte[bodyLength];								
		        in.getBytes(readerIndex+4, body);			 //todo  : avoid memory copy
		        Request req = (Request) KryoSerializer.read(body);  //buffer.array()  not copy memory
		        RPCContext context = new RPCContext();
		        context.setRequest(req);
		        out.add(context);
		        in.skipBytes(packetLength);
		        return;
            }
        }

        if(bodyLength <=0){ //Unrecoverable error, have to close connection.
        	ctx.close();
        }

    }


}
