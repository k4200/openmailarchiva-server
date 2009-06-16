
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

import com.stimulus.util.*;
import java.io.*;
import java.util.*;
import org.apache.commons.logging.*;
import com.stimulus.archiva.exception.ArchivaException;
import com.stimulus.archiva.exception.ConfigurationException;
import com.stimulus.archiva.monitor.Event;
import java.nio.channels.*;
import java.util.concurrent.*;
public class Volume implements Comparable<Volume>,Serializable,Props, Cloneable
 {

	  private static final long serialVersionUID = 5447936271470342602L;
	  
      protected static Log logger = LogFactory.getLog(Volume.class.getName());
      // EJECTED status is now deprecated, we now use ejected variable instead
      public enum Status { CLOSED,ACTIVE,UNUSED,NEW,UNMOUNTED,EJECTED };
     
      
      protected String 	path;
 	  protected String 	indexPath;
 	  protected long 	maxSize = 307200;
      protected Status	status = Status.NEW;
	  protected Date	closedDate = null;
	  protected Date    createdDate = null;
      protected boolean allowRemoteSearch = false;
      protected boolean checkClosedVolume = false;
      
      protected String  id;
      protected String  version;
      protected static final String INFO_FILE = "volumeinfo";
      protected static final String volumePathKey 			= "volume.store.path";
      protected static final String volumeNameKey 			= "volume.name";
      protected static final String volumeIndexPathKey 		= "volume.index.path";
      protected static final String volumeMaxSizeKey 		= "volume.max.size";
      protected static final String volumeRemoteSearchKey 	= "volume.remote.search";
      protected static final String volumeEjectedKey 		= "volume.ejected";
      protected static final String volumeCheckClosedKey    = "volume.check.closed";
      
      protected static final String defaultVolumePath = Character.toString(File.separatorChar);
      protected static final String defaultVolumeIndexPath = File.separatorChar + "index";
      protected static final int defaultVolumeMaxSize = 307200;
      protected static final String defaultVolumeRemoteSearch = "no";
      protected static final String defaultCheckClosed = "no";
      protected Object diskSpaceLock = new Object();
      protected Object volumeinfoLock = new Object();
      protected boolean currentlyCheckingDiskSpace = false;
      
      public static ConcurrentHashMap<String,Long> usedSpaceCache = new ConcurrentHashMap<String,Long>();
      
      
   
      //protected SignatureManifest manifest = new SignatureManifest(this);
      
      public Volume() {
      }
      
	  public Volume(String path, String indexPath, long maxSize, boolean allowRemoteSearch) throws ConfigurationException {
	  	setIndexPath(indexPath);
	  	setPath(path);
	  	setMaxSize(maxSize);
	  	setAllowRemoteSearch(allowRemoteSearch);
	  }

	  public String getID() {
		if (id==null) {
			try { load(); } catch (Exception e) {}
			if (id==null) {
				return UUID.randomUUID().toString();
			}
		} 
		return id;
	  }
	  
	  public void setID(String id) { this.id = id; }
	  public void setMaxSize(long maxSize) { this.maxSize = maxSize; }
	  public long getMaxSize() { return maxSize; }
	  public String getIndexPath() { return indexPath; }
	  
	  public void incUsedSpace(long indexInc,long storeInc) {
		  Volume.usedSpaceCache.put(getIndexPath().toLowerCase(),getUsedIndexSpace()+indexInc);
		  Volume.usedSpaceCache.put(getPath().toLowerCase(),getUsedArchiveSpace()+storeInc);
	  }
	  
	  
	  public boolean isDiskSpaceChecked() {
		  return Volume.usedSpaceCache.get(getPath().toLowerCase())!=null;
	  }
	  
	     
	  public void setIndexPath(String indexPath) { 
		  if (indexPath.length()>1 && indexPath.lastIndexOf(File.separator)==indexPath.length()-1)
		  	indexPath = indexPath.substring(0,indexPath.length()-1);
		   this.indexPath = indexPath.toLowerCase(Locale.ENGLISH); 
		  
	  }
	  public String getPath() { return path; }
	  
	  public void setPath(String path) { 
		  if (path.length()>1 && path.lastIndexOf(File.separator)==path.length()-1)
		  	path = path.substring(0,path.length()-1);
		  this.path = path.toLowerCase(Locale.ENGLISH);
	  }
	
	  
	  protected long getCachedUsedSpace(String path) {
		  Long cachedUsedSpace = usedSpaceCache.get(path.toLowerCase());
		  if (cachedUsedSpace!=null) {
			  return cachedUsedSpace;
		  } else {
			  return -1;
		  }
	  }
	  
	  public long getFreeIndexSpace() { 
		  long freeIndexSpace =  getAvailableBytes(new File(getIndexPath()),getUsedIndexSpace()); 
		  return freeIndexSpace < 0 ? 0 : freeIndexSpace;	  
	  }
	 
	  public long getFreeArchiveSpace() { 
		  long freeArchiveSpace = getAvailableBytes(new File(getPath()),getUsedArchiveSpace()); 
		  return freeArchiveSpace < 0 ? 0 : freeArchiveSpace;	  
	  }
	 
      public long getUsedIndexSpace() { return getCachedUsedSpace(getIndexPath()); }
      
      public long getUsedArchiveSpace() { return getCachedUsedSpace(getPath()); }
    
      protected long getAvailableBytes(File filePath, long usedBytes) {
    	  long freeSpaceBytes = filePath.getFreeSpace();
    	  long maxSizeBytes = maxSize * 1024 * 1024;
    	  long freeSpace2Bytes = maxSizeBytes - usedBytes;
    	  if (freeSpaceBytes < freeSpace2Bytes)
    		  return freeSpaceBytes;
    	  else
    		  return freeSpace2Bytes;
      }
      
	  public boolean enoughDiskSpace() {
		 
	
			  Volumes volumes = Config.getConfig().getVolumes();
			  if (volumes==null) {
				  logger.error("volumes is null");
				  return true;
			  }
			  
			  if (!Config.getConfig().getVolumes().getDiskSpaceChecking()) {
				  logger.debug("disk space checking is disabled. Check file permissions on volume index and store path {"+toString()+"}");
			  	  return true;
		  	  }
			  
			  File storePath = new File(getPath());
			  File indexPath = new File(getIndexPath());
			  
			  if (!storePath.exists() || !indexPath.exists()) {
				  logger.debug("attempt to calculate disk space on non-existant index or store. return true.");
				  return true;
			  }
			
			  long usedArchiveSpace = getUsedArchiveSpace();
			  long usedIndexSpace = getUsedArchiveSpace();
			  
			  if (!isDiskSpaceChecked()) {
				  logger.debug("disk space has not been checked yet. assume zero bytes are used.");
				  usedArchiveSpace = 0;
				  usedIndexSpace = 0;
			  }
			  
			  long freeArchiveSpace = getAvailableBytes(new File(getPath()),usedArchiveSpace);
			  long freeIndexSpace =  getAvailableBytes(new File(getIndexPath()),usedIndexSpace);
			  
			  //logger.debug("enoughDiskSpace() {usedIndexSpace='"+usedIndexSpace+"',usedArchiveSpace='"+usedArchiveSpace+"',diskSpaceWarnBytes='"+volumes.getDiskSpaceWarnBytes()+"',diskSpaceThresholdBytes='"+volumes.getDiskSpaceThresholdBytes()+"',freeArchiveSpace='"+freeArchiveSpace+"',freeindexSpace='"+freeIndexSpace+"'}");
			  
		     // free index space is nearly depleted
          
		 	 //logger.debug("free index space warn check {freeIndexSpace+DISK_SPACE_WARN='"+(freeIndexSpace-volumes.getDiskSpaceWarnBytes())+"<0'}");
		 	 if ((freeIndexSpace-volumes.getDiskSpaceWarnBytes())<=0) {
		 	     logger.warn("storage space is running low on volume {"+toString()+"}");
		 	    }
	         
		 	 // free index space is depleted
	         
		 	// logger.debug("free index space threshold check {freeIndexSpace-DISK_SPACE_THRESHOLD='"+(freeIndexSpace-volumes.getDiskSpaceThresholdBytes())+"<0'}");
		 	  
		 	  if ((freeIndexSpace-volumes.getDiskSpaceThresholdBytes())<=0) {
		 	     logger.warn("there is no storage space left on volume {"+toString()+"}");
		 	    return false;
		 	  }
	         // free archive space is nearly depleted
	             
	          //logger.debug("free archive space warn check {freeArchiveSpace-DISK_SPACE_WARN='"+(freeArchiveSpace-volumes.getDiskSpaceWarnBytes())+"<0'}");
	          if ((freeArchiveSpace-volumes.getDiskSpaceWarnBytes())<=0) {
	              logger.warn("storage space is running low on volume {"+toString()+"}");
	          }
	          
	          // free archive is depleted
	          
	          //logger.debug("free archive space threshold check {freeArchiveSpace-DISK_SPACE_THRESHOLD='"+(freeArchiveSpace-volumes.getDiskSpaceThresholdBytes())+"<0'}");
		 	  
		 	 if ((freeArchiveSpace-volumes.getDiskSpaceThresholdBytes())<=0) {
		 	    logger.warn("there is no storage space left on volume {"+toString()+"}");
		 	   return false;
		  	 }
		
	 	  return true;

	  }

	  public Status getStatus() { return status; }

	  public void setStatusNoAssertions(Status newStatus){
	     this.status = newStatus;
	  }

	  public void setStatusForced(Status newStatus) {
		  status = newStatus;
	  }
	  
	  public void setStatus(Status newStatus) throws ConfigurationException {
  	 
		  
          if (status==newStatus)
             return;
        logger.debug("setting volume status {newStatus='"+newStatus+"',"+ toString()+"}");
        switch(status) {
	  	    	case CLOSED: 
	  	    		if (newStatus!=Status.UNMOUNTED)
	  	    			throw new ConfigurationException("failed to change volume status. it is closed {newstatus='"+status+"'}",logger);
	  	    			break;
	  	    	case ACTIVE: if (newStatus!=Status.CLOSED)
	  	    	    		   throw new ConfigurationException("failed to change volume status. it can only be closed {newstatus='"+status+"'}",logger);
	  	    				 break;
	  	    	case UNUSED: if (newStatus!=Status.ACTIVE)
	  	    	    		   throw new ConfigurationException("failed to change volume status. it can only be made active {newstatus='"+status+"'}",logger);
	  	    				 break;
	  	    	case NEW: if (newStatus!=Status.UNUSED)
	  	    	    		throw new ConfigurationException("failed to change volume status. it can only be made active {newstatus='"+status+"'}",logger);
	  	    				break;
	  	    	case UNMOUNTED: if (newStatus!=Status.CLOSED)
	  	    		throw new ConfigurationException("failed to change volume status. it can only be closed {newstatus='"+status+"'}",logger);
	  	    		break;
	  	    	default: throw new ConfigurationException("failed to change volume status. internal status is set to invalid value.",logger);
	  	}
       
        status = newStatus;
	  }

	  public Date getCreatedDate() {
	      return createdDate;
	  }

	  public Date getClosedDate() {
	      return closedDate;
	  }

	  public void setClosedDate(Date d) {
	      this.closedDate = d;
	  }
	  
	  public void setCreatedDate(Date d) {
	      this.createdDate = d;
	  }
	  
	  protected void setVersion(String version) {
		  this.version = version;
	  }
	  
	  protected String getVersion() { 
		  return version;
	  }
	  public void setAllowRemoteSearch(boolean allowRemoteSearch) {
		  this.allowRemoteSearch = allowRemoteSearch;
	  }
	  
	  public boolean getAllowRemoteSearch() {
		  return allowRemoteSearch;
	  }

	  
	  public boolean isEjected() { 
		  return !(new File(getVolumeInfoFileName()).exists());
	  }
	
	/*  public SignatureManifest getManifest() {
		  return manifest;
	  }*/
	  
	  public int compareTo(Volume v) throws ClassCastException {
	      int compare = getStatus().compareTo(v.getStatus());
	      if (v.getCreatedDate()==null || getCreatedDate()==null)
	    	  return compare;
	      if (compare==0 && getStatus()==Status.CLOSED) // closed
	          compare = getCreatedDate().compareTo(v.getCreatedDate());
	      return compare;
	  }

	  public String toString() {
	      return "volumepath='"+path+"',indexpath='"+indexPath+"',volumestatus='"+status+"',closedDate='"+closedDate+"',createdDate='"+createdDate+"'";
	  }
	  
	 
	
	   public void calculateSpace()  throws ArchivaException {
		   
		   logger.debug("calculateSpace() {"+toString()+"}");
		   
		   if (!Config.getConfig().getVolumes().getDiskSpaceChecking()) {
			   logger.debug("skipping disk space check. checking disabled. {"+toString()+"}");
		   }
		   
		   if (getStatus() == Status.EJECTED || getStatus() == Status.UNMOUNTED || getStatus() == Status.NEW) {
			   logger.debug("skipping disk space check. volume status unmounted/ejected/new. {"+toString()+"}");
			   return;
		   }
		   if (getStatus() == Status.CLOSED && !checkClosedVolume) {
			   logger.debug("skipping disk space check. checking of closed volume disabled for performance reasons. {"+toString()+"}");
			   return;
		   }
			   
		   if (currentlyCheckingDiskSpace) {
			   logger.debug("skipping disk space check. already checking disk space.");
			   return;
		   }
		   
	       currentlyCheckingDiskSpace = true;
		   long totalUsedIndexSpace = 0;
           totalUsedIndexSpace += getFileOrDirectorySize(new File(getIndexPath()));
           long usedArchiveSpace = getFileOrDirectorySize(new File(getPath()));
		   usedSpaceCache.put(getPath(), usedArchiveSpace);
		   usedSpaceCache.put(getIndexPath(), totalUsedIndexSpace);
           logger.debug("used index disk space {usedIndexSpace='" + getUsedIndexSpace() + "' bytes',"+toString() + "}");
           logger.debug("used store disk space {usedStoreSpace='" + getUsedArchiveSpace() +"' bytes',"+toString() + "}");
           

			if (getStatus() == Status.ACTIVE) 
				enoughDiskSpace(); // warning
			
           currentlyCheckingDiskSpace = false;
	   }
	
	   

	   public static class SizeCounter implements FileFilter
	   {
	       private long total = 0;
	       public SizeCounter(){};
	       public boolean accept(File pathname) {
	           if ( pathname.isFile()) {
	               total+=pathname.length();
	           } else {
	        	   try { Thread.sleep(4); } catch (Exception e) {} 
	               pathname.listFiles(this);
	           }
	           return false;
	       }
	       public long getTotal()
	       {
	           return total;
	       }
	   }
	   
	   private static long getFileOrDirectorySize(File file) {
		   SizeCounter counter = new SizeCounter();
		   file.listFiles(counter);
		   return counter.getTotal();
       }
	   
	   public boolean isVolumeAccessible() {
		  return new File(getPath()+File.separator+INFO_FILE).exists();
	   }
	
		  protected void writeVolumeInfoLines(RandomAccessFile out) {
			  try {
				   logger.debug("writeVolumeInfoLines()");
				   out.setLength(0);
				   out.seek(0);
			       // don't save to ejected volume
			       if (isEjected())
			    	  return;
				    
					// make a new volume unused
					if (getStatus()==Volume.Status.NEW)
			    	  setStatus(Volume.Status.UNUSED);
			    	out.seek(0); //Seek to end of file
			        out.writeBytes("# Archiva "+Config.getConfig().getApplicationVersion()+" Volume Information\n");
			        out.writeBytes("# note: this file is crucial - do not delete it!\n");
			        out.writeBytes("version:3\n");
			        if (getID()!=null || getID().length()>0) {
			        	out.writeBytes("id:"+getID()+"\n");
			        }
			        if (getStatus()!=null) {
			        	out.writeBytes("status:"+getStatus()+"\n");
			        }
			        if (getCreatedDate()!=null)
			        	out.writeBytes("created:"+DateUtil.convertDatetoString(getCreatedDate())+"\n");
			        if (getClosedDate()!=null)
			        	out.writeBytes("closed:"+DateUtil.convertDatetoString(getClosedDate())+"\n");
			        
		      } catch (IOException io) {
		    	  if (getStatus()!=Volume.Status.UNMOUNTED)
		    		  logger.error("failed to write volumeinfo. {"+toString()+"} cause:"+io,io);
		      } catch (ConfigurationException ce) {
		    	  logger.error("failed to set volume status. {"+toString()+"} cause:"+ce,ce);
		      }
		  }
		  
		  protected void readVolumeInfoLines(RandomAccessFile randomAccessFile) {
			  logger.debug("readVolumeInfoLines()");
			  String line;
			  StringTokenizer st;
			  try {
				  randomAccessFile.seek(0);
				  while (( line = randomAccessFile.readLine()) != null) {
					
			               if (line.startsWith("#") || line.length()<1)
			                   continue;
			         	  	try {
			         	  		st = new StringTokenizer(line, ":");
			         	  	} catch (NoSuchElementException nse) {
			         	  		logger.debug("possible volumeinfo corruption. no such element.");
			         	  		continue;
			         	  	}
				       	   String name = st.nextToken();
				       	  if (name.toLowerCase(Locale.ENGLISH).trim().equals("modified"))
				                  setClosedDate(DateUtil.convertStringToDate(st.nextToken().trim()));
				       	  else if (name.toLowerCase(Locale.ENGLISH).trim().equals("latestarchived"))
				                  setClosedDate(DateUtil.convertStringToDate(st.nextToken().trim()));
				       	  else if (name.toLowerCase(Locale.ENGLISH).trim().equals("closed"))
			                  setClosedDate(DateUtil.convertStringToDate(st.nextToken().trim()));
			              else if (name.toLowerCase(Locale.ENGLISH).trim().equals("created"))
			              	   setCreatedDate(DateUtil.convertStringToDate(st.nextToken().trim()));
			              else if (name.toLowerCase(Locale.ENGLISH).trim().equals("earliestarchived"))
			              	   setCreatedDate(DateUtil.convertStringToDate(st.nextToken().trim()));
			              else if (name.toLowerCase(Locale.ENGLISH).trim().equals("version"))
			           	   setVersion(st.nextToken().trim());
			              else if (name.toLowerCase(Locale.ENGLISH).trim().equals("id"))
			              	   setID(st.nextToken().trim());
			              else if (name.toLowerCase(Locale.ENGLISH).trim().equals("status")) {
				           	  Status status = Status.CLOSED; // default
				           	  try {
				           		  status = Status.valueOf(st.nextToken().trim());
				           	  } catch (IllegalArgumentException iae) {
				     	  	    		logger.error("failed to load volume.info: status attribute is set to an illegal value {vol='"+toString()+"'}");
				     	  	    		logger.error("volume will be set closed (due to error)");
				           	  }
				           	 
		                      setStatusNoAssertions(status);
			              };
			              
			           
				  }
		           // we make sure that NEW entries become UNUSED
			        if (getStatus()==Volume.Status.NEW)
			            setStatus(Volume.Status.UNUSED);
			        // make sure that volume closed date is not set, when volume is active
			        if (getStatus()==Volume.Status.ACTIVE &&
			        	getClosedDate()!=null) {
			        	setClosedDate(null);
			        }
			        	
			  } catch (Exception e) {
				  logger.debug("failed to read volumeinfo {"+toString()+"}",e);
			  }
				  
	           
           }
		

		  public long getTotalMessageCount()  {
			  try {
				  return Config.getConfig().getSearch().getTotalMessageCount(this);
			  } catch (Exception e) {
				  return 0;
			  }
		  }
		  
		  public String formatTotalMessageCount() {
			  return FormatUtil.formatCount(getTotalMessageCount());
		  }
		  
		  public String formatDiskSpace(long bytes) {
			  //return new Long(bytes).toString();
			 return FormatUtil.formatSpace(bytes);
		  }
		  
		  // method to eliminate spurious sent dates
		  
		  public boolean isDateValid(Email email) {
			  Date sentDate = null;
			  try {
				  sentDate = email.getSentDate();
			  } catch (Exception e) {} 
			 
			  // there is no sent date
			  if (sentDate==null)
				  return false;
			  
			  // sent date should never be after archive date
			  if (sentDate.after(email.getArchiveDate()))
			      return false;
			  
			
			  Date receivedDate = email.getReceivedDate();
			  if (receivedDate!=null) {
				  // we add 30 minutes to account for minute time differentials
				  Calendar marginCal = Calendar.getInstance();
				  marginCal.setTime(receivedDate);
				  marginCal.add(Calendar.MINUTE, 30);
				  // sent date cannot be after received date
				  if (sentDate.after(marginCal.getTime())) {
					  return false;
					  
				  }
				  /*
				  // if the received date is available, sent date cannot be older than received date - 7 days
				  Calendar oldCal = Calendar.getInstance();
				  String oldOut = oldCal.getTime().toString();
				  oldCal.setTime(receivedDate);
				  
				  oldCal.add(Calendar.DATE, -7);
				  String newOut = oldCal.getTime().toString();
				  System.out.println("receiveDate:"+receivedDate);
				  System.out.println("oldOut:"+oldOut);
				  System.out.println("newOut:"+newOut);
				  System.out.println("sentDate:"+sentDate);
				  Date oldDate = oldCal.getTime();
				  if (sentDate.before(oldDate)) {
					  return false;
				  }*/
				  
			  }
			  return true;
		  }
		  
		
		  protected RandomAccessFile getRandomAccessFile(String attr) throws FileNotFoundException {
			  return new RandomAccessFile(getPath()+File.separator+INFO_FILE, attr);
		  }
		
		   public void saveSettings(String prefix, Settings prop, String suffix) {
			   		logger.debug("saving volume settings");
			   	    prop.setProperty(volumePathKey + suffix,getPath());
	    			prop.setProperty(volumeIndexPathKey + suffix,getIndexPath());
	    			prop.setProperty(volumeMaxSizeKey + suffix,Long.toString(getMaxSize()));
	    			prop.setProperty(volumeCheckClosedKey + suffix,ConfigUtil.getYesNo(checkClosedVolume));
		   }
			 
		   public boolean loadSettings(String prefix, Settings prop, String suffix) {
			    logger.debug("loading volume settings");
		  		String vp = prop.getProperty(volumePathKey + suffix);
		  	    String vip = prop.getProperty(volumeIndexPathKey + suffix);
		  	    String vms = prop.getProperty(volumeMaxSizeKey + suffix);
		  	    String vrs =  prop.getProperty(volumeRemoteSearchKey + suffix);
		  	 
		  	    if (vp == null && vip == null && vms == null && vrs == null)
		  	    	return false;
		  	    
		  	    setPath(ConfigUtil.getString(vp,defaultVolumePath));
		  	    setIndexPath(ConfigUtil.getString(vip,defaultVolumeIndexPath));
		  	    setMaxSize(ConfigUtil.getInteger(vms,Integer.toString(defaultVolumeMaxSize)));
		  	   
		  	    String cc = prop.getProperty(volumeCheckClosedKey + suffix);
		  	    if (cc!=null) {
		  	    	checkClosedVolume = ConfigUtil.getBoolean(cc,defaultCheckClosed);
		  	    }
		  	    return true;
		   }
		   

		  public boolean load() throws ConfigurationException {	 
			  
			  if (!new File(getVolumeInfoFileName()).exists()) {
				  setStatusNoAssertions(Status.EJECTED);
				  return false;
			  }
			  
			  synchronized(volumeinfoLock) {
				  logger.debug("load() volumeinfo");
				  RandomAccessFile randomAccessFile = null;
				  try {
					  randomAccessFile = getRandomAccessFile("r");
				  } catch (FileNotFoundException fnfe) {
					  logger.debug("failed open volumeinfo file:"+fnfe.getMessage()+" {"+toString()+"}");
					  closeVolInfo(randomAccessFile);
					  return false;
				  }
				  readVolumeInfoLines(randomAccessFile);
				  closeVolInfo(randomAccessFile);
			  }
			  return true;
		  }
		
		  // dummy function for utilities backwards compatibility
		  public void save(boolean overwrite) throws ConfigurationException {
			  save();
		  }

		  
		  protected void closeVolInfo(RandomAccessFile file) {
			  if (file!=null) {
				  try {  
					  file.close(); 
				  } catch (IOException io) {
					  logger.error("failed to close volumeinfo file:"+io.getMessage()+" {"+toString()+"}");
				  }
			  }
		  }
		  
		  public void readyFileSystem() throws ConfigurationException {
			  File indexFile = new File(getIndexPath());
			  File storeFile = new File(getPath());
			  
			  if (!indexFile.exists()) {
				  boolean createIndexDir = indexFile.mkdirs();
				  if (!createIndexDir) {
					  throw new ConfigurationException("unable to create volume index directory:"+getIndexPath(),logger);
				  }
			  }
			  if (!storeFile.exists()) {
				  boolean createStoreDir = storeFile.mkdirs();
				  if (!createStoreDir) {
					 throw new ConfigurationException("unable to create volume store directory:"+getPath(),logger);
				  }
			  }
			 
			  if (!Config.getFileSystem().checkReadWriteDeleteAccess(getIndexPath())) {
				  throw new ConfigurationException("there insufficient read/write/delete permissions on volume index:"+getIndexPath(),logger);
			  }
			  
			  if (!Config.getFileSystem().checkReadWriteDeleteAccess(getPath())) {
				  throw new ConfigurationException("there insufficient read/write/delete permissions on volume store:"+getPath(),logger);
			  }
		  }
		  
		  public void save()  throws ConfigurationException {
			  try {
					  Config.getConfig().getConfigAutoLoadService().block();
					  if (getStatus()!=Status.NEW && isEjected()) return;
					  
					  if (getStatus()==Status.EJECTED)
						  return;
					  
					  if (getStatus()==Status.NEW) {
						  setStatus(Status.UNUSED);
						  readyFileSystem();
						  if (getStatus()==Status.UNUSED) {
							  try {
									 calculateSpace();
								 } catch (Exception e) {
						            logger.error("failed to retrieve disk space {"+toString()+"}",e);
						         } 
						  }				 
					  }
				  
					  synchronized(volumeinfoLock) {
						  logger.debug("save() volumeinfo");
						  RandomAccessFile randomAccessFile = null;
						  try {
							  randomAccessFile = getRandomAccessFile("rw");
						  } catch (FileNotFoundException fnfe) {
							     logger.error("failed to write to volumeinfo:"+fnfe.getMessage(),fnfe);
							     logger.warn("ensure mailarchiva service is running under account with sufficient privileges");
							     closeVolInfo(randomAccessFile);
							  return;
						  }
						  logger.debug("open volumeinfo file for write {file='"+getPath()+File.separator+INFO_FILE+"'");
						  FileChannel channel = randomAccessFile.getChannel();
						 /* FileLock fileLock = null;
						  try {
							  fileLock = channel.lock();
						  } catch(IOException io) {
							  logger.error("failed to obtain lock to volumeinfo file:"+io.getMessage()+" {"+toString()+"}");
							  closeVolInfo(randomAccessFile);
							  return;
						  } catch(OverlappingFileLockException ofle) {
							  logger.error("failed to obtain lock to volumeinfo file:"+ofle.getMessage()+" {"+toString()+"}");
							  closeVolInfo(randomAccessFile);
							  return;
						  }*/
						  writeVolumeInfoLines(randomAccessFile);
						  /*
						  try {
							  fileLock.release();
						  } catch (IOException io) {
							  logger.error("failed to release the write lock on volumeinfo file:"+io.getMessage()+" {"+toString()+"}");
						  }
						 */
						  try {
							  channel.close();
						  } catch (IOException io) {
							  logger.error("failed to close volumeinfo file:"+io.getMessage()+" {"+toString()+"}");
						  }
						  closeVolInfo(randomAccessFile);
					  }
			  } finally {
				  Config.getConfig().getConfigAutoLoadService().unblock();
			  }
		  }
	
		public static class DirFilter implements FilenameFilter {

			 public boolean accept(File dir, String name) {
				 boolean isDir = new File(dir.getAbsolutePath()+File.separator+name).isDirectory();
				 return isDir;
			 }
		 }
		
		public boolean equals(Object obj) {
			if (obj instanceof Volume) {
				Volume v2 = (Volume)obj;
				if (Compare.equalsIgnoreCase(getPath(),v2.getPath()) &&
					Compare.equalsIgnoreCase(getIndexPath(),v2.getIndexPath()))
					return true;
			}
			return false;
		}
		
		 public class DiskCheck extends Thread {
	 		 Volume vol;
	 		 
	 		 public DiskCheck(Volume vol) {
	 			 this.vol = vol;
	 		 }
	 		 public void run() {
	 			 logger.debug("initiating disk space check {"+vol+"}");
	 			 setName("diskspace checker");
	 			 Thread.currentThread().setPriority(Thread.NORM_PRIORITY-1);
				 try {
					 vol.calculateSpace();
				 } catch (Exception e) {
		            logger.error("failed to retrieve disk space {"+vol+"}",e);
		         } 
				 
	 		 }
	 	 }
		 // DO NOT DELETE THIS
		 public int hashCode() {
			 int hashcode = getPath().hashCode() + getIndexPath().hashCode();
			 return hashcode;
		 }
		 
		
		   
		   public Volume clone() {
			   Volume volume = new Volume();
			   volume.setPath(getPath());
			   volume.setIndexPath(getIndexPath());
			   volume.setMaxSize(getMaxSize());
			   volume.setStatusNoAssertions(getStatus());
			   volume.setCreatedDate(getCreatedDate());
			   volume.setClosedDate(getClosedDate());
			   volume.setAllowRemoteSearch(getAllowRemoteSearch());
			   volume.setID(getID());
			   volume.setVersion(getVersion());
			   return volume;
		   }
		   
		
		   public String getVolumeInfoFileName() {
			   return getPath()+File.separator+INFO_FILE;
		   }
		  
  }
