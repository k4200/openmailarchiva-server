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
package com.stimulus.archiva.presentation;

import com.stimulus.archiva.domain.*;
import com.stimulus.archiva.service.*;
import com.stimulus.struts.*;
import com.stimulus.struts.BaseBean;

import java.util.*;
import java.text.*;
import org.apache.log4j.Logger;
import com.stimulus.archiva.exception.*;
import com.stimulus.archiva.security.realm.CorpGovPrincipal;


public class SearchBean extends BaseBean {
  	
  /* Constants */

 
  /* protected Fields */

  protected Search search;
  protected int page = 1; 
  protected int pageSize = 20;
  protected double searchTime;
  
  /* advanced search attributes */
  

  protected static final List METHOD_LIST;
  protected static final List METHOD_LABEL_LIST;
  protected static final List FIELD_LIST;
  protected static final List FIELD_LABEL_LIST;
  protected static final List OPERATOR_LIST;
  protected static final List OPERATOR_LABEL_LIST;
  protected static final List PAGE_SIZE_LIST;
  protected static final List PAGE_SIZE_LABEL_LIST;
  static {
    List methodList = new ArrayList();
    methodList.add("any");
    methodList.add("all");
    methodList.add("exact");
    methodList.add("none");
    METHOD_LIST = Collections.unmodifiableList(methodList);
    
    List methodLabelList = new ArrayList();
    methodLabelList.add("any of these words");
    methodLabelList.add("all of these words");
    methodLabelList.add("the exact phrase");
    methodLabelList.add("none of these words");
    METHOD_LABEL_LIST = Collections.unmodifiableList(methodLabelList);
    
    List fieldList = new ArrayList();
    fieldList.add("all");
    fieldList.add("to");
    fieldList.add("from");
    fieldList.add("subject");
    fieldList.add("cc");
    fieldList.add("bcc");
    fieldList.add("body");
    fieldList.add("attachments");
    FIELD_LIST = Collections.unmodifiableList(fieldList);
    
    List fieldLabelList = new ArrayList();
    fieldLabelList.add("all");
    fieldLabelList.add("to");
    fieldLabelList.add("from");
    fieldLabelList.add("subject");
    fieldLabelList.add("cc");
    fieldLabelList.add("bcc");
    fieldLabelList.add("body");
    fieldLabelList.add("attachments");
    FIELD_LABEL_LIST = Collections.unmodifiableList(fieldLabelList);
    
    List operatorList = new ArrayList();
    operatorList.add("AND");
    operatorList.add("OR");
    OPERATOR_LIST =  Collections.unmodifiableList(operatorList);
    List operatorLabelList = new ArrayList();
    operatorLabelList.add("AND");
    operatorLabelList.add("OR");
    OPERATOR_LABEL_LIST =  Collections.unmodifiableList(operatorLabelList);
    
    List pageSizeList = new ArrayList();
    pageSizeList.add("10");
    pageSizeList.add("20");
    pageSizeList.add("50");
    pageSizeList.add("100");
    PAGE_SIZE_LIST = Collections.unmodifiableList(pageSizeList);

    List pageSizeLabelList = new ArrayList();
    pageSizeLabelList.add("10");
    pageSizeLabelList.add("20");
    pageSizeLabelList.add("50");
    pageSizeLabelList.add("100");
    PAGE_SIZE_LABEL_LIST = Collections.unmodifiableList(pageSizeLabelList);
        
    
  }
  
  protected boolean hasAttachment;
  protected static final String recvDateFieldName = "sentdate";
  // for advanced search
  protected static Logger logger = Logger.getLogger(SearchBean.class.getName());
  protected static final Logger audit = Logger.getLogger("com.stimulus.archiva.audit");
  protected String[] selectedMessages = new String[0];
  
  protected ArrayList criteria = new ArrayList();
  
  
  /* Constructors */

  public SearchBean()  {
    search = new Search();
    page=1;
    pageSize=20;
    searchTime=0;
    
	newCriteria();
  }
  
  public List getMethods() {
  	return METHOD_LIST; 
  }
  
  public List getMethodLabels() {
  	return METHOD_LABEL_LIST; 
  }
  
  public List getFields() {
  	return FIELD_LIST; 
  }
  
  public List getFieldLabels() {
  	return FIELD_LABEL_LIST; 
  }
  
  public List getOperators() {
  	return OPERATOR_LIST; 
  }
  
  public List getOperatorLabels() {
  	return OPERATOR_LABEL_LIST; 
  }
  
  public List getCriteria() {
  	return criteria;
  }
  
  
  
  public String deleteCriteria(int id) {
    logger.debug("deleteToCriteria() {index='"+id+"'}");  
  	criteria.remove(criteria.get(id));
  	return "reload";
  }
  
  public String newCriteria() {
  	logger.debug("newCriteria()");
  	criteria.add(new Criteria("to"));
  	return "reload";
  }
  

  public void reset() {
  	selectedMessages = new String[0];
  }
  public String[] getSelectedMessages() {
      return selectedMessages;
  }
   
  public void setSelectedMessages(String[] selectedMessages) {
      this.selectedMessages = selectedMessages;
  }
  
  /* JavaBeans Properties */
  
  public int getNoHitsOnPage() {
  	logger.debug("getNoHitsOnPage()");
  	int noHitsOnPage = 0;
  	int noPages = getNoPages();
  	if (noPages<=1) 
  		noHitsOnPage = getTotalHits();
  	else if (page==noPages)
  		noHitsOnPage = getTotalHits() % pageSize;
  	else noHitsOnPage = pageSize;
  	logger.debug("getNoHitsOnPage() {ret='"+noHitsOnPage+"'}");
  	return noHitsOnPage;
  }
  
  public int getPreviousPage() { // doesn't alter current page, merely increments for UI purposes 
   
    int previousPage = page - 1 < 1 ? getNoPages() : page - 1;
  	logger.debug("getPreviousPage() {ret='"+previousPage+"'}");
  	return previousPage;
  }
  
  public int getNextPage() { // doesn't alter current page, merely increments for UI purposes 
    int nextPage = 0;
  	if (page+1>getNoPages())  nextPage = 1; else nextPage = page+1;
	logger.debug("getNextPage() {ret='"+nextPage+"'}");
	return nextPage;
  	
  }
  public int getFirstHitIndex() {
  	int firstHitIndex = page*pageSize - pageSize;
  	logger.debug("getFirstHitIndex() {ret='"+firstHitIndex+"'}");
  	return firstHitIndex;
  }
  
  public int getLastHitIndex() {
  	int lastHitIndex = getFirstHitIndex()+getNoHitsOnPage();
  	logger.debug("getLastHitIndex() {ret='"+lastHitIndex+"'}");
  	return lastHitIndex;
  }
  
  public void setPageSize(int pageSize) { 
  	logger.debug("setPageSize() {pagesize='"+pageSize+"'}"); 
  	this.pageSize = pageSize; 
  }
  
  public int getPageSize() { 
  	logger.debug("getPageSize() {pagesize='"+pageSize+"'}"); 
  	return pageSize; 
  }
  
  public List getPageSizes() {
    	return PAGE_SIZE_LIST; 
    }
    
    public List getPageSizeLabels() {
    	return PAGE_SIZE_LABEL_LIST; 
    }
    
  public int getPage() { 
  	logger.debug("getPage() {page='"+page+"'}"); 
  	return page; 
  }
  
  public void setPage(int page) { 
  	logger.debug("setPage() {page='"+page+"'}"); 
  	if (page>=0 && page<=getNoPages()) 
  		this.page = page; 
  	else logger.debug("failed to set page {page='"+page+"',totalpages = '"+getNoPages()+"'}"); 
  }
  
  public int getNoPages()
  {
  	int searchSize = getTotalHits();
  	//logger.debug("getNoPages <{ search size / page size = " + searchSize / pageSize + " } ");
  	//logger.debug("getNoPages <{ search size % page size = " + searchSize % pageSize+ " } ");
  	int noPages = searchSize / pageSize + ((searchSize % pageSize)>0 ? 1:0);
  	logger.debug("getNoPages() {ret='"+noPages+"'}"); 
  	return noPages;
  }
  public int getTotalHits()
  {
  	int totalHits = search.getSize();
  	logger.debug("getTotalHits() {ret='"+totalHits+"'}");
  	return search.getSize();
  }
  
  public int getMaxViewPage() {
      //int maxPage = (getMinViewPage() + 2) > getNoPages() ? getNoPages() : getMinViewPage() + 2;
      int maxPage = (getMinViewPage() + 9) > getNoPages() ? getNoPages() : getMinViewPage() + 9;
      logger.debug("getMaxViewPage() {ret='"+maxPage+"'}");
      return maxPage;
  }
  
  public int getMinViewPage() {
      //int minpage = page / 3 * 3;
      int minpage = page / 10 * 10;
      if (minpage<1) minpage = 1; 
      logger.debug("getMinViewPage() {ret='"+minpage+"'}");
      return minpage;
  }
  
  public String getSearchQuery() {
  	String searchQuery = "";
  	searchQuery = search.getSearchQuery();
    logger.debug("getSearchQuery() {ret='"+searchQuery+"'}");
    return searchQuery;
  }

  public void setSearchQuery(String searchQuery) {
  	logger.debug("setSearchQuery() {ret='"+searchQuery+"'}");
    search.setSearchQuery(searchQuery);
  }
  
  public String getSearchTime() {
    DecimalFormat nf = new DecimalFormat("0.00");
    String st = nf.format(searchTime / 1000);
    logger.debug("getSearchTime() {ret='"+st+"}");
    return st;
  }
 
  public String getSentAfter() {
  	if (search.getSentAfter()==null)
  	{
  		logger.debug("getSentAfter() {sentafter='null'}");
  		return "";
  	}
  	SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
  	String ra = format.format(search.getSentAfter());
  	logger.debug("getSentAfter() {sentafter='"+ra+"'}");
  	return ra;
  	//return Date  Format.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(sentAfter);
  }
	
  public String getSentBefore() {
  	if (search.getSentBefore()==null)
  	{
  		logger.debug("getSentBefore() {sentbefore='null'}");
  		return "";
  	}
  	SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
  	String rb = format.format(search.getSentBefore());
  	logger.debug("getSentBefore() {sentbefore='"+rb+"'}");
	//return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(sentAfter);
  	return rb;
  }
	
  public void setSentAfter(String sentAfter) { 
  	logger.debug("setSentAfter() {sentafter='"+sentAfter+"'}");

  	 //DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
  	 SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
  	 
  	 format.setLenient(true);
  	 try 
	 {
  	 	if (sentAfter.length()>0)
  	 		search.setSentAfter(format.parse(sentAfter));
  	 } catch(ParseException pe) {
        logger.warn("failed to parse date {sentafter='" + sentAfter+"'}");    	            		
     }
  }
   
  public void setSentBefore(String sentBefore) { 
  	logger.debug("setSentBefore() {sentBefore='"+sentBefore+"'}");
  	
  	//DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
  	SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
  	format.setLenient(true);
 	 try 
	 {
 	    if (sentBefore.length()>0)
 	        search.setSentBefore(format.parse(sentBefore));
 	 } catch(ParseException pe) {
 	 	 logger.warn("failed to parse date {sentBefore='"+sentBefore+"'}");
    }
  }


  public String search() throws MessageException
  {
    SubmitButton button = getSubmitButton();
  	logger.debug("search() {action='"+button.action+"', value='"+button.value+"'}");
  	if (button.action!=null && button.action.equals("newcriteria")) 
  		return newCriteria();
	else if (button.action!=null &&  button.action.equals("deletecriteria")) 
  		return deleteCriteria(Integer.parseInt(button.value));  
  	
  	String searchQuery = "";
  	boolean start = true;
  	Iterator i = criteria.iterator();
  	while (i.hasNext()) {
  		Criteria c = (Criteria)i.next();
  		String constructedQuery = c.getConstructedQuery();
  		if (constructedQuery.length()>0) {
  		    if (!start)
  		    	searchQuery += c.getOperator();
  			if (criteria.size()>1)
  			    searchQuery += " (" + constructedQuery + ") ";
  			else 
  			  searchQuery += constructedQuery;
  			start=false;
  		}
  	}
  	searchQuery = searchQuery.trim();
  		
  	try 
	{
	  	/*
	  	if (sentAfter!=null || sentBefore!=null || searchQuery.equals(""))
	  	{
	  		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
	  		
	  		String recBefore;
	  	  	String recAfter;
	  	  	
	  		if (sentAfter!=null)
	  			recAfter = format.format(sentAfter);
	  		else 
	  			recAfter = format.format(new Date(0));
	  		
	  		if (sentBefore!=null)
	  			recBefore = format.format(sentBefore);
	  		else
	  			recBefore = format.format(new Date()); // curent date/time
	  		
			searchQuery += " "+recvDateFieldName+":["+recAfter+ " TO " + recBefore+"]";	
			logger.info("advanced search query constructed {query='"+ searchQuery+"'}");
	  	}*/
	  	
	  	searchQuery.trim();
	  	search.setSearchQuery(searchQuery);
	  	
  	} catch (Exception e)
	{
  		logger.warn("failed to construct search query {searchquery='"+searchQuery+"'}",e);
  		// do some error handling here
  		return "failure";
	}
  	return searchCQ();
  }
  

  public String searchCQ() throws MessageException {
  	try {
  	    String userName = "anonymous";
  	    String userRole ;
  	    String remoteHost;
  	  
  		String searchQuery = search.getSearchQuery();
  		logger.debug("search() {searchquery='"+searchQuery+"'}");
  	
	  	search.clearResults();
	  
	  	long s = (new Date()).getTime();
	 
	 
	  	remoteHost = ActionContext.getActionContext().getRequest().getRemoteHost();
	  	CorpGovPrincipal cp = (CorpGovPrincipal)ActionContext.getActionContext().getRequest().getUserPrincipal();
	  	if (cp!=null) {
	  	    userName = cp.getName();
	  	    userRole = cp.getRole();
	  	} else {
	  	    if (ConfigurationService.getConfig().getConsoleAuthenticate()) {
	  	        logger.warn("search denied. attempted search by unknown user. ");
	  	        long e = (new Date()).getTime();
	  	        searchTime = e - s ;
	  	        return "success";
	  	    } else {
	  	        userName = "Administrator";
	  	        userRole = "Administrator";
	  	    }
	  	}
	  	search.setUserName(userName);
	  	search.setUserRole(userRole);
	  	
	  	audit.info("search email {query="+searchQuery.trim()+", remotehost="+remoteHost+", uname="+userName+"}");
	  	//try {
	  		SearchService.searchMessage(search);
	  	//} catch (Exception e) {
	  	//	throw new BeanActionException("failed to execute search query '"+searchQuery+"'",e,logger);
		//}	
	  	long e = (new Date()).getTime();
	  	searchTime = e - s ;
	  	setPage(1);
	  	
	  
  	} catch (Exception e) {
  		e.printStackTrace();
  	}
	  	return "success";
  }
  
  public Iterator getSearchResults() {
      return search.getResults();
  }
  
  public void setOrderBy(String sortField) {
      if (sortField.compareTo(search.getSortField())!=0)
          search.setSortOrder(true);
      else
          search.setSortOrder(!search.getSortOrder());
      search.setSortField(sortField);
  }
  
  public String getOrderBy() {
      return search.getSortField();
  }
  
  public boolean getSortOrder() {
      return search.getSortOrder();
  }
  
  public void setSortOrder(boolean sortOrder) {
      search.setSortOrder(sortOrder);
  }
  
  public class Criteria {
  		
  		String field = "all";
  		String method = "any";
  		String query = "";
  		String operator = "AND";
  		
  		
  		public Criteria(String field) {
  		  this.field = field;
  		}
  		
  		public Criteria(String field, String method, String query) {
  		  this.method = method;
  		  this.query = query;
  		  this.field = field;
  		}
  		
  		
  		public String getQuery() { return query; }
  		public void setQuery(String query) { this.query = query; }
  		
  		public String getMethod() { return method; }
  		public void setMethod(String method) { this.method = method; }
  		
  		public void  setOperator(String operator) { this.operator = operator; }
  		public String getOperator() { return this.operator; }
  		
  		public String getField() { return field; }
  		public void setField(String field) { this.field = field; }

  		protected String allFields(String token) {
  		    String constructedQuery = "(";
  		    for (int i=1;i<FIELD_LIST.size();i++) {
  		        String field = (String)FIELD_LIST.get(i);
  		        constructedQuery += field + ":" + token+" ";
  		    }
  		    constructedQuery = constructedQuery.trim() + ") ";
  		    return constructedQuery;
  		}
		protected String getConstructedQuery() {
  		    String constructedQuery = "";
  		    if (method.compareToIgnoreCase("all")==0) {
  		    	boolean start = true;
  		    	StringTokenizer allWordsTokenizer = new StringTokenizer(query);
  		    	while (allWordsTokenizer.hasMoreTokens()) {
  		    		if (!start) constructedQuery += "AND ";
		  		    if (field.compareTo("all")==0)
		  	   			constructedQuery += allFields(allWordsTokenizer.nextToken())+" ";     
		  	   		else
		  	   			constructedQuery += field+":"+allWordsTokenizer.nextToken()+" ";
		  	   		start=false;
		  		}
  			} else if (method.compareToIgnoreCase("exact")==0) {
  			 	if (field.compareTo("all")==0)
  					constructedQuery += allFields("\"" + query + "\"") + " ";
  				else
  					constructedQuery += field+":\"" + query + "\"" + " ";
  		 	} else if (method.compareToIgnoreCase("any")==0) {
  		 		StringTokenizer anyWordsTokenizer = new StringTokenizer(query);
  		 		while (anyWordsTokenizer.hasMoreTokens()) {
		  	   		if (field.compareTo("all")==0)
		  	   			constructedQuery += allFields(anyWordsTokenizer.nextToken()+" ");     
		  	   		else
		  	   			constructedQuery += field+":"+anyWordsTokenizer.nextToken()+" ";
		  		}
  		 	} else if (method.compareToIgnoreCase("none")==0) {
  		 		StringTokenizer noWordsTokenizer = new StringTokenizer(query);
  		 		while (noWordsTokenizer.hasMoreTokens()) {
  		 		if (field.compareTo("all")==0)
  		 			constructedQuery += "-" + allFields(noWordsTokenizer.nextToken()+" ");
  		 		else
  		 			constructedQuery += "-" + field+":"+noWordsTokenizer.nextToken()+" ";
  		 		}
  		 	}	
  		 	return constructedQuery.trim();
  		 }
  		   		
  }
  				 
}
  	
  
