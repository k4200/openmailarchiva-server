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

import java.util.*;

import org.apache.commons.logging.*;
import com.stimulus.archiva.exception.*;
import com.stimulus.archiva.domain.*;

public class RequestQueue
{
	protected LinkedList<Object> queue = new LinkedList<Object>();
    protected int maxQueueLength;
    protected int minThreads;
    protected int maxThreads;
    protected int currentThreads = 0;
    protected String requestHandlerClassName;
    protected List<RequestThread> threadPool = new ArrayList<RequestThread>();
    protected boolean running = true;
    protected static Log logger = LogFactory.getLog(RequestQueue.class);
    protected FetchMessageCallback callback;
    protected String serverName;

    public RequestQueue( String serverName,
    					 String requestHandlerClassName,
    					 FetchMessageCallback callback,
                         int maxQueueLength,
                         int minThreads,
                         int maxThreads ) {

        this.requestHandlerClassName = requestHandlerClassName;
        this.maxQueueLength = maxQueueLength;
        this.minThreads = minThreads;
        this.maxThreads = maxThreads;
        this.currentThreads = this.minThreads;
        this.callback = callback;
        this.serverName = serverName;

        for( int i=0; i<this.minThreads; i++ ) {
            RequestThread thread = new RequestThread( this, i, requestHandlerClassName, callback );
            thread.setName(serverName+" request processor "+i);
            thread.start();
            this.threadPool.add( thread );
        }
    }


    public String getRequestHandlerClassName() {
        return this.requestHandlerClassName;
    }


    public boolean isAlive() {
    	 if (currentThreads<1)
    		 return true;

    	 List<RequestThread> threadPool = new ArrayList<RequestThread>();
    	 for (RequestThread rt : threadPool) {
    		 if (rt.isAlive())
    			 return true;
    	 }
    	 return false;
    }

    public synchronized void add( Object o ) throws IncomingException {

        if( queue.size() > this.maxQueueLength ) {
            throw new IncomingException("the request queue is full. {maxsize='"+this.maxQueueLength+"'",logger );
        }


        queue.addLast( o );


        boolean availableThread = false;

        for (RequestThread rt : threadPool) {

            if( !rt.isProcessing() )
            {
            	logger.debug("found an available thread");
                availableThread = true;
                break;
            }
            logger.debug("incoming request thread is busy");
        }


        if( !availableThread )
        {
            if( this.currentThreads < this.maxThreads )
            {
            	logger.debug("creating a new thread to satisfy the incoming request");
                RequestThread thread = new RequestThread( this, currentThreads, this.requestHandlerClassName, callback );
                thread.setName(serverName+" request processor "+currentThreads);
                thread.start();
                this.threadPool.add( thread );
                currentThreads++;
            }
            else
            {
            	logger.debug("incoming connection thread pool has reach maxmimum size. waiting..");
            }
        }
        notifyAll();
    }

    public synchronized Object getNextObject()
    {
       while( queue.isEmpty() )
        {
            try
            {
                if( !running )
                {
                    return null;
                }
                wait();
            }
            catch( InterruptedException ie ) {}
        }
        return queue.removeFirst();
    }


    public synchronized void shutdown()
    {
    	logger.debug("shutting down request threads..." );
        this.running = false;

        for (RequestThread rt: threadPool) {
            rt.killThread();
        }

        notifyAll();
    }
}
