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

package com.stimulus.archiva.incoming;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import com.stimulus.archiva.domain.Agent;
import com.stimulus.archiva.domain.Config;

public class MilterServer extends Thread
{
    private static Logger logger = Logger.getLogger(MilterServer.class);

    protected ServerSocketChannel serverSocketChannel = null;
    protected boolean shutdown = false;
    protected int port = 8092;
    protected StoreMessageCallback callback;

	public MilterServer(StoreMessageCallback callback) {
		try {
			
			this.callback = callback;
			port = Config.getConfig().getAgent().getMilterPort();
	  
	        logger.info("mailarchiva milter server service listening for incoming milter requests {port='"+port+"'}");
	        this.serverSocketChannel = ServerSocketChannel.open();
	        this.serverSocketChannel.configureBlocking(true);
	        this.serverSocketChannel.socket().bind(new InetSocketAddress(port));
	        setDaemon(true);
	        setName("Milter");
		} catch (Exception e) {
			logger.fatal("mailarchiva milter server service failed to open server socket {port='"+port+"'}",e);
    		logger.fatal("it is likely that mailarchiva cannot listen for archiving request since port "+port+" is already taken.");
    		logger.fatal("change the port on which MailArchiva is listening and restart the server.");
    		shutdown = true;
		}
    }
   
    
    public void shutdown() {
    	shutdown = true;
    }
    
    public void run()
    {
    	if (serverSocketChannel==null || serverSocketChannel.socket().isClosed()) {
    		logger.fatal("mailarchiva smtp server could not open server socket");
    		logger.fatal("mailarchiva smtp server is not listening!!");
    		return;
    	}
    	shutdown = false;
        while (!shutdown)
        {
            SocketChannel connection = null;

            try
            {
            	connection = this.serverSocketChannel.accept();
            	Agent agent = Config.getConfig().getAgent();
            	InetAddress remoteAddress = connection.socket().getInetAddress();
            	String clientIp = remoteAddress.getHostAddress();
            	if (agent.isAllowed(remoteAddress)) {
            		logger.debug("milter accept connection (address='" + connection.socket().getInetAddress().getHostAddress()+"'}");
	                logger.debug("firing up new thread to handle milter requests");
	                new Thread( new JilterRunnable( connection, new MilterHandler(callback)),
	                			"Jilter " + connection.socket().getInetAddress().getHostAddress()
	                    		).start();
	                logger.debug("milter handler thread started");
            	} else {
            		logger.warn( "incoming milter connection disallowed due to security policy {hostname='"+remoteAddress.getHostName()+"',ipAddress='"+clientIp + "'}");
            		try { connection.close(); } catch (Exception e) {};
            	}
            }
            catch (Exception e)
            {
            	if (logger != null)
            		logger.debug("exception occured while processing incoming milter request", e);
            }
        }
    }

    
}
