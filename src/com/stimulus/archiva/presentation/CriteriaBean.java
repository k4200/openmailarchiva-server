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
