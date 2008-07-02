
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

import java.util.regex.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.security.*;
import java.util.*;

import javax.security.auth.callback.*;
import javax.security.auth.login.*;
import javax.security.auth.Subject;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.securityfilter.realm.SimpleSecurityRealmBase;
import org.subethamail.smtp.util.Base64;

import com.stimulus.archiva.domain.*;
import com.stimulus.archiva.exception.*;
import com.stimulus.archiva.authentication.*;
import com.stimulus.archiva.domain.Identity;
import com.stimulus.util.*;

public class ADRealm extends SimpleSecurityRealmBase implements Serializable {

  
	private static final long serialVersionUID = 1735467638548884618L;
	protected static final Logger logger = Logger.getLogger(ADRealm.class.getName());
	protected static final Logger audit = Logger.getLogger("com.stimulus.archiva.audit");
	protected static final String confName = "ADLogin";
	protected String lastLDAPError = "";

   @Override
public Principal authenticate(String username, String password) {
	   logger.debug("authenticate. {username='"+username+"'}");
	   Config config = Config.getConfig();
	   try {
		   return authenticate(config, username, password);
	   }  catch (ArchivaException ae) {
           logger.warn("failed login attempt. "+ae.getMessage()+" {username='"+username+"'}");
       }
	   return null;
   }
   
   public Principal authenticate(Config config, String username, String password) throws ArchivaException {
	   Authentication auth = config.getAuthentication();
	   Authentication.AuthMethod authMethod = auth.getAuthMethod();
       if (authMethod == Authentication.AuthMethod.BASIC) {
           logger.debug("authenticate: basic authentication enabled");
           return authBasic(config,username,password);
       } else if (authMethod == Authentication.AuthMethod.ACTIVEDIRECTORY) {
    	   logger.debug("authenticate: active directory authentication enabled");
    	   return authActiveDirectory(config,username,password);
       } 
       return null;
   }
   
   public Principal authBasic(Config config, String username, String password) throws ArchivaException {
	   logger.debug("authenticating user to web console using basec authentication {username='"+username+"'}");
	   // if there are no role maps defined default to ADMIN access
       if (config.getBasicIdentity().getRoleMaps().size()==0) {
    	   logger.info("SECURITY WARNING!! there are no role mappings defined for basic authentication. anonymous user is granted admin rights.");
    	   return new MailArchivaPrincipal(username,Roles.ADMINISTRATOR_ROLE.getName());
       }
       username = getExpandedLoginName(config,username);
       validateLoginName(username);
       validatePassword(password);
       Roles.Role role = null;
       Roles roles = config.getRoles();
       String passwordDigest = null;
       try {
  		 MessageDigest sha = MessageDigest.getInstance("SHA-1");
  	     byte[] input = sha.digest(ByteUtil.mergeByteArrays(password.getBytes("UTF-8"),Config.getConfig().getSalt()));
  	     passwordDigest = Base64.encodeToString(input,false); 
  	   } catch (Exception e) {
       	 logger.error("failed to generate password digest during basic authentication:"+e.getMessage(),e);
	   }
		  
       for (Identity.RoleMap rolemap : config.getBasicIdentity().getRoleMaps()) {
	         BasicIdentity.BasicRoleMap rm = (BasicIdentity.BasicRoleMap)rolemap;
	         logger.debug("find role {username='"+rm.getUsername()+"'}");
	         logger.debug("username:'"+username+"'}");    
	        
		         if (rm.getUsername().equals(username)) {
		        	 if ((rm.getLoginPassword().endsWith("=") && rm.getLoginPassword().equals(passwordDigest)) ||
		        		 (!rm.getLoginPassword().endsWith("=") && rm.getLoginPassword().equals(password))) {
		        		 Roles.Role newRole = roles.getRole(rm.getRole());
	                     if (role==null) {
	                  	   role = newRole;
	                     } else if (roles.getRolePriority(newRole)<roles.getRolePriority(role)) {
	                  	   role = newRole;
	                     }
	                     logger.debug("found matching user role for authenticated user {uname='"+username+"',role='"+role+"'}");
		        	 }
		        }
	       
       }
     
       if (role==null) 
           throw new ArchivaException("failed to authenticate user as no role could be assigned {username='"+username+"'}",logger);  	
        else {
        	 logger.debug("auth user is assigned a role {uname='"+username+"',role='"+role.getName()+"'}");
        	 List<String> emailAddresses = new ArrayList<String>();
        	 emailAddresses.add(username);
        	 List<Criteria> criteria = role.getViewFilter().getCriteria();
        	 return new MailArchivaPrincipal(username,role.getName(),emailAddresses,null);
        }
   }

   public Principal authActiveDirectory(Config config, String username, String password) throws ArchivaException {
       logger.debug("authenticating user to web console using active directory {username='"+username+"'}");
       
       // if there are no role maps defined default to ADMIN access
       ADIdentity identity = config.getADIdentity();
       if (identity.getRoleMaps().size()==0) {
    	   logger.info("SECURITY WARNING!! there are no role mappings defined for AD authentication. anonymous user is granted admin rights.");
    	   return new MailArchivaPrincipal(username,Roles.ADMINISTRATOR_ROLE.getName());
       }
       username = getExpandedLoginName(config,username);
       validateLoginName(username);
       validatePassword(password);
       ArrayList<AttributeValue> attributeValues = getADAttributes(config,identity, username, password);
       Roles.Role userRole = getADRole(config,identity,attributeValues);
       if (userRole==null) 
          throw new ArchivaException("failed to authenticate user as no role could be assigned {username='"+username+"'}",logger);  	
       else {
    	   List<String> emailAddresses = getEmailAddresses(username, identity.getEmailAttribute(),identity.getEmailValue(),attributeValues);
    	   return new MailArchivaPrincipal(username,userRole.getName(),emailAddresses);
       }
   }
    
 
   public List<String> getEmailAddresses(String username, String emailAttribute, String emailValue, ArrayList<AttributeValue> attributeValues) {
	   List<String> emailAddresses = new ArrayList<String>();
	   
		   logger.debug("getEmailAddresses(): analyzing email attribute {attribute='"+emailAttribute+"'}");   
		   for (AttributeValue attributeValue: attributeValues) {
			   if (Compare.equalsIgnoreCase(attributeValue.getAttribute(), emailAttribute)) {
				   String patternStr = emailValue;
				    Pattern pattern = Pattern.compile(patternStr.toLowerCase(Locale.ENGLISH));
				    String attrValue = attributeValue.getValue().toLowerCase(Locale.ENGLISH);
				    //logger.debug(attrValue);
				    Matcher matcher = pattern.matcher(attrValue);
				    if (matcher.matches()) {
				    	String email = matcher.group(1);
				    	if (email!=null && email.length()>0) {
					    	if (email.indexOf('@')>-1) {
					    		logger.debug("getEmailAddresses() add matching email address {email='"+email+"'}");
						    	if (email!=null) {
						    		emailAddresses.add(email.toLowerCase());
						    	}
					    	} else {
					    		logger.debug("getEmailAddresses(): email address does not appear to have a domain. constructing..");
					    		List<Domains.Domain> domains = Config.getConfig().getDomains().getDomains();
					    		for (Domains.Domain domain : domains) {
					    			String newEmail = email.trim() + "@" + domain.getName();
					    			logger.debug("getEmailAddresses() add matching email address {email='"+newEmail+"'}");
					    			emailAddresses.add(newEmail.toLowerCase());
					    		}
					    	}
				    	}
				    	
				    }
			   }		
		}
	    return emailAddresses;
	}
	  

   public ArrayList<AttributeValue> getADAttributes(Config config, ADIdentity identity, String username, String password) throws ArchivaException {
       String domain 			= null;
       String uname 			= null;
       LoginContext serverLC 	= null;
       BeanCallbackHandler beanCallbackHandler = null;
       String kdcAddress        =  identity.getKDCAddress();
       username = getExpandedLoginName(config,username);
       validateLoginName(username);
       validatePassword(password);
       int at = username.lastIndexOf('@');
       uname = username.substring(0,at).toLowerCase(Locale.ENGLISH);
       domain = username.substring(at+1).toUpperCase(Locale.ENGLISH);
       String confFile = Config.getFileSystem().getConfigurationPath()+ File.separatorChar + "login.conf";
       String krbFile = Config.getFileSystem().getConfigurationPath()+ File.separatorChar + "krb5.conf";
       beanCallbackHandler = new BeanCallbackHandler(uname, password);
       System.setProperty("java.security.krb5.realm", domain);
       System.setProperty("java.security.krb5.kdc", kdcAddress);
       if (logger.isDebugEnabled())
    	   System.setProperty("sun.security.krb5.debug","true");
       System.setProperty("java.security.auth.login.config", confFile);
       System.setProperty("java.security.krb5.conf",krbFile);
       try {
           serverLC = new LoginContext(confName , beanCallbackHandler);
           serverLC.login();   
       } catch (Exception e) {
           throw new ArchivaException("failed to login using kerberos server. "+e.getMessage()+" {realm='"+domain+"',kdcAddress='"+kdcAddress+"'}",e,logger);
       }
       ArrayList<AttributeValue> attributes = null;
       try {
   	       attributes = (ArrayList<AttributeValue>)Subject.doAs(serverLC.getSubject(), new ADAction(identity,uname, domain));
       } catch (Exception e) {
    	   throw new ArchivaException("failed to bind to ldap server {uname='"+uname+"',domain='"+domain+"'}",e,logger);
       }
       try {
    	   serverLC.logout();   
       } catch (Exception e) {
    	   throw new ArchivaException("failed to logout from kerberos server. "+e.getMessage()+" {realm='"+domain+"',kdcAddress='"+kdcAddress+"'}",e,logger);
       }
       return attributes;
      
   }
   public String getExpandedLoginName(Config config, String username) {
	   Authentication auth = config.getAuthentication();
	   String defaultDomain = auth.getDefaultLoginDomain();
	   if (username!=null && username.length()>0 && username.indexOf("@")==-1 && defaultDomain!=null &
		   defaultDomain.length()>0) {
		   username += "@" + auth.getDefaultLoginDomain();
	   }
	   return username;
   }
   
   public void validatePassword(String password) throws ArchivaException {
	 
       if (password==null || password.length()<1) {
    	   throw new ArchivaException("invalid password. it cannot be empty.",logger,Level.DEBUG);
       }
       
   }
   public void validateLoginName(String username) throws ArchivaException {
	   int at = username.lastIndexOf('@');
	   if (at<=0 || at>=username.length()-1) {
	       throw new ArchivaException("invalid login name. must be username@domain",logger,Level.DEBUG);
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
   @Override
public boolean isUserInRole(Principal principal, String rolename) {
       if (principal==null)
    	   return false;
       
       MailArchivaPrincipal cp = (MailArchivaPrincipal)principal;
       String userRole = cp.getRole();
       Roles.Role role = Config.getConfig().getRoles().getRole(userRole);
       
       if (rolename.equals("configure") && role.getName().equalsIgnoreCase("administrator"))
    	   return true;
       
       if (rolename.equals("search"))
    	   return true;
       
       return false;

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

   public Roles.Role getADRole(Config config, ADIdentity identity,ArrayList<AttributeValue> attributeValues) throws ArchivaException {
	   logger.debug("getting active directory role");
	   Roles.Role role = null;
       Roles roles = config.getRoles();
       for (Identity.RoleMap rm: identity.getRoleMaps()) {
    	   ADIdentity.ADRoleMap adrm = (ADIdentity.ADRoleMap)rm;
    	   
    	   for (AttributeValue attributeValue: attributeValues) {
    		  if (Compare.equalsIgnoreCase(attributeValue.getAttribute(), adrm.getAttribute())) {
    			   String value = attributeValue.getValue().trim();
    			   String value2 = adrm.getRegEx().trim();
    			   if (value.toLowerCase(Locale.ENGLISH).matches(value2.toLowerCase(Locale.ENGLISH))) {
                       logger.debug("found matching user role for authenticated user {attribute='"+adrm.getAttribute()+"', value='"+value+"', regex='"+adrm.getRegEx()+"'}");
                       Roles.Role newRole = roles.getRole(rm.getRole());
                       if (role==null) {
                    	   role = newRole;
                       } else if (roles.getRolePriority(newRole)<roles.getRolePriority(role)) {
                    	   role = newRole;
                       }
        		   }
    			   
    		   }
    	   }   
       }   
       if (role==null)
           logger.debug("failed to assign a user role for authenticated user. will assume default role");
       else
           logger.debug("authenticated user is assigned a role {role='"+role.getName()+"'");

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
