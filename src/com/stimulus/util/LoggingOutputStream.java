package com.stimulus.util;

import java.io.ByteArrayOutputStream; 
import java.io.IOException; 
import org.apache.commons.logging.*;
 
/** 
 * An OutputStream that writes contents to a Logger upon each call to flush() 
 */ 
public class LoggingOutputStream extends ByteArrayOutputStream { 
 
 private String lineSeparator; 
 
 private Log logger; 
 
 
 /** 
 * Constructor 
 * @param logger Logger to write to 
 * @param level Level at which to write the log message 
 */ 
 public LoggingOutputStream(Log logger) { 
	 super(); 
	 this.logger = logger; 
	 lineSeparator = System.getProperty("line.separator"); 
 } 
 
 /** 
 * upon flush() write the existing contents of the OutputStream
 * to the logger as a log record. 
 * @throws java.io.IOException in case of error 
 */ 
 public void flush() throws IOException { 
 
	 String record; 
	 synchronized(this) { 
		 super.flush(); 
		 record = this.toString(); 
		 super.reset(); 
		 
		 if (record.length() == 0 || record.equals(lineSeparator)) { 
			 // avoid empty records 
			 return; 
		 } 
		 
		 logger.debug(record); 
	} 
 } 
} 