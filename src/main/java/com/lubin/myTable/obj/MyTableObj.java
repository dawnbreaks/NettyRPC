package com.lubin.myTable.obj;

import com.lubin.myTable.server.LeveldbUtil;

public class MyTableObj implements IMyTable {

	@Override
	public byte[] get(byte[] key) {
		return LeveldbUtil.getInstance().get(key);
	}

	@Override
	public boolean put(byte[] key, byte[] value) {
		LeveldbUtil.getInstance().put(key, value);
		return true;
	}
}
