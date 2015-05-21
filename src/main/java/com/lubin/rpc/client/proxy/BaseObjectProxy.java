package com.lubin.rpc.client.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lubin.rpc.client.DefaultClientHandler;
import com.lubin.rpc.client.RPCClient;
import com.lubin.rpc.client.RPCClientInitializer;
import com.lubin.rpc.protocol.Constants;
import com.lubin.rpc.protocol.RPCContext;
import com.lubin.rpc.protocol.Request;
import com.lubin.rpc.registry.InstanceDetails;
import com.lubin.rpc.registry.ZooRegistry;

public class BaseObjectProxy<T> {

	private final Logger logger = LoggerFactory.getLogger(BaseObjectProxy.class);
	 
	protected Class<T> clazz;
	private String objName;
	
	private ReentrantLock lock = new ReentrantLock();
	private Condition connected  = lock.newCondition(); 
	
	
	protected CopyOnWriteArrayList<DefaultClientHandler> connectedHandlers = new CopyOnWriteArrayList<DefaultClientHandler>();
	private Map<InetSocketAddress, DefaultClientHandler> connectedServerNodes = new ConcurrentHashMap<>();
	private AtomicReference<HashSet<InetSocketAddress>> allServerNodeSetRef = new AtomicReference<>(new HashSet<InetSocketAddress>());
	
	private AtomicInteger roundRobin = new AtomicInteger(0);

	protected long syncCallTimeOutMillis;
    protected long connectTimeoutMillis;
    protected long reconnIntervalMillis;
    
    private String basePath;
    private ServerNodesWatcher zkWather;


	public void setClazz(Class<T> clazz) {
		this.clazz = clazz;
	}

	public Class<T> getClazz() {
		return clazz;
	}
	
	public BaseObjectProxy(Class<T> clazz){
	    this(null, true, clazz);
	}
	
	public BaseObjectProxy(List<InetSocketAddress> servers, Class<T> clazz){
	    this(servers, false, clazz);
    }
	
	public BaseObjectProxy(List<InetSocketAddress> servers, boolean useRegistry, Class<T> clazz) {
	    this.clazz = clazz;
	    this.objName = clazz.getName();
	    this.connectTimeoutMillis = RPCClient.getInstance().getConfig().getLong("client.connectTimeoutMillis");
	    this.reconnIntervalMillis = RPCClient.getInstance().getConfig().getLong("client.reconnIntervalMillis");
	    this.syncCallTimeOutMillis = RPCClient.getInstance().getConfig().getLong("client.syncCallTimeOutMillis");
	    this.basePath = RPCClient.getInstance().getConfig().getString("zookeeper.basePath");
	   

	    if(useRegistry){
	        servers = queryServerNodesFromZK(clazz);
	        this.zkWather = new ServerNodesWatcher();
	        //todo zookeeper data change
	        ZooRegistry.getInstance().addWatcher(getServiceZKPath(), zkWather);
	    }

	    //Firstly populate all data to allServerNodes, because hash set is not thread safe,you need to avoid concurrent access to it.  
	    for(final InetSocketAddress server : servers){
	        allServerNodeSetRef.get().add(server);
	    }
	    
	    for(final InetSocketAddress server : servers){
	        connect(server);
        }
	}
	
	
	private class ServerNodesWatcher implements Watcher{
        @Override
        public void process(WatchedEvent event) {
            switch (event.getType()) {
            case None:
            case NodeCreated:
            case NodeDeleted:
            case NodeDataChanged:
            case NodeChildrenChanged:
                handleServerNodesChange();
            default:
                break;
            }
        }
    }
	
	private String getServiceZKPath(){
	    return basePath + "/"+ clazz.getName();
	}
	
	private void handleServerNodesChange(){
	    try{
	        logger.info("Handle zookeeper push message, Server nodes has changed. objName=" + objName);
	        CopyOnWriteArrayList<DefaultClientHandler> connectedServerHandlers = (CopyOnWriteArrayList<DefaultClientHandler>) connectedHandlers.clone();
	        ArrayList<InetSocketAddress> newServerNodeList = queryServerNodesFromZK(clazz);
	        
	        //update local serverNodes cache
	        HashSet<InetSocketAddress> newAllServerNodeSet = new HashSet<InetSocketAddress>();
	        for(final InetSocketAddress serverNode : newServerNodeList){
	            newAllServerNodeSet.add(serverNode);
	        }
	        HashSet<InetSocketAddress> oldAllServerNodeSet = allServerNodeSetRef.get();
	        allServerNodeSetRef.set(newAllServerNodeSet);
	        
	        //add new serverNode
	        for(final InetSocketAddress serverNode : newServerNodeList){
	            if(!oldAllServerNodeSet.contains(serverNode)){
	                logger.info("HandleServerNodesChange|objName=" + objName+"|add new server node." + serverNode);
	                connect(serverNode);
	            }
	        }
	        
	        //close and remove invalid serverNodes
	        for(final DefaultClientHandler connectedServerHandler : connectedServerHandlers){
	            if(!newAllServerNodeSet.contains(connectedServerHandler.getRemotePeer())){
	                logger.info("HandleServerNodesChange|objName=" + objName+"|remove invalid server node." + connectedServerHandler.getRemotePeer());
	                DefaultClientHandler handler = connectedServerNodes.get(connectedServerHandler.getRemotePeer());
	                handler.close();
	                connectedServerNodes.remove(connectedServerHandler);
	            }
	        }
	    }finally{
	        ZooRegistry.getInstance().addWatcher(getServiceZKPath(), zkWather);
	    }
    }
	
	
	public ArrayList<InetSocketAddress> queryServerNodesFromZK(Class<T> clazz) {
        Collection<ServiceInstance<InstanceDetails>> instances;
        ArrayList<InetSocketAddress> serverList = new ArrayList<InetSocketAddress>();
        try {
            instances = ZooRegistry.getInstance().queryForInstances(clazz.getName());
            for(ServiceInstance<InstanceDetails> instance : instances){
                serverList.add(new InetSocketAddress(instance.getAddress(), instance.getPort()));
            }
            
            if(serverList.isEmpty()){
                throw new RuntimeException("Server list is empty, can not find any corresponding client.objects in the conf file.");
            }
        } catch (Exception e) {
            new RuntimeException("RPCClient|loadServerListFromZookeeper", e);
        }
        return serverList;
    }
	
	private void addHandler(DefaultClientHandler handler){
	    connectedHandlers.add(handler);
	    connectedServerNodes.put((InetSocketAddress)handler.getChannel().remoteAddress(), handler);
	    signalAvailableHandler();
	}
	
	private void connect(final InetSocketAddress remotePeer) {
	    doConnect(null, remotePeer, 0);
	}
	
	public void  reconnect(final Channel channel, final SocketAddress remotePeer){
	    doConnect(channel, remotePeer , reconnIntervalMillis);
	}

	private void doConnect(final Channel channel, final SocketAddress remotePeer, long delay) {
	    if(channel != null){
	        connectedHandlers.remove(channel.pipeline().get(DefaultClientHandler.class));
	    }
		
	    //invalid serverNode, just simply ignore it.
	    if(!allServerNodeSetRef.get().contains(remotePeer)){
	        logger.info("Reject reconnect-request to invalid server node. objName=" + objName + "|remote peer=" + remotePeer.toString());
	        return;
	    }
	    
		RPCClient.getInstance().getEventLoopGroup().schedule(new Runnable(){
			@Override
			public void run() {
				try {
				 	 Bootstrap b = new Bootstrap();
				 	 b.group(RPCClient.getInstance().getEventLoopGroup())
				 	     .channel(NioSocketChannel.class)
				 	     .handler(new RPCClientInitializer(BaseObjectProxy.this));
				 	 
					 ChannelFuture channelFuture = b.connect(remotePeer);
					 
					 channelFuture.addListener(new ChannelFutureListener(){
						@Override
						public void operationComplete(final ChannelFuture channelFuture) throws Exception {
							if(!channelFuture.isSuccess()){
							    logger.info("Can't connect to remote server. objName=" + objName + "|remote peer=" + remotePeer.toString());
								reconnect(channelFuture.channel(), remotePeer );
							}else{
							    logger.info("Successfully connect to remote server. objName=" + objName + "|remote peer=" + remotePeer);
								DefaultClientHandler handler = channelFuture.channel().pipeline().get(DefaultClientHandler.class);
								addHandler(handler);
							}
						}
					 });
	
				} catch (Exception e) {
					logger.warn("doConnect got exception|msg="+e.getMessage(),e);
					reconnect(channel, remotePeer);
				}
			}
		}, delay, TimeUnit.MILLISECONDS);
	}
	
	
	private boolean waitingForHandler() throws InterruptedException{
	    lock.lock();
	    try{
	        return connected.await(this.connectTimeoutMillis, TimeUnit.MILLISECONDS);
	    }finally{
	        lock.unlock();
	    }
	}
	
	private void signalAvailableHandler() {
        lock.lock();
        try{
            connected.signalAll();
        }finally{
            lock.unlock();
        }
    }
	
	DefaultClientHandler chooseHandler(){
		
		CopyOnWriteArrayList<DefaultClientHandler> handlers = (CopyOnWriteArrayList<DefaultClientHandler>) this.connectedHandlers.clone();
		int size = handlers.size();
		if(size <= 0){
		    try {
		        boolean available = waitingForHandler();
		        if(available){
		            handlers = (CopyOnWriteArrayList<DefaultClientHandler>) this.connectedHandlers.clone();
		            size = handlers.size();
		        }
		       
                if(size <= 0){
                    throw new RuntimeException("Cann't connect any servers!");
                }
                
            } catch (InterruptedException e) {
                logger.error("chooseHandler|msg=" + e.getMessage(), e);
                throw new RuntimeException("Cann't connect any servers!", e);
            }
		}
		int index = (roundRobin.getAndAdd(1) + size)%size;
		return handlers.get(index);
	}

	RPCContext createRequest(String funcName, Object[] args, long seqNum, byte type) {
		try{
			Request req = new Request();
			req.setSeqNum(seqNum);
			req.setObjName(clazz.getName());
			req.setFuncName(funcName);
			req.setSerializer((byte) RPCClient.getInstance().getConfig().getInt("client.serializer"));
			req.setArgs(args);
			   
			Class[] parameterTypes = new Class[args.length];
			for(int i=0; i<args.length;i++){
				parameterTypes[i] = args[i].getClass();
			}
		   
		    Method method = clazz.getMethod(funcName, parameterTypes);
		    if( method.getReturnType().equals(Void.TYPE) && Constants.RPCType.oneway == type){
			   req.setType(Constants.RPCType.oneway);
		    }else if( method.getReturnType().equals(Void.TYPE) && Constants.RPCType.normal == type){
		    	req.setType(Constants.RPCType.oneway);
		    }else if( method.getReturnType().equals(Void.TYPE) && Constants.RPCType.async == type){
		    	new RuntimeException("this method will not return, please use notify() to call this method.");
		    }else{
		    	req.setType(type);
		    }
		    
		    RPCContext rpcCtx = new RPCContext();
			rpcCtx.setRequest(req);
		    return rpcCtx ;
		}catch (Exception e) {
			throw new RuntimeException("BaseObjectProxy.createRequest got exception|",e);
		}
	}
	
//	public static class InetSocketAddressWraper implements Comparable<InetSocketAddressWraper>{
//	    InetSocketAddress inetAddr;
//	    public InetSocketAddressWraper(InetSocketAddress inetAddr){
//	        this.inetAddr = inetAddr;
//	    }
//	    
//        @Override
//        public int compareTo(InetSocketAddressWraper o) {
//            
//            return 0;
//        }
//	}
}
