
/* Copyright (C) 2005-2007 Jamie Angus Band 
 * MailArchiva Open Source Edition Copyright (c) 2005-2007 Jamie Angus Band
 * This program is free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation; either version
 * 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see http://www.gnu.org/licenses or write to the Free Software Foundation,Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

package com.stimulus.archiva.extraction;
import net.freeutils.tnef.mime.TNEFMime;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeUtility;
import org.apache.commons.logging.*;
import com.stimulus.archiva.domain.Config;
import com.stimulus.archiva.domain.Email;
import com.stimulus.archiva.domain.EmailID;
import com.stimulus.archiva.exception.MessageExtractionException;
import com.stimulus.util.DecodingUtil;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.stimulus.util.*;

public class MessageExtraction implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5021107690811863057L;
	protected static final Log logger					= LogFactory.getLog(MessageExtraction.class.getName());
	protected static final String serverEncoding 			= "UTF-8";
	protected HashMap<String,AttachmentInfo> attachments   	= new HashMap<String,AttachmentInfo>();
	protected String fileName;
	protected String baseURL;
	protected String viewFileName;
	protected String messageId;

	public MessageExtraction(Email message, InputStream originalMessageStream, String baseURL) throws MessageExtractionException {
		this.baseURL  = baseURL;
		if (originalMessageStream != null) 
		setFileName(writeOriginalMessage(message.getEmailID(), originalMessageStream));
		setViewFileName(extractMessage(message));
	}
	
	protected String extractMessage(Email message) throws MessageExtractionException
	{
	
		if (message == null)
			throw new MessageExtractionException("assertion failure: null message", logger);

		logger.debug("extracted message {" + message + "}");
	
		Hashtable<String,Part> att 			= new Hashtable<String,Part>();
		Hashtable<String,String> inl 		= new Hashtable<String,String>();
		Hashtable<String,String> imgs 		= new Hashtable<String,String>();
		Hashtable<String,String> nonImgs 	= new Hashtable<String,String>();
		Hashtable<String,String> ready 		= new Hashtable<String,String>();
		ArrayList<String> mimeTypes 		= new ArrayList<String>();
		String viewFileName;
		try {
			dumpPart(message.getUnderlyingMessage(), att, inl, imgs, nonImgs, mimeTypes, message.getSubject());
			for (String attachFileName: att.keySet()) {
				Part p = (Part) att.get(attachFileName);
				writeAttachment(p, attachFileName);
				ready.put(attachFileName, attachFileName);
				//if (!imgs.containsKey(attachFileName) && !nonImgs.containsKey(attachFileName))
					addAttachment(attachFileName, p);
			}
			
			if (inl.containsKey("text/html")) {
				viewFileName = prepareHTMLMessage(baseURL, inl, imgs, nonImgs, ready, mimeTypes);
			} else if (inl.containsKey("text/plain")) {
				viewFileName = preparePlaintextMessage(inl, imgs, nonImgs, ready, mimeTypes);
			} else {
				logger.debug("unable to extract message since the content type is unsupported. returning empty message body.");
				return writeTempMessage("<html><head><META http-equiv=Content-Type content=\"text/html; charset="+serverEncoding+"\"></head><body></body></html>",".html");
			}
		} catch (Exception ex) {
			throw new MessageExtractionException(ex.getMessage(),ex, logger);
		}
		logger.debug("message successfully extracted {filename='" + fileName + "' " + message + "}");
		return viewFileName;
	}
    
    private static void dumpPart(Part p, Hashtable<String,Part> attachments, Hashtable<String,String> inlines, Hashtable<String,String> images, Hashtable<String,String> nonImages, ArrayList<String> mimeTypes, String subject) throws Exception
	{
		mimeTypes.add(p.getContentType());
		if (!p.isMimeType("multipart/*")) {
			
			String disp = p.getDisposition();
			String fname = p.getFileName();
			if (fname!=null) {
				try {
				  fname = MimeUtility.decodeText(fname);
				} catch (Exception e) {
				  logger.debug("cannot decode filename:"+e.getMessage());
				}
			}
			if ((disp != null && Compare.equalsIgnoreCase(disp, Part.ATTACHMENT)) || fname!=null) {
				String filename = getFilename(subject,p);
				if (!filename.equalsIgnoreCase("winmail.dat")) {
					attachments.put(filename, p);
				}
				/*if (p.isMimeType("image/*")) {
					String str[] = p.getHeader("Content-ID");
					if (str != null) images.put(filename,str[0]);
					else images.put(filename, filename);
				}
				return;*/
			}
		}
		if (p.isMimeType("text/plain")) {
			String str = "";
			if (inlines.containsKey("text/plain")) 
				str = (String) inlines.get("text/plain") + "\n\n-------------------------------\n";
			inlines.put("text/plain", str + (String) p.getContent());
		} else if (p.isMimeType("text/html")) {
			inlines.put("text/html", (String) p.getContent());
		} else if (p.isMimeType("text/xml")) {
			attachments.put(getFilename(subject,p), p);
		} else if (p.isMimeType("multipart/*")) {
			Multipart mp = (Multipart) p.getContent();
			for (int i = 0; i < mp.getCount(); i++)
				dumpPart(mp.getBodyPart(i), attachments, inlines, images, nonImages, mimeTypes,subject);
		} else if (p.isMimeType("message/rfc822")) {
			dumpPart((Part) p.getContent(), attachments, inlines, images, nonImages, mimeTypes,subject);
		} else if (p.isMimeType("application/ms-tnef")) {
        	Part tnefpart = TNEFMime.convert(null, p, false); 
        	if (tnefpart!=null) {
        		dumpPart((Part) tnefpart, attachments, inlines, images, nonImages, mimeTypes,subject);
        	}
        }  else if (p.isMimeType("application/*")) {
			String filename = getFilename("application", p);
			attachments.put(filename, p);
			String str[] = p.getHeader("Content-ID");
			if (str != null) nonImages.put(filename, str[0]);
		} else if (p.isMimeType("image/*")) {
			String fileName = getFilename("image", p);
			attachments.put(fileName, p);
			String str[] = p.getHeader("Content-ID");
			if (str != null) 
				images.put(fileName, str[0]);
			else 
				images.put(fileName, fileName);
		} else {
			String contentType = p.getContentType();
			Object o = p.getContent();
			if (o instanceof String) {
				String str = "";
				if (inlines.containsKey("text/plain"))
					str = (String) inlines.get("text/plain") + "\n\n-------------------------------\n";
				inlines.put(contentType, str + (String) o);
			} else {
				String fileName = getFilenameFromContentType("attach", contentType);
				attachments.put(fileName, p);
			}
		}
	}

	// Find the next position of a URI in input text.
	private int getNextURIPos(String input, int pos)
	{
		String protocols[] =
		{ "http://", "https://", "mailto:" };
		int retVal = -1;
		for (int i = 0; i < protocols.length; i++)
		{
			int index = input.indexOf(protocols[i], pos);
			if (index > -1 && (index < retVal || retVal == -1)) retVal = index;
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
		while (idx >= oldIdx) {
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
		String lowerInput = input.toLowerCase(Locale.ENGLISH);
		int index = getNextURIPos(lowerInput, 0);
		int endIndex = 0;
		while (index > -1) {
			if (index > endIndex) output = output + encode(input.substring(endIndex, index));
			endIndex = input.indexOf("\n", index);
			if (endIndex == -1) endIndex = input.length();
			String lastGood = "";
			while (endIndex-- > index + 7) {
				String slice = input.substring(index, endIndex);
				if (slice.endsWith(".") || slice.endsWith("(") || slice.endsWith(")") || slice.endsWith("[") || slice.endsWith("]")
						|| slice.endsWith(",") || slice.endsWith(";") || slice.endsWith(":") || slice.endsWith("{") || slice.endsWith("}"))
					continue;
				try {
					java.net.URI testIt = new java.net.URI(slice);
					
					if (Compare.equalsIgnoreCase(testIt.toString(), slice)) {
						lastGood = input.substring(index, endIndex);
						break;
					}
				}
				catch (java.net.URISyntaxException ex) {}	
			}
			if (lastGood.equals(""))
				output = output + input.substring(index, endIndex);
			else 
				output = output + "<a href=\"" + lastGood + "\" target=\"_blank\">" + encode(lastGood) + "</a>";
			index = getNextURIPos(lowerInput, endIndex);
		}
		if (endIndex < input.length()) output = output + encode(input.substring(endIndex));
		return output;
	}
	
	private void writeAttachment(Part p, String filename) {
		filename = getFilename(filename,"attachment.att");
		File attachFile = new File(Config.getFileSystem().getViewPath() + File.separatorChar + filename);
		OutputStream os = null;
		InputStream is = null;
		try {
            logger.debug("writing attachment {filename='" + attachFile.getAbsolutePath() + "'}");
            os = new BufferedOutputStream(new FileOutputStream(attachFile));
            is = p.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            int c = 0;
            while ((c = bis.read()) != -1) {
            	os.write(c);
            }
            os.close();
            bis.close();
            Config.getFileSystem().getTempFiles().markForDeletion(attachFile);
		} catch (Exception ex) {
			logger.error("failed to write attachment {filename='" + attachFile + "'}", ex);
		} finally {
			try {
				if (os != null) os.close();
				if (is != null) is.close();
			} catch (Exception e) {}
		}
	}



	private String writeTempMessage(String toWrite, String ext)
	{
		BufferedWriter bw = null;
		try {
			File attachFile = File.createTempFile("temp", ext, new File(Config.getFileSystem().getViewPath()));
			logger.debug("writing temporary message {fileName='" + attachFile + "'}");
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(attachFile),"UTF8"));
			bw.write(toWrite);
			Config.getFileSystem().getTempFiles().markForDeletion(attachFile);
			return attachFile.getName();
		} catch (IOException ex) {
			logger.error("failed to write temporary message {fileName='" + fileName + "'}", ex);
		} finally {
			if (bw!=null) { 
				try { bw.close(); } catch (Exception e) {}
			}
		}
		return "";
	}

	
	private String writeOriginalMessage(EmailID emailId, InputStream originalMessageStream)
	{
		File file = new File(Config.getFileSystem().getViewPath() + File.separatorChar + emailId.getUniqueID() + ".eml");
		logger.debug("writeOriginalMessage() {fileName='" + file.getAbsolutePath() + "'}");
		OutputStream os = null;

		try {
            os = new BufferedOutputStream(new FileOutputStream(file));
            BufferedInputStream bis = new BufferedInputStream(originalMessageStream);
            int c = 0;
            while ((c = bis.read()) != -1) {
            	os.write(c);
            }
            Config.getFileSystem().getTempFiles().markForDeletion(file);
			return emailId.getUniqueID() + ".eml";
		} catch (Exception ex) {
			logger.error("failed to write original message, {fileName='" + file.getAbsolutePath() + "'}", ex);
		} finally {
			    try { 
					if (os!=null) { os.close(); } 
					if (originalMessageStream != null) { originalMessageStream.close(); }
				} catch (Exception e) {}
		}
		return "";
	}
	
    private String preparePlaintextMessage(Hashtable<String,String> inl, Hashtable<String,String> imgs, Hashtable<String,String> nonImgs, Hashtable<String,String> ready, ArrayList<String> mimeTypes)
    {
		String str = (String) inl.get("text/plain");
		str = activateURLs(str);
		Enumeration enuma = imgs.keys();
		StringBuffer buff = new StringBuffer();
		while (enuma.hasMoreElements())
		{
			String fl = (String) enuma.nextElement();
			fl = (String) ready.get(fl);
			if (fl.endsWith(".tif") || fl.endsWith(".tiff")) {
				buff.append("<BR><BR><EMBED SRC=\"" + fl + "\" TYPE=\"image/tiff\" >" + System.getProperty("line.separator"));
			} else {
				buff.append("<BR><BR><IMG SRC=\"" + fl + "\">" + System.getProperty("line.separator"));
			}
		}
		str = str.replaceAll("\r", "").replaceAll("\n", "<br>" + System.getProperty("line.separator"));
		return writeTempMessage("<html><head><META http-equiv=Content-Type content=\"text/html; charset="+serverEncoding+"\"></head><body>" + str + System.getProperty("line.separator") + buff.toString() + "</body></html>",".html");
	}

	private String prepareHTMLMessage(String baseURL, Hashtable<String,String> inl, Hashtable<String,String> imgs, Hashtable<String,String> nonImgs, Hashtable<String,String> ready, ArrayList<String> mimeTypes)
    {
		String str = (String) inl.get("text/html");
		boolean alternative = false;
		for (int i = 0; i < mimeTypes.size(); i++) {
			if (((String) mimeTypes.get(i)).toLowerCase(Locale.ENGLISH).indexOf("multipart/alternative") > -1) {
				alternative = true;
				break;
			}
		}
		if (!alternative && inl.containsKey("text/plain")) {
			String plain = activateURLs((String) inl.get("text/plain")).replaceAll("\r", "").replaceAll("\n",
					"<br>" + System.getProperty("line.separator")) + "<br><br>" + System.getProperty("line.separator") + "<hr><br>";
			int bestStart = 0;
			int next = str.toLowerCase(Locale.ENGLISH).indexOf("<body");
			if (next > 0) next = str.indexOf(">", next) + 1;
			if (next > 0 && next < str.length()) bestStart = next;
			if (bestStart > 0) 
					str = str.substring(0, bestStart) + plain + str.substring(bestStart);
			else str = plain + str;
		}
		
		HashSet<String> alreadyUsed = new HashSet<String>();
		Enumeration enuma = imgs.keys();
		
		while (enuma.hasMoreElements()) {
			String repl = (String) enuma.nextElement();
			String cidTag = (String) imgs.get(repl);
			if (cidTag.startsWith("<") && cidTag.endsWith(">")) {
				cidTag = cidTag.substring(1, cidTag.length() - 1);
			}
			if (str.indexOf("cid:" + cidTag) > -1) {
				alreadyUsed.add(repl);
			}
			String st = (String) ready.get(repl);
			str = Pattern.compile("cid:" + cidTag,Pattern.CASE_INSENSITIVE).matcher(str).replaceAll(ready.get(repl));
		}
		enuma = nonImgs.keys();
		
		while (enuma.hasMoreElements()) {
			String repl = (String) enuma.nextElement();
			String cidTag = (String) nonImgs.get(repl);
			if (cidTag.startsWith("<") && cidTag.endsWith(">"))
				cidTag = cidTag.substring(1, cidTag.length() - 1);
	
			if (str.indexOf("cid:" + cidTag) > -1)
				alreadyUsed.add(repl);
			
			String st = (String) ready.get(repl);
			str = Pattern.compile("cid:" + cidTag,Pattern.CASE_INSENSITIVE).matcher(str).replaceAll(ready.get(repl));
		}
		StringBuffer buff = new StringBuffer();
		enuma = imgs.keys();
		while (enuma.hasMoreElements()) {
			String fl = (String) enuma.nextElement();
			if (!alreadyUsed.contains(fl)) {
				fl = (String) ready.get(fl);
				if (fl.endsWith(".tif") || fl.endsWith(".tiff")) {
					buff.append(System.getProperty("line.separator") + "<BR><BR><EMBED SRC=\"" + baseURL.replaceAll("\\\\", "/") + "/temp/" + fl + "\" TYPE=\"image/tiff\">");
				} else {
					buff.append(System.getProperty("line.separator") + "<BR><BR><IMG SRC=\"" + baseURL.replaceAll("\\\\", "/") + "/temp/" + fl + "\">");
				}
			}
		}
		String output = "";
		int bestStart = 0;
		int next = str.toLowerCase(Locale.ENGLISH).indexOf("</body>");
		if (next > 0 && next < str.length()) bestStart = next;
		if (bestStart > 0) output = str.substring(0, bestStart) + buff.toString() + str.substring(bestStart);
		else output = str + buff.toString();
		
		if (output.indexOf("charset=") < 0) {
			next = output.toLowerCase(Locale.ENGLISH).indexOf("</head>");
			if (next > 0)
				output = output.substring(0, next) + "<META http-equiv=Content-Type content=\"text/html; charset="+serverEncoding+"\">"+ output.substring(next);
		} else 
			output = output.replaceFirst("charset=.*\"", "charset="+serverEncoding+"\"");
		
		output = output.replaceAll("FONT SIZE=\\d","FONT");
		output = output.replaceAll("font size=\\d","font");
	
		return writeTempMessage(output, ".html");
		
	}
	

	private static String getFilenameFromContentType(String defaultFileName, String contentType) {
		if (contentType==null) return defaultFileName;
		Pattern pattern = Pattern.compile("name=\"(.*)\"");
		Matcher matcher = pattern.matcher(contentType);
	    if (matcher.find()) 
    		return matcher.group(1);
    		else 
    		return defaultFileName;
	}
	
	private static String getFilename(String defaultFileName, Part p) throws Exception
	{
		if (defaultFileName=="" || defaultFileName==null)
			defaultFileName = "attachment.att";
		String fName = p.getFileName();
		try {
			  fName = MimeUtility.decodeText(fName);
		} catch (Exception e) {
			  logger.debug("cannot decode filename:"+e.getMessage());
			  fName = p.getFileName();
		}
		return getFilename(fName,defaultFileName);
	
	}
	
	private static String getFilename(String fileName, String defaultFileName) {
		try {
			fileName = new File(fileName).getName();
			char illegalChars[] = { '\'', '\\',':','*','?','<','>','|','\n','\r','\t',';',':','=',':','+',
								    '<','>','|','[',']','\"','?','*','#' };
			for (int i = 0; i < illegalChars.length; i++) {
				fileName = fileName.replace(illegalChars[i],'_');
			}
			if (fileName.length()<3)
				fileName = defaultFileName;
			
		} catch (Exception ex)
		{
			fileName = defaultFileName;
		}
		return fileName;
	}
	

	public void addAttachment(String fileName, Part part) {
		attachments.put(fileName,new AttachmentInfo(fileName, part));
	}
	
	public AttachmentInfo getAttachment(String fileName) {
		return attachments.get(fileName);
	}
	
	public List<AttachmentInfo> getAttachments() {
		return (List<AttachmentInfo>)new ArrayList<AttachmentInfo>(attachments.values());
	}
	
	public String getFilePath() {
		String path = Config.getFileSystem().getViewPath() + File.separatorChar + fileName;
		logger.debug("getOriginalMessageFilePath() {fileName='" + path + "'}");
		return path;
	}
	
	public String getFileSize() {
		double size = new File(getFilePath()).length() / 1024.0;
		DecimalFormat df = new DecimalFormat("0.##");
		return df.format(size)+"k";
	}

	public String getFileName() {
		logger.debug("getOriginalMessageFileName() {fileName='" + fileName + "'}");
		return fileName;
	}
	public String getBaseExtractionURL() {
		return baseURL + "/temp";
	}
	
	public String getViewFileName() {
		return viewFileName;
	}
	
	public String getFileURL() {
		logger.debug("getOriginalMessageURL() {URL='" + getBaseExtractionURL() + "/" + getFileName() + "'}");
		return getBaseExtractionURL() + "/" + getFileName();
	}
	
	public void setViewFileName(String fileName) {
		this.viewFileName = fileName;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public String getViewURL() {
		return getBaseExtractionURL() + "/" + getViewFileName();
	}
		
	

	public class AttachmentInfo
	{
		protected String	name;
		protected Part 		part;
		

		public AttachmentInfo(String name, Part part) {
			this.name = name;
			this.part = part;
		}
		
		public Part getPart() { 
			return part;
		}

		public String getName() {
			return name;
		}

		public String getContentType() {
			try {
				return part.getContentType();
			} catch (Exception e) { return ""; }
		}
		
		public boolean getIsEmail() {
			return getContentType().toLowerCase(Locale.ENGLISH).contains("message/rfc822");
		}
		
		public String getURL() {
			return getBaseExtractionURL() + "/" + name;
		}

		public String getFilePath() {
			return Config.getFileSystem().getViewPath() + File.separatorChar + name;
		}
		
		public String getFileSize() {
			double size = new File(getFilePath()).length() / 1024.0;
			  DecimalFormat df = new DecimalFormat("0.##");
			  return df.format(size)+"k";
		}
		
		public InputStream getInputStream() throws FileNotFoundException {
			return new FileInputStream(getFilePath());
		}
	}


}
