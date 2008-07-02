/*
 * $Id: AbstractMessageHandler.java 140 2007-11-06 16:49:53Z jon $
 * $URL: http://subethasmtp.tigris.org/svn/subethasmtp/trunk/smtp/src/org/subethamail/smtp/server/AbstractMessageHandler.java $
 */
package org.subethamail.smtp.server;

import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.RejectException;

/**
 * A simple base class to make implementing message handlers easier.  It
 * also makes modification of the interface class easier on users.
 * 
 * @author Jeff Schnitzer
 */
abstract public class AbstractMessageHandler implements MessageHandler
{
	/*
	 * (non-Javadoc)
	 * @see org.subethamail.smtp.MessageHandler#from(java.lang.String)
	 */
	public void from(String from) throws RejectException
	{
	}

	/*
	 * (non-Javadoc)
	 * @see org.subethamail.smtp.MessageHandler#recipient(java.lang.String)
	 */
	public void recipient(String recipient) throws RejectException
	{
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.subethamail.smtp.MessageHandler#resetMessageState()
	 */
	public void resetMessageState()
	{
	}
}
