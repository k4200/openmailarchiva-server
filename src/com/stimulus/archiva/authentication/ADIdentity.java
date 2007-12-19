package com.stimulus.archiva.authentication;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ADIdentity extends LDAPIdentity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2961646156098906034L;
	public static final List<String> ATTRIBUTES;
	public static final List<String> ATTRIBUTE_LABELS;
	
	protected String kdcAddress;
	
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

		Map<String, String> emailMappings = new LinkedHashMap<String, String>();
		emailMappings.put("mail", "(.*)@(.*)");
		defaultEmailMappings = emailMappings;
	}
	
	public ADIdentity() { }
	
	public ADIdentity(String ldapAddress, String kdcAddress) {
		super(ldapAddress);
		emailmap =new LinkedHashMap<String, String>(defaultEmailMappings);
	}
	
	public String getKDCAddress() { return kdcAddress; }
	
    public void setKDCAddress(String kdcAddress) {
        this.kdcAddress = kdcAddress.toLowerCase(Locale.ENGLISH);
        if (this.kdcAddress.indexOf(':')==-1)
         this.kdcAddress += ":88";
    }
  
   
}
