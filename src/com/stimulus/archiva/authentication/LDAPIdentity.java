package com.stimulus.archiva.authentication;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import com.stimulus.archiva.domain.Identity;
import com.stimulus.archiva.exception.ConfigurationException;
public class LDAPIdentity extends Identity implements Serializable {

	protected static Map<String, String> defaultEmailMappings;
	private static final long serialVersionUID = 8672529238138755867L;
	
	protected Map<String, String> emailmap = new LinkedHashMap<String, String>(defaultEmailMappings);
	String BindDN;
	
	String ldapAddress;
	
	static {
		Map<String, String> emailMappings = new LinkedHashMap<String, String>();
		emailMappings.put("proxyAddresses", "SMTP:(.*)@(.*)");
		defaultEmailMappings = emailMappings;	
	}
	
	public LDAPIdentity() { }
	
	protected LDAPIdentity(String ldapAddress) {
		emailmap = defaultEmailMappings;
		this.ldapAddress = ldapAddress;
	}
	
	public LDAPIdentity(String ldapAddress, String BindDN) {
		emailmap = defaultEmailMappings;
		this.ldapAddress = ldapAddress;
		this.BindDN = BindDN;
	}
	
    public String getLDAPAddress() { return ldapAddress; }
   
    public void setLDAPAddress(String ldapAddress) {
        this.ldapAddress = ldapAddress.toLowerCase(Locale.ENGLISH);
        if (this.ldapAddress.indexOf(':')==-1)
            this.ldapAddress += ":389";
    }
	    
	public String getBindDN() { return BindDN; }
	
	public void setBindDN(String BindDN) { this.BindDN = BindDN; }
	
	public void newRoleMap() throws ConfigurationException {
		addRoleMap(new LDAPRoleMap(ROLES.get(0), "", ""));
	}

	public void addEmailMapping(String attribute, String value) {
		emailmap.put(attribute, value);
	}

	public void clearEmailMappings() {
		emailmap.clear();
	}

	public Map<String, String> getEmailMappings() {
		return emailmap;
	}

	public void addRoleMap(String role, String attribute, String regex)
			throws ConfigurationException {
		addRoleMap(new LDAPRoleMap(role, attribute, regex));
	}

	public class LDAPRoleMap extends Identity.RoleMap {

		String attribute;
		String regex;

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

	}
}
