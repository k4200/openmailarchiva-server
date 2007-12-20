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

package com.stimulus.archiva.presentation;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.stimulus.archiva.authentication.LDAPIdentity.LDAPRoleMap;
import com.stimulus.archiva.domain.Identity.RoleMap;
import com.stimulus.archiva.exception.ConfigurationException;
import com.stimulus.struts.BaseBean;

public class LDAPRoleMapBean extends BaseBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8245556520189616857L;
	protected static Logger logger =  Logger.getLogger(LDAPRoleMapBean.class.getName());
	protected LDAPRoleMap roleMap;
	

    public LDAPRoleMapBean(LDAPRoleMap roleMap) {
        this.roleMap = roleMap;
    }

	  	public void setAttribute(String attribute) throws ConfigurationException {
	  		roleMap.setAttribute(attribute);
	  	}

	  	public String getAttribute() { return roleMap.getAttribute(); }

	  	public String getRole() { return roleMap.getRole(); }

	  	public void setRole(String role) throws ConfigurationException {
	  	    roleMap.setRole(role);
	  	}

	  	public String getRegEx() { return roleMap.getRegEx(); }
	  	public void setRegEx(String regex) { roleMap.setRegEx(regex); }
	  	public String getDomain() { return roleMap.getRegEx(); }
	 
	  	public void setRoleID(int roleId) throws ConfigurationException {
	  	   roleMap.setRoleID(roleId);
	  	}
	  	public int getRoleID() { return roleMap.getRoleID(); }

	    public static List<LDAPRoleMapBean> getLDAPRoleMapBeans(List<RoleMap> roleMaps) {
			  List<LDAPRoleMapBean> LDAPRoleMapBeans = new LinkedList<LDAPRoleMapBean>();
			  for (RoleMap roleMap: roleMaps)
				  LDAPRoleMapBeans.add(new LDAPRoleMapBean((LDAPRoleMap)roleMap));
			  return LDAPRoleMapBeans;
		}
	    

	 
}
