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

package com.stimulus.archiva.monitor;

import java.io.Serializable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.*;
import com.stimulus.archiva.domain.*;
import com.stimulus.archiva.service.MessageService;
import com.stimulus.util.ThreadUtil;

public class ReArchiveService implements Serializable, Service, Runnable {

	 private static final long serialVersionUID = -1310411162102378523L;
	 protected static final Log logger = LogFactory.getLog(ReArchiveService.class);
	 protected static ScheduledExecutorService scheduler;
	 protected static ScheduledFuture<?> scheduledTask;
	 protected Status status = Status.STOPPED;
     protected String adminEmail;
     protected boolean adminNotify;
     protected ServiceDelegate serviceDelegate;

	 public ReArchiveService() {
		 serviceDelegate = new ServiceDelegate("rearchive", this, logger);
	 }
	 public String getServiceName() {
		 return serviceDelegate.getServiceName();
	 }

	 public boolean isAlive() {
		 return serviceDelegate.isAlive(!scheduler.isShutdown());
	}

	 public void startup() {
		 scheduler = Executors.newScheduledThreadPool(1,ThreadUtil.getDaemonThreadFactory(getServiceName()));
		 scheduledTask = scheduler.scheduleWithFixedDelay(this,1,240,TimeUnit.MINUTES);
		 serviceDelegate.startup();
	 }

	 public void prepareShutdown() {
		 if (isAlive()) {
			 if (scheduledTask!=null)
				 scheduledTask.cancel(true);
			 serviceDelegate.prepareShutdown();
	 	 }
	 }

	 public void shutdown() {
		 if (isAlive()) {
			 serviceDelegate.shutdown();
			 if (scheduler!=null)
				 scheduler.shutdownNow();
		 }
	 }

	 public void reloadConfig() {
		 serviceDelegate.reloadConfig();
	 }

	 @Override
	protected void finalize() throws Throwable {
		 shutdown();
	 }

	 public Status getStatus() {
		 return serviceDelegate.getStatus();
	 }


     public void run() {
    	 if (MessageService.getNoMessagesForRecovery()>0) {
    		 MessageService.recoverNoArchiveMessages(null);
    	 }
     }


}
