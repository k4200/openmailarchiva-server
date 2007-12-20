/*
 * Subversion Infos:
 * $URL$
 * $Author$
 * $Date$
 * $Rev$
*/

		
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


package com.stimulus.archiva.extraction;
import java.io.BufferedReader;
import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

import com.stimulus.archiva.domain.Config;
import com.stimulus.archiva.exception.ExtractionException;
import com.stimulus.util.TempFiles;
public class WordExtractor implements TextExtractor, Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2150155650657302354L;
	protected static Logger logger = Logger.getLogger(Extractor.class);
	


 public WordExtractor() {
 }


 protected class WordDocInputStream extends FilterInputStream implements Serializable
 {
 	/**
	 * 
	 */
	private static final long serialVersionUID = 9036662460208941073L;
	File tempFile;

 	public WordDocInputStream(File tempFile, InputStream in) {
 	    super(in);
 	    this.tempFile = tempFile;
 	}

 	public void close() throws IOException
 	{
 		in.close();
 		tempFile.delete();
 		logger.debug("word document is processed. deleting temporary file.");
 	}

 	public int read() throws IOException {
 	    return in.read();
 	}
 	
 	public int read(byte[] b) throws IOException {
 	    return in.read(b);
 	}
 	
 	public int read(byte[] b,int off,int len) throws IOException {
 	    return in.read(b,off,len);
 	}
 	
 }
 

 class StreamGobbler extends Thread
 {
     InputStream is;
     String type;

     StreamGobbler(InputStream is, String type)
     {
         this.is = is;
         this.type = type;
    	 setDaemon(true);
     }

     public void run()
     {
    
    	 setName("WordExtractionGobbler");
    	 try
         {
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(isr);
             String line=null;
             while ( (line = br.readLine()) != null)
                logger.debug(type + ">" + line);
             } catch (IOException ioe)
               {
                 ioe.printStackTrace();
               }
     }
 }



 public Reader getText(InputStream is, TempFiles tempFiles) throws ExtractionException
 {
 	String filepath = "";
 	try {
 		filepath = Extractor.writeTemp(is,tempFiles);
 		String[] commands = null;
 		boolean windows =(System.getProperty("os.name").toLowerCase(Locale.ENGLISH).indexOf("windows") != -1);
 		ProcessBuilder pb = null;
 		if (!windows)
 			pb = new ProcessBuilder(Config.getBinPath() +File.separatorChar + ".antiword" +File.separatorChar + "antiword", "-t","-m","8859-1.txt",filepath);
 		else // windows
 			pb = new ProcessBuilder("\""+Config.getBinPath()  +File.separatorChar + "antiword" + File.separatorChar + "antiword.exe"+"\"", "-t", "-m","8859-1.txt","\""+filepath+"\"");

 		Map<String, String> env = pb.environment();
 		env.put("HOME", Config.getBinPath());
 		pb.directory(new File(Config.getBinPath()+File.separatorChar+"antiword"));
 	    Process process = pb.start();
 	    StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), "ERROR");
 	    errorGobbler.start();
 	    int exitVal = 0;
 	    //try {
 	    	List<String> cmdlist = pb.command();
 	    	String cmdStr = "";
 	    	for(String cmd: cmdlist) {
 	    		cmdStr += cmd + " "; 
 	    	}
 	    	logger.debug("exec() {basedir='"+pb.directory()+"',env='"+env.get("HOME")+"',cmd='"+cmdStr+"'}");
 	    	logger.debug("waiting for process to complete");
 	    	//exitVal = process.waitFor();
 	    	//logger.debug("process complete");
         //} catch (InterruptedException ie) {
        	// logger.debug("interrupted exec command. Cause:"+ie.toString(),ie);
        // }
		//logger.debug("exit value: " + exitVal);
 	   
 		WordDocInputStream textin = new WordDocInputStream(new File(filepath),process.getInputStream());
 		logger.debug("returning stream containing converted word text for search indexing");
 		return new InputStreamReader(textin);
 		//return new StringReader("shuttle");
 	} catch (IOException io) {
 		io.printStackTrace();
 		throw new ExtractionException("failed to extract text from word document {filename='" + filepath + "'}",io,logger);
 	}
 	//return new StringReader("");
 }




}
