package com.stimulus.archiva.incoming;


import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.apache.log4j.Logger;

import com.stimulus.archiva.exception.MaxMessageSizeException;
import com.stimulus.archiva.exception.SMTPEndStreamException;

public class SMTPInputStream extends InputStream
{
   private Reader isr;
   private int buffer[] = new int[5];
   private boolean end = false;
   private static Logger logger = Logger.getLogger(SMTPInputStream.class);
   private long size = 0;
   private long maxsize = 0;
   
   public SMTPInputStream(Reader isr, long maxsize) throws IOException,SMTPEndStreamException {
	 logger.debug("smtpinputstream() started");
     this.isr = isr;
     size = 5;
     this.maxsize = maxsize * 1024 * 1024;
     push(13);
     push(10);
     for (int i=0;i<4;i++) {
    	 push(isr.read());
    	 if (CRLFDotCRLF())
        	 throw new SMTPEndStreamException("end of stream",logger);
    	 transparent();
     }
   }

   public int read() throws IOException {
	   size += 1;
	   if (size>maxsize) {
		   while (readp()!=-1) {}
		   logger.warn("smtp maximum message size exceeded { size='"+size+" bytes'}");
		   throw new MaxMessageSizeException("maximum message size is exceeded",logger);
	   }
	   return readp();
	  
   }
   
   protected int readp() throws IOException {
	   if (end) return -1;
	   int i= isr.read();
	   push(i); 
	   if (i==-1 || CRLFDotCRLF())
    	  return end();
	   transparent();
       return buffer[4];
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
	   //bufout();
   }
   
   protected void bufout() {
	   String out = "";
	   for (int j=0;j<5;j++)
		   out += buffer[j]+" ";
	  System.out.println("buf:"+out);
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
		    //System.out.println("new buffer");
		    //bufout();
		    
	   }
   }
 
}