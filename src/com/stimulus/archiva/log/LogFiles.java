/* Copyright (C) 2005-2009 Jamie Angus Band
 * MailArchiva Open Source Edition Copyright (c) 2005-2009 Jamie Angus Band
 * This program is free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation; either version
 * 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see http://www.gnu.org/licenses or write to the Free Software Foundation,Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

package com.stimulus.archiva.log;

import java.io.*;
import java.text.DateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Multipart;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.stimulus.archiva.domain.*;
import com.stimulus.archiva.domain.Volume.Status;
import com.stimulus.util.*;
import com.stimulus.archiva.exception.*;

public class LogFiles {

	protected static final Log logger = LogFactory.getLog(LogFiles.class);
	protected static DateFormat formatter = DateUtil.getShortDateFormat();

	public LogFiles() {

	}

	public List<LogFile> getLogFiles() {
		List<LogFile> logFiles = new ArrayList<LogFile>();
		String logPath = Config.getFileSystem().getLogPath();
		File logDir = new File(logPath);
		File[] logFileList  = logDir.listFiles();
		for (File logFile : logFileList) {
			logFiles.add(new LogFile(logFile));
		}
		 Collections.sort(logFiles,Collections.reverseOrder());
		return logFiles;
	}


	public class LogFile implements Comparable<LogFile> {

		File logFile;

		public LogFile(File file) {
			this.logFile = file;
		}
		public String getName() { return logFile.getName(); }
		public String getModified() { return (new Date(logFile.lastModified()).toString()); }
		public String getSize() { return logFile.length() / 1024 + "k"; }

		public int compareTo(LogFile l) throws ClassCastException {
				if (logFile.lastModified() > l.logFile.lastModified())
					return 1;
				else
					return -1;
		}
	}

	 public String viewLog(String logFile) {
		  String logPath = Config.getFileSystem().getLogPath();
		  return Tail.tail(logPath + File.separator + logFile, 600, 400000);
	 }

	 public void deleteLog(String logFile) {
		 String logPath = Config.getFileSystem().getLogPath();
		 File file = new File(logPath + File.separator + logFile);
		 file.delete();
	 }

	 public File exportLog(String logFile) throws ArchivaException {

		 try {
			 	File tempFile = File.createTempFile(logFile, ".zip");
		        ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(tempFile));
		        FileInputStream in = new FileInputStream(Config.getFileSystem().getLogPath()+File.separator+logFile);
		        byte b[] = new byte[1024];
		        ZipEntry e = new ZipEntry(logFile);
			    zout.putNextEntry(e);
			    int len=0;
			    while((len=in.read(b)) != -1) {
			       zout.write(b,0,len);
			    }
			    zout.closeEntry();
			    in.close();
			    zout.close();
			    return tempFile;
			} catch (Exception e) {
				throw new ArchivaException("failed to export debug log:"+e.getMessage(),e,logger);
			}
	 }

	 /*
	 public void sendLog(String logFile) {

	    if (Config.getConfig().getSmtpServerAddress().length()<1) {
	    	logger.error("failed to send debug log as there are no SMTP server settings defined");
	    	return;
	    }

	    try {
	    	SMTPSendMessage sendMessage = new SMTPSendMessage(Config.getConfig());
	    	MimeMessage message = new MimeMessage(sendMessage.getSession());
		 	String subject = "Log "+logFile+" on "+formatter.format(new Date());
			List<Domains.Domain> domains = Config.getConfig().getDomains().getDomains();
		 	if (domains.size()>0)
		 		subject +=" " + domains.get(0).getName();
		 	Config config = Config.getConfig();
			String fromAddress = config.getSmtpFromAddress();
		 	message.setFrom(new InternetAddress(fromAddress));
		 	message.setSubject(subject);
	        MimeBodyPart mbp1 = new MimeBodyPart();
	        mbp1.setText(Config.getConfig().getStatusService().getStatusReport());
	        MimeBodyPart mbp2 = new MimeBodyPart();
	        File tempFile = File.createTempFile(logFile, ".zip");
	        ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(tempFile));
	        FileInputStream in = new FileInputStream(Config.getFileSystem().getDebugLogPath());
	        byte b[] = new byte[1024];
	        ZipEntry e = new ZipEntry("debug.log");
		    zout.putNextEntry(e);
		    int len=0;
		    while((len=in.read(b)) != -1) {
		       zout.write(b,0,len);
		    }
		    zout.closeEntry();
		    in.close();
		    zout.close();
	        FileDataSource fds = new FileDataSource(tempFile);
	        mbp2.setDataHandler(new DataHandler(fds));
	        mbp2.setFileName(fds.getName());
	        Multipart mp = new MimeMultipart();
	        mp.addBodyPart(mbp1);
	        mp.addBodyPart(mbp2);
	        message.setContent(mp);
	        message.setSentDate(new Date());
	        InternetAddress to = new InternetAddress(Config.getConfig().getSupportEmail());
	     	message.setRecipient(RecipientType.TO,to);
			Address[] addresses = { to };
			sendMessage.connect();
			sendMessage.sendMessage(message,addresses);
		} catch (Exception e) {
			logger.error("failed to send debug log:"+e.getMessage());
		}

	  }
	  */
	  private String readFile(String filename) {
		  String s = "";
		  FileInputStream in = null;
		  try {
			  	File file = new File(filename);
			  		byte[] buffer = new byte[(int) file.length()];
			  		in = new FileInputStream(file);
			  		in.read(buffer);
			  		s = new String(buffer);
			  		in.close();
		  } catch (FileNotFoundException fnfx) {
			  logger.error("failed to locate log file. "+fnfx.getMessage()+" {filename='"+filename+"'}");
		  } catch (IOException iox) {
			  	logger.error("io exception occurred while reading log file. "+iox.getMessage()+" {filename='"+filename+"'}");
				  } finally {
					  if (in != null) {
					  try
					  {
						  in.close();
					  } catch (IOException ignore) {}
			      }
		 }
	     return s;
	  }

	  public void setLoggingLevel(ChainedException.Level newLevel) {
		  Log logger = LogFactory.getLog("com.stimulus");
		  if (logger==null) {
			  logger.error("failed set logging level. failed to obtain logger for com.stimulus.archiva.");
			  return;
		  }

		  ChainedException.Level oldLevel = ChainedException.getLoggingLevel(logger);
		  if (oldLevel==null) {
			  logger.error("the standard log4j.properties file has been modified. therefore logging cannot be controlled in the server console.");
			  return;
		  }

		  // log4j specific code
		  org.apache.log4j.Logger logger2 = org.apache.log4j.Logger.getLogger("com.stimulus");
		  logger2.setLevel(org.apache.log4j.Level.toLevel(newLevel.toString()));
		  String logfilename = Config.getFileSystem().getClassesPath()+File.separator+"log4j.properties";
		  String logsource = readFile(logfilename);
		  logsource = logsource.replaceAll("log4j.logger.com.stimulus="+oldLevel.toString(),"log4j.logger.com.stimulus="+newLevel.toString());
		  logsource = logsource.replaceAll("log4j.logger.com.stimulus="+oldLevel.toString().toUpperCase(Locale.ENGLISH),"log4j.logger.com.stimulus="+newLevel.toString());
		  File file = new File(logfilename);
		  try {
			  FileWriter out = new FileWriter(file);
			  out.write(logsource);
			  out.close();
		  } catch (Exception e) {
			  logger.error("failed to write new log file. "+e.getMessage()+" {filename='"+logfilename+"'");
		  }
	  }

	  public ChainedException.Level getLoggingLevel() {
		  Log logger = LogFactory.getLog("com.stimulus");
		  if (logger!=null)
			  return ChainedException.getLoggingLevel(logger);
		  else {
			  logger.error("the standard log4j.properties file has been modified. therefore logging cannot be controlled in the server console.");
			  return ChainedException.Level.DEBUG;
		  }
	  }


}
