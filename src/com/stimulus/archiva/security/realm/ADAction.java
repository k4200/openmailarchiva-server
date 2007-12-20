/*
 * Subversion Infos:
 * $URL$
 * $Author$
 * $Date$
 * $Rev$
*/

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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.log4j.Logger;

import com.stimulus.archiva.authentication.ADIdentity;
import com.stimulus.archiva.authentication.LDAPIdentity;
import com.stimulus.archiva.security.realm.ADRealm.AttributeValue;


class ADAction implements java.security.PrivilegedAction {
	
	private String uname;
	private String domain;
	private LDAPIdentity identity;
	protected static Logger logger = Logger.getLogger(ADRealm.class.getName());
	protected static final Logger audit = Logger.getLogger("com.stimulus.archiva.audit");
	
	public ADAction(LDAPIdentity identity, String uname, String domain) {
	    this.identity = identity;
	    this.uname = uname;
	    this.domain = domain;
	}

	public Object run()  {
	   	return getADAttributeValuePairs();
	}


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

	// Active Directory Authentication
	 
	 private ArrayList<AttributeValue> getADAttributeValuePairs()  {
	
	     	String filter = "(&(sAMAccountName=" + uname + ")(objectClass=user))";
	     	Hashtable<String,String> env = new Hashtable<String,String>(11);
	 		String ldapAddress =  identity.getLDAPAddress();
	 	    if (!ldapAddress.toLowerCase(Locale.ENGLISH).startsWith("ldap://"))
	 	        ldapAddress = "ldap://" + ldapAddress;
	 		logger.debug("retrieving attributes from LDAP using Kereberos token {ldapAddress='"+ldapAddress+"', domain='"+convertDomainToDN(domain)+"', filter='"+filter+"'");
	 	    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
	 	   	env.put(Context.PROVIDER_URL, ldapAddress);
	 	   	env.put(Context.SECURITY_AUTHENTICATION, "GSSAPI");
	 	   	return getLDAPAttributes(env,filter);
	 }
 
	  private ArrayList<AttributeValue> getLDAPAttributes(Hashtable<String,String> env, String filter) {
		  		String ldapAddress =  identity.getLDAPAddress();
	    		if (!ldapAddress.toLowerCase(Locale.ENGLISH).startsWith("ldap://"))
	    			ldapAddress = "ldap://" + ldapAddress;
		  			ArrayList<AttributeValue> attributeValues = new ArrayList<AttributeValue>();   	
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