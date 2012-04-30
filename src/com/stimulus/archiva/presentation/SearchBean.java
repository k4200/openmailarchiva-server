
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

package com.stimulus.archiva.presentation;

import javax.servlet.http.HttpSessionBindingListener;
import com.stimulus.archiva.domain.*;
import com.stimulus.archiva.domain.Search.Result;
import com.stimulus.archiva.domain.fields.EmailField;
import com.stimulus.archiva.domain.fields.EmailFields;
import com.stimulus.archiva.service.*;
import com.stimulus.archiva.search.*;
import com.stimulus.struts.*;
import com.stimulus.util.*;
import java.util.*;
import java.text.*;
import org.apache.commons.logging.*;
import org.apache.struts.action.ActionForm;

import com.stimulus.archiva.exception.*;
import java.io.Serializable;
import javax.servlet.http.*;

public class SearchBean extends BaseBean implements HttpSessionBindingListener, Serializable {
  	
  private static final long serialVersionUID = -5738112871526292950L;

  protected int page = 1; 
  protected int pageSize = 20;
  protected double searchTime;
  protected Search search;
  protected int totalHits;
  protected int resultSize;
  protected List<Result> results;
  protected List<SearchResultBean> searchResults;
  
  protected String[] selected;
  

 
  protected static final int NO_DISPLAY_PAGES = 10;
  
  /* advanced search attributes */


  protected static final List PAGE_SIZE_LIST;
  protected static final List PAGE_SIZE_LABEL_LIST;
  protected static final List MAX_RESULTS_LABEL_LIST;
  protected static final List MAX_RESULTS_LIST;

  
  static {
   
    List<String> pageSizeList = new LinkedList<String>();
    pageSizeList.add("10");
    pageSizeList.add("20");
    pageSizeList.add("50");
    pageSizeList.add("100");
    PAGE_SIZE_LIST = Collections.unmodifiableList(pageSizeList);

    List<String> pageSizeLabelList = new LinkedList<String>();
    pageSizeLabelList.add("10");
    pageSizeLabelList.add("20");
    pageSizeLabelList.add("50");
    pageSizeLabelList.add("100");
    PAGE_SIZE_LABEL_LIST = Collections.unmodifiableList(pageSizeLabelList);
  
    List<String> maxResultsLabelList = new LinkedList<String>();
    maxResultsLabelList.add("20");
    maxResultsLabelList.add("50");
    maxResultsLabelList.add("100");
    maxResultsLabelList.add("200");
    maxResultsLabelList.add("500");
    maxResultsLabelList.add("1000");
    maxResultsLabelList.add("5000");
    maxResultsLabelList.add("10000");
    maxResultsLabelList.add("50000");
    maxResultsLabelList.add("100000");
    maxResultsLabelList.add(">100000");
    MAX_RESULTS_LABEL_LIST = Collections.unmodifiableList(maxResultsLabelList);
    
    List<String> maxResultsList = new LinkedList<String>();
    maxResultsList.add("20");
    maxResultsList.add("50");
    maxResultsList.add("100");
    maxResultsList.add("200");
    maxResultsList.add("500");
    maxResultsList.add("1000");
    maxResultsList.add("5000");
    maxResultsList.add("10000");
    maxResultsList.add("50000");
    maxResultsList.add("100000");
    maxResultsList.add("0");
    MAX_RESULTS_LIST = Collections.unmodifiableList(maxResultsList);
    
  }
  
  protected static final String recvDateFieldName = "sentdate";
  // for advanced search
  protected static Log logger = LogFactory.getLog(SearchBean.class.getName());
  protected static final Log audit = LogFactory.getLog("com.stimulus.archiva.audit");

  protected boolean notSearched;
  
  /* Constructors */

  public SearchBean()  {
	
  }
  
  public void reset() {
	
  }
  public String searchform() throws Exception {
	  return "success";
  }
  public String resetsearch() throws Exception {
	    logger.debug("resetsearch()");
		search.reset();
	    return "success";
		//return searchMessages();
  }
  
  /* bean setters and getters */
  
  public List<String> getMethods() {
  	return EnumUtil.enumToList(Criteria.Method.values());  
  }
  
  public List<String> getMethodLabels() {
  	return translateList(EnumUtil.enumToList(Criteria.Method.values(),"methode_label_"));
  }
  
  public List<String> getFields() {
	
	  ArrayList<String> fieldList = new ArrayList<String>();
	  EmailFields emailFields = Config.getConfig().getEmailFields();
	  for (EmailField ef : emailFields.getAvailableFields().values()) {
		  // we dont allow end-users to search using bcc
		  if (ef.getName().equals("bcc") && getMailArchivaPrincipal().getRole().equals("user"))
			  continue;
		  if (ef.getName().equals("deliveredto") && getMailArchivaPrincipal().getRole().equals("user"))
			  continue;
		  
		  if (ef.getAllowSearch()==EmailField.AllowSearch.SEARCH)
			  fieldList.add(ef.getName());
	  }
	  fieldList.add("addresses");
	  fieldList.add("all");
	  Collections.sort(fieldList, String.CASE_INSENSITIVE_ORDER);
	  return fieldList;
  }
  
  public List<String> getFieldLabels() {
	  ArrayList<String> fieldLabelList = new ArrayList<String>();
	  EmailFields emailFields = Config.getConfig().getEmailFields();
	  for (EmailField ef :  emailFields.getAvailableFields().values()) {
		  // we dont allow end-users to search using bcc
		  if (ef.getName().equals("bcc") && getMailArchivaPrincipal().getRole().equals("user"))
			  continue;
		  if (ef.getName().equals("deliveredto") && getMailArchivaPrincipal().getRole().equals("user"))
			  continue;
		  
		  if (ef.getAllowSearch()==EmailField.AllowSearch.SEARCH)
			  fieldLabelList.add(ef.getResource().toLowerCase(Locale.ENGLISH));
	  }
	  fieldLabelList.add("field_label_addresses");
	  fieldLabelList.add("field_label_all");
	  Collections.sort(fieldLabelList, String.CASE_INSENSITIVE_ORDER);
  	  return translateList(fieldLabelList,true); 
  }
  
  public List<String> getOperators() {
	return EnumUtil.enumToList(Criteria.Operator.values());  
  }
  
  public List<String> getOperatorLabels() {
	return translateList(EnumUtil.enumToList(Criteria.Operator.values(),"operator_"));
  }
  
  /* order by */
  
  public void setOrderBy(String sortField) {
	  if (Compare.equalsIgnoreCase(sortField, search.getSortField())) {
          if (search.getSortOrder()==Search.SortOrder.ASCENDING || 
        	  search.getSortOrder()==Search.SortOrder.NOSORT) {
        	  	search.setSortOrder(Search.SortOrder.DESCENDING);
          } else { 
        	  search.setSortOrder(Search.SortOrder.ASCENDING);
          }
	  }
      search.setSortField(sortField);
  }
  
  public String getOrderBy() {
	 
	  if (search.getSortField().equals("archivedate") ||
		  search.getSortField().equals("sentdate") ||
		  search.getSortField().equals("receiveddate")) {
		  	return search.getDateType().toString().toLowerCase(Locale.ENGLISH);
  	  }
	 
	  return search.getSortField();
  }
  
  /* sort order */
  
  public String getSortOrder() {
	  
      return search.getSortOrder().toString().toLowerCase(Locale.ENGLISH);
  }
  
  public void setSortOrder(String sortOrder) {
      search.setSortOrder(Search.SortOrder.valueOf(sortOrder));
  }
  
  /* search language */
  
  public void setLanguage(String language) {
      search.setLanguage(language);
  }
  
  public String getLanguage() {
      logger.debug("getLanguage() {language='"+search.getLanguage()+"'}");
      return search.getLanguage();
  }
  
  public List<String> getLanguages() {
	  List<String> labels = new ArrayList<String>();
      for (Map.Entry<String,String> searchAnalyzer : search.getSearchAnalyzers().entrySet()) { 
          labels.add((String)searchAnalyzer.getKey());
      }
      return labels;
  }
  
  public List<String> getLanguageLabels() {
      List<String> labels = new ArrayList<String>();
      for (Map.Entry<String,String> searchAnalyzer : search.getSearchAnalyzers().entrySet()) { 
          labels.add("searchresults.language_"+searchAnalyzer.getKey());
      }
      return translateList(labels);
  }
  
  /* search */
  
  public String getSearchType() {
      return search.getType().toString().toLowerCase(Locale.ENGLISH);
  }
  
  public List<String> getSearchTypes() {
	  return EnumUtil.enumToList(Search.Type.values());
  }
  public List<String> getSearchTypeLabels() {
	  return translateList(EnumUtil.enumToList(Search.Type.values(),"searchresults.type_"));
  }
  
 
  
  
  
  /* priority */
  
  public void setPriority(String priority) {
	  Search.Priority priorityResult =  Search.Priority.ANY;	
	  	try {
	  		priorityResult =  Search.Priority.valueOf(priority.trim().toUpperCase(Locale.ENGLISH));
	  	} catch (IllegalArgumentException iae) {
	    		logger.error("failed to apply priority field to search. priority field is set to an illegal value {priority='"+priority+"'}");
	    		logger.info("searching messages associated with any priority");
		}
      search.setPriority(priorityResult);
  }
  
  public String getPriority() {
	 return search.getPriority().toString().toLowerCase(Locale.ENGLISH);
  }
  
  public List<String> getPriorities() {
	  return EnumUtil.enumToList( Search.Priority.values());
  }
  
  public List<String> getPriorityLabels() {
	  return translateList(EnumUtil.enumToList(Search.Priority.values(),"searchresults.priority_"));
  }
  
  public void setDateType(String dateType) {
	  Search.DateType dateTypeResult = Search.DateType.SENTDATE;
	  try {
		  dateTypeResult = Search.DateType.valueOf(dateType.trim().toUpperCase(Locale.ENGLISH));
	  } catch (IllegalArgumentException iae) {
		  logger.error("failed to apply priority field to search. priority field is set to an illegal value {priority='"+dateTypeResult+"'}");
  		  logger.info("searching messages associated with any priority");
	  }
	  search.setDateType(dateTypeResult);
  }
  
  public String getDateType() {
	  return search.getDateType().toString().toLowerCase(Locale.ENGLISH);
  }
  
  public List<String> getDateTypes() { 
	  return EnumUtil.enumToList( Search.DateType.values());
  }
  
  public List<String> getDateTypeLabels() { 
	  return translateList(EnumUtil.enumToList(Search.DateType.values(),"searchresults.datetype_"));
  }
  
  protected Search getSearch() {
	  return search;
  }

  /* attachment */
  
  public void setAttachment(String hasAttachment) {
		Search.Attachment attach = Search.Attachment.EITHER;	
	  	try {
	  		attach = Search.Attachment.valueOf(hasAttachment.trim().toUpperCase(Locale.ENGLISH));
	  	} catch (IllegalArgumentException iae) {
	    		logger.error("failed to apply attachment field to search. attachment is set to an illegal value {attach='"+hasAttachment+"'}");
	    		logger.info("searching messages associated with all attachments (error recovery)");
		}
	  	search.setAttachment(attach);
  }
  
  public List<String> getAttachments() {
  	return EnumUtil.enumToList(Search.Attachment.values());
  }
  
  public List<String> getAttachmentLabels() {
  	return translateList(EnumUtil.enumToList(Search.Attachment.values(),"searchresults.attachment_"));
  }
  
  public String getAttachment() {
	  return ((Search)search).getAttachment().toString().toLowerCase(Locale.ENGLISH);
  }
  
/* flag */
  
  public void setFlag(String flag) {
	  Search.Flag flagResult = Search.Flag.ANY;
	  	try {
	  		flagResult = Search.Flag.valueOf(flag.trim().toUpperCase(Locale.ENGLISH));
	  	} catch (IllegalArgumentException iae) {
	    		logger.error("failed to apply flag field search. flag is set to an illegal value {attach='"+flag+"'}");
	    		logger.info("searching messages associated with any flag (error recovery)");
		}
	  	search.setFlag(flagResult);
  }
  
  public String getFlag() {
	  return search.getFlag().toString().toLowerCase(Locale.ENGLISH);
  }
  
  public List<String> getFlagLabels() {
	  return translateList(EnumUtil.enumToList(Search.Flag.values(),"searchresults.flag_"));
  }
  
  public List<String> getFlags() {
	  return EnumUtil.enumToList(Search.Flag.values());
  }
  
  
  /* pages */
  
  public int getNoHitsOnPage() {
  	//logger.debug("getNoHitsOnPage()");
  	int noHitsOnPage = 0;
  	int noPages = getNoPages();
  	if (noPages<=1) 
  		noHitsOnPage = getResultSize();
  	else if (page==noPages)
  		noHitsOnPage = getResultSize() % pageSize;
  	else noHitsOnPage = pageSize;
  	//logger.debug("getNoHitsOnPage() {ret='"+noHitsOnPage+"'}");
  	return noHitsOnPage;
  }
  
  public int getPreviousPage() { // doesn't alter current page, merely increments for UI purposes 
   
    int previousPage = page - 1 < 1 ? getNoPages() : page - 1;
  	//logger.debug("getPreviousPage() {ret='"+previousPage+"'}");
  	return previousPage;
  }
  
  public int getFirstPage() {
	  return 1;
  }
  
  public int getLastPage() {
	  return getNoPages();
  }
  public int getNextPage() { // doesn't alter current page, merely increments for UI purposes 
    int nextPage = 0;
  	if (page+1>getNoPages())  nextPage = 1; else nextPage = page+1;
	//logger.debug("getNextPage() {ret='"+nextPage+"'}");
	return nextPage;
  }
  public int getFirstHitIndex() {
  	int firstHitIndex = page*pageSize - pageSize;
  	//logger.debug("getFirstHitIndex() {ret='"+firstHitIndex+"'}");
  	return firstHitIndex;
  }
  
  public int getLastHitIndex() {
  	int lastHitIndex = getFirstHitIndex()+getNoHitsOnPage();
  	//logger.debug("getLastHitIndex() {ret='"+lastHitIndex+"'}");
  	return lastHitIndex;
  }
  
  public void setPageSize(int pageSize) { 
  	//logger.debug("setPageSize() {pagesize='"+pageSize+"'}"); 
  	this.pageSize = pageSize; 
  }
  
  public int getPageSize() { 
  	//logger.debug("getPageSize() {pagesize='"+pageSize+"'}"); 
  	return pageSize; 
  }
  
  public List<String> getPageSizes() {
    	return PAGE_SIZE_LIST; 
    }
    
    public List<String> getPageSizeLabels() {
    	return PAGE_SIZE_LABEL_LIST; 
    }
    
   
  public int getPage() { 
  	//logger.debug("getPage() {page='"+page+"'}"); 
  	return page; 
  }
  
  public void setPage(int page) { 
  	//logger.debug("setPage() {page='"+page+"'}"); 
  	if (page>=0 && page<=getNoPages()) 
  		this.page = page; 
  	//else logger.debug("failed to set xxx {page='"+page+"',totalpages = '"+getNoPages()+"'}"); 
  }
  
  public int getNoPages()
  {
  	int searchSize = getResultSize();
  	//logger.debug("getNoPages <{ search size / page size = " + searchSize / pageSize + " } ");
  	//logger.debug("getNoPages <{ search size % page size = " + searchSize % pageSize+ " } ");
  	int noPages = searchSize / pageSize + ((searchSize % pageSize)>0 ? 1:0);
  	//logger.debug("getNoPages() {ret='"+noPages+"'}"); 
  	return noPages;
  }
  public int getTotalHits()
  {
	  return totalHits;
  }
  
  public int getResultSize() {
	  return resultSize;
  }
  
  public int getMaxViewPage() {
      int maxPage = getMinViewPage() + NO_DISPLAY_PAGES > getNoPages() ? getNoPages() : getMinViewPage() + NO_DISPLAY_PAGES;
      //logger.debug("getMaxViewPage() {ret='"+maxPage+"'}");
      return maxPage;
  }
  
  public int getMinViewPage() {
	  int minpage = page+NO_DISPLAY_PAGES+1>getNoPages() ? getNoPages()-NO_DISPLAY_PAGES : page - (NO_DISPLAY_PAGES)/2;
      if (minpage<1) minpage = 1; 
      //logger.debug("getMinViewPage() {ret='"+minpage+"'}");
      return minpage;
  }
  
  public String getSearchQuery() {
  	String searchQuery = "";
  	searchQuery = search.getSearchQuery();
    //logger.debug("getSearchQuery() {ret='"+searchQuery+"'}");
    return searchQuery;
  }

  
  public String getSearchTime() {
    DecimalFormat nf = new DecimalFormat("0.00");
    String st = nf.format(searchTime / 1000);
    //logger.debug("getSearchTime() {ret='"+st+"}");
    return st;
  }
 
  public String getAfter() {
	
  	if (search.getAfter()==null)
  	{
  		logger.debug("getAfter() {sentafter='null'}");
  		return "";
  	}
  	
  	DateFormat format = DateUtil.getShortDateFormat(getLocale());
  	String ra = format.format(search.getAfter());
  	logger.debug("getAfter() {sentafter='"+ra+"'}");
  	return ra;
  	}
	
  public String getBefore() {
  	if (search.getBefore()==null)
  	{
  		logger.debug("getBefore() {sentbefore='null'}");
  		return "";
  	}
  	DateFormat format = DateUtil.getShortDateFormat(getLocale());
  	String rb = format.format(search.getBefore());
  	logger.debug("getBefore() {sentbefore='"+rb+"'}");
	return rb;
  }
	
  public void setAfter(String after) { 
  	logger.debug("setAfter() {sentafter='"+after+"'}");

  	// see if a time was specified
  	DateFormat format = DateUtil.getShortDateFormat(getLocale());
  	 format.setLenient(false);
  	 try 
	 {
  	 	if (after.length()>0)
  	 		search.setAfter(format.parse(after));
  	 	else
  	 		search.setAfter(null);
  	 	return;
  	 } catch(Exception pe) {
        logger.debug("failed to parse date {sentafter='" + after+"'}");    	            		
     }
  	 // ok could not parse datetime, so now parse date only
  	 DateFormat format2 = DateUtil.getShortDateFormat(getLocale());
 	format2.setLenient(false);
 	 try 
	 {
 	 	if (after.length()>0) {
 	 		Date date = format2.parse(after);
 	 		Calendar cal = Calendar.getInstance();
 	 		cal.setTime(date);
 	 		cal.set(Calendar.HOUR_OF_DAY,1);
 	 		cal.set(Calendar.MINUTE,0);
 	 		cal.set(Calendar.SECOND,0);
 	 		search.setAfter(cal.getTime());
 	 		return;
 	 	} else search.setAfter(null);
 	 } catch(ParseException pe) {
       logger.debug("failed to parse date {sentafter='" + after+"'}");    	            		
    }
  }
   
  public void setBefore(String before) { 
  	logger.debug("setBefore() {before='"+before+"'}");
	// see if a time was specified
  	DateFormat format = DateUtil.getShortDateFormat(getLocale());
  	 format.setLenient(false);
  	 try 
	 {
  	 	if (before.length()>0)
  	 		search.setBefore(format.parse(before));
  	 	else search.setBefore(null);
  	 	return;
  	 		
  	 } catch(Exception pe) {
        logger.debug("failed to parse date {sentafter='" + before+"'}");    	            		
     }
  	 // ok could not parse datetime, so now parse date only
  	 DateFormat format2 = DateUtil.getShortDateFormat(getLocale());
 	 format2.setLenient(false);
 	 try 
	 {
 	 	if (before.length()>0) {
 	 		Date date = format2.parse(before);
 	 		Calendar cal = Calendar.getInstance();
 	 		cal.setTime(date);
 	 		cal.set(Calendar.HOUR_OF_DAY,23);
 	 		cal.set(Calendar.MINUTE,59);
 	 		cal.set(Calendar.SECOND,59);
 	 		search.setBefore(cal.getTime());
 	 		return;
 	 	} else search.setBefore(null);
 	 } catch(ParseException pe) {
       logger.debug("failed to parse date {sentbefore='" + before+"'}");    	            		
    }
  }
  
  public void newCriteria() {
     ((StandardSearch)search).newCriteria();
}

  public List<CriteriaBean> getCriteria() {
	return CriteriaBean.getCriteriaBeans(((StandardSearch)search).getCriteria());
  }
 
  public String getDateFormat() {
	  DateFormat sdf = DateUtil.getShortDateFormat(getLocale());
	  return ((SimpleDateFormat)sdf).toPattern();
  }
  
  public String getLocalizedDateFormat() {
	  DateFormat sdf = DateUtil.getShortDateFormat(getLocale());
	  return ((SimpleDateFormat)sdf).toLocalizedPattern();
  }
  

  public String searchsort() {
	  searchMessages();
	  return "success";
  }
  
  public String search() throws Exception
  {
	  logger.debug("search() begin");
	  if (search==null)
		  searchform();
    SubmitButton button = getSubmitButton();
  	logger.debug("search() {action='"+button.action+"', value='"+button.value+"'}");
  	
  	if (button.action==null)
  		return "success";
  	
  	if (button.action!=null && button.action.equals("newcriteria")) {
  		((StandardSearch)search).newCriteria();
  		return "success";
  	} else if (button.action!=null &&  button.action.equals("deletecriteria")) { 
  		((StandardSearch)search).deleteCriteria(Integer.parseInt(button.value));
  		return "success";
  	} else if (button.action!=null && button.action.equals("reset")) {
		return resetsearch();
  	} else if (button.action!=null && button.action.equals("export")) {
		return "export";
  	} 
  	
  	return searchMessages();
  }
  
  
  protected String searchMessages() {
	  try {
		  	logger.debug("searchmessages() begin");
		  	notSearched = false;
	  	    String searchQuery = search.getSearchQuery();
	  		logger.debug("search() {searchquery='"+searchQuery+"'}");
		  	long s = (new Date()).getTime();
		  	search.setPrincipal(getMailArchivaPrincipal());
		  	SearchService.searchMessage(search);
		  	long e = (new Date()).getTime();
		  	searchTime = e - s ;
		  	setPage(1);
			resultSize = search.getResultSize();
			totalHits = search.getTotalHits();
			results = search.getResults();
			searchResults = SearchResultBean.getSearchResultBeans(results,getLocale());
		  	logger.debug("searchmessages() end");
	  	} catch (Exception e) {
	  	}
	  	return "success";
  }
  
 
  public List<SearchResultBean> getSearchResults() {
	  //logger.debug("getSearchResults() {size='"+results.size()+"'}");
	  return searchResults;
//	  return SearchResultBean.getSearchResultBeans(results,getLocale());
  }
  
  public List<EmailField> getAvailableFields() {
		 ArrayList<EmailField>  list = new ArrayList<EmailField>();
		 EmailFields emailFields = Config.getConfig().getEmailFields();
		 for (EmailField ef :  emailFields.getAvailableFields().values()) {
			 if (ef.getShowResults() || ef.getShowConditional())
				 list.add(ef);			 
		 }
		 return list;
	 }
  
  public boolean getNotSearched() { return notSearched; }
  
  public String[] getSelected() {
	  return selected;
  }
  public void setSelected(String selected[]) {
	  logger.debug("SearchBean.setChecked()");
	  this.selected = selected;
  }
  
  public void valueUnbound(HttpSessionBindingEvent event) {
	  	if (search!=null) {
	  		search.reset();
	  		search = null;
	  	}
}	
  public void valueBound(HttpSessionBindingEvent event) {
		 logger.debug("searchform() begin");
		   try {
				 search = (StandardSearch)SearchFactory.getFactory(Search.Type.STANDARD);
				 search.loadSettings(null,Config.getConfig().getSettings(), null);
			} catch (MessageSearchException mse) {
				 logger.error("failed to create search object",mse);
			}
	        String language = getLocale().getLanguage();
		    logger.debug("browser language detected {language='"+language+"'}");
		    search.setLanguage(language);
		    page=1;
		    pageSize=20;
		    logger.debug("searchform() end");
		    
  }
 
}
 
 
  
