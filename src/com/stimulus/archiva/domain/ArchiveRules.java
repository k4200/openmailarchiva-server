
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
import com.stimulus.archiva.exception.*;
import org.apache.log4j.Logger;
import javax.mail.internet.*;
import javax.mail.*;
import java.io.Serializable;
import java.util.*;

 public class ArchiveRules  {

	 	private static final long serialVersionUID = -7175192678619225338L;

		protected static Logger logger = Logger.getLogger(Email.class.getName());

     	protected boolean 	 archiveInbound;
     	protected boolean 	 archiveOutbound;
     	protected boolean 	 archiveInternal;
        protected LinkedList<Rule> archiveRules = new LinkedList<Rule>();

        public enum Priority { HIGHEST, HIGHER, LOWER, LOWEST  };
        public enum Action { SKIP, ARCHIVE, NOARCHIVE };  // action_label_
        public enum Field { ALL, FROM, SUBJECT, TO, CC, BCC }; // field_label_
        public enum Location { INTERNAL, EXTERNAL };
     
        public void setArchiveInbound(boolean archiveInbound) { this.archiveInbound = archiveInbound; }

        public void setArchiveOutbound(boolean archiveOutbound) { this.archiveOutbound = archiveOutbound; }

        public void setArchiveInternal(boolean archiveInternal) { this.archiveInternal = archiveInternal; }

        public boolean getArchiveInbound() {return archiveInbound; }

        public boolean getArchiveOutbound() {return archiveOutbound; }

        public boolean getArchiveInternal() {return archiveInternal; }

 	   public List<Rule> getArchiveRules() {
 	  	return archiveRules;
 	    }

 	    public Rule getArchiveRule(int index) {
 	    	return (Rule)archiveRules.get(index);
 	    }

 	    public void clearAllArchiveRules() {
 	     archiveRules.clear();
 	    }
 	    
 	    public void addArchiveRule() throws ConfigurationException {
 	    	addArchiveRule(Action.values()[0],Field.values()[0],"");
 	    }
 	    public void addArchiveRule(Action action, Field field, String regex) throws ConfigurationException {
 	    	Rule af = new Rule(action,field,regex);
 	    	archiveRules.add(af);
 	    }

 	    public void deleteArchiveRule(int id) {
 	    	archiveRules.remove(id);
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
	           e.printStackTrace();
	           return Action.ARCHIVE; }

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

	               for (Iterator icount=domain.getDomains().iterator();icount.hasNext();) {
	                   Domains.Domain d = (Domains.Domain)icount.next();
	                   if (dom.equalsIgnoreCase(d.getName()))
	                       return Location.INTERNAL; 
	               }
	           }
              return Location.EXTERNAL; 
          } catch (Exception e) {
              throw new MessageException(e.toString(),e,logger);
          }
      }

 	  protected Action checkAdvancedRules(Email email) {
 	        Iterator i = archiveRules.iterator();
	        while (i.hasNext()) {
	            Rule ar = (Rule)i.next();
	            Action action = ar.shouldArchive(email);
	            switch(action) {
	            	case ARCHIVE:
	            	    try {
	            	        logger.debug("advanced archive rule matched. message will be archived {"+ar+","+email+"}");
	            	    } catch (Exception e) {}
	            	    return action;
	            	case NOARCHIVE:
	            	    try {
		            	    logger.debug("advanced archive rule matched. message will not be archived {"+ar+","+email+"}");
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

 	   public void setArchiveRulePriority(int id, Priority priority)  {
	 	  	LinkedList<Rule> list = archiveRules;
	 	  	Rule ar = (Rule)archiveRules.get(id);
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

 	   protected String getDomain(String addr) {

	       	int i = 0,start = 0;
	       	if ((i = addr.indexOf('@', start)) >= 0) {
	       	    if (i == addr.length() - 1)
	       		 return null;
	       	    return addr.substring(i + 1);
	 	    } else
	 	        return null;
 	   }


       public class Rule {
    	   
	 	  	protected Action action = Action.SKIP;
	 	  	protected Field field 	= Field.ALL;
	 	  	protected String regex;

	 	  	Rule(Action action, Field field, String regex) throws ConfigurationException {
	 	  		setAction(action);
	 	  		setField(field);
	 	  		setRegEx(regex);
	 	  	}
	 	  	
	 	  	public void setAction(Action action) throws ConfigurationException {
	 	  	    logger.debug("set archive rule action {action='"+action+"'}");
	 	  	    this.action = action;
	 	  	}
	 	  	
	 	  	public Action getAction() { return action; }
	 	  	
	 	  	public Field getField() { return field; }

	 	  	public void setField(Field field) throws ConfigurationException{
	 	  		logger.debug("set archive rule field {field='"+field+"'}");
	 	  		this.field = field;
	 	  	}
	 	  	
	 	  	public String getRegEx() { return regex; }
	 	  	
	 	  	public void setRegEx(String regex) {
	 	  	 logger.debug("set archive rule criterion {criterion='"+regex+"'}");
	 	  	 this.regex = regex;
	 	  	 }

	 	  	public Action shouldArchive(Email email) {
	 	  		logger.debug("shouldArchive {action='"+action+"',field='"+field+"',regex='"+regex+"'}");
	 	  	 	
	 	  	 	switch(field) {
 	  	        	case FROM:
 	  	        	    	//logger.debug("*****archive rule from address:"+email.getFromAddress().matches(regex));
 	  	        	    	return email.getFromAddress(Email.DisplayMode.EMAIL_ONLY).matches(regex) ? action : Action.SKIP;

 	  	        	case SUBJECT: try {
 	  	        	    		return email.getSubject().matches(regex) ? action : Action.SKIP;
 	  	        			} catch (Exception e) {
 	  	        			    logger.debug("failed to process archive rule. exception occurred. will archive anyway. Cause:",e);
 	  	        			}

 	  	        	case TO: return email.getToAddresses(Email.DisplayMode.EMAIL_ONLY).matches(regex) ? action : Action.SKIP;

 	  	        	case CC: return email.getCCAddresses(Email.DisplayMode.EMAIL_ONLY).matches(regex) ? action : Action.SKIP;

 	  	        	case BCC: return email.getBCCAddresses(Email.DisplayMode.EMAIL_ONLY).matches(regex)  ? action : Action.SKIP;
	        		
 	  	        	case ALL: try {
			        		    return (email.getFromAddress(Email.DisplayMode.EMAIL_ONLY).matches(regex)) ||
			        					   (email.getSubject().matches(regex)) ||
			        					   (email.getToAddresses(Email.DisplayMode.EMAIL_ONLY).matches(regex)) ||
			        					   (email.getCCAddresses(Email.DisplayMode.EMAIL_ONLY).matches(regex)) ||
			        					   (email.getBCCAddresses(Email.DisplayMode.EMAIL_ONLY).matches(regex)) ? action : Action.SKIP;
		        			} catch (Exception e) {
		        			    logger.debug("failed to process archive rule. exception occurred. Cause:",e);
		        			}
	        		default: return action;
	 	  	    }
	 	  	}

	 	  	public String toString() {
	 	  	    return "action='"+action+"',field='"+field+"',regex='"+regex+"'";
	 	  	}
       }
}