/*
 * $Id: MessageHandlerFactory.java 72 2006-09-20 08:36:18Z lhoriman $
 * $URL: http://subethasmtp.tigris.org/svn/subethasmtp/trunk/smtp/src/org/subethamail/smtp/MessageHandlerFactory.java $
 */
package org.subethamail.smtp;


/**
 * The primary interface to be implemented by clients of the SMTP library.
 * This factory is called for every message to be exchanged in an SMTP
 * conversation.  If multiple messages are transmitted in a single connection
 * (via RSET), multiple handlers will be created from this factory.
 * 
 * @author Jeff Schnitzer
 */
public interface MessageHandlerFactory
{
	/**
	 * Called for the exchange of a single message during an SMTP conversation.
	 * 
	 * @param ctx provides information about the client.
	 */
	public MessageHandler create(MessageContext ctx);

}
