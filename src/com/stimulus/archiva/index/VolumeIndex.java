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
package com.stimulus.archiva.index;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import javax.mail.MessagingException;
import org.apache.commons.logging.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.store.FSDirectory;
import com.stimulus.archiva.domain.Config;
import com.stimulus.archiva.domain.Email;
import com.stimulus.archiva.domain.EmailID;
import com.stimulus.archiva.domain.Indexer;
import com.stimulus.archiva.domain.Volume;
import com.stimulus.archiva.exception.*;
import com.stimulus.archiva.language.AnalyzerFactory;
import com.stimulus.archiva.search.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.AlreadyClosedException;
import java.util.concurrent.locks.ReentrantLock;

public class VolumeIndex extends Thread {
	
		 protected static final Log logger = LogFactory.getLog(VolumeIndex.class.getName());
		 public static final int indexOpenTime = 2000;
	   	 IndexWriter writer = null;
	   	 Volume volume;
	   	  protected static ScheduledExecutorService scheduler;
		 protected static ScheduledFuture<?> scheduledTask;
		 
		 ReentrantLock indexLock = new ReentrantLock();
		 ArchivaAnalyzer analyzer 	= new ArchivaAnalyzer();
		 Indexer indexer = null;
		 File indexLogFile;
		 PrintStream indexLogOut;
		 
	  	  public VolumeIndex(Indexer indexer, Volume volume) {
	  		  logger.debug("creating new volume index {"+volume+"}");
	  		  this.volume = volume;
	  		  this.indexer = indexer;
	  		  try {
	  			  indexLogFile = getIndexLogFile(volume);
	  			  if (indexLogFile!=null) {
	  				  if (indexLogFile.length()>10485760)
	  					  indexLogFile.delete();
	  				  indexLogOut = new PrintStream(indexLogFile);
	  			  }
	  			  logger.debug("set index log file path {path='"+indexLogFile.getCanonicalPath()+"'}");
	  		  } catch (Exception e) {
	  			  logger.error("failed to open index log file:"+e.getMessage(),e);
	  		  }
	  		  startup();
	  	  }
		
	  	protected File getIndexLogFile(Volume volume) {
	  		 try {
	  			  String indexpath = volume.getIndexPath();
	  			  int lio = indexpath.lastIndexOf(File.separator)+1;
	  			  String logfilepath = indexpath.substring(lio,indexpath.length()-1);
	  			  logfilepath += ".log";
	  			  logfilepath = "index_"+logfilepath;
	  			  logfilepath = Config.getFileSystem().getLogPath()+File.separator+logfilepath;
	  			  return new File(logfilepath);
	  		  } catch (Exception e) {
	  			  logger.error("failed to open index log file:"+e.getMessage(),e);
	  			  return null;
	  		  }
	  	}
	  	
 		public void deleteMessage(EmailID emailID) throws MessageSearchException {
			  if (emailID == null)
		            throw new MessageSearchException("assertion failure: null emailID",logger);
			  logger.debug("delete message {'"+emailID+"'}");
			  try {
				  boolean isLocked = indexLock.tryLock(10,TimeUnit.MINUTES);
				  if (!isLocked) {
					  throw new MessageSearchException("failed to delete email from index. could not acquire lock on index",logger);
				  }
				  openIndex();
				  try {
					  writer.deleteDocuments(new Term("uid",emailID.getUniqueID()));
				  } catch (Exception e) {
					  throw new MessageSearchException("failed to delete email from index.",e,logger);
				  } 
			  } catch (InterruptedException ie) {
				  throw new MessageSearchException("failed to delete email from index. interrupted.",logger);
			  } finally {
				  indexLock.unlock();
			  }
		}
		
		  protected void openIndex() throws MessageSearchException {
			 Exception lastError = null;
			 try {
				boolean isLocked = indexLock.tryLock(10,TimeUnit.MINUTES);
				  if (!isLocked) {
					  throw new MessageSearchException("failed to open index. could not acquire lock on index.",logger);
				}
		    	if (writer==null) {
		    		logger.debug("openIndex() index will be opened. it is currently closed.");
		    	} else {
		    		logger.debug("openIndex() did not bother opening index. it is already open.");
		    		return; 
		    	}
		    	logger.debug("opening index for write {"+volume+"}");
				indexer.prepareIndex(volume);
				logger.debug("opening search index for write {indexpath='"+volume.getIndexPath()+"'}");
				boolean writelock;
				int attempt = 0;
				int maxattempt = 10;
				
				do {
					writelock = false;
					try {
							FSDirectory fsDirectory = FSDirectory.getDirectory(volume.getIndexPath());
							writer = new IndexWriter(fsDirectory,analyzer,new IndexWriter.MaxFieldLength(50000));
							if (logger.isDebugEnabled() && indexLogOut!=null) {
								writer.setInfoStream(indexLogOut);
							}
					} catch (LockObtainFailedException lobfe) {
							logger.debug("write lock on index. will reopen in 50ms.");
							try { Thread.sleep(50); } catch (Exception e) {}
							attempt++;
							writelock = true;
					} catch (CorruptIndexException cie) {
						throw new MessageSearchException("index appears to be corrupt. please reindex the active volume."+cie.getMessage(),logger);
					} catch (IOException io) {
						throw new MessageSearchException("failed to write document to index:"+io.getMessage(),logger);
					}
			   } while (writelock && attempt<maxattempt);
			   if (attempt>=10000)
				 throw new MessageSearchException("failed to open index writer {location='"+volume.getIndexPath()+"'}",lastError,logger);
			} catch (InterruptedException ie) {
				 throw new MessageSearchException("failed to open index. interrupted.",logger);
			} finally {
				  indexLock.unlock();
			}
		}
		
		public void indexMessage(Email message) throws MessageSearchException  {
      
			long s = (new Date()).getTime();
			if (message == null)
			    throw new MessageSearchException("assertion failure: null message",logger);
			logger.debug("indexing message {"+message+"}");
			
			Document doc = new Document();
			try {
			 
			   DocumentIndex docIndex = new DocumentIndex(indexer);
			   String language = doc.get("lang");
			   if (language==null)
				   language = indexer.getIndexLanguage();
			   IndexInfo indexInfo = new IndexInfo();
			   
			   docIndex.write(message,doc,indexInfo); 
			    try {
					boolean isLocked = indexLock.tryLock(10,TimeUnit.MINUTES);
					  if (!isLocked) {
						  throw new MessageSearchException("failed to write email to index. could not acquire lock on index.",logger);
					}
		   			openIndex();
		   			writer.addDocument(doc,AnalyzerFactory.getAnalyzer(language,AnalyzerFactory.Operation.INDEX));
			    } catch (InterruptedException ie) {
					 throw new MessageSearchException("failed to write email to index. interrupted.",logger);
				} finally {
					  indexLock.unlock();
					  indexInfo.cleanup();
				}
		   		doc = null;
			  
			   logger.debug("message indexed successfully {"+message+",language='"+language+"'}");
			} catch (MessagingException me) {
			   throw new MessageSearchException("failed to decode message during indexing",me,logger, ChainedException.Level.DEBUG);
			} catch (IOException me) {
			    throw new MessageSearchException("failed to index message {"+message+"}",me,logger, ChainedException.Level.DEBUG);
			} catch (ExtractionException ee)
			{
				// we will want to continue indexing
			   //throw new MessageSearchException("failed to decode attachments in message {"+message+"}",ee,logger, ChainedException.Level.DEBUG);
			} catch (AlreadyClosedException ace) {
				indexMessage(message);
			} catch (Throwable e) {
			    throw new MessageSearchException("failed to index message",e,logger, ChainedException.Level.DEBUG);
			}
			logger.debug("indexing message end {"+message+"}");
			
			long e = (new Date()).getTime();
		    logger.debug("indexing time {time='"+(e-s)+"'}");
		}
			
		protected void closeIndex() {
	 		try {
	 			 boolean isLocked = indexLock.tryLock(10,TimeUnit.MINUTES);
				 if (!isLocked) {
					 logger.error("failed to close index. could not acquire lock on index.");
					  return;
				 }
		         if (writer==null)
		        		return;
		         try {
		        	writer.close();
				    logger.debug("writer closed");
				 } catch (Exception io) {
					logger.error("failed to close index writer:"+io.getMessage(),io);

				 }
				 writer = null;
	 		} catch (InterruptedException ie) {
				 logger.error("failed to close index. interrupted.");
			} finally {
				  indexLock.unlock();
			}		
	   	}
	
		  public void deleteIndex() throws MessageSearchException {
			  	 logger.debug("delete index {indexpath='"+volume.getIndexPath()+"'}");
		  		 try {
		 			 boolean isLocked = indexLock.tryLock(10,TimeUnit.MINUTES);
					 if (!isLocked) {
						 throw new MessageSearchException("failed to close index. could not acquire lock on index.",logger);
					 }
					 try {
						  writer = new IndexWriter(FSDirectory.getDirectory(volume.getIndexPath()),false, analyzer,true);
					 } catch (Exception cie) {
						 logger.error("failed to delete index {index='"+volume.getIndexPath()+"'}",cie);
						 return;
					 } 
					 try {
					   writer.close();
	              	 } catch (Exception e) {
	              		logger.error("failed to delete index {index='"+volume.getIndexPath()+"'}",e);
	              	 }
	              	 MessageIndex.volumeIndexes.remove(this);
		  		} catch (InterruptedException ie) {
					 throw new MessageSearchException("failed to delete email from index. interrupted.",logger);
				} finally {
					  indexLock.unlock();
				}
		  }
		
		  public void startup() {
			logger.debug("volumeindex is starting up");
			File lockFile = new File(volume.getIndexPath()+File.separatorChar + "write.lock");
			if (lockFile.exists()) {
				logger.warn("The server lock file already exists. Either another indexer is running or the server was not shutdown correctly.");
				logger.warn("If it is the latter, the lock file must be manually deleted at "+lockFile.getAbsolutePath());
				logger.warn("index lock file detected. the server was shutdown incorrectly. automatically deleting lock file.");
				logger.warn("indexer is configured to deal with only one indexer process.");
				logger.warn("if you are running more than one indexer, your index could be subject to corruption.");
				lockFile.delete();
			}
			scheduler = Executors.newScheduledThreadPool(1);
			scheduledTask = scheduler.scheduleWithFixedDelay(new TimerTask() {
	            @Override
				public void run() {
	             	closeIndex();
	            }
	        }, indexOpenTime, indexOpenTime,TimeUnit.MILLISECONDS);
			Runtime.getRuntime().addShutdownHook(this);
			
		  }
		  
		  public void shutdown() {
			  logger.debug("volumeindex is shutting down");
			  scheduler.shutdownNow();
		      closeIndex();
		  }
		  
		  @Override
		public void run() {
			  closeIndex();
		  }
		  
	 
}

	

