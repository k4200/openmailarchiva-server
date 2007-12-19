package com.stimulus.archiva.incoming;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import com.stimulus.archiva.domain.Agent;
import com.stimulus.archiva.domain.Config;

public class SMTPServer extends Thread
{
    private static Logger logger = Logger.getLogger(SMTPServer.class);

    protected ServerSocketChannel serverSocketChannel = null;
    protected boolean shutdown = false;
    protected int port = 8091;
    protected StoreMessageCallback callback;

	public SMTPServer(StoreMessageCallback callback) {
		try {
			
			this.callback = callback;
			port = Config.getConfig().getAgent().getSMTPPort();
	  
	        logger.info("mailarchiva smtp server service listening for incoming SMTP Server requests {port='"+port+"'}");
	        this.serverSocketChannel = ServerSocketChannel.open();
	        this.serverSocketChannel.configureBlocking(true);
	        this.serverSocketChannel.socket().bind(new InetSocketAddress(port));
	        setDaemon(true);
	        setName("SMTP Server");
		} catch (Exception e) {
			logger.fatal("mailarchiva smtp server service failed to open server socket {port='"+port+"'}",e);
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
            		logger.debug("SMTP Server accept connection (address='" + connection.socket().getInetAddress().getHostAddress()+"'}");
	                logger.debug("firing up new thread to handle SMTP Server requests");
	                new Thread( new SMTPRunnable( connection, callback),
	                			"SMTPServer " + connection.socket().getInetAddress().getHostAddress()
	                    		).start();
	                logger.debug("smtp handler thread started");
            	} else {
            		logger.warn( "incoming milter connection disallowed due to security policy {hostname='"+remoteAddress.getHostName()+"',ipAddress='"+clientIp + "'}");
            		try { connection.close(); } catch (Exception e) {};
            	}
            }
            catch (Exception e)
            {
            	if (logger != null)
            		logger.debug("exception occured while processing incoming smtp server request", e);
            }
        }
    }

    
}