[![Build Status](https://travis-ci.org/dawnbreaks/NettyRPC.png?branch=master)](https://travis-ci.org/dawnbreaks/NettyRPC)
NettyRPC
========

Yet another RPC framework based on [Netty](https://github.com/netty/netty).


Features
========

  * Simple, small code base, easy to learn API
  * Very fast, high performance
  * Totally non-blocking asynchronous call, synchronous call, oneway call.
  * Long lived persistent connection, reconnect to server automatically
  * High availability, load balance and failover 
  * Multi thread server and multi thread client
  * Thread safe client, for an remote service you only need to create a singleton client. 
  * Service Discovery support, use zookeeper as a service registry. 
  * PHP client (Unimplemented yet)  
  
Simple tutorial
========
####1.Define an obj interface
```java
public interface IHelloWordObj {
	String hello(String msg);
	String test(Integer i, String s, Long l);
	void notifySomeThing(Integer i, String s, Long l);
}
```
  
####2.Implements the previous defined interface
```java
public class HelloWorldObj implements IHelloWordObj {
	@Override
	public String hello(String msg) {
		return msg;
	}
	@Override
	public String test(Integer i, String s, Long l) {
		return i+s+l;
	}
	@Override
	void notifySomeThing(Integer i, String s, Long l) {
		System.out.println("notifySomeThing|i="+i+"|s="+s+"l="+l);
	}
}
```

####3. Update configuration file and start the server "com.lubin.rpc.server.RPCServer"
```javascript
server {
	port = 9090
	backlog = 1000
	async = false	//handling request in business logic thread pool
	asyncThreadPoolSize = 4
    ioThreadNum = 4
    enableServiceDiscovery = true   
	objects = [
		com.lubin.rpc.example.obj.HelloWorldObj
	]
}
client {
	syncCallTimeOutMillis = 300
	connectTimeoutMillis = 300
	reconnIntervalMillis = 1000	//time interval for reconnecting to server
	asyncThreadPoolSize = 1   //thread pool for excuting Async callback
    ioThreadNum = 1   
    objects = [ 
		{ 
			name = com.lubin.rpc.example.obj.IHelloWordObj
			servers ="127.0.0.1:9090 127.0.0.1:9091"
		}
	]
}
```


####4.Synchronous call. Create an Obj proxy and call the remote Obj.
```java
    IHelloWordObj client = RPCClient.proxyBuilder(IHelloWordObj.class)
    					            .withServerNode("127.0.0.1", 9090)
    					            .build();
    String result = client.hello("hello world!");
```

####5. Asynchronous call
#####5.1.
```java
    IAsyncObjectProxy asyncClient = RPCClient.proxyBuilder(IHelloWordObj.class)
    					                     .withServerNode
    						                 .buildAsyncObjPrx();
    
    RPCFuture helloFuture = asyncClient.call("hello", "hello world!");
    RPCFuture testFuture = asyncClient.call("test", 1,"hello world!",2L);
    Object res1= helloFuture.get(3000, TimeUnit.MILLISECONDS);
    Object res2= testFuture.get(3000, TimeUnit.MILLISECONDS);

```
#####5.2. Optionally you can provide a callback which will be automatically called by NettyRPC after received response from server.
```java
public class AsyncHelloWorldCallback implements AsyncRPCCallback {
	@Override
	public void fail(Exception e) {
		System.out.println(e.getMessage());
	}
	@Override
	public void success(Object result) {
		System.out.println(result);
	}
}

    IAsyncObjectProxy asyncClient = RPCClient.proxyBuilder(IHelloWordObj.class)
    						                 .withServerNode("127.0.0.1", 9090)
    					   	                 .buildAsyncObjPrx();
    RPCFuture helloFuture = asyncClient.call("hello", "hello world!")
    						           .addCallback(new AsyncHelloWorldCallback());
    RPCFuture testFuture = asyncClient.call("test", 1,"hello world!",2L)
    						           .addCallback(new AsyncHelloWorldCallback());
```

####6.Oneway call
```java
    IAsyncObjectProxy asyncClient = RPCClient.proxyBuilder(IHelloWordObj.class)
    						                 .withServerNode("127.0.0.1", 9090)
    						                 .buildAsyncObjPrx();
    asyncClient.notify("notifySomeThing", 1, "hello world!", 2L);
```

####7 High availability, you can deploy more than one servers to achieve HA, NettyRPC handle load balance and failover automatically.  
```java
    ArrayList<InetSocketAddress> serverNodeList = new ArrayList<InetSocketAddress>();
    serverNodeList.add(new InetSocketAddress("127.0.0.1",9090));
    serverNodeList.add(new InetSocketAddress("127.0.0.1",9091));
         
    IHelloWordObj client = RPCClient.proxyBuilder(IHelloWordObj.class)
    					            .withServerNodes(serverNodeList)
    					            .build();
    System.out.println("test server list:"+client.hello("test server list11"));
```

####8 Service Discovery support
Instead of hard coding the server address in config file( or java source code), NettyRPC support service discovery. 
All Services will automatically register themself to registry(Zookeeper) after services started,  and NettyRPC client will automatically query addresses from the registry.
If you want to scale out and deploy more server, you just simply start the new services, NettyRPC client will automatically got notified and dispatch requests to that new services.

```java
    IHelloWordObj client = RPCClient.proxyBuilder(IHelloWordObj.class)
    					            .enableRegistry()
    					            .build();
    System.out.println("test server list:"+client.hello("test server list11"));
```
========
Please feel free to contact me(2005dawnbreaks@gmail.com) if you have any questions.
