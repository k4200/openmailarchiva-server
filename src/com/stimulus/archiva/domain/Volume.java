
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
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.*;
import java.io.*;
import org.apache.log4j.Logger;
import com.stimulus.archiva.exception.ArchivaException;
import com.stimulus.archiva.exception.ConfigurationException;
import com.stimulus.archiva.monitor.Event;
import java.nio.channels.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

  public class Volume implements Comparable<Volume>,Serializable,Props {

	  private static final long serialVersionUID = 5447936271470342602L;
	  
      protected static Logger logger = Logger.getLogger(Volume.class.getName());
      public enum Status { CLOSED,ACTIVE,UNUSED,NEW,UNMOUNTED,EJECTED,REMOTE };
      protected String 	path;
 	  protected String 	indexPath;
 	  protected long 	maxSize = 3000;
      protected Status	status = Status.NEW;
 	  protected long    freeIndexSpace 	 = Long.MAX_VALUE;
	  protected long    freeArchiveSpace = Long.MAX_VALUE;
	  protected Date	latestArchived = null;
	  protected Date    earliestArchived = null;
	  protected Date    earliestSent = null;
	  protected Date	latestSent = null;
	  protected Date    earliestReceived = null;
	  protected Date	latestReceived = null;
      protected long    usedIndexSpace 	 = -1;
      protected long    usedArchiveSpace = -1;
      public 	boolean diskSpaceChecking = true;
      protected boolean allowRemoteSearch = false;
      protected String  id;
      protected String  version;

      protected static final String INFO_FILE = "volumeinfo";
      protected static final String volumePathKey 			= "volume.store.path";
      protected static final String volumeNameKey 			= "volume.name";
      protected static final String volumeIndexPathKey 		= "volume.index.path";
      protected static final String volumeMaxSizeKey 		= "volume.max.size";
      protected static final String volumeRemoteSearchKey 	= "volume.remote.search";
      
      protected static final String defaultVolumePath = Character.toString(File.separatorChar);
      protected static final String defaultVolumeIndexPath = File.separatorChar + "index";
      protected static final int defaultVolumeMaxSize = 30000;
      protected static final String defaultVolumeRemoteSearch = "no";
      protected Object diskSpaceLock = new Object();
      protected Object volumeinfoLock = new Object();
      protected boolean currentlyCheckingDiskSpace = false;
      protected boolean isDiskSpaceChecked = false;
      
      public Volume() {
    	  
      }
      
	  public Volume(String path, String indexPath,  int maxSize, boolean allowRemoteSearch) throws ConfigurationException {
	  	setIndexPath(indexPath);
	  	setPath(path);
	  	setMaxSize(maxSize);
	  	setAllowRemoteSearch(allowRemoteSearch);
	  	diskSpaceChecking = Config.getConfig().getVolumes().getDiskSpaceChecking();
	  	if (diskSpaceChecking) {
		  	try {
		  		calculateFreeSpace();
		  	} catch (ArchivaException ae) {
		  		diskSpaceChecking = false;
		  	}
	  	}
	  }

	  public String getID() {
		if (id==null) {
			return DateUtil.convertDatetoString(new Date()); // legacy
		} else return id;
	  }
	  
	  public void setID(String id) { this.id = id; }
	 
	  public long getMaxSize() { return maxSize; }
	  public String getIndexPath() { return indexPath; }
	  
	  
	  public void setIndexPath(String indexPath) { 
		  if (indexPath.length()>1 && indexPath.lastIndexOf(File.separator)==indexPath.length()-1)
		  	indexPath = indexPath.substring(0,indexPath.length()-2);
		   this.indexPath = indexPath.toLowerCase(Locale.ENGLISH); 
		  
	  }
	  public String getPath() { return path; }
	  
	  public void setPath(String path) { 
		  if (path.length()>1 && path.lastIndexOf(File.separator)==path.length()-1)
		  	path = path.substring(0,path.length()-2);
		  this.path = path.toLowerCase(Locale.ENGLISH);
	  }
	  public void setMaxSize(long maxSize) { this.maxSize = maxSize; }

	  public long getFreeIndexSpace() { return freeIndexSpace; }
	  public void setFreeIndexSpace(long freeIndexSpace) { this.freeIndexSpace = freeIndexSpace; }
  
	  public long getFreeArchiveSpace() { return freeArchiveSpace; }
	  public void setFreeArchiveSpace(long freeArchiveSpace) { this.freeArchiveSpace = freeArchiveSpace; }
	  
      public void setUsedIndexSpace(long usedIndexSpace) { this.usedIndexSpace = usedIndexSpace; }
      public long getUsedIndexSpace() { return usedIndexSpace; }
      
      public void setUsedArchiveSpace(long usedArchiveSpace) { this.usedArchiveSpace = usedArchiveSpace; }
      public long getUsedArchiveSpace() { return usedArchiveSpace; }
      
      
      public void ensureDiskSpaceCheck() {
	    	  if (!shouldCheckDiskSpace()) {
					  logger.debug("skipping disk space check");
					  return;
			  }
    	  	  ExecutorService checkService = Executors.newSingleThreadExecutor();
	    	  checkService.execute(new DiskCheck(this));
	    	  checkService.shutdown();
      }
      
	  public boolean enoughDiskSpace() {
		 
		  synchronized(diskSpaceLock) {
		
			  
			  Volumes volumes = Config.getConfig().getVolumes();
			  if (volumes==null) {
				  logger.error("volumes is null");
				  return true;
			  }
			  
			  
			  logger.debug("enoughDiskSpace() {usedIndexSpace='"+usedIndexSpace+"',usedArchiveSpace='"+usedArchiveSpace+"',diskSpaceWarnBytes='"+volumes.getDiskSpaceWarnBytes()+"',diskSpaceThresholdBytes='"+volumes.getDiskSpaceThresholdBytes()+"',freeArchiveSpace='"+freeArchiveSpace+"'");
			  
			  if (!diskSpaceChecking) {
				  logger.debug("disk space checking is disabled due to previous error. Check file permissions on volume index and store path {"+toString()+"}");
			  	  return true;
		  	  }
			
				
			  if (!isDiskSpaceChecked) {
				  logger.debug("disk space has not been checked yet. reporting enough disk space.");
				  return true;
			  }
			  
		     // free index space is nearly depleted
          
		 	 logger.debug("free index space warn check {freeIndexSpace+DISK_SPACE_WARN='"+(freeIndexSpace-volumes.getDiskSpaceWarnBytes())+"<0'}");
		 	 if ((freeIndexSpace-volumes.getDiskSpaceWarnBytes())<=0) {
		 	     logger.warn("storage space is running low on volume {"+toString()+"}");
		 	    Event.notifyEvent("storage space is running low on volume index "+getIndexPath()+" ("+(freeIndexSpace/1024/1024)+"mb remaining)",Event.Category.SPACE);
		 	  }
	         
		 	 // free index space is depleted
	         
		 	 logger.debug("free index space threshold check {freeIndexSpace-DISK_SPACE_THRESHOLD='"+(freeIndexSpace-volumes.getDiskSpaceThresholdBytes())+"<0'}");
		 	  
		 	  if ((freeIndexSpace-volumes.getDiskSpaceThresholdBytes())<=0) {
		 	     logger.warn("there is no storage space left on volume {"+toString()+"}");
		 	    Event.notifyEvent("there is no storage space left on volume index "+getIndexPath()+" ("+(freeIndexSpace/1024/1024)+"mb remaining)",Event.Category.SPACE);
		 	     return false;
		 	  }
	         // free archive space is nearly depleted
	             
	          logger.debug("free archive space warn check {freeArchiveSpace-DISK_SPACE_WARN='"+(freeArchiveSpace-volumes.getDiskSpaceWarnBytes())+"<0'}");
	          if ((freeArchiveSpace-volumes.getDiskSpaceWarnBytes())<=0) {
	              logger.warn("storage space is running low on volume {"+toString()+"}");
	              Event.notifyEvent("storage space is running low on volume store "+getPath()+" ("+(freeArchiveSpace/1024/1024)+"mb remaining)",Event.Category.SPACE);
	          }
	          
	          // free archive is depleted
	          
	          logger.debug("free archive space threshold check {freeArchiveSpace-DISK_SPACE_THRESHOLD='"+(freeArchiveSpace-volumes.getDiskSpaceThresholdBytes())+"<0'}");
		 	  
		 	 if ((freeArchiveSpace-volumes.getDiskSpaceThresholdBytes())<=0) {
		 	    logger.warn("there is no storage space left on volume {"+toString()+"}");
		 	   Event.notifyEvent("there is no storage space left on volume "+getPath()+" ("+(freeArchiveSpace/1024/1024)+"mb remaining)",Event.Category.SPACE);
		 	    return false;
		  	 }
			 	 
			  boolean sameDrive = (freeIndexSpace==freeArchiveSpace);
			  long usedSpace = usedArchiveSpace;
			  
			  if (sameDrive) {
			          usedSpace += usedIndexSpace;
			  }
			
	          // max volume size about to be exceeded
	          
	          logger.debug("used disk space warn check {(usedSpace+DISK_SPACE_WARN)>=maxSize='"+(usedSpace+volumes.getDiskSpaceWarnBytes())+">"+maxSize * 1024 * 1024+"'}");
	          if ((usedSpace+volumes.getDiskSpaceWarnBytes())>=maxSize * 1024 * 1024) {
	             logger.warn("storage space is running low on volume. (max volume size nearly exceeded) {"+toString()+"}");
	             Event.notifyEvent("storage space is running low on volume store "+getPath()+" ("+((maxSize-usedSpace)/1024/1024)+"mb remaining)",Event.Category.SPACE);
	          }
	          
	          // max volume size is exceeded
	          
	          logger.debug("used disk space threshold check {(usedSpace+DISK_SPACE_THRESHOLD)>=maxSize='"+(usedSpace+volumes.getDiskSpaceThresholdBytes())+">"+maxSize * 1024 * 1024 +"'}");
	          if ((usedSpace+volumes.getDiskSpaceThresholdBytes())>=maxSize * 1024 * 1024) {
	             logger.warn("there is no storage space left on volume (max volume size exceeded) {"+toString()+"}");
	             Event.notifyEvent("storage space has run out volume store "+getPath()+" ("+((maxSize-usedSpace)/1024/1024)+"mb remaining)",Event.Category.SPACE);
	             return false;
	          }
	  
		  }
			
	 	  return true;

	  }

	  public Status getStatus() { return status; }

	  public void setStatusNoAssertions(Status newStatus) throws ConfigurationException {
	     this.status = newStatus;
	  }

	  public boolean shouldCheckDiskSpace() { 
		  
		  if (!diskSpaceChecking) {
			  logger.debug("skipping disk space check. checking disabled.");
			  return false;
		  }
		  
		  if (currentlyCheckingDiskSpace) {
			  logger.debug("skipping disk space check. already busy checking disk space.");
			  return false;
		  }
		  
		 
		  if (isDiskSpaceChecked) {
			  logger.debug("skipping disk space check. already checked.");
			  return false;
		  }
		  return true;
		  
	  }
	  
	  
	  public boolean isDiskSpaceChecked() {
	
		  return isDiskSpaceChecked;
	  }
	  
	  public void setDiskSpaceChecked(boolean isDiskSpaceChecked) {
		  this.isDiskSpaceChecked = isDiskSpaceChecked;
	  }
	  
	  public void setStatusForced(Status newStatus) {
		  status = newStatus;
	  }
	  
	  public void setStatus(Status newStatus) throws ConfigurationException {
  	   
		  
          if (status==newStatus)
             return;
        
        if (newStatus!=Status.EJECTED) {
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
        }
        status = newStatus;
	  }

	  
	  // method to eliminate spurious sent dates
	  
	  public boolean isValidSentDate(Email email) {
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
			  marginCal.setTime(email.getReceivedDate());
			  marginCal.add(Calendar.MINUTE, 30);
			  // sent date cannot be after received date
			  if (sentDate.after(marginCal.getTime())) {
				  return false;
				  
			  }
			  
			// if the received date is available, sent date cannot be older than received date - 7 days
			  Calendar oldCal = Calendar.getInstance();
			  oldCal.setTime(email.getReceivedDate());
			  oldCal.add(Calendar.DATE, -7);
			  Date oldDate = oldCal.getTime();
			  if (sentDate.before(oldDate)) {
				  return false;
			  }
		  }
		  return true;
	  }
	  
	

	  public Date getEarliestReceived() {
		  return earliestReceived;
	  }
	  
	  public void setEarliestReceived(Date d) {
		  this.earliestReceived = d;
	  }
	  
	  public Date getLatestReceived() {
		  return latestReceived;
	  }
	  
	  public void setLatestReceived(Date d) {
		  this.latestReceived = d;
	  }
	  
	  public Date getEarliestArchived() {
	      return earliestArchived;
	  }

	  public Date getLatestArchived() {
	      return latestArchived;
	  }

	  public void setLatestArchived(Date d) {
	      this.latestArchived = d;
	  }
	  
	  public void setEarliestArchived(Date d) {
	      this.earliestArchived = d;
	  }
	  
	  
	  public void setEarliestSent(Date d) {
		  this.earliestSent = d;
	  }
	  
	  public Date getEarliestSent() {
		  return this.earliestSent;
	  }
	  
	  public void setLatestSent(Date d) {
		  this.latestSent = d;
	  }
	  
	  public Date getLatestSent() {
		  return this.latestSent;
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

	  public boolean isRemote() {
		  return indexPath.startsWith("rmi://");
	  }
	  
	  public int compareTo(Volume v) throws ClassCastException {
	      int compare = getStatus().compareTo(v.getStatus());
	      if (v.getEarliestArchived()==null || getEarliestArchived()==null)
	    	  return compare;
	      if (compare==0 && getStatus()==Status.CLOSED) // closed
	          compare = getEarliestArchived().compareTo(v.getEarliestArchived());
	      return compare;
	  }

	  public String toString() {
	      return "volumepath='"+path+"',indexpath='"+indexPath+"',volumestatus='"+status+"',latestArchived='"+latestArchived+"',earliestArchived='"+earliestArchived+"'";
	  }
	  
	   protected synchronized void calculateFreeSpace() throws ArchivaException {
		   
		   if (getStatus() != Status.CLOSED && getStatus() != Status.ACTIVE && getStatus() != Status.UNUSED)
			   return;
			   
		   if (!new File(getIndexPath()).exists()) {
			   logger.debug("cannot determine disk space (volume index path does not exist) {"+toString()+"}");
			   return;
		   }
		   
           if (!new File(getPath()).exists()) {
        	   logger.debug("cannot determine disk space (volume store path does not exist) {"+toString()+"}");
        	   return;
           }
           
           long freeIndexSpace 	 = Long.MAX_VALUE / 2;
           long freeArchiveSpace = Long.MAX_VALUE / 2;
           
           try {
        	   if (!isRemote())
        		   freeIndexSpace = new File(getIndexPath()).getUsableSpace();
	           freeArchiveSpace  = new File(getPath()).getUsableSpace();
           } catch (Exception e) {
        	   logger.error("unable to retrieve free space on volume and/or index path. file permissions? {"+toString()+"}",e);
        	   diskSpaceChecking = false;
           }

           synchronized(diskSpaceLock) {
        	   diskSpaceChecking = true;
        	   setFreeIndexSpace(freeIndexSpace);
        	   setFreeArchiveSpace(freeArchiveSpace);
           }
           
           logger.debug("available index disk space {freeIndexSpace='"+freeIndexSpace +" bytes',"+toString()+"}");
           logger.debug("available store disk space {freeArchiveSpace='"+freeArchiveSpace +" bytes',"+toString()+"}");
           
	   }
	   
	   protected synchronized void calculateUsedSpace() throws ArchivaException {
		   
		   if (getStatus() != Status.CLOSED && getStatus() != Status.ACTIVE)
			   return;
			   
		   long totalUsedIndexSpace = 0;
           if (!isRemote()) {
        	   	totalUsedIndexSpace += getFileOrDirectorySize(new File(getIndexPath()));
           }
           setUsedIndexSpace(totalUsedIndexSpace);
           
           long usedArchiveSpace = getFileOrDirectorySize(new File(getPath()));
		   setUsedArchiveSpace(usedArchiveSpace);
		   
           logger.debug("used index disk space {usedIndexSpace='" + usedIndexSpace + "' bytes',"+toString() + "}");
           logger.debug("used store disk space {usedStoreSpace='" + usedArchiveSpace +"' bytes',"+toString() + "}");
	   }
	   
	   public synchronized void calculateSpace()  throws ArchivaException {
	       currentlyCheckingDiskSpace = true;
	       calculateFreeSpace();
		   calculateUsedSpace();
		   currentlyCheckingDiskSpace = false;
		   isDiskSpaceChecked = true;
	   }
	   
	   private static long getFileOrDirectorySize(File file) {
           long size = 0;
           if(file.isDirectory()) {
               File[] files = file.listFiles();
               if(files != null) {
                   for(int i = 0; i < files.length; i++) {
                       long tmpSize = getFileOrDirectorySize(files[i]);
                       if(tmpSize != -1) {
                           size += tmpSize;
                       }
                   }
                   return size;
               } else return 0;
           } else return file.length();
      }
	   
	
	   
	   public boolean isVolumeAccessible() {
		  return new File(getPath()+File.separator+INFO_FILE).exists();
	   }
	
		  protected void writeVolumeInfoLines(RandomAccessFile out) {
			  try {
				   logger.debug("writeVolumeInfoLines()");
				   out.seek(0);
			       // don't save to ejected volume
			       if (getStatus()==Volume.Status.EJECTED || getStatus()==Volume.Status.REMOTE)
			    	  return;
			     
			       // make a new volume unused
			       if (getStatus()==Volume.Status.NEW)
			    	  setStatus(Volume.Status.UNUSED);
			
			    	out.seek(0); //Seek to end of file
			        out.writeBytes("# Archiva "+Config.getConfig().getApplicationVersion()+" Volume Information\n");
			        out.writeBytes("# note: this file is crucial - do not delete it!\n");
			        out.writeBytes("version:2\n");
			        out.writeBytes("id:"+getID()+"\n");
			        if (getStatus()!=null) {
			        	out.writeBytes("status:"+getStatus()+"\n");
			        }
			        if (getEarliestArchived()!=null)
			        	out.writeBytes("earliestarchived:"+DateUtil.convertDatetoString(getEarliestArchived())+"\n");
			        if (getLatestArchived()!=null)
			        	out.writeBytes("latestarchived:"+DateUtil.convertDatetoString(getLatestArchived())+"\n");
			        if (getEarliestReceived()!=null)
			        	out.writeBytes("earliestreceived:"+DateUtil.convertDatetoString(getEarliestReceived())+"\n");
			        if (getLatestReceived()!=null)
			        	out.writeBytes("latestreceived:"+DateUtil.convertDatetoString(getLatestReceived())+"\n");
			        if (getEarliestSent()!=null)
			        	out.writeBytes("earliestsent:"+DateUtil.convertDatetoString(getEarliestSent())+"\n");
			        if (getLatestSent()!=null)
			        	out.writeBytes("latestsent:"+DateUtil.convertDatetoString(getLatestSent())+"\n");
			       
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
			  try {
				  randomAccessFile.seek(0);
				  while (( line = randomAccessFile.readLine()) != null) {
					
			               if (line.startsWith("#"))
			                   continue;
						   StringTokenizer st = new StringTokenizer(line, ":");
				       	   String name = st.nextToken();
				       	   if (name.toLowerCase(Locale.ENGLISH).trim().equals("modified"))
				                  setLatestArchived(DateUtil.convertStringToDate(st.nextToken().trim()));
				       	   else if (name.toLowerCase(Locale.ENGLISH).trim().equals("latestarchived"))
				                  setLatestArchived(DateUtil.convertStringToDate(st.nextToken().trim()));
			              else if (name.toLowerCase(Locale.ENGLISH).trim().equals("created"))
			              	   setEarliestArchived(DateUtil.convertStringToDate(st.nextToken().trim()));
			              else if (name.toLowerCase(Locale.ENGLISH).trim().equals("earliestarchived"))
			              	   setEarliestArchived(DateUtil.convertStringToDate(st.nextToken().trim()));
			              else if (name.toLowerCase(Locale.ENGLISH).trim().equals("earliest"))
			           	   setEarliestSent(DateUtil.convertStringToDate(st.nextToken().trim()));
			              else if (name.toLowerCase(Locale.ENGLISH).trim().equals("earliestsent"))
			           	   setEarliestSent(DateUtil.convertStringToDate(st.nextToken().trim()));
			              else if (name.toLowerCase(Locale.ENGLISH).trim().equals("latest"))
			           	   setLatestSent(DateUtil.convertStringToDate(st.nextToken().trim()));
			              else if (name.toLowerCase(Locale.ENGLISH).trim().equals("latestsent"))
			           	   setLatestSent(DateUtil.convertStringToDate(st.nextToken().trim()));
			              else if (name.toLowerCase(Locale.ENGLISH).trim().equals("earliestreceived"))
			           	   setEarliestReceived(DateUtil.convertStringToDate(st.nextToken().trim()));
			              else if (name.toLowerCase(Locale.ENGLISH).trim().equals("latestreceived"))
			           	   setLatestReceived(DateUtil.convertStringToDate(st.nextToken().trim()));
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
				           	  // here we check if the volume remote, if so change the status to remote
				            	 
			            	  if (isRemote()) {
			            		  status = Volume.Status.REMOTE;
			            	  }
			            	  
			            	  // here we make doubly sure that there is not another active volume
			            	  // if another volume is active, we close the current one and save it.
			            	  if (status == Volume.Status.ACTIVE) {
			            		  Volume activeVolume = Config.getConfig().getVolumes().getActiveVolume();
			            		  if (activeVolume!=null && !this.equals(activeVolume)) {
			            			      activeVolume.setStatus(Volume.Status.CLOSED);
			            				  activeVolume.save();
			            		  }
			            	  }	  
		                      setStatusNoAssertions(status);
			              };
			              
			           
				  }
				  // to deal with legacy volumeinfo files
		           if (getVersion()==null) {
		        	   earliestSent = new Date(0);
		        	   latestSent = new Date();
		           }
		           // we make sure that NEW entries become UNUSED
			        if (getStatus()==Volume.Status.NEW)
			            setStatus(Volume.Status.UNUSED);
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
			  DecimalFormat formatter = new DecimalFormat("#,###,###,###.##");
			  
			  long totalMessageCount = 0;
			  try { 
				  totalMessageCount = getTotalMessageCount();
			  } catch (Exception e) { return ""; };
			  
			  double mil = totalMessageCount / 1000000.0;
			  if (mil>=1)
				  return formatter.format(mil)+"M";
			  
			  double k = totalMessageCount / 1000.0;
			  if (k>=1)
				  return formatter.format(k)+"K";
			 
			  return formatter.format(totalMessageCount);
		  }
		  
		  
		  public String formatDiskSpace(long bytes) {
			  double mb = bytes / 1024.0 / 1024.0;
			  DecimalFormat formatter = new DecimalFormat("#,###,###.##");
			  double tb = mb / 1024.0 / 1024.0;
			  if (tb>=1)
				  return formatter.format(tb)+" TB";
			  
			  double gb = mb / 1024.0;
			  if (gb>=1)
				  return formatter.format(gb)+" GB";
			 
			  return formatter.format(mb)+" MB";
			  
		  }
		  public void setModified(Email email) {
			  
			  if (latestArchived==null || email.getArchiveDate().after(latestArchived)) {
				  
				  latestArchived = email.getArchiveDate();
			  }
			  
			  if (earliestArchived==null || email.getArchiveDate().before(earliestArchived)) {
				  earliestArchived = email.getArchiveDate();
			  }
			  
			  Date sentDate = null;
			  try {
				  sentDate = email.getSentDate();
			  } catch (Exception e) {} 

			  if (isValidSentDate(email)) {
			
				  if (earliestSent==null || sentDate.before(earliestSent)) {
					  earliestSent = sentDate;
				  }
				  if (latestSent==null || sentDate.after(latestSent)) {
					  latestSent = sentDate;
				  }
			  }
			  
			  Date receivedDate = null;
			  receivedDate = email.getReceivedDate();
			  if (receivedDate!=null) {
				  
				  if (earliestReceived==null || receivedDate.before(earliestReceived)) {
					  earliestReceived = receivedDate;
				  }
				  
				  if (latestReceived==null || receivedDate.after(latestReceived)) {
					  latestReceived = receivedDate;
				  }
			  }
		  }
			  
		  protected RandomAccessFile getRandomAccessFile(String attr) throws FileNotFoundException {
			  return new RandomAccessFile(getPath()+File.separator+INFO_FILE, attr);
		  }
		  
		  public void touchModified(Email email) throws ConfigurationException {
			  synchronized(volumeinfoLock) {
				  logger.debug("touchModified() {"+email+"}");
				  RandomAccessFile randomAccessFile = null;
				  FileLock fileLock = null;
				  try {
					  randomAccessFile = getRandomAccessFile("rw");
					  FileChannel channel = randomAccessFile.getChannel();
					  try {
						  fileLock = channel.lock();
					  } catch (IOException io) {
						  logger.error("failed to obtain lock to volumeinfo file:"+io.getMessage()+" {"+toString()+"}");
						  return;
					  }
					  // we do not want to read disk space info, as it will overwrite existing info
					  readVolumeInfoLines(randomAccessFile);
					  if (email!=null) {
						  setModified(email);
					  }
				  } catch (FileNotFoundException fnfe) {
					  try {
						  randomAccessFile = getRandomAccessFile("w");
					  } catch (FileNotFoundException fnfe2) {
						  logger.error("failed open volumeinfo file:"+fnfe2.getMessage()+" {"+toString()+"}");
						  return;
					  }
					  FileChannel channel = randomAccessFile.getChannel();
					  try {
						  fileLock = channel.lock();
					  } catch (IOException io) {
						  logger.error("failed to obtain lock to volumeinfo file:"+io.getMessage()+" {"+toString()+"}");
						  try { fileLock.release(); } catch (Exception e) {}
						  return;
					  }
				  }
				  writeVolumeInfoLines(randomAccessFile);
				
				  try {
					  if (fileLock!=null)
					  	fileLock.release();
				  } catch (IOException io) {
					  logger.error("failed to release file lock on volumeinfo file:"+io.getMessage()+" {"+toString()+"}");
				  }
				  try {
					  randomAccessFile.close();
				  } catch (IOException io) {
					  logger.error("failed to close volumeinfo file:"+io.getMessage()+" {"+toString()+"}");
				  }
			  }
		  }
		  
		   
		   public void saveSettings(String prefix, Settings prop, String suffix) {
			   		logger.debug("saving volume settings");
			   	    prop.setProperty(volumePathKey + suffix,getPath());
	    			prop.setProperty(volumeIndexPathKey + suffix,getIndexPath());
	    			prop.setProperty(volumeMaxSizeKey + suffix,Long.toString(getMaxSize()));
	    			prop.setProperty(volumeRemoteSearchKey +suffix,ConfigUtil.getYesNo(getAllowRemoteSearch()));
	    			
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
		  	    setAllowRemoteSearch(ConfigUtil.getBoolean(vrs, defaultVolumeRemoteSearch));
		  	    return true;
		   }
		   

		  public void load() throws ConfigurationException {	 
			  synchronized(volumeinfoLock) {
				  logger.debug("load() volumeinfo");
				  RandomAccessFile randomAccessFile = null;
				  try {
					  randomAccessFile = getRandomAccessFile("rw");
				  } catch (FileNotFoundException fnfe) {
					  logger.debug("failed open volumeinfo file:"+fnfe.getMessage()+" {"+toString()+"}");
					  return;
				  }
				  FileChannel channel = randomAccessFile.getChannel();
				  FileLock fileLock = null;
				  try {
					  fileLock = channel.lock();
				  } catch(IOException io) {
					  logger.error("failed to obtain lock to volumeinfo file:"+io.getMessage()+" {"+toString()+"}");
					  return;
				  } catch(OverlappingFileLockException ofle) {
					  logger.error("failed to obtain lock to volumeinfo file:"+ofle.getMessage()+" {"+toString()+"}");
					  return;
				  }
				  readVolumeInfoLines(randomAccessFile);
				  try {
					  fileLock.release();
				  } catch (IOException io) {
					  logger.error("failed to release the write lock on volumeinfo file:"+io.getMessage()+" {"+toString()+"}");
				  }
				 
				  try {
					  channel.close();
					  randomAccessFile.close();
				  } catch (IOException io) {
					  logger.error("failed to close volumeinfo file:"+io.getMessage()+" {"+toString()+"}");
				  }
			  }
		  }
		
		  // dummy function for utilities backwards compatibility
		  public void save(boolean overwrite) throws ConfigurationException {
			  save();
		  }
		  
		  public void save()  throws ConfigurationException {
		
		  
			  synchronized(volumeinfoLock) {
				  logger.debug("save() volumeinfo");
				  RandomAccessFile randomAccessFile = null;
				  try {
					  randomAccessFile = getRandomAccessFile("rw");
				  } catch (FileNotFoundException fnfe) {
						 if (getStatus()!=Status.UNMOUNTED) {
					  		 logger.error("failed open volumeinfo file:"+fnfe.getMessage()+" {"+toString()+"}");
						 }
					  return;
				  }
				  FileChannel channel = randomAccessFile.getChannel();
				  FileLock fileLock = null;
				  try {
					  fileLock = channel.lock();
				  } catch(IOException io) {
					  logger.error("failed to obtain lock to volumeinfo file:"+io.getMessage()+" {"+toString()+"}");
					  return;
				  } catch(OverlappingFileLockException ofle) {
					  logger.error("failed to obtain lock to volumeinfo file:"+ofle.getMessage()+" {"+toString()+"}");
					  return;
				  }
				  writeVolumeInfoLines(randomAccessFile);
				  try {
					  fileLock.release();
				  } catch (IOException io) {
					  logger.error("failed to release the write lock on volumeinfo file:"+io.getMessage()+" {"+toString()+"}");
				  }
				  try {
					  channel.close();
					  randomAccessFile.close();
				  } catch (IOException io) {
					  logger.error("failed to close volumeinfo file:"+io.getMessage()+" {"+toString()+"}");
				  }
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
		
  }