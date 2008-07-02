/*
 * $Id: TooMuchDataException.java 6 2006-06-03 22:42:58Z lhoriman $
 * $Source: /cvsroot/Similarity4/src/java/com/similarity/mbean/BindStatisticsManagerMBean.java,v $
 */
package org.subethamail.smtp;

import java.io.IOException;

/**
 * Thrown by message listeners if an input stream provides more data than the
 * listener can handle.
 * 
 * @author Jeff Schnitzer
 */
@SuppressWarnings("serial")
public class TooMuchDataException extends IOException
{
	/** */
	public TooMuchDataException()
	{
		super();
	}

	/** */
	public TooMuchDataException(String message)
	{
		super(message);
	}
}
