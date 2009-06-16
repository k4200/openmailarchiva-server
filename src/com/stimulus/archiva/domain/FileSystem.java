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

package com.stimulus.archiva.domain;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.ServletException;
import com.stimulus.util.*;
import org.apache.commons.logging.*;

public class FileSystem {

	protected Log 	logger = LogFactory.getLog(FileSystem.class);
    protected TempFiles tempFiles = null;
    protected String 	applicationPath = null;
    protected String 	auditPath;
    protected String 	loggingPath;
    protected String 	loggingLevel;
    protected String 	installDirectory;
    protected static String productName = "MailArchiva";

    public FileSystem() {
    	 tempFiles = new TempFiles();
       	 tempFiles.startDaemon();
    }


    public static String getProductName() {
    	return productName;
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
    	String os =  System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch");

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
    		logger.error("server is unable to access the "+dirname+" path {path='"+path+"'}");
    		logger.error("make sure this directory exists and check the permissions on it.");
    		return false;
    	}
    }

    public String getDir(String path) {
    	File dir = new File(path);
    	if (!dir.exists())
    		dir.mkdirs();
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

    public String getApplicationName() {
    	if (applicationPath==null) {
    		logger.error("attempt to build application name, but application path has a null value!");
    		return productName;
    	}
    	int dotpos = applicationPath.lastIndexOf(File.separator);
    	if (dotpos==-1) dotpos = 0;
    	return applicationPath.substring(dotpos+1,applicationPath.length());
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
 	   return System.getProperty("catalina.home")+File.separator+"logs"+File.separator+Config.getFileSystem().getApplicationName()+"_debug.log";
    }

    public String getAuditLogPath() {
 	   return System.getProperty("catalina.home")+File.separator+"logs"+File.separator+Config.getFileSystem().getApplicationName()+"_audit.log";
    }

    public String getKeyStorePath() {
    	return System.getProperty("catalina.home")+File.separator+".keystore";
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


    public void initCrypto() {
    	if (java.security.Security.getProvider("BC") == null) {
    		logger.debug("bouncycastle crypto api not installed. installing...");
    		java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider ());
    	}
    }


    public void clearTempDirectory() {
	    	  String tmpDir = getTempPath();
	    	  logger.debug("clearing temporary directory {directory='"+tmpDir+"'}");
	    	  clearDirectory(tmpDir);
    }

    public boolean checkStartupPermissions() {
    	logger.debug("checking for required file and directory permissions...");
    	boolean config = checkReadWriteDeleteAccess(getConfigurationPath());
    	boolean view = checkReadWriteDeleteAccess(getViewPath());
    	boolean noarchive = checkReadWriteDeleteAccess(getNoArchivePath());
    	boolean quarantine = checkReadWriteDeleteAccess(getQuarantinePath());
    	boolean log = checkReadWriteDeleteAccess(getLogPath());
    	boolean success = config && view && noarchive && quarantine && log;
    	if (!success) {
    		logger.fatal("file and directory permissions failed.");
    		logger.fatal("for this server to function correctly, the file and directory permissions must be adjusted.");
    	} else {
    		logger.debug("file and directory permissions ok");
    	}
    	return success;
    }

    public boolean checkReadWriteDeleteAccess(String dirLocation) {
    	logger.debug("checking file permissions (dirLocation='"+dirLocation+"'}");
    	boolean read = true;
    	boolean write = true;
    	boolean delete = true;
    	BufferedWriter out = null;

    	if (!new File(dirLocation).exists()) {
    		logger.error("directory does not exist {dir='"+dirLocation+"'");
    		return false;
    	}

    	File testFile = new File(dirLocation+File.separator+"writetest");
    	try {
    		FileWriter fwrite = new FileWriter(testFile);
    		out = new BufferedWriter(fwrite);
    	    out.write("test");
    	    out.close();
    	} catch (IOException io) {
    		logger.error("write permission test failed. please enable write access to "+dirLocation+".");
    		read = false;
    	} finally {
    		try { if (out!=null) out.close(); } catch (Exception e) {}
    	}
    	BufferedReader in = null;
    	try {
    	    FileReader fread = new FileReader(testFile);
    	    in = new BufferedReader(fread);
    	    in.readLine();
    	} catch (IOException io) {
    		logger.error("read permission test failed. please enable read access to "+dirLocation+".");
    		write = false;
    	} finally {
    		try { if (in!=null) in.close(); } catch (Exception e) {}
    	}
    	try {
    		delete = testFile.delete();
    	} catch (Exception e) {
    		delete = false;
    	}
    	if (!delete) {
    		logger.error("delete permission test failed. please allow delete access to  "+dirLocation+".");
    	}
    	boolean success = read && write && delete;
    	if (!success) {
    		logger.debug("permission check failed (dirLocation='"+dirLocation+"'}");
    	} else {
    		logger.debug("permission check ok (dirLocation='"+dirLocation+"'}");
    	}
    	return read && write && delete;
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
