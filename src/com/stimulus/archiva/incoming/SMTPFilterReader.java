package com.stimulus.archiva.incoming;


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


import java.io.IOException;
import java.io.FilterReader;
import java.io.Reader;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import com.stimulus.archiva.exception.MaxMessageSizeException;
import com.stimulus.archiva.exception.SMTPEndStreamException;

public class SMTPFilterReader extends FilterReader {
   private Reader isr;
   private int buffer[] = new int[5];
   private boolean end = false;
   private static Logger logger = Logger.getLogger(SMTPFilterReader.class);
   private long size = 0;
   private long maxsize = 0;
   
   public SMTPFilterReader(Reader isr, long maxsize) throws IOException,SMTPEndStreamException {
	 super(isr);
	 logger.debug("smtpinputstream() construct");
     this.isr = isr;
     size = 5;
     this.maxsize = maxsize * 1024 * 1024;
     push(13);
     push(10);
     for (int i=0;i<4;i++) {
    	 push(isr.read());
    	 if (CRLFDotCRLF())
        	 throw new SMTPEndStreamException("end of stream",logger,Level.DEBUG);
    	 transparent();
     }
     logger.debug("smtpinputstream() constructor end");
   }

   @Override
public int read() throws IOException {
	   if (end) return -1;
	   size += 1;
	   if (size>maxsize) {
		   while (readp()!=-1) {}
		   logger.warn("smtp maximum message size exceeded { size='"+size+" bytes'}");
		   throw new MaxMessageSizeException("maximum message size is exceeded",logger);
	   }
	   return readp();
	  
   }
   
   @Override
public boolean markSupported() {
	   return false;
   }
   
   protected int readp() throws IOException {
	   if (end) return -1;
	   int i= isr.read();
	   push(i); 
	   bufout();
	   if (i==-1 || CRLFDotCRLF())
    	  return end();
	   transparent();
       return buffer[4];
   }
   
   @Override
public int read(char[] cbuf,int off,int len) throws IOException {
	   if (end) return -1;
	   int i;
	   for (i=0; i<len; i++) {
		   int c = read();
		   //System.out.print(c);
		   if (c==-1) {
			   break;
		   }
		   cbuf[off+i]=(char)c;
		   //System.out.print(cbuf[off+i]);
	   }
	   return i;
   }
   
   protected int end() {
	   end = true;
	   logger.debug("smtpinputstream() end");
	   return -1;
   }
   
   public int available() throws IOException { return 0; } 
  
   protected void push(int t) {
	 
	   for (int i=3;i>=0;i--)
		   buffer[i+1] = buffer[i];
	   buffer[0] = t;
   }
   
   protected void bufout() {
	   String out = "";
	   for (int j=0;j<5;j++)
		   out += buffer[j]+" ";
   }
   protected int pop() {
	   return buffer[4];
   }
   
   protected boolean CRLFDotCRLF() {
	   if (buffer[0]==10 &&
		   buffer[1]==13 &&
		   buffer[2]==46 &&
		   buffer[3]==10  &&
		   buffer[4]==13) {
		   		logger.debug("dot received"); 
		   		return true;
		   } else {
			   return false;
		   }
   }
   
   protected void transparent() throws IOException {
	   if (buffer[0]==13 &&
    	   buffer[1]==46 &&
    	   buffer[2]==46 &&
    	   buffer[3]==10 &&
    	   buffer[4]==13) {
		    buffer[2] = buffer[3];
		    buffer[3] = buffer[4];
		    push(isr.read());
		    logger.debug("transparent marker received"); 
	   }
   }
   
   @Override
public void close()throws IOException {
	   end = true;
   }
 
}
