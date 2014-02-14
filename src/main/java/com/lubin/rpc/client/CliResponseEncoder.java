package com.lubin.rpc.client;

import com.lubin.rpc.server.kryoProtocol.KryoSerializer;
import com.lubin.rpc.server.kryoProtocol.RPCContext;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;


public class CliResponseEncoder extends MessageToByteEncoder<RPCContext> {

	@Override
	protected void encode(ChannelHandlerContext ctx, RPCContext rpcContext, ByteBuf out) throws Exception {
		byte[] bytes = KryoSerializer.write(rpcContext.getReq());
		out.writeInt(bytes.length);
		out.writeBytes(bytes);
	}
}
