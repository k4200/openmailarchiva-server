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

import java.util.*;
import org.apache.commons.logging.*;
import com.stimulus.archiva.domain.fields.*;
import com.stimulus.util.*;

public class EmailFilter {

	public enum Priority { HIGHEST, HIGHER, LOWER, LOWEST  };    
	public enum Condition { MATCHES, NOTMATCHES, CONTAINS, NOTCONTAINS, IS, ISNT, BEGINSWITH, ENDSWITH, LESSTHAN, GREATERTHAN };
	public enum Operator { ANY, ALL };
	protected static Log logger = LogFactory.getLog(EmailFilter.class);
	LinkedList<FilterRule> filterRules = new LinkedList<FilterRule>();
	String action;
	
	public EmailFilter() {}
	

	public void addRule(FilterRule rule) {
		filterRules.add(rule);
	}
	
	public void deleteRule(FilterRule rule) {
		filterRules.remove(rule);
	}
	
	public List<FilterRule> getFilterRules() { 
		return filterRules;
	}
	
	public void clearAllRules() {
		filterRules.clear();
	}
	

	 public void setPriority(int id, Priority priority)  {
	 	  	LinkedList<FilterRule> list = filterRules;
	 	  	FilterRule ar = filterRules.get(id);
	 	   	list.remove(ar);
	 	   	
	 	    switch (priority) {
	 		  	    case HIGHER:	if ((id-1)<=0)
	 		  	                                list.addFirst(ar);
	 										else
	 											list.add(id-1,ar);
	 		  	    					 	break;
	 		  	    case LOWER: 	if ((id+1)>=list.size())
	 		  	    							list.addLast(ar);
	 		  	    						else
	 		  	    							list.add(id+1,ar);
	 		  	                         	break;
	 		  	    case HIGHEST: 	list.addFirst(ar);
	 		  	     						break;
	 		  	    case LOWEST:    list.addLast(ar);
	 		  	    						break;
	 	    }
	 }

	   
	
	public class FilterRule {

		String 	 action;
		Operator operator;
	
		LinkedList<FilterClause> filterClauses = new LinkedList<FilterClause>();
		
		public FilterRule(Operator operator,String action )  {
			this.operator = operator;
			this.action = action;
		}
		public void addClause(FilterClause clause) {
			filterClauses.add(clause);
		}
		
		public void deleteClause(FilterClause clause) {
			filterClauses.remove(clause);
		}
		
		public FilterClause getClause(int index) {
			return filterClauses.get(index);
		}
		
		public List<FilterClause> getFilterClauses() { 
			return filterClauses;
		}
		
		public void setAction(String action) {
			this.action = action;
		}
		
		public void setOperator(Operator operator) {
			this.operator = operator;
		}
		
		public Operator getOperator() { return operator; }
		
		public String getAction() { return action; }
		
		public boolean match(Email email) {
			
			if (filterClauses.size()<1)
				return false;
			
			boolean check; 
			
			if (operator.equals(Operator.ANY)) {
				check = false;
			} else 
				check = true;
			
			for (FilterClause clause : filterClauses) {
				
				boolean result = false;
				if (Compare.equalsIgnoreCase(clause.getField(),"addresses")) {
					EmailFieldValue to = email.getFields().get("to");
					EmailFieldValue cc = email.getFields().get("cc");
					EmailFieldValue from = email.getFields().get("from");
					EmailFieldValue bcc = email.getFields().get("bcc");
					result = match(clause,to) || match(clause,cc) || match(clause,from) || match(clause,bcc);
				} else {
					EmailFieldValue efv = email.getFields().get(clause.getField());
					result = match(clause,efv);
					
				}
				if (operator.equals(Operator.ANY)) {
					check = check || result;
					if (check)
						return check;
				} else if (operator.equals(Operator.ALL)) {
					check = check && result;
					if (!check)
						return check;
				}
			}
			return check;
		}
		
		public boolean match(FilterClause clause,EmailFieldValue efv) {
			if (efv==null) 
				return false;
			
			String ev = efv.getValue().toLowerCase(Locale.ENGLISH);
			String arv = clause.getValue().toLowerCase(Locale.ENGLISH);
			boolean result = false;
			if (clause.condition==Condition.IS) {
				result = ev.equalsIgnoreCase(arv);
			} else if (clause.condition==Condition.ISNT) {
				result = !ev.equalsIgnoreCase(arv);
			} else if (clause.condition==Condition.BEGINSWITH) {
				result = ev.startsWith(arv);
			} else if (clause.condition==Condition.ENDSWITH) {
				result = ev.endsWith(arv);
			} else if (clause.condition==Condition.GREATERTHAN) {
				try { 
					float v = Float.valueOf(ev);
					float v2 = Float.valueOf(arv);
					result = v > v2;
				} catch (NumberFormatException nfe) {
					logger.debug("failed to match greater than in filter rule. not a number {emailfield='"+ev+"',archivevalue='"+arv+"'}"); 
				}
			} else if (clause.condition==Condition.LESSTHAN) {
				try { 
					float v = Float.valueOf(ev);
					float v2 = Float.valueOf(arv);
					result = v < v2;
				} catch (NumberFormatException nfe) {
					logger.debug("failed to match less than in filter rule. not a number {emailfield='"+ev+"',archivevalue='"+arv+"'}"); 
				}
			} else if (clause.condition==Condition.MATCHES) {
				result = ev.matches(arv);
			} else if (clause.condition==Condition.NOTMATCHES) {
				result = !ev.matches(arv);
			} else if (clause.condition==Condition.CONTAINS) {
				result = ev.contains(arv);
			} else if (clause.condition==Condition.NOTCONTAINS) {
				result = !ev.contains(arv);
			}
			return result;
		}
		
		@Override
		public String toString() {
			StringBuffer output = new StringBuffer();
			for (FilterClause clause : filterClauses) {
				output.append(clause.toString());
				output.append(",");
				output.append("action");
				output.append(action);
				output.append(",");
			}
			String out = output.toString();
			if (out.endsWith(",")) { 
				return out.substring(0,out.length()-1);
		 	} else { 
				return out;
			}
		}
		
	}
	
	
	
	public static class FilterClause {
	
		protected String field;
		protected Condition condition;
		protected String value;

		public FilterClause(String field, Condition condition, String value) {
			this.field = field;
			this.condition = condition;
			this.value = value;
		}
		
		public void setField(String field) { this.field = field; }
		
		public void setCondition(Condition condition) { this.condition = condition; }
		
		public void setValue(String value) { this.value = value; }
		
		public String getField() { return field; }
		
		public Condition getCondition() { return condition; }
		
		public String getValue() { return value; }
		
		public String getString() {
			return "field='"+getField()+",value='"+getValue()+"',condition='"+condition+"'";
		}
	}

}
