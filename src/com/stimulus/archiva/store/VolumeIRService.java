package com.stimulus.archiva.store;

import java.io.File;
import java.io.Serializable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.*;
import com.stimulus.archiva.domain.Config;
import com.stimulus.archiva.domain.Service;
import com.stimulus.archiva.domain.ServiceDelegate;
import com.stimulus.archiva.domain.Volume;
import com.stimulus.archiva.exception.ConfigurationException;
import com.stimulus.archiva.domain.*;
import com.stimulus.archiva.monitor.*;
import com.stimulus.util.ThreadUtil;

import java.util.Hashtable;

public class VolumeIRService implements Runnable, Serializable, Service {
	  
	 private static final long serialVersionUID = -1331431169000318321L;
	 protected static final Log logger = LogFactory.getLog(VolumeIRService.class);
	 protected static ScheduledExecutorService scheduler;
	 protected static ScheduledFuture<?> scheduledTask;
	 protected Status status = Status.STOPPED;
	 protected ServiceDelegate serviceDelegate;
	 protected Hashtable <String,Boolean> ejectedTable = new Hashtable<String,Boolean>();
	 protected boolean block = false;
	 
	 public VolumeIRService() {
		 serviceDelegate = new ServiceDelegate("volume insert/removal", this, logger);
	 }
	 public String getServiceName() {
		 return serviceDelegate.getServiceName();
	 }
	
	 public boolean isAlive() {
		 return serviceDelegate.isAlive(!scheduler.isShutdown());
	}
	 
	 
	 public void block() {
		 block = true;
	 }
	 
	 public void unblock() {
		 block = false;
	 }
	
	 
	 public void startup() {
		 scheduler = Executors.newScheduledThreadPool(1,ThreadUtil.getDaemonThreadFactory(getServiceName()));
		 scheduledTask = scheduler.scheduleWithFixedDelay(this, 1, 1, TimeUnit.SECONDS);
		 ejectedTable.clear();
		 for (Volume volume: Config.getConfig().getVolumes().getVolumes()) {
			 ejectedTable.put(volume.getPath(), new Boolean(volume.isEjected()));
		 }
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
	 
	 
	 public void updateEjectedStatus(Volume v) {
		 Boolean oldEjected = ejectedTable.get(v.getPath());
         if (oldEjected==null || v.isEjected()!=oldEjected) {
        	 ejectedTable.put(v.getPath(),v.isEjected());
         }
	 }
	
	  public void run() { 
		  if (!block) {
			  try {
				  Config.getConfig().getVolumes().readyActiveVolume();
				  Config config = Config.getConfig();
				  if (config!=null) {
				      for (Volume v : config.getVolumes().getVolumes()) {
				    	  updateEjectedStatus(v);
				      }
				  }
			  } catch (Throwable t) {
				  // this should never happen, although we never want this loop to exit
				  logger.error("error ocurred in volumeir service:"+t.getMessage(),t);
			  }
		  }
	  }
 }
