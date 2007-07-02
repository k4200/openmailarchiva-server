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
 * Founda tion, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.stimulus.util;
import java.util.*;
import java.io.*;

import org.apache.log4j.Logger;
import com.stimulus.archiva.exception.*;

public class DiskInfo {

    protected String path;
    protected long freeSpace = 0;
    protected long usedSpace = 0;
    protected static Logger logger = Logger.getLogger(DiskInfo.class.getName()); 
    
    public DiskInfo(String path, boolean create) throws ArchivaException {
        File dir = new File(path);
        if (!dir.exists()) {
            if (!create) 
                throw new ArchivaException("path does not exist {path='"+path+"'",logger);
            else
                dir.mkdir();
        }
                      
        if (!dir.isDirectory())
            throw new ArchivaException("path does not reference a directory {path='"+path+"'",logger);
        
        if (System.getProperty("os.name").startsWith("Windows")) {
    	    retrieveSpaceOnWindows(path);
    	}
    	if (System.getProperty("os.name").startsWith("Linux")) {
    	    retrieveSpaceOnLinux(path);
    	}
    }
    
    public long getFreeSpace() { return freeSpace; }
    public long getUsedSpace() { return usedSpace; }
     

	private void retrieveSpaceOnWindows(String path) throws ArchivaException {
	    try {
		    File script = new File(System.getProperty("java.io.tmpdir"),"script.bat");
		    PrintWriter writer = new PrintWriter(new FileWriter(script, false));
		    writer.println("dir \"" + path + "\" /w/s"); 
		    writer.close();
		    Process p = Runtime.getRuntime().exec(script.getAbsolutePath());
		    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		    boolean marker = false;
		    
		    while (true) {
		        String line = reader.readLine();
		        
		        if (line==null)
		            break;
		        logger.debug("line:"+line);
		        if (marker) {
		            if (line.endsWith("bytes free")) {
			            StringTokenizer tokenizer1 = new StringTokenizer(line, " ");
			            String token = tokenizer1.nextToken();
			            token = tokenizer1.nextToken();
			            freeSpace = Long.parseLong(tokenizer1.nextToken().replaceAll(",", "")) / 1024 / 1024;
		            }
			        if (line.endsWith("bytes")) {
			            StringTokenizer tokenizer2 = new StringTokenizer(line, " ");
			            String token = tokenizer2.nextToken();
			            token = tokenizer2.nextToken();
			            usedSpace = Long.parseLong(tokenizer2.nextToken().replaceAll(",", "")) / 1024 / 1024;
			        }
		        } else if (line.indexOf("Total Files Listed:")>0) 
		           marker=true;   
		    }
		    logger.debug("diskspace {free='"+freeSpace+"', used='"+usedSpace+"'");
		    reader.close();
	
	    } catch (Exception e) { 
	        throw new ArchivaException(e.getMessage(),e,logger); 
	    }
	 }

	private void retrieveSpaceOnLinux(String path) throws ArchivaException {
	    try {
	        
		    Process p = Runtime.getRuntime().exec("df -B1 " + "/" + path);
		    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		    reader.readLine();
		    reader.readLine();
		    String line = reader.readLine();
		    logger.debug("free space {line='"+line+"'}");
		    String[] tokens = line.split(".\\b");
		    for (int i=0;i<tokens.length;i++) {
		        logger.debug("free token ("+i+") {token='"+tokens[i]+"'}");
		    }
		    freeSpace = Long.parseLong(tokens[5]) / 1024 / 1024;
		    reader.close();
		    
		    // used space
		    
		    Process p2 = Runtime.getRuntime().exec("du -B1 -s " + "/" + path);
		    BufferedReader reader2 = new BufferedReader(new InputStreamReader(p2.getInputStream()));
		    String line2 = reader2.readLine();
		    logger.debug("free space {line='"+line2+"'}");
		    String[] tokens2 = line2.split("\\t");
		    for (int i=0;i<tokens2.length;i++) {
		        logger.debug("used token ("+i+") {token='"+tokens2[i]+"'}");;
		    }
		    usedSpace = Long.parseLong(tokens2[0]) / 1024 / 1024;
		    reader2.close();
		    
		    logger.debug("diskspace {free='"+freeSpace+"', used='"+usedSpace+"'}");
		  
	    } catch (Exception e) {
	        throw new ArchivaException(e.getMessage(),e,logger); 
	    }
	 }
	
	public String toString() {
	    return "usedspace='"+usedSpace+"mb', freespace='"+freeSpace+"mb'";
	}
}

