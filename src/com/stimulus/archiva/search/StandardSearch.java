
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
import java.rmi.Naming;
import java.security.Principal;
import org.apache.lucene.search.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import org.apache.commons.logging.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.*;
import com.stimulus.archiva.domain.*;
import com.stimulus.archiva.exception.MessageSearchException;
import com.stimulus.archiva.language.AnalyzerFactory;
import com.stimulus.archiva.webservice.*;

import java.util.*;
import com.stimulus.archiva.domain.fields.*;
import com.stimulus.util.*;
import com.stimulus.archiva.exception.*;

public class StandardSearch extends Search  implements Serializable, Config.UpdateObserver {

	private static final long serialVersionUID = 2998075692858439809L;
	
	protected String			compiledQuery = ""; 
	protected static final Log logger = LogFactory.getLog(StandardSearch.class.getName());
	protected Searcher searchers = null;
	protected Date lastSentAfter  = new Date();
	protected Date lastSentBefore = new Date();
	protected Analyzer analyzer; 
	protected boolean queryModified = true;
	protected boolean sortModified = true;
    protected boolean searchersModified = true;
    protected boolean filterModified = true;
	protected Query query;
	protected Sort sort;
	protected Filter queryFilter;
	protected String oldQuery = "";
	protected ArrayList<Result> results = new ArrayList<Result>();
	protected int totalHits = 0;
	protected String filterQuery = "";
	
	public StandardSearch() {
	    newCriteria();
	    reset();
	}
	
	public void updateConfig() {
		reset();
	}
	
	public void reset() {
		super.reset();
		init();
		viewFilter.clearCriteria();
		viewFilter.newCriteria();
	}
	
	
	public void init() {
		queryModified = true;
		sortModified = true;
		filterModified = true;
		searchersModified = true;
		setSearchQuery(null);
		setFilterQuery(null);
		totalHits = 0;
		
	}
	public void setSearchQuery(String searchQuery) {
		if (searchQuery!=null) {
			this.searchQuery = searchQuery.trim();
		} else {
			this.searchQuery = null;
		}
		
	}
	
	public String getSearchQuery() {
		return searchQuery;
	}
	
	public void setFilterQuery(String filterQuery) {
		if (filterQuery!=null) {
			this.filterQuery = filterQuery.trim();
		} else {
			this.filterQuery = null;
		}
	}
	
	public String getFilterQuery() {
		return filterQuery;
	}
	
	
	public synchronized void searchMessage() throws MessageSearchException {
	
		logger.debug("standard search executed {query='"+getSearchQuery()+"'}");
		
		// this a gui operation, must come back fast
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		
		if (getSearchQuery()==null) {
			setSearchQuery(compileSearchQuery(viewFilter.getCriteria()));
		}

		if (getSearchQuery().compareTo(oldQuery)!=0) {
			queryModified = true;
			oldQuery = getSearchQuery();
		}
		
		if (queryModified) {
			analyzer = AnalyzerFactory.getAnalyzer(getLanguage(),AnalyzerFactory.Operation.SEARCH);
			query = getQuery(analyzer); 
			queryModified = false;
		}
		
		if (sortModified) {
			sort = getSortPreference();
			sortModified = false;
		}

        //if (filterModified) {
        	queryFilter = getFilter(analyzer);
        	filterModified = false;
        //}
     
	        try { 
				
				if ((openIndex==OpenIndex.SEARCH) || (openIndex==OpenIndex.SESSION && searchersModified)) {
					try {
						if (searchers!=null) {
							try {
					    		logger.debug("//closing searchers");
//								searchers.close();
							} catch (Exception e) {}
						}
				    	searchers = getVolumeSearchers();
				    	if (searchers==null) {
				    		logger.debug("there are no volumes to search");
				    		init();
				    		return;
				    	}
				    	searchersModified = false;
	
					} catch (MessageSearchException mse) {
						logger.error("failed to create volume searchers:"+mse.getMessage(),mse);
						init();
						return;
					}  
				}
	
				search(query, queryFilter, sort);
				
			
			} catch (MessageSearchException mse) {
				logger.debug("standard search no volumes available for searching:"+mse.getMessage(),mse);
				init();
			} finally {
				// reset searchQuery
				searchQuery=null;
				filterQuery=null;
			}
      
	 }
	
	  protected Query getQuery(Analyzer analyzer) throws MessageSearchException {
		  	QueryParser queryParser = new QueryParser("body",analyzer);
		  	
	  		Query query;
	  		String dateQuery = getDateFilter();
	  		if (getSearchQuery().length()>0) {
	  			setSearchQuery(and(getSearchQuery(),dateQuery));
	  		} else { 
	  			setSearchQuery(dateQuery);
	  		}
	  		
	  		if (getSearchQuery()!=null && getSearchQuery().length()>0) {
		  			
		  		try {
		  			query = queryParser.parse(getSearchQuery());
		  			logger.debug("successfully parsed search query {query='"+getSearchQuery()+"'}");
		  		} catch (Exception e) {
		  			searchQuery = null;
		  			filterQuery=null;
		  			throw new MessageSearchException("failed to parse search query {query='"+getSearchQuery()+"'}",e,logger,ChainedException.Level.DEBUG);
		  		}
	  		} else {
	  			query = new MatchAllDocsQuery();
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
				filterQuery = attachF;
				filterQuery = and(filterQuery,flagF);
				filterQuery = and(filterQuery,priorityF);
				filterQuery = and(filterQuery,userRoleF);
			    logger.debug("standard search: parsing filter query {query='"+filterQuery+"'}"); 
			    if (filterQuery.length()==0)
			    	return null;
			    QueryParser filterQueryParser = new QueryParser("to",analyzer);
			    Query query = filterQueryParser.parse(filterQuery);
			    queryFilter = new QueryWrapperFilter(query);
			    logger.debug("successfully parsed filter query {query='"+filterQuery+"'}");
			} catch (Exception pe)
			{
				throw new MessageSearchException("failed to parse search query {searchquery='"+getSearchQuery()+"'}",pe,logger,ChainedException.Level.DEBUG);
			}
			return queryFilter;
	  }
	
	protected String and(String prevClause, String nextClause) {
		if (prevClause.isEmpty())
			return nextClause;
		if (nextClause.isEmpty())
			return prevClause;
		return prevClause + " AND " + nextClause;
	}
		
	  protected Sort getSortPreference() {
		  Sort sort;
		  String sortField = getSortField(); 
		  SortOrder so = getSortOrder();
		  boolean sortOrder;
		  if (so==SortOrder.DESCENDING)
			  sortOrder = true;
		  else if (so==SortOrder.ASCENDING)
			  sortOrder = false;
		  else
			  return null;
		  
		  Locale locale = new Locale(language);
		  if (sortField.equals("size") || sortField.equals("priority"))  
			  sort = new Sort(new SortField[]{new SortField(sortField,SortField.FLOAT,sortOrder)});
		  else if (sortField.equals("attach"))
			  sort = new Sort(new SortField[]{new SortField(sortField,SortField.INT,!sortOrder)});
		  else if (sortField.equals("score"))  
			  sort = new Sort(SortField.FIELD_SCORE);
		  else if (sortField.equals("subject") || sortField.equals("to") || sortField.equals("from"))
			  sort = new Sort(new SortField[]{new SortField(sortField+"s",sortOrder)});
		  else if (sortField.equals("sentdate"))
			  sort = new Sort(new SortField[]{new SortField(sortField,sortOrder)});
		  else if (sortField.equals("archivedate"))
			  sort = new Sort(new SortField[]{new SortField(sortField,sortOrder)});
		  else if (sortField.equals("receiveddate"))
			  sort = new Sort(new SortField[]{new SortField(sortField,sortOrder)});
		  else return null;
		  return sort;  	  
	  }
		  

	  /* date search filter */
    
	  protected String getDateFilter() {	
		  
		  if (getAfter()==null && getBefore()==null)
			  return "";
		  
		  Date sentAfter = defaultDate(getAfter(),new Date(0));
		  Date sentBefore = defaultDate(getBefore(), new Date());
		  if (getDateType()==DateType.SENTDATE) {
			  return "sentdate:[d"+DateUtil.convertDatetoString(sentAfter) + " TO d" + DateUtil.convertDatetoString(sentBefore)+"]";	
		  } else if (getDateType()==DateType.ARCHIVEDATE) {
			  return "archivedate:[d"+DateUtil.convertDatetoString(sentAfter) + " TO d" + DateUtil.convertDatetoString(sentBefore)+"]";	
		  } else {
			  return "receiveddate:[d"+DateUtil.convertDatetoString(sentAfter) + " TO d" + DateUtil.convertDatetoString(sentBefore)+"]";	
		  }
		  
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
		  
		  if (getPrincipal()==MailArchivaPrincipal.SYSTEM_PRINCIPAL)
			  return "";
		  
		  String roleStr = ((MailArchivaPrincipal)getPrincipal()).getRole();
		  Roles.Role role = Config.getConfig().getRoles().getRole(roleStr);
		  String filterQuery = compileSearchQuery(role.getViewFilter().getCriteria());

		  if (filterQuery.contains("%email%")) {
			  
			  List<String> emailAddresses = ((MailArchivaPrincipal)getPrincipal()).getEmailAddresses();
			  if (emailAddresses !=null && emailAddresses.size()>0) {
				  	  String newFilterQuery = "";
					  for (String email : emailAddresses) {
						  newFilterQuery += "(" + filterQuery.replaceAll("%email%",email.trim()) + ") ";
					  }
					  filterQuery = newFilterQuery.trim();
			  } 
			 
		  }
		 
			  	
	  	 if (filterQuery.length()>0 && !filterQuery.endsWith(")"))
	  		 filterQuery = "(" + filterQuery + ")";
	  
		  logger.debug("getUserRoleFilter() {filterQuery='"+filterQuery+"'}");
		  
		  return filterQuery;
	  }

	  
	  public boolean shouldSearch(Volume v) {

      	 if (v.getStatus()!=Volume.Status.ACTIVE &&
      		 v.getStatus()!=Volume.Status.CLOSED) // we still want to display the messages
      		return  false;
      	 
		  if (getBefore()==null || getAfter()==null)
			  return true;
		  
		  if (this.getDateType()==DateType.SENTDATE || this.getDateType()==DateType.RECEIVEDDATE) {
			  return true;
		  } else if (this.getDateType()==DateType.ARCHIVEDATE) {
			  	  
			  	 /*Calendar closedDate = Calendar.getInstance();
		 		 File store = new File(v.getPath());
		 		 if (!store.exists())
		 			 return true;
		 			 
		 		//closedDate.setTimeInMillis(store.lastModified());
		 		 * 
		 		 */
		 		logger.debug("should search check {datetype='"+getDateType()+"',closeDate='"+v.getClosedDate()+"',after='"+getAfter()+"',createdDate='"+v.getCreatedDate()+"',before='"+getBefore()+"'}");
		 		if (v.getClosedDate()!=null && v.getClosedDate().before(getAfter()))
		 			return false;
		 		if (v.getCreatedDate()!=null && v.getCreatedDate().after(getBefore()))
		 			return false;
		 		 
		  }
		  return true;
	  }
	  
	  protected Searcher getVolumeSearchers() throws MessageSearchException {
		  	
		    logger.debug("getVolumeSearchers()");
		  	boolean searcherPresent = false;
			Hashtable<String,String> remoteServers = new Hashtable<String,String>();
		    List<Volume> volumes = Config.getConfig().getVolumes().getVolumes();
			LinkedList<Searchable> searchers = new LinkedList<Searchable>();
			Iterator<Volume> vl = volumes.iterator();
			logger.debug("searching for suitable searchers");
			while (vl.hasNext()) {
				Volume volume = (Volume)vl.next();
			    logger.debug("should search volume? {"+volume+"}");
			    try {
			    		Searchable volsearcher;
			    		if (shouldSearch(volume)) {
			            			try {
				            			volsearcher = new IndexSearcher(volume.getIndexPath());
				            			logger.debug("adding volume to search {indexpath='"+volume.getIndexPath()+"'}");
				            			searchers.add(volsearcher); 
				            			searcherPresent = true; 
				            		} catch (Exception e) {
				            			logger.error("failed to volume to search{"+volume+"}: "+e.getMessage(),e);
				            		}
	            		} else {
	            			logger.debug("deliberately not searching inside volume {"+volume.getIndexPath()+"}");
	            		}
			    } catch (Exception io) {
			    	logger.error("failed to open index for search {"+volume+"}.",io);
			    }
				    
			}
		
			if (!searcherPresent)
				return null;
			
			for (String remotePath : remoteServers.values()) {
				try {
					Searchable volsearcher = (Searchable)Naming.lookup(remotePath);
					searchers.add(volsearcher);
				} catch (Exception e) {
					logger.error("failed to add volume searcher",e);
				}
			}
			Searchable[] searcherarraytype = new Searchable[searchers.size()];
			Searchable[] allsearchers = (Searchable[])(searchers.toArray(searcherarraytype));

			Searcher searcher;
			 try {
			     searcher = new ParallelMultiSearcher(allsearchers);
			} catch (IOException io) {
					throw new MessageSearchException("failed to open/create one or more index searchers",logger);
			}
			return searcher;
	  }
	  
		public List<Result> getResults() {
			return results;
		}
		
		public int getResultSize() {
			if (results!=null)
				return results.size();
			else
				return 0;
		}
		
		public int getTotalHits() {
			return totalHits;
		}
		
	
	  protected void search(Query query, Filter queryFilter, Sort sort) throws MessageSearchException  {
		  try {
			   logger.debug("start search  {searchquery='"+getSearchQuery()+"'}");
			   
			   Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
			   results.clear(); 
			   totalHits = getMaxSearchResults();
			   TopFieldDocs topDocs = searchers.search(query,queryFilter,totalHits,sort);
			   totalHits = topDocs.scoreDocs.length;
			   for (int i=0; i < totalHits; i++) {
					results.add(new LuceneResult(searchers,topDocs.scoreDocs[i]));
			   }
			   logger.info("search executed successfully {query='"+getSearchQuery()+"',returnedresults='"+results.size()+"'}");
		  } catch (IOException io) {
			throw new MessageSearchException("failed to execute search query {searchquery='"+getSearchQuery()+"}",io,logger,ChainedException.Level.DEBUG);
		  }
	  }
	  
	 
	
		public String compileSearchQuery(List<Criteria> criteria)
		{
			
			StringBuffer sQuery = new StringBuffer();
		 	boolean operator = true;
			if (criteria.size()>1) {
	  				sQuery.append("(");
			}
			
	  	  	for (Criteria c : criteria) {
	  	  		if (c.getQuery().length()<1)
	  	  			continue;
	  	  		String constructedQuery = c.getConstructedQuery().trim();
	  	  		if (constructedQuery.length()>0) {
	  	  			String sQ = sQuery.toString();
	  	  			String q2 = c.getQuery();
	  	  		    if (!operator) {
	  	  		    	sQuery.append(c.getOperator().toString().toUpperCase(Locale.ENGLISH));
	  	  		    }
	  	  		    operator = false;
	  	  			if (c.getMethod()==Criteria.Method.NONE) {
	  	  			 sQuery.append(" " + constructedQuery + " ");
	  	  			} else {
	  	  				sQuery.append(" (" + constructedQuery + ") ");
	  	  			}
	  	  		}
	  	  		  
	  	  	}
	  	  	if (criteria.size()>1) {
				sQuery.append(")");
	  	  	}
	  	  	return sQuery.toString();
		}

	    /* criteria */
	    
	    public void clearCriteria() {
	    	viewFilter.clearCriteria();
	    	queryModified = true;
	    }
	    
	    public void newCriteria() {
	          logger.debug("newCriteria()");
	          viewFilter.addCriteria(new Criteria("subject"));
	          queryModified = true;
	    }
	    
	    public List<Criteria> getCriteria() {
	    	return viewFilter.getCriteria();
	    }
	    
	    public void deleteCriteria(int id) {
	      logger.debug("deleteToCriteria() {index='"+id+"'}");  
	      viewFilter.deleteCriteria(id);
	    	queryModified = true;
	    }
	    
	    protected void finalize() throws Throwable {
	    	Config.getConfig().unregisterUpdateObserver(this);
	    	results = null;
	    	/*
		    try {
				searchers.close();
		 } catch (IOException io) {
			logger.error("failed to close search indexes (opened for read)",io);
		 }*/
	   }
	    
	 
	    
		  public long getTotalMessageCount(Volume volume) throws MessageSearchException {
			  if (volume == null)
		            throw new MessageSearchException("assertion failure: null volume",logger);
			  //logger.debug("get total no emails {indexpath='"+volume.getIndexPath()+"'}");
		      int count = 0;
			  File indexDir = new File(volume.getIndexPath());
		      if (!indexDir.exists())
		    	  	return 0;
		      IndexReader indexReader = null;
		      try {
		    	  indexReader = IndexReader.open(indexDir);
		    	  count += indexReader.numDocs();
		    	  indexReader.close();
		      } catch (IOException e ) {
		    	  logger.debug("failed to open index to calculate total email count", e);
		    	  //throw new MessageSearchException("failed to open index to calculate total email count",e,logger);
		      }
			  return count;
		  }
		  
		// sort modified
		  
		 public void setSortField(String field)
		 {
			 if (getSortField()!=field) {
				 super.setSortField(field);
				 sortModified = true;
			 }
		 }
		  
		public void setSortOrder(SortOrder sortOrder)
		{
			if (getSortOrder()!=sortOrder) {
				super.setSortOrder(sortOrder);
				sortModified = true;
			}
		}
		
		  public void setLanguage(String language) {
		    	if (getLanguage().compareTo(language)!=0) {
		    		super.setLanguage(language);
		    		sortModified = true;	
		    		searchersModified = true;
		    	}
		    }
		  
		// date modified
	
		  
		public void setAfter(Date sentAfter)
		{
			if (getAfter()==null || sentAfter==null || getAfter().compareTo(sentAfter)!=0) {
				super.setAfter(sentAfter);
				searchersModified = true;
				queryModified = true;
			}
		}
		
		public void setBefore(Date sentBefore)
		{
			if (getBefore()==null || sentBefore==null || getBefore().compareTo(sentBefore)!=0) {
				super.setBefore(sentBefore);
				searchersModified = true;
				queryModified = true;
			}
		}
		
		public void setDateType(DateType dateType) {
			if (getDateType()!=dateType) {
				super.setDateType(dateType);
				searchersModified = true;
				queryModified = true;
				sortModified = true;
			}
		}
		
		public void setMaxSearchResults(int maxSearchResults) {
			if (getMaxSearchResults()!=maxSearchResults) {
				super.setMaxSearchResults(maxSearchResults);
				searchersModified = true;
			}
		}
		
		
		// filter modified
		
		public void setFlag(Flag flag) {
			if (getFlag()!=flag) {
				super.setFlag(flag);
	    		filterModified = true;
			}
		}
		 
	    public void setPriority(Priority priority) {
	    	if (getPriority()!=priority) {
	    		super.setPriority(priority);
	    		filterModified = true;
	    	}
	    }
	    
	    public void setAttachment(Attachment attach) {
	    	if (getAttachment()!=attach) {
	    		super.setAttachment(attach);
	    		filterModified = true;
	    	}
	    }

	    public void setPrincipal(Principal principal) {
	    	if (getPrincipal()!=principal) {
	    		super.setPrincipal(principal);
	    		filterModified = true;
	    	}
		}
	
	 

}
