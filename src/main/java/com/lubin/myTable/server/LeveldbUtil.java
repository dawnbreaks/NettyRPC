package com.lubin.myTable.server;

import static org.fusesource.leveldbjni.JniDBFactory.factory;

import java.io.File;
import java.io.IOException;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

public class LeveldbUtil {
	
	private static LeveldbUtil instance;

	private DB db=null;
	
	public static LeveldbUtil getInstance(){
		if (instance == null){
			synchronized (LeveldbUtil.class){
				if (instance == null){
					instance = new LeveldbUtil("default");
				}
			}
		}
		return instance;
	}

	LeveldbUtil(String dbName){
		try {
			Options options = new Options();
			options.createIfMissing(true);
			db = factory.open(new File(dbName), options);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void put(byte[] key, byte[] value){
		db.put(key, value);
	}
	
	public byte[] get(byte[] key){
		return db.get(key);
	}
}
