package com.lubin.rpc.server.kryoProtocol;


public class Response {
	private long seqNum;
	private int version;
	private char type;  //0 normal, 1oneway, 2 async
	private String objName;
	private String funcName;
	
	//output
	private Object result;
	private char status;//0 ok , 1 error, 2 unknown error
	private String msg;
	
	
	
	public long getSeqNum() {
		return seqNum;
	}
	public void setSeqNum(long seqNum) {
		this.seqNum = seqNum;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public char getType() {
		return type;
	}
	public void setType(char type) {
		this.type = type;
	}
	public String getObjName() {
		return objName;
	}
	public void setObjName(String objName) {
		this.objName = objName;
	}
	public String getFuncName() {
		return funcName;
	}
	public void setFuncName(String funcName) {
		this.funcName = funcName;
	}
	public Object getResult() {
		return result;
	}
	public void setResult(Object results) {
		this.result = results;
	}
	public char getStatus() {
		return status;
	}
	public void setStatus(char status) {
		this.status = status;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	
	
	
	
}
