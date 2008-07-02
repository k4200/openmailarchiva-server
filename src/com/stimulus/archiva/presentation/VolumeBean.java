
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

package com.stimulus.archiva.presentation;

import com.stimulus.archiva.domain.*;
import com.stimulus.archiva.domain.Volume.Status;
import com.stimulus.archiva.exception.ConfigurationException;
import com.stimulus.util.EnumUtil;

import java.io.Serializable;
import java.util.*;
import org.apache.log4j.Logger;
import com.stimulus.struts.BaseBean;
import java.text.*;

public class VolumeBean extends BaseBean implements Serializable {

	 private static final long serialVersionUID = -166626751279723226L;
	 protected static Logger logger = Logger.getLogger(VolumeBean.class.getName());
	 protected static final SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	 
	 protected Volume v;
	 
	 public VolumeBean(Volume v)  {
		 this.v = v;
	 }

	 public String getStatus() {
		 return v.getStatus().toString();
	 }
	 
	 public void setStatus(String status) throws ConfigurationException {
			 Status newStatus = Status.CLOSED;
			 try {
				 newStatus = Status.valueOf(status.trim().toUpperCase());
			 } catch (IllegalArgumentException iae) {
	 	    		logger.error("failed to set volume status. status is set to an illegal value {status='"+status+"'}");
	 	    		logger.info("volume is automatically set to closed (error recovery)");
	  		 }
			 v.setStatus(newStatus);
		 
	 }
	 
	  public int getStatusID() {
		  return v.getStatus().ordinal();
	  }

	  public String getModified() {
		  if (v.getLatestArchived()!=null)
	  	  	return format.format(v.getLatestArchived());
		  else return "";
	  }

	  public String getCreated() {
		  if (v.getEarliestArchived()!=null)
			  return format.format(v.getEarliestArchived());
		  else return "";
	  }
		 
	  public long getMaxSize() { return v.getMaxSize() ; }
	  public String getIndexPath() { return v.getIndexPath(); }
	  
	  public void setIndexPath(String indexPath) { v.setIndexPath(indexPath); }
	  public String getPath() { return v.getPath(); }
	  public void setPath(String path) { v.setPath(path);}
	  public void setMaxSize(long maxSize) { v.setMaxSize(maxSize); }

	  public static List<VolumeBean> getVolumeBeans(List<Volume> volumes) {
		  List<VolumeBean> volumeBeans = new LinkedList<VolumeBean>();
		  for (Volume vol: volumes)
			  volumeBeans.add(new VolumeBean(vol));
		  return volumeBeans;
	  }
	  
	  public static List<VolumeBean> getVolumeBeans(Volumes volumes) {
		  return getVolumeBeans(volumes.getVolumes());
	  }
	  
	  public static List<String> getStatuses() {
	    	return EnumUtil.enumToList(Volume.Status.values());
	  }
	
	  public String getFreeIndexSpace() { 
		  if (v.getFreeIndexSpace() == Long.MAX_VALUE ) 
			  return "";
		  else
			  return v.formatDiskSpace(v.getFreeIndexSpace()); 
	  }
	  
	  public String getFreeArchiveSpace() { 
		  if (v.getFreeArchiveSpace() == Long.MAX_VALUE ) 
			  return "";
		  else
			  return v.formatDiskSpace(v.getFreeArchiveSpace());  
	  }
	  
	  public String getUsedIndexSpace() { 
		  if (v.getUsedIndexSpace() == -1)
			  return "";
		  else
			  return v.formatDiskSpace(v.getUsedIndexSpace());
	  }
	  
	  public String getUsedArchiveSpace() { 
		  if (v.getUsedArchiveSpace() == -1)
			  return "";
		  else
			  return v.formatDiskSpace(v.getUsedArchiveSpace());
	  }
	  
	
	  public String getTotalMessageCount() {
		 return v.formatTotalMessageCount();
	  }
	  
	
}
