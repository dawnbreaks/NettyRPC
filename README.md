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
  * Thread safe client, for an remote Object you only need to create a singleton client. 
  * Service Discovery, use zookeeper as a service registry. (Unimplemented yet)
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
	public String notifySomeThing(Integer i, String s, Long l) {
	}
}
```

####3. Edit  application.conf to add the previous obj and Start the server com.lubin.rpc.server.RPCServer
```javascript
server {
	port = 9090
	backlog = 1000
	async = false	//handling request in business logic thread pool
	asyncThreadPoolSize = 4
    ioThreadNum = 1   
	objects = [
		com.lubin.rpc.example.obj.HelloWorldObj
	]
}
client {
	reconnInterval = 1000	//time interval(million second) for reconnecting to server
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


####4.Make an Obj proxy and call the remote Obj.
```java
    IHelloWordObj client = RPCClient.createObjectProxy("127.0.0.1", 9090, IHelloWordObj.class);
    
    String result = client.hello("hello world!");
    if(!result.equals("hello world!"))
           System.out.println("error="+result);
```

####5. Asynchronous call
#####5.1. Create an asynchronous Obj proxy and call the remote Obj.
```java
    IAsyncObjectProxy asyncClient = RPCClient.createAsyncObjPrx("127.0.0.1", 9090, IHelloWordObj.class);
    
    RPCFuture helloFuture = client.call("hello", new Object[]{"hello world!"});
    RPCFuture testFuture = client.call("test", new Object[]{1,"hello world!",2L});
    Object res1= helloFuture.get(3000, TimeUnit.MILLISECONDS);
    Object res2= testFuture.get(3000, TimeUnit.MILLISECONDS);
```
#####5.2. Optionally you can provide a callback which will be called by NettyRPC after received response from server.
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

    IAsyncObjectProxy asyncClient = RPCClient.createAsyncObjPrx("127.0.0.1", 9090, IHelloWordObj.class);
    RPCFuture helloFuture = client.call("hello", new Object[]{"hello world!"},new AsyncHelloWorldCallback());
    RPCFuture testFuture = client.call("test", new Object[]{1,"hello world!",2L}, new AsyncHelloWorldCallback());
```

####6 High availability, you can deploy more than one servers to achieve HA, NettyRPC  handle load balance and failover automatically.  
```java
    ArrayList<InetSocketAddress> serverList = new ArrayList<InetSocketAddress>();
    serverList.add(new InetSocketAddress("127.0.0.1",9090));
    serverList.add(new InetSocketAddress("127.0.0.1",9091));
         
    IHelloWordObj client = RPCClient.createObjectProxy(serverList, IHelloWordObj.class);
    System.out.println("test server list:"+client.hello("test server list11"));
```


========
Please feel free to contact me(2005dawnbreaks@gmail.com) if you have any questions.
