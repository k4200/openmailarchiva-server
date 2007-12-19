
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

import java.io.Serializable;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.stimulus.archiva.domain.Config;
import com.stimulus.archiva.domain.Search;
import com.stimulus.archiva.domain.fields.EmailField;
import com.stimulus.archiva.search.SearchFactory;
import com.stimulus.archiva.search.StandardSearch;
import com.stimulus.archiva.service.ConfigurationService;
import com.stimulus.archiva.service.SearchService;
import com.stimulus.struts.BaseBean;
import com.stimulus.util.EnumUtil;


public class SearchBean extends BaseBean implements Serializable {
  	
  private static final long serialVersionUID = -5738112871526292950L;
  protected Search search;
  protected int page = 1; 
  protected int pageSize = 20;
  protected double searchTime;

 
  protected static final int NO_DISPLAY_PAGES = 10;
  
  /* advanced search attributes */

  protected static final List METHOD_LIST;
  protected static final List METHOD_LABEL_LIST;

  protected static final List OPERATOR_LIST;
  protected static final List OPERATOR_LABEL_LIST;
  protected static final List PAGE_SIZE_LIST;
  protected static final List PAGE_SIZE_LABEL_LIST;
  protected static final List MAX_RESULTS_LABEL_LIST;
  protected static final List MAX_RESULTS_LIST;

  
  static {
   
    List<String> methodList = new LinkedList<String>();
    methodList.add("any");
    methodList.add("all");
    methodList.add("exact");
    methodList.add("none");
    METHOD_LIST = Collections.unmodifiableList(methodList);
    
    List<String> methodLabelList = new LinkedList<String>();
    methodLabelList.add("methode_label_any");
    methodLabelList.add("methode_label_all");
    methodLabelList.add("methode_label_exact");
    methodLabelList.add("methode_label_none");
    METHOD_LABEL_LIST = Collections.unmodifiableList(methodLabelList);
    
 
    
    List<String> operatorList = new LinkedList<String>();
    operatorList.add("AND");
    operatorList.add("OR");
    OPERATOR_LIST =  Collections.unmodifiableList(operatorList);
    
    List<String> operatorLabelList = new LinkedList<String>();
    operatorLabelList.add("operator_and");
    operatorLabelList.add("operator_or");
    OPERATOR_LABEL_LIST =  Collections.unmodifiableList(operatorLabelList);
    
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
  protected static Logger logger =  Logger.getLogger(SearchBean.class.getName());
  protected static final Logger audit = Logger.getLogger("com.stimulus.archiva.audit");

  protected boolean notSearched = true;
  
  /* Constructors */

  public SearchBean()  {

  }
  
  public void reset() {
	  if (search==null) {
		  try { searchform(); } catch (Exception e) { logger.debug("exception occurred",e); }
	  }
  }
  
  public String searchform() throws Exception {
	  	search = (StandardSearch)SearchFactory.getFactory(Search.Type.STANDARD);
	    String language = getLocale().getLanguage();
	    logger.debug("browser language detected {language='"+language+"'}");
	    search.setLanguage(language);
	    search.setMaxResults(ConfigurationService.getConfig().getMaxSearchResults());
	    notSearched = true;
	    page=1;
	    pageSize=20;
	    return "success";
  }
  public String resetsearch() throws Exception {
	    logger.debug("resetSearch()");
	    searchform();
	    return searchMessages();
  }
  
  /* bean setters and getters */
  
  public List getMethods() {
  	return METHOD_LIST; 
  }
  
  public List getMethodLabels() {
  	return translateList(METHOD_LABEL_LIST); 
  }
  
  public List getFields() {
	
	  ArrayList<String> fieldList = new ArrayList<String>();
	  Iterator i = EmailField.getAvailableFields().iterateValues();
	  while (i.hasNext()) {
		  EmailField ef = (EmailField)i.next();
		  // we dont allow end-users to search using bcc
		  if (ef.getName().equals("bcc") && getMailArchivaPrincipal().getRole().equals("user"))
			  continue;
		  if (ef.getName().equals("deliveredto") && getMailArchivaPrincipal().getRole().equals("user"))
			  continue;
		  
		  if (ef.getAllowSearch()==EmailField.AllowSearch.SEARCH)
			  fieldList.add(ef.getName());
	  }
	  fieldList.add("all");
	  Collections.sort(fieldList, String.CASE_INSENSITIVE_ORDER);
	  return fieldList;
  }
  
  public List getFieldLabels() {
	  ArrayList<String> fieldLabelList = new ArrayList<String>();
	  Iterator i = EmailField.getAvailableFields().iterateValues();
	  while (i.hasNext()) {
		  EmailField ef = (EmailField)i.next();
		  
		  // we dont allow end-users to search using bcc
		  if (ef.getName().equals("bcc") && getMailArchivaPrincipal().getRole().equals("user"))
			  continue;
		  if (ef.getName().equals("deliveredto") && getMailArchivaPrincipal().getRole().equals("user"))
			  continue;
		  
		  if (ef.getAllowSearch()==EmailField.AllowSearch.SEARCH)
			  fieldLabelList.add(ef.getResourceKey().toLowerCase());
	  }
	  fieldLabelList.add("field_label_all");
	  Collections.sort(fieldLabelList, String.CASE_INSENSITIVE_ORDER);
  	  return translateList(fieldLabelList,true); 
  }
  
  public List getOperators() {
  	return OPERATOR_LIST; 
  }
  
  public List getOperatorLabels() {
  	return translateList(OPERATOR_LABEL_LIST); 
  }
  
  /* order by */
  
  public void setOrderBy(String sortField) {
      if (sortField.equalsIgnoreCase(search.getSortField()))
          search.setSortOrder(!search.getSortOrder());
      search.setSortField(sortField);
  }
  
  public String getOrderBy() {
      return search.getSortField();
  }
  
  /* sort order */
  
  public boolean getSortOrder() {
      return search.getSortOrder();
  }
  
  public void setSortOrder(boolean sortOrder) {
      search.setSortOrder(sortOrder);
  }
  
  /* search language */
  
  public void setLanguage(String language) {
      search.setLanguage(language);
  }
  
  public String getLanguage() {
      logger.debug("getLanguage() {language='"+search.getLanguage()+"'}");
      return search.getLanguage();
  }
  
  public List getLanguages() {
      Config config = ConfigurationService.getConfig();
      Map searchAnalyzers = config.getSearchAnalyzers();
      List<String> labels = new ArrayList<String>();
      Iterator i = searchAnalyzers.entrySet().iterator();
      while (i.hasNext()) {
          Map.Entry searchAnalyzer = (Map.Entry)i.next();
          labels.add((String)searchAnalyzer.getKey());
      }
      return labels;
  }
  
  public List getLanguageLabels() {
      Config config = ConfigurationService.getConfig();
      Map searchAnalyzers = config.getSearchAnalyzers();
      List<String> labels = new ArrayList<String>();
      Iterator i = searchAnalyzers.entrySet().iterator();
      while (i.hasNext()) {
          Map.Entry searchAnalyzer = (Map.Entry)i.next();
          labels.add("searchresults.language_"+searchAnalyzer.getKey());
      }
      List translatedLabels = translateList(labels);
      //Collections.sort(translatedLabels);
      return translatedLabels;
  }
  
  
  /* max results */
  
  public void setMaxResult(String maxResults) {
      search.setMaxResults(Integer.parseInt(maxResults));    
  }
  
  public String getMaxResult() {
      return Integer.toString(search.getMaxResults());
  }
  
  public List getMaxResults() {
      return MAX_RESULTS_LIST;
  }
  
  public List getMaxResultLabels() {
      return MAX_RESULTS_LABEL_LIST;
  }
  
  /* search type */
  
  public void setSearchType(String searchType) {
	  Search.Type typeResult = Search.Type.STANDARD;	
	  	try {
	  		typeResult = Search.Type.valueOf(searchType.trim().toUpperCase());
	  	} catch (IllegalArgumentException iae) {
	    		logger.error("failed to apply search type field to search. search type field is set to an illegal value {type='"+searchType+"'}");
	    		logger.info("using standard search (error recovery)");
		}
	  	search.setType(typeResult);
  }
  
  /* search */
  
  public String getSearchType() {
      return search.getType().toString().toLowerCase();
  }
  
  public List getSearchTypes() {
	  return EnumUtil.enumToList(Search.Type.values());
  }
  public List getSearchTypeLabels() {
	  return translateList(EnumUtil.enumToList(Search.Type.values(),"searchresults.type_"));
  }
  
  /* priority */
  
  public void setPriority(String priority) {
	  Search.Priority priorityResult =  Search.Priority.ANY;	
	  	try {
	  		priorityResult =  Search.Priority.valueOf(priority.trim().toUpperCase());
	  	} catch (IllegalArgumentException iae) {
	    		logger.error("failed to apply priority field to search. priority field is set to an illegal value {priority='"+priority+"'}");
	    		logger.info("searching messages associated with any priority");
		}
      search.setPriority(priorityResult);
  }
  
  public String getPriority() {
	 return search.getPriority().toString().toLowerCase();
  }
  
  public List getPriorities() {
	  return EnumUtil.enumToList( Search.Priority.values());
  }
  
  public List getPriorityLabels() {
	  return translateList(EnumUtil.enumToList(Search.Priority.values(),"searchresults.priority_"));
  }
  
  public void setDateType(String dateType) {
	  Search.DateType dateTypeResult = Search.DateType.SENTDATE;
	  try {
		  dateTypeResult = Search.DateType.valueOf(dateType.trim().toUpperCase());
	  } catch (IllegalArgumentException iae) {
		  logger.error("failed to apply priority field to search. priority field is set to an illegal value {priority='"+dateTypeResult+"'}");
  		  logger.info("searching messages associated with any priority");
	  }
	  search.setDateType(dateTypeResult);
  }
  
  public String getDateType() {
	  return search.getDateType().toString().toLowerCase();
  }
  
  public List getDateTypes() { 
	  return EnumUtil.enumToList( Search.DateType.values());
  }
  
  public List getDateTypeLabels() { 
	  return translateList(EnumUtil.enumToList(Search.DateType.values(),"searchresults.datetype_"));
  }
  
  protected Search getSearch() {
	  return search;
  }

  /* attachment */
  
  public void setAttachment(String hasAttachment) {
		Search.Attachment attach = Search.Attachment.EITHER;	
	  	try {
	  		attach = Search.Attachment.valueOf(hasAttachment.trim().toUpperCase());
	  	} catch (IllegalArgumentException iae) {
	    		logger.error("failed to apply attachment field to search. attachment is set to an illegal value {attach='"+hasAttachment+"'}");
	    		logger.info("searching messages associated with all attachments (error recovery)");
		}
	  	search.setAttachment(attach);
  }
  
  public List getAttachments() {
  	return EnumUtil.enumToList(Search.Attachment.values());
  }
  
  public List getAttachmentLabels() {
  	return translateList(EnumUtil.enumToList(Search.Attachment.values(),"searchresults.attachment_"));
  }
  
  public String getAttachment() {
	  return ((Search)search).getAttachment().toString().toLowerCase();
  }
  
/* flag */
  
  public void setFlag(String flag) {
	  Search.Flag flagResult = Search.Flag.ANY;
	  	try {
	  		flagResult = Search.Flag.valueOf(flag.trim().toUpperCase());
	  	} catch (IllegalArgumentException iae) {
	    		logger.error("failed to apply flag field search. flag is set to an illegal value {attach='"+flag+"'}");
	    		logger.info("searching messages associated with any flag (error recovery)");
		}
	  	search.setFlag(flagResult);
  }
  
  public String getFlag() {
	  return search.getFlag().toString().toLowerCase();
  }
  
  public List getFlagLabels() {
	  return translateList(EnumUtil.enumToList(Search.Flag.values(),"searchresults.flag_"));
  }
  
  public List getFlags() {
	  return EnumUtil.enumToList(Search.Flag.values());
  }
  
  
  /* pages */
  
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
  
  public int getFirstPage() {
	  return 1;
  }
  
  public int getLastPage() {
	  return getNoPages();
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
  	else logger.debug("failed to set xxx {page='"+page+"',totalpages = '"+getNoPages()+"'}"); 
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
      int maxPage = getMinViewPage() + NO_DISPLAY_PAGES > getNoPages() ? getNoPages() : getMinViewPage() + NO_DISPLAY_PAGES;
      logger.debug("getMaxViewPage() {ret='"+maxPage+"'}");
      return maxPage;
  }
  
  public int getMinViewPage() {
	  int minpage = page+NO_DISPLAY_PAGES+1>getNoPages() ? getNoPages()-NO_DISPLAY_PAGES : page - (NO_DISPLAY_PAGES)/2;
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

  
  public String getSearchTime() {
    DecimalFormat nf = new DecimalFormat("0.00");
    String st = nf.format(searchTime / 1000);
    logger.debug("getSearchTime() {ret='"+st+"}");
    return st;
  }
 
  public String getAfter() {
	
  	if (search.getAfter()==null)
  	{
  		logger.debug("getAfter() {sentafter='null'}");
  		return "";
  	}
  	DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, getLocale());
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
  	DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, getLocale());
  	String rb = format.format(search.getBefore());
  	logger.debug("getBefore() {sentbefore='"+rb+"'}");
	return rb;
  }
	
  public void setAfter(String after) { 
  	logger.debug("setAfter() {sentafter='"+after+"'}");

  	// see if a time was specified
  	DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, getLocale());
  	 format.setLenient(false);
  	 try 
	 {
  	 	if (after.length()>0)
  	 		search.setAfter(format.parse(after));
  	 	else
  	 		search.setAfter(null);
  	 	return;
  	 } catch(ParseException pe) {
        logger.debug("failed to parse date {sentafter='" + after+"'}");    	            		
     }
  	 // ok could not parse datetime, so now parse date only
  	 DateFormat format2 = DateFormat.getDateInstance(DateFormat.SHORT, getLocale());
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
  	DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, getLocale());
  	 format.setLenient(false);
  	 try 
	 {
  	 	if (before.length()>0)
  	 		search.setBefore(format.parse(before));
  	 	else search.setBefore(null);
  	 	return;
  	 		
  	 } catch(ParseException pe) {
        logger.debug("failed to parse date {sentafter='" + before+"'}");    	            		
     }
  	 // ok could not parse datetime, so now parse date only
  	 DateFormat format2 = DateFormat.getDateInstance(DateFormat.SHORT, getLocale());
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
      logger.debug("newCriteria()");
     ((StandardSearch)search).newCriteria();
}

  public List getCriteria() {
	return ((StandardSearch)search).getCriteria();
  }
 
  public String getDateFormat() {
	  DateFormat sdf = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT, getLocale() );
	  return ((SimpleDateFormat)sdf).toPattern();
  }
  
  public String getLocalizedDateFormat() {
	  DateFormat sdf = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT, getLocale() );
	  return ((SimpleDateFormat)sdf).toLocalizedPattern();
  }
  

  public String searchsort() {
	  searchMessages();
	  return "success";
  }
  
  public String search() throws Exception
  {
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
  	} 
  	
  	
  	return searchMessages();
  }
  
  protected String searchMessages() {
	  try {
		  	notSearched = false;
	  	    String searchQuery = search.getSearchQuery();
	  		logger.debug("search() {searchquery='"+searchQuery+"'}");
		  	search.clearResults();
		  	long s = (new Date()).getTime();
		  	search.setPrincipal(getMailArchivaPrincipal());
		  	SearchService.searchMessage(search);
		  	long e = (new Date()).getTime();
		  	searchTime = e - s ;
		  	setPage(1);
		  	
	  	} catch (Exception e) {
	  		e.printStackTrace();
	  	}
	  	return "success";
  }
  
  
  


  public List<SearchResultBean> getSearchResults() {
      return SearchResultBean.getSearchResultBeans(search.getResultsList(),getLocale());
  }
  
  public List<EmailField> getAvailableFields() {
		 ArrayList<EmailField>  list = new ArrayList<EmailField>();
		 Iterator i = EmailField.getAvailableFields().iterateValues();
		 while (i.hasNext()) {
			 EmailField ef = (EmailField)i.next();
			 if (ef.getShowResults() || ef.getShowConditional())
				 list.add(ef);			 
		 }
		 return list;
	 }
	
	public boolean getNotSearched() { return notSearched; }

}
 
 
  
