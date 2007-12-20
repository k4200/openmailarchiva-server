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
