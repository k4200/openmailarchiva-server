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
