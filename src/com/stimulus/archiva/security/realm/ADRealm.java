
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

package com.stimulus.archiva.security.realm;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;

import org.apache.log4j.Logger;
import org.securityfilter.realm.SimpleSecurityRealmBase;

import com.stimulus.archiva.authentication.ADIdentity;
import com.stimulus.archiva.authentication.BasicIdentity;
import com.stimulus.archiva.authentication.LDAPIdentity;
import com.stimulus.archiva.domain.Config;
import com.stimulus.archiva.domain.Identity;
import com.stimulus.archiva.exception.ArchivaException;
import com.stimulus.archiva.service.ConfigurationService;
import com.stimulus.util.Compare;

public class ADRealm extends SimpleSecurityRealmBase implements Serializable {

   /**
	 * 
	 */
	private static final long serialVersionUID = 1735467638548884618L;
	private String exampleProperty;
	protected static Logger logger = Logger.getLogger(ADRealm.class.getName());
	protected static final Logger audit = Logger.getLogger("com.stimulus.archiva.audit");
	protected static final String confName = "ADLogin";
	protected String lastLDAPError = "";

   public Principal authenticate(String username, String password) {
	   logger.debug("authenticate. {username='"+username+"'}");
	   Config config = ConfigurationService.getConfig();
	   try {
		   return authenticate(config, username, password);
	   }  catch (ArchivaException ae) {
           logger.warn("failed login attempt. "+ae.getMessage()+" {username='"+username+"'}");
       }
	   return null;
   }
   
   public Principal authenticate(Config config, String username, String password) throws ArchivaException {
	   Config.AuthMethod authMethod = config.getAuthMethod();
       if (authMethod == Config.AuthMethod.BASIC) {
           logger.debug("authenticate: basic authentication enabled");
           return authBasic(config,username,password);
       } else if (authMethod == Config.AuthMethod.ACTIVEDIRECTORY) {
    	   logger.debug("authenticate: active directory authentication enabled");
    	   return authActiveDirectory(config,username,password);
      }
       return null;
   }
   
   public Principal authBasic(Config config, String username, String password) throws ArchivaException {
	
       // if there are no role maps defined default to ADMIN access
       if (config.getBasicIdentity().getRoleMaps().size()==0) {
    	   logger.info("SECURITY WARNING!! there are no role mappings defined for basic authentication. anonymous user is granted admin rights.");
    	   return new MailArchivaPrincipal(username,"administrator",null);
       }
       
	   int at = username.lastIndexOf('@');
       if (at<=0 || at>=username.length()-1) {
           throw new ArchivaException("invalid login name. must be username@domain",logger);
       }
       
       Iterator roleMapIterator = config.getBasicIdentity().getRoleMaps().iterator();
       int role = 0;
       while (roleMapIterator.hasNext()) {
         BasicIdentity.BasicRoleMap rm = (BasicIdentity.BasicRoleMap)roleMapIterator.next();
         logger.debug("find role {username='"+rm.getUsername()+"'}");
         logger.debug("username:'"+username+"'}");      
         if (rm.getUsername().equals(username) && rm.getLoginPassword().equals(password)) {
        	 			logger.debug("found matching user role for authenticated user {uname='"+username+"'}");
                   		if (role<=rm.getRoleID())
                   			role = rm.getRoleID();
        }
       }
     
       String roleStr = config.getADIdentity().getRoleFromID(role);
       
       if (role==0) 
           throw new ArchivaException("failed to authenticate user as no role could be assigned {username='"+username+"',role='"+roleStr+"'}",logger);  	
        else {
        	 logger.debug("auth user is assigned a role {uname='"+username+"',role='"+roleStr+"'}");
        	 List<String> emailAddresses = new ArrayList<String>();
        	 emailAddresses.add(username);
        	 return new MailArchivaPrincipal(username,config.getADIdentity().getRoleFromID(role),emailAddresses,null);
        }
   }

   public Principal authActiveDirectory(Config config, String username, String password) throws ArchivaException {
       logger.debug("authenticating user to web console using active directory {username='"+username+"'}");
       // if there are no role maps defined default to ADMIN access
       ADIdentity identity = config.getADIdentity();
       if (identity.getRoleMaps().size()==0) {
    	   logger.info("SECURITY WARNING!! there are no role mappings defined for AD authentication. anonymous user is granted admin rights.");
    	   return new MailArchivaPrincipal(username,"administrator",null,null);
       }
       ArrayList<AttributeValue> attributeValues = getADAttributes(identity, username, password);
       int userRole = getRole(identity,attributeValues);
       if (userRole==0) 
          throw new ArchivaException("failed to authenticate user as no role could be assigned {username='"+username+"'}",logger);  	
       else {
    	   List<String> emailAddresses = getEmailAddresses(identity,attributeValues);
    	   return new MailArchivaPrincipal(username,identity.getRoleFromID(userRole),emailAddresses);
       }
   }
   
 
   public List<String> getEmailAddresses(LDAPIdentity identity, ArrayList<AttributeValue> attributeValues) {
	   List<String> emailAddresses = new ArrayList<String>();
	   Map<String,String> emailMappings = identity.getEmailMappings();
	   for (String emailMappingKey: emailMappings.keySet()) {
		   logger.debug("getEmailAddresses(): analyzing email mapping entry {key='"+emailMappingKey+"'}");   
		   for (AttributeValue attributeValue: attributeValues) {
			   if (Compare.equalsIgnoreCase(attributeValue.getAttribute(), emailMappingKey)) {
				   String patternStr = emailMappings.get(emailMappingKey);
				    Pattern pattern = Pattern.compile(patternStr.toLowerCase(Locale.ENGLISH));
				    String attrValue = attributeValue.getValue().toLowerCase(Locale.ENGLISH);
				    logger.debug(attrValue);
				    Matcher matcher = pattern.matcher(attrValue);
				    if (matcher.matches()) {
				    	String username = matcher.group(1);
				    	String domain = matcher.group(2);
				    	logger.debug("getEmailAddresses(): found matching email address {email='"+username+"@"+domain+"'}");
				    	emailAddresses.add(username+"@"+domain);
				    }
			   }		
		   }
		}
	    return emailAddresses;
	}
	  
 
   
   public ArrayList<AttributeValue> getADAttributes(ADIdentity identity, String username, String password) throws ArchivaException {
       String domain 			= null;
       String uname 			= null;
       LoginContext serverLC 	= null;
       BeanCallbackHandler beanCallbackHandler = null;
       String kdcAddress        =  identity.getKDCAddress();
       int at = username.lastIndexOf('@');
       if (at<=0 || at>=username.length()-1) {
           throw new ArchivaException("invalid login name. must be username@domain",logger);
       }
       uname = username.substring(0,at).toLowerCase(Locale.ENGLISH);
       domain = username.substring(at+1).toUpperCase(Locale.ENGLISH);
       String confFile = Config.getConfigurationPath()+ File.separatorChar + "login.conf";
       beanCallbackHandler = new BeanCallbackHandler(uname, password);
       System.setProperty("java.security.krb5.realm", domain);
       System.setProperty("java.security.krb5.kdc", kdcAddress);
       System.setProperty("java.security.auth.login.config", confFile);
       if (logger.isDebugEnabled())
    	   System.setProperty("sun.security.krb5.debug","true");
       System.setProperty("java.security.auth.login.config", confFile);
       try {
           serverLC = new LoginContext(confName, beanCallbackHandler);
           serverLC.login();   
       } catch (Exception e) {
           throw new ArchivaException("failed to login using kerberos server. "+e.getMessage()+" {realm='"+domain+"',kdcAddress='"+kdcAddress+"'}",e,logger);
       }
       try {
   	       return (ArrayList<AttributeValue>)Subject.doAs(serverLC.getSubject(), new ADAction(identity,uname, domain));
       } catch (Exception e) {
    	   throw new ArchivaException("failed to bind to ldap server {uname='"+uname+"',domain='"+domain+"'}",e,logger);
       }
   }
   
   /**
    * Test for role membership.
    *
    * Implement this method in a subclass to avoid dealing with Principal objects.
    *
    * @param username The name of the user
    * @param role name of a role to test for membership
    * @return true if the user is in the role, false otherwise
    */
   public boolean isUserInRole(Principal principal, String rolename) {
       if (principal==null)
    	   return false;
       MailArchivaPrincipal cp = (MailArchivaPrincipal)principal;
       String userRole = ((MailArchivaPrincipal)principal).getRole();
       logger.debug("isUserInRole {principalname='"+cp.getName()+"' principalrole='"+cp.getRole()+"' rolename='"+rolename+"'}");
       return Compare.equalsIgnoreCase(rolename, userRole);
   }

   public class BeanCallbackHandler implements CallbackHandler {

       // Store username and password.
       String name = null;
       String password = null;

       public BeanCallbackHandler(String name, String password)
       {
           this.name = name;
           this.password = password;
       }//BeanCallbackHandler


       public void handle (Callback[] callbacks) throws
           UnsupportedCallbackException, IOException
       {
           for(int i=0;i<callbacks.length;i++) {
               Callback callBack = callbacks[i];

               // Handles username callback.
               if (callBack instanceof NameCallback) {
                   NameCallback nameCallback = (NameCallback)callBack;
                   nameCallback.setName(name);

                // Handles password callback.
               } else if (callBack instanceof PasswordCallback) {
                 PasswordCallback passwordCallback = (PasswordCallback)callBack;
                 passwordCallback.setPassword(password.toCharArray());

             } else {
                 throw new UnsupportedCallbackException(callBack, "Call back not supported");
             }//else
         }//for

     }//handle

   }//BeanCallbackHandler

  

   public int getRole(Identity identity,ArrayList<AttributeValue> attributeValues) throws ArchivaException {
       int role =0;
       for (Identity.RoleMap rm: identity.getRoleMaps()) {
    	   LDAPIdentity.LDAPRoleMap adrm = (LDAPIdentity.LDAPRoleMap)rm;
    	   for (AttributeValue attributeValue: attributeValues) {
    		   if (Compare.equalsIgnoreCase(attributeValue.getAttribute(), adrm.getAttribute())) {
    			   String value = attributeValue.getValue().toLowerCase(Locale.ENGLISH);
    			   if (value.matches(adrm.getRegEx().toLowerCase(Locale.ENGLISH))) {
                       logger.debug("found matching user role for authenticated user {attribute='"+adrm.getAttribute()+"', value='"+value+"', regex='"+adrm.getRegEx()+"'}");
                       if (role < rm.getRoleID()) {
                           role = rm.getRoleID();
                       }
        		   }
    			   
    		   }
    	   }   
       }   
       String roleStr = identity.getRoleFromID(role);
       if (role==0)
           logger.debug("failed to assign a user role for authenticated user. will assume default role");
       else
           logger.debug("authenticated user is assigned a role {role='"+roleStr+"'");

       return role;
   }
   
   public static class AttributeValue {
	   
	   private String attribute;
	   private String value;
	   
	   public AttributeValue(String attribute, String value) {
		   this.attribute = attribute.trim();
		   this.value = value.trim();
	   }
	   
	   public String getValue() { return this.value; }
	   public String getAttribute() { return this.attribute; }
   }
	 
	
	
}
