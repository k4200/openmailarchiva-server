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
package com.stimulus.archiva.domain;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.*;

import com.stimulus.util.*;

public class MailboxConnections implements Props {
	
  
	//private static Logger logger = Logger.getLogger(MailboxConnection.class);

	protected MailboxConnection connection;
	protected int pollingIntervalSecs = 2;
	protected int maxMessages = 50;
	
	public enum Protocol { POP, IMAP };
	public enum ConnectionMode { INSECURE, FALLBACK, TLS, SSL }
	protected static final String mailboxMaxMessagesKey 			= "mailbox.max.messages";
	protected static final String mailboxPollingIntervalKey 		= "mailbox.polling.interval";
	protected static final String defaultMailboxPollingInterval 	= "2";
	protected static final String defaultMailboxMaxMessages 		= "50";
	protected static Log logger = LogFactory.getLog(MailboxConnections.class.getName());
    
    
public MailboxConnection getConnection() {
        return connection;
    }


    public void setPollingIntervalSecs(int pollingIntervalSecs) {
    	this.pollingIntervalSecs = pollingIntervalSecs;
    }
    
    public int getPollingIntervalSecs() { return pollingIntervalSecs; }

    
    public void setMaxMessages( int maxMessages ) { 
    	this.maxMessages = maxMessages;
    }
    
    public int getMaxMessages() { 
    	return maxMessages;
    }
    public boolean loadSettings(String prefix, Settings prop, String suffix) {
    	logger.debug("loading mailbox connections");
    	setPollingIntervalSecs(ConfigUtil.getInteger(prop.getProperty(mailboxPollingIntervalKey),defaultMailboxPollingInterval));
    	setMaxMessages(ConfigUtil.getInteger(prop.getProperty(mailboxMaxMessagesKey),defaultMailboxMaxMessages));
    	connection = new MailboxConnection();
	  	connection.loadSettings(null,prop,".1");
		return true;
    }
	
    
    public void saveSettings(String prefix, Settings prop, String suffix) {
    	  logger.debug("saving mailbox connections");
    	  prop.setProperty(mailboxPollingIntervalKey, Integer.toString(getPollingIntervalSecs()));
		  prop.setProperty(mailboxMaxMessagesKey, Integer.toString(getMaxMessages()));
    	  connection.saveSettings(null,prop,".1");
    }
 

}
