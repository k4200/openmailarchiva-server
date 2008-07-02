package com.stimulus.archiva.exception;

import java.io.IOException;

import org.apache.log4j.Logger;



public class MaxMessageSizeException extends IOException {

	protected static Logger logger = Logger.getLogger(MaxMessageSizeException.class.getName());
	
	public MaxMessageSizeException(String message, Logger logger) {
	    super(message);
	    logger.debug(message); 
	 }
}
