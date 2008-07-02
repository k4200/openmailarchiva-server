package com.stimulus.archiva.authentication;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import com.stimulus.archiva.domain.*;
import com.stimulus.archiva.exception.ConfigurationException;
import com.stimulus.util.*;

public class ADIdentity extends Identity implements Serializable {

	//proxyAddresses","SMTP:(.*)@(.*)");
	private static final long serialVersionUID = 2961646156098906034L;
	public static final List<String> ATTRIBUTES;
	public static final List<String> ATTRIBUTE_LABELS;
	
	static {

		
		List<String> AttributeList = new LinkedList<String>();
		AttributeList.add("memberOf");
		AttributeList.add("userPrincipalName");
		AttributeList.add("sAMAccountName");
		AttributeList.add("distinguishedName");
		ATTRIBUTES = Collections.unmodifiableList(AttributeList);

		List<String> AttributeLabelList = new LinkedList<String>();
		AttributeLabelList.add("role.attribute.memberof");
		AttributeLabelList.add("role.attribute.userprincipalname");
		AttributeLabelList.add("role.attribute.samAccountname");
		AttributeLabelList.add("role.attribute.distinguishedname");
		ATTRIBUTE_LABELS = Collections.unmodifiableList(AttributeLabelList);

	}
	
	protected static final String adKDCAddressKey  				= "authentication.kdc.address";
	protected static final String adLDAPAddressKey 				= "authentication.ldap.address";
    protected static final String defaultADKdcAddress 			= "activedirectory.company.com:88";
    protected static final String defaultADLdapAddress 			= "activedirectory.company.com:389";
    protected static final String defaultADLoginAttribute 		= ADIdentity.ATTRIBUTES.get(0);
    protected static final String defaultADLoginMatch 			= "";
    protected String BindDN;
	protected String ldapAddress;
    protected String kdcAddress;

	
	public ADIdentity() { 
	
	}
	
	public ADIdentity(String ldapAddress, String kdcAddress) {
		this.ldapAddress = ldapAddress;
		this.kdcAddress = kdcAddress;
	}
	
	public String getEmailAttribute() {
		return "proxyAddresses";
	}
	
	public String getEmailValue() {
		return "SMTP:(.*)";
	}
	
	
	public String getBindDN() { return BindDN; }
	
	public void setBindDN(String BindDN) { this.BindDN = BindDN; }
	
	
	
	public void addRoleMap(String role, String attribute, String regex)
	throws ConfigurationException {
		addRoleMap(new ADRoleMap(role, attribute, regex));
	}	
	
	@Override
	public void newRoleMap() throws ConfigurationException {
		addRoleMap(new ADRoleMap(Roles.ADMINISTRATOR_ROLE.getName(), "", ""));
	}
	
	public String getKDCAddress() { return kdcAddress; }
	
    public void setKDCAddress(String kdcAddress) {
        this.kdcAddress = kdcAddress.toLowerCase(Locale.ENGLISH);
        if (this.kdcAddress.indexOf(':')==-1)
         this.kdcAddress += ":88";
    }
    
    public String getLDAPAddress() { return ldapAddress; }
    
    public void setLDAPAddress(String ldapAddress) {
        this.ldapAddress = ldapAddress.toLowerCase(Locale.ENGLISH);
        if (this.ldapAddress.indexOf(':')==-1)
            this.ldapAddress += ":389";
    }
    
    
    
    public boolean loadSettings(String prefix, Settings prop, String suffix) {
    	logger.debug("loading ad identity");
	  	// active directory identity info
	  	
	 	setKDCAddress(ConfigUtil.getString(prop.getProperty(adKDCAddressKey),defaultADKdcAddress));
	 	setLDAPAddress(ConfigUtil.getString(prop.getProperty(adLDAPAddressKey),defaultADLdapAddress));
	  	
	  	clearAllRoleMaps();
	  	int c = 1;
	  	boolean load = false;
	  	do {
	  		RoleMap rm = new ADRoleMap();
	  		load = rm.loadSettings(null,prop,"."+c++);
	  		if (load) addRoleMap(rm);
	  	} while (load);
        
        return true;
    }
    
    
    public void saveSettings(String prefix, Settings prop, String suffix) {
    	logger.debug("saving ad identity");
	  	prop.setProperty(adLDAPAddressKey, getLDAPAddress());
	  	prop.setProperty(adKDCAddressKey, getKDCAddress());
	  	
	  	int c = 1;
	  	for (RoleMap rolemap : getRoleMaps()) {
	  		rolemap.saveSettings(null,prop,"."+c++);
	  	}
	
		
    }
    
  
    public class ADRoleMap extends Identity.RoleMap implements Props,Serializable {
    	
    	protected static final String adRoleMapKey  				= "role.assignment.role";
    	protected static final String adRoleDomainKey 				= "role.assignment.domain";
    	protected static final String adRoleAttributeKey 			= "role.assignment.attribute";
    	protected static final String adRoleMatchKey 				= "role.assignment.criterion";
    	private static final long serialVersionUID = 2161647256098102054L;
    	protected String attribute;
		protected String regex;
		
    	public ADRoleMap() {
    		
    	}
    	
    	public ADRoleMap(String role, String attribute, String regex) throws ConfigurationException {
    		setRole(role);
    		setAttribute(attribute);
    		setRegEx(regex);
    	}	
    	
    	
    	 public void saveSettings(String prefix, Settings prop, String suffix) {
 		     prop.setProperty(adRoleMapKey + suffix,getRole());
 			 prop.setProperty(adRoleAttributeKey + suffix,getAttribute());
 			 prop.setProperty(adRoleMatchKey + suffix,getRegEx());
 			 
    	 }
    	 
    	 public boolean loadSettings(String prefix, Settings prop, String suffix) {
    		 String lr = prop.getProperty(adRoleMapKey + suffix);
 	  	     String la = prop.getProperty(adRoleAttributeKey + suffix);
 	  	     String lm = prop.getProperty(adRoleMatchKey + suffix);
 
 	  	     if (lr== null || la == null || lm == null)
 	  	    	return false;
 	  	    
 	  	    try {
 	  	    	setRoleValue(ConfigUtil.getString(lr,Roles.ADMINISTRATOR_ROLE.getName()));
 	  	    } catch (ConfigurationException ce) {
 	  	    	return false;
 	  	    }
 	  	    
 	  	    setAttribute(ConfigUtil.getString(la,defaultADLoginAttribute));
 	  	    setRegEx(ConfigUtil.getString(lm,defaultADLoginMatch));
 	  	    
 	  	    return true;
 	  	  
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
    }
    
   
}
