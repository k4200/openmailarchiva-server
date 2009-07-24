package com.stimulus.archiva.authentication;

import java.io.Serializable;
import java.util.*;
import com.stimulus.util.*;
import com.stimulus.archiva.domain.*;
import com.stimulus.archiva.exception.ConfigurationException;
import com.stimulus.archiva.exception.MessageStoreException;

public class LDAPIdentity extends Identity implements Serializable, Props {

	protected static Map<String, String> defaultEmailMappings;
	private static final long serialVersionUID = 8672529238138755867L;
	
	protected static final String LDAPAddressKey 			= "ldap3.authentication.ldap.address";
	protected static final String baseDNKey  				= "ldap3.base.dn";
	protected static final String serviceDNKey  			= "ldap3.service.dn";
	protected static final String servicePasswordKey  		= "ldap3.service.password";
	protected static final String bindAttributeKey			= "ldap3.bind.attribute";
	protected static final String emailAttributeKey 		= "ldap3.emailaddress.attribute";
	protected static final String roleMapKey  				= "ldap.role.assignment.role";
	protected static final String roleDomainKey 			= "ldap.role.assignment.domain";
	protected static final String roleAttributeKey 			= "ldap.role.assignment.attribute";
	protected static final String roleMatchKey 				= "ldap.role.assignment.criterion";
	protected final String defaultLoginRole = Roles.ADMINISTRATOR_ROLE.getName();
	protected final String defaultLoginAttribute = ADIdentity.ATTRIBUTES.get(0);
	protected final String defaultLoginMatch = "";
	
	
    protected static final String defaultLdapAddress 		= "openldap.company.com:389";
    protected static final String defaultBaseDN  			= "";
    protected static final String defaultBindAttribute	  	= "uid";
    protected static final String defaultEmailAttribute		= "email";
    protected static final String defaultServicePassword    = "";
    protected static final String defaultServiceDN			= "";
   
	protected String baseDN;
	protected String serviceDN;
	protected String servicePassword;
	protected String ldapAddress;
	protected String bindAttribute;
	protected String uidValue;
	protected String emailAttribute;
	
	public LDAPIdentity() {
		super();	
	}
	
	
    public String getLDAPAddress() { return ldapAddress; }
   
    public void setLDAPAddress(String ldapAddress) {
        this.ldapAddress = ldapAddress.toLowerCase(Locale.ENGLISH);
        if (this.ldapAddress.indexOf(':')==-1)
            this.ldapAddress += ":389";
    }
    
  
	
	public String getBindAttribute() {
		return bindAttribute;
	}
	    
	public void setBindAttribute(String uidAttribute) {
		bindAttribute = uidAttribute;
	}
	
	
	public String getEmailAttribute() {
		return emailAttribute;
	}
	
	public String getEmailValue() {
	    	return "(.*)";
	}
	  
	public void setEmailAttribute(String emailAttribute) {
		this.emailAttribute = emailAttribute;
	}
	
	public String getBaseDN() { return baseDN; }
	
	public void setBaseDN(String bindDN) { this.baseDN = bindDN; }
	
	public String getServiceDN() { return serviceDN; }
	
	public void setServiceDN(String serviceDN) { this.serviceDN = serviceDN; }
	
	public String getServicePassword() { return servicePassword; }
	
	public void setServicePassword(String servicePassword) { this.servicePassword = servicePassword; }
	
	@Override
	public void newRoleMap() throws ConfigurationException {
		addRoleMap(new LDAPRoleMap(Roles.ADMINISTRATOR_ROLE.getName(), "", ""));
	}

	public void addRoleMap(String role, String attribute, String regex)
			throws ConfigurationException {
		addRoleMap(new LDAPRoleMap(role, attribute, regex));
	}
/*	protected String BindDN;
	protected String ldapAddress;
	protected String bindAttribute;
	protected String uidValue;
	protected String emailAttribute;
	*/
    public boolean loadSettings(String prefix, Settings prop, String suffix) {
	 	setLDAPAddress(ConfigUtil.getString(prop.getProperty(LDAPAddressKey),defaultLdapAddress));
	 	setBaseDN(ConfigUtil.getString(prop.getProperty(baseDNKey),defaultBaseDN));
	 	setServiceDN(ConfigUtil.getString(prop.getProperty(serviceDNKey),defaultServiceDN));
	  	setBindAttribute(ConfigUtil.getString(prop.getProperty(bindAttributeKey),defaultBindAttribute));
	  	setEmailAttribute(ConfigUtil.getString(prop.getProperty(emailAttributeKey),defaultEmailAttribute));
	  	String encryptedPassword = ConfigUtil.getString(prop.getProperty(servicePasswordKey),defaultServicePassword);
    	if (!encryptedPassword.endsWith("=")) {
    		setServicePassword(encryptedPassword);
        } else {
        	try {
        		setServicePassword(Crypto.decryptPassword(encryptedPassword));
        	} catch (MessageStoreException mse) {
        		logger.error("failed to load ldap service account pass phrase",mse);
        	}
        }
	  	clearAllRoleMaps();
	  	
	  	int c = 1;
	  	boolean load = false;
	  	do {
	  		RoleMap rm = new LDAPRoleMap();
	  		load = rm.loadSettings(null,prop,"."+c++);
	  		if (load) addRoleMap(rm);
	  	} while (load);

        return true;
    }
    
    
    public void saveSettings(String prefix, Settings prop, String suffix) {
    	logger.debug("saving ldap identity");
	  	prop.setProperty(LDAPAddressKey, getLDAPAddress());
	  	prop.setProperty(baseDNKey, getBaseDN());
	  	prop.setProperty(serviceDNKey, getServiceDN());
	  	prop.setProperty(bindAttributeKey, getBindAttribute());
	  	prop.setProperty(emailAttributeKey, getEmailAttribute());
		
	    if (getServicePassword()!=null) {
	        // if raw password is hash value, we know to see the passphrase
	        try {
	        		prop.setProperty(servicePasswordKey,Crypto.encryptPassword(getServicePassword()));
        	} catch (MessageStoreException mse) {
        		logger.error("failed to save ldap service account pass phrase",mse);
        	}
	       
        }
	  	int c = 1;
	  	for (RoleMap rolemap : getRoleMaps()) {
	  		rolemap.saveSettings(null,prop,"."+c++);
	  	}
	
    }
	public class LDAPRoleMap extends Identity.RoleMap implements Props, Serializable {

		protected String attribute;
		protected String regex;
		
		private static final long serialVersionUID = 1161047256198102210L;
		
		public LDAPRoleMap() { }
		
		public LDAPRoleMap(String role, String attribute, String regex)
				throws ConfigurationException {
			setRole(role);
			setAttribute(attribute);
			setRegEx(regex);
		}

		public void setAttribute(String attribute) {
			this.attribute = attribute;
		}

		public String getAttribute() {
			return attribute;
		}

		public String getRegEx() {
			return regex;
		}

		public void setRegEx(String regex) {
			this.regex = regex;
		}
		
		
		 public void saveSettings(String prefix, Settings prop, String suffix) {
			     prop.setProperty(roleMapKey + suffix,getRole());
				 prop.setProperty(roleAttributeKey + suffix,getAttribute());
				 prop.setProperty(roleMatchKey + suffix,getRegEx());
				 
		 }
		 
		 public boolean loadSettings(String prefix, Settings prop, String suffix) {
			 String lr = prop.getProperty(roleMapKey + suffix);
		  	     String la = prop.getProperty(roleAttributeKey + suffix);
		  	     String lm = prop.getProperty(roleMatchKey + suffix);
		
		  	     if (lr== null || la == null || lm == null)
		  	    	return false;
		  	    
		  	    try {
		  	    	setRoleValue(ConfigUtil.getString(lr,defaultLoginRole));
		  	    } catch (ConfigurationException ce) {
		  	    	return false;
		  	    }
		  	    
		  	    setAttribute(ConfigUtil.getString(la,defaultLoginAttribute));
		  	    setRegEx(ConfigUtil.getString(lm,defaultLoginMatch));
		  	    
		  	    return true;
		  	  
		 }
	
		 @Override
		public String toString() {
			 return "serviceDN='"+serviceDN+"',servicePassword='"+servicePassword+"',ldapAddress='"+ldapAddress+"',bindAttribute='"+bindAttribute+
			 		"',uidValue='"+uidValue+"',emailAttribute='"+emailAttribute+"'";
		 }


	}
}
