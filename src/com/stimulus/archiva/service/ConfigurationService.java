
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
import com.stimulus.archiva.domain.Config;
import com.stimulus.archiva.exception.*;
import com.stimulus.archiva.security.realm.*;
import java.util.*;
//import java.util.List;

public class ConfigurationService {

 protected static final Logger logger = Logger.getLogger(ConfigurationService.class);
 protected static Config config = null;

  public static synchronized Config getConfig() {
      return Config.getConfig();
  }

  public static void setConfig(Config config) {
  	Config.setConfig(config);
  }
  
  public static ArrayList<ADRealm.AttributeValue> getLDAPAttributeValues(Config config, String username, String password) throws ArchivaException {
      ADRealm ar = new ADRealm();
      //Principal p = ar.authenticate(username,password);
      return ar.getLDAPAttributes(config,username,password);
  }
  
  public static String testAuthenticate(Config config, String username, String password)  {
      ADRealm ar = new ADRealm();
      //Principal p = ar.authenticate(username,password);
      MailArchivaPrincipal cgp = null;
      try {
          cgp = (MailArchivaPrincipal)ar.authActiveDirectory(config,username,password);
          if (cgp!=null) {
              String roleName = cgp.getRole();
              return "Authentication success. Role "+roleName+" is assigned.";
          } else
              return "Authentication failed.";
      } catch (ArchivaException ae) {
          return "Authentication failed. "+ae.getMessage()+".";
      }
  } 
}

