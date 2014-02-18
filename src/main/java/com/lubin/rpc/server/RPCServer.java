package com.lubin.rpc.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.HashMap;

public class RPCServer {

	private static ServerConfig conf;
	
	private static HashMap<String,Object> objects =new HashMap<String,Object>();
	
	public static ServerConfig getServerConf(){
		return conf;
	}
	public static Object getObject(String objName){
		return objects.get(objName);
	}

	public RPCServer(ServerConfig conf) {
		this.conf = conf;
		
		for(Object obj : conf.getObjList()){
			Class[] interfaces= obj.getClass().getInterfaces();
			for(int i =0;i<interfaces.length;i++){
				objects.put(interfaces[i].getSimpleName(), obj);
			}
			
		}
	}

	public void run() throws Exception {

		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(new DefaultServerInitializer(conf))
					.option(ChannelOption.SO_BACKLOG, conf.getBacklog())
					.option(ChannelOption.SO_REUSEADDR, true)
					.option(ChannelOption.SO_KEEPALIVE, true);

			Channel ch = b.bind(conf.getPort()).sync().channel();
			ch.closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}


}
