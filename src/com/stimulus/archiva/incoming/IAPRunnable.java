package com.stimulus.archiva.incoming;

/* Copyright (C) 2005-2008 Jamie Angus Band 
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

import java.net.UnknownHostException;
import java.security.Security;
import java.util.*;

import com.stimulus.archiva.domain.*;
import javax.mail.*;

import org.apache.log4j.*;
import java.io.*;
import com.stimulus.archiva.exception.ArchivaException;
import com.stimulus.archiva.exception.ArchiveException;
import com.sun.mail.imap.*;
import javax.mail.event.*;
import com.stimulus.util.*;

import java.util.concurrent.*;

public class IAPRunnable extends Thread  {

	
	private static Logger logger = Logger.getLogger(IAPRunnable.class);
    protected URLName url;
    protected javax.mail.Store store = null;    
    protected Folder inboxFolder = null;
    protected boolean shutdown = false;
    protected int intervalSecs = 0;
    protected FetchMessageCallback callback;
    protected String ipAddress = "";
    protected boolean test = false;
    protected IAPTestCallback testCallback;
    protected static int WAIT_PERIOD = 300000;
    protected MailboxConnection connection;
    protected static final short MAX_MESSAGES_TO_PROCESS = 50; // before saving
    protected static int DEAD_PERIOD = 6000000;
    private final String DUMMY_SSL_FACTORY = "com.stimulus.archiva.incoming.DummySSLSocketFactory";
    private final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
    private static final int IDLE_TIMEOUT = 300000; // 5 minutes
    protected static int CLOSE_WAIT_PERIOD = 10000;
    protected ExecutorService archivePool = Executors.newFixedThreadPool(Config.getConfig().getArchiver().getArchiveThreads());
    
    static {
    	Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
    }
    
    
	public IAPRunnable(String threadName, IAPTestCallback testCallback, MailboxConnection connection, int intervalSecs, FetchMessageCallback callback) {
		logger.debug("iaprunnable constructor called");
		setDaemon(true);
		setName(threadName);
		this.intervalSecs = intervalSecs;
		this.callback = callback;
		this.testCallback = testCallback;
		this.connection = connection;
    	setPriority(Thread.NORM_PRIORITY);
	}
	
	public MailboxConnection getMailboxConnection() {
		return connection;
	}
	
	public String getSSLFactory() {
		if (connection.getAuthCerts()) 
			return SSL_FACTORY;
		else
			return DUMMY_SSL_FACTORY;
	}
	
	
	
	
		
	  
	    
	    public void run()
	    {
	    	
	    	try { 
	    		java.net.InetAddress inetAdd = java.net.InetAddress.getByName(connection.getServerName());
	    		ipAddress = inetAdd.getHostAddress();
	    	} catch (UnknownHostException uhe) {
	    		logger.debug("failed to resolve address of mail server {connection.getServerName()='"+connection.getServerName()+"',port='"+connection.getPort()+"',userName='"+connection.getUsername()+",protocol='"+connection.getProtocol()+"'}");
	    		testOutput("failed to resolve ip address of mail server "+connection.getServerName());
	    	}

	    	shutdown = false;
	    	
	    	while (!shutdown) {
	    
	            	logger.debug("connecting to mail server {serverName='"+connection.getServerName()+"',port='"+connection.getPort()+"',userName='"+connection.getUsername()+",protocol='"+connection.getProtocol()+"'}");
	            	testOutput("connecting to mail server "+connection.getServerName());
	            	try {
	            		connect();
	            	} catch (Exception e) {
	            		logger.error("failed to connect to mail server {serverName='"+connection.getServerName()+"',port='"+connection.getPort()+"',userName='"+connection.getUsername()+"',protocol='"+connection.getProtocol()+"'}. will retry in 1 second.",e);
	            		if (e.getCause() instanceof AuthenticationFailedException)
	            			testOutput("could not authenticate with mail server "+connection.getServerName()+". username and/or password incorrect?");
	            		else {
	            			String errormessage = "";
	            			if (e.getCause()!=null && e.getCause().getMessage()!=null)
	            				errormessage = e.getCause().getMessage();
	            			testOutput("failed to connect to mail server "+connection.getServerName()+". "+e.getMessage()+"["+errormessage+"]");
	            		}
	            		if (testCallback!=null) {
	            			break;
	            		} else {
	            			try { disconnect(); } catch (Exception ed) {}
	            			try { Thread.sleep(WAIT_PERIOD); } catch (Exception e2) {}
	            			continue;
	            		}
	            	}
	      
	                logger.debug("connected to  mail server {serverName='"+connection.getServerName()+"',port='"+connection.getPort()+"',userName='"+connection.getUsername()+"'}");
	                testOutput("connected to mail server "+connection.getServerName());
	                if (testCallback==null && Compare.equalsIgnoreCase(connection.getProtocol().toString(),"IMAP") && connection.getIdle()) {
	                	imapIdleMessages();
	                	logger.debug("imap idle messages return()");
	                } else {
       		 			pollMessages();
	                }

            	try {
					 disconnect(); 
					
				 } catch (Exception e) {
	                	logger.error("failed to disconnect from mail server serverName='"+connection.getServerName()+"',port='"+connection.getPort()+"',userName='"+connection.getUsername()+"'}",e);
	                	testOutput("failed to disconnect from mail server "+connection.getServerName());
				 }   
				 if (testCallback==null && !(connection.getProtocol()==MailboxConnections.Protocol.IMAP && connection.getIdle())) {
					 logger.debug("iap sleep {time='"+(intervalSecs * 1000)+"'}");
					 try { Thread.sleep(intervalSecs * 1000); } catch (Exception e) {}
				 }
	    	}
	    	if (testCallback==null && logger!=null) {
	    		logger.warn("the iap service is shutdown {shutdown='"+shutdown+"'}");
	    	}
	    }
	    
	    

		public void connect() throws Exception {
			/*
			if (store!=null) {
				if (store.isConnected())
				return; // already connected
			}*/

			Properties props = new Properties();  
		    String protocol = connection.getProtocol().toString().toLowerCase(Locale.ENGLISH);
		    if (protocol.equals("pop"))
		    	protocol = "pop3";
		 
		    String server = connection.getServerName();
		    String username = connection.getUsername();
		    String password = connection.getPassword();
		    int port = connection.getPort();
		    int secureport = connection.getSSLPort();

		    if (connection.getConnectionMode()==MailboxConnections.ConnectionMode.INSECURE) {
		    } else if (connection.getConnectionMode()==MailboxConnections.ConnectionMode.FALLBACK) {
		    	 props.put("mail."+protocol+".starttls.enable", "true");
		    	 props.put("mail."+protocol+".socketFactory.fallback","true");
		    	 props.put("mail."+protocol+".startTLS.socketFactory.class", getSSLFactory());
	    	 } else if (connection.getConnectionMode()==MailboxConnections.ConnectionMode.TLS) {
	    		 props.put("mail."+protocol+".starttls.enable", "true");
	 	    	 props.put("mail."+protocol+".socketFactory.fallback","false");
	 	    	 props.put("mail."+protocol+".startTLS.socketFactory.class", getSSLFactory());
	   	 	} else if (connection.getConnectionMode()==MailboxConnections.ConnectionMode.SSL) {
	    		protocol = protocol + "s";
	   	 		port = secureport;
	    		props.put("mail."+protocol+".socketFactory.class", getSSLFactory());
	        	props.put("mail."+protocol+".socketFactory.port",secureport); 
	        	props.put("mail."+protocol+".socketFactory.fallback","false");
	   	 	}
		    // we put a timeout of 10 minutes
		    props.put("mail."+protocol+".connectiontimeout",100000);
		    props.put("mail."+protocol+".timeout",100000);
		    establishConnection(protocol,server,port,username,password,props);
		}
		

		public void establishConnection(String protocol, String server,int port, String username, String password, Properties props) throws ArchivaException {
			logger.debug("establishConnection() protocol='"+protocol+"',server='"+server+"',port='"+port+"',username='"+username+"',password='********'}");
			Session session = Session.getInstance(props, null);
			if (System.getProperty("mailarchiva.mail.debug")!=null)
				session.setDebug(true);
	        
	        try {
	        	logger.debug("iap connect {"+props+"}");
	            store = session.getStore(protocol);
	        } catch (Exception nspe) {
	        	return;
	        }
	        if (logger.isDebugEnabled()) {
	        	logger.debug("mailbox connection properties "+props);
	        }
	        try {
	        	store.connect(server,port, username,password);
	        } catch (AuthenticationFailedException e) {
	        	logger.error("cannot connect to mail server. authentication failed {"+props+"}");
	            throw new ArchivaException ("unable to connect to mail server. could not authenticate. {"+props+"}",e,logger);
	        } catch (IllegalStateException ise) {
	        	throw new ArchivaException ("attempt to connect mail server when it already connected. {"+props+"}",ise,logger);
	        } catch (MessagingException me) {
	        	if (me.getMessage().contains("sun.security.validator.ValidatorException")) {
	        		throw new ArchivaException ("failed to authenticate TLS certificate. You must install the mail server's certificate as per the administration guide.",me,logger);
	        	} else if (connection.getConnectionMode()==MailboxConnections.ConnectionMode.FALLBACK &&
	        			   me.getMessage().contains("javax.net.ssl.SSLHandshakeException")) {
	        		logger.debug("cannot establish SSL handshake with mail server. falling back to insecure. {"+props+"}");
	        		connection.setConnectionMode(MailboxConnections.ConnectionMode.INSECURE);
	        	} else throw new ArchivaException ("failed to connect to mail server. "+me.getMessage()+". {"+props+"}",me,logger);
	        }
	        try {    
	            inboxFolder = store.getDefaultFolder();
			 } catch (Exception e) {
		    	 throw new ArchivaException ("unable to get default folder. ",e,logger);
			 }
	      
	        if (inboxFolder == null) {
	            throw new ArchivaException ("there was no default POP inbox folder found.",logger);
	        }    
	   
	        try {    
	            inboxFolder = inboxFolder.getFolder("INBOX");
	            if (inboxFolder == null) {
	                throw new ArchivaException ("the inbox folder does not exist.",logger);
	            }
	        } catch (Exception e) {
	        	 throw new ArchivaException ("unable to get INBOX folder. ",e,logger);
	        }
	        try {
	            inboxFolder.open(Folder.READ_WRITE);            
	        } catch (Exception e) {
	            throw new ArchivaException ("unable to open folder. ",e,logger);
	        }    
	        return;
		}
	    
	    public void pollMessages() {
	    	logger.debug("iap pollmessages() {mailboxworker='"+getName()+"'}");
	    	boolean complete = false;
	    	do {
		    	Message[] messages = null;		    	
		    	try {
		    		if (store.isConnected()) {
		    			logger.debug("iap inboxfolder.getmessages() called");
		    			messages = inboxFolder.getMessages();
		    			logger.debug("iap inboxfolder.getmessages() end");
		    		} else {
		    			logger.debug("iap not connected. returning");
		    			return;
		    		}
		    	} catch (FolderClosedException folder) {
	       		 	logger.error("mail server inbox folder is closed {serverName='"+connection.getServerName()+"',port='"+connection.getPort()+"',userName='"+connection.getUsername()+"'}");
	       		 	testOutput("mail server inbox folder is closed");
	       		 	return;
		       	 } catch (FolderNotFoundException folder) {
		           	logger.error("mail server inbox folder does not exist {serverName='"+connection.getServerName()+"',port='"+connection.getPort()+"',userName='"+connection.getUsername()+"'}");
		           	testOutput("mail server inbox folder does not exist");
		           	return;
		        } catch( IllegalStateException ise) {
		           	logger.error("mail server inbox folder is not opened {serverName='"+connection.getServerName()+"',port='"+connection.getPort()+"',userName='"+connection.getUsername()+"'}");
		           	testOutput("mail server inbox folder is not opened");
		           	return;
		        } catch (IndexOutOfBoundsException iobe) {
		           	logger.error("mail server start and end message numbers are out of range {serverName='"+connection.getServerName()+"',port='"+connection.getPort()+"',userName='"+connection.getUsername()+"'}");
		           	testOutput("mail server start and end message numbers are out of range");
		            return;
		       } catch (MessagingException me) {
		           	logger.error("failed to open mail server inbox {serverName='"+connection.getServerName()+"',port='"+connection.getPort()+"',userName='"+connection.getUsername()+"'}",me);
		           	testOutput("failed to open mail server inbox");
		           	return;
		           }
	        	
	        	if (testCallback!=null) { // we're just testing 
	        		 	testOutput("retrieved messages from inbox");
	        		 	testOutput("test successful");
	        			shutdown = true;
	        			return;
	        	}
	        	if (messages.length<1) {
	        		logger.debug("poll messages: no messages to process");
	        		break;
	        	}
	        	logger.debug("iap archivemesssages() called");
	        	complete = archiveMessages(messages);
	        	logger.debug("iap archivemesssages() end");
	    	} while (!complete);
	    }
	    
	    public void imapIdleMessages() { 
	    	 logger.debug("iap imapidlemessages() {mailboxworker='"+getName()+"'}");
	    	 inboxFolder.addMessageCountListener(new MessageCountAdapter() {
             public void messagesAdded(MessageCountEvent ev) {
                	Message[] messages = null;
                	try {
                		if (store.isConnected()) {
                			messages = ev.getMessages();
                		} else {
                			return;
                		}
                		
        	        } catch( IllegalStateException ise) {
        	           	logger.error("mail server inbox folder is not opened {serverName='"+connection.getServerName()+"',port='"+connection.getPort()+"',userName='"+connection.getUsername()+"'}");
        	           	testOutput("mail server inbox folder is not opened {serverName='"+connection.getServerName()+"',port='"+connection.getPort()+"',userName='"+connection.getUsername()+"'}");
        	           	return;
        	        } catch (IndexOutOfBoundsException iobe) {
        	           	logger.error("mail server start and end message numbers are out of range {serverName='"+connection.getServerName()+"',port='"+connection.getPort()+"',userName='"+connection.getUsername()+"'}");
        	           	testOutput("mail server start and end message numbers are out of range {serverName='"+connection.getServerName()+"',port='"+connection.getPort()+"',userName='"+connection.getUsername()+"'}");
        	            return;
        	        }
        	        
        	        if (messages.length<1) {
    	        		logger.debug("idle messages: no messages to process");
    	        		return;
        	        } 
        	        archiveMessages(messages);
                }
            });
	    	pollMessages();
	    	
	    	while (!shutdown && inboxFolder.isOpen()) {
	    		try {
	    			logger.debug("idling to IMAP server {serverName='"+connection.getServerName()+"',port='"+connection.getPort()+"',userName='"+connection.getUsername()+"'}");
	    			((IMAPFolder)inboxFolder).idle();
	    			logger.debug("return from idling with IMAP server {serverName='"+connection.getServerName()+"',port='"+connection.getPort()+"',userName='"+connection.getUsername()+"'}");
	    		} catch (FolderClosedException fce) {
	    			logger.debug("imap folder closed. possibly due to server timeout.");
	    			break;
	    		} catch (java.lang.IllegalStateException se) {
	    			logger.debug("idling has stopped. it is likely that the mailbox connection was shutdown:"+se.getMessage());
	    			break;
	    		} catch (MessagingException me) {
	    			logger.error("failed to execute IMAP idle {serverName='"+connection.getServerName()+"',port='"+connection.getPort()+"',userName='"+connection.getUsername()+"'}",me);
	    			break;
	    		} catch (Throwable t) {
	    			logger.error("hard error occurred while executing IMAP idle {serverName='"+connection.getServerName()+"',port='"+connection.getPort()+"',userName='"+connection.getUsername()+"'}",t);
	    			break;
	    		}
	    	} 
	    	logger.debug("imap idling loop exit {shutdown='"+shutdown+"'}");
	    }
	
	    public boolean archiveMessages(Message[] messages) {
	    	if (messages==null)
	    		return true;
	    	logger.debug("archivemesages called()");
	    	boolean complete = true;
	    	
	    	if (archivePool.isShutdown()) {
	    		logger.debug("iap archive pool is shutdown. recreating new fixed thread pool {archiveThreads='"+Config.getConfig().getArchiver().getArchiveThreads()+"'}");
	    		archivePool = Executors.newFixedThreadPool(Config.getConfig().getArchiver().getArchiveThreads());
	    	}
	    	int i = 0;
	    	logger.debug("iap executing archive thread pool jobs {messages='"+messages.length+"'}");
          	for (Message message : messages) {
          		logger.debug("adding message to thread pool {i='"+i+"'}");
          		archivePool.execute(new MessageStoreAction(message));
          		i++;
          		if (i>=MAX_MESSAGES_TO_PROCESS) {
          			logger.debug("iap max messages to process exceeded");
          			complete = false;
          			break;
          		}
          	}
          	shutdownAndAwaitTermination(archivePool,"archive workers");
          	logger.debug("thread pool jobs complete");
          	
  			if (Compare.equalsIgnoreCase(connection.getProtocol().toString(),"IMAP") && messages!=null && messages.length>0) {
  				IMAPFolder f = (IMAPFolder)inboxFolder;
  				try {
	  				logger.debug("expunging deleted emails on IMAP server {serverName='"+connection.getServerName()+"',port='"+connection.getPort()+"',userName='"+connection.getUsername()+"'}");
	          		f.expunge(); 
	          	    logger.debug("deleted emails have been expunged {serverName='"+connection.getServerName()+"',port='"+connection.getPort()+"',userName='"+connection.getUsername()+"'}");
	          	} catch (FolderNotFoundException folder) {
	   	           	logger.error("mail server inbox folder does not exist {serverName='"+connection.getServerName()+"',port='"+connection.getPort()+"',userName='"+connection.getUsername()+"'}");
	   	           	testOutput("mail server inbox folder does not exist {serverName='"+connection.getServerName()+"',port='"+connection.getPort()+"',userName='"+connection.getUsername()+"'}");
	   	        } catch (MessagingException me) {
	   	        	logger.error("messaging exception occurs while attempting to expunge IMAP folder {serverName='"+connection.getServerName()+"',port='"+connection.getPort()+"',userName='"+connection.getUsername()+"'}");
	   	        }
  			} 
  			logger.debug("archivemesages exited()");
  			return complete;
   	   
	    }
	    
	    public void shutdownAndAwaitTermination(ExecutorService pool, String job) {
	    	  pool.shutdown(); 
	    	   try {
	    		   logger.debug("awaiting termination of "+job);
	    	     if (!pool.awaitTermination(DEAD_PERIOD,TimeUnit.MILLISECONDS)) {
	    	    	 logger.debug("awaiting "+job+" did not terminate");
	    	    	 pool.shutdownNow(); 
	    	    	 if (!pool.awaitTermination(60, TimeUnit.SECONDS))
	    	    		 logger.debug("awaiting "+job+" still did not terminate");
	    	     } else {
	    	    	 logger.debug("awaiting "+job+" terminated");
	    	     }
	    	   } catch (InterruptedException ie) {
	    		   logger.debug("awaiting "+job+" were interrupted. shutting thread pool down immediately.");
	    	       pool.shutdownNow();
	    	       Thread.currentThread().interrupt();
	    	   }
	    }

	    public void prepareShutdown() {
	    	logger.debug("iaprunnable prepare shutdown()");
	    	shutdown = true;
	    }
	  
	    public void shutdown() {
	    	logger.debug("iaprunnable shutdown called()");
	    	try { disconnect(); } catch (Exception e) {}
	    	shutdown = true;
	    }
	    
	    public void testOutput(String output) {
    		if (testCallback!=null)
    			testCallback.statusUpdate(output);
	    }
	
	    protected void finalize() throws Throwable {
	    	interrupt();
	    	shutdown();
	    }
	    
	    public void disconnect() throws Exception {
			logger.debug("iap disconnect called()");
			// here we want to make sure we are not disconnecting until complete of archive worker jobs
			if (!archivePool.isTerminated()) {
				shutdownAndAwaitTermination(archivePool,"archive workers");
			}
			logger.debug("closing folder");
			try {
	    		if (inboxFolder!=null) {
	    			inboxFolder.close(true);
	    		}
	    	} catch (Exception e) {}
	    	logger.debug("closing store");
	    	try {
	    		if (store!=null) {
	    			store.close();
	    		}
	    	} catch (Exception e) {}
	    	
	    	inboxFolder = null;
	    	store = null;
			logger.debug("iap disconnect ended()");
		}
	 
	   
	    public interface IAPTestCallback {

	    		public void statusUpdate(String result);
	    		
	    }
	    
	    public class MessageStoreAction extends Thread implements StopBlockTarget {
	  	 
	    	Message message;
	    	
	    	public MessageStoreAction(Message message) {
	    		this.message = message;
	    	}
	    	
	    	public void run() {	    		
		    		try {
		    				try {
		    					logger.debug("found new message in mailbox queue {subject='"+message.getSubject()+"'}");
		    				} catch (MessagingException e) {
		    						logger.error("failed to retrieve subject from message:"+e.getMessage(),e); 
		    						return;
		    				}
		    					
		    				PipedInputStream in = new PipedInputStream();
		    				PipedOutputStream out = null;
		    				
		    				try {
		    					out = new PipedOutputStream(in);
		    				} catch (IOException ioe) {
		    					logger.error("io exception occurred while creating piped output stream:"+ioe.getMessage(),ioe); 
		    					return; 
		    				}
		    				
		 	                WriteMessageToOutputStream wmtos = new WriteMessageToOutputStream(message,new BufferedOutputStream(out));
		 	                ExecutorService writeout = Executors.newSingleThreadExecutor();
		 	                writeout.execute(wmtos);
		    				logger.debug("callback.store() called");
		    				Config.getStopBlockFactory().detectBlock("iap client",Thread.currentThread(),this,IDLE_TIMEOUT);
		    				try {
		    					callback.store(new BufferedInputStream(in), ipAddress);
		    					
		    				} catch (OutOfMemoryError oome) {
		    	    			logger.error("failed to store message:"+oome.getMessage(),oome);
		    	    			try { message.setFlag(Flags.Flag.SEEN, true); } catch (Exception e5) {}
		    	    			return; 
		    	    		}
		    				logger.debug("callback store complete");
		    				try {
		    					message.setFlag(Flags.Flag.DELETED, true);
		    				}  catch (Exception e) {
				    			try { 
									logger.error("failed to set deleted flag on message {subject='"+message.getSubject()+"'}",e); 
									return;
				    			} catch (MessagingException e2) { 
				    				logger.error("failed to retrieve subject from message (2):"+e.getMessage(),e); 
				    				return;
				    			}
				    		} 
		    				
		    		} catch (ArchiveException e) {
		    			try { 
							logger.error("failed to store message {subject='"+message.getSubject()+"'}",e); 
						} catch (Exception e2) { 
							logger.error("failed to retrieve subject from message (3):"+e.getMessage(),e); 
							return; 
						}
		    			if (e.getRecoveryDirective()==ArchiveException.RecoveryDirective.REJECT) {
		    				try { message.setFlag(Flags.Flag.SEEN, true); } catch (Exception e5) {}
		    				return;
		    			} else  if (e.getRecoveryDirective()==ArchiveException.RecoveryDirective.RETRYLATER) {
		    				return;
		    			}
		    		} catch (Throwable t) {
		    			try { 
							logger.error("failed to store message {subject='"+message.getSubject()+"'}",t); 
						} catch (Exception e2) {}
						try {
	    					message.setFlag(Flags.Flag.DELETED, true);
	    				}  catch (Exception e) {
			    			try { 
								logger.error("failed to set deleted flag on message {subject='"+message.getSubject()+"'}",e); 
			    			} catch (MessagingException e2) {}
			    			return;
			    		} 
		    		} finally {
		    			Config.getStopBlockFactory().endDetectBlock(Thread.currentThread());
		    		}
		    	
	    	}
	    	
	    	 
	        public void handleBlock(Thread thread) {
	        	if (store!=null) {
	        		try{
	        			 logger.debug("close socket()");
	        			store.close();
	        		} catch (MessagingException me) {
	        			
	        		}
	        	}
	        	
	             synchronized (this) {
	                 if (thread != null) {
	                	 logger.debug("interrupt()");
	                	 thread.interrupt();
	                 }
	             }
	        }
	        
	    }
	    
	    public class WriteMessageToOutputStream extends Thread {
	    	
	    	 Message message;
	    	 OutputStream os;
	    	 
	    	 public WriteMessageToOutputStream(Message message, OutputStream os) {
	    		 this.message = message;
	    		 this.os = os;
	    		 setDaemon(true);
	    		 setName("outputstreamwriter");
	    		 setPriority(Thread.NORM_PRIORITY);
	    		 logger.debug("writemessagetooutputstream contructor called");
	    	 }
	    	 
	    	 public void run() {
	    		 try { 
	    			 logger.debug("writeto() begin()");
	    			 message.writeTo(os);
	    			 os.flush();
	    			 os.close();
	    			 logger.debug("writeto() end()");
	    		 } catch (Exception io) {
	    			 logger.error("failed to write message to output stream",io);
	    			 try {  os.close(); } catch (Exception e) {}
	    		 }
	    		 
	    	 }
	    }
	    
	  	
}
	
