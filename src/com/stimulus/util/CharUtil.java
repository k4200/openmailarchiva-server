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
				return Charset.forName("UTF-8");
			try {
				String javaCharset = MimeUtility.javaCharset(charset);
				Charset cs = Charset.forName(CharUtil.getCharset(javaCharset));
				return cs;
			} catch (Exception e) {
				return Charset.forName("UTF-8");
			}
    		
	  }
	  public static InputStream getInputStreamFromPart(MimePart mpb) throws IOException, MessagingException {
			
		  Charset charset = getCharsetFromPartContent(mpb);
		  if (charset==null) {
			  	return mpb.getInputStream();
		  } 
		  return CharUtil.getCharEncodedStream( mpb.getInputStream(),charset);
	  }
	
	public static String getEncodedStringFromPartContent(MimePart mpb) throws IOException, MessagingException {
		 Charset charset = getCharsetFromPartContent(mpb);
		 if (charset==null) {
		  	return (String)mpb.getContent();
		 } else
			 return decode(charset,(String)mpb.getContent());
	}
	
	public static String encode(Charset encoding, String inStr) throws IOException {
		Charset koi = Charset.forName("KOI8-R");
		ByteBuffer bbuf = encoding.encode(CharBuffer.wrap(inStr));
		return bbuf.asCharBuffer().toString();
	}
	public static String decode(Charset encoding, InputStream is) {
		try { 
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		Reader r = new InputStreamReader(is, "KOI8-R");
		 Writer w = new BufferedWriter(new OutputStreamWriter(bos, "UTF-8"));
		
	    char[] buffer = new char[4096];
	    int len;
	    while ((len = r.read(buffer)) != -1)
	      // Read a block of input.
	      w.write(buffer, 0, len); // And write it out.
	    r.close(); // Close the input.
	    w.close(); // Flush and close output.
	    return bos.toString("UTF-8");
		} catch (IOException io) {
			io.printStackTrace();
			return "";
		}
	}
	public static String decode(Charset encoding, String inStr) {
		try { 
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			Reader r = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(inStr.getBytes()), "KOI8-R"));
		    Writer w = new BufferedWriter(new OutputStreamWriter(bos, "KOI8-R"));
	
		    char[] buffer = new char[4096];
		    int len;
		    while ((len = r.read(buffer)) != -1)
		      // Read a block of input.
		      w.write(buffer, 0, len); // And write it out.
		    r.close(); // Close the input.
		    w.close(); // Flush and close output.
		    return bos.toString("UTF-8");
		} catch (IOException io) {
			io.printStackTrace();
			return "";
		}
	    
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
 
	// bad coding but little choice
  	public static InputStream getCharEncodedStream(InputStream is, Charset outputEncoding) throws IOException {
	  	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	  	CharUtil.convert(outputEncoding,Charset.forName("UTF-8"),is,bos);
	  	byte[] out = bos.toByteArray();
	  	bos = null;
	  	return new ByteArrayInputStream(out);
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
