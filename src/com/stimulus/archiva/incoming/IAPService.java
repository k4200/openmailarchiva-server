
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
import java.util.*;
import com.stimulus.archiva.domain.*;

import java.util.concurrent.*;

public class IAPService implements Service {

	private static Logger logger = Logger.getLogger(IAPService.class);
	protected FetchMessageCallback callback;
	protected boolean shutdown = false;
	protected String serverAddress;
	protected int port;
	protected ArrayList<IAPRunnable> iapworkers = new ArrayList<IAPRunnable>();
	protected ExecutorService executor; 
	protected Status status = Status.STOPPED;
	protected ServiceDelegate serviceDelegate;
	
	public IAPService(FetchMessageCallback callback) {
		 this.callback = callback;
		 serviceDelegate = new ServiceDelegate("imap/pop", this, logger);
	}
	
	 public String getServiceName() {
		 return serviceDelegate.getServiceName();
	 }
	
	 public boolean isAlive() {
		 return serviceDelegate.isAlive(!executor.isShutdown());
	 }
	 
	 public void startup() {
		iapworkers.clear();
		MailboxConnection connection;
		Config config = Config.getConfig();
		connection = config.getMailboxConnections().getConnection();
		int i = 1;
		executor = Executors.newCachedThreadPool();

    	IAPRunnable worker = new IAPRunnable("mailbox worker "+i, null, connection,
											 config.getMailboxConnections().getPollingIntervalSecs(),callback);
		iapworkers.add(worker);
		logger.debug("startup {name='"+worker.getName()+"'}");
		if (connection.getEnabled()) {	 
			executor.execute(worker);
		}
	serviceDelegate.startup();
	}
	
	 public void prepareShutdown() {
		 serviceDelegate.prepareShutdown();
		 for (IAPRunnable iapworker : iapworkers) {
				iapworker.prepareShutdown();
		 }
	 }
	 
	 public void shutdown() {
		executor.shutdown();
		for (IAPRunnable iapworker : iapworkers) {
			logger.debug("shutdown iap worker {name='"+iapworker.getName()+"'}");
			iapworker.interrupt();
			iapworker.shutdown();
		}
		serviceDelegate.shutdown();
	 }
	 
	 public void reloadConfig() {
		 MailboxConnection connection;
		 Config config = Config.getConfig();	
		 connection = config.getMailboxConnections().getConnection();
		 boolean changed = false;
		 
		 boolean found = false;
		 logger.debug("checking mailbox connections");
		 for (IAPRunnable iapworker : iapworkers) {
			 if (iapworker.getMailboxConnection().equals(connection)) {
				 found = true;
				 logger.debug("found equivalent mailbox connection {"+iapworker.getMailboxConnection()+"}");
				 break;
			 }
		 }
		 if (!found) {
			 changed = true;
		 }
		
		 if (changed) {
			 logger.warn("something has changed in the mailbox connection configuration. will restart.");
			 prepareShutdown();
			 shutdown();
			 startup();
			 serviceDelegate.reloadConfig();
		 } 
	 }
	 
	 @Override
	protected void finalize() throws Throwable {
		 shutdown();
	 }
	 
	 public Status getStatus() {
		 return serviceDelegate.getStatus();
	 }

	
	public void testConnection(MailboxConnection connection, 
							   IAPRunnable.IAPTestCallback testCallback) {
			Config config = Config.getConfig();
			ExecutorService executor = Executors.newSingleThreadExecutor();
			IAPRunnable worker = new IAPRunnable("iap test", testCallback, connection, 
												 config.getMailboxConnections().getPollingIntervalSecs(),null);
			executor.execute(worker);
	}
   
    
}
