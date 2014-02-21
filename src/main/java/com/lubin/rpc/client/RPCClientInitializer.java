
package com.lubin.rpc.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

import com.lubin.rpc.client.proxy.BaseObjectProxy;
import com.lubin.rpc.protocol.CliRequestEncoder;
import com.lubin.rpc.protocol.CliResponseDecoder;

public class RPCClientInitializer extends ChannelInitializer<SocketChannel> {

	private BaseObjectProxy objProxy;
	
	public  RPCClientInitializer(BaseObjectProxy objProxy){
		super();
		this.objProxy = objProxy;
	}
	
    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        p.addLast("decoder", new CliResponseDecoder());

        p.addLast("encoder", new CliRequestEncoder());
 
        p.addLast("handler", new DefaultClientHandler(objProxy));
    }
}
