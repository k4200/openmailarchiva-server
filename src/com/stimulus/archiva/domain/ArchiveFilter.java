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
import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.NewsAddress;
import org.apache.log4j.Logger;
import com.stimulus.util.Compare;
import com.stimulus.util.ConfigUtil;
import com.stimulus.archiva.exception.*;

public class ArchiveFilter extends EmailFilter implements Props {

		protected static Logger logger = Logger.getLogger(ArchiveFilter.class);
	
		protected static final String archiveInboundKey 		= "archive.inbound";
	    protected static final String archiveOutboundKey 		= "archive.outbound";
	    protected static final String archiveInternalKey 		= "archive.internal";
	    protected static final String defaultArchiveInbound		= "yes";
	    protected static final String defaultArchiveOutbound 	= "yes";
	    protected static final String defaultArchiveInternal 	= "yes";
	    
	    protected static final String archiveRuleKey				= "archive.newrule";
   		protected static final String archiveRuleActionKey			= "archive.newrule.action";
     	protected static final String archiveRuleOperatorKey		= "archive.newrule.operator";
	    protected static final String archiveRuleClauseKey			= "archive.newrule.clause";
	    		
		protected static final String archiveClauseFieldKey 			= "field";
		protected static final String archiveClauseConditionKey 		= "condition";
		protected static final String archiveClauseValueKey 			= "value"; 
		 
		protected static final String defaultArchiveRuleAction 			= "archive";
		protected static final String defaultArchiveRuleOperator		= "all";
		protected static final String defaultArchiveClauseField 		= "subject";
		protected static final String defaultArchiveClauseCondition 	= "contains";
		protected static final String defaultArchiveClauseValue			= "";
		
		protected boolean 	 archiveInbound;
     	protected boolean 	 archiveOutbound;
     	protected boolean 	 archiveInternal;
       
     	public enum Location { INTERNAL, EXTERNAL };
     	public enum Action { SKIP, ARCHIVE, NOARCHIVE };  // action_label_

        public void setArchiveInbound(boolean archiveInbound) { this.archiveInbound = archiveInbound; }

        public void setArchiveOutbound(boolean archiveOutbound) { this.archiveOutbound = archiveOutbound; }

        public void setArchiveInternal(boolean archiveInternal) { this.archiveInternal = archiveInternal; }

        public boolean getArchiveInbound() {return archiveInbound; }

        public boolean getArchiveOutbound() {return archiveOutbound; }

        public boolean getArchiveInternal() {return archiveInternal; }
        

  	    public List<FilterRule> getArchiveRules() {
  	  	return filterRules;
  	    }

  	    public FilterRule getArchiveRule(int index) {
  	    	return filterRules.get(index);
  	    }

  	    public void clearAllArchiveRules() {
  	    	filterRules.clear();
  	    }
  	    
  	    public void addArchiveRule(Operator operator, Action action) throws ConfigurationException {
  	    	filterRules.add(new FilterRule(operator,action.toString().toLowerCase(Locale.ENGLISH)));
  	    }
  	    public void newArchiveRule() throws ConfigurationException {
  	    	FilterRule fr = new FilterRule(Operator.ALL,Action.ARCHIVE.toString().toLowerCase());
  	    	fr.addClause(new FilterClause("subject",Condition.CONTAINS,""));
  	    	filterRules.add(fr);
  	    }
  	  
  	    public void deleteArchiveRule(int id) {
  	    	filterRules.remove(id);
  	    }

  	    
        protected Action checkBasicRules(Email email, Domains domains) {
  	       logger.debug("checkBasicRules() {archiveOutbound='"+archiveOutbound+"', archiveInbound='"+archiveInbound+"', archiveInternal='"+archiveInternal+"'}");
  	      try {

  	           if (archiveOutbound && archiveInternal && archiveInbound) {
  	                logger.debug("message will be archived. basic archive rules are set to archive everything");
  	                return Action.ARCHIVE;
  	           }
  	           if (!archiveOutbound && !archiveInternal && !archiveInbound)
  	           {
  	               logger.debug("message will not be archived. basic archive rules stipulate no archiving");
  	               return Action.NOARCHIVE;
  	           }

  	           Address[] recipients = email.getAllRecipients();
  	           Address[] from 		= email.getFrom();

 	 	       if (archiveOutbound &&
 	 	           findAddress(from, domains)==Location.INTERNAL &&
 	 	           findAddress(recipients, domains)==Location.EXTERNAL) {
 	 	           try {
 	 	               logger.debug("message is outbound and will be archived {"+email+"}");
 	 	          	} catch (Exception e) {}

 	 	           return Action.ARCHIVE;
 	 	       } else
 	 	           logger.debug("message is not outbound");

 	 	      if (archiveInbound &&
 		 	           findAddress(from, domains)==Location.EXTERNAL &&
 		 	           findAddress(recipients, domains)==Location.INTERNAL) {
 	 	         try {
 	 	               logger.debug("message is inbound and will be archived {"+email+"}");
 	 	         } catch (Exception e) {}
 	 	          return Action.ARCHIVE;
 	 	      } else
 	 	           logger.debug("message is not inbound");


 	 	      if (archiveInternal &&
 	 	               findAddress(from, domains)==Location.INTERNAL &&
 		 	           findAddress(recipients, domains)==Location.INTERNAL) {
 	 	         try {
 	 	               logger.debug("message is internal and will be archived {"+email+"}");
 	 	         } catch (Exception e) {}
 	 	         return Action.ARCHIVE;
 	 	      } else
 	 	           logger.debug("message is not internal");

 	 	      try {
 	               logger.debug("none of the basic archiving rules apply. message will not be archived {"+email+"}");
 	         } catch (Exception e) {}

 	 	      return Action.NOARCHIVE;

 	       } catch (Exception e) {
 	           logger.debug("failed to check basic archiving rules. will default to archive. cause:",e);
 	           return Action.ARCHIVE; }

  	   }
        
  	  protected Action checkAdvancedRules(Email email) {
	      
	        for (FilterRule rule : filterRules) {
	        	boolean matched = rule.match(email);
	        	if (!matched) continue;
	        	String actionStr = rule.getAction();
	        	Action action = Action.valueOf(actionStr.toUpperCase(Locale.ENGLISH));
	        	if (action==null) {
	        		logger.error("invalid archive rule action specified {"+rule+","+action+"}");
	        		return null;
	        	}
	            
	            switch(action) {
	            	case ARCHIVE:
	            	    try {
	            	        logger.debug("advanced archive rule matched. message will be archived {"+rule+","+action+","+email+"}");
	            	    } catch (Exception e) {}
	            	    return action;
	            	case NOARCHIVE:
	            	    try {
		            	    logger.debug("advanced archive rule matched. message will not be archived {"+rule+","+action+","+email+"}");
		            	 } catch (Exception e) {}
	            	    return action;
	            	case SKIP: continue;
	            }
	        }
	        logger.debug("none of the advanced rules matched. basic rules apply.");
	        return Action.SKIP;
	  }
	    /* we check the advanced rules first, if no match, then basic rules apply */
	    public Action shouldArchive(Email email, Domains domains) {
	        logger.debug("shouldArchive()");

	        Action action = checkAdvancedRules(email);

	        switch(action) {
	        	case ARCHIVE: return Action.ARCHIVE;
	        	case NOARCHIVE: return Action.NOARCHIVE;
	        	default: return checkBasicRules(email,domains);
	        }
	    }
	    
  	    protected Location findAddress(Address[] addresses, Domains domain) throws MessageException {
  		
           logger.debug("findAddress()");

           try {
 	           for (int account=0;account<addresses.length;account++) {
 	      
 	        	   Address address = addresses[account];
 	               if (address==null) 
 	                   return Location.INTERNAL;

 	               if (address instanceof NewsAddress)
 	                   return Location.EXTERNAL; 

 	               String dom = getDomain(((InternetAddress)address).getAddress());

 	               if (dom==null)
                       return Location.INTERNAL; 

 	               for (Iterator<Domains.Domain> icount=domain.getDomains().iterator();icount.hasNext();) {
 	                   Domains.Domain d = icount.next();
 	                   if (Compare.equalsIgnoreCase(dom, d.getName()))
 	                	   return Location.INTERNAL; 
 	               }
 	           }
               return Location.EXTERNAL; 
           } catch (Exception e) {
               throw new MessageException(e.toString(),e,logger);
           }
       }
  	    
 	   protected String getDomain(String addr) {

	       	int i = 0,start = 0;
	       	if ((i = addr.indexOf('@', start)) >= 0) {
	       	    if (i == addr.length() - 1)
	       		 return null;
	       	    return addr.substring(i + 1);
	 	    } else
	 	        return null;
	   }
 	 
  		public void saveSettings(String prefix, Settings prop, String suffix) {
  			prop.setProperty(archiveInboundKey,ConfigUtil.getYesNo(getArchiveInbound()));
  			prop.setProperty(archiveOutboundKey,ConfigUtil.getYesNo(getArchiveOutbound()));
  			prop.setProperty(archiveInternalKey,ConfigUtil.getYesNo(getArchiveInternal()));
  			int r = 1;
  			for (FilterRule rule : filterRules) {
   	  		  int c = 1;
   	  		  prop.setProperty(archiveRuleActionKey+"."+r,rule.getAction());
   	  		  prop.setProperty(archiveRuleOperatorKey+"."+r,rule.getOperator().toString().toLowerCase(Locale.ENGLISH));
   	  		  for (FilterClause clause : rule.getFilterClauses()) {
   	  			prop.setProperty(archiveRuleClauseKey+"."+r+"."+archiveClauseFieldKey+"."+c,clause.getField());
   	  			prop.setProperty(archiveRuleClauseKey+"."+r+"."+archiveClauseConditionKey+"."+c,clause.getCondition().toString().toLowerCase(Locale.ENGLISH));
   	  			prop.setProperty(archiveRuleClauseKey+"."+r+"."+archiveClauseValueKey+"."+c++,clause.getValue());
   	  		  }
   	  		  r++;
  			}
   	  	}
  			
  		
  		public boolean loadSettings(String prefix, Settings prop, String suffix) {
  			setArchiveInbound(ConfigUtil.getBoolean(prop.getProperty(archiveInboundKey),defaultArchiveInbound));
  			setArchiveOutbound(ConfigUtil.getBoolean(prop.getProperty(archiveOutboundKey),defaultArchiveOutbound));
  			setArchiveInternal(ConfigUtil.getBoolean(prop.getProperty(archiveInternalKey),defaultArchiveInternal));
  			filterRules.clear();
  			int r = 1;
  			do {
  				String ruleActionProp = prop.getProperty(archiveRuleActionKey+"."+r);
  				String ruleOperatorProp = prop.getProperty(archiveRuleOperatorKey+"."+r);
  				
  				if (ruleActionProp==null || ruleOperatorProp==null)
  					break;
  				
  				String ruleAction = ConfigUtil.getString(ruleActionProp,defaultArchiveRuleAction);
  				String ruleOperator = ConfigUtil.getString(ruleOperatorProp,defaultArchiveRuleOperator);
  				
  				Operator operator = Operator.valueOf(ruleOperator.trim().toUpperCase(Locale.ENGLISH));
  			
  				FilterRule filterRule = new FilterRule(operator,ruleAction);
  				int c = 1;
  				
  				do {
  					
  					String clauseFieldProp 		=  prop.getProperty(archiveRuleClauseKey+"."+r+"."+archiveClauseFieldKey+"."+c);
  					String clauseConditionProp 	=  prop.getProperty(archiveRuleClauseKey+"."+r+"."+archiveClauseConditionKey+"."+c);
  					String clauseValueProp 		=  prop.getProperty(archiveRuleClauseKey+"."+r+"."+archiveClauseValueKey+"."+c);
  					
  					if (clauseFieldProp == null || clauseConditionProp == null || clauseValueProp == null)
  						break;
  					
  					String clauseField = ConfigUtil.getString(clauseFieldProp,defaultArchiveClauseField);
  					String clauseCondition = ConfigUtil.getString(clauseConditionProp,defaultArchiveClauseCondition);
  					String clauseValue = ConfigUtil.getString(clauseValueProp,defaultArchiveClauseValue);
  					Condition condition = Condition.valueOf(clauseCondition.trim().toUpperCase(Locale.ENGLISH));
  					FilterClause clause = new FilterClause(clauseField,condition,clauseValue);
  					filterRule.addClause(clause);
  					c++;
  				} while (true);

  				if (c>1)
  					filterRules.add(filterRule);
  				
  				r++;
  				
  			} while (true);
  			return true;
  		}


	
	
}
