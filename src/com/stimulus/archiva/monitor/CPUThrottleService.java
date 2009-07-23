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
import com.stimulus.archiva.exception.MessageStoreException;
import com.stimulus.util.ConfigUtil;
import com.stimulus.util.Crypto;
import com.stimulus.util.ThreadUtil;

import java.lang.management.*;
import java.util.Random;

public class CPUThrottleService implements Serializable, Service, Runnable, Props {

	 private static final long serialVersionUID = -1312411162102378528L;
	 protected static final Log logger = LogFactory.getLog(CPUThrottleService.class);
	 protected static ScheduledExecutorService scheduler;
	 protected static ScheduledFuture<?> scheduledTask;
	 protected Status status = Status.STOPPED;
     protected String adminEmail;
     protected boolean adminNotify;
     protected ServiceDelegate serviceDelegate;
     protected static final String throttleEnableKey = "throttle.enable";
     protected static final String throttleIncreaseCPULoadThresholdKey = "cputhrottle.increase.cpuload.threshold";
     protected static final String throttleDecreaseCPULoadThresholdKey = "cputhrottle.decrease.cpuload.threshold";
     protected static final String throttleIncreaseMemoryKBThresholdKey = "cputhrottle.increase.memorykb.threshold";
     protected static final String throttleDecreaseMemoryKBThresholdKey = "cputhrottle.decrease.memorykb.threshold";
     protected static String defaultThrottleDecreaseCPULoadThreshold = "1.1";
     protected static String defaultThrottleIncreaseCPULoadThreshold = "0.90";
     protected static String defaultThrottleDecreaseMemoryKBThreshold = "4096";
     protected static String defaultThrottleIncreaseMemoryKBThreshold = "20480";
     protected static String defaultThrottleEnable = "no";
     protected float throttleDecreaseCPULoadThreshold;
     protected float throttleIncreaseCPULoadThreshold;
     protected float throttleDecreaseMemoryKBThreshold;
     protected float throttleIncreaseMemoryKBThreshold;
     protected boolean throttleEnable;

	 public CPUThrottleService() {
		 serviceDelegate = new ServiceDelegate("cpu", this, logger);
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
			 if (scheduledTask!=null)
				 scheduledTask.cancel(true);
			 serviceDelegate.prepareShutdown();
		 }
	 }

	 public void shutdown() {
		 if (isAlive()) {
			 if (scheduler!=null)
				 scheduler.shutdownNow();
			 serviceDelegate.shutdown();
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

    	 if (!throttleEnable) return;

    	 OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
    	 double loadAverage = os.getSystemLoadAverage();
    	 //loadAverage = Math.random() + Math.random();
    	 long freeMemory = Runtime.getRuntime().freeMemory();
    	 int archiveThreads = Config.getConfig().getArchiver().getRunningArchiveThreads();
    	 int maxArchiveThreads = Config.getConfig().getArchiver().getArchiveThreads();

    	 if (loadAverage==-1) {  // windows
    		 if (archiveThreads > 1 && (freeMemory/1024)<throttleDecreaseMemoryKBThreshold) {
    			 archiveThreads = archiveThreads > 1 ? archiveThreads -1 : archiveThreads;
    			 Config.getConfig().getArchiver().setRunningArchiveThreads(archiveThreads);
            	 logger.debug("system overloaded. lowered no. of archiving threads. {load='"+loadAverage+"', freeMemory='"+freeMemory/1024+"kb', archiveThreads='"+archiveThreads+"'}");
            	 return;
    		 }
    		 if (archiveThreads < maxArchiveThreads && (freeMemory/1024)>4096) {
        		 archiveThreads = archiveThreads < maxArchiveThreads ? archiveThreads + 1 : archiveThreads;
        		 Config.getConfig().getArchiver().setRunningArchiveThreads(archiveThreads);
        		 logger.debug("system underloaded. increase no. of archiving threads. {load='"+loadAverage+"', freeMemory='"+freeMemory/1024+"kb', archiveThreads='"+archiveThreads+"'}");
        		 return;
        	 }
    	 } else {  // unix
    		 if (archiveThreads > 1 && (loadAverage>throttleDecreaseCPULoadThreshold || (freeMemory/1024)<throttleDecreaseMemoryKBThreshold)) {
        		 archiveThreads = archiveThreads > 1 ? archiveThreads -1 : archiveThreads;
        		 Config.getConfig().getArchiver().setRunningArchiveThreads(archiveThreads);
            	 logger.debug("system overloaded. lowered no. of archiving threads. {load='"+loadAverage+"', freeMemory='"+freeMemory/1024+"kb', archiveThreads='"+archiveThreads+"'}");
            	 return;
        	 }
    		 if (archiveThreads < maxArchiveThreads && (loadAverage<throttleIncreaseCPULoadThreshold && (freeMemory/1024)>throttleIncreaseMemoryKBThreshold)) {
        		 archiveThreads = archiveThreads < maxArchiveThreads ? archiveThreads + 1 : archiveThreads;
        		 Config.getConfig().getArchiver().setRunningArchiveThreads(archiveThreads);
        		 logger.debug("system underloaded. increase no. of archiving threads. {load='"+loadAverage+"', freeMemory='"+freeMemory/1024+"kb', archiveThreads='"+archiveThreads+"'}");
        		 return;
        	 }
    	 }
     }



 	public void saveSettings(String prefix, Settings prop, String suffix) {
 		logger.debug("saving throttle settings");
 		prop.setProperty(throttleDecreaseCPULoadThresholdKey, Float.toString(throttleDecreaseCPULoadThreshold));
 	  	prop.setProperty(throttleIncreaseCPULoadThresholdKey, Float.toString(throttleIncreaseCPULoadThreshold));
 	  	prop.setProperty(throttleDecreaseMemoryKBThresholdKey,Float.toString(throttleDecreaseMemoryKBThreshold));
        prop.setProperty(throttleIncreaseMemoryKBThresholdKey,Float.toString(throttleIncreaseMemoryKBThreshold));
        prop.setProperty(throttleEnableKey,ConfigUtil.getYesNo(throttleEnable));

 	}

 	public boolean loadSettings(String prefix, Settings prop, String suffix) {
 		logger.debug("loading throttle settings");
 		throttleDecreaseCPULoadThreshold = ConfigUtil.getFloat(prop.getProperty(throttleDecreaseCPULoadThresholdKey),defaultThrottleDecreaseCPULoadThreshold);
        throttleIncreaseCPULoadThreshold = ConfigUtil.getFloat(prop.getProperty(throttleIncreaseCPULoadThresholdKey),defaultThrottleIncreaseCPULoadThreshold);
        throttleDecreaseMemoryKBThreshold = ConfigUtil.getFloat(prop.getProperty(throttleDecreaseMemoryKBThresholdKey),defaultThrottleDecreaseMemoryKBThreshold);
        throttleIncreaseMemoryKBThreshold = ConfigUtil.getFloat(prop.getProperty(throttleIncreaseMemoryKBThresholdKey),defaultThrottleIncreaseMemoryKBThreshold);
        throttleEnable = ConfigUtil.getBoolean(prop.getProperty(throttleEnableKey),defaultThrottleEnable);
        return true;
 	}



}
