NettyRPC
========

Yet another RPC framework based on Netty(https://github.com/netty/netty) and Kryo(https://github.com/EsotericSoftware/kryo)


features
========

  * simple, small code base
  * very fast, high performance
  * multi thread server and multi thread client
  * easy to learn API
  * load balance and fail over (unimplemented)
  * support asynchronous RPC call(unimplemented)


Example:
========
###Define an obj interface
```java
package com.lubin.rpc.example;

public interface IHelloWordObj {
	
	String hello(String msg);

	String test(int i, String s, long l);
}

```
  
###Implemente the previous defined interface
```java
package com.lubin.rpc.example;

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

###Start up the following server
```java
public class HelloServer {
	public static void main(String[] args) throws Exception {
		List<Object> objList = new ArrayList<Object>();
		objList.add(new HelloWorldObj());
		new RPCServer(new ServerConfig(objList)).run();
	}
}

```


###Make an obj proxy and call it.
```java
        final int requestNum = 100000;
        final String host ="127.0.0.1";//192.168.0.51  127.0.0.1
    	final int port = 9090;
	IHelloWordObj client = RPCClient.createObjProxyInstance(host, port, IHelloWordObj.class);
				
	for(int i=0;i<requestNum;i++){
	    String result = client.hello("hello world!");
	    if(!result.equals("hello world!"))
		System.out.print("error="+result);
	}

```
