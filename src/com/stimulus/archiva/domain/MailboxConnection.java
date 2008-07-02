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
 */package com.stimulus.archiva.domain;

import java.util.Locale;

import org.apache.log4j.Logger;

import com.stimulus.archiva.domain.MailboxConnections.ConnectionMode;
import com.stimulus.archiva.domain.MailboxConnections.Protocol;
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

	    protected static final String defaultMailboxConnectionServerName  	= "";
	    protected static final String defaultMailboxConnectionPort 			= "110";
	    protected static final String defaultMailboxConnectionSSLPort   	= "995";
	    protected static final String defaultMailboxConnectionProtocol 		= "pop";
	    protected static final String defaultMailboxConnectionUserName 		= "";
	    protected static final String defaultMailboxConnectionPassword  	= "";
	    protected static final String defaultMailboxConnectionMode			= "insecure";
	    protected static final String defaultMailboxConnectionAuthCerts		= "no";
	    protected static final String defaultMailboxConnectionEnabled		= "no";
	    protected static final String defaultMailboxConnectionIdle			= "yes";
	    
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
		protected static Logger logger = Logger.getLogger(MailboxConnection.class.getName());
		public MailboxConnection() {
			
		}
	    public MailboxConnection(Protocol protocol, String serverName, int port, int sslPort, String username, String password, ConnectionMode connectionMode, boolean authCerts, boolean enabled, boolean idle) {
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
		}
	    
	    public void setEnabled(boolean enabled) { this.enabled = enabled; }
	    
	    public boolean getEnabled() { return this.enabled; }
	    
	    public void setIdle(boolean idle) { this.idle = idle; }
	    
	    public boolean getIdle() { return this.idle; }
	    
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
      		
      		if (sn== null && sp == null && ss == null && pr == null && un == null && pw == null && 
      			 cm == null && ac==null && en == null)
		  	    	return false; 
      		
      		if (id==null) id = "yes"; // backwards compatibility
      		
      		setProtocol(Protocol.valueOf(pr.toUpperCase(Locale.ENGLISH)));
      		setServerName(ConfigUtil.getString(sn,defaultMailboxConnectionServerName));
      		setPort(ConfigUtil.getInteger(sp,defaultMailboxConnectionPort));
      		setSSLPort(ConfigUtil.getInteger(ss,defaultMailboxConnectionSSLPort));
      		setUsername(ConfigUtil.getString(un,defaultMailboxConnectionUserName));
      		setPassword(ConfigUtil.getString(pw,defaultMailboxConnectionPassword));
      		setConnectionMode(MailboxConnections.ConnectionMode.valueOf(cm.toUpperCase(Locale.ENGLISH)));
      		setAuthCerts(ConfigUtil.getBoolean(ac,defaultMailboxConnectionAuthCerts));
      		setEnabled(ConfigUtil.getBoolean(en,defaultMailboxConnectionEnabled));
      		setIdle(ConfigUtil.getBoolean(id,defaultMailboxConnectionIdle));
      		return true;

		}
		
		@Override
		public boolean equals(Object obj){
			if (obj instanceof MailboxConnection) {
				MailboxConnection mbc = (MailboxConnection)obj;
				if (mbc.getPort()==port &&
					mbc.getSSLPort()==sslPort &&
					Compare.equalsIgnoreCase(mbc.getServerName(),serverName) &&
					Compare.equalsIgnoreCase(mbc.getUsername(),username) &&
					Compare.equalsIgnoreCase(mbc.getPassword(),password) &&
					mbc.getAuthCerts()==authCerts &&
					mbc.getEnabled()==enabled &&
					mbc.getIdle()==idle)
					return true;
			} 
			return false;
		}
	 
}
