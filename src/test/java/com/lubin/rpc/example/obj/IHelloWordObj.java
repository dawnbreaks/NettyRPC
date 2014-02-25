package com.lubin.rpc.example.obj;

public interface IHelloWordObj {
	
	String hello(String msg);

	String test(Integer i, String s, Long l);
	
	Msg testMst(HellMsg msg);
	
	class HellMsg{
		private int i;
		private long l;
		private String s;
		private Msg msg;
		public int getI() {
			return i;
		}
		public void setI(int i) {
			this.i = i;
		}
		public long getL() {
			return l;
		}
		public void setL(long l) {
			this.l = l;
		}
		public String getS() {
			return s;
		}
		public void setS(String s) {
			this.s = s;
		}
		public Msg getMsg() {
			return msg;
		}
		public void setMsg(Msg msg) {
			this.msg = msg;
		}
		
	}
	
	class Msg{
		private int i;
		private long l;
		private String s;
		public int getI() {
			return i;
		}
		public void setI(int i) {
			this.i = i;
		}
		public long getL() {
			return l;
		}
		public void setL(long l) {
			this.l = l;
		}
		public String getS() {
			return s;
		}
		public void setS(String s) {
			this.s = s;
		}
	}
}
