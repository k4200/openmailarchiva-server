package com.stimulus.archiva.incoming;

import java.net.*;
import java.nio.channels.*;
import org.apache.log4j.Logger;
import com.stimulus.archiva.domain.*;


public class Server extends Thread {
	
    protected ServerSocketChannel serverSocketChannel;
    protected boolean running;
    protected int port;
    protected int backlog;
    protected String ipAddress;
    protected RequestQueue requestQueue;
    protected static Logger logger = Logger.getLogger(Server.class);
    protected static int MAX_BIND_ATTEMPTS = 20;
  
    public Server( 		   String ipAddress,
    					   int port, 
                           int backlog,
                           String requestHandlerClassName,
                           FetchMessageCallback callback,
                           int maxQueueLength,
                           int minThreads,
                           int maxThreads ) {
    	this.ipAddress = ipAddress;
        this.port = port;
        this.backlog = backlog;
    	setName("server "+requestHandlerClassName);
		setDaemon(true);
        this.requestQueue = new RequestQueue( requestHandlerClassName,
        									  callback,
                                              maxQueueLength,
                                              minThreads,
                                              maxThreads );
    }

    public int getPort() { return port; }
    
    public void startup() {
    	int attempts = 0;
		while (attempts < MAX_BIND_ATTEMPTS) {
	        try {
		        this.serverSocketChannel = ServerSocketChannel.open();
		        this.serverSocketChannel.configureBlocking(true);
		        this.serverSocketChannel.socket().setReuseAddress(true);
		        this.serverSocketChannel.socket().setSoTimeout(0);
		        if (ipAddress!=null && ipAddress.length()>0 && !ipAddress.equalsIgnoreCase("all")) {
		        	this.serverSocketChannel.socket().bind(new InetSocketAddress(InetAddress.getByName(ipAddress),port));
		        } else {
		        	this.serverSocketChannel.socket().bind(new InetSocketAddress(port));
		        }
		        this.start(); // backlog
	            break;
	        } catch (java.net.BindException e) {
	        	logger.fatal("failed bind to the server server:"+e.getMessage());
	        	logger.info("it looks like the server may already be running or some other application is listening on the socket.");
	        	logger.info("please kill other server processes and restart this server" );
	        } catch( Exception e ) {
	        	 logger.error("could not create server socket",e);
	        	 try { Thread.sleep(10000); }  catch (Exception e5) {}
	        }
	        attempts++;
		}
    }
    
    @Override
	protected void finalize() throws Throwable {
    	interrupt();
    	shutdown();
    }

    public void prepareShutdown() {
    	this.running = false;
    }
    
    public void shutdown() {
        try {
            this.running = false;
            if (serverSocketChannel!=null && serverSocketChannel.isOpen()) {
            	this.serverSocketChannel.close();
            	this.serverSocketChannel = null;
            }
        }
        catch( Exception e ) {
        }
    }

   
    @Override
	public void run()
    {
        logger.debug("server started. listening on port {port='"+port+"'}");
        this.running = true;
        while( running ) {
            try {
            	
                SocketChannel s = serverSocketChannel.accept();
                InetAddress addr = s.socket().getInetAddress();
                logger.debug("received a new connection {hostaddress='"+addr.getHostAddress()+"',hostname='"+addr.getHostName()+"'");
                this.requestQueue.add( s );
            } catch( SocketException se ) {
                if( this.running ) {
                	logger.error("server socket exception occured while processing requests",se);
                }
            } catch( Exception e ) {
            	logger.error("server exception occured while processing requests",e);
            }
        }
        logger.debug("shutting down the request queue");
      
        this.requestQueue.shutdown();
    }
    
   
}