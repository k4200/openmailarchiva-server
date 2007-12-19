
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

public class TempFiles implements Serializable {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 4872238025782129277L;
	protected ArrayList<TempFile> fileDeleteList = new ArrayList<TempFile>();
    public static final int DELETE_WAIT = 30; // minutes
    protected boolean exit = false;
    protected Thread deleteHelper;
    protected transient Object lock = new Object();
    
    public TempFiles() {
    	deleteHelper = new DeleteHelper();
    }
    
    public void startDaemon() {
    	deleteHelper.start();
    }
    
	 public void markForDeletion(File file) {
		 synchronized(lock) {
		     fileDeleteList.add(new TempFile(file));
			 file.deleteOnExit(); // just in case
		 }
	 }
	 
	 protected void finalize() throws Throwable {
		 synchronized(lock) {
			 exit = true;
			 for (TempFile temp : fileDeleteList) {
		         try { temp.getFile().delete(); } catch(Exception e) {}
		     }
		     fileDeleteList.clear();
		 }
	     super.finalize();
	 }
	 
	 public class TempFile implements Serializable{
		 
		private static final long serialVersionUID = 1024913070784419025L;
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
	 
	 public class DeleteHelper extends Thread implements Serializable{ 
		private static final long serialVersionUID = -6971822512796791665L;

			public DeleteHelper() {
		    	setDaemon(true);
		    }
		    
	        public void run() {
	        	
	        	setName("deleteHelper");
	        	while (!exit) {
	        	  synchronized(lock) {
	        		  for (Iterator it = fileDeleteList.iterator (); it.hasNext (); ) {
	        			  	TempFile temp = (TempFile)it.next();
	        			  	if (temp.getOld()) {
	        			  		it.remove();
	        			  		
		        				try { temp.getFile().delete(); } catch (Exception e) {}
		        			}
	        		  }
	        	  }
        		  try { sleep(60000); } catch (Exception e) { exit=true; }
	        	}
	        }
	 }

}
