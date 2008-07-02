
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

import com.stimulus.util.*;

import org.apache.log4j.Logger;
import com.stimulus.archiva.exception.MessageSearchException;
import com.stimulus.archiva.domain.fields.*;

import java.security.Principal;

public abstract class Search implements java.io.Serializable, Props
{
	private static final long serialVersionUID = -6424054985456683316L;
	protected static Logger logger = Logger.getLogger(Search.class.getName());
	public enum Type { STANDARD }; // searchresults.type_
	public enum Attachment { EITHER, ATTACH, NOATTACH }; // searchresults.attachment_
	public enum Priority { ANY, HIGHEST, HIGH, NORMAL, LOW, LOWEST }; // searchresults.priority_
	public enum Flag { ANY, DELETED, SEEN, ANSWERED, FLAGGED, RECENT, DRAFT }; //searchresults.flag_
	public enum DateType { SENTDATE,ARCHIVEDATE, RECEIVEDDATE  }; 
	public enum SortOrder { NOSORT, DESCENDING, ASCENDING };
	public enum OpenIndex { SEARCH,SESSION };

	
	protected String			searchQuery	  = "";
	protected ArrayList<Result> results	  	  = new ArrayList<Result>();
	protected FetchMessage	    fetchMessage  = null;
    protected int               maxResults    = 10000;
    protected Type				searchType	  = Type.STANDARD;			
	protected String            language      = "en"; 
	protected Attachment  		attach  	  = Attachment.EITHER;
	protected Priority          priority	  = Priority.ANY;
	protected Flag				flag		  = Flag.ANY;
	protected Date				after	  	  = null;
	protected Date				before	  	  = null;
	protected String			sortField	  = "archivedate";
	protected SortOrder			sortOrder	  = SortOrder.NOSORT;
	protected String			initialSortField = "archivedate";
	protected SortOrder			initialSortOrder = SortOrder.DESCENDING;
	protected DateType			initialDateType	 = DateType.SENTDATE;
	protected Principal 		principal  	  	= null;
	protected DateType			dateType 	   	= DateType.ARCHIVEDATE;
	protected OpenIndex			openIndex  		= OpenIndex.SEARCH;
    protected static final String maxSearchResultsKey 			= "search.max.result";
    protected static final String searchAnalyzerLanguageKey		= "search.analyzer.language";
    protected static final String searchAnalyzerClassKey 		= "search.analyzer.class";
    protected static final String searchSortInitialOrderKey    	= "search.sort.initial.order";
    protected static final String searchSortInitialFieldKey		= "search.sort.initial.field";
    protected static final String searchInitialDateTypeKey	= "search.sort.initial.datetype";
    protected static final String searchOpenIndexKey			= "search.open.index";
    
    
    protected static Map<String,String> defaultSearchAnalyzers;
    protected static final String defaultMaxSearchResults="0";
    protected static final String defaultSearchSortInitialField="archivedate";
    protected static final String defaultSearchSortInitialOrder="descending";
    protected static final String defaultSearchInitialDateType="sentdate";
    protected static final String defaultSearchOpenIndex="search";
    protected static final String defaultSortDateType="search";
    protected int maxSearchResults;
    protected Map<String,String> searchAnalyzers;

    
    static {
        Map<String,String> analyzerMap = new LinkedHashMap<String,String>();
        analyzerMap.put("en","com.stimulus.archiva.search.ArchivaAnalyzer");
        analyzerMap.put("pt","org.apache.lucene.analysis.br.BrazilianAnalyzer");
        analyzerMap.put("zh","org.apache.lucene.analysis.cn.ChineseAnalyzer");    
        analyzerMap.put("cs","org.apache.lucene.analysis.cz.CzechAnalyzer");    
        analyzerMap.put("de","org.apache.lucene.analysis.de.GermanAnalyzer");    
        analyzerMap.put("el","org.apache.lucene.analysis.el.GreekAnalyzer");   
        analyzerMap.put("fr","org.apache.lucene.analysis.fr.FrenchAnalyzer");
        analyzerMap.put("nl","org.apache.lucene.analysis.nl.DutchAnalyzer");
        analyzerMap.put("ru","org.apache.lucene.analysis.ru.RussianAnalyzer");
        analyzerMap.put("ja","org.apache.lucene.analysis.cjk.CJKAnalyzer");
        analyzerMap.put("ko","org.apache.lucene.analysis.cjk.CJKAnalyzer");
        analyzerMap.put("th","org.apache.lucene.analysis.th.ThaiAnalyzer");
        analyzerMap.put("tr","org.apache.lucene.analysis.tr.TurkishAnalyzer");
        defaultSearchAnalyzers = Collections.unmodifiableMap(analyzerMap);
    }
    
	public Search()
	{
		Calendar c1 = Calendar.getInstance(); 
		c1.add(Calendar.DATE,-60);
		c1.set(c1.get(Calendar.YEAR),c1.get(Calendar.MONTH),c1.get(Calendar.DAY_OF_MONTH),1,0,0);
		after = c1.getTime();
		c1.add(Calendar.DATE,61);
		c1.set(c1.get(Calendar.YEAR),c1.get(Calendar.MONTH),c1.get(Calendar.DAY_OF_MONTH),23,59,59);
		before = c1.getTime();
		searchAnalyzers = new LinkedHashMap<String,String>(defaultSearchAnalyzers);
	}
	
	// abstract methods
	
	public abstract void searchMessage() throws MessageSearchException;

	//	 allow for lazy retrieval of message contents

	public static abstract class FetchMessage
	{
		public abstract void fetchMessage(Result result);
	}
	
	public void reset() {
		
		sortOrder = initialSortOrder;
		sortField = initialSortField;
		searchType	= Type.STANDARD;	
		attach  	= Attachment.EITHER;
		priority	= Priority.ANY;
		flag	    = Flag.ANY;

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
		sortField = dateType.toString().toLowerCase(Locale.ENGLISH);
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

	public SortOrder getSortOrder()
	{
		return sortOrder;
	}

	public void setSortOrder(SortOrder sortOrder)
	{
		this.sortOrder = sortOrder;
	}

	public SortOrder getInitialSortOrder() {
		return this.initialSortOrder;
	}
	
	public void setInitialSortOrder(SortOrder initialSortOrder) {
		this.initialSortOrder = initialSortOrder;
	}
	
	public void setInitialSortField(String initialSortField) {
		this.initialSortField = initialSortField;
	}
	
	public String getInitialSortField() { 
		return this.initialSortField;
	}
	
	public DateType getInitialDateType() {
		return this.initialDateType;
	}
	
	public void setInitialDateType(DateType initialDateType) {
		this.initialDateType = initialDateType;
	}

	public abstract List<Result> getResults();
	
	public abstract int getResultSize();
	
	public abstract int getTotalHits();
	
	public void setFetchMessage(FetchMessage fetchMessage) {
		this.fetchMessage = fetchMessage;
	}

	
	public int getMaxSearchResults() { return maxSearchResults; }
	
	public void setMaxSearchResults(int maxSearchResults) {
		this.maxSearchResults = maxSearchResults;
	}
	
	public OpenIndex getOpenIndex() {
		return openIndex;
	}
	
	public void setOpenIndex(OpenIndex openIndex) {
		this.openIndex = openIndex;
	}
	
	public abstract long getTotalMessageCount(Volume v) throws MessageSearchException;
	
	
	public abstract class Result
	{

		public Result() {}
		
		public abstract EmailFieldValue getFieldValue(String key) throws MessageSearchException;

		
		public abstract EmailID getEmailId() throws MessageSearchException;
	}
	
	
	public void saveSettings(String prefix, Settings prop, String suffix) {
		logger.debug("saving search settings");
		prop.setProperty(maxSearchResultsKey, Integer.toString(getMaxSearchResults()));
		int c = 1;
		for (Map.Entry<String,String> searchAnalyzer : searchAnalyzers.entrySet()) {
			prop.setProperty(searchAnalyzerLanguageKey + "."+c, searchAnalyzer.getKey());
            prop.setProperty(searchAnalyzerClassKey + "."+c++, searchAnalyzer.getValue());
		}	
		
		prop.setProperty(searchSortInitialFieldKey, initialSortField.toString().toLowerCase(Locale.ENGLISH));
		prop.setProperty(searchSortInitialOrderKey, initialSortOrder.toString().toLowerCase(Locale.ENGLISH));
		prop.setProperty(searchInitialDateTypeKey, initialDateType.toString().toLowerCase(Locale.ENGLISH));
		prop.setProperty(searchOpenIndexKey, openIndex.toString().toLowerCase(Locale.ENGLISH) );
	}
	
	public boolean loadSettings(String prefix, Settings prop, String suffix) {
		logger.debug("loading search settings");
	    setMaxSearchResults(ConfigUtil.getInteger(prop.getProperty(maxSearchResultsKey),defaultMaxSearchResults));
	    searchAnalyzers = new LinkedHashMap<String,String>(); 
        
	    int i = 1;
        do {
            String className = prop.getProperty(searchAnalyzerClassKey + "."+ Integer.toString(i));
            String language = prop.getProperty(searchAnalyzerLanguageKey + "."+ Integer.toString(i));
            if (className ==null || language ==null) break;
            searchAnalyzers.put(language,className);
            i++;
        } while (true);
        
        // if no analyzers are found, load up the defaults
        if (searchAnalyzers.size()==0) {
            logger.debug("no search analyzers found in server.conf. using defaults.");
            searchAnalyzers = new LinkedHashMap<String,String>(defaultSearchAnalyzers);
        }
        
        initialSortField = ConfigUtil.getString(prop.getProperty(searchSortInitialFieldKey),defaultSearchSortInitialField);
        String so  = ConfigUtil.getString(prop.getProperty(searchSortInitialOrderKey),defaultSearchSortInitialOrder);
        initialSortOrder = SortOrder.valueOf(so.toUpperCase(Locale.ENGLISH));
        
        String dy  = ConfigUtil.getString(prop.getProperty(searchInitialDateTypeKey),defaultSearchInitialDateType);
        initialDateType = DateType.valueOf(dy.toUpperCase(Locale.ENGLISH));
        dateType = initialDateType;

  	    String oi  = ConfigUtil.getString(prop.getProperty(searchOpenIndexKey),defaultSearchOpenIndex);
  	    openIndex = OpenIndex.valueOf(oi.toUpperCase(Locale.ENGLISH));
  	    
  	    reset();
        return true;
	}
	
	public Map<String,String> getSearchAnalyzers() {
		return searchAnalyzers;
	}


	
	 
}
