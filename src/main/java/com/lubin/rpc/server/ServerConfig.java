package com.lubin.rpc.server;

import java.util.ArrayList;
import java.util.List;

public class ServerConfig {

	int backlog = 128;
	int port = 9090;
	int clientMaxBodySize = 1048576 ;
	int taskThreadPoolSize=8;
	
	public ServerConfig(){
	}
	
	public ServerConfig(List<Object> objList){
		this.objList = objList;
	}
	
	List<Object> objList= new ArrayList<Object>();

	public List<Object> getObjList() {
		return objList;
	}
	public void setObjList(List<Object> objList) {
		this.objList = objList;
	}
	public int getBacklog() {
		return backlog;
	}
	public void setBacklog(int backlog) {
		this.backlog = backlog;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public int getClientMaxBodySize() {
		return clientMaxBodySize;
	}
	public void setClientMaxBodySize(int clientMaxBodySize) {
		this.clientMaxBodySize = clientMaxBodySize;
	}
	public int getTaskThreadPoolSize() {
		return taskThreadPoolSize;
	}
	public void setTaskThreadPoolSize(int taskThreadPoolSize) {
		this.taskThreadPoolSize = taskThreadPoolSize;
	}

}
