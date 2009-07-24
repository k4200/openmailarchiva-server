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

package com.stimulus.archiva.presentation;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.*;

import com.stimulus.archiva.domain.*;
import com.stimulus.archiva.domain.MailboxConnections.Protocol;
import com.stimulus.struts.BaseBean;

public class MailboxConnectionBean extends BaseBean implements Serializable {

	private static final long serialVersionUID = -136426151372723221L;
	protected static Log logger = LogFactory.getLog(BasicRoleMapBean.class.getName());
	MailboxConnection connection;
	
	public MailboxConnectionBean(MailboxConnection connection) {
		this.connection = connection;
	}
    
    public String getUsername() { return connection.getUsername(); }
    
    public void setUsername(String username) { connection.setUsername(username); }
    
    public String getPassword() { return connection.getPassword(); }
    
    public void setPassword(String password) { connection.setPassword(password); }
    
    public int getPort() { return connection.getPort(); } 

    public void setPort(int port) { connection.setPort(port); }
    
    public String getServerName() { return connection.getServerName(); }
    
    public void setServerName(String serverName) { connection.setServerName(serverName); }
    
    public int getSSLPort() { return connection.getSSLPort(); }
    
    public void setSSLPort(int sslPort) { connection.setSSLPort(sslPort); }
   
    public String getProtocol() { 
    	return connection.getProtocol().toString().toLowerCase(Locale.ENGLISH); 
    }
    
    public void setProtocol(String protocol) {
    
    	 MailboxConnections.Protocol newProtocol = MailboxConnections.Protocol.POP;
  		 try {
  			newProtocol = Protocol.valueOf(protocol.trim().toUpperCase(Locale.ENGLISH));
  		 } catch (IllegalArgumentException iae) {
 	    		logger.error("failed to set iap service protocol. protocol is set to an illegal value {protocol='"+protocol+"'}");
 	    		logger.info("mailbox connection protocol is set to POP (error recovery)");
  		 }
  		 connection.setProtocol(newProtocol);
    }
    
   
	  public String getConnectionMode() {
		  return connection.getConnectionMode().toString().toLowerCase(Locale.ENGLISH); 
	  }
	  
	  public void setConnectionMode(String connectionMode) {
		    
	    	 MailboxConnections.ConnectionMode newConnectionMode = MailboxConnections.ConnectionMode.INSECURE;
	  		 try {
	  			newConnectionMode = MailboxConnections.ConnectionMode.valueOf(connectionMode.trim().toUpperCase(Locale.ENGLISH));
	  		 } catch (IllegalArgumentException iae) {
	 	    		logger.error("failed to set mailbox connection mode. connection mode is set to an illegal value {connectionMode='"+connectionMode+"'}");
	 	    		logger.info("mailbox connection mode is set to INSECURE (error recovery)");
	  		 }
	  		 connection.setConnectionMode(newConnectionMode);
	    }
	  
	  
	  public boolean getAuthCerts() { return connection.getAuthCerts(); }
	  
	  public void setAuthCerts(boolean authCerts) { connection.setAuthCerts(authCerts); }
	
	  public boolean getEnabled() { return connection.getEnabled(); }
	  
	  public void setEnabled(boolean enabled) { connection.setEnabled(enabled); }

	  public void setIdle(boolean idle) { connection.setIdle(idle); }
	  
	  public boolean getIdle() { return connection.getIdle(); }
	  
	  public void setUnread(boolean unread) { connection.setUnread(unread); }
	  
	  public boolean getUnread() { return connection.getUnread(); }
}
