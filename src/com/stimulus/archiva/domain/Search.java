
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
import java.text.DateFormat;
import java.util.*;

import org.apache.log4j.Logger;

import com.stimulus.archiva.exception.MessageSearchException;
import com.stimulus.archiva.service.MessageService;
import com.stimulus.util.DecodingUtil;

public abstract class Search 
{
	private static final long serialVersionUID = -6424054985456683316L;
	protected static Logger logger = Logger.getLogger(Search.class.getName());
	public enum Type { STANDARD }; // searchresults.type_
	public enum Attachment { EITHER, ATTACH, NOATTACH }; // searchresults.attachment_
	public enum Priority { ANY, HIGHEST, HIGH, NORMAL, LOW, LOWEST }; // searchresults.priority_
	public enum Flag { ANY, DELETED, SEEN, ANSWERED, FLAGGED, RECENT, DRAFT }; //searchresults.flag_
	protected static final List FIELD_LIST;
	protected static final List FIELD_LABEL_LIST;
	
		static {
		  List<String> fieldList = new LinkedList<String>();
		    fieldList.add("all");
		    fieldList.add("to");
		    fieldList.add("from");
		    fieldList.add("subject");
		    fieldList.add("cc");
		    fieldList.add("bcc");
		    fieldList.add("body");
		    fieldList.add("attachments");
		    FIELD_LIST = Collections.unmodifiableList(fieldList);
		
		    List<String> fieldLabelList = new LinkedList<String>();
		    fieldLabelList.add("field_label_all");
		    fieldLabelList.add("field_label_to");
		    fieldLabelList.add("field_label_from");
		    fieldLabelList.add("field_label_subject");
		    fieldLabelList.add("field_label_cc");
		    fieldLabelList.add("field_label_bcc");
		    fieldLabelList.add("field_label_body");
		    fieldLabelList.add("field_label_attachments");
		    FIELD_LABEL_LIST = Collections.unmodifiableList(fieldLabelList);
		}  
		    
	protected String			searchQuery	  = "";
	protected ArrayList<Result> results	  = new ArrayList<Result>();
	protected FetchMessage	    fetchMessage  = null;
	protected String			userName	  = null;
	protected String			userRole	  = null;
    protected int               maxResults    = 0;
    protected Type				searchType	  = Type.STANDARD;			
	protected String            language      = "en"; 
	protected Attachment  		attach  	  = Attachment.EITHER;
	protected Priority          priority	  = Priority.ANY;
	protected Flag				flag		  = Flag.ANY;
	protected Date				sentAfter	  = null;
	protected Date				sentBefore	  = null;
	protected String			sortField	  = "sentdate";
	protected boolean			sortOrder	  = true;
	protected List<String>      emailAddresses = new ArrayList<String>();
	
	public Search()
	{
	}
	
	// abstract methods
	
	public abstract void searchMessage() throws MessageSearchException;
	
	//	 allow for lazy retrieval of message contents

	public static abstract class FetchMessage
	{
		public abstract void fetchMessage(Result result);
	}
	
    public void setType(Type searchType) {
        this.searchType = searchType;
    }
    
    public Type getType() {
        return this.searchType;
    }
    
	public void setSentAfter(Date sentAfter)
	{
		this.sentAfter = sentAfter;
	}

	public Date getSentAfter()
	{
		return sentAfter;
	}

	public void setSentBefore(Date sentBefore)
	{
		this.sentBefore = sentBefore;
	}
	
	public Date getSentBefore()
	{
       return sentBefore;
	}
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public String getLanguage() {
        return language;
    }
    
    
    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }
    
    public int getMaxResults() {
        return maxResults;
    }
    
    public void setFlag(Flag flag) {
    	this.flag = flag;
    }
    
    public Flag getFlag()  {
    	return flag;
    }

    public void setAttachment(Attachment attach) {
        this.attach = attach;
    }
    
    public Attachment getAttachment() {
        return attach;
    }
    
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
    
    public Priority getPriority() {
        return priority;
    }
    
	public void setSortField(String field)
	{
		this.sortField = field;
	}

	public String getSortField()
	{
		return sortField;
	}

	public String getSearchQuery()
	{
		return searchQuery;
	}

	public boolean getSortOrder()
	{
		return sortOrder;
	}

	public void setSortOrder(boolean sortOrder)
	{
		this.sortOrder = sortOrder;
	}

	public void setUserName(String userName)
	{
		this.userName = userName;
	}

	public String getUserName()
	{
		return userName;
	}

	public void setUserRole(String userRole)
	{
		this.userRole = userRole;
	}

	public String getUserRole()
	{
		return userRole;
	}
	
	public void setEmailAddresses(List<String> emailAddresses) {
		this.emailAddresses = emailAddresses;
	}
	
	public List<String> getEmailAddresses() {
		return emailAddresses;
	}
	
	public void addMessage(EmailID emailId, float score, String subject, String toAddresses, String fromAddress, Date sentDate, int size, boolean hasAttachment, int priority)
	{
		results.add(new Result(emailId, score, subject, toAddresses, fromAddress, sentDate, size,hasAttachment,priority));
	}

	public void addMessage(EmailID emailId, float score)
	{
		results.add(new Result(emailId, score));
	}

	public Iterator getResults()
	{
		return results.iterator();
	}

	public List<Result> getResultsList()
	{
		return (List<Result>) results;
	}

	public void clearResults()
	{
		results.clear();
	}

	public int getSize()
	{
		return results.size();
	}

	public void setFetchMessage(FetchMessage fetchMessage)
	{
		this.fetchMessage = fetchMessage;
	}

	public class Result
	{
		EmailID	emailId;
		String	toAddresses;
		String	fromAddress;
		Date	sentDate;
		int		size;
		String	subject;
		float	score;
        boolean hasAttachment = false;
		boolean	fetchedMessage	= false;
        int priority = 3; // normal

		public Result(EmailID emailId, float score)
		{
			//logger.debug("adding search result {emailid='"+emailId.getUniqueID()+"'}");
			this.emailId = emailId;
			this.score = score;

		}

		public Result(EmailID emailId, float score, String subject, String toAddresses, String fromAddress, Date sentDate, int size, boolean hasAttachment, int priority)
		{
			this.emailId = emailId;
			this.toAddresses = toAddresses;
			this.fromAddress = fromAddress;
			this.sentDate = sentDate;
			this.size = size;
			this.subject = subject;
			this.score = score;
			this.hasAttachment = hasAttachment;
            this.priority = priority;
		}

		public EmailID getEmailId()
		{
			return emailId;
		}

		public String getSubject()
		{
			fetchMessage();
			return DecodingUtil.decodeWord(subject);
		}

		public void setSubject(String subject)
		{
			this.subject = subject;
		}

		public void setToAddresses(String toAddresses)
		{
			this.toAddresses = toAddresses;
		}

		public void setFromAddress(String fromAddress)
		{
			this.fromAddress = fromAddress;
		}

		public void setSize(int size)
		{
			this.size = size;
		}
        
        public void setHasAttachment(boolean hasAttachment) {
            this.hasAttachment = hasAttachment;
        }
        
        public boolean getHasAttachment() {
        	fetchMessage();
            return hasAttachment;
        }
        
        public void setPriority(int priority) {
            this.priority = priority;
        }

        public int getPriority() {
        	fetchMessage();
            return priority;
        }
        
		public void setSentDate(Date sentDate)
		{
			this.sentDate = sentDate;
		}

		public String getToAddresses()
		{
			fetchMessage();
			// String address = "";
			if (toAddresses == null) return "";
			return DecodingUtil.decodeWord(toAddresses);
		}

		public String getFromAddress()
		{
			fetchMessage();

			if (fromAddress != null)
			{
				return DecodingUtil.decodeWord(fromAddress);
			} else return "";
		}

		public Date getSentDate()
		{
			fetchMessage();
			return sentDate;
		}

		public int getSize()
		{
			fetchMessage();
			return size;
		}

		public Float getScore()
		{
			return score;

		}

		private void fetchMessage()
		{
			if (fetchMessage == null) return;
			if (!fetchedMessage)
			{
				fetchMessage.fetchMessage(this);
				fetchedMessage = true;
			}
		}
	}
	 
}
