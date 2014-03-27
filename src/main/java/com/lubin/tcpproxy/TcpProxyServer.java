package com.lubin.tcpproxy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.lubin.rpc.client.proxy.BaseObjectProxy;
import com.lubin.rpc.thread.BetterExecutorService;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;

public class TcpProxyServer {

	private static Config conf = ConfigFactory.load();
	
	private static HashMap<Integer, ProxyHost> proxyHosts = new HashMap<Integer, ProxyHost>();
	
	public static HashMap<Integer, ProxyHost> getProxyHosts() {
		return proxyHosts;
	}


	public static void setProxyHosts(HashMap<Integer, ProxyHost> proxyHosts) {
		TcpProxyServer.proxyHosts = proxyHosts;
	}


	public static Config getConfig(){
		return conf;
	}
	
	private static BetterExecutorService threadPool;

	public static void submit(Runnable task){
		if(threadPool == null){
			synchronized (BaseObjectProxy.class) {
				if(threadPool==null){
					LinkedBlockingDeque<Runnable> linkedBlockingDeque = new LinkedBlockingDeque<Runnable>();
					ThreadPoolExecutor executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 600L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
					threadPool = new BetterExecutorService(linkedBlockingDeque, executor,"Client async thread pool",TcpProxyServer.getConfig().getInt("server.asyncThreadPoolSize"));
				}
			}
		}
		
		threadPool.submit(task);
	}

	
	public TcpProxyServer() throws InstantiationException, IllegalAccessException, ClassNotFoundException {

	}

	public void run() throws Exception {

		EventLoopGroup bossGroup = new NioEventLoopGroup();
		
		EventLoopGroup workerGroup = new NioEventLoopGroup(TcpProxyServer.getConfig().getInt("tcpProxyServer.ioThreadNum"));
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(new FrontendInitializer())
					.option(ChannelOption.SO_BACKLOG, TcpProxyServer.getConfig().getInt("tcpProxyServer.SO_BACKLOG"))
					.option(ChannelOption.SO_REUSEADDR, true)
					.option(ChannelOption.SO_TIMEOUT, TcpProxyServer.getConfig().getInt("tcpProxyServer.SO_TIMEOUT"))
					.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TcpProxyServer.getConfig().getInt("tcpProxyServer.CONNECT_TIMEOUT_MILLIS"))
//					.option(ChannelOption.AUTO_READ, false)
					.option(ChannelOption.SO_KEEPALIVE, true);

			ArrayList<Channel> allchannels =new ArrayList<Channel>();
			ArrayList<ProxyHost> hostList = getProxyHostList();
			for(ProxyHost host : hostList) {
				proxyHosts.put(host.localPort, host);
				Channel ch = b.bind(host.localPort).sync().channel();
				allchannels.add(ch);
			}
	
			for(Channel ch : allchannels){
				ch.closeFuture().sync();
			}
			
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	
	public static ArrayList<ProxyHost> getProxyHostList(){
		ArrayList<ProxyHost> proxyHosts = new ArrayList<ProxyHost>();
		
		List<? extends ConfigObject> hosts = TcpProxyServer.getConfig().getObjectList("tcpProxyServer.hosts");
		for(ConfigObject host : hosts) {
			ConfigValue localPort = host.get("localPort");
			ConfigValue remoteHost = host.get("remoteHost");
			ConfigValue remotePort = host.get("remotePort");
			proxyHosts.add(new ProxyHost(Integer.parseInt(localPort.render()), (String) remoteHost.unwrapped(), Integer.parseInt(remotePort.render())));
		}
		return proxyHosts;
	}
	
	public static class ProxyHost{
		int localPort;
		String remoteHost;
		int remotePort;
		
		public int getRemotePort() {
			return remotePort;
		}
		public void setRemotePort(int remotePort) {
			this.remotePort = remotePort;
		}
		public ProxyHost(int localPort, String remoteHost, int remotePort) {
			super();
			this.localPort = localPort;
			this.remoteHost = remoteHost;
			this.remotePort = remotePort;
		}
		public int getLocalPort() {
			return localPort;
		}
		public void setLocalPort(int localPort) {
			this.localPort = localPort;
		}
		public String getRemoteHost() {
			return remoteHost;
		}
		public void setRemoteHost(String remoteHost) {
			this.remoteHost = remoteHost;
		}
	
	}

	public static void main(String[] args) throws Exception {
		new TcpProxyServer().run();
	}
}
