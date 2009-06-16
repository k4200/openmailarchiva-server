/* Copyright (C) 2005-2009 Jamie Angus Band
 * MailArchiva Open Source Edition Copyright (c) 2005-2009 Jamie Angus Band
 * This program is free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation; either version
 * 3 of the License, or (at your option) any later version.
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
import java.util.*;
import org.apache.commons.logging.*;
import com.stimulus.util.*;

public class Roles implements Props {

	protected static final String roleKey 					= "role";
	protected static final String roleNameKey 				= "name";
	protected static final String rolePermissionsKey 		= "permission";
	protected static final String roleViewFilterKey 		= "viewfilter";

	protected LinkedList<Role> roles = new LinkedList<Role>();

	public static Role ADMINISTRATOR_ROLE;
	public static Role SYSTEM_ROLE;
	public static Role USER_ROLE;
	public static Role AUDITOR_ROLE;
	public static Role MASTER_ROLE;
    protected static Log logger = LogFactory.getLog(Roles.class.getName());


	static {
		ViewFilter systemFilter = new ViewFilter();
		SYSTEM_ROLE = new Role("system",systemFilter); // built in role

		ViewFilter masterFilter = new ViewFilter();
		MASTER_ROLE = new Roles.Role("master",masterFilter); // built in role
		masterFilter.addCriteria(new Criteria("addresses",Criteria.Method.EXACT,"",Criteria.Operator.OR));


		ViewFilter adminFilter = new ViewFilter();
		ADMINISTRATOR_ROLE = new Roles.Role("administrator",adminFilter); // built in role
		adminFilter.addCriteria(new Criteria("addresses",Criteria.Method.EXACT,"",Criteria.Operator.OR));

		ViewFilter auditorFilter = new ViewFilter();
		auditorFilter.addCriteria(new Criteria("addresses",Criteria.Method.EXACT,"",Criteria.Operator.OR));
		AUDITOR_ROLE = new Role("auditor",auditorFilter); // built in role

		ViewFilter userFilter = new ViewFilter();
		userFilter.addCriteria(new Criteria("addresses",Criteria.Method.EXACT,"%email%",Criteria.Operator.OR));
		USER_ROLE = new Role("user",userFilter); // built in role


	}

	public Roles() {


		// built in administrator role (for administration)
		roles.add(MASTER_ROLE);
		roles.add(ADMINISTRATOR_ROLE);
		roles.add(AUDITOR_ROLE);
		roles.add(USER_ROLE);
	}



	public static class Role {

		protected String name;
		protected ViewFilter viewFilter;

		protected String defaultRoleName = "";

		public Role(String name, ViewFilter viewFilter) {
			this.name = name;
			this.viewFilter = viewFilter;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setViewFilter(ViewFilter viewFilter) {
			this.viewFilter = viewFilter;
		}

		public ViewFilter getViewFilter() {
			return viewFilter;
		}

		@Override
		public String toString() {
			return "role='"+getName()+"'";
		}

		public boolean equals(Role role) {
			return (Compare.equalsIgnoreCase(role.getName(), this.getName()));
		}
	}

	public Role getRole(int id) {
		return roles.get(id);
	}

	public int getRolePriority(Role role) {
		int p = 0;
		for (Role roli : roles) {
			if (roli==role) return p;
			p++;
		}
		return p;
	}

	public Role getRole(String name) {
		for (Role role : roles) {
			if (Compare.equalsIgnoreCase(role.getName(),name))
				return role;
		}
		return null;
	}

	public void clearRoles() {
	  	roles.clear();
	}


	public Collection<Role> getRoles() {
		return roles;
	}

	public void deleteRole(int id) {
		roles.remove(id);
	}

	public void saveSettings(String prefix, Settings prop, String suffix) {

	 }

	public boolean loadSettings(String prefix, Settings prop, String suffix) {
	    roles.clear();
		roles.addLast(MASTER_ROLE);
		roles.add(ADMINISTRATOR_ROLE);
		roles.add(AUDITOR_ROLE);
		roles.add(USER_ROLE);
		return true;
	}



}
