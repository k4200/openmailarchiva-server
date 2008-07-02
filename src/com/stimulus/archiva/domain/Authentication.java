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
package com.stimulus.archiva.domain;

import java.util.List;
import java.util.Locale;
import org.apache.log4j.Logger;

public class Authentication implements Props {

	
    protected static final String defaultAuthMethod = "basic";

    protected AuthMethod	authMethod = AuthMethod.BASIC;
	public enum AuthMethod { BASIC, ACTIVEDIRECTORY};
    protected String defaultLoginDomain = "";
	protected static final String authMethodKey = "security.loginmethod";
	protected static final String defaultLoginDomainKey = "security.login.defaultdomain";
	
	protected static final Logger logger = Logger.getLogger(Authentication.class);
	
	  
	public void setDefaultLoginDomain(String domain) {
		defaultLoginDomain = domain;
	}
	
	public String getDefaultLoginDomain() {
		return defaultLoginDomain;
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
   	  		if (domains!=null & domains.size()>0) 
   	  			defaultLoginDomain = domains.get(0).getName();
   	  		else defaultLoginDomain = "";
   	  	} 
   	  	return true;
	}
	
	
    
}
