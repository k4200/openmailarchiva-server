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

package com.stimulus.archiva.persistence;

import org.apache.lucene.document.*;
import java.io.*;
import com.stimulus.util.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.queryParser.*;
import com.stimulus.archiva.domain.*;
import com.stimulus.archiva.exception.*;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.analysis.standard.*;
import org.apache.log4j.Logger;
import javax.mail.*;

import com.stimulus.archiva.exception.MessageSearchException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.lucene.search.QueryFilter;
import com.stimulus.archiva.persistence.textextraction.*;


public class MessageSearch
{
	/* Private Methods */
	  protected static final Logger logger = Logger.getLogger(MessageSearch.class.getName());

	  protected StandardAnalyzer analyzer;
	  protected LinkedList indexQueue = new LinkedList();
	  protected static int INDEX_WAIT_PERIOD = 500;
	  protected boolean shutdown = false;
	  protected static int MAX_SEARCH_RESULTS = 200;
	  private SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
	  protected MessageStore ms = null;

	  public MessageSearch(MessageStore ms) {
	      this.ms = ms;
	      new IndexWorker().start();
	  }

	  protected String getNormalizedMimeType(String mimeType)
	  {
	  	// check this code may be a dodgy
		int index = mimeType.indexOf(";");
		if (index!=-1)
			mimeType = mimeType.substring(0,index);
		mimeType.toLowerCase();
		mimeType.trim();
	  	return mimeType;
	  }


	  private void extractAndIndex(InputStream is, String mimetype, String disposition, Document doc) {
	    
	      // logging stuff
	    /*
	      if (logger.isDebugEnabled() && mimetype.compareToIgnoreCase("application/zip")!=0) {
  			int i = 0;
  			try {
  			    Reader debugTextReader = Extractor.getText(is,mimetype);
  			    if (textReader!=null) {
	  			    BufferedReader debugBreader = new BufferedReader(debugTextReader);
	  	  			logger.debug("message part content decoded successfully (10 lines max will be shown):");
	  	  			for (String line = debugBreader.readLine();line != null; line = debugBreader.readLine()) {
		            		i++; if (i>10) break;
		            		logger.debug("line ("+i+"):"+line);
		            	 }
		        }
  			} catch (Exception ee) {
  	  		  logger.debug("failed to decode message part content (mimetype unsupported?) {mimetype='"+mimetype+"'}");
  	  		}
  		}*/
	    // application stuff
	    TempFiles tempFiles = new TempFiles(); // make sure we delete temporary files
  		if (mimetype.compareToIgnoreCase("application/zip")==0) {
  		    ZipInputStream zis = new ZipInputStream(is);
  		    ZipEntry entry;
 	         	try {
  		    	while((entry = zis.getNextEntry()) != null) {
     	             String name = entry.getName();
     	             int dot = name.lastIndexOf('.');
     	             if (dot==-1)
     	                 continue;
     	             String extention = name.substring(dot+1,name.length());
     	             try {
     	                Reader textReader = Extractor.getText(zis,extention,tempFiles);
	     			  		if (textReader!=null) {
		     	                if (disposition.equalsIgnoreCase(Part.ATTACHMENT)) 
		     			  		    doc.add(Field.Text("attachments", textReader));
		     			  		else
		     			  		    doc.add(Field.Text("body", textReader));
	     			  		}
	       	          } catch (ExtractionException ee) {
	       	              logger.debug("failed to decode message part content (mimetype unsupported?) {extention='"+extention+"'}");
	       	          }
 	         	}
 	         	zis.close();
 	         	} catch (IOException io) {
 	         	    logger.error(io.getMessage(),io);
 	         	}
  		} else {
  		    try {
  		        Reader textReader = Extractor.getText(is,mimetype,tempFiles);
  		        if (textReader!=null) {
	  		        if (disposition!=null && disposition.equalsIgnoreCase(Part.ATTACHMENT)) 
			  		    doc.add(Field.Text("attachments", textReader));
			  		else
			  		    doc.add(Field.Text("body", textReader));
  		        }
  		        //try { textReader.close(); } catch (Exception e) {}
  		    } catch (ExtractionException ee) {
   	              logger.debug("failed to decode message part content (mimetype unsupported?) {mimetype='"+mimetype+"'}");
   	        }
  		}
  		logger.debug("message part content decoded successfully");
	  }
	  
	  protected void writeBodyPartsToDocument(Object bodyPart, String mimeTypeA, String disposition, Document doc)throws MessagingException,IOException
	  {
	    String mimetype = getNormalizedMimeType(mimeTypeA);
	    logger.debug("extracting message part {mimetype = '"+mimetype+"'}");

	    if (bodyPart instanceof String) {
	  		String bodyPartStr = (String)bodyPart;
	  		extractAndIndex(new ByteArrayInputStream(bodyPartStr.getBytes()),mimetype,disposition,doc);
        } else if (bodyPart instanceof InputStream) {
            extractAndIndex((InputStream)bodyPart,mimetype,disposition, doc);
        } else if (bodyPart instanceof Multipart) {
        	logger.debug("message contains a multiple parts.");
            Multipart mp = (Multipart)bodyPart;
            for (int j = 0; j < mp.getCount(); j++) {
            	writeBodyPartsToDocument(mp.getBodyPart(j).getContent(),mp.getBodyPart(j).getContentType(),mp.getBodyPart(j).getDisposition(),doc);
            }
        } else
        	logger.debug("failed to identify message part."+bodyPart);
	  }

	  protected void writeMessageToDocument(Email message, Document doc) throws MessagingException,IOException,ExtractionException
	  {
	      if (message.getEmailID()==null)
		  	     throw new MessagingException("failed to index message as unique ID is null");

	  	 String uniqueID 	= message.getEmailID().getUniqueID();
		 String subject 	= message.getSubject();
		 String to 			= message.getToAddresses();
		 String from 		= message.getFromAddress();
		 Date sent 			= message.getSentDate();
		 int size 			= message.getSize();

		 logger.debug("writing message to document {contenttype='" + message.getContentType()+"', "+message+"}");

		 doc.add(Field.Keyword("uid",uniqueID));

		 if (subject!=null)
		     doc.add(new Field ("subject", subject, false, true, true));

		 if (to!=null)
		     doc.add(new Field ("to", to, false, true, true));
		    
		 if (from!=null)
		     doc.add(new Field ("from", from, false, true, true));
		   

		 if (sent!=null) {
		     String sentDateStr = "d"+format.format(sent);
		     doc.add(new Field ("sentdate", sentDateStr, false, true, false));
		 }

		 doc.add (new Field ("size", Integer.toString(message.getSize() / 1024 ), false, true, false));
		 Object bodyPart = message.getContent();
	  	 writeBodyPartsToDocument(bodyPart,message.getContentType(),message.getDisposition(),doc);
	  }



	  public void deleteMessage(Email email) throws MessageSearchException {

	  }

	  public void indexMessage(EmailID emailID) throws MessageSearchException {
	      if (emailID==null)
	            throw new MessageSearchException("assertion failure: null emailId",logger);
	      indexQueue.addFirst(emailID);
	  }

	  public String createIndexDir(Volume volume) throws MessageSearchException {

	      if (volume==null)
	            throw new MessageSearchException("assertion failure: null volume",logger);

	      File indexDir = new File(volume.getIndexPath());
          if (!indexDir.exists()) {
   			logger.info("index directory does not exist. will proceed with creation {location='" + volume.getIndexPath() + "'}");
   			boolean success = indexDir.mkdir();
   			if (!success)
   					throw new MessageSearchException("failed to create index directory {location='" + volume.getIndexPath() + "'}",logger);
   			logger.info("index directory successfully created {location='" + volume.getIndexPath() + "'}");
   		  }
          return volume.getPath();
	  }

	  public Search searchMessage(Search search) throws MessageSearchException {
	      if (search==null)
	           throw new MessageSearchException("assertion failure: null search",logger);
	      
	      if (search.getUserRole()==null || search.getUserName()==null)
	          throw new MessageSearchException("assertion failure: null userRole or userName",logger);
	      	
	        String queryStr = search.getSearchQuery();
	        
	        Date sentAfter = search.getSentAfter();
	        Date sentBefore = search.getSentBefore();
	        if (sentAfter==null)
	            sentAfter = new Date(0);
	        if (sentBefore==null)
	            sentBefore = new Date();
	  		if (queryStr.trim().compareTo("")==0)
	  		    queryStr += " sentdate:[d"+format.format(sentAfter) + " TO d" + format.format(sentBefore)+"]";	
	  		logger.debug("search {searchquery='"+queryStr+"'}");
	  		ParallelMultiSearcher searcher = null;
		    ArchivaAnalyzer analyzer = new ArchivaAnalyzer();

	  		Query query = null;
	  		QueryFilter filter = null;
	  		Query filterQuery = null;

			try {
			    query = QueryParser.parse(queryStr,"body",analyzer);

			    //query = MultiFieldQueryParser.parse(queryStr,fields, analyzer);
				//query = QueryParser.parse(queryStr,"body", analyzer);
			    if (search.getUserRole().compareToIgnoreCase("user")==0) {
				    filterQuery = QueryParser.parse("to:"+search.getUserName()+" from:"+search.getUserName(),"to",analyzer);
				    filter = new QueryFilter(filterQuery);
				}
			} catch (ParseException pe)
			{
				throw new MessageSearchException("failed to parse search query {searchquery='"+search.getSearchQuery()+"'}",pe,logger);
			}

			List volumes = Config.getConfig().getVolumes().getVolumes();
			ArrayList searchers = new ArrayList();
			Iterator vl = volumes.iterator();
			
			while (vl.hasNext()) {
			    Volume volume = (Volume)vl.next();
			    if (volume.getModified()!=null && sentAfter.compareTo(volume.getModified())>0)
			            continue;
			    if (volume.getCreated()!=null && sentBefore.compareTo(volume.getCreated())<0)
			            continue;
			    try {
			        if (volume.getStatusID()==Volume.ACTIVE || volume.getStatusID()==Volume.CLOSED) {
			            Searcher volsearcher = new IndexSearcher(volume.getIndexPath());
			            searchers.add(volsearcher);
			        }
			    } catch (IOException io) {
			        logger.error("failed to open index for search {"+volume+"}.");
			    }
			}

			/*Searcher[] searchers = new Searcher[volumes.size()];

			for (int j=0;j<volumes.size();j++) {
			    Volume volume = (Volume)volumes.get(j);
			    try {
			    searchers[j] = new IndexSearcher(volume.getIndexPath());
			    } catch (IOException io) {
					throw new MessageSearchException("failed to open index for searching {"+volume+"}",logger);
			    }
			}*/
			Searcher[] searcherarraytype = new Searcher[searchers.size()];
			Searcher[] allsearchers = (Searcher[])(searchers.toArray(searcherarraytype));

			logger.debug("searching for email {query="+queryStr+"}");
			 try {
			     searcher = new ParallelMultiSearcher(allsearchers);
			} catch (IOException io) {
					throw new MessageSearchException("failed to open/create one or more index searchers",logger);
			}

			Hits hits = null;
			try {
				    Sort sort;
				    String sortField = search.getSortField();
				    boolean sortOrder = search.getSortOrder();
				    if (sortField.compareToIgnoreCase("score")==0)
				        sort = new Sort(new SortField[]{SortField.FIELD_SCORE,new SortField("sentdate",false)});
				    else if (sortField.compareToIgnoreCase("sentdate")==0) {
				        sort = new Sort(new SortField(sortField,SortField.STRING,sortOrder));
				    } else
				        sort = new Sort(new SortField[]{new SortField(sortField,SortField.STRING,sortOrder),SortField.FIELD_SCORE,new SortField("sentdate",false)});
					//logger.debug("executing search query {query='"+query+"',filter='"+filter+"',sort='"+sort+"'}");

				    hits = searcher.search(query,filter,sort);
					logger.info("search executed successfully {query='"+query.toString()+"', nohits='"+hits.length()+"'}");
			 } catch (IOException io)
			 {
				throw new MessageSearchException("failed to execute search query {searchquery='"+search.getSearchQuery()+"}",io,logger);
			 }

				String messageUidDebugOutput = "search results returned following unique message ID's:";

			long norecords = hits.length() > MAX_SEARCH_RESULTS ? MAX_SEARCH_RESULTS : hits.length();

			for (int start = 0; start < norecords; start++)
			{
			    Document doc = null;
				try {

					doc = hits.doc(start);
					float score = hits.score(start);
					search.addMessage(new LuceneEmailID(start,hits),score);
					messageUidDebugOutput+=doc.get("uid")+",";
				} catch (IOException io)
				{
					throw new MessageSearchException("failed to retrieve indexed value from search query {searchquery='"+search.getSearchQuery()+"'}",io,logger);
				}
			}
			logger.debug(messageUidDebugOutput);
			try {
				searcher.close();
			} catch (IOException io) {
				throw new MessageSearchException("failed to close search indexes (opened for read)",io,logger);
			}

			return search;
	  }

	  // deliberately non recursive (so we avoid situations where the whole h/d is deleted)
	  public void deleteIndex(Volume volume) throws MessageSearchException {
	      if (volume == null)
	            throw new MessageSearchException("assertion failure: null volume",logger);

	      logger.debug("delete index {indexpath='"+volume.getIndexPath()+"'}");
	      File indexDir = new File(volume.getIndexPath());
	      if (!indexDir.exists()) return;
	      if (indexDir.isDirectory()) {
	            String[] children = indexDir.list();
	            for (int i=0; i<children.length; i++) {
	                String filepath = volume.getIndexPath()+File.separatorChar+children[i];
	                logger.debug("deleting file {path='" + filepath +"'}");
	                boolean success = new File(filepath).delete();
	                if (!success)
                        throw new MessageSearchException("failed to delete file in existing index {filepath='"+filepath+"'}",logger);
                     else
                         logger.debug("deleted file successfully {filepath='" + filepath +"'}");
	            }
	      }
	  }


	  private class IndexWorker extends Thread {

	    	IndexWriter writer 		= null;
    		ArchivaAnalyzer analyzer = new ArchivaAnalyzer();
    		Volume volume 			= null;
    		
    		boolean	volumeOpened   = false;

	        public void run() {

	            while (!shutdown) {
	                	Email email = null;
	                	volumeOpened = false;
	                	 while (!indexQueue.isEmpty()) {
	           		          EmailID emailId = (EmailID)indexQueue.removeLast();
	           		          Volume newVolume = emailId.getVolume();
	           		          try {
		           		          if (!volumeOpened || newVolume!=volume) {
		           		              volume = newVolume;
		           		              openIndex(volume);
		           		              volumeOpened = true;
		           		          }
	           		              email = ms.retrieveMessage(emailId, true, true, false);
	           		              index(email);
	           		          } catch (MessageStoreException mste) {
	           		              try { ms.copyEmailToNoIndexQueue(emailId); } catch (Exception e) {};
	           		              logger.debug("failed to retrieve message for indexing {"+emailId+"}");
	           		          } catch(MessageSearchException mse) {
	           		           	  try { ms.copyEmailToNoIndexQueue(emailId); } catch (Exception e) {};
	           		              logger.error("failed to index message {"+email+"}");
	           		          }
           		      	}
		      	      if (volumeOpened)
        		          closeIndex();
		      	        try { Thread.sleep(INDEX_WAIT_PERIOD); } catch(Exception e) {}
	            }

	        }


	        public void openIndex(Volume volume) throws MessageSearchException {
	            if (volume == null)
		            throw new MessageSearchException("assertion failure: null volume",logger);
	            logger.info("opening index for write {"+volume+"}");
	            createIndexDir(volume);
 	    		File indexFile = new File(volume.getIndexPath()+File.separatorChar+"segments"); // create index if segments doesn't exist
  	    		boolean createIndex = !indexFile.exists();
  	    		if (createIndex)
  	    			logger.debug("index does not exist. creating new index {segments='"+indexFile.getPath()+"'}");
 	    		logger.debug("opening search index for write {indexpath='"+volume.getIndexPath()+"', createIndex='"+createIndex+"'}");
 	    		try {
 	    			writer = new IndexWriter(volume.getIndexPath(), analyzer, createIndex);
 	    			writer.mergeFactor = 2;
 	    			writer.maxMergeDocs = 7000000;
 	    			writer.minMergeDocs = 5;
 	    	  	} catch (IOException io)
 	    		{
 	    	  	    throw new MessageSearchException("failed to open/create index writer {location='"+volume.getIndexPath()+"'}",io,logger);
 	    		}
	        }

	        public void index(Email message) throws MessageSearchException {
	            if (message == null)
		            throw new MessageSearchException("assertion failure: null message",logger);

    	    	logger.debug("indexing message {"+message+"}");
    	    	Document doc = new Document();

	    		try {
	    		   writeMessageToDocument(message,doc);
	    	 	   writer.addDocument(doc);
	    	 	   logger.debug("message indexed successfully {"+message+"}");
	    		} catch (MessagingException me)
	    		{
	    		   throw new MessageSearchException("failed to decode message during indexing",me,logger);
	    		} catch (IOException me) {
	    		    throw new MessageSearchException("failed to index message {"+message+"}",me,logger);
	    		} catch (ExtractionException ee)
	    		{
	    		   throw new MessageSearchException("failed to decode attachments in message {"+message+"}",ee,logger);
	    		} catch (Exception e) {
	    		    throw new MessageSearchException("failed to index message",e,logger);
	    		}
	        }

	        public void closeIndex() {
	            try
    			{
    				logger.debug("closing index");
    				if (writer!=null)
    				    writer.close();
    			} catch (Exception io) {}
	        }
	    }





}
