/*
 * Subversion Infos:
 * $URL$
 * $Author$
 * $Date$
 * $Rev$
*/

		
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

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ParallelMultiSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;

import com.stimulus.archiva.domain.Config;
import com.stimulus.archiva.domain.EmailID;
import com.stimulus.archiva.domain.Search;
import com.stimulus.archiva.domain.Volume;
import com.stimulus.archiva.domain.fields.EmailField;
import com.stimulus.archiva.domain.fields.EmailFieldValue;
import com.stimulus.archiva.exception.MessageSearchException;
import com.stimulus.archiva.language.AnalyzerFactory;
import com.stimulus.archiva.security.realm.MailArchivaPrincipal;
import com.stimulus.util.Compare;
import com.stimulus.util.DateUtil;

public class StandardSearch extends Search {

	 /**
	 * 
	 */
	private static final long serialVersionUID = -4471820570355127398L;
	protected ArrayList<Criteria> criteria   = new ArrayList<StandardSearch.Criteria>();
	 protected String			compiledQuery = ""; 
	 protected static Logger logger = Logger.getLogger(StandardSearch.class.getName());
	 protected SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
	 protected Searcher searchers = null;
	public StandardSearch() {
	    newCriteria();
	}
	public void searchMessage() throws MessageSearchException {
		logger.debug("standard search executed {query='"+getSearchQuery()+"'}");
		
		Analyzer analyzer = AnalyzerFactory.getAnalyzer(getLanguage(),AnalyzerFactory.Operation.SEARCH);
		compileSearchQuery();
		Query query = getQuery(analyzer); 
        Sort sort = getSortPreference();
		Filter queryFilter = getFilter(new FilterAnalyzer());
		try { 
			/*if (searchers!=null) {
				 try {
						searchers.close();
				 } catch (IOException io) {
					logger.error("failed to close search indexes (opened for read)",io);
				 }
			}*/
			searchers = getVolumeSearchers();
			if (searchers!=null) {
				Hits hits = search(query, queryFilter, sort);
				compileSearchResults(hits); 
			}
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
			    QueryParser filterQueryParser = new QueryParser("to",new FilterAnalyzer());
			    Query query = filterQueryParser.parse(filterstr);
			    queryFilter = new QueryWrapperFilter(query);
			    logger.debug("successfully parsed filter query {query='"+filterstr+"'}");
			    logger.debug("queryfilter"+query.toString());
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
			  sort = new Sort(new SortField[]{new SortField(sortField,SortField.FLOAT,sortOrder)});
		  else if (sortField.equals("attach"))
			  sort = new Sort(new SortField[]{new SortField(sortField,SortField.INT,!sortOrder)});
		  else if (sortField.equals("score"))  
			  sort = new Sort(SortField.FIELD_SCORE);
		  else if (sortField.equals("subject") || sortField.equals("to") || sortField.equals("from"))
			  sort = new Sort(new SortField[]{new SortField(sortField+"s",SortField.STRING,sortOrder)});
		  else if (sortField.equals("sentdate"))
			  sort = new Sort(new SortField[]{new SortField(sortField,SortField.STRING,sortOrder)});
		  else if (sortField.equals("archivedate"))
			  sort = new Sort(new SortField[]{new SortField(sortField,SortField.STRING,sortOrder)});
		  else return null;
		  return sort;  	  
	  }
		  

	  /* date search filter */
    
	  protected String getDateFilter() {	
		  Date sentAfter = defaultDate(getAfter(),new Date(0));
		  Date sentBefore = defaultDate(getBefore(), new Date());
		  if (getDateType()==DateType.SENTDATE)
			  return "sentdate:[d"+DateUtil.convertDatetoString(sentAfter) + " TO d" + DateUtil.convertDatetoString(sentBefore)+"]";	
		  else // ARCHIVEDATE
			  return "archivedate:[d"+DateUtil.convertDatetoString(sentAfter) + " TO d" + DateUtil.convertDatetoString(sentBefore)+"]";	
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
	  			return "flag:"+flag.toString().toLowerCase(Locale.ENGLISH);
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
		  List<String> emailAddresses = ((MailArchivaPrincipal)getPrincipal()).getEmailAddresses();
		  if (emailAddresses !=null && emailAddresses.size()>0) {
			  
			  if (Compare.equalsIgnoreCase(((MailArchivaPrincipal)getPrincipal()).getRole(), "user")) {
				  String filterstr = "(";
				  for (String email : emailAddresses) {
					    email = email.replaceAll("\"", "\\\\\"");
					    filterstr += " to:\""+email+"\" deliveredto:\""+email+"\" from:\""+email+"\" cc:\""+email+"\" bcc:\""+email+"\"";
				  }
				  return filterstr+")";
			  } 
		  }
		  return "";
	  }

	  protected Searcher getVolumeSearchers() throws MessageSearchException {
		  	boolean searcherPresent = false;
		  	Date after = defaultDate(getAfter(),new Date(0));
			Date before = defaultDate(getBefore(), new Date());
			DateType dateType = getDateType();
		    List volumes = Config.getConfig().getVolumes().getVolumes();
			LinkedList<Searchable> searchers = new LinkedList<Searchable>();
			Iterator vl = volumes.iterator();
			while (vl.hasNext()) {
			    Volume volume = (Volume)vl.next();
			    if (volume.getModified()!=null && dateType==DateType.ARCHIVEDATE && after.compareTo(volume.getModified())>0) {
			    	logger.debug("standard search: not using volume {modified='"+volume.getModified()+"', ater='"+after+"',"+volume);
			    	continue;
			    }
			    if (volume.getCreated()!=null && dateType==DateType.ARCHIVEDATE && before.compareTo(volume.getCreated())<0) {
			    	logger.debug("standard search: not using volume {created='"+volume.getCreated()+"', before='"+before+"',"+volume);    
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
				return null;

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
	  
	  protected Hits search(Query query, Filter queryFilter, Sort sort) throws MessageSearchException  {
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
	  
	  protected void compileSearchResults(Hits hits) throws MessageSearchException {
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
				try {
					addMessage(hits,start);
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
	  }
	  
	  
	  protected void addMessage(Hits hits, int position) throws IOException {
		  results.add(new LuceneResult(hits,position));
		  
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
	    		    StringBuffer constructedQuery = new StringBuffer();
	    		    constructedQuery.append("(");
	    		    Iterator i = EmailField.getAvailableFields().values().iterator();
	    		    while (i.hasNext()) {
	    		    	EmailField ef = (EmailField)i.next();
	    		    	String field = ef.getName();
	    		    	
	    		    	// do not allow bcc and deliveredto fields
	    		    	
	    		    	if (Compare.equalsIgnoreCase(((MailArchivaPrincipal)getPrincipal()).getRole(), "user")) {
	    		    		if (Compare.equalsIgnoreCase(field, "bcc"))
	    		    				continue;
	    		    		if (Compare.equalsIgnoreCase(field, "deliveredto"))
	    		    				continue;
	    		    	}
	    		    	constructedQuery.append(field);
	    		    	constructedQuery.append(":");
	    		    	constructedQuery.append(token);
	    		    	constructedQuery.append(" ");
	    		    }
	    		    return constructedQuery.toString().trim() + ") ";
	    		}
	    		
	    		public String getConstructedQuery() {
	    		    StringBuffer constructedQuery = new StringBuffer();
	    		    if (Compare.equalsIgnoreCase(method, "all")) {
	    		    	boolean start = true;
	    		    	StringTokenizer allWordsTokenizer = new StringTokenizer(query);
	    		    	while (allWordsTokenizer.hasMoreTokens()) {
	    		    		if (!start) constructedQuery.append("AND ");
		  		  		    if (Compare.equalsIgnoreCase(field, "all")) {
		  		  		    	constructedQuery.append(allFields(allWordsTokenizer.nextToken()));
		  		  		    	constructedQuery.append(" ");    
		  		  		    } else {
		  		  	   			constructedQuery.append(field);
		  		  	   			constructedQuery.append(":");
		  		  	   			constructedQuery.append(allWordsTokenizer.nextToken());
		  		  	   			constructedQuery.append(" ");
		  		  		    }
		  		  	   		start=false;
	  		  			}
	    			} else if (Compare.equalsIgnoreCase(method, "exact")) {
	    				
	    			 	if (Compare.equalsIgnoreCase(field, "all")) {
	    					constructedQuery.append(allFields("\"" + query + "\" "));
	    			 	} else {
	    					constructedQuery.append(field);
	    					constructedQuery.append(":\"");
	    					constructedQuery.append(query);
	    					constructedQuery.append("\" ");
	    			 	}
	    		 	} else if (Compare.equalsIgnoreCase(method, "any")) {
	    		 		StringTokenizer anyWordsTokenizer = new StringTokenizer(query);
	    		 		while (anyWordsTokenizer.hasMoreTokens()) {
		  		  	   		if (Compare.equalsIgnoreCase(field, "all")) {
		  		  	   			constructedQuery.append(allFields(anyWordsTokenizer.nextToken()));
		  		  	   			constructedQuery.append(" "); 
		  		  	   		} else {
		  		  	   			constructedQuery.append(field);
		  		  	   			constructedQuery.append(":");
		  		  	   			constructedQuery.append(anyWordsTokenizer.nextToken());
		  		  	   			constructedQuery.append(" ");
		  		  	   		}
		  		  	   	}
	  		  	   
	    		 	} else if (Compare.equalsIgnoreCase(method, "none")) {
	    		 		StringTokenizer noWordsTokenizer = new StringTokenizer(query);
	    		 		while (noWordsTokenizer.hasMoreTokens()) {
		    		 		if (Compare.equalsIgnoreCase(field, "all")) {
		    		 			constructedQuery.append("-");
		    		 			constructedQuery.append(allFields(noWordsTokenizer.nextToken()));
		    		 			constructedQuery.append(" ");
		    		 		} else {
		    		 			constructedQuery.append("-");
		    		 			constructedQuery.append(field);
		    		 			constructedQuery.append(":");
		    		 			constructedQuery.append(noWordsTokenizer.nextToken());
		    		 			constructedQuery.append(" ");
		    		 		}
	    		 		}	
	    		 	}
	    		    return constructedQuery.toString().trim();
	    		}
	    }

	    protected void finalize() throws Throwable {
	    	/*
		    try {
				searchers.close();
		 } catch (IOException io) {
			logger.error("failed to close search indexes (opened for read)",io);
		 }*/
	   }
	    
	    private static double round(double val, int places)
		{
			long factor = (long) Math.pow(10, places);
			val = val * factor;
			long tmp = Math.round(val);
			return (double) tmp / factor;
		}

		private static float round(float val, int places)
		{
			return (float) round((double) val, places);
		}
		  
	 /*
	  *   protected void addMessage(Document doc,float score) {
		  
		  String uid 	  = doc.get("uid");
		  if (uid==null)  {
			  logger.warn("found message with null ID during construction of search results");
			  return;
		  }
		  
		  Volume volume = null;
		  try { 
			  volume = Config.getConfig().getVolumes().getVolume(uid);
		  } catch (Exception ce) {
			  logger.error("failed to set the volume associated with emailid");
		  }
		  
		  String ver = doc.get("ver"); 
		  EmailID emailID = new EmailID(uid,volume);
		  results.add(new LuceneResult(emailID, score, doc)); 
	  }
	  */
		
	    public class LuceneResult extends Search.Result implements Serializable{

	    	/**
			 * 
			 */
			private static final long serialVersionUID = 338211546039332906L;
			Document doc = null;
	    	Hits hits 	 = null;
	    	int position = -1;
	    	
	    	public LuceneResult(){
	    		super();
	    	}
	    	
	    	public LuceneResult(Hits hits, int position) {
	    		super();
	    		this.hits = hits;
	    		this.position = position;
	    	}
	    			
	    	public EmailID getEmailId() {
	    		Document doc = getDocument();
	    		String uid 	  = doc.get("uid");
	    		String name   = doc.get("vol");
	  		  	
	    		if (uid==null)  {
	  			  logger.warn("found message with null ID during construction of search results");
	  			  return null;
	  		  	}
	    		Volume volume = null;
	    		  try { 
	    			 if (name==null) // legacy
	    				 volume = Config.getConfig().getVolumes().getLegacyVolume(uid);
	    			 else
	    				 volume = Config.getConfig().getVolumes().getNewVolume(name);
		  		  } catch (Exception ce) {
		  			  logger.error("failed to set the volume associated with emailid");
		  		  }
		  		  return EmailID.getEmailID(volume,uid);
		  		
	    	}
	    	protected Document getDocument() {
	    		try {
		    		if (doc==null)
		    			doc = hits.doc(position);
		    		return doc;
	    		} catch (Exception e) {
	    			logger.error("failed to retrieve document from lucene hits object",e);
	    			return null;
	    		}
	    	}
	    	
	    	public EmailFieldValue getFieldValue(String key) {
	    		Document doc = getDocument();
	    		float score = 0;
	    		try {
	    			score = hits.score(position);
	    		} catch (IOException ioe) {
	    			logger.error("failed to retrieve score from hits object",ioe);
	    		}
	    		String value = "";
	    		EmailField field = EmailField.get(key);
	    		if (field!=null) {
	    			if (Compare.equalsIgnoreCase(key, "score"))
		    			return new EmailFieldValue(field,Float.toString(round(score * 100, 2)) + "%");
	    			EmailField.SearchMethod searchMethod = field.getSearchMethod();
					  if (searchMethod==EmailField.SearchMethod.STORED) {
						  value = doc.get(field.getIndexKey());
					  } else if (searchMethod==EmailField.SearchMethod.TOKENIZED_AND_STORED) {
						  value = doc.get(field.getIndexKey()+"s");
					  }
					  return new EmailFieldValue(field,value);
	    		} 
	    		logger.error("failed to retrieve email field {key='"+key+"'}");
	    		return null;
	    	}
	    	
	    	
	    }


}
