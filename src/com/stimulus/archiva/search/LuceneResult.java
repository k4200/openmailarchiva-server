package com.stimulus.archiva.search;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.*;
import com.stimulus.archiva.domain.Config;
import com.stimulus.archiva.domain.EmailID;
import com.stimulus.archiva.domain.Search;
import com.stimulus.archiva.domain.Volume;
import com.stimulus.archiva.domain.fields.EmailField;
import com.stimulus.archiva.domain.fields.EmailFieldValue;
import com.stimulus.archiva.domain.fields.EmailFields;
import com.stimulus.archiva.exception.ChainedException;
import com.stimulus.archiva.exception.MessageSearchException;
import com.stimulus.util.Compare;


public class LuceneResult extends Search.Result {

  	Document doc = null;
  	ScoreDoc scoreDoc;
  	Searcher searcher;
  	
  	protected static final Log logger = LogFactory.getLog(LuceneResult.class.getName());
  	
  	public LuceneResult(Searcher searcher, ScoreDoc scoreDoc) {
  		this.scoreDoc = scoreDoc;
  		this.searcher = searcher;
  	}
  			
  	public EmailID getEmailId() throws MessageSearchException {
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
  			  logger.error("failed to lookup the volume from index information");
  		  }
	  	  return EmailID.getEmailID(volume,uid);
	  		
  	}
  	protected Document getDocument() throws MessageSearchException {
  		try {
  			
  			if (doc==null)
	    			doc = searcher.doc(scoreDoc.doc);
  			return doc;
  		} catch (Exception e) {
  			throw new MessageSearchException("failed to retrieve document from hits:"+e.getMessage(),e,logger,ChainedException.Level.DEBUG);
  			
  		}
  	}
  	
  	public EmailFieldValue getFieldValue(String key) throws MessageSearchException {
  		Document doc = getDocument();
  		String value = "";
  		EmailFields emailFields = Config.getConfig().getEmailFields();
  		EmailField field = emailFields.get(key);
  		if (field!=null) {
	  			if (Compare.equalsIgnoreCase(key, "score")) {
	  				float score = 0;
			    	score = scoreDoc.score;
			    	return new EmailFieldValue(field,Float.toString(round(score * 100, 2)) + "%");
	  			}
  				EmailField.SearchMethod searchMethod = field.getSearchMethod();
			    if (searchMethod==EmailField.SearchMethod.STORED) {
				  value = doc.get(field.getIndex());
			    } else if (searchMethod==EmailField.SearchMethod.TOKENIZED_AND_STORED) {
				  value = doc.get(field.getIndex()+"s");
			    }
			    return new EmailFieldValue(field,value);
  		} 
  		logger.error("failed to retrieve email field {key='"+key+"'}");
  		return null;
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
	  

  	
  }
  
