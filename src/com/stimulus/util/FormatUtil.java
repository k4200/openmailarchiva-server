package com.stimulus.util;

import java.text.DecimalFormat;

public class FormatUtil {

	
	

	public static String formatSpace(long bytes) {
		  double mb = bytes / 1024.0 / 1024.0;
		  DecimalFormat formatter = new DecimalFormat("#,###,###.##");
		  double tb = mb / 1024.0 / 1024.0;
		  if (tb>=1)
			  return formatter.format(tb)+" TB";
		  
		  double gb = mb / 1024.0;
		  if (gb>=1)
			  return formatter.format(gb)+" GB";
		 
		  return formatter.format(mb)+" MB";
		  
	  }
	
	  public static String formatCount(long count) {
		  DecimalFormat formatter = new DecimalFormat("#,###,###,###.##");
		 
		  double mil = count / 1000000.0;
		  if (mil>=1)
			  return formatter.format(mil)+"M";
		  
		  double k = count / 1000.0;
		  if (k>=1)
			  return formatter.format(k)+"K";
		 
		  return formatter.format(count);
	  }
	  
}
