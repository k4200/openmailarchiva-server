package com.stimulus.util;

public class ByteUtil {
	  
	 	public static byte[] mergeByteArrays(byte[] pa, byte[] pb) {
	 		byte[] arr = new byte[pa.length + pb.length];
	 		for (int x=0; x < pa.length; x++) {
	 			arr[x] = pa[x];
	 		}
	 		for (int x=0; x < pb.length; x++) {
	 			arr[x+pa.length] = pb[x];
	 		}
	 		return arr;
	 	}
	 	
}
