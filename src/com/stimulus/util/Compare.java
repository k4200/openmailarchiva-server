package com.stimulus.util;
import java.util.*;

public class Compare {

	// turkish locale fix
	public static boolean equalsIgnoreCase(String str1, String str2) {
		return str1.toLowerCase(Locale.ENGLISH).equals(str2.toLowerCase(Locale.ENGLISH));
	}
	
}
