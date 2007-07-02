/* Copyright (C) 2005 Jamie Angus Band 
 * This software program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.stimulus.archiva.domain;
import com.stimulus.archiva.exception.ConfigurationException;
import org.apache.log4j.Logger;
import java.util.*;

 public class RoleMaps {

     protected static Logger logger = Logger.getLogger(RoleMaps.class.getName());

     static {
	     List roles = new ArrayList();
	     roles.add("none");
	     roles.add("user");
	     roles.add("auditor");
	     roles.add("administrator");
	     ROLES = Collections.unmodifiableList(roles);

	     List AttributeList = new ArrayList();
	     AttributeList.add("memberOf");
	     AttributeList.add("userPrincipalName");
	     AttributeList.add("sAMAccountName");
	     AttributeList.add("distinguishedName");
	     ATTRIBUTES = Collections.unmodifiableList(AttributeList);
     }

     protected List roleMaps = new ArrayList();
     public static final List ROLES;
     public static final List ATTRIBUTES;

     public List getRoleMaps() {
         return roleMaps;
     }

     public RoleMap getRoleMap(int index) {
     	return (RoleMap)roleMaps.get(index);
     }

     public void clearAllRoleMaps() {
      roleMaps.clear();
     }


     public void addRoleMap(String role, String domain, String attribute, String regex) throws ConfigurationException {
     	RoleMap af = new RoleMap(role,domain, attribute,regex);
       roleMaps.add(af);
     }

     public void deleteRoleMap(int id) {
     	roleMaps.remove(id);
     }

     public String getRoleFromID(int roleId) {
	  	    return (String)ROLES.get(roleId);
	 }

     public class RoleMap {
     	String role;
 	  	String attribute;
 	  	String regex;
 	  	String domain;


 	  	RoleMap(String role, String domain, String attribute, String regex) throws ConfigurationException {
 	  		setRole(role);
 	  		setDomain(domain);
 	  		setAttribute(attribute);
 	  		setRegEx(regex);
 	  	}

 	  	public void setAttribute(String attribute) throws ConfigurationException {
 	  	if (!ATTRIBUTES.contains(attribute))
 	  	    throw new ConfigurationException("failed to set attribute in role mapping {attribute='"+attribute+"'}",logger);
 	  	    this.attribute = attribute;
 	  	}

 	  	public String getAttribute() { return attribute; }

 	  	public String getRole() { return role; }

 	  	public void setRole(String role) throws ConfigurationException {
 	  	    if (!ROLES.contains(role))
 	  	        throw new ConfigurationException("failed to set role in role mapping {role='"+role+"'}",logger);
 	  	    this.role = role;
 	  	}

 	  	public String getRegEx() { return regex; }
 	  	public void setRegEx(String regex) { this.regex = regex;}
 	  	public String getDomain() { return domain; }
 	  	public void setDomain(String domain) {
 	  	    this.domain = domain.toLowerCase().trim();
 	  	}

 	  	public void setRoleID(int roleId) throws ConfigurationException {
 	  	    String newrole = (String)ROLES.get(roleId);
 	  	    if (newrole==null)
 	  	        throw new ConfigurationException("failed to set role. invalid role id "+role,logger);
 	  	    this.role = newrole;
 	  	}

 	  	public int getRoleID() {
 	  	    return ROLES.indexOf(role);
 	  	}


     }
}