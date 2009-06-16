/* Copyright (C) 2005-2009 Jamie Angus Band
 * MailArchiva Open Source Edition Copyright (c) 2005-2009 Jamie Angus Band
 * This program is free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation; either version
 * 3 of the License, or (at your option) any later version.
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

import java.util.Locale;

import org.apache.commons.logging.*;

import com.stimulus.archiva.domain.MailboxConnections.ConnectionMode;
import com.stimulus.archiva.domain.MailboxConnections.Protocol;
import com.stimulus.archiva.exception.MessageStoreException;
import com.stimulus.util.*;

public class MailboxConnection implements Props {

		protected String serverName;
		protected int port = 143;
		protected Protocol protocol = Protocol.IMAP;
		protected String username;
		protected String password;
	    protected int sslPort = 993;
	    protected ConnectionMode connectionMode = ConnectionMode.FALLBACK;
	    protected boolean authCerts = false;
	    protected boolean enabled = false;
		protected boolean idle = true;
		protected boolean unread = true;

	    protected static final String defaultMailboxConnectionServerName  	= "";
	    protected static final String defaultMailboxConnectionPort 			= "143";
	    protected static final String defaultMailboxConnectionSSLPort   	= "993";
	    protected static final String defaultMailboxConnectionProtocol 		= "imap";
	    protected static final String defaultMailboxConnectionUserName 		= "";
	    protected static final String defaultMailboxConnectionPassword  	= "";
	    protected static final String defaultMailboxConnectionMode			= "fallback";
	    protected static final String defaultMailboxConnectionAuthCerts		= "no";
	    protected static final String defaultMailboxConnectionEnabled		= "no";
	    protected static final String defaultMailboxConnectionIdle			= "yes";
	    protected static final String defaultMailboxConnectionUnread		= "yes";

		protected static final String mailboxConnectionServerNameKey  	= "mailbox.connection.server.address";
		protected static final String mailboxConnectionPortKey 			= "mailbox.connection.port";
		protected static final String mailboxConnectionSSLPortKey     	= "mailbox.connection.sslport";
		protected static final String mailboxConnectionProtocolKey 		= "mailbox.connection.protocol";
		protected static final String mailboxConnectionUserNameKey 		= "mailbox.connection.username";
		protected static final String mailboxConnectionPasswordKey    	= "mailbox.connection.password";
		protected static final String mailboxConnectionModeKey    		= "mailbox.connection.mode";
		protected static final String mailboxConnectionAuthCertsKey    	= "mailbox.connection.authenticate.certs";
		protected static final String mailboxConnectionEnabledKey      	= "mailbox.connection.enabled";
		protected static final String mailboxConnectionIdleKey      	= "mailbox.connection.idle";
		protected static final String mailboxConnectionUnreadKey      	= "mailbox.connection.unread";

		protected static Log logger = LogFactory.getLog(MailboxConnection.class.getName());

		public MailboxConnection() {

		}
	    public MailboxConnection(Protocol protocol, String serverName, int port, int sslPort, String username, String password, ConnectionMode connectionMode, boolean authCerts, boolean enabled, boolean idle, boolean unread) {
			this.protocol = protocol;
			this.serverName = serverName;
			this.port = port;
			this.sslPort = sslPort;
			this.username = username;
			this.password = password;
			this.connectionMode = connectionMode;
			this.authCerts =authCerts;
			this.enabled = enabled;
			this.idle = idle;
			this.unread = unread;
		}

	    public void setEnabled(boolean enabled) { this.enabled = enabled; }

	    public boolean getEnabled() { return this.enabled; }

	    public void setIdle(boolean idle) { this.idle = idle; }

	    public boolean getIdle() { return this.idle; }

	    public void setUnread(boolean unread) { this.unread = unread; }

	    public boolean getUnread() { return this.unread; }

	    public void setAuthCerts(boolean authCerts) { this.authCerts = authCerts; }

	    public boolean getAuthCerts() { return this.authCerts; }

	    public ConnectionMode getConnectionMode() { return connectionMode; }

	    public void setConnectionMode(ConnectionMode connectionMode) {
	    	this.connectionMode = connectionMode;
	    }

	    public String getUsername() { return username; }

	    public void setUsername(String username) { this.username = username; }

	    public String getPassword() { return password; }

	    public void setPassword(String password) { this.password = password; }

	    public int getPort() { return port; }


	    public void setPort(int port) { this.port = port; }

	    public String getServerName() { return serverName; }

	    public void setServerName(String serverName) { this.serverName = serverName; }

	    public int getSSLPort() { return sslPort; }

	    public void setSSLPort(int sslPort) { this.sslPort = sslPort; }

	    public Protocol getProtocol() { return protocol; }

	    public void setProtocol(Protocol protocol) { this.protocol = protocol; }

		public void saveSettings(String prefix, Settings prop, String suffix) {
				logger.debug("saving mailbox connection settings");
            	prop.setProperty(mailboxConnectionServerNameKey + suffix, getServerName());
            	prop.setProperty(mailboxConnectionPortKey + suffix, Integer.toString(getPort()));
            	prop.setProperty(mailboxConnectionSSLPortKey + suffix,Integer.toString(getSSLPort()));
            	prop.setProperty(mailboxConnectionProtocolKey + suffix,getProtocol().toString().toLowerCase(Locale.ENGLISH));
            	prop.setProperty(mailboxConnectionUserNameKey + suffix,getUsername());
            	prop.setProperty(mailboxConnectionPasswordKey + suffix,getPassword());
            	prop.setProperty(mailboxConnectionModeKey + suffix,getConnectionMode().toString().toLowerCase(Locale.ENGLISH));
            	prop.setProperty(mailboxConnectionAuthCertsKey + suffix,ConfigUtil.getYesNo(getAuthCerts()));
            	prop.setProperty(mailboxConnectionEnabledKey + suffix,ConfigUtil.getYesNo(getEnabled()));
            	prop.setProperty(mailboxConnectionIdleKey + suffix,ConfigUtil.getYesNo(getIdle()));
            	prop.setProperty(mailboxConnectionUnreadKey + suffix,ConfigUtil.getYesNo(getUnread()));
            	if (getPassword()!=null) {
        	        // if raw password is hash value, we know to see the passphrase
        	        try {
        	        		prop.setProperty(mailboxConnectionPasswordKey + suffix,Crypto.encryptPassword(getPassword()));
                	} catch (MessageStoreException mse) {
                		logger.error("failed to load mailbox password",mse);
                	}

                }
		}


		public boolean loadSettings(String prefix, Settings prop, String suffix) {
			logger.debug("loading mailbox connection settings");
			String pr = prop.getProperty(mailboxConnectionProtocolKey  + suffix);
      		String sn = prop.getProperty(mailboxConnectionServerNameKey + suffix);
      		String sp = prop.getProperty(mailboxConnectionPortKey  + suffix);
      		String ss = prop.getProperty(mailboxConnectionSSLPortKey  + suffix);
      		String un = prop.getProperty(mailboxConnectionUserNameKey  + suffix);
      		String pw = prop.getProperty(mailboxConnectionPasswordKey  + suffix);
      		String cm = prop.getProperty(mailboxConnectionModeKey  + suffix);
      		String ac = prop.getProperty(mailboxConnectionAuthCertsKey  + suffix);
      		String en = prop.getProperty(mailboxConnectionEnabledKey  + suffix);
      		String id = prop.getProperty(mailboxConnectionIdleKey  + suffix);
      		String ur = prop.getProperty(mailboxConnectionUnreadKey  + suffix);


      		if (id==null) id = "yes"; // backwards compatibility
      		if (ur==null) ur = "yes";

      		Protocol protocol = Protocol.IMAP;
      		if (pr!=null) {

	      		try {
	      			protocol = Protocol.valueOf(pr.trim().toUpperCase(Locale.ENGLISH));
	 	   	  	} catch (IllegalArgumentException iae) {
	 	   	    		logger.error("invalid protocol specified in mailbox connection {protocol='"+pr+"'}");
	 	   	    		try {
	 	   	    			protocol = Protocol.valueOf(defaultMailboxConnectionProtocol.toUpperCase());
	 	   	    		} catch (IllegalArgumentException iae2) {}
	 	   	  	}
      		} else {
      			try {
	   	    		protocol = Protocol.valueOf(defaultMailboxConnectionProtocol.toUpperCase());
   	    		} catch (IllegalArgumentException iae2) {}
      		}
      		setProtocol(protocol);

 	   	  	MailboxConnections.ConnectionMode connectionMode = MailboxConnections.ConnectionMode.FALLBACK;
 	   	  	if (cm!=null) {
	 	   	  	try {
	 	   	  		connectionMode = MailboxConnections.ConnectionMode.valueOf(cm.trim().toUpperCase(Locale.ENGLISH));
		   	  	} catch (IllegalArgumentException iae) {
		   	    		logger.error("invalid connection mode specified in mailbox connection {mode='"+pr+"'}");
		   	    		try {
		   	    			connectionMode = ConnectionMode.valueOf(defaultMailboxConnectionMode.toUpperCase());
		   	    		} catch (IllegalArgumentException iae2) {
		   	    			connectionMode = MailboxConnections.ConnectionMode.FALLBACK;
		   	    		}
		   	  	}
 	   	  	} else {
	 	   	  	try {
	 	   	  		connectionMode = ConnectionMode.valueOf(defaultMailboxConnectionMode.toUpperCase());
		    		} catch (IllegalArgumentException iae2) {}
	 	    }

	   	  	setConnectionMode(connectionMode);

      		setServerName(ConfigUtil.getString(sn,defaultMailboxConnectionServerName));
      		setPort(ConfigUtil.getInteger(sp,defaultMailboxConnectionPort));
      		setSSLPort(ConfigUtil.getInteger(ss,defaultMailboxConnectionSSLPort));
      		setUsername(ConfigUtil.getString(un,defaultMailboxConnectionUserName));

      		setAuthCerts(ConfigUtil.getBoolean(ac,defaultMailboxConnectionAuthCerts));
      		setEnabled(ConfigUtil.getBoolean(en,defaultMailboxConnectionEnabled));
      		setIdle(ConfigUtil.getBoolean(id,defaultMailboxConnectionIdle));
      		setUnread(ConfigUtil.getBoolean(ur,defaultMailboxConnectionUnread));

      		String encryptedPassword = ConfigUtil.getString(pw,defaultMailboxConnectionPassword);
        	if (!encryptedPassword.endsWith("=")) {
        		setPassword(encryptedPassword);
            } else {
            	try {
            		setPassword(Crypto.decryptPassword(encryptedPassword));
            	} catch (MessageStoreException mse) {
            		logger.error("failed to set mailbox pass phrase",mse);
            	}
            }
      		return true;

		}

		@Override
		public boolean equals(Object obj){
			if (obj==null)
				return false;

			if (obj instanceof MailboxConnection) {
				MailboxConnection mbc = (MailboxConnection)obj;

				if (mbc.getServerName()==null || serverName==null) {
					return false;
				}
				if (mbc.getUsername()==null || username==null) {
					return false;
				}

				if (mbc.getPassword()==null || password==null) {
					return false;
				}

				if (mbc.getPort()==port &&
					mbc.getSSLPort()==sslPort &&
					Compare.equalsIgnoreCase(mbc.getServerName(),serverName) &&
					Compare.equalsIgnoreCase(mbc.getUsername(),username) &&
					Compare.equalsIgnoreCase(mbc.getPassword(),password) &&
					mbc.getAuthCerts()==authCerts &&
					mbc.getEnabled()==enabled &&
					mbc.getIdle()==idle &&
					mbc.getUnread()==unread)
					return true;
			}
			return false;
		}

}
