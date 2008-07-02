
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


package com.stimulus.archiva.service;
import org.apache.log4j.Logger;
import com.stimulus.util.*;
import com.stimulus.archiva.authentication.*;
import com.stimulus.archiva.domain.*;
import com.stimulus.archiva.exception.*;
import com.stimulus.archiva.security.realm.*;
import javax.activation.*;
import java.text.DateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.*;

import javax.mail.*;
import javax.mail.Message.RecipientType;
import javax.mail.internet.*;

import org.apache.log4j.Level;
import com.stimulus.archiva.incoming.*;
import com.stimulus.archiva.security.*;

public class ConfigurationService implements Serializable {

	private static final long serialVersionUID = -7518836928932742067L;
	protected static final Logger logger = Logger.getLogger(ConfigurationService.class);
	protected static Config config = null;
	protected static DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);


  public static void modifyConfig(MailArchivaPrincipal principal, Config newConfig) throws ConfigurationException {
	  Config.getConfig().modify(principal,newConfig);
  }
  
  public static ArrayList<ADRealm.AttributeValue> getLDAPAttributeValues(Config config,Identity identity, String username, String password) throws ArchivaException {
      ADRealm ar = new ADRealm();
      return ar.getADAttributes(config,(ADIdentity)identity,username,password);
  }
  
  
  public static MailArchivaPrincipal authenticate(String username, String password) {
	    ADRealm ar = new ADRealm();
	    MailArchivaPrincipal cgp = null;
	    try {
	          cgp = (MailArchivaPrincipal)ar.authenticate(Config.getConfig(),username,password);
	          return cgp;
	      } catch (ArchivaException ae) {
	          return null;
	      }
  }
  
  public static String testAuthenticate(Config config, String username, String password)  {
      ADRealm ar = new ADRealm();
      //Principal p = ar.authenticate(username,password);
      MailArchivaPrincipal cgp = null;
      try {
          cgp = (MailArchivaPrincipal)ar.authenticate(config,username,password);
          if (cgp!=null) {
              String roleStr = cgp.getRole();
              Roles.Role role = Config.getConfig().getRoles().getRole(roleStr);
              return "Authentication success. Role "+role.getName()+" is assigned.";
          } else
              return "Authentication failed.";
      } catch (ArchivaException ae) {
          return "Authentication failed. "+ae.getMessage()+".";
      }
  } 
  
  public static String getDebugLog() {
	  return Tail.tail(Config.getFileSystem().getDebugLogPath(),300, 200000);
  }
  
  public static String getAuditLog() {
	  return Tail.tail(Config.getFileSystem().getAuditLogPath(),300, 200000);
  }
  
  private static String readFile(String filename) {
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
  
  public static void setLoggingLevel(String level) {
	  Logger logger = Logger.getLogger("com.stimulus");
	  if (logger==null) {
		  logger.error("failed set logging level. failed to obtain logger for com.stimulus.archiva.");
		  return;
	  }
	  Level newLevel = Level.toLevel(level);
	  Level oldLevel = logger.getLevel();
	  if (oldLevel==null) {
		  logger.error("the standard log4j.properties file has been modified. therefore logging cannot be controlled in the server console.");
		  return;
	  }
	
	  logger.setLevel(newLevel);
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
  
  public static String getLoggingLevel() {
	  Logger logger = Logger.getLogger("com.stimulus");
	  if (logger!=null || logger.getLevel()!=null)
		  return logger.getLevel().toString().toUpperCase(Locale.ENGLISH);
	  else {	  
		  logger.error("the standard log4j.properties file has been modified. therefore logging cannot be controlled in the server console.");
		  return "OFF";
	  }
  }
  
  public static void testMailboxConnection(MailboxConnection connection, IAPTestStatus testStatus) {
	  IAPService service = new IAPService(null);
	  service.testConnection(connection,testStatus);
  }
  
  
  public static abstract class IAPTestStatus implements IAPRunnable.IAPTestCallback {
	  
	  public abstract void statusUpdate(String result);
  }
  
}

