
/* Copyright (C) 2005 Jamie Angus Band 
 * This software program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.stimulus.archiva.domain;
import com.stimulus.archiva.exception.*;
import org.apache.log4j.Logger;
import java.util.*;

 public class ArchiveRules {

		 public static final List RULE_FIELD_LIST;
		 public static final List RULE_ACTION_LIST;
		 public static final int PRIORITY_HIGHER  = 1;
		 public static final int PRIORITY_HIGHEST = 2;
		 public static final int PRIORITY_LOWER   =-1;
		 public static final int PRIORITY_LOWEST  =-2;


     	protected static Logger logger = Logger.getLogger(Email.class.getName());

     	protected boolean 	archiveInbound;
     	protected boolean 	archiveOutbound;
     	protected boolean 	archiveInternal;
        protected List		archiveRules = new ArrayList();

        protected static final int SKIP = -1;
        protected static final int ARCHIVE = 0;
        protected static final int NOARCHIVE = 1;


        static {
            List ruleFieldList = new ArrayList();
     	    ruleFieldList.add("all fields");
     	    ruleFieldList.add("from");
     	    ruleFieldList.add("subject");
     	    ruleFieldList.add("to");
     	    ruleFieldList.add("cc");
     	    ruleFieldList.add("bcc");

     	   RULE_FIELD_LIST = Collections.unmodifiableList(ruleFieldList);

     	   List ruleActionList = new ArrayList();
     	    ruleActionList.add("archive");
     	    ruleActionList.add("do not archive");

     	   RULE_ACTION_LIST = Collections.unmodifiableList(ruleActionList);

        }

        public void setArchiveInbound(boolean archiveInbound) { this.archiveInbound = archiveInbound; }

        public void setArchiveOutbound(boolean archiveOutbound) { this.archiveOutbound = archiveOutbound; }

        public void setArchiveInternal(boolean archiveInternal) { this.archiveInternal = archiveInternal; }

        public boolean getArchiveInbound() {return archiveInbound; }

        public boolean getArchiveOutbound() {return archiveOutbound; }

        public boolean getArchiveInternal() {return archiveInternal; }

 	   public List getArchiveRules() {
 	  	return archiveRules;
 	    }

 	    public Rule getArchiveRule(int index) {
 	    	return (Rule)archiveRules.get(index);
 	    }

 	    public void clearAllArchiveRules() {
 	     archiveRules.clear();
 	    }


 	    public void addArchiveRule(String action, String field, String regex) throws ConfigurationException {
 	    	Rule af = new Rule(action,field,regex);
 	      archiveRules.add(af);
 	    }

 	    public void deleteArchiveRule(int id) {
 	    	archiveRules.remove(id);
 	    }

 	   protected boolean checkBasicRules(Email email, Domains domains) {
 	       logger.debug("checkBasicRules() {archiveOutbound='"+archiveOutbound+"', archiveInbound='"+archiveInbound+"', archiveInternal='"+archiveInternal+"'}");
 	      try {

 	           if (archiveOutbound && archiveInternal && archiveInbound) {
 	                logger.debug("message will be archived. basic archive rules are set to archive everything");
 	                return true;
 	           }
 	           if (!archiveOutbound && !archiveInternal && !archiveInbound)
 	           {
 	               logger.debug("message will not be archived. basic archive rules stipulate no archiving");
 	               return archiveOutbound; // shortcut
 	           }

 	           String[] recipients = email.getAllRecipientsList();
	 	       String[] from = email.getFromList();

	 	       if (archiveOutbound &&
	 	           findAddress(true,from, domains) &&
	 	           findAddress(false,recipients, domains)) {
	 	           try {
	 	               logger.debug("message is outbound and will be archived {"+email+"}");
	 	          	} catch (Exception e) {}

	 	           return true;
	 	       } else
	 	           logger.debug("message is not outbound");

	 	      if (archiveInbound &&
		 	           findAddress(false,from, domains) &&
		 	           findAddress(true,recipients, domains)) {
	 	         try {
	 	               logger.debug("message is inbound and will be archived {"+email+"}");
	 	         } catch (Exception e) {}
	 	          return true;
	 	      } else
	 	           logger.debug("message is not inbound");


	 	      if (archiveInternal &&
	 	               findAddress(true,from, domains) &&
		 	           findAddress(true,recipients, domains)) {
	 	         try {
	 	               logger.debug("message is internal and will be archived {"+email+"}");
	 	         } catch (Exception e) {}
	 	         return true;
	 	      } else
	 	           logger.debug("message is not internal");

	 	      try {
	               logger.debug("none of the basic archiving rules apply. message will not be archived {"+email+"}");
	         } catch (Exception e) {}

	 	      return false;

	       } catch (Exception e) {
	           logger.debug("failed to check basic archiving rules. will default to archive. cause:",e);
	           e.printStackTrace();
	           return true; }

 	   }

 	  protected int checkAdvancedRules(Email email) {
 	        Iterator i = archiveRules.iterator();
	        while (i.hasNext()) {
	            Rule ar = (Rule)i.next();
	            int result = ar.shouldArchive(email);
	            switch(result) {
	            	case ARCHIVE:
	            	    try {
	            	        logger.debug("advanced archive rule matched. message will be archived {"+ar+","+email+"}");
	            	    } catch (Exception e) {}
	            	    return ARCHIVE;
	            	case NOARCHIVE:
	            	    try {
		            	    logger.debug("advanced archive rule matched. message will not be archived {"+ar+","+email+"}");
		            	 } catch (Exception e) {}
	            	    return NOARCHIVE;
	            	default: continue;
	            }
	        }
	        logger.debug("none of the advanced rules matched. basic rules apply.");
	        return SKIP;
 	  }
 	    /* we check the advanced rules first, if no match, then basic rules apply */
 	    public boolean shouldArchive(Email email, Domains domains) {
 	        logger.debug("shouldArchive()");

 	        int outcome = checkAdvancedRules(email);

 	        switch(outcome) {
 	        	case ARCHIVE: return true;
 	        	case NOARCHIVE: return false;
 	        	default: return checkBasicRules(email,domains);
 	        }
 	    }

 	   public void setArchiveRulePriority(int id, int priority)  {
	 	  	List list = archiveRules;
	 	  	Rule ar = (Rule)archiveRules.get(id);
	 	   	list.remove(ar);
	 	    switch (priority) {
	 		  	    case PRIORITY_HIGHER:	if ((id-1)<=0)
	 		  	    							list.add(0,ar);
	 										else
	 											list.add(id-1,ar);
	 		  	    					 	break;
	 		  	    case PRIORITY_LOWER: 	if ((id+1)>=list.size())
	 		  	    							list.add(ar);
	 		  	    						else
	 		  	    							list.add(id+1,ar);
	 		  	                         	break;
	 		  	    case PRIORITY_HIGHEST: 	list.add(0,ar);
	 		  	     						break;
	 		  	    case PRIORITY_LOWEST:   list.add(ar);
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

 	   protected String getAddress(String fullAddress) {
 	      int start = fullAddress.lastIndexOf('<');
 	      int end = fullAddress.lastIndexOf('>');
 	      logger.debug("start:"+start+" end:"+end);
 	      if (start==-1 || end==-1)
	          return null;
 	      int lenpos = fullAddress.length() - 1;
 	     logger.debug("start:"+start+" end:"+end+" lenpos:"+lenpos);
 	      if (start+1>lenpos) {
 	          logger.debug("start<>end");
 	          return null;
 	      }
 	      return fullAddress.substring(start+1,end).trim();
 	   }


       protected boolean findAddress(boolean internal, String[] addresses, Domains domain) throws MessageException{

           logger.debug("findAddress {internal='"+internal+"'}");

           try {


	           for (int account=0;account<addresses.length;account++) {

	               String address = getAddress(addresses[account]);

	               //logger.debug("address:" + address + " from "+addresses[account]);

	               if (address==null) // internal address
	                   return internal; //false;

	               if (address.matches("news:"))
	                   return !internal; //true;

	               String dom = getDomain(address);

	               logger.debug("dom:" + dom);

                   if (dom==null)
                       return internal; // false; //  internal address

	               for (Iterator icount=domain.getDomains().iterator();icount.hasNext();) {
	                   Domains.Domain d = (Domains.Domain)icount.next();
	                   //logger.debug("loggername:"+d.getName());
	                   if (dom.equalsIgnoreCase(d.getName()))
	                       return internal; // false;  // internal address
	               }
	           }
               return !internal; // true;
           } catch (Exception e) {
               throw new MessageException(e.toString(),e,logger);
           }
       }


       public class Rule {
	 	  	protected int action = SKIP;
	 	  	protected int field = 0;
	 	  	protected String regex;

	 	  	Rule(String action, String field, String regex) throws ConfigurationException {
	 	  		setAction(action);
	 	  		setField(field);
	 	  		setRegEx(regex);
	 	  	}
	 	  	public void setAction(String action) throws ConfigurationException {
	 	  	    logger.debug("set archive rule action {action='"+action+"'}");
	 	  	    int temp = RULE_ACTION_LIST.indexOf(action);
	 	  	    if (temp<0)
	 	  	        throw new ConfigurationException("invalid archive rule action specified",logger);
	 	  	    this.action = temp;
	 	  	}
	 	  	public String getAction() { return (String)RULE_ACTION_LIST.get(action); }
	 	  	public String getField() { return (String)RULE_FIELD_LIST.get(field); }

	 	  	public void setField(String field) throws ConfigurationException{
	 	  	 logger.debug("set archive rule field {field='"+field+"'}");
	 	  	    int temp = RULE_FIELD_LIST.indexOf(field);
		  	    if (temp<0)
		  	        throw new ConfigurationException("invalid archive rule action specified",logger);

	 	  	    this.field = temp;

	 	  	}
	 	  	public String getRegEx() { return regex; }
	 	  	public void setRegEx(String regex) {
	 	  	 logger.debug("set archive rule criterion {criterion='"+regex+"'}");
	 	  	 this.regex = regex;
	 	  	 }

	 	  	/* return -1 if no action applies
	 	  	 * return 0 if do not archive
	 	  	 * return 1 if archive
	 	  	 */
	 	  	public int shouldArchive(Email email) {
	 	  	 logger.debug("shouldArchive {action='"+action+"',field='"+field+"',regex='"+regex+"'}");


	 	  	        switch(field) {
	 	  	        	case 1:
	 	  	        	    	//logger.debug("*****archive rule from address:"+email.getFromAddress().matches(regex));
	 	  	        	    	return email.getFromAddress().matches(regex) ? action : SKIP;

	 	  	        	case 2: try {
	 	  	        	    		return email.getSubject().matches(regex) ? action : SKIP;
	 	  	        			} catch (Exception e) {
	 	  	        			    logger.debug("failed to process archive rule. exception occurred. will archive anyway. Cause:",e);
	 	  	        			}

	 	  	        	case 3: return email.getToAddresses().matches(regex) ? action : SKIP;

	 	  	        	case 4: return email.getCCAddresses().matches(regex) ? action : SKIP;

	 	  	        	case 5: return email.getBCCAddresses().matches(regex)  ? action : SKIP;
		        		case 0: try {
				        		    return (email.getFromAddress().matches(regex)) ||
				        					   (email.getSubject().matches(regex)) ||
				        					   (email.getToAddresses().matches(regex)) ||
				        					   (email.getCCAddresses().matches(regex)) ||
				        					   (email.getBCCAddresses().matches(regex)) ? action : SKIP;
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