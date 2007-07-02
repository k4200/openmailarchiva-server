/* Copyright (C) 2005 Jamie Angus Band 
 * This software program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.stimulus.archiva.domain;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import org.apache.log4j.Logger;
import com.stimulus.archiva.exception.ConfigurationException;

  public class Volume implements Comparable {

      protected static Logger logger = Logger.getLogger(Volume.class.getName());
      public static final int CLOSED = 0;
      public static final int ACTIVE = 1;
      public static final int UNUSED = 2;
      public static final int NEW    = 3;
      public static final List STATUS;
      public static final int DISK_SPACE_WARN  = 20;
      public static final int DISK_SPACE_THRESHOLD = 3;
      public static final SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
 	  protected String 	path;
 	  protected String 	indexPath;
 	  protected long 	maxSize = 3000;
      protected String 	status = "NEW";
 	  protected long    usedIndexSpace = 0;
 	  protected long    freeIndexSpace = Long.MAX_VALUE / 2;
 	  protected long    usedArchiveSpace = 0;
	  protected long    freeArchiveSpace = Long.MAX_VALUE / 2;
	  protected Date	modified = null;
	  protected Date    created = null;

 	 static {
	     List status = new ArrayList();
	     status.add("CLOSED");
	     status.add("ACTIVE");
	     status.add("UNUSED");
	     status.add("NEW");
	     STATUS = Collections.unmodifiableList(status);

     }

	  public Volume(String path, String indexPath,  int maxSize) throws ConfigurationException {
	  	setIndexPath(indexPath);
	  	setPath(path);
	  	setMaxSize(maxSize);
	  }

	  public long getMaxSize() { return maxSize; }
	  public String getIndexPath() { return indexPath; }
	  public void setIndexPath(String indexPath) { this.indexPath = indexPath.toLowerCase(); }
	  public String getPath() { return path; }
	  public void setPath(String path) { this.path = path.toLowerCase();}
	  public void setMaxSize(long maxSize) { this.maxSize = maxSize; }

	  public void setUsedIndexSpace(long usedIndexSpace) { this.usedIndexSpace = usedIndexSpace; }
	  public long getUsedIndexSpace() { return usedIndexSpace; }
	  public long getFreeIndexSpace() { return freeIndexSpace; }
	  public void setFreeIndexSpace(long freeIndexSpace) { this.freeIndexSpace = freeIndexSpace; }

	  public void setUsedArchiveSpace(long usedArchiveSpace) { this.usedArchiveSpace = usedArchiveSpace; }
	  public long getUsedArchiveSpace() { return usedArchiveSpace; }
	  public long getFreeArchiveSpace() { return freeArchiveSpace; }
	  public void setFreeArchiveSpace(long freeArchiveSpace) { this.freeArchiveSpace = freeArchiveSpace; }

	  public boolean enoughDiskSpace() {

	 	  long usedSpace = usedIndexSpace > usedArchiveSpace ? usedIndexSpace : usedArchiveSpace;

	 	  logger.debug("diskspace check {usedSpace-DISK_SPACE_WARN='"+(usedSpace-DISK_SPACE_WARN)+"'}");
	 	  if ((usedSpace-DISK_SPACE_WARN)>=maxSize) {
	 	     logger.warn("storage space is running low on volume. {"+toString()+"}");
	 	  }
	 	 logger.debug("diskspace check {freeIndexSpace-DISK_SPACE_WARN='"+(freeIndexSpace-DISK_SPACE_WARN)+"'}");
	 	 if ((freeIndexSpace-DISK_SPACE_WARN)<=0) {
	 	     logger.warn("storage space is running low on volume {"+toString()+"}");
	 	 }
	 	logger.debug("diskspace check {freeArchiveSpace-DISK_SPACE_WARN='"+(freeArchiveSpace-DISK_SPACE_WARN)+"'}");
	 	 if ((freeArchiveSpace-DISK_SPACE_WARN)<=0) {
	 	     logger.warn("storage space is running low on volume {"+toString()+"}");
	 	 }
	 	logger.debug("diskspace check {usedSpace-DISK_SPACE_THRESHOLD='"+(usedSpace-DISK_SPACE_THRESHOLD)+"'}");
	 	  if ((usedSpace-DISK_SPACE_THRESHOLD)>=maxSize) {
	 	     logger.warn("there is no storage space left on volume {"+toString()+"}");
	 	     return false;
	 	  }
	 	 logger.debug("diskspace check {freeIndexSpace-DISK_SPACE_THRESHOLD='"+(freeIndexSpace-DISK_SPACE_THRESHOLD)+"'}");
	 	  
	 	  if ((freeIndexSpace-DISK_SPACE_THRESHOLD)<=0) {
	 	     logger.warn("there is no storage space left on volume {"+toString()+"}");
	 	     return false;
	 	  }
	 	 logger.debug("diskspace check {freeArchiveSpace-DISK_SPACE_THRESHOLD='"+(freeArchiveSpace-DISK_SPACE_THRESHOLD)+"'}");
	 	  
	 	 if ((freeArchiveSpace-DISK_SPACE_THRESHOLD)<=0) {
	 	    logger.warn("there is no storage space left on volume {"+toString()+"}");
	 	    return false;
	  	 }
	 	  return true;

	  }

	  public String getStatus() { return status; }

	  public void setStatusNoAssertions(String status) throws ConfigurationException {
	      if (!STATUS.contains(status.toUpperCase()))
	  	        throw new ConfigurationException("failed to set volume status {newstatus='"+status+"'}",logger);
	      this.status = status.toUpperCase().trim();
	  }

	  public void setStatus(String status) throws ConfigurationException {
  	    if (!STATUS.contains(status.toUpperCase()))
  	        throw new ConfigurationException("failed to set volume status {newstatus='"+status+"'}",logger);

  	    int newStatusID = STATUS.indexOf(status);
  	    int oldStatusID = STATUS.indexOf(this.status);
  	    logger.debug("setStatus {newstatusid='"+newStatusID+"',oldstatusid='"+oldStatusID+"'}");
  	    if (newStatusID==oldStatusID)
  	     return;

  	    switch(oldStatusID) {
  	    	case CLOSED: throw new ConfigurationException("failed to change volume status. it is closed {newstatus='"+status+"'}",logger);
  	    	case ACTIVE: if (newStatusID!=CLOSED)
  	    	    		   throw new ConfigurationException("failed to change volume status. it can only be closed {newstatus='"+status+"'}",logger);
  	    				 break;
  	    	case UNUSED: if (newStatusID!=ACTIVE)
  	    	    		   throw new ConfigurationException("failed to change volume status. it can only be made active {newstatus='"+status+"'}",logger);
  	    				 break;
  	    	case NEW: if (newStatusID!=UNUSED)
  	    	    		throw new ConfigurationException("failed to change volume status. it can only be made active {newstatus='"+status+"'}",logger);
  	    				break;
  	    	default: throw new ConfigurationException("failed to change volume status. internal status is set to invalid value.",logger);
  	    }


  	    this.status = status.toUpperCase().trim();
	  }

	  public int getStatusID() {
	  	    return STATUS.indexOf(status);
	  }

	  public  void setStatusID(int id) throws ConfigurationException {
	  	 setStatus((String)STATUS.get(id));
	  }

	  public Date getCreated() {
	      return created;
	  }

	  public Date getModified() {
	      return modified;
	  }

	  public String getModifiedStr() {
	  	  //DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
	  	  return format.format(modified);
	  }

	  public String getCreatedStr() {
	  	  //DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
	  	  return format.format(created);
	  }


	  public void setModified(Date d) {
	      this.modified = d;
	  }

	  public void setCreated(Date d) {
	      this.created = d;
	  }

	  public int compareTo(Object o) throws ClassCastException {
	      Volume v = (Volume)o;
	      int compare = new Integer(getStatusID()).compareTo(new Integer(v.getStatusID()));
	      if (compare==0 && getStatusID()==CLOSED) // closed
	          compare = getModified().compareTo(v.getModified());
	      return compare;
	  }

	  public String toString() {
	      return "volumepath='"+path+"',indexpath='"+indexPath+"',volumestatus='"+status+"',modified='"+modified+"',created='"+created+"'";
	  }

	/*  protected Date 	modified = null;
      protected Date    created = null;
 	  protected long    usedIndexSpace = 0;
 	  protected long    freeIndexSpace = Long.MAX_VALUE / 2;
 	  protected long    usedArchiveSpace = 0;
	  protected long    freeArchiveSpace = Long.MAX_VALUE / 2;
	 *

	  public String getCreatedStr() {
	      SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSS");
	  	  if (created!=null)
	  	      return format.format(created);
	  	  else
	  	      return "";
	  }*/

  }