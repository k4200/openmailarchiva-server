
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

 package com.stimulus.archiva.store;

import java.io.Serializable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.*;
import com.stimulus.archiva.domain.*;
import com.stimulus.archiva.exception.*;
import com.stimulus.util.*;
import java.util.*;

public class VolumeInfoService implements Serializable, Service, Runnable {

	 private static final long serialVersionUID = -1313413162502378522L;
	 protected static final Log logger = LogFactory.getLog(VolumeInfoService.class);
	 protected static ScheduledExecutorService scheduler;
	 protected static ScheduledFuture<?> scheduledTask;
	 protected static int DISKSPACE_CHECK_WAIT_HOURS = 24;
	 protected Status status = Status.STOPPED;
     protected String adminEmail;
     protected boolean adminNotify;
     protected ServiceDelegate serviceDelegate;
     protected Calendar lastCheck = null;

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
		 scheduler = Executors.newScheduledThreadPool(1,ThreadUtil.getDaemonThreadFactory(getServiceName()));
		 scheduledTask = scheduler.scheduleWithFixedDelay(this,0,1,TimeUnit.MINUTES);
		 serviceDelegate.startup();
	 }

	 public void prepareShutdown() {
		 if (isAlive()) {
			 serviceDelegate.prepareShutdown();
			 if (scheduledTask!=null)
				 scheduledTask.cancel(true);
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

	 protected void finalize() throws Throwable {
		 shutdown();
	 }

	 public Status getStatus() {
		 return serviceDelegate.getStatus();
	 }

	 protected boolean checkActiveVolume() {
		 //logger.debug("checkActiveVolume()");
		 // here we check if active volume has not been checked yet
         Volume activeVolume = Config.getConfig().getVolumes().getVolume(Volume.Status.ACTIVE);
         if (activeVolume!=null && !activeVolume.isDiskSpaceChecked()) {
        	 //logger.debug("checking disk space on active volume");
	     	  try  { activeVolume.calculateSpace();
	     	  } catch (ArchivaException ae) {
	     		  logger.error("failed to check disk space:"+ae.getMessage(),ae);
	     	  }
	     	 //logger.debug("checkActiveVolume() return true");
	     	  return true;
         }
         //logger.debug("checkActiveVolume() return false");
         return false;
	 }

     public void run() {
    	//logger.debug("volume info run()");
    	Volume activeVolume = Config.getConfig().getVolumes().getVolume(Volume.Status.ACTIVE);
	    boolean activeChecked = checkActiveVolume();

    	// here we force-check all volumes
    	Calendar newCheck = Calendar.getInstance();
    	long difInHours = 0;

    	if (lastCheck!=null) {
    		difInHours = ((newCheck.getTime().getTime() - lastCheck.getTime().getTime())/(1000*60*60));
    	}
    	int diskSpaceCheckWaitHours = Config.getConfig().getVolumes().getDiskSpaceCheckWait();
		if (lastCheck==null || difInHours>diskSpaceCheckWaitHours) {
    		logger.debug("scheduled disk space check");

    		for (Volume volume : Config.getConfig().getVolumes().getVolumes()) {

    				if (activeChecked && volume==activeVolume)
    					continue; // already checked active volume
    				try  {
    					volume.calculateSpace();
    		     	} catch (ArchivaException ae) {
    		     		  logger.error("failed to check disk space:"+ae.getMessage(),ae);
    		     	}
    		}

    		logger.debug("volume info end()");
    		lastCheck = newCheck;
    	}

     }


     public Date midnight(Date date, TimeZone tz) {
    	  Calendar cal = new GregorianCalendar(tz);
    	  cal.setTime(date);
    	  cal.set(Calendar.HOUR_OF_DAY, 0);
    	  cal.set(Calendar.MINUTE, 0);
    	  cal.set(Calendar.SECOND, 0);
    	  cal.set(Calendar.MILLISECOND, 0);
    	  return cal.getTime();
     }


}
