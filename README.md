NettyRPC
========

Yet another RPC framework based on Netty(https://github.com/netty/netty) and Kryo(https://github.com/EsotericSoftware/kryo)


Features
========

  * simple, small code base
  * very fast, high performance
  * multi thread server and multi thread client
  * easy to learn API
  * support asynchronous call, totally non-blocking call.
  * load balance and fail over (unimplemented yet)  
  * provide php clinet (unimplemented yet)  
 

Simple tutorial
========
####1.Define an obj interface
```java
public interface IHelloWordObj {
	String hello(String msg);
	String test(int i, String s, long l);
}
```
  
####2.Implemente the previous defined interface
```java
public class HelloWorldObj implements IHelloWordObj {
	@Override
	public String hello(String msg) {
		return msg;
	}
	@Override
	public String test(int i, String s, long l) {
		return i+s+l;
	}
}
```

####3.Start up the following server
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

####5. Asynchronous call: Synchronous call suck? You can do asynchronous call to achieve high performance.
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
    
    RPCFuture result = asyncClient.Call("hello", new Object[]{"hello world!"}, new AsyncHelloWorldCallback());
```
Concluson
========
Oh, that's all! Easy to understand, right? Please feel free to contact me(2005dawnbreaks@gmail.com) if you have any questions.
