package com.stimulus.util;

import java.io.*;

public class StreamUtil {

	public static void  emptyStream(InputStream is) {
			if (is==null) return;
	  		byte[] b = new byte[1024];
	  	    try {
	  	    	while (is.read(b)!=-1) {  }
	  	    } catch (Exception e) {}
	}
}
