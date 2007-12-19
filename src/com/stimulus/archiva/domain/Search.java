
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
import java.security.Principal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.stimulus.archiva.domain.fields.EmailFieldValue;
import com.stimulus.archiva.exception.MessageSearchException;

public abstract class Search implements java.io.Serializable 
{
	private static final long serialVersionUID = -6424054985456683316L;
	protected static Logger logger = Logger.getLogger(Search.class);
	public enum Type { STANDARD }; // searchresults.type_
	public enum Attachment { EITHER, ATTACH, NOATTACH }; // searchresults.attachment_
	public enum Priority { ANY, HIGHEST, HIGH, NORMAL, LOW, LOWEST }; // searchresults.priority_
	public enum Flag { ANY, DELETED, SEEN, ANSWERED, FLAGGED, RECENT, DRAFT }; //searchresults.flag_
	public enum DateType { SENTDATE,ARCHIVEDATE  }; 
	
	protected String			searchQuery	  = "";
	protected ArrayList<Result> results	  	  = new ArrayList<Result>();
	protected FetchMessage	    fetchMessage  = null;
    protected int               maxResults    = 0;
    protected Type				searchType	  = Type.STANDARD;			
	protected String            language      = "en"; 
	protected Attachment  		attach  	  = Attachment.EITHER;
	protected Priority          priority	  = Priority.ANY;
	protected Flag				flag		  = Flag.ANY;
	protected Date				after	  	  = null;
	protected Date				before	  	  = null;
	protected String			sortField	  = "archivedate";
	protected boolean			sortOrder	  = true;
	protected Principal 		principal  	  = null;
	protected DateType			dateType 	   = DateType.SENTDATE;
	
	public Search()
	{
		Calendar c1 = Calendar.getInstance(); 
		c1.add(Calendar.DATE,-60);
		c1.set(c1.get(Calendar.YEAR),c1.get(Calendar.MONTH),c1.get(Calendar.DAY_OF_MONTH),1,0,0);
		after = c1.getTime();
		c1.add(Calendar.DATE,61);
		c1.set(c1.get(Calendar.YEAR),c1.get(Calendar.MONTH),c1.get(Calendar.DAY_OF_MONTH),23,59,59);
		before = c1.getTime();
	}
	
	// abstract methods
	
	public abstract void searchMessage() throws MessageSearchException;

	//	 allow for lazy retrieval of message contents

	public static abstract class FetchMessage
	{
		public abstract void fetchMessage(Result result);
	}
	
	public void setPrincipal(Principal principal) {
		this.principal = principal;
	}
	
	public Principal getPrincipal() {
		return principal;
	}
	
    public void setType(Type searchType) {
        this.searchType = searchType;
    }
    
    public Type getType() {
        return this.searchType;
    }
    
	public void setAfter(Date sentAfter)
	{
		this.after = sentAfter;
	}

	public Date getAfter()
	{
		return after;
	}

	public void setBefore(Date sentBefore)
	{
		this.before = sentBefore;
	}
	
	public Date getBefore()
	{
       return before;
	}
	
	public void setDateType(DateType dateType) {
		this.dateType = dateType;
		if (dateType==DateType.ARCHIVEDATE && sortField.equals("sentdate"))
			sortField = "archivedate";
		
		if (dateType==DateType.SENTDATE && sortField.equals("archivedate"))
			sortField = "sentdate";
	}
	
	public DateType getDateType() {
		return dateType;
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

	public abstract class Result
	{
	
    
		public Result() {}

		
		public abstract EmailFieldValue getFieldValue(String key);

		public abstract EmailID getEmailId();

		
	}
	 
}
