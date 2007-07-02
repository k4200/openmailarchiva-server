/* Copyright (C) 2005 Jamie Angus Band 
 * This software program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.stimulus.archiva.persistence;
import java.io.*;
import java.util.*;

import javax.mail.*;
import com.stimulus.archiva.domain.*;
import org.apache.log4j.Logger;
import com.stimulus.archiva.exception.*;

public class MessageExtraction
{
	
	protected static final Logger logger = Logger.getLogger(MessageExtraction.class.getName());
	protected Vector 		filesToRemove  = new Vector();
	protected ArrayList     attachments = new ArrayList();

	protected static int attachmentNo  = 0;
	protected static int tempMessageNo = 0;
	protected Config config;
	protected String baseURL;
	protected String originalMessageFileName;

	public MessageExtraction(Config config, String baseURL) {
		this.config = config;
		this.baseURL = baseURL;
	}

	public List getAttachments() {
	    return attachments;
	}
	
	public Attachment getAttachment(String fileName) throws MessageExtractionException {
	    Iterator i = attachments.listIterator();
	    while (i.hasNext()) {
	        Attachment a = (Attachment)i.next();
	        if (fileName.equalsIgnoreCase(a.getName()))
	            return a;
	    }
	    throw new MessageExtractionException("failed to lookup attachment {fileName='"+fileName+"')",logger);
	}
	

	public String getOriginalMessageFilePath() {
	    String path = Config.getViewPath() + File.separatorChar + originalMessageFileName;
	    logger.debug("getOriginalMessageFilePath() {fileName='"+path+"'}");
	    return path;
	}
	
	public String getOriginalMessageFileName() {
	    logger.debug("getOriginalMessageFileName() {fileName='"+originalMessageFileName+"'}");
	    return originalMessageFileName;
	}

	public String getOriginalMessageURL() {
	    logger.debug("getOriginalMessageURL() {URL='"+getExtractionURL() + "/"+ originalMessageFileName+"'}");
	    return getExtractionURL() + "/"+ originalMessageFileName;
	}

  	public void deleteExtractedMessage() {

	    for(int i=0; i<filesToRemove.size(); i++) {
	        try {
	            new File((String)filesToRemove.elementAt(i)).delete();
	        } catch(Exception ex) {
	        	logger.warn(ex);
	        }
	    }
	}


  	public String getExtractionURL() {
  		return baseURL + "/temp";
  	}

	 public String extractMessage(Email message, InputStream originalMessageStream) throws MessageExtractionException
	  {
	     if (message == null || originalMessageStream == null)
	          throw new MessageExtractionException("assertion failure: null message",logger);

	 	logger.debug("extracted message {"+message+"}");
	 	String messageFile = "";
	  	if (message == null)
	  		throw new MessageExtractionException("failed to display message. no message is selected",logger);

	  	if (originalMessageStream!=null)
	  	    originalMessageFileName = writeOriginalMessage(message.getEmailID(),originalMessageStream);

	    String out = "";
	    Hashtable att 		= new Hashtable();
	    Hashtable inl 		= new Hashtable();
	    Hashtable imgs 		= new Hashtable();
	    Hashtable nonImgs 	= new Hashtable();
	    Hashtable ready 	= new Hashtable();
	    Vector mimeTypes	= new Vector();
	    attachments.clear();

	    try {
		    dumpPart(message, att, inl, imgs, nonImgs, mimeTypes);
		    Enumeration en = att.keys();
		    while(en.hasMoreElements()) {
		        String st = (String)en.nextElement();
		            Part p = (Part)att.get(st);
		            String str = writeAttachment(p, st);
		            ready.put(st, str);
		            if (!imgs.containsKey(st) && !nonImgs.containsKey(st)) {
		            	attachments.add(new Attachment(getFilename(p),baseURL));
		            }
		    }
		    String currDir = Config.getApplicationPath().replace('\\', '/');
		    if (inl.containsKey("text/plain")) {
		        if (inl.containsKey("text/html")) {
		        	messageFile = prepareHTMLMessage(inl, imgs, nonImgs, ready, mimeTypes);
		        } else {
		        	messageFile = preparePlaintextMessage(inl, imgs, nonImgs, ready, mimeTypes);
		        }
		    } else {
		    	Enumeration enum = inl.keys();
	            if (enum.hasMoreElements()) {
	                String key = (String)enum.nextElement();
	                int mn = ++tempMessageNo;
                  writeTempMessage(mn, (String)inl.get(key), ".txt");
                  messageFile = "temp" + mn + ".txt";
	            }
		    }
		  } catch (Exception ex) {
		        throw new MessageExtractionException(ex.toString(),logger);
		  }
		  logger.debug("message successfully extracted {filename='"+messageFile+"' "+message+"}");
		  return messageFile;
	  }

	  private static void dumpPart(Part p, Hashtable attachments, Hashtable inlines,
	                              Hashtable images, Hashtable nonImages, Vector mimeTypes) throws Exception {
      mimeTypes.addElement(p.getContentType());
      if (!p.isMimeType("multipart/*")) {
		    String disp = p.getDisposition();
			if (disp != null && disp.equalsIgnoreCase(Part.ATTACHMENT)) {
			      String fl = getFilename(p);
			      String fn = new File(fl).getName();
			      attachments.put(fn, p);
			      if (p.isMimeType("image/*")) {
			          String str[] = p.getHeader("Content-ID");
			          if (str != null)
			              images.put(fn, str[0]);
			          else
			              images.put(fn, fn);
			      }
			      return;
			}
      }
	    /********************** content type ********************************************/
      if (p.isMimeType("text/plain")) {
            String str = "";
            if (inlines.containsKey("text/plain"))
                str = (String)inlines.get("text/plain") + "\n\n-------------------------------\n";
            inlines.put("text/plain", str + (String)p.getContent());
	    } else if (p.isMimeType("text/html")) {
            inlines.put("text/html", (String)p.getContent());
	    } else if (p.isMimeType("text/xml")) {
	    	  attachments.put(new File(getFilename(p)).getName(),p);
	    } else if (p.isMimeType("multipart/*")) {
				Multipart mp = (Multipart)p.getContent();
				for (int i = 0; i < mp.getCount(); i++)
				      dumpPart(mp.getBodyPart(i), attachments, inlines, images, nonImages, mimeTypes);
	    } else if (p.isMimeType("message/rfc822")) {
	        dumpPart((Part)p.getContent(), attachments, inlines, images, nonImages, mimeTypes);
	    } else if (p.isMimeType("application/*")) {
            String fl = getFilename(p);
            String fn = new File(fl).getName();
            attachments.put(fn, p);
            String str[] = p.getHeader("Content-ID");
            if (str != null)
                nonImages.put(fn, str[0]);
	    } else if (p.isMimeType("image/*")) {
            String fl = getFilename(p);
            String fn = new File(fl).getName();
            attachments.put(fn, p);
            String str[] = p.getHeader("Content-ID");
            if (str != null)
                images.put(fn, str[0]);
            else
                images.put(fn, fn);
	    } else {
	        Object o = p.getContent();
          String contentType = p.getContentType();
	        if (o instanceof String) {
	            String str = "";
		        if (inlines.containsKey("text/plain")) {
		           str = (String)inlines.get("text/plain") + "\n\n-------------------------------\n";
		        }
		        inlines.put(contentType, str + (String)o);
	        } else if (o instanceof InputStream) {
          InputStream is = (InputStream)o;
		    int c;
          StringBuffer buff = new StringBuffer();
		    while ((c = is.read()) != -1)
		        buff.append(c);
              String str = "";
              if (inlines.containsKey("text/plain"))
                  str = (String)inlines.get("text/plain") + "\n\n-------------------------------\n";
              inlines.put(contentType, str + buff.toString());
	        } else
	        	attachments.put(new File(getFilename(p)).getName(),p);
	    }
	  }

		private void writeTempMessage(int mn, String toWrite, String ext) {
			String filename = Config.getViewPath() + File.separatorChar + "temp" + mn + ext;
			logger.debug("writing temporary message {filename='"+filename+"'}");
			try {
		        FileWriter fw = new FileWriter(filename);
		        BufferedWriter bw = new BufferedWriter(fw);
		        bw.write(toWrite);
		        bw.close();
		        filesToRemove.addElement(filename);
		    }
		    catch(IOException ex) {
		        logger.error("failed to write temporary message {filename='"+filename+"'}",ex);
		    }
		}

		public static String replaceCasePreserving(String sourceString, String replace, String with) {
		    if (sourceString == null || replace == null || with == null)
		    	return sourceString;
		    String lower = sourceString.toLowerCase();
		    int shift = 0;
		    int idx = lower.indexOf(replace);
		    if (idx<0)
		        return sourceString;
		    int length = replace.length();
		    StringBuffer resultString = new StringBuffer(sourceString);
		  
		    do {
		    
		      resultString = resultString.replace(idx + shift, idx + shift + length, with);
		    
		      shift += with.length() - length;
		      idx = lower.indexOf(with, idx + length);
		    } while (idx > 0);
		    return resultString.toString();
		}

		  // Find the next position of a URI in input text.
		  private int getNextURIPos(String input, int pos) {
		      String protocols[] = {"http://", "https://", "mailto:"};
		      int retVal = -1;
		      for(int i=0; i<protocols.length; i++) {
		          int index = input.indexOf(protocols[i], pos);
		          if (index > -1 && (index < retVal || retVal == -1))
		              retVal = index;
		      }
		      return retVal;
		  }

		  // encode HTML characters so they do not get interpreted as markup.
		  // Double spaces get changed to use &nbsp; so the spaces are preserved.
		  public static String encode(String src) {
		      src = src.replaceAll("&", "&amp;");
		      src = src.replaceAll("\"", "&quot;");
		      src = src.replaceAll("<", "&lt;");
		      src = src.replaceAll(">", "&gt;");
		      StringBuffer converted = new StringBuffer("");
		      int idx = src.indexOf("  ");
		      int oldIdx = 0;
		      while(idx >= oldIdx) {
		          converted.append(src.substring(oldIdx, idx) + "&nbsp;");
		          oldIdx = idx + 1;
		          idx = src.indexOf("  ", oldIdx);
		      }
		      converted.append(src.substring(oldIdx));
		      src = converted.toString();
		      return src;
		  }

		  private String activateURLs(String input) {
		      if (input.length() == 0) return "";
		      String output = "";
		      String lowerInput = input.toLowerCase();
		      int index = getNextURIPos(lowerInput, 0);
		      int endIndex = 0;
		      while(index > -1) {
		          if (index > endIndex)
		              output = output + encode(input.substring(endIndex, index));
		          endIndex = input.indexOf("\n", index);
		          if (endIndex == -1) endIndex = input.length();
		          String lastGood = "";
		          while(endIndex-- > index+7) {
		              String slice = input.substring(index, endIndex);
		              if (slice.endsWith(".") || slice.endsWith("(") || slice.endsWith(")") ||
		                  slice.endsWith("[") || slice.endsWith("]") || slice.endsWith(",") ||
		                  slice.endsWith(";") || slice.endsWith(":") || slice.endsWith("{") ||
		                  slice.endsWith("}")) continue;
		              try {
		                  java.net.URI testIt = new java.net.URI(slice);
		                  if (testIt.toString().equalsIgnoreCase(slice)) {
		                      lastGood = input.substring(index, endIndex);
		                      break;
		                  }
		              } catch(java.net.URISyntaxException ex) {
		              }
		          }
		          if (lastGood.equals("")) {
		              output = output + input.substring(index, endIndex);
		          } else
		              output = output + "<a href=\"" + lastGood + "\" target=\"_blank\">" + encode(lastGood) + "</a>";
		          index = getNextURIPos(lowerInput, endIndex);
		      }
		      if (endIndex < input.length()) output = output + encode(input.substring(endIndex));
		      return output;
		  }

     private String writeAttachment(Part p, String filename) {
      
	  String end = filename;
      String fl = Config.getViewPath() + File.separatorChar + end;
      OutputStream os = null;
      InputStream is = null;
		try {

	        while(new File(fl).exists()) {
	            end = (++attachmentNo) + filename;
	            fl = Config.getViewPath() + File.separatorChar + end;
	        }
	        logger.debug("writing attachment {filename='"+fl+"'}");
	        
	        os = new BufferedOutputStream(new FileOutputStream(fl));
	        is = p.getInputStream();
	        BufferedInputStream bis = new BufferedInputStream(is);
	        int c;
	        while ((c = bis.read()) != -1)
	            os.write(c);
	        os.close();
	        filesToRemove.addElement(fl);
	        return end;
	    } catch (Exception ex) {
	    	logger.error("failed to write attachment {filename='"+fl+"'}",ex);

	    	try {
	    	    if (os!=null) os.close();
	    	    if (is!=null) is.close();
	    	} catch (Exception e) {}
	    }
	    return "";
	}

     private String writeOriginalMessage(EmailID emailId,InputStream originalMessageStream) {
        String filename =  Config.getViewPath() + File.separatorChar + emailId.getUniqueID() + ".eml";
        logger.debug("writeOriginalMessage() {filename='"+filename+"'}");
        OutputStream os = null;

        try {
            os = new BufferedOutputStream(new FileOutputStream(filename));
            BufferedInputStream bis = new BufferedInputStream(originalMessageStream);
            int c;
            while ((c = bis.read()) != -1)
                os.write(c);
            os.close();
            filesToRemove.addElement(filename);
            return emailId.getUniqueID() + ".eml";
        } catch (Exception ex) {
	    	logger.error("failed to write original message, {filename='"+filename+"'}",ex);
	    	try {
	    	    if (os!=null) os.close();
	    	    if (originalMessageStream!=null) originalMessageStream.close();
	    	} catch (Exception e) {}
        }
        return "";
     }


	private String preparePlaintextMessage(Hashtable inl, Hashtable imgs, Hashtable nonImgs, Hashtable ready, Vector mimeTypes) {
			String str = (String)inl.get("text/plain");
	        str = activateURLs(str);
	        int mn = ++tempMessageNo;
	        Enumeration enum = imgs.keys();
	        StringBuffer buff = new StringBuffer();
	        while(enum.hasMoreElements()) {
	            String fl = (String)enum.nextElement();
	            fl = (String)ready.get(fl);
	            buff.append("<BR><BR><IMG SRC=\"" + fl + "\">" + System.getProperty("line.separator"));
	        }
	        str = str.replaceAll("\r", "").replaceAll("\n", "<br>" +
	        System.getProperty("line.separator"));
	        writeTempMessage(mn, "<html><head></head><body>" + str +
	        System.getProperty("line.separator") +
	        buff.toString() + "</body></html>", ".html");
	        return "temp" + mn + ".html";
	}

	private String prepareHTMLMessage(Hashtable inl, Hashtable imgs, Hashtable nonImgs, Hashtable ready, Vector mimeTypes) {
		String str = (String)inl.get("text/html");
	    boolean alternative = false;
	    for (int i=0; i<mimeTypes.size(); i++) {
	        if (((String)mimeTypes.elementAt(i)).toLowerCase().indexOf("multipart/alternative") > -1) {
	            alternative = true;
	            break;
	        }
	    }
	    if (!alternative && inl.containsKey("text/plain")) {
	        String plain = activateURLs((String)inl.get("text/plain")).replaceAll
	            ("\r", "").replaceAll("\n", "<br>" + System.getProperty("line.separator")) +
	             "<br><br>" + System.getProperty("line.separator") + "<hr><br>";
	        int bestStart = 0;
	        int next = str.toLowerCase().indexOf("<body");
	        if (next > 0) next = str.indexOf(">", next) + 1;
	        if (next > 0 && next < str.length()) bestStart = next;
	        if (bestStart > 0) str = str.substring(0, bestStart) +
	            plain + str.substring(bestStart);
	        else str = plain + str;
	    }
	    int mn = ++tempMessageNo;
	    HashSet alreadyUsed = new HashSet();
	    Enumeration enum = imgs.keys();
	    while(enum.hasMoreElements()) {
	        String repl = (String)enum.nextElement();
	        String cidTag = (String)imgs.get(repl);
	        if (cidTag.startsWith("<") && cidTag.endsWith(">")) {
	            cidTag = cidTag.substring(1, cidTag.length()-1);
	        }
	        if (str.indexOf("cid:" + cidTag) > -1) {
	            alreadyUsed.add(repl);
	        }
	        str = replaceCasePreserving(str,"cid:" + cidTag,(String)ready.get(repl));
	    }
	    enum = nonImgs.keys();
	    while(enum.hasMoreElements()) {
	        String repl = (String)enum.nextElement();
	        String cidTag = (String)nonImgs.get(repl);
	        if (cidTag.startsWith("<") && cidTag.endsWith(">")) {
	            cidTag = cidTag.substring(1, cidTag.length()-1);
	        }
	        if (str.indexOf("cid:" + cidTag) > -1) {
	            alreadyUsed.add(repl);
	        }
	        //str = str.replaceAll("cid:" + cidTag,  (String)ready.get(repl));
	        str = replaceCasePreserving(str,"cid:" + cidTag,(String)ready.get(repl));
	    }
	    StringBuffer buff = new StringBuffer();
	    enum = imgs.keys();
	    while(enum.hasMoreElements()) {
	        String fl = (String)enum.nextElement();
	        if (!alreadyUsed.contains(fl)) {
	            fl = (String)ready.get(fl);
	            buff.append(System.getProperty("line.separator") +
	                "<BR><BR><IMG SRC=\"" + baseURL.replaceAll("\\\\", "/") +
	                "/temp/" + fl + "\">");
	        }
	    }
	    String output = "";
	    int bestStart = 0;
	    int next = str.toLowerCase().indexOf("</body>");
	    if (next > 0 && next < str.length()) bestStart = next;
	    if (bestStart > 0) output = str.substring(0, bestStart) +
	        buff.toString() + str.substring(bestStart);
	    else output = str + buff.toString();
	    writeTempMessage(mn, output, ".html");
	    return "temp" + mn + ".html";
	  }

	  private static void dumpAttachments(Part p, List attachments) throws Exception {
	  	String disp = p.getDisposition();
	  	 if (disp == null || disp.equalsIgnoreCase(Part.ATTACHMENT)) {
	  	 	String f1 = getFilename(p);
	  	 	String fn = new File(f1).getName();
	  	 	attachments.add(fn);
	  	 	return;
	  	 } else {
	  	 	if (p.isMimeType("multipart/*")) {
	  	 		Multipart mp = (Multipart)p.getContent();
		        int count = mp.getCount();
		        for (int i = 0; i < count; i++) {
		        	dumpAttachments(mp.getBodyPart(i), attachments);
	              }
	  	 	}
	  	 }
	  }

	  private static String getFilename(Part p) throws Exception {
	      String filename = p.getFileName();
	      if (filename == null || filename.trim().equals("")) {
	          filename = "Attachment" + (attachmentNo++) + ".att";
	      } else {
	          try {
	              filename = new File(filename).getName();
	              String illegalChars[] =
	                  {"\\\\", "\\/", "\\:", "\\*", "\\?", "\\\"", "\\<", "\\>", "\\|", "\\n", "\\r", "\\t"};
	              for(int i=0; i<illegalChars.length; i++) {
	                  filename = filename.replaceAll(illegalChars[i], "_");
	              }
	          } catch(Exception ex) {
	              filename = "Attachment" + (++attachmentNo) + ".att";
	          }
	      }
	      return filename;
	  }

	  protected void finalize() {
	  	deleteExtractedMessage();
	  }


	  public class Attachment {
	  	protected String name;
	  	protected String baseUrl;
	
	  	public Attachment(String name, String baseUrl) {
	  		this.name = name;
	  		this.baseUrl = baseUrl;
	  	}

	  	public String getName() { return name; }
	    
	  	public String getURL() { 
	        return baseURL +File.separatorChar + "temp" + File.separatorChar + name;
	    }
	    public String getFilePath() { 
	        return  Config.getViewPath() + File.separatorChar + File.separatorChar + name; 
        }
	  }

}