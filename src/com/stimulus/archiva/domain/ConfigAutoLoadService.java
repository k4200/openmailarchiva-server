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
 package com.stimulus.archiva.domain;

import java.util.concurrent.*;
import java.io.File;
import java.io.Serializable;
import org.apache.commons.logging.*;
import com.stimulus.archiva.domain.Config;
import com.stimulus.archiva.domain.Service;
import com.stimulus.archiva.service.MessageService;
import com.stimulus.util.ThreadUtil;
import java.util.concurrent.locks.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConfigAutoLoadService implements Runnable, Serializable, Service {

	 private static final long serialVersionUID = -1121431162020318521L;
	 protected static final Log logger = LogFactory.getLog(ConfigAutoLoadService.class);
	 protected static final Log audit = LogFactory.getLog("com.stimulus.archiva.audit");
	 protected static ScheduledExecutorService scheduler;
	 protected static ScheduledFuture<?> scheduledTask;
	 protected CopyOnWriteArrayList<ModifiedCheck> modifiedFiles = new CopyOnWriteArrayList<ModifiedCheck>();
	 protected ServiceDelegate serviceDelegate;
	 protected boolean block = false;

	 public ConfigAutoLoadService() {
		 serviceDelegate = new ServiceDelegate("config auto load", this, logger);
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
		 registerModifiedCheckFiles();
		 scheduler = Executors.newScheduledThreadPool(1,ThreadUtil.getDaemonThreadFactory(getServiceName()));
		 scheduledTask = scheduler.scheduleWithFixedDelay(this,3,3,TimeUnit.SECONDS);
		 serviceDelegate.startup();
	 }

	 public void prepareShutdown() {
		 if (isAlive()) {
			 serviceDelegate.prepareShutdown();
		 }
	 }

	 public void shutdown() {
		 if (isAlive()) {
			 modifiedFiles.clear();
			 scheduler.shutdownNow();
			 serviceDelegate.shutdown();
		 }
	 }

	 public void reloadConfig() {
		 serviceDelegate.reloadConfig();
	 }

	 public Status getStatus() {
		 return serviceDelegate.getStatus();
	 }

	 @Override
	protected void finalize() throws Throwable {
	 }

	 public synchronized void registerModifiedCheckFiles() {
		 try {
			 modifiedFiles.clear();
		     modifiedFiles.add(new ServerConfigModifiedCheck());
		     modifiedFiles.add(new UsersConfigModifiedCheck());
		     for (Volume v : Config.getConfig().getVolumes().getVolumes()) {
		    	 modifiedFiles.add( new VolumeInfoModifiedCheck(v));
		     }
		 } catch (Throwable t) {
			 t.printStackTrace();
		 }
	 }

     public void run() {
    	 try {
	    	 for (ModifiedCheck check : modifiedFiles) {
	    		  check.update();
	    	 }
    	 } catch (ConcurrentModificationException ce) {

    	 } catch (Throwable t) {
			logger.error("exception occurred while auto loading:"+t.getMessage());
		 }
     }

	 public abstract class ModifiedCheck {

		 File file;
		 long modified;

		 public ModifiedCheck(File file) {
			 this.file = file;
			 modified = getModified(file);
		 }

		 public long getModified() {
			 return modified;
		 }

		 public File getFile() {
			 return file;
		 }

		 public boolean isModified() {

			 long newModified;
			 newModified = getModified(file);
			 return (modified!=newModified);
		 }

		 public void update() {
			 try {
				 if (!block) {
					 if (isModified())
						 modified();
				  	 modified = getModified(file);
				 }
			 } catch (Throwable t) {
					logger.error("exception occurred while auto loading:"+t.getMessage());
			 }
		 }

		 protected long getModified(File file) {
			 if (file.exists())
				 return file.lastModified();
			 else
				 return -1;
		 }

		 public abstract void modified();
	 }

	 public class UsersConfigModifiedCheck extends ModifiedCheck {

		 public UsersConfigModifiedCheck() {
			 super(new File(Config.getFileSystem().getConfigurationPath() + File.separator + "users.conf"));

		 }
		 @Override
		public synchronized void modified() {
			 try {
				 audit.info("detected change in users.conf file. auto loading configuration.");
				 Config.getConfig().getBasicIdentity().loadXMLFile();
				 Config.getConfig().getServices().reloadConfigAll();
    		 } catch (Throwable e) {
    			 logger.error("failed to load users.conf configuration during auto load:"+e.getMessage(),e);
    		 }
		 }
	 }

	 public class ServerConfigModifiedCheck extends ModifiedCheck {

		 public ServerConfigModifiedCheck() {
			 super(new File(Config.getFileSystem().getConfigurationPath() + File.separator + "server.conf"));

		 }
		 @Override
		public synchronized void modified() {
			 try {
				 audit.info("detected change in server.conf file. auto loading configuration");
				 Config.getConfig().loadSettings(MailArchivaPrincipal.SYSTEM_PRINCIPAL);
				 registerModifiedCheckFiles();
    		 } catch (Throwable e) {
    			 logger.error("failed to load users.conf configuration during auto load:"+e.getMessage(),e);
    		 }
		 }
	 }

 public class VolumeInfoModifiedCheck extends ModifiedCheck {

	 	public Volume volume;

		 public VolumeInfoModifiedCheck(Volume volume) {
			 super(new File(volume.getPath()+File.separator+Volume.INFO_FILE));
			 this.volume = volume;
		 }
		 @Override
		public synchronized void modified() {
			 try {
				 audit.info("detected change in volumeinfo file. auto loading configuration");
				 Config.getConfig().loadSettings(MailArchivaPrincipal.SYSTEM_PRINCIPAL);
    		 } catch (Throwable e) {
    			 logger.error("failed to load users.conf configuration during auto load:"+e.getMessage(),e);
    		 }
		 }
	 }
	}