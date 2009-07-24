
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
import org.apache.commons.logging.*;
import com.stimulus.util.*;
import com.stimulus.archiva.authentication.*;
import com.stimulus.archiva.domain.*;
import com.stimulus.archiva.exception.*;
import com.stimulus.archiva.security.realm.*;
import com.stimulus.archiva.log.*;
import javax.activation.*;
import java.text.DateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.*;
import javax.mail.*;
import javax.mail.Message.RecipientType;
import javax.mail.internet.*;
import com.stimulus.archiva.incoming.*;
import com.stimulus.archiva.security.*;
import org.apache.log4j.*;

public class ConfigurationService implements Serializable {

	private static final long serialVersionUID = -7518836928932742067L;
	protected static final Log logger = LogFactory.getLog(ConfigurationService.class);
	protected static Config config = null;
	protected static DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);


  public static ArrayList<ADRealm.AttributeValue> getLDAPAttributeValues(Config config,Identity identity, String username, String password) throws ArchivaException {
      ADRealm ar = new ADRealm();
      return ar.getADAttributes(config,(ADIdentity)identity,username,password);
  }
  public static List<LogFiles.LogFile> getLogFiles() { 
	  return Config.getConfig().getLogFiles().getLogFiles();
  }
  
  public static String viewLog(String logFile) {
	  return Config.getConfig().getLogFiles().viewLog(logFile);
  }
  /*
  public static void sendLog(String logFile) {
	  Config.getConfig().getLogFiles().sendLog(logFile);
  }*/
  
  public static File exportLog(String logFile) throws ArchivaException {
	  return Config.getConfig().getLogFiles().exportLog(logFile);
  }
  public static void setLoggingLevel(ChainedException.Level level) {
	  Config.getConfig().getLogFiles().setLoggingLevel(level);
  }
  
  public static ChainedException.Level getLoggingLevel() {
	  return Config.getConfig().getLogFiles().getLoggingLevel();
  }
  
  public static void deleteLog(String logFile) {
	  Config.getConfig().getLogFiles().deleteLog(logFile);
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

    public static void testMailboxConnection(MailboxConnection connection, IAPTestStatus testStatus) {
	  IAPService service = new IAPService();
	  service.testConnection(connection,testStatus);
  }
  
  
  public static abstract class IAPTestStatus implements IAPRunnable.IAPTestCallback {
	  public abstract void statusUpdate(String result);
  }
  
 
  
}

