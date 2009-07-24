package com.stimulus.archiva.exception;

import java.io.IOException;

import org.apache.commons.logging.*;



public class MaxMessageSizeException extends IOException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2298840703783985434L;
	protected static Log logger = LogFactory.getLog(MaxMessageSizeException.class.getName());
	
	public MaxMessageSizeException(String message, Log logger) {
	    super(message);
	    logger.debug(message); 
	 }
}
