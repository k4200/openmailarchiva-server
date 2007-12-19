
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

import com.stimulus.archiva.domain.Domains;
import com.stimulus.struts.BaseBean;

public class DomainBean extends BaseBean implements Serializable {

	private static final long serialVersionUID = -3532049048337701224L;
	protected static Logger logger =  Logger.getLogger(DomainBean.class);
	protected Domains.Domain domain;

    public DomainBean(Domains.Domain domain) {
        this.domain = domain;
    }

    public void setName(String name) {
        domain.setName(name);
    }

    public String getName() {
        return domain.getName();
    }
    
    public static List<DomainBean> getDomainBeans(List<Domains.Domain> Domains) {
		  List<DomainBean> DomainBeans = new LinkedList<DomainBean>();
		  for (Domains.Domain domain: Domains)
			  DomainBeans.add(new DomainBean(domain));
		  return DomainBeans;
	}
    
}
