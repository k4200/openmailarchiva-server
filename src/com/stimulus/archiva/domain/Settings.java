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
import java.io.*;
import java.util.Map;
import org.apache.commons.logging.*;
import com.stimulus.archiva.exception.ConfigurationException;


public class Settings extends java.util.LinkedHashMap<String,String> {
 
	private static final long serialVersionUID = 1598672170612192642L;
	private static final char keyValueSeparator='=';

    protected static Log logger = LogFactory.getLog(Settings.class);

    public static Settings loadProperties (final String name, String charset) throws ConfigurationException
    {
    	Settings result = null;
    	BufferedReader input = null;
  
        try {
  	      result = new Settings ();
  	      input = new BufferedReader(new InputStreamReader(new FileInputStream(name),charset));
  	      result.load(input);
  
        } catch (Exception e)
        {
        	  logger.info("configuration file not found, where location='WEB-INF/conf/server.conf'");
            result = new Settings ();
        }
        finally
        {
            if (input != null) try { input.close (); } catch (Throwable ignore) {}
        }
  
        return result;
    }
    ////"ISO-8859-1"
  
    public static void saveProperties( final String name, String intro, Settings prop, String charset) throws ConfigurationException {
    	File f = null;
    	try {
    		// if the disk is full we dont want to end up in a situation where we delete
    		// server.conf file
        	f = File.createTempFile("server_conf",".tmp");
            prop.store(intro, new FileOutputStream(f),charset);
          
        } catch (Exception e) {
        	if (f!=null)
        		f.delete();
            throw new ConfigurationException("failed to save properties. cause:"+e.toString(),e, logger);
        }
        File newFile = new File(name);
        newFile.delete();
        f.renameTo(newFile);
    }
  
    
   public void load(BufferedReader input) throws IOException {
     String line;
      boolean oldVersion = true; // deal with legacy properties file
      while((line=input.readLine())!=null) {
          int pos=line.indexOf(keyValueSeparator);
          if(!line.startsWith("#") && pos>=0) {
       	   String key = line.substring(0,pos).trim();
       	   String value = line.substring(pos+1).trim();
       	   if (Compare.equalsIgnoreCase(key, "version"))
       		   oldVersion = false;
       	   if (oldVersion)
       		   value = value.replace("\\:",":").replace("\\\\","\\").replace("\\=","="); // legacy
            put(key,value);
         }
      }
      input.close();    
   }
  
   public void store(String intro, OutputStream out, String charset) throws IOException {
      BufferedWriter output= new BufferedWriter(new OutputStreamWriter(out,charset));
      output.append(intro);
      
      for(Map.Entry<String,String> property:entrySet()){
         output.append(property.getKey());
         output.append(keyValueSeparator);
         output.append(property.getValue());
         output.newLine();
      }
      output.close();
   }
   
   public String getProperty(String key) { return get(key); }
   
   public void setProperty(String key, String value) { put(key, value); } 
   
 
}
