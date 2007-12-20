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
package com.stimulus.archiva.domain;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.stimulus.archiva.exception.ConfigurationException;

 public abstract class Identity implements java.io.Serializable  {
	 
	 public static final List<String> ROLES;
	 
	 static {
	   	 
	     List<String> roles = new LinkedList<String>();
	     roles.add("none");
	     roles.add("user");
	     roles.add("auditor");
	     roles.add("administrator");
	     ROLES = Collections.unmodifiableList(roles);
	 }

	 List<RoleMap> roleMaps = new LinkedList<RoleMap>();
	 
	 protected static Logger logger = Logger.getLogger(Identity.class);
     
     public Identity() {
    	 
     }
    
     
     public List<RoleMap> getRoleMaps() {
         return roleMaps;
     }

     public RoleMap getRoleMap(int index) {
     	return (RoleMap)roleMaps.get(index);
     }

     
     public void clearAllRoleMaps() {
      roleMaps.clear();
     }

     public abstract void newRoleMap() throws ConfigurationException;
     
     
     public void addRoleMap(RoleMap roleMap) throws ConfigurationException {
       roleMaps.add(roleMap);
     }

     public void deleteRoleMap(int id) {
     	roleMaps.remove(id);
     }

     public String getRoleFromID(int roleId) {
 	  	    return (String)ROLES.get(roleId);
 	 }
     
     public static List<String> getRoles() {
     	return Identity.ROLES;
     }
     
     public static List<String> getRoleLabels() {
     	 List<String> rolesLabelList = new LinkedList<String>();
     	 rolesLabelList.add("roles_label_none");
     	 rolesLabelList.add("roles_label_user");
     	 rolesLabelList.add("roles_label_auditor");
     	 rolesLabelList.add("roles_label_administrator");
     	 return rolesLabelList;   
     }  

     
     public abstract class RoleMap {
    	 
    	 protected String role; 
    	 
    	 public String getRole() {
    		 return role;
    	 }
    	 
    	 public void setRole(String role) throws ConfigurationException {
    		  if (!ROLES.contains(role))
  	  	        throw new ConfigurationException("failed to set role in role mapping {role='"+role+"'}",logger);
    		 this.role = role;
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