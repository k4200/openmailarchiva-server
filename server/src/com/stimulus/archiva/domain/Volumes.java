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

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

import com.stimulus.archiva.exception.*;
import com.stimulus.util.*;

public class Volumes {

    public static final int PRIORITY_HIGHER  			=  1;
    public static final int PRIORITY_LOWER   			= -1;
    protected static final Logger logger 				= Logger.getLogger(Volumes.class);
    protected static final int DISK_SPACE_WORKER_WAIT 	= 5000;
    protected static final int DISK_SPACE_WARN_BYTES 	= 10240;
    protected static final String INFO_FILE				= "volumeinfo";
    protected List 			 	volumes 	   	= new ArrayList();
    private   SimpleDateFormat	format	   		= new SimpleDateFormat("yyyyMMddHHmmssSS");
    protected boolean  		 	shutdown 	    = true;
    protected Thread         	diskInfoWorker 	= new DiskInfoWorker();

 	  public void addVolume(String path, String indexPath,  int maxSize) throws ConfigurationException {
 	     Iterator i = volumes.iterator();


 	      	Volume mv = new Volume(path,indexPath, maxSize);
 		  	volumes.add(mv);
 		  	Collections.sort(volumes);
 	  }

 	 /*******************************/
	  /* Load/save volatile volume info
	   *******************************/

	  public void saveVolumeInfo(Volume v) throws ConfigurationException {
	      if (v == null)
	          throw new ConfigurationException("assertion failure: null volume",logger);
	      PrintWriter output = null;
	      try {
	        output = new PrintWriter( new FileWriter(v.getPath()+File.separator+INFO_FILE) );
	        output.println("# Archiva "+Config.getApplicationVersion()+" Volume Information");
	        output.println("# note: this file is crucial - do not delete it!");
	        output.println("modified:"+convertDatetoString(v.getModified()));
	        output.println("created:"+convertDatetoString(v.getCreated()));
	        output.println("status:"+v.getStatus());

	      } catch (IOException io) {
	          logger.error("failed to write volumeinfo. {"+v.toString()+"} cause:",io);
	      } finally {
	        if (output != null) output.close();
	      }
	    }

	  public void loadAllVolumeInfo() throws ConfigurationException {
	      Iterator i = volumes.iterator();
	      while (i.hasNext()) {
	          Object o = (Object)i.next();
	          if (o!=null) {
	              Volume v = (Volume)o;
	              loadVolumeInfo(v);
	          }
	      }
	  }

	  public void loadVolumeInfo(Volume v) throws ConfigurationException {

	      if (v == null)
	          throw new ConfigurationException("assertion failure: null volume",logger);
	      if (v.getPath().length()<1)
	          throw new ConfigurationException("assertion failure: volume path not set",logger);

	        BufferedReader input = null;
	        String line = null;

	        File file = new File(v.getPath()+File.separator+INFO_FILE);
	        if (!file.exists()) {
	            v.setStatusID(Volume.UNUSED);
	            logger.debug("volumeinfo does not exist (will be created when first message is archived). {"+v+"}");
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
		               if (name.toLowerCase().trim().equals("modified"))
		                   v.setModified(convertStringtoDate(st.nextToken().trim()));
		               else if (name.toLowerCase().trim().equals("created"))
		               	   v.setCreated(convertStringtoDate(st.nextToken().trim()));
		               else if (name.toLowerCase().trim().equals("status"))
		                   v.setStatusNoAssertions(st.nextToken().trim());
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
	        if (v.getStatusID()==Volume.NEW)
	            v.setStatusID(Volume.UNUSED);
	  }


	  public void touch(Volume v) throws ConfigurationException {
	      if (v.getModified()==null) {
	          v.setCreated(new Date());
	      }
	      v.setModified(new Date());
	      saveVolumeInfo(v);
	  }


 	  public void removeVolume(int id) throws ConfigurationException {

 	     Volume v = (Volume)volumes.get(id);
 	     if (v.getStatusID()==Volume.ACTIVE)
 	         throw new ConfigurationException("failed to delete active volume. it must be closed first",logger);
	  	 volumes.remove(v);
	  	Collections.sort(volumes);
 	  }

 	  public int indexOfVolume(Volume v) {
 		  	return volumes.indexOf(v);
 	  }

 	  public List getVolumes() {
 		    return volumes;
 	  }

 	  public Volume getVolume(int index) {
 	  	return (Volume) volumes.get(index);
 	  }

 	  public void setVolumePriority(int id, int priority)  {

 	 	List list = volumes;
 	   	Volume v = (Volume)list.get(id);

 	   	if (v.getStatusID()!=Volume.UNUSED) // can only reorder unused
 	   	    return;

 	   	if (priority==PRIORITY_HIGHER && id-1>=0) { // cannot affect non unused vols
 	   	    Object o = list.get(id-1);
 	   	    Volume vs = (Volume)o;
 	   	    if (vs.getStatusID()!=Volume.UNUSED)
 	   	        return;
 	   	}

 	   	list.remove(v);
 	    switch (priority) {
 		  	    case PRIORITY_HIGHER:   if ((id-1)<=0)
 		  	    							list.add(0,v);
 										else
 											list.add(id-1,v);
 		  	    					 	break;
 		  	    case PRIORITY_LOWER: 	if ((id+1)>=list.size())
 		  	    							list.add(v);
 		  	    						else
 		  	    							list.add(id+1,v);
 		  	                         	break;

 	      }

 	  }

 	   public void clearAllVolumes() {
 	  	 volumes.clear();
 	   }

 	   public Volume getVolume(String uniqueId) throws ConfigurationException {
	  	  if (uniqueId==null)
	  	      throw new ConfigurationException("uniqueid cannot be null",logger);

 	       Date date = null;
	  	  	try {
	  	  	    date = format.parse(uniqueId);
	  	  	} catch (Exception e) {
	  	  	    logger.error("failed to parse uniqueid {id='"+uniqueId+"'}");
	  	  	}
	  	  	Iterator i = volumes.iterator();
	  	  	Volume sv = null;
	  	  	while (i.hasNext()) {
	  	  	    sv = (Volume)i.next();
	  	  	    if (sv.getStatusID()==Volume.UNUSED)
	  	  	        throw new ConfigurationException("failed to lookup volumes {uniqueId='"+uniqueId+"'}",logger);
	  	  	    if (sv != null && sv.getModified()!=null && sv.getModified().compareTo(date)>0)
	  	  	        break;
	  	  	}
	  	  	return sv;
 	   }

 	   public void closeVolume(int index) throws ConfigurationException {
 	       Object o = volumes.get(index);
 	       if (o==null)
 	           throw new ConfigurationException("failed to close volume. no such volume exists",logger);
	   	    Volume vs = (Volume)o;
	   	    vs.setStatus("CLOSED");
	   	    saveVolumeInfo(vs);
	   	    Collections.sort(volumes);
	   	    logger.debug("volume is now closed {"+vs+"}");
 	   }

 	  public Volume  getActiveVolume()  {
		Iterator i = volumes.iterator();
		 Volume volume = null;
		while (i.hasNext()) {
		      volume = (Volume)i.next();
		      if (volume.getStatusID()==Volume.ACTIVE) {
		          return volume;
		      }
		}
		return null;
	  }
 	  /*
 	  public boolean validate(Volume v) {
 	     while (i.hasNext()) {
	          Volume v = (Volume)i.next();

	          if (v.getPath().compareToIgnoreCase(path)==0)
	              throw new ConfigurationException("two or more volumes share the same path");
	          if (v.getIndexPath().compareToIgnoreCase(indexpath)==0)
	              throw new ConfigurationException("two or more volumes share the same index path");


	             (v.getIndexPath().compareToIgnoreCase(indexPath)==0))
	              throw new ConfigurationException("")

	          if (o!=null) {
	              Volume v = (Volume)o;
	              loadVolumeInfo(v);
	          }
	      }*/



 	   public synchronized Volume nextActiveVolume() throws ConfigurationException  {
 	      try {
	 	      Volume activeVolume = getActiveVolume();
	 	     if (activeVolume!=null)
                 activeVolume.setStatusID(Volume.CLOSED); // close old active volume
	 	      Iterator i = volumes.iterator();

	 	      while (i.hasNext()) {
		          Volume volume = (Volume)i.next();
		          if (volume.getStatusID()==Volume.UNUSED) {
		              retrieveDiskSpace(volume);
		              if (volume.enoughDiskSpace()) {
	                      volume.setStatusID(Volume.ACTIVE); // make the new one active
	                      Collections.sort(volumes);
	                      return volume;
		              }
		          }
		      }
 	      } catch (Exception e) {
 	          logger.debug("ERROR: THIS SHOULD NOT HAPPEN!",e);
 	      }
	      throw new ConfigurationException("failed to archive message. there are no volumes available with sufficient diskspace",logger);
 	   }

 	   protected synchronized void retrieveDiskSpace(Volume v) {
          try {
              DiskInfo indexInfo 	= new DiskInfo(v.getIndexPath(),true); //create
              DiskInfo archiveInfo  = new DiskInfo(v.getPath(),true); //create

              logger.debug("index disk space {"+indexInfo+","+v);
              logger.debug("store disk space {"+archiveInfo+","+v);

              v.setFreeArchiveSpace(archiveInfo.getFreeSpace());
              v.setUsedArchiveSpace(archiveInfo.getUsedSpace());
              v.setFreeIndexSpace(indexInfo.getFreeSpace());
              v.setUsedIndexSpace(indexInfo.getUsedSpace());

          } catch (Exception e) {
              logger.error("failed to retrieve disk space. {volumepath='"+v.getPath()+"', indexpath='"+v.getIndexPath()+"'} cause:"+e.toString());
          }
 	   }

 	   public synchronized void startDiskSpaceCheck() {
 	      if (shutdown) {
 	          shutdown = false;
 	          diskInfoWorker.start();
 	      }
 	   }

 	  private class DiskInfoWorker extends Thread {

 		  public DiskInfoWorker() {
 			shutdown = false;
 		  }

 	      public void run() {

 	          while (!shutdown) {
 	              Volume activeVolume = getActiveVolume();
 	              if (activeVolume!=null)
 	                  retrieveDiskSpace(activeVolume);

 	              activeVolume.enoughDiskSpace(); // warning

 	          }
 	          try { Thread.sleep(DISK_SPACE_WORKER_WAIT); } catch(Exception e) {}
 	      }
 	 	}

	 	 protected void finalize() throws Throwable {
	 	    super.finalize();
	 	    shutdown = true;
	 	    diskInfoWorker.interrupt();
	 	 }

		  protected String convertDatetoString(Date date) {
		  	  return format.format(date);
		  }

		  protected Date convertStringtoDate(String str) throws Exception {
		      return format.parse(str);
		  }


}
