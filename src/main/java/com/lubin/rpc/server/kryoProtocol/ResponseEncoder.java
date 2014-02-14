package com.lubin.rpc.server.kryoProtocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;


public class ResponseEncoder extends MessageToByteEncoder<RPCContext> {

	@Override
	protected void encode(ChannelHandlerContext ctx, RPCContext rpcContext, ByteBuf out) throws Exception {
		byte[] bytes = KryoSerializer.write(rpcContext.res);
		out.writeInt(bytes.length);
		out.writeBytes(KryoSerializer.write(rpcContext.res));
	}
}
