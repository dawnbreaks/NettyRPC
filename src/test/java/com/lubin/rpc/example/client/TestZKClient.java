package com.lubin.rpc.example.client;

import com.lubin.rpc.client.RPCClient;
import com.lubin.rpc.example.obj.IHelloWordObj;
import com.lubin.rpc.example.obj.IHelloWordObj.HellMsg;
import com.lubin.rpc.example.obj.IHelloWordObj.Msg;

public class TestZKClient {
	

    public static void main(String[] args) throws Exception {

		IHelloWordObj client = RPCClient.proxyBuilder(IHelloWordObj.class).enableRegistry().build();

		HellMsg msg =new HellMsg();
		msg.setI(1);
		msg.setL(2L);
		msg.setS("hello1");
		msg.setMsg(new Msg());
		msg.getMsg().setI(2);
		msg.getMsg().setL(3L);
		msg.getMsg().setS("hello2");
		
		System.out.println("test server list:" + client.testMst(msg));
		Msg res = client.testMst(msg);
		if(!(res.getI() == msg.getI() + msg.getMsg().getI()) 
			|| !(res.getL() == msg.getL() + msg.getMsg().getL())
			||!(res.getS() .equals(msg.getS() + msg.getMsg().getS())))
		{
			System.out.println("tesg Msg got error!");
		}
		
//		RPCClient.getInstance().shutdown();
    }
}
