
package com.lubin.rpc.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

import com.lubin.rpc.protocol.CliRequestEncoder;
import com.lubin.rpc.protocol.CliResponseDecoder;

public class RPCClientInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        p.addLast("decoder", new CliResponseDecoder());

        p.addLast("encoder", new CliRequestEncoder());
 
        p.addLast("handler", new DefaultClientHandler());
    }
}
