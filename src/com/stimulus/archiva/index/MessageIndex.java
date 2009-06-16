/* Copyright (C) 2005-2009 Jamie Angus Band
 * MailArchiva Open Source Edition Copyright (c) 2005-2009 Jamie Angus Band
 * This program is free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation; either version
 * 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see http://www.gnu.org/licenses or write to the Free Software Foundation,Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */


package com.stimulus.archiva.index;


import java.io.*;
import java.util.*;
import org.apache.commons.logging.*;
import com.stimulus.archiva.domain.*;
import com.stimulus.archiva.exception.*;
import com.stimulus.archiva.language.*;
import com.stimulus.archiva.extraction.*;
import com.stimulus.archiva.search.*;
import com.stimulus.util.TempFiles;
import com.stimulus.archiva.domain.fields.*;
import com.stimulus.util.*;
import java.nio.charset.Charset;


public class MessageIndex extends Indexer implements Serializable {

	private static final long serialVersionUID = -17692874371162272L;
	protected static final Log logger = LogFactory.getLog(MessageIndex.class.getName());
	protected static int INDEX_WAIT_PERIOD = 50;
	protected static int DEAD_PERIOD = 300000000;
	static Hashtable<Volume,VolumeIndex> volumeIndexes = new Hashtable<Volume,VolumeIndex>();
	static Object volumeIndexLock = new Object();
	protected ServiceDelegate serviceDelegate;

	 public MessageIndex() {
		 serviceDelegate = new ServiceDelegate("message index", this, logger);
	}

	 public String getServiceName() {
		 return serviceDelegate.getServiceName();
	 }

	 public boolean isAlive() {
		 return serviceDelegate.isAlive(true);
	}

	 public void startup() {
		 for (VolumeIndex volumeIndex : volumeIndexes.values()) {
			 volumeIndex.startup();
		 }
		 serviceDelegate.startup();
	 }

	 public void prepareShutdown() {
		 serviceDelegate.prepareShutdown();
		 try { Thread.sleep(2000); } catch (Exception e) {}
	 }

	 public void shutdown() {
		 for (VolumeIndex volumeIndex : volumeIndexes.values()) {
			 if (volumeIndex!=null)
				 volumeIndex.shutdown();
		 }
		 serviceDelegate.shutdown();
	 }

	 public void reloadConfig() {
		 serviceDelegate.reloadConfig();
	 }

	public Status getStatus() {
		return serviceDelegate.getStatus();
	}

	@Override
	public void deleteIndex(Volume volume) throws MessageSearchException {
		  VolumeIndex volumeIndex = getVolumeIndex(volume);
		  volumeIndex.deleteIndex();
	}


	public VolumeIndex getVolumeIndex(Volume volume) {
		 VolumeIndex volumeIndex = null;
		  synchronized (volumeIndexLock) {
			  volumeIndex = volumeIndexes.get(volume);
			  if (volumeIndex==null) {
				  volumeIndex = new VolumeIndex(this,volume);
				  volumeIndexes.put(volume,volumeIndex);
			  }
		  }
		  return volumeIndex;
	}

	@Override
	public void indexMessage(Email email) throws MessageSearchException {
		if (serviceDelegate.getStatus()!=Status.STARTED) {
			return;
		}
		VolumeIndex volumeIndex = getVolumeIndex(email.getEmailId().getVolume());
		volumeIndex.indexMessage(email);
	}


	@Override
	public void deleteMessage(EmailID emailID) throws MessageSearchException {
		  VolumeIndex volumeIndex = getVolumeIndex(emailID.getVolume());
		  volumeIndex.deleteMessage(emailID);
	}

	@Override
	public void prepareIndex(Volume volume) throws MessageSearchException {

		  if (volume==null)
		            throw new MessageSearchException("assertion failure: null volume",logger);

	  if (volume.getIndexPath().startsWith("rmi://"))
			  return;

	  File indexDir = new File(volume.getIndexPath());
	  if (!indexDir.exists()) {
		logger.info("index directory does not exist. will proceed with creation {location='" + volume.getIndexPath() + "'}");
		boolean success = indexDir.mkdirs();
		if (!success)
				throw new MessageSearchException("failed to create index directory {location='" + volume.getIndexPath() + "'}",logger);
		logger.info("index directory successfully created {location='" + volume.getIndexPath() + "'}");
	  }

	}



}

