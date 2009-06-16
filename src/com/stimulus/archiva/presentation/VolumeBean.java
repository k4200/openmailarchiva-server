
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
import com.stimulus.archiva.exception.ArchivaException;
import com.stimulus.archiva.exception.ConfigurationException;
import com.stimulus.util.EnumUtil;

import java.io.Serializable;
import java.util.*;
import org.apache.commons.logging.*;
import com.stimulus.struts.BaseBean;
import java.text.*;

public class VolumeBean extends BaseBean implements Serializable {

	 private static final long serialVersionUID = -166626751279723226L;
	 protected static Log logger = LogFactory.getLog(VolumeBean.class.getName());
	 protected static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

	 protected Volume v;

	 public VolumeBean(Volume v)  {

		 this.v = v;

		  		if (v.getStatus()==Volume.Status.ACTIVE ||
		  			v.getStatus()==Volume.Status.CLOSED  ||
		  			v.getStatus()==Volume.Status.UNUSED) {

		  		}

	 }

	 public String getStatus() {
		 // if its ejected, we want to show the status as ejected
		 if (v.getStatus()!=Volume.Status.NEW && v.isEjected()) {
			 return Volume.Status.EJECTED.toString();
		 }

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

			 // we are using a different flag for ejected status
			 //so as to preserve the original volume status when the drive is reinserted
			 if (newStatus!=Volume.Status.EJECTED)
				 v.setStatus(newStatus);

	 }

	  public int getStatusID() {
		 // if its ejected, we want to show the status as ejected
		  if (v.isEjected()) {
				 return Volume.Status.EJECTED.ordinal();
			 }

		  return v.getStatus().ordinal();
	  }

	  public String getClosed() {
		  if (v.getClosedDate()!=null)
	  	  	return format.format(v.getClosedDate());
		  else return "";
	  }

	  public String getCreated() {
		  if (v.getCreatedDate()!=null)
			  return format.format(v.getCreatedDate());
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
		  if (v.isDiskSpaceChecked())
			  return v.formatDiskSpace(v.getFreeIndexSpace());
		  else
			  return "";
	  }

	  public String getFreeArchiveSpace() {
		  try {
		  if (v.isDiskSpaceChecked())
			  return v.formatDiskSpace(v.getFreeArchiveSpace());
		  else
			  return "";
		  } catch (Throwable t) {
			  logger.error("failed to obtain free archive space:"+t.getMessage(),t);
			  return "";
		  }
	  }

	  public String getUsedIndexSpace() {
		  if (v.isDiskSpaceChecked())
			  return v.formatDiskSpace(v.getUsedIndexSpace());
		  else
			  return "";
	  }

	  public String getUsedArchiveSpace() {
		  if (v.isDiskSpaceChecked())
			  return v.formatDiskSpace(v.getUsedArchiveSpace());
		  else
			  return "";
	  }


	  public String getTotalMessageCount() {
		 return v.formatTotalMessageCount();
	  }


}
