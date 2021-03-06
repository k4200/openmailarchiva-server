package com.stimulus.archiva.security.realm;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import org.apache.commons.logging.*;
import com.stimulus.archiva.authentication.LDAPIdentity;
import com.stimulus.archiva.exception.*;
import com.stimulus.archiva.security.realm.ADRealm.AttributeValue;

public class GetAttributesAction implements java.security.PrivilegedAction  {

	protected LDAPIdentity identity;
	protected String loginName;
	protected static final Log logger = LogFactory.getLog(GetAttributesAction.class.getName());
	protected static final Log audit = LogFactory.getLog("com.stimulus.archiva.audit");
	protected Hashtable<String,String> env;
	protected String bindDN;
	
	public GetAttributesAction(LDAPIdentity identity, String loginName, Hashtable<String,String> env, String bindDN) throws ArchivaException {
	    this.identity = identity;
	    this.loginName = loginName;
	    this.env = env;
	    this.bindDN = bindDN;
	}
	
	public Object run()  {
		try {
			return getLDAPAttributes();
		} catch (ArchivaException e) {
			logger.error("failed to retrieve user attributes:"+e.getMessage());
			ArrayList<AttributeValue> av = new ArrayList<AttributeValue>();
			return av;
		}
	}
	
	 public ArrayList<AttributeValue> getLDAPAttributes() throws ArchivaException {
	  		
			ArrayList<AttributeValue> attributeValues = new ArrayList<AttributeValue>();   	
			try {
				   		DirContext ctx = new InitialDirContext(env);
			    	   	logger.debug("search for ldap attributes {principal='"+bindDN+"'}");
			    	   	Attributes attrs = ctx.getAttributes(bindDN);
			            if (attrs == null) {
			                logger.debug("no attributes found");
			            } else {
			                /* print each attribute */
			                for (NamingEnumeration ae = attrs.getAll(); ae.hasMoreElements();) {
			                    Attribute attr = (Attribute)ae.next();
			                    String attrId = attr.getID();
			                    /* print each value */
			                    for (Enumeration vals = attr.getAll();vals.hasMoreElements();) {
			                        String value = vals.nextElement().toString();
			                        logger.debug("LDAP attribute: "+ attrId + " = " + value);
			                        attributeValues.add(new AttributeValue(attrId,value));
			                    }
			                }
			            }
			            ctx.close();
			} catch (javax.naming.PartialResultException pre) {
			} catch (javax.naming.AuthenticationException ae) {
				throw new ArchivaException("failed to authenticate the user. verify that the user's login name and password is correct.",logger,ChainedException.Level.WARN);
			} catch (Exception e) {
				throw new ArchivaException("failed to bind to user:"+e.getMessage(),logger,ChainedException.Level.DEBUG);
			}
			return attributeValues;
   }

 
}
