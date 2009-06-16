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

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import org.apache.commons.logging.*;
import com.stimulus.archiva.exception.ConfigurationException;
import com.stimulus.struts.BaseBean;
import com.stimulus.archiva.domain.*;

public class ArchiveRuleBean extends BaseBean implements Serializable {

	private static final long serialVersionUID = -2975630165623482789L;
	protected static Log logger = LogFactory.getLog(ArchiveFilter.class.getName());
	protected ArchiveFilter.FilterRule filterRule;
	protected  List<ArchiveClauseBean> archiveClauseBeans = null;

    public ArchiveRuleBean(ArchiveFilter.FilterRule filterRule) {
    	this.filterRule = filterRule;
    }

  	public void setAction(String action) throws ConfigurationException {
  		filterRule.setAction(action);
	}

  	public void setOperator(String operator) throws ConfigurationException {
  		EmailFilter.Operator op = EmailFilter.Operator.valueOf(operator.trim().toUpperCase(Locale.ENGLISH));
  		filterRule.setOperator(op);
  	}

	public String getAction() { return filterRule.getAction().toString().toLowerCase(Locale.ENGLISH); }


	public String getOperator() { return filterRule.getOperator().toString().toLowerCase(Locale.ENGLISH); }

	public List<ArchiveClauseBean> getArchiveClauses() {
		  List<ArchiveClauseBean> archiveClauseBeans = new LinkedList<ArchiveClauseBean>();
		  for (ArchiveFilter.FilterClause archiveClause: filterRule.getFilterClauses())
			  archiveClauseBeans.add(new ArchiveClauseBean(archiveClause));
		  return archiveClauseBeans;
	}

    public static List<ArchiveRuleBean> getArchiveRuleBeans(List<ArchiveFilter.FilterRule> archiveRules) {
		  List<ArchiveRuleBean> ArchiveRuleBeans = new LinkedList<ArchiveRuleBean>();
		  for (ArchiveFilter.FilterRule archiveRule: archiveRules)
			  ArchiveRuleBeans.add(new ArchiveRuleBean(archiveRule));
		  return ArchiveRuleBeans;
	}

    public static List<ArchiveRuleBean> getArchiveRuleBeans(ArchiveFilter archiveRules) {
    	return getArchiveRuleBeans(archiveRules.getArchiveRules());
    }


    public class ArchiveClauseBean {

    	ArchiveFilter.FilterClause archiveClause;

    	public ArchiveClauseBean(ArchiveFilter.FilterClause archiveClause) {
    		this.archiveClause = archiveClause;
    	}

    	public String getField() { return archiveClause.getField(); }

    	public String getCondition() { return archiveClause.getCondition().toString().toLowerCase(Locale.ENGLISH); }

    	public String getValue() { return archiveClause.getValue(); }

    	public void setField(String field) {
    		archiveClause.setField(field);
    	}

    	public void setValue(String value) {
    		archiveClause.setValue(value);
    	}

    	public void setCondition(String condition) throws ConfigurationException {
      		EmailFilter.Condition con = EmailFilter.Condition.valueOf(condition.trim().toUpperCase(Locale.ENGLISH));
      		archiveClause.setCondition(con);
      	}


    }
}
