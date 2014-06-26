package com.lubin.myTable.test;

import java.io.File;
import java.io.IOException;

import org.iq80.leveldb.*;
import static org.fusesource.leveldbjni.JniDBFactory.*;
import java.io.*;

@SuppressWarnings("unused")
public class TestLeveldbjni {
	public static void main(String[] args) throws IOException{
		Options options = new Options();
		options.createIfMissing(true);
		DB db=null;
	
		try {
			db = factory.open(new File("example"), options);
			
			db.put(bytes("Tampa"), bytes("rocks"));
			String value = asString(db.get(bytes("Tampa")));
			db.delete(bytes("Tampa"));
			
		  // Use the db in here....
		} finally {
		  // Make sure you close the db to shutdown the 
		  // database and avoid resource leaks.
		  db.close();
		}
	}
}
