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

import org.apache.log4j.Logger;
import java.nio.channels.*;
import com.stimulus.archiva.domain.*;

public class RequestThread extends Thread
{
	protected RequestQueue queue;
    protected static Logger logger = Logger.getLogger(RequestThread.class);
    protected boolean running;
    protected boolean processing = false;
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

    public boolean isProcessing() {
        return this.processing;
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
                    this.processing = true;
                    this.requestHandler.handleRequest( socket, callback );
                    this.processing = false;
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