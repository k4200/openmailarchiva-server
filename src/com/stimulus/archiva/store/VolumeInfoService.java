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

package com.stimulus.archiva.store;

import java.io.Serializable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import com.stimulus.archiva.domain.*;

public class VolumeInfoService implements Runnable, Serializable, Service {
	 
	 private static final long serialVersionUID = -1311431162000378521L;
	 protected static final Logger logger = Logger.getLogger(VolumeInfoService.class);
	 protected static ScheduledExecutorService scheduler;
	 protected static ScheduledFuture<?> scheduledTask;
	 protected ServiceDelegate serviceDelegate;
	 
	 public VolumeInfoService() {
		 serviceDelegate = new ServiceDelegate("diskspace checker", this, logger);
	 }
	 public String getServiceName() {
		 return serviceDelegate.getServiceName();
	 }
	
	 public boolean isAlive() {
		 return serviceDelegate.isAlive(!scheduler.isShutdown());
	}
	 
	 public void startup() {
		 scheduler = Executors.newScheduledThreadPool(1);
		 scheduledTask = scheduler.scheduleAtFixedRate(this, 0, Config.getConfig().getVolumes().getDiskSpaceCheckWait(), TimeUnit.SECONDS);
		 serviceDelegate.startup();
		
	 }
	
	 public void prepareShutdown() {
		 serviceDelegate.prepareShutdown();
		 scheduledTask.cancel(true);
	 }
	 
	 public void shutdown() {
		 scheduledTask.cancel(true);
		 scheduler.shutdownNow();
		 serviceDelegate.shutdown();
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
    	Config.getConfig().getVolumes().readyActiveVolume();
        Volume activeVolume = Config.getConfig().getVolumes().getActiveVolume();
        try {
              if (activeVolume!=null) {
            	  activeVolume.calculateSpace();
            	  if (serviceDelegate.getStatus() == Status.STOPPED) {
            		  return;
            	  }
                  activeVolume.enoughDiskSpace(); // warning
              }
        } catch (Exception e) {
            logger.error("failed to retrieve disk space {"+activeVolume+"}",e);
        }
     }
     
	}
