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

package com.stimulus.archiva.persistence.textextraction;
import com.stimulus.archiva.domain.Config;
import com.stimulus.archiva.exception.ExtractionException;
import java.io.*;

import com.stimulus.util.*;

import org.apache.log4j.Logger;

public class WordExtractor implements TextExtractor
{
	protected static final Logger logger = Logger.getLogger(Extractor.class.getName());
	


 public WordExtractor() {
 }


 protected class WordDocInputStream extends FilterInputStream
 {
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
     }

     public void run()
     {
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

 protected static String writeTemp(InputStream is,TempFiles tempFiles) throws IOException {
     	File file = File.createTempFile("extract", ".tmp");
     	tempFiles.markForDeletion(file);
		logger.debug("writing temporary file for text extraction {filename='"+file.getPath()+"'}");
		OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
		BufferedInputStream bis = new BufferedInputStream(is);
		int c;
		while ((c = bis.read()) != -1)
		    os.write(c);
		os.close();
		return file.getPath();
 }

 public Reader getText(InputStream is, TempFiles tempFiles) throws ExtractionException
 {
 	String filepath = "";
 	try {
 		filepath = writeTemp(is,tempFiles);
 		String[] commands = null;
 		boolean windows =(System.getProperty("os.name").toLowerCase().indexOf("windows") != -1);
 		if (!windows)
 		   commands = new String[]{Config.getBinPath() +File.separatorChar + ".antiword" +File.separatorChar + "antiword", "-t","-m","8859-1.txt",filepath};
 		else // windows
 		    commands = new String[]{Config.getBinPath()  +File.separatorChar + "antiword" + File.separatorChar + "antiword.exe", "-t","-m","8859-1.txt",filepath};

 		String[] env = new String[]{"HOME="+ Config.getBinPath()};
 		if (logger.isDebugEnabled()) {

 			String cmd = "";
 			for (int i=0;i<commands.length;i++)
 				cmd+=commands[i]+" ";
 			logger.debug("received word doc, executing word conversion command:"+cmd);


	 		Process debugProc = Runtime.getRuntime().exec(commands,env);
	 		 // any error message?
            StreamGobbler errorGobbler = new StreamGobbler(debugProc.getErrorStream(), "word extraction error");

            // any output?
            StreamGobbler outputGobbler = new StreamGobbler(debugProc.getInputStream(), "word document");

            // kick them off
            errorGobbler.start();
            outputGobbler.start();
            int exitVal = 0;
            // any error???
            try {
            exitVal = debugProc.waitFor();
            } catch (InterruptedException ie) {
            	logger.debug("interrupted exec command. Cause:"+ie.toString(),ie);

            }
            logger.debug("word doc conversion process has exited {exitvalue='" + exitVal+"'}");
 		}

 		Process proc = Runtime.getRuntime().exec(commands,env);
 		WordDocInputStream textin = new WordDocInputStream(new File(filepath),proc.getInputStream());
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
