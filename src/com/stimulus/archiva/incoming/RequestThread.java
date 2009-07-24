package com.stimulus.archiva.incoming;

import org.apache.commons.logging.*;
import java.nio.channels.*;
import com.stimulus.archiva.domain.*;

public class RequestThread extends Thread
{
	protected RequestQueue queue;
    protected static Log logger = LogFactory.getLog(RequestThread.class);
    protected boolean running;
    protected volatile boolean processing = false;
    protected int threadNumber;
    protected RequestHandler requestHandler;
    protected FetchMessageCallback callback;
    
    public RequestThread( RequestQueue queue, int threadNumber, String requestHandlerClassName, FetchMessageCallback callback )
    {
    	setName("request"+threadNumber);
		setDaemon(true);
		this.callback = callback;
        this.queue = queue;
        this.threadNumber = threadNumber;
        try {
           this.requestHandler = ( RequestHandler )( Class.forName( requestHandlerClassName ).newInstance() );
        } catch( Exception e ) {
            logger.error("failed to instantiate request handler",e);
        }
    }

    public synchronized boolean isProcessing() {
        return this.processing;
    }
    
    protected synchronized void setProcessing(boolean processing) {
    	this.processing = processing;
    }
   
    public void killThread() {
        this.running = false;
    }

    @Override
	public void run() {
        this.running = true;
        while( running ) {
            try {
            	Object o = queue.getNextObject();
                if( running )  {
                    SocketChannel socket = ( SocketChannel)o;
                    setProcessing(true);
                    this.requestHandler.handleRequest( socket, callback );
                    setProcessing(false);
                }
            } catch( Exception e ) {
            	logger.error("exception occured while attempting to handle request",e);
            }
        }
        if (logger!=null) {
        	logger.debug("request thread shutting down {threadno='"+threadNumber+"'}");
        }
    }
}
