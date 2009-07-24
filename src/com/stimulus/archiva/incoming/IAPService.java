
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

import org.apache.commons.logging.*;
import java.util.*;
import com.stimulus.archiva.domain.*;

import java.util.concurrent.*;

public class IAPService implements Service {

	private static Log logger = LogFactory.getLog(IAPService.class);
	protected boolean shutdown = false;
	protected String serverAddress;
	protected int port;
	protected IAPRunnable iapworker;
	protected ExecutorService executor; 
	protected Status status = Status.STOPPED;
	protected ServiceDelegate serviceDelegate;
	
	public IAPService() {
		 serviceDelegate = new ServiceDelegate("imap/pop", this, logger);
	}
	
	 public String getServiceName() {
		 return serviceDelegate.getServiceName();
	 }
	
	 public boolean isAlive() {
		 return serviceDelegate.isAlive(!executor.isShutdown());
	 }
	 
	 public void startup() {
			MailboxConnection connection;
			Config config = Config.getConfig();
			connection = config.getMailboxConnections().getConnection();
			executor = Executors.newCachedThreadPool();
			iapworker = new IAPRunnable("mailbox worker", null, connection,config.getMailboxConnections().getPollingIntervalSecs(),Config.getConfig().getFetchMessageCallback());
			logger.debug("startup {name='"+iapworker.getName()+"'}");
			if (connection.getEnabled()) {	 
				executor.execute(iapworker);
			}
			serviceDelegate.startup();
	 }
	
	 public void prepareShutdown() {
		if (isAlive()) {
			 serviceDelegate.prepareShutdown();
			 iapworker.prepareShutdown();
		}
	 }
	 
	 public void shutdown() {
 		if (isAlive()) {
			 if (executor!=null)
				 executor.shutdown();
			logger.debug("shutdown iap worker {name='"+iapworker.getName()+"'}");
			iapworker.interrupt();
			iapworker.shutdown();
			serviceDelegate.shutdown();
		}
	 }
	 
	 public void reloadConfig() {
		 MailboxConnection connection;
		 Config config = Config.getConfig();	
		 connection = config.getMailboxConnections().getConnection();
		 logger.debug("checking mailbox connections");
		 if (iapworker.getMailboxConnection().equals(connection)) {
				 logger.debug("found equivalent mailbox connection {"+iapworker.getMailboxConnection()+"}");
		 } else {
			 logger.warn("something has changed in the mailbox connection configuration. will restart.");
			 prepareShutdown();
			 shutdown();
			 startup();
			 serviceDelegate.reloadConfig();
		 } 
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
