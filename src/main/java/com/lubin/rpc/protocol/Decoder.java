package com.lubin.rpc.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Decoder extends ByteToMessageDecoder  {
	
	private static final ObjectMapper objectMapper = new ObjectMapper();

	static {
		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	}
	
	private boolean decodeRequst;
	public Decoder(boolean decodeRequst){
		this.decodeRequst = decodeRequst;
	}
	
	/*
	package =header + body
	header = bodylen + serializer = int(4) +char(2)
	body= binary bytes
	 */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) { 
    	
    	int packetLength = 0;
    	int bodyLength = 0;
    	int readerIndex=in.readerIndex();
    	
    	int headerLenth = 6;
    	char serializer = Constants.RPCSerializer.kryo;
    	if( in.readableBytes() < headerLenth)	{
    		return; 
    	}else if( in.readableBytes() >= headerLenth){
    		bodyLength =  in.getInt(readerIndex);
    		serializer = in.getChar(readerIndex+4);
    		packetLength = headerLenth + bodyLength ;
    	}
 
        //  we have got the length of the package
        if (packetLength > headerLenth) {
            if (in.readableBytes() < packetLength) {
                return;
            } else {

            	if(serializer == Constants.RPCSerializer.kryo){
    		        byte[] body = new byte[bodyLength];								
    		        in.getBytes(readerIndex + headerLenth, body);			 //todo  : avoid memory copy
    		        Object bodyObj =  KryoSerializer.read(body);  
    		        
    		        RPCContext context = new RPCContext();
    		        if(bodyObj instanceof Request){
    		        	context.setRequest((Request) bodyObj);
    		        }else if(bodyObj instanceof Response){
    		        	context.setResponse( (Response) bodyObj);
    		        }else{//decoder got error
    		        	ctx.close();
    		        }

    		        out.add(context);
    		        in.skipBytes(packetLength);
    		        return;
            	}else if(serializer == Constants.RPCSerializer.json){
    		        byte[] body = new byte[bodyLength];								
    		        in.getBytes(readerIndex + headerLenth, body);			 //todo  : avoid memory copy
    		        RPCContext context = new RPCContext();
    		        try{
    		        	if(decodeRequst){
        		        	Request req = objectMapper.readValue(body, Request.class); 
        		        	context.setRequest(req);
        		        }else{
        		        	Response res = objectMapper.readValue(body, Response.class); 
        		        	context.setResponse(res);
        		        }
    		        } catch (Exception e) {
						ctx.close();
					}
    		        
    		        out.add(context);
    		        in.skipBytes(packetLength);
    		        return;
            	}else {
            		//todo....
            	}
            }
        }

        if(bodyLength <=0){ //Unrecoverable error, have to close connection.
        	ctx.close();
        }

    }


}
