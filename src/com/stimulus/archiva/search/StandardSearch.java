
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

package com.stimulus.archiva.search;

import java.text.ParsePosition;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.*;
import org.apache.lucene.search.*;
import org.apache.lucene.analysis.standard.*;
import com.stimulus.archiva.domain.Config;
import com.stimulus.archiva.domain.EmailID;
import com.stimulus.archiva.domain.Search;
import com.stimulus.archiva.domain.Volume;
import com.stimulus.archiva.exception.MessageSearchException;
import com.stimulus.archiva.language.AnalyzerFactory;

public class StandardSearch extends Search {

	 protected ArrayList<Criteria> criteria   = new ArrayList<StandardSearch.Criteria>();
	 protected String			compiledQuery = ""; 
	 protected static final Logger logger = Logger.getLogger(StandardSearch.class.getName());
	 protected SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");

	public StandardSearch() {
	    newCriteria();
	}
	public void searchMessage() throws MessageSearchException {
		logger.debug("standard search executed {query='"+getSearchQuery()+"'}");
		Searcher searchers;
		Analyzer analyzer = AnalyzerFactory.getAnalyzer(getLanguage());
		compileSearchQuery();
		Query query = getQuery(analyzer); 
        Sort sort = getSortPreference();
		Filter queryFilter = getFilter(new StandardAnalyzer());
		try { 
			searchers = getVolumeSearchers();
			Hits hits = search(searchers, query, queryFilter, sort);
			compileSearchResults(searchers, hits); 
		} catch (MessageSearchException mse) {
			logger.debug("standard search: no volumes available for searching");
		}
		
	 }
	
	  protected Query getQuery(Analyzer analyzer) throws MessageSearchException {
		  	QueryParser queryParser = new QueryParser("body",analyzer);
	  		Query query;
	  		searchQuery = searchQuery.trim();
	  		String dateQuery = getDateFilter();
	  		searchQuery += and(searchQuery,dateQuery);
	  		try {
	  			query = queryParser.parse(searchQuery);
	  			logger.debug("successfully parsed search query {query='"+searchQuery+"'}");
	  		} catch (Exception e) {
	  			throw new MessageSearchException("failed to parse search query {query='"+searchQuery+"'}",e,logger);
	  		}
	  		return query;
	  }  
	  
	 protected Filter getFilter(Analyzer analyzer) throws MessageSearchException {
		  Filter queryFilter;
			try {
				
				String attachF 	 = getAttachmentFilter();
				String flagF 	 = getFlagFilter();
				String priorityF = getPriorityFilter();
				String userRoleF = getUserRoleFilter();
				String filterstr = attachF;
				filterstr+= and(filterstr,flagF);
				filterstr+= and(filterstr,priorityF);
				filterstr+= and(filterstr,userRoleF);
				
			    logger.debug("standard search: parsing filter query {query='"+filterstr+"'}"); 
			    if (filterstr.length()==0)
			    	return null;
			    QueryParser filterQueryParser = new QueryParser("to",analyzer);
			    Query query = filterQueryParser.parse(filterstr);
			    queryFilter = new QueryWrapperFilter(query);
			    logger.debug("successfully parsed filter query {query='"+filterstr+"'}");
			} catch (Exception pe)
			{
				throw new MessageSearchException("failed to parse search query {searchquery='"+getSearchQuery()+"'}",pe,logger);
			}
			return queryFilter;
	  }
	
	protected String and(String prevClause, String nextClause) {
		if (prevClause.isEmpty() || nextClause.isEmpty())
			return nextClause;
		return " AND " + nextClause;
	}
		
	  protected Sort getSortPreference() {
		  Sort sort;
		  String sortField = getSortField(); 
          // we need untokenized field
		  boolean sortOrder = getSortOrder();
		  if (sortField.equals("size") || sortField.equals("priority"))  
			  sort = new Sort(new SortField[]{new SortField(sortField,SortField.INT,sortOrder)});
		  else if (sortField.equals("attach"))
			  sort = new Sort(new SortField[]{new SortField(sortField,SortField.INT,!sortOrder)});
		  else if (sortField.equals("score"))  
			  sort = new Sort(SortField.FIELD_SCORE);
		  else if (sortField.equals("subject") || sortField.equals("to") || sortField.equals("from"))
			  sort = new Sort(new SortField[]{new SortField(sortField+"s",SortField.STRING,sortOrder)});
		  else if (sortField.equals("sentdate"))
			  sort = new Sort(new SortField[]{new SortField(sortField,SortField.STRING,sortOrder)});
		  else return null;
		  return sort;  	  
	  }
		  

	  /* date search filter */
    
	  protected String getDateFilter() {	
		  Date sentAfter = defaultDate(getSentAfter(),new Date(0));
		  Date sentBefore = defaultDate(getSentBefore(), new Date());
		  return "sentdate:[d"+format.format(sentAfter) + " TO d" + format.format(sentBefore)+"]";	
	  }
	  
	  protected Date defaultDate(Date date, Date defaultDate) {
		  if (date==null)
			  return defaultDate;
		  else
			  return date;
	  }
	  /* attachment filter */
	  
	  protected String getAttachmentFilter() {
			Attachment attachment = getAttachment();
			if (attachment!=Attachment.EITHER) {
				if (attachment==Attachment.ATTACH)
					return "attach:1";
				else
					return "attach:0";
			}
			return "";
	  }
	  
	  /* flag filter */
	  
	  protected String getFlagFilter() {		
	  		Flag flag = getFlag();
	  		if (flag!=Flag.ANY) {
	  			return "flag:"+flag.toString().toLowerCase();
	  		}
	  		return "";
	  }
	  
	  /* priority filter */
	
	  protected String getPriorityFilter() {
		Priority priority = getPriority();
		if (priority!=Priority.ANY) {
			return  "priority:"+Integer.toString(priority.ordinal());
		}
		return "";
	  }
	  /* user role filter */
	  
	  protected String getUserRoleFilter() {
		  List<String> emailAddresses = getEmailAddresses();
		  if (emailAddresses !=null && emailAddresses.size()>0) {
			  if (getUserRole().equalsIgnoreCase("user")) {
				  String filterstr = "(";
				  for (String email : emailAddresses) {
			  			filterstr += " to:\""+email+"\" from:\""+email+"\" cc:\""+email+"\" bcc:\""+email+"\"";
				  }
				  return filterstr+")";
			  } 
		  }
		  return "";
	  }

	  protected Searcher getVolumeSearchers() throws MessageSearchException {
		  	boolean searcherPresent = false;
		  	Date sentAfter = defaultDate(getSentAfter(),new Date(0));
			Date sentBefore = defaultDate(getSentBefore(), new Date());
		    List volumes = Config.getConfig().getVolumes().getVolumes();
			LinkedList<Searcher> searchers = new LinkedList<Searcher>();
			Iterator vl = volumes.iterator();
			while (vl.hasNext()) {
			    Volume volume = (Volume)vl.next();
			    if (volume.getModified()!=null && sentAfter.compareTo(volume.getModified())>0) {
			    	logger.debug("standard search: not using volume {modified='"+volume.getModified()+"', sentAfter='"+sentAfter+"',"+volume);
			    	continue;
			    }
			    if (volume.getCreated()!=null && sentBefore.compareTo(volume.getCreated())<0) {
			    	logger.debug("standard search: not using volume {created='"+volume.getCreated()+"', sentBefore='"+sentBefore+"',"+volume);    
			    	continue;
			    }
			    try {
			        if (volume.getStatus()==Volume.Status.ACTIVE || volume.getStatus()==Volume.Status.CLOSED) {
			            Searcher volsearcher = new IndexSearcher(volume.getIndexPath());
			            searchers.add(volsearcher);
			            searcherPresent = true; 
			        }
			    } catch (IOException io) {
			        logger.error("failed to open index for search {"+volume+"}.");
			    }
			}
			
			if (!searcherPresent)
				throw new MessageSearchException("no volumes are ready to search",logger);

			Searcher[] searcherarraytype = new Searcher[searchers.size()];
			Searcher[] allsearchers = (Searcher[])(searchers.toArray(searcherarraytype));

			Searcher searcher;
			 try {
			     searcher = new ParallelMultiSearcher(allsearchers);
			} catch (IOException io) {
					throw new MessageSearchException("failed to open/create one or more index searchers",logger);
			}
			return searcher;
	  }
	  
	  protected Hits search(Searcher searchers, Query query, Filter queryFilter, Sort sort) throws MessageSearchException  {
		  Hits hits;
		  try {
			   hits = searchers.search(query,queryFilter,sort);
				logger.info("search executed successfully {query='"+getSearchQuery()+"', nohits='"+hits.length()+"'}");
		 } catch (IOException io)
		 {
			throw new MessageSearchException("failed to execute search query {searchquery='"+getSearchQuery()+"}",io,logger);
		 }
		
		 return hits;
	  }
	  
	  protected void compileSearchResults(Searcher searchers, Hits hits) throws MessageSearchException {
		  String messageUidDebugOutput = "search results {";
          int maxSearchResults = getMaxResults();
          
			long norecords;
			
		  if (maxResults!=0)
			  norecords = hits.length() > maxSearchResults ? maxSearchResults : hits.length();
		  else
		      norecords = hits.length();
		  
          logger.debug("max search results {maxSearchResults='"+maxSearchResults+"', noRecords='"+norecords+"'}");
			for (int start = 0; start < norecords; start++)
			{
			    Document doc = null;
				try {

					doc = hits.doc(start);
					float score = hits.score(start);
					addMessage(doc,score);
					/*if (logger.isDebugEnabled())
						messageUidDebugOutput+=doc.get("uid")+",";*/
				} catch (IOException io)
				{
					logger.error("failed to retrieve indexed value from search query {searchquery='"+getSearchQuery()+"'}",io);
				}
			}
			/* if (logger.isDebugEnabled()) {
				 if (messageUidDebugOutput.length()>0) 
					 messageUidDebugOutput = messageUidDebugOutput.substring(0,messageUidDebugOutput.length()-1);
				 messageUidDebugOutput += "'}"; 
				 logger.debug(messageUidDebugOutput);
			 }*/
             	
			 try {
					searchers.close();
			 } catch (IOException io) {
				throw new MessageSearchException("failed to close search indexes (opened for read)",io,logger);
			 }
	  }
	  
	  protected void addMessage(Document doc,float score) {
		  String uid 	  = doc.get("uid");
		  if (uid==null)  {
			  logger.warn("found message with null ID during construction of search results");
			  return;
		  }
		  
		  String ver = doc.get("ver"); 
		  
		  // new index format (email fields are stored in the index)
		  if (ver!=null && ver.equals("2")) {
			 
			  String subject  = doc.get("subjects");
			  String to 	  = doc.get("tos");
			  String from 	  = doc.get("froms");
			  String size     = doc.get("size");
			  String priority = doc.get("priority");
			  String attach   = doc.get("attach");
			 
			  subject = (subject!=null) ? subject : "";
			  to 	  = (to!=null) ? to : "";
			  from 	  = (from!=null) ? from : "";
			  
			  int sz 	   = (size!=null) ? Integer.parseInt(doc.get("size")) : 0;
			  int pri 	   = (priority!=null) ? Integer.parseInt(doc.get("priority")) : 3;
			  boolean att  = (attach!=null) ? Integer.parseInt(doc.get("attach"))==1 : false;
			  
			  SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
			  String sentDate = doc.get("sentdate");
			  Date sent = (sentDate!=null) ? format.parse(sentDate, new ParsePosition(1)) : new Date(0);
			  addMessage(new EmailID(uid), score, subject,to,from, sent, sz, att, pri);
		  } else {
			
			  // legacy index (all fields are taken from the email directly as opposed to the lucene index
			  addMessage(new EmailID(uid), score);
			  setFetchMessage(new LegacyFetchMessage());
		  }
	  }
	  

		protected void compileSearchQuery()
		{
			searchQuery = "";
		 	boolean start = true;
	  	  	Iterator i = criteria.iterator();
	  	  	while (i.hasNext()) {
	  	  		StandardSearch.Criteria c = (StandardSearch.Criteria)i.next();
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
		}

	    /* criteria */
	    
	    public void clearCriteria() {
	    	criteria.clear();
	    }
	    
	    public void newCriteria() {
	          logger.debug("newCriteria()");
	          criteria.add(new Criteria("to"));
	    }
	    
	    public List getCriteria() {
	    	return criteria;
	    }
	    
	    public void deleteCriteria(int id) {
	      logger.debug("deleteToCriteria() {index='"+id+"'}");  
	    	criteria.remove(criteria.get(id));
	    }
	    
	    
	    public class Criteria implements Serializable {
	    	
	    		private static final long serialVersionUID = 5689502035159175796L;
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

	    		public String allFields(String token) {
	    		    String constructedQuery = "(";
	    		    for (int i=1;i<FIELD_LIST.size();i++) {
	    		        String field = (String)FIELD_LIST.get(i);
	    		        constructedQuery += field + ":" + token+" ";
	    		    }
	    		    constructedQuery = constructedQuery.trim() + ") ";
	    		    return constructedQuery;
	    		}
	    		
	    		public String getConstructedQuery() {
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
