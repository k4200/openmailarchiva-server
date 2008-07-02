
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
package com.stimulus.util;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.*;
import com.stimulus.archiva.index.VolumeIndex;

public class TempFiles implements Serializable, Runnable {
    
    
	private static final long serialVersionUID = 4872238025782129277L;
	protected ArrayList<TempFile> fileDeleteList = new ArrayList<TempFile>();
	ScheduledExecutorService scheduler;
	ScheduledFuture<?> scheduledTask;
    protected Object lock = new Object();
    protected static final Log logger = LogFactory.getLog(VolumeIndex.class.getName());
    public static final int DELETE_WAIT = 5; // minutes
    
    public TempFiles() {
		 scheduler = Executors.newScheduledThreadPool(1); 
    }
    
    public void startDaemon() {
   	 	scheduledTask = scheduler.scheduleAtFixedRate(this, 5, 5, TimeUnit.SECONDS);
    }
    
    public void stopDaemon() {
    	scheduledTask.cancel(true);
		scheduler.shutdownNow();
    }
    
	 public void markForDeletion(File file) {
		 synchronized(lock) {
		     fileDeleteList.add(new TempFile(file));
			 file.deleteOnExit(); // just in case
		 }
	 }
	 
	 @Override
	protected void finalize() throws Throwable {
		 stopDaemon();
	 }
	 
	 public class TempFile {
		 
		 File file;
		 long created;
		 
		 public TempFile(File file) {
			 this.file = file;
			 created = System.currentTimeMillis();
		 }
		 
		 public File getFile() { return file; } 
		 
		 public boolean getOld() {
			 long now = System.currentTimeMillis()-created;
			 float elapsedmins = now/(60*1000F);
			 return elapsedmins > DELETE_WAIT;
		 }
	 }

        public void run() {
        	
        	Thread.currentThread().setName("deleteHelper");
        	synchronized(lock) {
        		  for (Iterator it = fileDeleteList.iterator (); it.hasNext (); ) {
        			  	TempFile temp = (TempFile)it.next();
        			  	if (temp.getOld()) {
        			  		logger.debug("removing temporary file {tempfile='"+temp.getFile().getAbsolutePath()+"'");
        			  		it.remove();
        			  		try { temp.getFile().delete(); } catch (Exception e) {}
	        			}
        		  }
        	  }
        }
	

}
