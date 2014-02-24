
package com.lubin.rpc.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

import com.lubin.rpc.client.proxy.BaseObjectProxy;
import com.lubin.rpc.protocol.Decoder;
import com.lubin.rpc.protocol.Encoder;

public class RPCClientInitializer extends ChannelInitializer<SocketChannel> {

	private BaseObjectProxy objProxy;
	
	public  RPCClientInitializer(BaseObjectProxy objProxy){
		super();
		this.objProxy = objProxy;
	}
	
    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        p.addLast("decoder", new Decoder(false));

        p.addLast("encoder", new Encoder());
 
        p.addLast("handler", new DefaultClientHandler(objProxy));
    }
}
