package com.lubin.rpc.example.obj;

public interface IHelloWordObj {
	
	String hello(String msg);

	String test(Integer i, String s, Long l);
	
	Msg testMst(HellMsg msg);
	
	public class HellMsg{
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
        @Override
        public String toString() {
            return "HellMsg [i=" + i + ", l=" + l + ", s=" + s + ", msg=" + msg
                    + "]";
        }
	}
	
	public class Msg{
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
        @Override
        public String toString() {
            return "Msg [i=" + i + ", l=" + l + ", s=" + s + "]";
        }
	}
}
