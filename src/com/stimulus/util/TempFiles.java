
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
import java.util.*;
import java.util.concurrent.*;

import org.apache.commons.logging.*;
import com.stimulus.archiva.index.VolumeIndex;

public class TempFiles implements Serializable, Runnable {
    
    
	private static final long serialVersionUID = 4872238025782129277L;
	protected ConcurrentLinkedQueue<TempFile> deleteQueue = new ConcurrentLinkedQueue<TempFile>();
	protected ConcurrentHashMap<String,TempFile> deleteMap = new ConcurrentHashMap<String,TempFile>();
	ScheduledExecutorService scheduler;
	ScheduledFuture<?> scheduledTask;
    protected static final Log logger = LogFactory.getLog(VolumeIndex.class.getName());
    public static final int DELETE_WAIT = 20; // minutes
    
    public TempFiles() {
    	scheduler = Executors.newScheduledThreadPool(1,ThreadUtil.getDaemonThreadFactory("tempfiles"));
		 
    }
    
    public void startDaemon() {
   	 	scheduledTask = scheduler.scheduleAtFixedRate(this, 1, 1, TimeUnit.SECONDS);
    }
    
    public void stopDaemon() {
    	scheduledTask.cancel(true);
		scheduler.shutdownNow();
    }
    
	 public void markForDeletion(File file) {
		 	 TempFile tempFile = deleteMap.get(file.getPath());
		 	 if (tempFile!=null) {
		 		deleteQueue.remove(tempFile);
		 		deleteMap.remove(tempFile);
		 	 } else {
		 		 tempFile = new TempFile(file);
		 		 deleteQueue.add(new TempFile(file));
		 		 deleteMap.put(tempFile.getFile().getPath(),tempFile);
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
	 
	 private static long deleteOldTempFiles(File file) {
         long size = 0;
         if(file.isDirectory()) {
             File[] files = file.listFiles();
             if(files != null) {
                 for(int i = 0; i < files.length; i++) {
                     long modified = files[i].lastModified();
                     long now = System.currentTimeMillis()-modified;
                     float elapsedmins = now/(60*1000F);
                     if (elapsedmins > DELETE_WAIT)
                    	 files[i].delete();
                     	 try { Thread.sleep(10); } catch (Exception e) {} 
                 }
                 return size;
             } else return 0;
         } else return file.length();
    }

        public void run() {
        	Thread.currentThread().setName("deleteHelper");
        	if (deleteQueue.size()>0) {
	        	TempFile temp = deleteQueue.peek();
	    		if (temp!=null && temp.getOld()) {
			  		logger.debug("removing temporary file {tempfile='"+temp.getFile().getAbsolutePath()+"'}");
			  		deleteQueue.poll();
			  		deleteMap.remove(temp);
			  		try { temp.getFile().delete(); } catch (Exception e) {}
				} 
        	}
        }
        
        /*  // delete any stray files in the temp dir
        		  String tmpdir = System.getProperty("java.io.tmpdir");
        		  if (tmpdir!=null) 
        			  deleteOldTempFiles(new File(tmpdir));*/
        
}
