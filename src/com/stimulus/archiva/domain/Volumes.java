
package com.stimulus.archiva.domain;

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


import org.apache.commons.logging.*;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.stimulus.archiva.exception.*;
import com.stimulus.util.*;
import com.stimulus.archiva.monitor.*;

public class Volumes  implements Serializable, Props, Cloneable {

	
	private static final long serialVersionUID = -1331431169000378587L;
	
    public enum Priority { PRIORITY_HIGHER, PRIORITY_LOWER };
    public enum AutoCreateEvent { WHENFULL, MONTHLY, QUARTERLY, YEARLY}; 
    
    protected static final Log logger 					= LogFactory.getLog(Volumes.class);
    protected int 				diskSpaceCheckWait 		= 24;
    protected int 				diskSpaceWarn 			= 50;
    protected int 				diskSpaceThreshold  	= 0;
    protected boolean 			diskSpaceChecking 		= true;
    protected boolean			autoCreate				= false;
    protected AutoCreateEvent	autoCreateEvent 		= AutoCreateEvent.WHENFULL;
    protected LinkedList<Volume> volumes = new LinkedList<Volume>();

    public static final String INFO_FILE = "volumeinfo";
   
    protected static final String autoCreateKey 			= "volume.autocreate";
    protected static final String autoCreateEventKey		= "volume.autocreate.event";
    protected static final String diskSpaceCheckWaitKey 	= "volume.diskspace.interval";
    protected static final String diskSpaceWarnKey  	  	= "volume.diskspace.warning";
    protected static final String diskSpaceThresholdKey 	= "volume.diskspace.limit"; // mb
    protected static final String diskSpaceCheckKey			= "volume.diskspace.check"; // mb
    protected static final String defaultDiskSpaceCheckWait	= "24"; // hours
    protected static final String defaultDiskSpaceWarn  	  	= "50"; // megabytes
    protected static final String defaultDiskSpaceThreshold 	= "0"; // megabytes
    protected static final String defaultDiskSpaceCheck		= "yes";
    protected static final String defaultMessageStorePath = Character.toString(File.separatorChar)+"store";
    protected static final String defaultSearchIndexPath = Character.toString(File.separatorChar)+"index";
    protected static final int defaultVolumeMaxSize = 30000;
    protected static final String defaultAutoCreate = "no";
    protected static final String defaultAutoCreateEvent = "whenfull";
    
    public Volumes() {
    }
    
      public Volumes(boolean diskSpaceChecking, int diskSpaceCheckWait, int diskSpaceWarn, int diskSpaceThreshold) {
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
      
      public AutoCreateEvent getAutoCreateEvent() {
    	  return autoCreateEvent;
      }
      
      public void setAutoCreateEvent(AutoCreateEvent autoCreateEvent) {
    	  this.autoCreateEvent = autoCreateEvent;
      }
      
      public boolean getAutoCreate() {
    	  return autoCreate;
      }
      
      public void setAutoCreate(boolean autoCreate) {
    	  this.autoCreate = autoCreate;
      }
      
 	  public Volume addVolume(String path, String indexPath,  long maxSize, boolean allowRemoteSearch) throws ConfigurationException {	
 		  Volume mv = new Volume(path,indexPath, maxSize, allowRemoteSearch);
 	      	addVolume(mv);
 	      	return mv;
 	  }
 	  
 	  public synchronized Volume newVolume() throws ConfigurationException {
 		  
 		 String storePath;
 		 String indexPath;
 		 
 		boolean windows =(System.getProperty("os.name").toLowerCase(Locale.ENGLISH).indexOf("windows") != -1);
		if (windows) {
			storePath = File.separator + "store" + File.separator + "store0";
			indexPath = File.separator + "index" + File.separator + "store0";
		} else {
			storePath = File.separator + "var" + File.separator +  "store" + File.separator + "store0";
			indexPath = File.separator + "var" + File.separator + "index" + File.separator + "index0";
		}
			
 		long volumeSize = getDefaultVolumeMaxSize();
 		 
 		
 		 int maxno=0;
 		 Pattern pattern = Pattern.compile("(.*[a-zA-z])([0-9]+)");
 		 for (Volume volume : volumes) {
 			 String sp = volume.getPath();
 			 String ip = volume.getIndexPath();
 			 Matcher matcher = pattern.matcher(sp);
 			 Matcher matcher2 = pattern.matcher(ip);
 			 if (matcher.find() && matcher2.find()) {
 				 int digit = Integer.parseInt(matcher.group(2));
 				 if (digit>=maxno) {
 					 digit++;
 					 storePath = matcher.group(1) + (digit);
 					 indexPath = matcher2.group(1) + (digit);
 					 volumeSize = volume.getMaxSize();
 					 maxno = digit;
 					 
 				 }
 			 }
 		 }
 		
 		 return addVolume(storePath,indexPath,volumeSize,false);
 	  }
 	  
 	 public synchronized void addVolume(Volume volume) {
 			 volumes.add(volume);
 			 Collections.sort(volumes);
 	 }

	  public synchronized void saveAllVolumeInfo(boolean overwrite) throws ConfigurationException {
		  Config.getConfig().getVolumeIRService().block();
		  logger.debug("saveAllVolumeInfo()");
		  for (Volume v : volumes) {
			  v.save();
		  }
		  Config.getConfig().getVolumeIRService().unblock();
	  }
	  
	  public synchronized void loadAllVolumeInfo() throws ConfigurationException {
		  Config.getConfig().getVolumeIRService().block();
		  logger.debug("loadAllVolumeInfo()");
		  for (Volume v : volumes) {
			  v.load();
		  } 
		  Config.getConfig().getVolumeIRService().unblock();
	  }

	  public synchronized void readActiveVolumeStatus(Email email) throws ConfigurationException {
			  Volume v = getVolume(Volume.Status.ACTIVE);  
			  if (v==null)
				  throw new ConfigurationException("failed to touch active volume. no volume is active.",logger);
	  }


 	  public synchronized Volume removeVolume(int id) throws ConfigurationException {
	 	     Volume v = volumes.get(id);
	 	     if (v.getStatus()==Volume.Status.ACTIVE)
	 	         throw new ConfigurationException("failed to delete active volume. it must be closed first",logger);
		  	 volumes.remove(v);
		  	Collections.sort(volumes);
		  	return v;
 	  }

 	  public synchronized int indexOfVolume(Volume v) {
 		  	return volumes.indexOf(v);
 	  }

 	  public synchronized List<Volume> getVolumes() {
 		    return (List<Volume>)volumes.clone(); // want to avoid synchronization issues
 	  }

 	  public synchronized Volume getVolume(int index) {
 	  	return volumes.get(index);
 	  }

 	  public synchronized void setVolumePriority(int id, Priority priority)  {
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

 	   public synchronized void clearAllVolumes() {
 	  	 volumes.clear();
 	   }

 	   
 	   public synchronized Volume getNewVolume(String id) throws ConfigurationException {
 		  for (Volume volume : volumes) {
	  	  	    if (volume.getStatus()==Volume.Status.UNUSED)
	  	  	        throw new ConfigurationException("failed to lookup new volumes {id='"+id+"'}",logger,ChainedException.Level.DEBUG);
	  	  	    if (Compare.equalsIgnoreCase(volume.getID(), id))
	  	  	    	return volume;
	  	  }
 		  // backup plan - try search using modified date
 		 for (Volume volume : volumes) {
 			 if (Compare.equalsIgnoreCase(DateUtil.convertDatetoString(volume.getClosedDate()), id))
	  	  	    	return volume;
 		 }
 		  throw new ConfigurationException("failed to lookup a volume from index information {id='"+id+"'}",logger,ChainedException.Level.DEBUG);
 	   }
 	   
 	  public synchronized Volume getLegacyVolume(String uniqueId) throws ConfigurationException {
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
	  	  	        throw new ConfigurationException("failed to lookup legacy volumes {uniqueId='"+uniqueId+"'}",logger,ChainedException.Level.DEBUG);
	  	  	    if (volume != null && volume.getCreatedDate()!=null && volume.getClosedDate().compareTo(date)>=0) {
	  	  	    	return volume;
	  	  	    	
	  	  	    }
	  	  	}
	  	  	return null;
 	   }
 	  
 	  public synchronized Volume closeVolume(Volume volume) throws ConfigurationException {
 		  		volume.setStatus(Volume.Status.CLOSED);
				volume.setClosedDate(new Date());
 		  		logger.debug("volume is now closed {"+volume+"}");
 		  		return volume;
 	  }
 	  
 	   public synchronized void closeVolume(int index) throws ConfigurationException {
	 	       Volume volume = volumes.get(index);
	 	       if (volume==null)
	 	           throw new ConfigurationException("failed to close volume. no such volume exists",logger);
	 	      closeVolume(volume);
 	   }
 	 
 	   public synchronized Volume activateVolume(Volume volume) throws ConfigurationException {
 		  volume.readyFileSystem(); 
 		  volume.setStatus(Volume.Status.ACTIVE); // make the new one active
 		  volume.setCreatedDate(new Date());
		  Collections.sort(volumes);
          volume.save();
          Config.getConfig().saveConfiguration(MailArchivaPrincipal.SYSTEM_PRINCIPAL);
          return volume;
 	   }
 	   
 	  public synchronized void unmountVolume(int index) throws ConfigurationException {
		       Object o = volumes.get(index);
		       if (o==null)
		           throw new ConfigurationException("failed to unmount volume. no such volume exists",logger);
		   	    	Volume vs = (Volume)o;
			   	    vs.setStatus(Volume.Status.UNMOUNTED);
			   	    Collections.sort(volumes);
			   	    vs.save();
			   	    logger.debug("volume is now unmounted {"+vs+"}");
	   }
 	  

 	 public synchronized Volume getVolume(Volume.Status status)  {
 		for (Volume volume : volumes) {
 			if (volume.getStatus()==status) {
		          //logger.debug("getActiveVolume() {"+volume+"}");
		    	  return volume;
		      }
		}
 		return null;
 	 }

		public synchronized void activateUnusedVolume() {
	 	     Volume unusedVolume =  getVolume(Volume.Status.UNUSED);
	 	     if (unusedVolume!=null) {
	 	    	  try {
	 	    		  activateVolume(unusedVolume);
	 	    	  } catch (ConfigurationException e) {
	 	    		  logger.error("failed to active volume:"+e.getMessage()+" {"+unusedVolume+"}",e);
	 	    	  }
		     } 
		}
		
 	   public synchronized void readyActiveVolume() {
 		   		try {
 		   			spaceCheck();
 		   		} catch (ConfigurationException ce) {
 		   			logger.error("error occurred during space check procedure:"+ce.getMessage(),ce);
 		   		}
 		   	    Volume activeVolume = getVolume(Volume.Status.ACTIVE);
 		   	    if (activeVolume==null) {
 		   	    	activateUnusedVolume();
 		   	    }
 	   }
 	   
 	   public synchronized void spaceCheck() throws ConfigurationException {
 		  Volume activeVolume = getVolume(Volume.Status.ACTIVE);
 	      if (activeVolume!=null) {
 	    	  if (activeVolume.enoughDiskSpace()) {
 	    		//logger.debug("volume has enough disk space {"+activeVolume+"}");
 	    	  } else {
 	    		 logger.info("closing volume. volume has run out of disk space. {"+activeVolume+"}");
 	    		 closeVolume(activeVolume);	
	 	      }
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
		prop.setProperty(autoCreateKey,ConfigUtil.getYesNo(getAutoCreate()));
		prop.setProperty(autoCreateEventKey,getAutoCreateEvent().toString().toLowerCase(Locale.ENGLISH));

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
		
		    String newAutoCreateEvent = AutoCreateEvent.WHENFULL.toString().toLowerCase(Locale.ENGLISH);
		    try {
		    	newAutoCreateEvent = prop.getProperty(autoCreateEventKey);
		    	 if (newAutoCreateEvent==null) {
		   	  			logger.info("config load: new auto create was not found in server.conf. defaulting to whenfull.");
		   	  			autoCreateEvent = AutoCreateEvent.WHENFULL;
		   	  		 } else
		   	  			autoCreateEvent = AutoCreateEvent.valueOf(newAutoCreateEvent.trim().toUpperCase(Locale.ENGLISH));
		    	
		    } catch (IllegalArgumentException iae) {
		    	logger.error("failed to set auto create event field. auto create event is set to an illegal value {field='"+newAutoCreateEvent+"'}");
   	    		logger.info("auto create event is set to 'to' by default (error recovery)");
		    }
		    
		    boolean autoCreate 	= ConfigUtil.getBoolean(prop.getProperty(autoCreateKey),defaultAutoCreate);
		    setAutoCreate(autoCreate);
 	  	return true;
	 }
 	 
 	 public void out(String heading) {
 		 for (Volume v : volumes) {
 			 System.out.println(v.toString());
 		 }
 	 }
 	 
 	 public Volumes clone() {
 		Volumes volumes = new Volumes();
 		volumes.setDiskSpaceCheckWait(getDiskSpaceCheckWait());
 		volumes.setDiskSpaceWarn(getDiskSpaceWarn());
 		volumes.setDiskSpaceThreshold(getDiskSpaceThreshold());
 		volumes.setDiskSpaceChecking(getDiskSpaceChecking());
 		volumes.setAutoCreate(getAutoCreate());
 		volumes.setAutoCreateEvent(getAutoCreateEvent());
 		 for (Volume sourceVolume : getVolumes()) {
			 volumes.addVolume(sourceVolume.clone());
 		 }
 		 return volumes;
 	 }
 
 
}
