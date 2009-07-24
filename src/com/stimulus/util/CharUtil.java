package com.stimulus.util;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.SortedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.MessagingException;
import javax.mail.internet.MimePart;
import javax.mail.internet.MimeUtility;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.*;
import com.stimulus.archiva.index.MessageIndex;

public class CharUtil {
	
	protected static final Log logger = LogFactory.getLog(MessageIndex.class.getName());
	protected static final Pattern pattern = Pattern.compile(".*charset=(.*)");
	
	  public static String getCharSetFromContentType(String contentType) {
		 	Matcher matcher = pattern.matcher(contentType);
	    	if (matcher.find()) {
	    		return matcher.group(1);
	    	} else {
	    		Pattern pattern = Pattern.compile(".*charset=(.*);");
	    		matcher = pattern.matcher(contentType);
	    		if (matcher.find()) {
	    		   return matcher.group(1);
	    		}
	    	}
	    	return null; 
	  }

	  public static Charset getCharsetFromPartContent(MimePart mpb) throws IOException, MessagingException  {
		 	String contentType = null;	
			try {
				contentType = mpb.getContentType();
			} catch (MessagingException e) {
				logger.debug("failed to retrieve content type from message. character stream assumed to already decoded.");
			}
			String charset = getCharSetFromContentType(contentType);
			if (charset==null)
				return null;
			
			try {
				String javaCharset = MimeUtility.javaCharset(charset);
				Charset cs = Charset.forName(CharUtil.getCharset(javaCharset));
				return cs;
			} catch (Exception e) {
				return null;
			}
    		
	  }
	
	public static String getEncodedStringFromPartContent(MimePart mpb) throws IOException, MessagingException {
		 Charset charset = getCharsetFromPartContent(mpb);
		 if (charset==null) {
		  	return (String)mpb.getContent();
		 } else
			 return decode(charset,(String)mpb.getContent());
	}
	
	public static String decode(Charset encoding, String inStr) {
		ByteBuffer cb = ByteBuffer.wrap(inStr.getBytes());
		return encoding.decode(cb).toString();
	}
	
	public static void convert(Charset inputEncoding, Charset outputEncoding,InputStream inStream, OutputStream outStream) throws IOException {
			ReadableByteChannel in = Channels.newChannel(inStream);
			WritableByteChannel out = Channels.newChannel(outStream);
	
    	for (ByteBuffer inBuffer = ByteBuffer.allocate(4096);
    		 in.read(inBuffer) != -1; inBuffer.clear()) {
    		
    		inBuffer.flip();
    		CharBuffer cBuffer = inputEncoding.decode(inBuffer);
    		ByteBuffer outBuffer = outputEncoding.encode(cBuffer);
    		while (outBuffer.hasRemaining()) out.write(outBuffer);
    	}
		}
 
	
	public static InputStream getTransferDecodedStream(MimePart mpb, InputStream encodedStream) { 
		try {
			String[] transEncoding = mpb.getHeader("Content-Transfer-Encoding");
		  	if(transEncoding!=null && transEncoding.length>0){
		  		return MimeUtility.decode(encodedStream,transEncoding[0]);
		  	} 
		} catch (MessagingException me) {
			logger.debug("failed to decode message transfer encoding. returning original stream.");
		}
		return encodedStream;
	}
	
	public static String getCharset(String charset) {
		      SortedMap<String,Charset> charsets = Charset.availableCharsets();
		      
		      for (Charset cs : charsets.values()) {
		    	  if (charset.toLowerCase(Locale.ENGLISH).contains(cs.name().toLowerCase(Locale.ENGLISH)))
		    		  return cs.name();
		    	  
		    	  for (String alias : cs.aliases()) {
		    		  //System.out.println("alias:"+alias+" charset:"+charset);
		    		  if (charset.toLowerCase(Locale.ENGLISH).contains(alias.toLowerCase(Locale.ENGLISH))) {
		    			  return cs.name();
		    		  } 
		    	  }
		      }
		      return Charset.defaultCharset().name();
	}
	

}
