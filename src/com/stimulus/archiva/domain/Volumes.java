/*
 * Subversion Infos:
 * $URL$
 * $Author$
 * $Date$
 * $Rev$
*/

		
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.stimulus.archiva.domain.Volume.Status;
import com.stimulus.archiva.exception.ConfigurationException;
import com.stimulus.util.Compare;
import com.stimulus.util.DateUtil;

public class Volumes  implements Serializable {

	private static final long serialVersionUID = -1331431169000378587L;
	
    public enum Priority { PRIORITY_HIGHER,PRIORITY_LOWER };
    protected static Logger		logger					= Logger.getLogger(Volumes.class);
    protected int 				diskSpaceCheckWait;
    protected int 				diskSpaceWarn;
    protected int 				diskSpaceThreshold;
    protected boolean 			diskSpaceChecking;
    protected static final String INFO_FILE				= "volumeinfo";
    protected LinkedList<Volume> volumes              	= new LinkedList<Volume>();
    protected boolean  		 	shutdown 	            = true;
    protected Thread         	diskInfoWorker 	        = null;
    protected Thread			diskIrChecker           = null;

      public Volumes(boolean diskSpaceChecking, int diskSpaceCheckWait, int diskSpaceWarn, int diskSpaceThreshold) {
    	  this.diskSpaceCheckWait = diskSpaceCheckWait;
    	  this.diskSpaceWarn = diskSpaceWarn;
    	  this.diskSpaceThreshold = diskSpaceThreshold;
    	  this.diskSpaceChecking = diskSpaceChecking;
      }
      
      public boolean getDiskSpaceChecking() {
    	  return diskSpaceChecking;
      }
      
      public int getDiskSpaceCheckWait() {
    	  return diskSpaceCheckWait;
      }
      
      public int getDiskSpaceWarnBytes() {
    	return diskSpaceWarn * 1024 * 1024;
      }
      
      public int getDiskSpaceThresholdBytes() {
    	return diskSpaceThreshold * 1024 * 1024;
      }
      
 	  public void addVolume(String path, String indexPath,  int maxSize) throws ConfigurationException {
 	      	Volume mv = new Volume(this, path,indexPath, maxSize);
 		  	volumes.add(mv);
 		  	Collections.sort(volumes);
 	  }

 	 /*******************************/
	  /* Load/save volatile volume info
	   *******************************/

	  public void saveVolumeInfo(Volume v, boolean overwrite) throws ConfigurationException {
	      if (v == null)
	          throw new ConfigurationException("assertion failure: null volume",logger);
	      
	      // don't save to ejected volume
	      if (v.getStatus()==Volume.Status.EJECTED)
	    	  return;
	      
	      // make a new volume unused
	      if (v.getStatus()==Volume.Status.NEW)
	    	  v.setStatus(Volume.Status.UNUSED);
	      
	      String filename = v.getPath()+File.separator+INFO_FILE;
	      File file = new File(filename);
	      
	      if (overwrite || !file.exists()) { 
		      PrintWriter output = null;
		      try {
		        output = new PrintWriter( new FileWriter(file) );
		        output.println("# Archiva "+Config.getApplicationVersion()+" Volume Information");
		        output.println("# note: this file is crucial - do not delete it!");
		        output.println("id:"+v.getID());
		        output.println("modified:"+DateUtil.convertDatetoString(v.getModified()));
		        output.println("created:"+DateUtil.convertDatetoString(v.getCreated()));
		        output.println("status:"+v.getStatus());
	
		      } catch (IOException io) {
		    	  if (v.getStatus()!=Volume.Status.UNMOUNTED)
		    		  logger.error("failed to write volumeinfo. {"+v.toString()+"} cause:",io);
		      } finally {
		        if (output != null) output.close();
		      }
	      }
	    }
	  
	  public void saveAllVolumeInfo(boolean overwrite) throws ConfigurationException {
		  logger.debug("saveAllVolumeInfo()");
		  Iterator i = volumes.iterator();
	      while (i.hasNext()) {
	          Object o = i.next();
	          if (o!=null) {
	              Volume v = (Volume)o;
	              saveVolumeInfo(v,overwrite);
	          }
	      }
	  }
	  
	  public void loadAllVolumeInfo() throws ConfigurationException {
		  logger.debug("loadAllVolumeInfo()");
		  Iterator i = volumes.iterator();
	      while (i.hasNext()) {
	          Object o = i.next();
	          if (o!=null) {
	              Volume v = (Volume)o;
	              loadVolumeInfo(v);
	          }
	      }
	  }

	  public void loadVolumeInfo(Volume v) throws ConfigurationException {

	      	if (v == null)
	          throw new ConfigurationException("assertion failure: null volume",logger);
	      	
	      	logger.debug("loadVolumeInfo {path='"+v.getPath()+"',status='"+v.getStatus()+"'}");
	      		        
	      	if (v.getPath().length()<1)
	          throw new ConfigurationException("assertion failure: volume path not set",logger);
	      
	      	
	      	BufferedReader input = null;
	        String line = null;
	        logger.debug("volume ejection check {filename='"+v.getPath()+File.separator+INFO_FILE+"'}");
	        File file = new File(v.getPath()+File.separator+INFO_FILE);
	        if (!file.exists()) {
	            v.setStatus(Volume.Status.EJECTED);
	            logger.debug("volume is ejected (volumeinfo does not exist). {"+v+"}");
	            return;
	        }

	        try {

	           input = new BufferedReader( new FileReader(v.getPath()+File.separator+INFO_FILE) );
	           while (( line = input.readLine()) != null){
	               if (line.startsWith("#"))
	                   continue;
	               StringTokenizer st = new StringTokenizer(line, ":");
	               String name = st.nextToken();
	               try {
		               if (name.toLowerCase(Locale.ENGLISH).trim().equals("modified"))
		                   v.setModified(DateUtil.convertStringToDate(st.nextToken().trim()));
		               else if (name.toLowerCase(Locale.ENGLISH).trim().equals("created"))
		               	   v.setCreated(DateUtil.convertStringToDate(st.nextToken().trim()));
		               else if (name.toLowerCase(Locale.ENGLISH).trim().equals("id"))
		               	   v.setID(st.nextToken().trim());
		               else if (name.toLowerCase(Locale.ENGLISH).trim().equals("status")) {
		            	  Status status = Status.CLOSED; // default
		            	  try {
		            		  status = Status.valueOf(st.nextToken().trim());
		            	  } catch (IllegalArgumentException iae) {
		      	  	    		logger.error("failed to load volume.info: status attribute is set to an illegal value {vol='"+v+"'}");
		      	  	    		logger.error("volume will be set closed (due to error)");
		            	  }
		            	  
		            	  // here we make doubly sure that there is not another active volume
		            	  // if another volume is active, we close the current one and save it.
		            	  if (status == Volume.Status.ACTIVE) {
		            		  Volume activeVolume = getActiveVolume();
		            		  if (activeVolume!=null && !activeVolume.getPath().equalsIgnoreCase(v.getPath())) {
		            			  if (v.getModified().before(activeVolume.getModified())) {
		            				  v.setStatus(Volume.Status.CLOSED);
		            				  saveVolumeInfo(v,true);
		            			  } else {
		            				  activeVolume.setStatus(Volume.Status.CLOSED);
		            				  saveVolumeInfo(activeVolume,true);
		            			  }
		            			  Collections.sort(volumes); 
		            		  }
		            	  }
		            		  
                          v.setStatusNoAssertions(status);
                       }
	               } catch (Exception e) {
	                   logger.error("failed to parse information in volumeinfo file. check the contents of the file.{"+v+"}"); // could be acceptable
	               }
	           }

	        } catch (IOException e) {
	            logger.debug("failed to read volumeinfo {"+v+"}",e);

	        } finally {
	           if (input != null) {
	              try {
	                 input.close();
	              } catch (IOException ioe) {}
	           }
	        }
	        // we make sure that NEW entries become UNUSED
	        if (v.getStatus()==Volume.Status.NEW)
	            v.setStatus(Volume.Status.UNUSED);
	  }


	  public void touchActiveVolume() throws ConfigurationException {
		  
		  Volume v = getActiveVolume();
		  
		  if (v==null)
			  throw new ConfigurationException("failed to touch active volume. no volume is active",logger);
	      
		  if (v.getModified()==null) {
	          v.setCreated(new Date());
	      }
	      v.setModified(new Date());
	      saveVolumeInfo(v,true);
	  }


 	  public void removeVolume(int id) throws ConfigurationException {

 	     Volume v = volumes.get(id);
 	     if (v.getStatus()==Volume.Status.ACTIVE)
 	         throw new ConfigurationException("failed to delete active volume. it must be closed first",logger);
	  	 volumes.remove(v);
	  	Collections.sort(volumes);
 	  }

 	  public int indexOfVolume(Volume v) {
 		  	return volumes.indexOf(v);
 	  }

 	  public List<Volume> getVolumes() {
 		    return volumes;
 	  }

 	  public Volume getVolume(int index) {
 	  	return volumes.get(index);
 	  }

 	  public void setVolumePriority(int id, Priority priority)  {

 	 	LinkedList<Volume> list = volumes;
 	   	Volume v = list.get(id);

 	   	if (v.getStatus()!=Volume.Status.UNUSED) // can only reorder unused
 	   	    return;

 	   	if (priority==Priority.PRIORITY_HIGHER && id-1>=0) { // cannot affect non unused vols
 	   	    Volume vs = list.get(id-1);
 	   	    if (vs.getStatus()!=Volume.Status.UNUSED)
 	   	        return;
 	   	}

 	   	list.remove(v);
 	    switch (priority) {
 		  	    case PRIORITY_HIGHER:   if ((id-1)<=0)
 		  	    							list.addFirst(v);
 										else
 											list.add(id-1,v);
 		  	    					 	break;
 		  	    case PRIORITY_LOWER: 	if ((id+1)>=list.size())
 		  	    							list.addLast(v);
 		  	    						else
 		  	    							list.add(id+1,v);
 		  	                         	break;

 	      }

 	  }

 	   public void clearAllVolumes() {
 	  	 volumes.clear();
 	   }

 	   
 	   public Volume getNewVolume(String id) throws ConfigurationException {
 		  for (Volume volume : volumes) {
	  	  	    if (volume.getStatus()==Volume.Status.UNUSED)
	  	  	        throw new ConfigurationException("failed to lookup new volumes {id='"+id+"'}",logger);
	  	  	    if (Compare.equalsIgnoreCase(volume.getID(), id))
	  	  	    	return volume;
	  	  }
 		  throw new ConfigurationException("failed to lookup volumes {id='"+id+"'}",logger);
 	   }
 	   
 	  public Volume getLegacyVolume(String uniqueId) throws ConfigurationException {
	  	  if (uniqueId==null)
	  	      throw new ConfigurationException("uniqueid cannot be null",logger);
	  	  
	  	  	Date date = null;
		  	try {
	  	  	    date = DateUtil.convertStringToDate(uniqueId);
	  	  	} catch (Exception e) {
	  	  	    logger.warn("failed to parse uniqueid {id='"+uniqueId+"'}");
	  	  	}

	  	  	for (Volume volume : volumes) {
	  	  	    if (volume.getStatus()==Volume.Status.UNUSED)
	  	  	        throw new ConfigurationException("failed to lookup legacy volumes {uniqueId='"+uniqueId+"'}",logger);
	  	  	    if (volume != null && volume.getModified()!=null && volume.getModified().compareTo(date)>=0) {
	  	  	    	boolean isModified = volume.getModified().compareTo(date)>0;
	  	  	    	return volume;
	  	  	    	
	  	  	    }
	  	  	}
	  	  	return null;
 	   }
 	 
 	   public void closeVolume(int index) throws ConfigurationException {
 	       Object o = volumes.get(index);
 	       if (o==null)
 	           throw new ConfigurationException("failed to close volume. no such volume exists",logger);
	   	    Volume vs = (Volume)o;
	   	    vs.setStatus(Volume.Status.CLOSED);
	   	    saveVolumeInfo(vs,true);
	   	    Collections.sort(volumes);
	   	    logger.debug("volume is now closed {"+vs+"}");
 	   }
 	   
 	  public void unmountVolume(int index) throws ConfigurationException {
	       Object o = volumes.get(index);
	       if (o==null)
	           throw new ConfigurationException("failed to unmount volume. no such volume exists",logger);
	   	    Volume vs = (Volume)o;
	   	    vs.setStatus(Volume.Status.UNMOUNTED);
	   	    saveVolumeInfo(vs,true);
	   	    Collections.sort(volumes);
	   	    logger.debug("volume is now unmounted {"+vs+"}");
	   }
 	  

 	 public Volume  getActiveVolume()  {
 		for (Volume volume : volumes) {
		      if (volume.getStatus()==Volume.Status.ACTIVE) {
		          logger.debug("getActiveVolume() {"+volume+"}");
		    	  return volume;
		      }
		}
		return null;
	  }
 	 
 	   public synchronized void readyActiveVolume() {
		    try {
		    	 Volume vol = getActiveVolume();
		 	     if (vol!=null) {
		 	    	 if (vol.enoughDiskSpace()) {
		 	    		logger.debug("volume has enough disk space {"+vol+"}");
		 	    		return;
		 	    	 } else {
		 	    		
		 	    		 logger.info("closing volume. volume has run out of disk space. {"+vol+"}");
		 	    		vol.setStatus(Volume.Status.CLOSED);
		 	    		saveVolumeInfo(vol,true);
		 	    	 }
		 	     }
	 	 
		 	     for (Volume volume : volumes) {
			          if (volume.getStatus()==Volume.Status.UNUSED ) {
			              volume.setDiskSpace();
			              if (volume.enoughDiskSpace()) {
		                      volume.setStatus(Volume.Status.ACTIVE); // make the new one active
		                      Collections.sort(volumes);
		                      break;
			              }
			          }
			      }
	 	      } catch (Exception e) {
	 	          logger.debug("an error occurred while switching volumes",e);
	 	      }
 	   }

    
 	   public synchronized void startDiskSpaceCheck() {
 	      if (diskSpaceCheckWait>0) {
    		 diskInfoWorker = new DiskInfoWorker();
    		
 	       }
 	       diskIrChecker = new DiskIRChecker();
 		   if (shutdown) {
 	          shutdown = false;
 	          if (diskSpaceCheckWait>0)
 	        	  diskInfoWorker.start();
 	          diskIrChecker.start();
 	      }
 	   }
 	   
	 	 protected void stopDiskSpaceCheck()  {
	 	   
	 	    shutdown = true;
	 	    if (diskInfoWorker!=null)
	 	    	diskInfoWorker.interrupt();
	 	    if (diskIrChecker!=null)
	 	    	diskIrChecker.interrupt();
	 	 }
	 	 
	 	 protected void finalize() throws Throwable {
	 		 super.finalize();
	 		 stopDiskSpaceCheck();
	 	 }

 	   // disk insert removal checker
 	  protected class DiskIRChecker extends Thread implements Serializable{
 		  
 		  
 		  /**
		 * 
		 */
		private static final long serialVersionUID = -5888336233797871738L;

		public DiskIRChecker() {
 			  setDaemon(true);
 		  }
 		  
 		  public void run() { 
 			 
 			  setName("DiskInsertRemovalChecker");
 			  while (!shutdown) {
 				  Iterator i = volumes.iterator();
 			      while (i.hasNext()) {
 			          Object o = i.next();
 			          if (o!=null) {
 			              Volume v = (Volume)o;
 			             File file = new File(v.getPath()+File.separator+INFO_FILE);
 			             // something has changed
 			              if (file.exists() && v.getStatus()==Volume.Status.EJECTED ||
 			            	  !file.exists() && v.getStatus()!=Volume.Status.EJECTED) {
 			            		 try {
 			            			 loadVolumeInfo(v);
 			            		 } catch (ConfigurationException ce) {
 			            			 logger.warn("failed to load volume info {"+v+"}");
 			            		 }
 			              }
 			          }
 			      }
 			     try { Thread.sleep(10000); } catch(Exception e) { shutdown = true; } 				  
 			  } 
 		  }
 	  }
 	  
 	  
 	  private class DiskInfoWorker extends Thread implements Serializable{
 		 
 		 /**
		 * 
		 */
		private static final long serialVersionUID = -2048553069551273094L;

		public DiskInfoWorker() {
			  setDaemon(true);
		  }
 		 
 	      public void run() {
 	    	 setName("DiskSpaceChecker");
 	          while (!shutdown) {
 	              Volume activeVolume = getActiveVolume();
                  try {
     	              if (activeVolume!=null) {
     	            	  activeVolume.setDiskSpace();
     	                  activeVolume.enoughDiskSpace(); // warning
     	              }
     	              	  
                  } catch (Exception e) {
                      logger.error("failed to retrieve disk space {"+activeVolume+"}",e);
                  }
 	             try { Thread.sleep(getDiskSpaceCheckWait()*1000); } catch(Exception e) { shutdown = true; }
 	          }
 	          
 	      }
 	 	}


		

}
