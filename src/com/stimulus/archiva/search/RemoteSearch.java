package com.stimulus.archiva.search;

import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ParallelMultiSearcher;
import org.apache.lucene.search.RemoteSearchable;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.search.Searcher;

import com.stimulus.archiva.domain.Config;
import com.stimulus.archiva.domain.Volume;

public class RemoteSearch  implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5805774014063729997L;
	protected static final Logger logger = Logger.getLogger(RemoteSearch.class.getName());
	
	static {
		try {
			logger.debug("creating rmi registry {objectID='1099'}");
			LocateRegistry.createRegistry(1099);
		} catch (Exception e) {
			
		}
	}
	  public static void bindRemoteSearchables()  {
	    	try { 
	    		logger.debug("binding remote searchables");
	    		List<Volume> volumes = Config.getConfig().getVolumes().getVolumes();
	    		LinkedList<Searchable> searchers = new LinkedList<Searchable>();
	    		for (Volume v: volumes) {
	    			if (v.getAllowRemoteSearch() && !v.isRemote() && v.getStatus()==Volume.Status.CLOSED || v.getStatus()==Volume.Status.ACTIVE) {
	    				Searchable volsearcher = new IndexSearcher(v.getIndexPath());
		            		try {
		            			searchers.add(volsearcher); 
		            		} catch (Exception e) {
		            			logger.error("failed to add searcher {"+v+"}: "+e.getMessage(),e);
		            		}
	    			}
	    		}
	    		if (searchers.size()>0) {
		    		Searcher[] searcherarraytype = new Searcher[searchers.size()];
					Searcher[] allsearchers = (searchers.toArray(searcherarraytype));
					Searcher searcher = new ParallelMultiSearcher(allsearchers);
					RemoteSearchable remoteSearchable = new RemoteSearchable(searcher);
					Naming.rebind("mailarchiva",remoteSearchable);
	    		}
	    	} catch (Exception e) {
	    		logger.error("failed to bind remote access searchables",e);
	    	}
	    }
}
