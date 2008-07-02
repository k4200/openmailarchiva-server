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

import java.io.*;
import java.util.Locale;

import com.stimulus.util.*;

import org.apache.log4j.Logger;

public class FileSystem {

	protected Logger 	logger = Logger.getLogger(FileSystem.class);
    protected TempFiles tempFiles = null;
    protected String 	applicationPath = null;
    protected String 	auditPath;
    protected String 	loggingPath;
    protected String 	loggingLevel;
    protected String 	installDirectory;
    
 
    public FileSystem() {
    	 tempFiles = new TempFiles();
       	 tempFiles.startDaemon();
    }

    public void shutdown() {
    	tempFiles.stopDaemon();
    }

    @Override
	protected void finalize() throws Throwable {
    	shutdown();
    	super.finalize();
    }

    public void outputSystemInfo() {

    	String javavm =  System.getProperty("java.version")+" "+System.getProperty("java.vendor")+" " + System.getProperty("java.vm.name")+" "+System.getProperty("java.vm.version");
    	String os =  System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("system.arch");
    	
    	logger.debug("java runtime environment (JRE) {javaversion='"+javavm+"'}");
    	logger.debug("guest operating system {os='"+os+"'}");
    	logger.debug("java home {java.home='"+System.getProperty("java.home")+"'}");	
    }
    
    public TempFiles getTempFiles() { return tempFiles; } 
    
    public void setApplicationPath(String applicationPath) {

        if (applicationPath.endsWith(Character.toString(File.separatorChar)))
            this.applicationPath = applicationPath.substring(0,applicationPath.length()-1);
        else
            this.applicationPath = applicationPath;
        logger.debug("setApplicationPath {path='"+applicationPath+"'}");
    }
  
    public boolean checkPath(String path, String dirname) {
    	if (new File(path).exists() && new File(path).isDirectory()) {
    		logger.debug(dirname+" path is present {path='"+Config.getFileSystem().getNoArchivePath()+"'}");
    		return true;
    	} else {
    		logger.error("mailarchiva is unable to access the "+dirname+" path {path='"+path+"'}");
    		logger.error("make sure this directory exists and check the permissions on it.");
    		return false;
    	}
    }
    
    public String getDir(String path) {
    	File dir = new File(path);
    	if (!dir.exists())
    		dir.mkdir();
	    return path;
    }
    
    public boolean checkAllSystemPaths() {
    	
    	boolean check = true;
    	
    	check = check && checkPath(getApplicationPath(),"application");
    	check = check && checkPath(getConfigurationPath(),"configuration");
    	check = check && checkPath(getViewPath(),"view");
    	check = check && checkPath(getNoArchivePath(),"noarchive");
    	check = check && checkPath(getQuarantinePath(),"quarantine");
    	check = check && checkPath(getClassesPath(),"classes");
    	check = check && checkPath(getLogPath(),"debug log");
    	return check;
    	
    }
    public String getApplicationPath() {
        return applicationPath;
    }
  
    public String getConfigurationPath() {
        return getDir(applicationPath + File.separatorChar + "WEB-INF"+File.separatorChar + "conf");
    }
  
    public String getViewPath() {
        return getDir(applicationPath + File.separatorChar + "temp");
    }
  
  
    public String getBinPath() {
        return applicationPath + File.separatorChar + "WEB-INF"+File.separatorChar + "bin";
    }
    
    public String getNoIndexPath() {
  	  return getDir(applicationPath + File.separatorChar + "WEB-INF"+File.separatorChar + "noindex");
    }
    
    public String getNoArchivePath() {
  	  return getDir(applicationPath + File.separatorChar + "WEB-INF"+File.separatorChar + "noarchive");
    }
    
    public String getQuarantinePath() {
  	  return getDir(applicationPath + File.separatorChar + "WEB-INF"+File.separatorChar + "quarantine");
    }
  
    public String getClassesPath() {
  	  return applicationPath + File.separatorChar + "WEB-INF"+ File.separatorChar + "classes";
    }
    
    public String getLogPath() {
    	return getDir(System.getProperty("catalina.home")+File.separator+"logs");
    }
    
    public String getDebugLogPath() {
 	   return System.getProperty("catalina.home")+File.separator+"logs"+File.separator+"debug.log";
    }
    
    public String getAuditLogPath() {
 	   return System.getProperty("catalina.home")+File.separator+"logs"+File.separator+"audit.log";
    }
    
  
    public void setLoggingPath(String loggingPath) { this.loggingPath = loggingPath.toLowerCase(Locale.ENGLISH); }
    
    public String getLoggingPath() { return loggingPath; }
  
    public void setLoggingLevel(String loggingLevel) { this.loggingLevel = loggingLevel; }
  
    public String getLoggingLevel() { return loggingLevel; }
  
    public void setAuditPath(String auditPath) { this.auditPath = auditPath.toLowerCase(Locale.ENGLISH); }
  
    public String getAuditPath() { return auditPath; }
    
    
  
    public void clearViewDirectory() {
        if (applicationPath==null)
            return;
        logger.debug("clearing view directory {directory='"+getViewPath()+"'}");
        clearDirectory(getViewPath());
    }
    
    public void initLogging() {
    	//PrintStream stdout = System.out;                                       
        //PrintStream stderr = System.err;                                       
        LoggingOutputStream los;                                               
        los = new com.stimulus.util.LoggingOutputStream(logger);          
        System.setOut(new PrintStream(los, true));           
        System.setErr(new PrintStream(los, true));       
    }
    
    
    public void initTempDirectory() {
  	  logger.debug("initializing temp directory");
  	  String tmpDir = System.getProperty("java.io.tmpdir");
  	  if (tmpDir.charAt(tmpDir.length()-1)==File.separatorChar)
  		  tmpDir = tmpDir.substring(0,tmpDir.length()-1);
  	  if (tmpDir==null || tmpDir.length()<2) {
  		  System.setProperty("java.io.tmpdir", File.separatorChar + "tmp" + File.separatorChar + "mailarchiva");
  	  } else {
  		  if (!tmpDir.contains("mailarchiva")) {
  			  System.setProperty("java.io.tmpdir",tmpDir + File.separatorChar + "mailarchiva"); 
  		  }
  	  }    
  	  File tmpDirFile = new File(System.getProperty("java.io.tmpdir"));
  	  if (!tmpDirFile.exists()) {
  		  logger.debug("creating temp directory {path='"+tmpDirFile.getAbsolutePath()+"'}");
  		  tmpDirFile.mkdirs();
  	  }
    }
  	
    public void clearTempDirectory() {
	    	  String tmpDir = getTempPath();
	    	  logger.debug("clearing temporary directory {directory='"+tmpDir+"'}");
	    	  clearDirectory(tmpDir);
    }
    
    public String getTempPath() {
  	  return System.getProperty("java.io.tmpdir");
    }
    
    //deliberately non recursive
    public void clearDirectory(String path) {
        File indexDir = new File(path);
        if (!indexDir.exists()) return;
        if (indexDir.isDirectory()) {
              String[] children = indexDir.list();
              for (int i=0; i<children.length; i++) {
                  String filepath = null;
                  if (path.charAt(path.length()-1)==File.separatorChar)
                  	filepath = path + children[i];
                  else
                  	filepath = path + File.separatorChar + children[i];
             
                  logger.debug("deleting file {path='" + filepath +"'}");
                  boolean success = new File(filepath).delete();
                  if (!success)
                      logger.debug("failed to delete file {path='" + filepath +"'}");
                   else
                       logger.debug("deleted file successfully {path='" + filepath +"'}");
              }
        }
    }
    
}
