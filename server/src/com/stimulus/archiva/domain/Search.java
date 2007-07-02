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

import java.io.Serializable;
import java.text.DateFormat;
import java.util.*;

public class Search implements Serializable {

  /* Private Fields */

  protected String    searchQuery = "";
  protected ArrayList results = new ArrayList();
  protected FetchMessage fetchMessage = null;
  protected Date sentAfter = null; 
  protected Date sentBefore  = null;
  protected String sortField = "sentdate";
  protected boolean sortOrder = true;
  protected String userName = null;
  protected String userRole = null;
  
  
  public Search()  {
  }
  /* JavaBeans Properties */

  
  public void setUserName(String userName) {
      this.userName = userName;
  }
  
  public String getUserName() { return userName; }
  
  public void setUserRole(String userRole) { this.userRole = userRole; }
  
  public String getUserRole() { return userRole;}
  
  public void setSortField(String field) {
      this.sortField = field;
  }
  
  public String getSortField() { 
      return sortField;
  }
  
  public String getSearchQuery() {
    return searchQuery;
  }
  
  public boolean getSortOrder() {
      return sortOrder;
  }
  
  public void setSortOrder(boolean sortOrder) {
      this.sortOrder = sortOrder;
  }

  public void setSearchQuery(String searchQuery) {
    this.searchQuery = searchQuery;
  }
  
  public void setSentAfter(Date sentAfter) {
      this.sentAfter = sentAfter;
  }
  
  public Date getSentAfter() { return sentAfter; }
  
  public void setSentBefore(Date sentBefore) {
      this.sentBefore = sentBefore;
  }
  
  public Date getSentBefore() { return sentBefore; }
  
  

  public void addMessage(EmailID emailId, float score, String subject, String toAddresses, String fromAddress, Date sentDate, int size)
  {
  	results.add(new Result(emailId,score,subject,toAddresses,fromAddress,sentDate,size));
  }

  public void addMessage(EmailID emailId,float score)
  {
  	results.add(new Result(emailId,score));
  }

  public Iterator getResults()
  {
  	return results.iterator();
  }

  public List getResultsList()
  {
  	return (List)results;
  }

  public void clearResults()
  {
  	results.clear();
  }

  public int getSize()
  {
  	return results.size();
  }

  public void setFetchMessage(FetchMessage fetchMessage) {
  	this.fetchMessage = fetchMessage;
  }

  public class Result {
  	EmailID emailId;
  	String toAddresses;
  	String fromAddress;
  	Date sentDate;
  	int size;
  	String subject;
  	float score;

  	boolean fetchedMessage = false;

  	public Result(EmailID emailId,float score) {
  	    this.emailId = emailId;
  	    this.score = score;

  	}

  	public Result(EmailID emailId, float score, String subject, String toAddresses, String fromAddress, Date sentDate, int size) {
  		this.emailId = emailId;
  		this.toAddresses = toAddresses;
  		this.fromAddress = fromAddress;
  		this.sentDate = sentDate;
  		this.size = size;
  		this.subject = subject;
  		this.score = score;

  	}
  	public EmailID getEmailId() { return emailId; }
  	public String getSubject() { fetchMessage(); return subject; }
  	public void setSubject(String subject) { this.subject = subject; }
  	public void setToAddresses(String toAddresses) { this.toAddresses = toAddresses; }
  	public void setFromAddress(String fromAddress) { this.fromAddress = fromAddress; }
  	public void setSize(int size) { this.size = size; }
  	public void setSentDate(Date sentDate) { this.sentDate = sentDate; }

  	public String getToAddresses() {
  	    fetchMessage();
  	    String address = "";
  	    
  	  if (toAddresses == null)
  	      return "";
  
  	  String[] result = toAddresses.split(",");
      for (int x=0; x<result.length; x++) {
          int bracketpos = result[x].indexOf('<',0);
      	  if (bracketpos>2)
      	      address += result[x].substring(0,bracketpos)+",";
      	  else
      	      address = result[x]+",";
      }
      if (address.length()>1)
          address = address.substring(0,address.length()-1);
      return address;
  	}

  	public String getFromAddress() {
  	    fetchMessage();

  	  if (fromAddress!=null) {
	  	  int bracketpos = fromAddress.indexOf('<',0);
	  	  if (bracketpos<3)
	  	      return fromAddress;
	  	  else
	  	    return fromAddress.substring(0,bracketpos);
  	  } else return "";
  	}

  	public String getSentDate() {

  	    fetchMessage();
  	  if (sentDate!=null) {
  	    DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
  	    return formatter.format(sentDate);
  	  } else
  	    return "";
  	}

  	public String getSize() { fetchMessage(); return Integer.toString(size / 1024)+"k"; }
  	public String getScore() { return Float.toString(round(score*100,2)) + "%"; }

    private void fetchMessage() {
        if (fetchMessage==null)
            return;
        if (!fetchedMessage) {
            fetchMessage.fetchMessage(this);
            fetchedMessage = true;
        }

    }
  }
  // allow for lazy retrieval of message contents
  
  
  public static abstract class FetchMessage {
      public abstract void fetchMessage(Result result);
  }



  private static double round(double val, int places) {
	long factor = (long)Math.pow(10,places);
	val = val * factor;
	long tmp = Math.round(val);
	return (double)tmp / factor;
  }


  private static float round(float val, int places) {
	return (float)round((double)val, places);
  }

}
