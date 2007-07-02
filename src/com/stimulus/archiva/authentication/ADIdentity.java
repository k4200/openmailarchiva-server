package com.stimulus.archiva.authentication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.stimulus.archiva.domain.*;

import com.stimulus.archiva.exception.ConfigurationException;

public class ADIdentity extends Identity {

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
	     
	     Map<String,String> emailMappings = new LinkedHashMap<String,String>();
         emailMappings.put("proxyAddresses","SMTP:(.*)@(.*)");
         defaultADEmailMappings = emailMappings;
    }

    
    public static final List<String> ATTRIBUTES;
    public static final List<String> ATTRIBUTE_LABELS;
    protected Map<String,String>  adEmailMappings = new LinkedHashMap<String,String>(defaultADEmailMappings);
    protected static Map<String,String> defaultADEmailMappings;
    
    public void newRoleMap() throws ConfigurationException {
   	 addRoleMap(new ADRoleMap(ROLES.get(0),ATTRIBUTES.get(0),""));
    }
    public void addEmailMapping(String attribute, String value) {
    	adEmailMappings.put(attribute, value);
    }
    
    public void clearEmailMappings() {
    	adEmailMappings.clear();
    }
    
    public Map<String,String> getEmailMappings() { return adEmailMappings; }
    
    
    public void addRoleMap(String role, String attribute, String regex) throws ConfigurationException {
    	addRoleMap(new ADRoleMap(role, attribute, regex));
    }
    
    public class ADRoleMap extends Identity.RoleMap {
    
	  	String attribute;
	  	String regex;
	 
	  	public ADRoleMap(String role, String attribute, String regex) throws ConfigurationException {
	  		setRole(role);
	  		setAttribute(attribute);
	  		setRegEx(regex);
	  	}

	  	public void setAttribute(String attribute) throws ConfigurationException {
	  		//if (!ATTRIBUTES.contains(attribute)) 
	  		//	throw new ConfigurationException("failed to set attribute in role mapping {attribute='"+attribute+"'}",logger);
	  	    this.attribute = attribute;
	  	}

	  	public String getAttribute() { return attribute; }

	  	public String getRegEx() { return regex; }
	  	public void setRegEx(String regex) { this.regex = regex;}

    }
}
