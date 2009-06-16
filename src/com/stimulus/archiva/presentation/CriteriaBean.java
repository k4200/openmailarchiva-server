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

package com.stimulus.archiva.presentation;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.*;

import com.stimulus.archiva.domain.*;

public class CriteriaBean {

	Criteria criteria;
	protected static Log logger = LogFactory.getLog(CriteriaBean.class.getName());

	public CriteriaBean(Criteria criteria) {
		this.criteria = criteria;
	}

	public String getQuery() { return criteria.getQuery(); }
	public void setQuery(String query) { criteria.setQuery(query); }

	public String getMethod() {
		return criteria.getMethod().toString().toLowerCase(Locale.ENGLISH);
	}

	public void setMethod(String method) {
		Criteria.Method me = Criteria.Method.valueOf(method.trim().toUpperCase(Locale.ENGLISH));
		criteria.setMethod(me);
	}

	public void  setOperator(String operator) {
		Criteria.Operator op = Criteria.Operator.valueOf(operator.trim().toUpperCase(Locale.ENGLISH));
		criteria.setOperator(op);
	}

	public String getOperator() {
		return criteria.getOperator().toString().toLowerCase(Locale.ENGLISH);
	}

	public String getField() { return criteria.getField(); }
	public void setField(String field) { criteria.setField(field); }

   public static List<CriteriaBean> getCriteriaBeans(List<Criteria> criteria) {
		  List<CriteriaBean> criteriaBeans = new LinkedList<CriteriaBean>();
		  for (Criteria crit: criteria)
			  criteriaBeans.add(new CriteriaBean(crit));
		  return criteriaBeans;
	}

}
