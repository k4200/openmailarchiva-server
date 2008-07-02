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
package com.stimulus.archiva.domain;

import java.io.File;
import java.io.Serializable;
import org.apache.log4j.Logger;
import com.stimulus.archiva.domain.Config;
import com.stimulus.archiva.domain.Service;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ConfigAutoLoadService implements Runnable, Serializable, Service {
	 
	 private static final long serialVersionUID = -1121431162020318521L;
	 protected static final Logger logger = Logger.getLogger(ConfigAutoLoadService.class);
	 protected static final Logger audit = Logger.getLogger("com.stimulus.archiva.audit");
	 protected static ScheduledExecutorService scheduler;
	 protected static ScheduledFuture<?> scheduledTask;
	 protected ArrayList<ModifiedCheck> modifiedFiles = new ArrayList<ModifiedCheck>();
	 protected ServiceDelegate serviceDelegate;
	 
	 public ConfigAutoLoadService() {
		 serviceDelegate = new ServiceDelegate("config auto load", this, logger);
	 }
	 public String getServiceName() {
		 return serviceDelegate.getServiceName();
	 }
	
	 public boolean isAlive() {
		 return serviceDelegate.isAlive(!scheduler.isShutdown());
	}
	 
	 public void startup() {
		 registerModifiedCheckFiles();
		 scheduler = Executors.newScheduledThreadPool(1);
		 scheduledTask = scheduler.scheduleAtFixedRate(this, 5, 5, TimeUnit.SECONDS);
		 serviceDelegate.startup();
	 }
	
	 public void prepareShutdown() {
		 serviceDelegate.prepareShutdown();
	 }
	 
	 public void shutdown() {
		 scheduler.shutdownNow();
		 serviceDelegate.shutdown();
	 }
	 
	 public void reloadConfig() {
		 serviceDelegate.reloadConfig();
	 }
	
	 public Status getStatus() {
		 return serviceDelegate.getStatus();
	 }
	
	 @Override
	protected void finalize() throws Throwable {
		 shutdown();
	 }
	 
	 public void registerModifiedCheckFiles() {
		 modifiedFiles.clear();
	     modifiedFiles.add(new ServerConfigModifiedCheck());
	     modifiedFiles.add(new UsersConfigModifiedCheck());
	 }
	
     public void run() {
    	 for (ModifiedCheck check : modifiedFiles) {
    		  check.update();
    	 }
     }
     
	 public abstract class ModifiedCheck {
		 
		 File file;
		 long modified;
		 
		 public ModifiedCheck(File file) {
			 this.file = file;
			 modified = file.lastModified();
		 }
		 
		 public long getModified() {
			 return modified;
		 }
		 
		 public File getFile() {
			 return file;
		 }
		 
		 public boolean isModified() {
			 long newModified = file.lastModified();
			 return (modified!=newModified);
		 }
		 
		 public void update() {
			 if (isModified())
				 modified();
			 modified = file.lastModified();
		 }
		 
		 public abstract void modified(); 
	 }
	 
	 public class UsersConfigModifiedCheck extends ModifiedCheck {
		 
		 public UsersConfigModifiedCheck() {
			 super(new File(Config.getFileSystem().getConfigurationPath() + File.separatorChar + "users.conf"));
			 
		 }
		 @Override
		public void modified() {
			 
			 audit.info("detected change in users.conf file. auto loading configuration.");
			 try {
    			 Config.getConfig().loadSettings(MailArchivaPrincipal.SYSTEM_PRINCIPAL);
    		 } catch (Exception e) {
    			 logger.error("failed to load users.conf configuration during auto load",e);
    		 }
		 }
	 }
	 
	 public class ServerConfigModifiedCheck extends ModifiedCheck {
		 
		 public ServerConfigModifiedCheck() {
			 super(new File(Config.getFileSystem().getConfigurationPath() + File.separatorChar + "server.conf"));
			 
		 }
		 @Override
		public void modified() {
			 audit.info("detected change in server.conf file. auto loading configuration");
			 try {
    			 Config.getConfig().loadSettings(MailArchivaPrincipal.SYSTEM_PRINCIPAL);
    			 Config.getConfig().getServices().reloadConfigAll();
    		 } catch (Exception e) {
    			 logger.error("failed to load server.conf configuration during auto load",e);
    		 }
		 }
	 }
	 
	}