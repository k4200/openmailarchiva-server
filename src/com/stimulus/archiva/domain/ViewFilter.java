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
import org.apache.log4j.Logger;
import java.util.*;

public class ViewFilter implements java.io.Serializable, Props {
	
	private static final long serialVersionUID = -219142124332720112L;
	
	protected ArrayList<Criteria> criteria   = new ArrayList<Criteria>();
	protected static final Logger logger = Logger.getLogger(ViewFilter.class.getName());
	protected static final String viewCriteriaFieldKey 			= "field";
	protected static final String viewCriteriaMethodKey 		= "method";
	protected static final String viewCriteriaQueryKey 			= "query";
	protected static final String viewCriteriaOperatorKey 		= "operator";
	
	protected String defaultViewCriteriaField = "from";
	protected String defaultViewCriteriaMethod = "all";
	protected String defaultViewCriteriaQuery  = "%email%";
	protected String defaultViewCriteriaOperator = "and";
	

    public void clearCriteria() {
    	criteria.clear();
    }
    
    public void addCriteria(Criteria crit) {
    	logger.debug("addCriteria()");
    	criteria.add(crit);
    }
    public void newCriteria() {
          logger.debug("newCriteria()");
          criteria.add(new Criteria("subject"));
    }
    
    public List<Criteria> getCriteria() {
    	
    	return criteria;
    }
    
    public void deleteCriteria(int id) {
      logger.debug("deleteToCriteria() {index='"+id+"'}");  
    	criteria.remove(criteria.get(id));

    }

    /*	protected static final String viewCriteriaField 		= "view.criteria.field";
	protected static final String viewCriteriaMethod 		= "view.criteria.method";
	protected static final String viewCriteriaQuery 		= "view.criteria.query";
	protected static final String viewCriteriaOperator 		= "view.criteria.operator";
	*/
    public void saveSettings(String prefix, Settings prop, String suffix) {
    	logger.debug("saving view filter");
    	int c = 1;
    	for (Criteria crit : criteria) {
	       	prop.setProperty(prefix + viewCriteriaFieldKey + "." + c , crit.getField());
	       	prop.setProperty(prefix + viewCriteriaMethodKey + "." + c, crit.getMethod().toString().toLowerCase(Locale.ENGLISH));
	       	prop.setProperty(prefix + viewCriteriaQueryKey + "." + c, crit.getQuery());
	       	prop.setProperty(prefix + viewCriteriaOperatorKey + "." + c, crit.getOperator().toString().toLowerCase(Locale.ENGLISH));
	       	c++;
    	}
   }
   
   public boolean loadSettings(String prefix, Settings prop, String suffix) {
	   logger.debug("loading view filter");
	   
	    int c = 1;
	    criteria.clear();
	    do {
	    	String cf = prop.getProperty(prefix + viewCriteriaFieldKey + "." + c );
	    	String cm = prop.getProperty(prefix + viewCriteriaMethodKey + "." + c );
	    	String cq = prop.getProperty(prefix + viewCriteriaQueryKey + "." + c );
	    	String co =  prop.getProperty(prefix + viewCriteriaOperatorKey + "." + c );
	    	
	    	if (cf == null && cm == null && cq == null && co == null)
	  	    	return true;
	    	
	    	Criteria.Method method = Criteria.Method.valueOf(cm.toUpperCase(Locale.ENGLISH));
	    	Criteria.Operator operator = Criteria.Operator.valueOf(co.toUpperCase(Locale.ENGLISH));
	    	Criteria crit = new Criteria(cf,method,cq,operator);
	    	criteria.add(crit);   
	    	c++;
	    } while (true);
   }
}
