NettyRPC
========

Yet another RPC framework based on Netty(https://github.com/netty/netty) and kryo-serializers(https://github.com/magro/kryo-serializers)


Features
========

  * simple, small code base
  * very fast, high performance
  * multi thread server and multi thread client
  * easy to learn API
  * support asynchronous call, totally non-blocking call.
  * persistent connection, reconnect to server automatically
  * thread safe client, for an remote Object you only need to create a singleton client. 
  * load balance and fail over (unimplemented yet)  
  * php client (unimplemented yet)  
 

Simple tutorial
========
####1.Define an obj interface
```java
public interface IHelloWordObj {
	String hello(String msg);
	String test(Integer i, String s, Long l);
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
}
```

####3.Start the following server
```java
public class HelloServer {
	public static void main(String[] args) throws Exception {
		List<Object> objList = new ArrayList<Object>();
		objList.add(new HelloWorldObj());
		new RPCServer(new ServerConfig(objList)).run();
	}
}
```


####4.Make an Obj proxy and call the remote Obj.
```java
    final String host ="127.0.0.1";//192.168.0.51  127.0.0.1
    final int port = 9090;
    IHelloWordObj client = RPCClient.createObjProxyInstance(host, port, IHelloWordObj.class);
    
    String result = client.hello("hello world!");
    if(!result.equals("hello world!"))
           System.out.print("error="+result);
```

####5. Synchronous call suck? You can do asynchronous call to achieve high performance.
#####5.1. Firstly implements the AsyncRPCCallback interface
```java
public class AsyncHelloWorldCallback implements AsyncRPCCallback {
	@Override
	public void fail(Exception e) {
		System.out.print(e.getMessage());
	}
	@Override
	public void success(Object result) {
		System.out.print(result);
	}
}
```
#####5.2. And make an asynchronous Obj proxy and call the remote Obj.
```java
    final String host ="127.0.0.1";//192.168.0.51  127.0.0.1
    final int port = 9090;
    AsyncObjectProxy<IHelloWordObj> asyncClient = RPCClient.createAsyncObjProxyInstance(host, port, IHelloWordObj.class);
    
    RPCFuture helloFuture = client.call("hello", new Object[]{"hello world!"}, new AsyncHelloWorldCallback("hello world!"));
    RPCFuture testFuture = client.call("test2", new Object[]{1,"hello world!",2L}, new AsyncHelloWorldCallback("hello world!"));
    Object res1= helloFuture.get(3000, TimeUnit.MILLISECONDS);
    Object res2= testFuture.get(3000, TimeUnit.MILLISECONDS);
```
Conclusion
========
Oh, that's all! Easy to understand, right? Please feel free to contact me(2005dawnbreaks@gmail.com) if you have any questions.
