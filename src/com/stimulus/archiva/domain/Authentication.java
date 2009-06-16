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
package com.stimulus.archiva.domain;

import java.util.List;
import java.util.Locale;
import org.apache.commons.logging.*;

import com.stimulus.archiva.exception.MessageStoreException;
import com.stimulus.util.Compare;
import com.stimulus.util.ConfigUtil;
import com.stimulus.util.Crypto;

public class Authentication implements Props {


    protected static final String defaultAuthMethod = "basic";

    protected AuthMethod	authMethod = AuthMethod.BASIC;
	public enum AuthMethod { BASIC, ACTIVEDIRECTORY};
    protected String defaultLoginDomain = "";
	protected static final String authMethodKey = "security.loginmethod";
	protected static final String defaultLoginDomainKey = "security.login.defaultdomain";
	protected static final String masterLoginPasswordKey = "security.login.master.password";
	protected static final String masterLoginUsernameKey = "security.login.master.username";
	protected static final String defaultMasterLoginPassword = "admin";
	protected static final String defaultMasterLoginUsername = "admin";
	protected String masterLoginPassword = defaultMasterLoginPassword;
	protected String masterLoginUsername = defaultMasterLoginUsername;
	protected boolean legacyMasterPassword = false;
	protected static final Log logger = LogFactory.getLog(Authentication.class);


	public boolean isLegacyMasterPassword() {
		return legacyMasterPassword;
	}

	public void setDefaultLoginDomain(String domain) {
		defaultLoginDomain = domain;
	}

	public String getDefaultLoginDomain() {
		return defaultLoginDomain;
	}

	public void setMasterLoginPassword(String masterLoginPassword) {
		this.masterLoginPassword = masterLoginPassword;
	}

	public String getMasterLoginPassword() {
		return masterLoginPassword;
	}

	public void setMasterLoginUsername(String masterLoginUsername) {
		this.masterLoginUsername = masterLoginUsername;
	}

	public String getMasterLoginUsername() {
		return masterLoginUsername;
	}

	public boolean isDefaultMasterLoginModified() {
		  if (masterLoginPassword==null)
			  return false;
		  boolean modified = !Compare.equalsIgnoreCase(masterLoginPassword,defaultMasterLoginPassword);
		  logger.debug("default password {modified='"+modified+"'}");
		  return modified;
	}

    public void setAuthMethod(AuthMethod authMethod) {
  	  this.authMethod = authMethod;
    }

    public void setAuthMethod(String loginMethod) {
  		  	AuthMethod newAuthMethod = AuthMethod.BASIC;
  		  	try {
  		  		newAuthMethod = AuthMethod.valueOf(loginMethod.trim().toUpperCase(Locale.ENGLISH));
  		  	} catch (IllegalArgumentException iae) {
  		    		logger.error("failed to set login method. login method is set to an illegal value {loginMethod='"+loginMethod+"'}");
  		    		logger.info("defaulting to basic login method");
  			}
  		  	setAuthMethod(newAuthMethod);
    }

    public AuthMethod getAuthMethod() {
    	  return authMethod;
    }
	public void saveSettings(String prefix, Settings prop, String suffix) {
		logger.debug("saving authentication settings");
		prop.setProperty(authMethodKey,authMethod.toString().toLowerCase(Locale.ENGLISH));
		prop.setProperty(defaultLoginDomainKey,defaultLoginDomain.toLowerCase(Locale.ENGLISH));

	    if (getMasterLoginPassword()!=null) {
	        try {
	        		prop.setProperty(masterLoginPasswordKey,Crypto.encryptPassword(getMasterLoginPassword()));
        	} catch (MessageStoreException mse) {
        		logger.error("failed to save active directory service account pass phrase",mse);
        	}

        }
	    if (getMasterLoginUsername()!=null) {
	    	prop.setProperty(masterLoginUsernameKey,getMasterLoginUsername());
	    }
	}

	public boolean loadSettings(String prefix, Settings prop, String suffix) {
		logger.debug("loading authentication settings");
		   String newAuthMethod = "basic";
       	try {
   	  		 newAuthMethod = prop.getProperty(authMethodKey);
   	  		 if (newAuthMethod==null) {
   	  			logger.info("config load: auth method was not found in server.conf. defaulting to basic.");
   	  			authMethod = AuthMethod.BASIC;
   	  		 } else
   	  			 authMethod = AuthMethod.valueOf(newAuthMethod.trim().toUpperCase(Locale.ENGLISH));
   	  	} catch (IllegalArgumentException iae) {
   	    		logger.error("failed to set auth method field. auth method is set to an illegal value {field='"+newAuthMethod+"'}");
   	    		logger.info("auth method field is set to 'to' by default (error recovery)");
   	  	}
   	  	defaultLoginDomain = prop.getProperty(defaultLoginDomainKey);
   	  	if (defaultLoginDomain==null) {
   	  		List<Domains.Domain> domains = Config.getConfig().getDomains().getDomains();
   	  		if (domains!=null && domains.size()>0)
   	  			defaultLoginDomain = domains.get(0).getName();
   	  		else defaultLoginDomain = "";
   	  	}

   	  	String encPassword = prop.getProperty(masterLoginPasswordKey);
   	  	if (encPassword==null && newAuthMethod!=null) {
   	  		legacyMasterPassword = true;
   	  	} else {
   	  		legacyMasterPassword = false;
   	  	}

	   	String encryptedPassword = ConfigUtil.getString(prop.getProperty(masterLoginPasswordKey),defaultMasterLoginPassword);
	 	if (!encryptedPassword.endsWith("=")) {
	 		setMasterLoginPassword(encryptedPassword);
	     } else {
	     	try {
	     		setMasterLoginPassword(Crypto.decryptPassword(encryptedPassword));
	     	} catch (MessageStoreException mse) {
	     		logger.error("failed to load active directory service account pass phrase",mse);
	     	}
	     }
	 	 String username = ConfigUtil.getString(prop.getProperty(masterLoginUsernameKey),defaultMasterLoginUsername);
	 	 setMasterLoginUsername(username);
	 	 return true;
    }




}
