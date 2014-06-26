package com.lubin.myTable.obj;

public interface IMyTable {
	boolean put(byte[] key, byte[] value);
	byte[] get(byte[] key);
}
