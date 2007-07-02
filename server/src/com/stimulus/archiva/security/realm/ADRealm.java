/* Copyright (C) 2005 Jamie Angus Band 
 * This software program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.stimulus.archiva.security.realm;

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
/**
 * Trivial implementation of the SecurityRealmInterface.
 *
 * There is one user: username is 'username', password is 'password'
 * And this user is in one role: 'inthisrole'
 *
 * @author Max Cooper (max@maxcooper.com)
 * @version $Revision$ $Date$
 */
public class ADRealm extends SimpleSecurityRealmBase {

   private String exampleProperty;
   protected static final Logger logger = Logger.getLogger(ADRealm.class.getName());
   protected static final Logger audit = Logger.getLogger("com.stimulus.archiva.audit");
   protected static final String confName = "ADLogin";

   public Principal authenticate(String username, String password) {
       boolean authenticate = ConfigurationService.getConfig().getConsoleAuthenticate();
       if (!authenticate) {
           logger.warn("!!!!!console authentication is disabled!!!!! this setting should only be disabled for testing purposes. ");
           return new CorpGovPrincipal(username,"Administrator");
       }
       try { 
           return auth(ConfigurationService.getConfig(),username,password);
       } catch (ArchivaException ae) {
           logger.warn("failed login attempt. "+ae.getMessage()+" {username='"+username+"'}");
           return null;
       }
   }
   
   public Principal auth(Config config, String username, String password) throws ArchivaException {
       logger.debug("authenticating user to web console {username='"+username+"'");
       List attributeValues = null;
       attributeValues = getLDAPAttributes(config, username, password);
       int userRole = 0;
       userRole = getRole(config,username,attributeValues);
       if (userRole==0) 
          throw new ArchivaException("failed to authenticate user as no role could be assigned {username='"+username+"'}",logger);  	
       else 
          return new CorpGovPrincipal(username,config.getRoleMaps().getRoleFromID(userRole));
   }
 
   public List getLDAPAttributes(Config config, String username, String password) throws ArchivaException {
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
       String ldapAttributes = "";
       try {
           serverLC = new LoginContext(confName, beanCallbackHandler);
           serverLC.login();
   	       return (List)Subject.doAs(serverLC.getSubject(), new LDAPAction(config,uname, domain));
      } catch (Exception e) {
          throw new ArchivaException(e.getMessage(),logger);
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
       boolean authenticate = ConfigurationService.getConfig().getConsoleAuthenticate();

       if (!authenticate && rolename.equalsIgnoreCase("Administrator")) {
           return true;
       }
       CorpGovPrincipal cp = (CorpGovPrincipal)principal;
       String userRole = ((CorpGovPrincipal)principal).getRole();
       logger.debug("isUserInRole {prinicipalname='"+cp.getName()+"' principalrole='"+cp.getRole()+"' rolename='"+rolename+"'}");
       return (rolename.compareToIgnoreCase(userRole)==0);
   }

   /**
    * Setter for exampleProperty to deomonstrate setting realm properties from config file.
    *
    * This has no effect other than printing a message when the property is set.
    *
    * @param value example property value
    */
   public void setExampleProperty(String value) {
      exampleProperty = value;
   }

   /**
    * Getter for exampleProperty.
    *
    * @return the value of exampleProperty
    */
   public String getExampleProperty() {
      return exampleProperty;
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

   public static class AttributeValue {
       String attribute;
       String value;
       public AttributeValue(String attribute, String value)
       {
           this.attribute = attribute;
           this.value = value;
       }
       public String getAttribute() { return attribute; }
       public String getValue() { return value; }

   }

   public int getRole(Config config,String username,List attributeValues) throws ArchivaException {
       int at = username.lastIndexOf('@');
       if (at<=0 || at>=username.length()-1) {
           throw new ArchivaException("invalid login name. must be username@domain",logger);
       }
       String uname = username.substring(0,at).toLowerCase();
       String domain = username.substring(at+1).toLowerCase();
       int role = 0;
       for (Iterator i = attributeValues.iterator(); i.hasNext();) {
           AttributeValue av = (AttributeValue)i.next();
           Iterator roleMapIterator = config.getRoleMaps().getRoleMaps().iterator();
           while (roleMapIterator.hasNext()) {
               RoleMaps.RoleMap rm = (RoleMaps.RoleMap)roleMapIterator.next();
               logger.debug("find role {domainU='"+domain+"',domainR='"+rm.getDomain()+"',attributeU='"+av.getAttribute()+"',attributeR='"+rm.getAttribute()+"',criterionU='"+av.getValue()+"',criterionR='"+rm.getRegEx()+"'");
               if (rm.getDomain().compareToIgnoreCase(domain)==0 &&
                   rm.getAttribute().compareToIgnoreCase(av.getAttribute())==0 &&
                   av.getValue().matches(rm.getRegEx())) {
                   logger.debug("found matching user role for authenticated user {uname='"+uname+"', domain='"+domain+"', role='"+rm.getRole()+"', attributename='"+av.getAttribute()+"', attributevalue='"+av.getValue()+"', regularexp='"+rm.getRegEx()+"'}");
                   if (role<=rm.getRoleID())
                       role = rm.getRoleID();
               }
           }
       }

       String roleStr = config.getRoleMaps().getRoleFromID(role);
       if (role==0)
           logger.debug("failed to assign a user role for authenticated user. will assume default role {uname='"+uname+"', domain='"+domain+"', defaultrole='"+roleStr+"'}");
       else
           logger.debug("authenticated user is assigned a role {uname='"+uname+"', domain='"+domain+"', role='"+roleStr+"'}");

       return role;
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

    private List getLDAPAttributeValuePairs(Config config, String uname, String domain)  {

        	String filter = "(&(sAMAccountName=" + uname + ")(objectClass=user))";
        	Vector attributeValues = new Vector();

        	Hashtable env = new Hashtable(11);
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
	    	   	String[] attributearraytype = new String[RoleMaps.ATTRIBUTES.size()];
	    	   	 constraints.setReturningAttributes((String[])RoleMaps.ATTRIBUTES.toArray(attributearraytype));
	    	   	 // look for user with sAMAccountName set to the username
	    	 
	    		NamingEnumeration results = null;
	    		
	    	   	logger.debug("**@@search for ldap attributes {domain='"+convertDomainToDN(domain)+"',filter='"+filter+"'}");
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
    	   	   logger.debug("error occured while retrieving LDAP attributes during user login. Cause:",e);
    	   	}
			return attributeValues;


     }
   }


}


// ----------------------------------------------------------------------------
// EOF
