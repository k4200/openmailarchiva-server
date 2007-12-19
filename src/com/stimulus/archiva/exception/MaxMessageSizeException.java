package com.stimulus.archiva.exception;

import java.io.IOException;

import org.apache.log4j.Logger;



public class MaxMessageSizeException extends IOException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5802371090445932662L;
	protected static Logger logger = Logger.getLogger(MaxMessageSizeException.class);
	
	public MaxMessageSizeException(String message, Logger logger) {
	    super(message);
	    logger.debug(message); 
	  }
}
