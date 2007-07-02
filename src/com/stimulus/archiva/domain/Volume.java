
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
import java.util.*;
import org.apache.log4j.Logger;

import com.stimulus.archiva.exception.ArchivaException;
import com.stimulus.archiva.exception.ConfigurationException;

  public class Volume implements Comparable<Volume> {

	  private static final long serialVersionUID = 5447936271470342602L;
	  
      protected static Logger logger = Logger.getLogger(Volume.class.getName());
      public enum Status { CLOSED,ACTIVE,UNUSED,NEW };
      protected String 	path;
 	  protected String 	indexPath;
 	  protected long 	maxSize = 3000;
      protected Status	status = Status.NEW;
 	  protected long    freeIndexSpace 	 = Long.MAX_VALUE;
	  protected long    freeArchiveSpace = Long.MAX_VALUE;
	  protected Date	modified = null;
	  protected Date    created = null;
      protected long    usedIndexSpace 	 = 0;
      protected long    usedArchiveSpace = 0;
      public boolean 	diskSpaceChecking = true;
      protected boolean spaceCheck  = false;
      
      protected Volumes volumes;
      
	  public Volume(Volumes volumes, String path, String indexPath,  int maxSize) throws ConfigurationException {
	  	setIndexPath(indexPath);
	  	setPath(path);
	  	setMaxSize(maxSize);
	  	this.volumes = volumes;
	  	diskSpaceChecking = volumes.getDiskSpaceChecking();
	  	if (diskSpaceChecking) {
		  	try {
		  		setFreeSpace();
		  	} catch (ArchivaException ae) {
		  		diskSpaceChecking = false;
		  	}
	  	}
	  }

	  public long getMaxSize() { return maxSize; }
	  public String getIndexPath() { return indexPath; }
	  public void setIndexPath(String indexPath) { this.indexPath = indexPath.toLowerCase(); }
	  public String getPath() { return path; }
	  public void setPath(String path) { this.path = path.toLowerCase();}
	  public void setMaxSize(long maxSize) { this.maxSize = maxSize; }

	  public long getFreeIndexSpace() { return freeIndexSpace; }
	  public void setFreeIndexSpace(long freeIndexSpace) { this.freeIndexSpace = freeIndexSpace; }
  
	  public long getFreeArchiveSpace() { return freeArchiveSpace; }
	  public void setFreeArchiveSpace(long freeArchiveSpace) { this.freeArchiveSpace = freeArchiveSpace; }
	  
      public void setUsedIndexSpace(long usedIndexSpace) { this.usedIndexSpace = usedIndexSpace; }
      public long getUsedIndexSpace() { return usedIndexSpace; }
      
      public void setUsedArchiveSpace(long usedArchiveSpace) { this.usedArchiveSpace = usedArchiveSpace; }
      public long getUsedArchiveSpace() { return usedArchiveSpace; }
      
	  public boolean enoughDiskSpace() {

		  if (!diskSpaceChecking) {
			  logger.warn("disk space checking is disabled due to previous error. Check file permissions on volume index and store path {"+toString()+"}");
		  	  return true;
	  	  }
		  
		  if (!spaceCheck) {
			  logger.debug("disk space checks have not been performed. report enough disk space.");
			  return true;
		  }
			  
          long usedSpace = usedIndexSpace > usedArchiveSpace ? usedIndexSpace : usedArchiveSpace;

          // max volume size about to be exceeded
          
          logger.debug("diskspace check {(usedSpace-DISK_SPACE_WARN)>=maxSize='"+(usedSpace-volumes.getDiskSpaceWarnBytes())+"'}");
          if ((usedSpace-volumes.getDiskSpaceWarnBytes())>=maxSize * 1024 * 1024) {
             logger.warn("storage space is running low on volume. (max volume size nearly exceeded) {"+toString()+"}");
          }
          
          // max volume size is exceeded
          
          logger.debug("diskspace check {(usedIndexSpace-DISK_SPACE_THRESHOLD)>=maxSize='"+(usedIndexSpace-volumes.getDiskSpaceThresholdBytes())+"'}");
          if ((usedSpace-volumes.getDiskSpaceThresholdBytes())>=maxSize * 1024 * 1024) {
             logger.warn("there is no storage space left on volume (max volume size exceeded) {"+toString()+"}");
             return false;
          }
        
          // free index space is nearly depleted
          
	 	 logger.debug("diskspace check {freeIndexSpace-DISK_SPACE_WARN='"+(freeIndexSpace-volumes.getDiskSpaceWarnBytes())+"'}");
	 	 if ((freeIndexSpace-volumes.getDiskSpaceWarnBytes())<=0) {
	 	     logger.warn("storage space is running low on volume {"+toString()+"}");
	 	  }
         
	 	 // free index space is depleted
         
	 	 logger.debug("diskspace check {freeIndexSpace-DISK_SPACE_THRESHOLD='"+(freeIndexSpace-volumes.getDiskSpaceThresholdBytes())+"'}");
	 	  
	 	  if ((freeIndexSpace-volumes.getDiskSpaceThresholdBytes())<=0) {
	 	     logger.warn("there is no storage space left on volume {"+toString()+"}");
	 	     return false;
	 	  }
         // free archive space is nearly depleted
             
          logger.debug("diskspace check {freeArchiveSpace-DISK_SPACE_WARN='"+(freeArchiveSpace-volumes.getDiskSpaceWarnBytes())+"'}");
          if ((freeArchiveSpace-volumes.getDiskSpaceWarnBytes())<=0) {
              logger.warn("storage space is running low on volume {"+toString()+"}");
          }
          
          // free archive is depleted
          
          logger.debug("diskspace check {freeArchiveSpace-DISK_SPACE_THRESHOLD='"+(freeArchiveSpace-volumes.getDiskSpaceThresholdBytes())+"'}");
	 	  
	 	 if ((freeArchiveSpace-volumes.getDiskSpaceThresholdBytes())<=0) {
	 	    logger.warn("there is no storage space left on volume {"+toString()+"}");
	 	    return false;
	  	 }
	 	  return true;

	  }

	  public Status getStatus() { return status; }

	  public void setStatusNoAssertions(Status newStatus) throws ConfigurationException {
	     this.status = newStatus;
	  }

	  public void setStatus(Status newStatus) throws ConfigurationException {
  	   
		  
          if (status==newStatus)
             return;
          
  	    switch(status) {
  	    	case CLOSED: throw new ConfigurationException("failed to change volume status. it is closed {newstatus='"+status+"'}",logger);
  	    	case ACTIVE: if (newStatus!=Status.CLOSED)
  	    	    		   throw new ConfigurationException("failed to change volume status. it can only be closed {newstatus='"+status+"'}",logger);
  	    				 break;
  	    	case UNUSED: if (newStatus!=Status.ACTIVE)
  	    	    		   throw new ConfigurationException("failed to change volume status. it can only be made active {newstatus='"+status+"'}",logger);
  	    				 break;
  	    	case NEW: if (newStatus!=Status.UNUSED)
  	    	    		throw new ConfigurationException("failed to change volume status. it can only be made active {newstatus='"+status+"'}",logger);
  	    				break;
  	    	default: throw new ConfigurationException("failed to change volume status. internal status is set to invalid value.",logger);
  	    }
        status = newStatus;
	  }

	  public Date getCreated() {
	      return created;
	  }

	  public Date getModified() {
	      return modified;
	  }

	  public void setModified(Date d) {
	      this.modified = d;
	  }

	  public void setCreated(Date d) {
	      this.created = d;
	  }

	  public int compareTo(Volume v) throws ClassCastException {
	      int compare = getStatus().compareTo(v.getStatus());
	      if (compare==0 && getStatus()==Status.CLOSED) // closed
	          compare = getModified().compareTo(v.getModified());
	      return compare;
	  }

	  public String toString() {
	      return "volumepath='"+path+"',indexpath='"+indexPath+"',volumestatus='"+status+"',modified='"+modified+"',created='"+created+"'";
	  }
	  
	   protected synchronized void setFreeSpace() throws ArchivaException {
		   if (!new File(getIndexPath()).exists())
               throw new ArchivaException("cannot determine disk space (volume index path does not exist) {"+toString()+"}",logger);
           
           if (!new File(getPath()).exists())
               throw new ArchivaException("cannot determine disk space (volume store path does not exist) {"+toString()+"}",logger);
           
           long freeIndexSpace 	 = Long.MAX_VALUE / 2;
           long freeArchiveSpace = Long.MAX_VALUE / 2;
           
           try {
	           freeIndexSpace = new File(getIndexPath()).getUsableSpace();
	           freeArchiveSpace  = new File(getPath()).getUsableSpace();
           } catch (Exception e) {
        	   logger.error("unable to retrieve free space on volume and/or index path. file permissions? {"+toString()+"}",e);
        	   diskSpaceChecking = false;
           }
           diskSpaceChecking = true;
         
           setFreeIndexSpace(freeIndexSpace);
           setFreeArchiveSpace(freeArchiveSpace);
           
           logger.debug("available index disk space {freeIndexSpace='"+freeIndexSpace +" bytes',"+toString()+"}");
           logger.debug("available store disk space {freeArchiveSpace='"+freeArchiveSpace +" bytes',"+toString()+"}");
           
	   }
	   
	   protected synchronized void setUsedSpace() throws ArchivaException {
		   long usedArchiveSpace = getFileOrDirectorySize(new File(getPath()));
           long usedIndexSpace   = getFileOrDirectorySize(new File(getIndexPath()));
           setUsedIndexSpace(usedIndexSpace );
           setUsedArchiveSpace(usedArchiveSpace);
           logger.debug("used index disk space {usedIndexSpace='"+usedIndexSpace + " bytes',"+toString()+"}");
           logger.debug("used store disk space {usedStoreSpace='"+usedArchiveSpace +" bytes',"+toString()+"}");
	   }
	   
	   public synchronized void setDiskSpace()  throws ArchivaException {
		   setFreeSpace();
		   setUsedSpace();
		   spaceCheck = true;
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
               }
           return -1;
           }
           return file.length();
      }
     

	 

  }