package com.lubin.rpc.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lubin.rpc.client.proxy.BaseObjectProxy;
import com.lubin.rpc.registry.ZooRegistry;
import com.lubin.rpc.thread.BetterExecutorService;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class RPCServer {
    static {
        // initiate SLF4J Logger Factory setting
        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
    }
    
    private final Logger logger = LoggerFactory.getLogger(RPCServer.class);
	private static Config conf = ConfigFactory.load();
	
	private static HashMap<String,Object> objects =new HashMap<String,Object>();

    private int port;
    private int backlog;
    private int ioThreadNum;
    
	public static Config getConfig(){
		return conf;
	}
	
	public static Object getObject(String objName){
		return objects.get(objName);
	}

	private static BetterExecutorService threadPool;

	public static void submit(Runnable task){
		if(threadPool == null){
			synchronized (BaseObjectProxy.class) {
				if(threadPool==null){
					LinkedBlockingDeque<Runnable> linkedBlockingDeque = new LinkedBlockingDeque<Runnable>();
					ThreadPoolExecutor executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 600L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
					threadPool = new BetterExecutorService(linkedBlockingDeque, executor,"Client async thread pool",RPCServer.getConfig().getInt("server.asyncThreadPoolSize"));
				}
			}
		}
		
		threadPool.submit(task);
	}


	
	public RPCServer() throws Exception {

	    this.port = RPCServer.getConfig().getInt("server.port");
	    this.backlog = RPCServer.getConfig().getInt("server.backlog");
	    this.ioThreadNum = getConfig().getInt("server.ioThreadNum");
	    
		List<String> objClassList = RPCServer.getConfig().getStringList("server.objects");
		boolean enableServiceDiscovery = RPCServer.getConfig().getBoolean("server.enableServiceDiscovery");
		logger.info("Object list:");
		for( String objClass : objClassList){
			Object obj = RPCServer.class.forName(objClass).newInstance();
			Class[] interfaces= obj.getClass().getInterfaces();
			
			for(int i =0;i<interfaces.length;i++){
				objects.put(interfaces[i].getName(), obj);
				if(enableServiceDiscovery){
				    ZooRegistry.getInstance().registerService(interfaces[i].getName(), port);
				}
				logger.info("   " + interfaces[i].getName());
			}
		}
	}

	public void run() throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup(this.ioThreadNum);
		try {

			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(new DefaultServerInitializer())
					.option(ChannelOption.SO_BACKLOG, this.backlog)
					.option(ChannelOption.SO_REUSEADDR, true)
					.option(ChannelOption.SO_KEEPALIVE, true);

			Channel ch = b.bind(port).sync().channel();
			
			logger.info("NettyRPC server listening on port "+ port + " and ready for connections...");
	         Runtime.getRuntime().addShutdownHook(new Thread(){
	                @Override
	                public void run(){
	                    for( String objName : objects.keySet()){
	                        try {
                                ZooRegistry.getInstance().unregisterService(objName, port);
                            } catch (Exception e) {
                                logger.info("fail to unregister server node. objName=" + objName, e);
                            }
	                    }
	                }
	            });
			ch.closeFuture().sync();

		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	public static void main(String[] args) throws Exception {
		new RPCServer().run();
	}
}
