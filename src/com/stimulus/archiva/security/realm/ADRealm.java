
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
import java.security.*;
import java.util.*;

import javax.security.auth.callback.*;
import javax.security.auth.login.*;
import com.stimulus.archiva.service.*;
import javax.security.auth.Subject;
import org.apache.log4j.Logger;
import org.securityfilter.realm.SimpleSecurityRealmBase;
import javax.naming.directory.*;
import javax.naming.*;
import com.stimulus.archiva.domain.*;
import com.stimulus.archiva.exception.*;
import com.stimulus.archiva.authentication.*;
import com.stimulus.archiva.domain.Identity;


public class ADRealm extends SimpleSecurityRealmBase {

   private String exampleProperty;
   protected static final Logger logger = Logger.getLogger(ADRealm.class.getName());
   protected static final Logger audit = Logger.getLogger("com.stimulus.archiva.audit");
   protected static final String confName = "ADLogin";
   protected String lastLDAPError = "";

   public Principal authenticate(String username, String password) {
	   logger.debug("authenticate. {username='"+username+"'}");
	   Config.AuthMethod authMethod = ConfigurationService.getConfig().getAuthMethod();
       try {
	       if (authMethod == Config.AuthMethod.BASIC) {
	           logger.debug("authenticate: basic authentication enabled");
	           return authBasic(ConfigurationService.getConfig(),username,password);
	       } else if (authMethod == Config.AuthMethod.ACTIVEDIRECTORY) {
	    	   logger.debug("authenticate: active directory authentication enabled");
	    	   return authActiveDirectory(ConfigurationService.getConfig(),username,password);
	       }   
       } catch (ArchivaException ae) {
           logger.warn("failed login attempt. "+ae.getMessage()+" {username='"+username+"'}");
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
        	 logger.debug("authenticated user is assigned a role {uname='"+username+"',role='"+roleStr+"'}");
        	 List<String> emailAddresses = new ArrayList<String>();
        	 emailAddresses.add(username);
        	 return new MailArchivaPrincipal(username,config.getADIdentity().getRoleFromID(role),emailAddresses);
        }
   }

   public Principal authActiveDirectory(Config config, String username, String password) throws ArchivaException {
       logger.debug("authenticating user to web console {username='"+username+"'}");
       // if there are no role maps defined default to ADMIN access
       if (config.getADIdentity().getRoleMaps().size()==0) {
    	   logger.info("SECURITY WARNING!! there are no role mappings defined for AD authentication. anonymous user is granted admin rights.");
    	   return new MailArchivaPrincipal(username,"administrator",null);
       }
       ArrayList<AttributeValue> attributeValues = getLDAPAttributes(config, username, password);
       int userRole = getRole(config,attributeValues);
       if (userRole==0) 
          throw new ArchivaException("failed to authenticate user as no role could be assigned {username='"+username+"'}",logger);  	
       else {
    	   List<String> emailAddresses = getADEmailAddresses(config,attributeValues);
    	   return new MailArchivaPrincipal(username,config.getADIdentity().getRoleFromID(userRole),emailAddresses);
       }
   }
 
   public List<String> getADEmailAddresses(Config config, ArrayList<AttributeValue> attributeValues) {
	   List<String> emailAddresses = new ArrayList<String>();
	   Map<String,String> emailMappings = config.getADIdentity().getEmailMappings();
	   for (String emailMappingKey: emailMappings.keySet()) {
		   logger.debug("getADEmailAddresses(): analyzing email mapping entry {key='"+emailMappingKey+"'");   
		   for (AttributeValue attributeValue: attributeValues) {
			   if (attributeValue.getAttribute().equalsIgnoreCase(emailMappingKey)) {
				   String patternStr = emailMappings.get(emailMappingKey);
				    Pattern pattern = Pattern.compile(patternStr.toLowerCase());
				    String attrValue = attributeValue.getValue().toLowerCase();
				    logger.debug(attrValue);
				    Matcher matcher = pattern.matcher(attrValue);
				    if (matcher.matches()) {
				    	String username = matcher.group(1);
				    	String domain = matcher.group(2);
				    	logger.debug("getADEmailAddresses(): found matching email address {email='"+username+"@"+domain+"'}");
				    	emailAddresses.add(username+"@"+domain);
				    }
			   }		
		   }
		}
	    return emailAddresses;
	}
	  
   
   public ArrayList<AttributeValue> getLDAPAttributes(Config config, String username, String password) throws ArchivaException {
       String domain 			= null;
       String uname 			= null;
       LoginContext serverLC 	= null;
       BeanCallbackHandler beanCallbackHandler = null;
       String kdcAddress        =  config.getKDCAddress();
       int at = username.lastIndexOf('@');
       if (at<=0 || at>=username.length()-1) {
           throw new ArchivaException("invalid login name. must be username@domain",logger);
       }
       uname = username.substring(0,at).toLowerCase();
       domain = username.substring(at+1).toUpperCase();
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
   	       return (ArrayList<AttributeValue>)Subject.doAs(serverLC.getSubject(), new LDAPAction(config,uname, domain));
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
       return (rolename.compareToIgnoreCase(userRole)==0);
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

   private static String convertDomainToDN(String domain) {

	    String[] result = domain.split("\\.");

	    if (result == null || result.length<1)
	        return domain;

	    String dn = "";
	    for (int i=0;i<result.length;i++)
	        dn += "DC="+result[i]+",";
	    if (dn.lastIndexOf(',')!=-1)
	        dn = dn.substring(0,dn.length()-1);

	    return dn;
	}


   public int getRole(Config config,ArrayList<AttributeValue> attributeValues) throws ArchivaException {
       int role =0;
       for (Identity.RoleMap rm: config.getADIdentity().getRoleMaps()) {
    	   ADIdentity.ADRoleMap adrm = (ADIdentity.ADRoleMap)rm;
    	   for (AttributeValue attributeValue: attributeValues) {
    		   if (attributeValue.getAttribute().equalsIgnoreCase(adrm.getAttribute())) {
    			   String value = attributeValue.getValue().toLowerCase();
    			   if (value.matches(adrm.getRegEx().toLowerCase())) {
                       logger.debug("found matching user role for authenticated user {attribute='"+adrm.getAttribute()+"', value='"+value+"', regex='"+adrm.getRegEx()+"'}");
                       if (role < rm.getRoleID()) {
                           role = rm.getRoleID();
                       }
        		   }
    			   
    		   }
    	   }   
       }   
       String roleStr = config.getADIdentity().getRoleFromID(role);
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
	 
	
   static class LDAPAction implements java.security.PrivilegedAction {
	private String uname;
	private String domain;
	private Config config;

	public LDAPAction(Config config, String uname, String domain) {
	    this.config = config;
	    this.uname = uname;
	    this.domain = domain;
	}

	public Object run()  {
	   	return getLDAPAttributeValuePairs(config, uname, domain);
	}



	// returns users role if successful, otherwise, null

    private ArrayList<AttributeValue> getLDAPAttributeValuePairs(Config config, String uname, String domain)  {

        	String filter = "(&(sAMAccountName=" + uname + ")(objectClass=user))";
        	ArrayList<AttributeValue> attributeValues = new ArrayList<AttributeValue>();
        	
        	Hashtable<String,String> env = new Hashtable<String,String>(11);
    		String ldapAddress =  config.getLDAPAddress();
    	    if (!ldapAddress.toLowerCase().startsWith("ldap://"))
    	        ldapAddress = "ldap://" + ldapAddress;
    		logger.debug("retrieving attributes from LDAP using Kereberos token {ldapAddress='"+ldapAddress+"', domain='"+convertDomainToDN(domain)+"', filter='"+filter+"'");

    	    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    	   	// Must use fully qualified hostname
    	   	env.put(Context.PROVIDER_URL, ldapAddress);
    	   	// Request the use of the "GSSAPI" SASL mechanism
    	   	// Authenticate by using already established Kerberos credentials
    	   	env.put(Context.SECURITY_AUTHENTICATION, "GSSAPI");

    	   	try {

	    	   	 /* Create initial context */
	    	   	 DirContext ctx = new InitialDirContext(env);
	    		 //Create the search controls
	    	   	  /* specify search constraints to search subtree */
	    	   	 SearchControls constraints = new SearchControls();
	    	   	 constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
	    	   	String[] attributearraytype = new String[ADIdentity.ATTRIBUTES.size()];
	    	   	 //constraints.setReturningAttributes((String[])ADIdentity.ATTRIBUTES.toArray(attributearraytype));
	    	   	 // look for user with sAMAccountName set to the username
	    	 
	    		NamingEnumeration results = null;
	    		
	    	   	logger.debug("search for ldap attributes {domain='"+convertDomainToDN(domain)+"',filter='"+filter+"'}");
	    	   	//NamingEnumeration results2 = null;
	    	   	try {
	    	   		results =ctx.search(convertDomainToDN(domain),filter, constraints);
	    	   	} catch (javax.naming.PartialResultException pre) {}
	    	  
	    	   	 while (results != null && results.hasMore()) {

	                    SearchResult si = (SearchResult)results.next();
	                    /* print its name */
	                    logger.debug("retrieving LDAP attributes {name='"+si.getName()+"'}");

	                    Attributes attrs = si.getAttributes();
	                    if (attrs == null) {
	                        logger.debug("no attributes found");
	                    } else {
	                        /* print each attribute */
	                        for (NamingEnumeration ae = attrs.getAll(); ae.hasMoreElements();) {
	                            Attribute attr = (Attribute)ae.next();
	                            String attrId = attr.getID();
	                            /* print each value */
	                            for (Enumeration vals = attr.getAll();vals.hasMoreElements();) {
	                                String value = (String)vals.nextElement().toString();
	                                logger.debug("LDAP attribute: "+ attrId + " = " + value);
	                                attributeValues.add(new AttributeValue(attrId,value));
	                            }

	                        }
	                    }
	    	   	 }
	    	   	ctx.close();
    	   	} catch (javax.naming.PartialResultException pre) {
    	   	} catch (Exception e) {
    	   	   logger.error("error occured while retrieving LDAP attributes during user login. Cause:",e);
    	   	}
			return attributeValues;
     }
   }
  
}
