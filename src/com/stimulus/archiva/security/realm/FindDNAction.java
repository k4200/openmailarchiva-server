package com.stimulus.archiva.security.realm;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Locale;

import javax.naming.NamingEnumeration;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.stimulus.archiva.authentication.LDAPIdentity;
import com.stimulus.archiva.exception.ArchivaException;
import com.stimulus.archiva.exception.ChainedException;
import com.stimulus.archiva.security.realm.ADRealm.AttributeValue;

public class FindDNAction implements java.security.PrivilegedAction {
	
	protected static final Log logger = LogFactory.getLog(FindDNAction.class.getName());
	protected String loginName;
	protected LDAPIdentity identity;
	protected Hashtable<String,String> env;
	
	public FindDNAction(LDAPIdentity identity, String loginName, Hashtable<String,String> env) throws ArchivaException {
	    this.identity = identity;
	    this.loginName = loginName;
	    this.env = env;
	}
	

	// LDAP Authentication
	public Object run()  {
		try {
			return findUserDN();
		} catch (ArchivaException e) {
			logger.error("failed to retrieve user DN:"+e.getMessage(),e);
			return null;
		}
	}
	
	 public String findUserDN() throws ArchivaException {  	
			try {
						logger.debug("findUserDN() {'"+identity.toString()+"'}");
						DirContext ctx = new InitialDirContext(env);
			    	   	SearchControls sc = new SearchControls(SearchControls.SUBTREE_SCOPE,0,5000,null,false,false);
			            NamingEnumeration results = ctx.search(identity.getBaseDN(),"("+identity.getBindAttribute()+"="+loginName+")",sc);
			            String userDN = null;
			            while (results.hasMore()) {
			                SearchResult result = (SearchResult)results.next();
			                userDN = result.getNameInNamespace();
			                break;
			            }
			            ctx.close();
			            if (userDN==null) {
			            	throw new ArchivaException("the user "+loginName+" does exist in the LDAP repository. is the login name or bind attribute correct?",logger,ChainedException.Level.WARN);
			            }
			            logger.debug("findUserDN() return {userdn='"+userDN+"'}");
			            return userDN;
			           
			} catch (javax.naming.AuthenticationException ae) {
				throw new ArchivaException("failed to authenticate with LDAP service account. verify that the service DN and password is correct.",logger,ChainedException.Level.WARN);
			} catch (javax.naming.CommunicationException ce) {
				throw new ArchivaException("failed to connect to ldap server. verify that the server address and port is correct.",logger,ChainedException.Level.WARN);
			} catch (javax.naming.NameNotFoundException nnfe ) {
				throw new ArchivaException("could not lookup user in LDAP as the Base DN does not appear to be correct",logger,ChainedException.Level.WARN);
			} catch (javax.naming.PartialResultException pre) {
	 		} catch (ArchivaException e) {
	 			throw e;
			} catch (Exception e) {
				throw new ArchivaException("failed to lookup user using LDAP service account:"+e.getMessage(),logger,ChainedException.Level.DEBUG);
			}
			return null;
	 }
	
}
