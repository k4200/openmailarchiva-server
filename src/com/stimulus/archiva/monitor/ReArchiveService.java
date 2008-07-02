package com.stimulus.archiva.monitor;

import java.io.Serializable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import com.stimulus.archiva.domain.*;
import com.stimulus.archiva.service.MessageService;

public class ReArchiveService implements Serializable, Service, Runnable {
	 
	 private static final long serialVersionUID = -1310411162102378523L;
	 protected static final Logger logger = Logger.getLogger(ReArchiveService.class);
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
		 scheduler = Executors.newScheduledThreadPool(1);
		 scheduledTask = scheduler.scheduleAtFixedRate(this,5,30,TimeUnit.MINUTES);
		 serviceDelegate.startup();
	 }
	
	 public void prepareShutdown() {
		 serviceDelegate.prepareShutdown();
		 scheduledTask.cancel(true);
	 }
	 
	 public void shutdown() {
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
    	 if (MessageService.getNoMessagesForRecovery()>0) {
    		 MessageService.recoverNoArchiveMessages(null);
    	 }
     }
   
     
}
