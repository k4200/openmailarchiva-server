package com.stimulus.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Locale;

import org.apache.commons.logging.*;

import com.stimulus.archiva.exception.ArchivaException;

public class ConfigUtil {

    protected static final Log logger = LogFactory.getLog(ConfigUtil.class);
	private static String hexits = "0123456789abcdef";
	
    public static boolean getBoolean(String onoff, String defaultValue) {
    	if (onoff==null) return getBoolean(defaultValue,"yes");
    	if (onoff.toLowerCase(Locale.ENGLISH).equals("yes")) return true;
    	else if (onoff.toLowerCase(Locale.ENGLISH).equals("no")) return false;
    	else return getBoolean(defaultValue,"yes");
    }
  
    public static String getString(String str, String defaultValue) {
    	if (str==null) return getString(defaultValue,"");
    	return str;
    }
  
    public static int getInteger(String str, String defaultValue) {
    	int i = Integer.parseInt(defaultValue);
    	if (str==null)
    		return i;
    	try {
    		i = Integer.parseInt(str);
    	} catch (Exception e) {
    		logger.error("failed to parse configuration value {str='"+str+"'}");
    	}
    	return i;
    }
    
    public static float getFloat(String str, String defaultValue) {
    	float i = Float.parseFloat(defaultValue);
    	if (str==null)
    		return i;
    	try {
    		i = Float.parseFloat(str);
    	} catch (Exception e) {
    		logger.error("failed to parse configuration value {str='"+str+"'}");
    	}
    	return i;
    }
  
    public static String getYesNo(boolean b) {
    	if (b) return "yes"; else return "no";
    }
  
    /**
  	* Convert a byte array to a hex encoded string
  	*
  	* @param block
  	*      byte array to convert to hexString
  	* @return String representation of byte array
  	*/
    public static String toHex(byte[] block) {
  		StringBuffer buf = new StringBuffer();
  
  		for (int i = 0; i < block.length; ++i) {
  			buf.append(hexits.charAt((block[i] >>> 4) & 0xf));
  			buf.append(hexits.charAt(block[i] & 0xf));
  		}
  
  		return buf + "";
  	}
  
  	/**
  	* Convert a String hex notation to a byte array
  	*
  	* @param s
  	*      string to convert
  	* @return byte array
  	*/
    public static byte[] fromHex(String s) {
  		s = s.toLowerCase(Locale.ENGLISH);
  		byte[] b = new byte[(s.length() + 1) / 2];
  		int j = 0;
  		int h;
  		int nibble = -1;
  
  		for (int i = 0; i < s.length(); ++i) {
  			h = hexits.indexOf(s.charAt(i));
  			if (h >= 0) {
  				if (nibble < 0) {
  					nibble = h;
  				} else {
  					b[j++] = (byte) ((nibble << 4) + h);
  					nibble = -1;
  				}
  			}
  		}
  
  		if (nibble >= 0) {
  			b[j++] = (byte) (nibble << 4);
  		}
  
  		if (j < b.length) {
  			byte[] b2 = new byte[j];
  			System.arraycopy(b, 0, b2, 0, j);
  			b = b2;
  		}
  
  		return b;
  	}
  
    public static void copyFile(File in, File out)   throws ArchivaException 
    { 
 	   FileChannel inChannel = null;
 	   FileChannel outChannel = null;
 	   try {
	    	   logger.debug("copyFile {source='"+in.getAbsolutePath()+"',dest='"+out.getAbsolutePath()+"'}");
	           inChannel = new FileInputStream(in).getChannel();
	           outChannel = new FileOutputStream(out).getChannel();
	           inChannel.transferTo(0, inChannel.size(),outChannel);
	       } catch (IOException io) {
            throw new ArchivaException("failed to copy file:"+io.getMessage(),io,logger);
        } finally {
            if (inChannel != null) try { inChannel.close(); } catch (Exception e) {} 
            if (outChannel != null) try {  outChannel.close(); } catch (Exception e) {} 
        }
    }

}
