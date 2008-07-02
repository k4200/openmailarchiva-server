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

import org.apache.log4j.*;

import java.io.*;
import java.util.*;
import com.stimulus.archiva.exception.*;
import com.stimulus.util.*;
import com.stimulus.archiva.monitor.*;

public class Volumes  implements Serializable, Props {

	
	private static final long serialVersionUID = -1331431169000378587L;
	
    public enum Priority { PRIORITY_HIGHER,PRIORITY_LOWER };
    protected static final Logger logger 				= Logger.getLogger(Volumes.class);
    protected int 				diskSpaceCheckWait;
    protected int 				diskSpaceWarn;
    protected int 				diskSpaceThreshold;
    protected boolean 			diskSpaceChecking;
    protected LinkedList<Volume> volumes              	= new LinkedList<Volume>();
    protected boolean  		 	shutdown 	            = true;

    public static final String INFO_FILE = "volumeinfo";
    
    protected static final String diskSpaceCheckWaitKey 	= "volume.diskspace.wait";
    protected static final String diskSpaceWarnKey  	  	= "volume.diskspace.warning";
    protected static final String diskSpaceThresholdKey 	= "volume.diskspace.limit"; // mb
    protected static final String diskSpaceCheckKey			= "volume.diskspace.check"; // mb
    protected static final String defaultDiskSpaceCheckWait	= "3600"; // seconds
    protected static final String defaultDiskSpaceWarn  	  	= "50"; // megabytes
    protected static final String defaultDiskSpaceThreshold 	= "10"; // megabytes
    protected static final String defaultDiskSpaceCheck		= "yes";
    protected static final String defaultMessageStorePath = Character.toString(File.separatorChar)+"store";
    protected static final String defaultSearchIndexPath = Character.toString(File.separatorChar)+"index";
    protected static final int defaultVolumeMaxSize = 30000;
    static int count = 0;
    protected int id = 0;
   
    public Volumes() {
    	id = count;
    	count++;
    	
    }
    
      public Volumes(boolean diskSpaceChecking, int diskSpaceCheckWait, int diskSpaceWarn, int diskSpaceThreshold) {
    	  id = count;
      	count++;
    	  this.diskSpaceCheckWait = diskSpaceCheckWait;
    	  this.diskSpaceWarn = diskSpaceWarn;
    	  this.diskSpaceThreshold = diskSpaceThreshold;
    	  this.diskSpaceChecking = diskSpaceChecking;
      }
      
      
      public int getDefaultVolumeMaxSize() {
          return defaultVolumeMaxSize;
      }
      
      public void setDiskSpaceChecking(boolean diskSpaceChecking) { 
    	  this.diskSpaceChecking = diskSpaceChecking;
      }
      
      public void setDiskSpaceWarn(int diskSpaceWarn) {
    	  this.diskSpaceWarn = diskSpaceWarn;
      }
      
      public long getDiskSpaceWarnBytes() {
    	  return this.diskSpaceWarn * 1024 * 1024;
      }
      
      public void setDiskSpaceThreshold(int diskSpaceThresholdBytes) {
    	  this.diskSpaceThreshold = diskSpaceThresholdBytes;
      }
      
      public boolean getDiskSpaceChecking() {
    	  return diskSpaceChecking;
      }
      
      public int getDiskSpaceCheckWait() {
    	  return diskSpaceCheckWait;
      }
      
      public void setDiskSpaceCheckWait(int diskSpaceCheckWait) {
    	  this.diskSpaceCheckWait = diskSpaceCheckWait;
      }
      
      public int getDiskSpaceWarn() {
    	return diskSpaceWarn;
      }
      
      public int getDiskSpaceThreshold() {
    	return diskSpaceThreshold;
      }
      
      public long getDiskSpaceThresholdBytes() {
      	return diskSpaceThreshold * 1024 * 1024;
        }
      
 	  public void addVolume(String path, String indexPath,  int maxSize, boolean allowRemoteSearch) throws ConfigurationException {	
 		  Volume mv = new Volume(path,indexPath, maxSize, allowRemoteSearch);
 	      	addVolume(mv);
 	  }
 	  
 	 public void addVolume(Volume volume) {
 			 volumes.add(volume);
 			 Collections.sort(volumes);
 	 }

	  public void saveAllVolumeInfo(boolean overwrite) throws ConfigurationException {
		  logger.debug("saveAllVolumeInfo()");
		  for (Volume v : volumes) {
			  v.save();
		  }
	  }
	  
	  public void loadAllVolumeInfo() throws ConfigurationException {
		  logger.debug("loadAllVolumeInfo()");
		  for (Volume v : volumes) {
			  v.load();
		  } 
		
	  }

	  public void touchActiveVolume(Email email) throws ConfigurationException {
			  Volume v = getActiveVolume();  
			  if (v==null)
				  throw new ConfigurationException("failed to touch active volume. no volume is active",logger);
			  v.touchModified(email);
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
	  	  	        throw new ConfigurationException("failed to lookup new volumes {id='"+id+"'}",logger,Level.DEBUG);
	  	  	    if (Compare.equalsIgnoreCase(volume.getID(), id))
	  	  	    	return volume;
	  	  }
 		  // backup plan - try search using modified date
 		 for (Volume volume : volumes) {
 			 if (Compare.equalsIgnoreCase(DateUtil.convertDatetoString(volume.getLatestArchived()), id))
	  	  	    	return volume;
 		 }
 		  throw new ConfigurationException("failed to lookup a volume from index information {id='"+id+"'}",logger,Level.DEBUG);
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
	  	  	        throw new ConfigurationException("failed to lookup legacy volumes {uniqueId='"+uniqueId+"'}",logger,Level.DEBUG);
	  	  	    if (volume != null && volume.getEarliestArchived()!=null && volume.getEarliestArchived().compareTo(date)>=0) {
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
		   	    vs.save();
		   	    Collections.sort(volumes);
		   	    logger.debug("volume is now closed {"+vs+"}");
 	   }
 	   
 	  public void unmountVolume(int index) throws ConfigurationException {
		       Object o = volumes.get(index);
		       if (o==null)
		           throw new ConfigurationException("failed to unmount volume. no such volume exists",logger);
		   	    Volume vs = (Volume)o;
		   	    vs.setStatus(Volume.Status.UNMOUNTED);
		   	    vs.save();
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
 	 
 	   public void readyActiveVolume() {
		    try {
		    	 Volume activeVolume = null;
		    	 Volume vol = getActiveVolume();
		 	     if (vol!=null) {
		 	    	 if (vol.enoughDiskSpace()) {
		 	    		logger.debug("volume has enough disk space {"+vol+"}");
		 	    		return;
		 	    	 } else {
		 	    		 logger.info("closing volume. volume has run out of disk space. {"+vol+"}");
		 	    		 Event.notifyEvent("volume "+vol.getPath()+" closed. run out of disk space.",Event.Category.VOLSTATUS);
		 	    		 vol.setStatus(Volume.Status.CLOSED);
		 	    		 vol.save();
		 	    	 }
		 	     }
		 	    
		 	     for (Volume volume : volumes) {
		 	    	  if (volume==null) {
		 	    		  logger.debug("null volume found during iteration. this should not occur.");
		 	    		  continue;
		 	    	  }
			          if (volume.getStatus()==Volume.Status.UNUSED ) {
			              	  volume.setStatus(Volume.Status.ACTIVE); // make the new one active
		                      Event.notifyEvent("volume "+volume.getPath()+" is activated.",Event.Category.VOLSTATUS);
		                      volume.save();
		                      Collections.sort(volumes);
		                      activeVolume = volume;
		                      break;
			          }
			      }
		 	      if (activeVolume==null) {
		 	    	 Event.notifyEvent("no volumes are available for usage.",Event.Category.VOLSTATUS);
		 	      } 
	 	      } catch (Exception e) {
	 	          logger.debug("an error occurred while switching volumes",e);
	 	      }
	 	      
 	   }

	 	 
	 @Override
	protected void finalize() throws Throwable {
		 super.finalize();
	 }

	 public void saveSettings(String prefix, Settings prop, String suffix) {
		 logger.debug("saving volumes");
		
	  	int c = 1;
	  	for (Volume volume : volumes) {
	  		volume.saveSettings(null, prop,"."+c++);
	  	}
	  	
	  	prop.setProperty(diskSpaceCheckWaitKey, Integer.toString(getDiskSpaceCheckWait()));
	  	prop.setProperty(diskSpaceWarnKey,Integer.toString(getDiskSpaceWarn()));
	  	prop.setProperty(diskSpaceThresholdKey,Integer.toString(getDiskSpaceThreshold()));
	  	prop.setProperty(diskSpaceCheckKey,ConfigUtil.getYesNo(getDiskSpaceChecking()));

	 }

 	 
 	 public boolean loadSettings(String prefix, Settings prop, String suffix) {
 		logger.debug("loading volumes");
 			clearAllVolumes();
	 		int c = 1;
	 	  	boolean load = false;
	 	  	do {
	 	  		Volume vol = new Volume();
	 	  		load = vol.loadSettings(null, prop,"."+c++);
	 	  		if (load) {
	 	  			addVolume(vol);
	 	  		}
	 	  	} while (load);
	 	  	
	 	    int diskSpaceCheckWait 	= ConfigUtil.getInteger(prop.getProperty(diskSpaceCheckWaitKey),defaultDiskSpaceCheckWait);
		    int diskSpaceWarn 		= ConfigUtil.getInteger(prop.getProperty(diskSpaceWarnKey),defaultDiskSpaceWarn);
		    int diskSpaceThreshold 	= ConfigUtil. getInteger(prop.getProperty(diskSpaceThresholdKey),defaultDiskSpaceThreshold);
		    boolean diskSpaceCheck 	= ConfigUtil.getBoolean(prop.getProperty(diskSpaceCheckKey),defaultDiskSpaceCheck);
		    
		    setDiskSpaceChecking(diskSpaceCheck);
		    setDiskSpaceThreshold(diskSpaceThreshold);
		    setDiskSpaceWarn(diskSpaceWarn);
		    setDiskSpaceCheckWait(diskSpaceCheckWait);
		   
 	  	return true;
	 }
 
 	
 
}
