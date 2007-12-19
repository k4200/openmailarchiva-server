
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.stimulus.archiva.authentication.ADIdentity;
import com.stimulus.archiva.authentication.LDAPIdentity;
import com.stimulus.archiva.domain.Config;
import com.stimulus.archiva.exception.ArchivaException;
import com.stimulus.archiva.security.realm.ADRealm;
import com.stimulus.archiva.security.realm.MailArchivaPrincipal;
import com.stimulus.util.Tail;
public class ConfigurationService implements Serializable {

 /**
	 * 
	 */
	private static final long serialVersionUID = -7518836928932742067L;
protected static Logger logger = Logger.getLogger(ConfigurationService.class);
 protected static Config config = null;

  public static synchronized Config getConfig() {
      return Config.getConfig();
  }

  public static void setConfig(Config config) {
  	Config.setConfig(config);
  }
  
  public static ArrayList<ADRealm.AttributeValue> getLDAPAttributeValues(LDAPIdentity identity, String username, String password) throws ArchivaException {
      ADRealm ar = new ADRealm();
      return ar.getADAttributes((ADIdentity)identity,username,password);

  }
  
  public static String testAuthenticate(Config config, String username, String password)  {
      ADRealm ar = new ADRealm();
      //Principal p = ar.authenticate(username,password);
      MailArchivaPrincipal cgp = null;
      try {
          cgp = (MailArchivaPrincipal)ar.authenticate(config,username,password);
          if (cgp!=null) {
              String roleName = cgp.getRole();
              return "Authentication success. Role "+roleName+" is assigned.";
          } else
              return "Authentication failed.";
      } catch (ArchivaException ae) {
          return "Authentication failed. "+ae.getMessage()+".";
      }
  } 
  
  public static String getDebugLog() {
	  return Tail.tail(Config.getDebugLogPath(),300, 200000);
  }
  
  public static String getAuditLog() {
	  return Tail.tail(Config.getAuditLogPath(),300, 200000);
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
	  Logger logger =  Logger.getLogger("com.stimulus");
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
	  String logfilename = Config.getClassesPath()+File.separator+"log4j.properties";
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
	  Logger logger =  Logger.getLogger("com.stimulus");
	  if (logger!=null || logger.getLevel()!=null)
		  return logger.getLevel().toString().toUpperCase(Locale.ENGLISH);
	  else {	  
		  logger.error("the standard log4j.properties file has been modified. therefore logging cannot be controlled in the server console.");
		  return "OFF";
	  }
  }
  
  
}

