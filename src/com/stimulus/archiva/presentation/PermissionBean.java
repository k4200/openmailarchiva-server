
/* Copyright (C) 2005-2007 Jamie Angus Band 
 * MailArchiva Open Source Edition Copyright (c) 2005-2007 Jamie Angus Band
 * This program is free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation; either version
 * 2 of the License, or (at  your option) any later version.
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
import org.apache.struts.action.ActionServlet;

import com.stimulus.archiva.domain.Permission;
import com.stimulus.archiva.domain.Permission.PermissionRoleMap;
import com.stimulus.archiva.exception.ConfigurationException;
import com.stimulus.struts.BaseBean;

public class PermissionBean extends BaseBean implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8042669432887610673L;
	protected static Logger logger =  Logger.getLogger(PermissionBean.class.getName());
	protected PermissionRoleMap roleMap;
	
	  
	public PermissionBean(PermissionRoleMap roleMap) {
		this.roleMap = roleMap;
	}
  	public String getRole() { return roleMap.getRole(); }

  	public void setRole(String role) throws ConfigurationException {
  	    roleMap.setRole(role);
  	}
  	
	public String getRoleLabel() {
		String s = getMessage("roles_label_"+roleMap.getRole());
		return (s.length()>0)? Character.toUpperCase(s.charAt(0))+s.substring(1) : s;
	}
	
  	
    public static List<PermissionBean> getPermissionBeans(List<Permission.PermissionRoleMap> roleMaps, ActionServlet servlet) {
		  List<PermissionBean> permissionBeans = new LinkedList<PermissionBean>();
		  for (PermissionRoleMap roleMap: roleMaps) {
			  PermissionBean pb = new PermissionBean(roleMap);
			  pb.setServlet(servlet);
			  permissionBeans.add(pb);
		  }
		  return permissionBeans;
	}
    
    public Permission.PermissionRoleMap getPermission() {
    	return roleMap;
    }
    
  
}
