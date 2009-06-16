/* Copyright (C) 2005-2009 Jamie Angus Band
 * MailArchiva Open Source Edition Copyright (c) 2005-2009 Jamie Angus Band
 * This program is free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation; either version
 * 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see http://www.gnu.org/licenses or write to the Free Software Foundation,Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

package com.stimulus.archiva.incoming;

import java.net.*;
import java.nio.channels.*;
import org.apache.commons.logging.*;
import com.stimulus.archiva.domain.*;


public class Server extends Thread {

    protected ServerSocketChannel serverSocketChannel;
    protected boolean running;
    protected int port;
    protected int backlog;
    protected String ipAddress;
    protected RequestQueue requestQueue;
    protected static Log logger = LogFactory.getLog(Server.class);
    protected static int MAX_BIND_ATTEMPTS = 20;

    public Server( 		   String ipAddress,
    					   int port,
                           int backlog,
                           String requestHandlerClassName,
                           FetchMessageCallback callback,
                           int maxQueueLength,
                           int minThreads,
                           int maxThreads,
                           String serverName) {
    	this.ipAddress = ipAddress;
        this.port = port;
        this.backlog = backlog;
    	setName(serverName);
		setDaemon(true);
        this.requestQueue = new RequestQueue( serverName,
        									  requestHandlerClassName,
        									  callback,
                                              maxQueueLength,
                                              minThreads,
                                              maxThreads);
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


    public void prepareShutdown() {
    	if (isAlive()) {
    		this.running = false;
    	}
    }

    public void shutdown() {
    	if (isAlive()) {
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
            } catch (java.nio.channels.AsynchronousCloseException se) {
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
