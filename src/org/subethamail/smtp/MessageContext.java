/*
 * $Id: MessageContext.java 146 2008-01-08 01:01:00Z ted_kods $
 * $URL: http://subethasmtp.tigris.org/svn/subethasmtp/trunk/smtp/src/org/subethamail/smtp/MessageContext.java $
 */
package org.subethamail.smtp;

import org.subethamail.smtp.auth.Credential;
import org.subethamail.smtp.server.BaseContext;


/**
 * Interface which provides context to the message handlers.
 * 
 * @author Jeff Schnitzer
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public interface MessageContext extends BaseContext
{
	/**
	 * @return the logged identity. Can be null if connection is still in
	 * authorization state or if authentication isn't required. 
	 */
	public Credential getCredential();	
}
