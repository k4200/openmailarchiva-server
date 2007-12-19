
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

import org.apache.log4j.Logger;

import com.stimulus.struts.ActionContext;
import com.stimulus.struts.BaseBean;

public class AccountBean extends BaseBean  implements Serializable {

	private static final long serialVersionUID = -6557623393822264395L;
	protected static Logger logger = Logger.getLogger(MessageBean.class.getName());
    protected static final Logger audit = Logger.getLogger("com.stimulus.archiva.audit");
    
    
    public AccountBean() {}
    
	  public String signoff() {
	    ActionContext.getActionContext().getRequest().getSession().invalidate();
	    clear();
	    return "success";
	  }
	
}

