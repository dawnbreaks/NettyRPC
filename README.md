NettyRPC
========

Yet another RPC framework based on Netty(https://github.com/netty/netty).


Features
========

  * Simple, small code base, easy to learn API
  * Very fast, high performance
  * Totally non-blocking asynchronous call, synchronous call, oneway call.
  * Long lived persistent connection, reconnect to server automatically
  * High availability, load balance and failover 
  * Multi thread server and multi thread client
  * Thread safe client, for an remote Object you only need to create a singleton client. 
  * PHP client (unimplemented yet)  
  
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
	asyncThreadPoolSize = 3
	objects = [
		"com.lubin.rpc.example.obj.HelloWorldObj"
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
#####5.2. Optionally you can provide a call back which will be called by NettyRPC after received response form server.
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

####6 High availability, You hate Single point of failure? You could deploy more than one servers to achieve HA, NettyRPC  handle load balance and failover automatically.  
```java
    InetSocketAddress server1 = new InetSocketAddress("127.0.0.1",9090);
    InetSocketAddress server2 = new InetSocketAddress("127.0.0.1",9091);
    ArrayList<InetSocketAddress> serverList = new ArrayList<InetSocketAddress>();
    serverList.add(server1);
    serverList.add(server2);
         
    IHelloWordObj client = RPCClient.createObjectProxy(serverList, IHelloWordObj.class);
    System.out.println("test server list:"+client.hello("test server list11"));
```


For more information please refer to example in the test folder.


Build
========

To build the JAR file of NettyRPC, you need to install Maven (http://maven.apache.org), then type the following command:

    $ mvn package

To generate project files (.project, .classpath) for Eclipse, do

    $ mvn eclipse:eclipse

then import the folder from your Eclipse.


========
Oh, that's all! Easy to understand, right? Please feel free to contact me(2005dawnbreaks@gmail.com) if you have any questions.
