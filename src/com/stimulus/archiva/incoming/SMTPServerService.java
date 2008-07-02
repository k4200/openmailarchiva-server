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
package com.stimulus.archiva.incoming;

import com.stimulus.archiva.exception.*;
import com.stimulus.archiva.domain.*;
import com.stimulus.util.ConfigUtil;
import com.stimulus.util.StopBlockTarget;
import org.apache.log4j.Logger;
import org.subethamail.smtp.server.*;
import org.subethamail.smtp.*;
import org.subethamail.smtp.auth.*;
import java.io.*;
import java.util.*;
import java.net.*;

public class SMTPServerService implements Service, Props, MessageListener, StopBlockTarget {

	
	private static final int IDLE_TIMEOUT = 300000; // 5 minutes
	protected static final String smtpAuthKey 			   	   	= "agent.authentication";
	protected static final String smtpUsernameKey 			   	= "agent.username";
	protected static final String smtpPasswordKey 			  	 = "agent.password";
	protected static final String smtpTLSKey 			   	   	= "agent.tls";
	protected static final String smtpPortKey 			   	   	= "agent.port";
	protected static final String smtpIpAddressKey 	   			= "agent.milter.ipaddress";  
	protected static final String smtpEnableKey 			   	= "agent.enable";
	protected static final String smtpSocketBackLogKey			= "smtp.socket.backlog";
	protected static final String smtpMaxConnectionsKey			= "smtp.maxconnections";
	protected static final String smtpDeferredSizeKey			= "smtp.deferredsize";
	
	protected static final String defaultSMTPAuth				= "no";
	protected static final String defaultSMTPUsername  	    	= "mailarchiva";
	protected static final String defaultSMTPPassword			= "password";
	protected static final String defaultSMTPTLS				= "no";
	protected static final String defaultSMTPEnable				= "yes";
	protected static final String defaultSMTPPort				= "8091";
	protected static final String defaultSMTPIpAddress			= "all";
	protected static final String defaultSMTPDeferredSize           = "268435456";
	protected static final String defaultSMTPMaxConnections			= "30000";
	protected static final String defaultSMTPSocketBackLog			= "30000";
	
	protected static Logger logger = Logger.getLogger(SMTPServerService.class);
	protected int port							= 8091;
	protected boolean smtpAuth    				= false;
	protected String  smtpUsername 				=  "";
	protected String  smtpPassword 				=  "";
	protected boolean smtpTLS					= false;
	protected boolean smtpEnable 				= false;
	protected SMTPServer  smtpServer 			= null;
	protected FetchMessageCallback callback 	= null;
	protected String ipAddress = "";
	protected ServiceDelegate serviceDelegate;
	protected int smtpSocketBackLog = 30000;
	protected int smtpDeferredSize = 30000;
	protected int smtpMaxConnections = 16777216;
		
	
 	public SMTPServerService(FetchMessageCallback callback) {
	    this.callback = callback;
	    serviceDelegate = new ServiceDelegate("smtp", this, logger);
	}
	
	 public String getServiceName() {
		 return serviceDelegate.getServiceName();
	 }
	
	 public boolean isAlive() {
		 return serviceDelegate.isAlive(smtpServer.isRunning());
	}
	 
	 public void startup() {
		 if (!getSMTPEnable()) {
				logger.info("mailarchiva smtp server not started. it is disabled. not listening.");
				return;
			}
			//int archiveThreads = Config.getConfig().getArchiver().getArchiveThreads();
			logger.debug(getServiceName()+" started");
			List<MessageListener> listeners = new ArrayList<MessageListener>();
			listeners.add(this);
			smtpServer = new SMTPServer(listeners);
			smtpServer.setName("MailArchiva "+Config.getConfig().getApplicationVersion());
			
			((MessageListenerAdapter)smtpServer.getMessageHandlerFactory()).setAuthenticationHandlerFactory(new AuthHandlerFactory());
			if (!ipAddress.equalsIgnoreCase("all")) {
				InetAddress bindAddress;
				try {
					bindAddress = InetAddress.getByName(ipAddress);
				} catch (Exception uhe) {
					logger.error("failed to start stmp server. bind ip address is invalid:"+uhe.getMessage());
					return;
				}
				smtpServer.setBindAddress(bindAddress);
			}
			// we set the deferred size limit to 10MB. we dont want to hammer the hard drive any more 
			// than it already is
		
			smtpServer.setPort(port);
			smtpServer.setAnnounceTLS(smtpTLS);
			smtpServer.setBacklog(smtpSocketBackLog);
			smtpServer.setMaxConnections(smtpMaxConnections);
			smtpServer.setDataDeferredSize(smtpDeferredSize);
			
			logger.debug("smtp server advanced startup parameters {port='"+port+"',tls='"+smtpTLS+"',smtpSocketBackLog='"+smtpSocketBackLog+"',smtpMaxConnections='"+smtpMaxConnections+"'}");
			try {
				smtpServer.start();
				serviceDelegate.startup();
			} catch(RuntimeException re) {
				logger.error("failed to start smtp server. mostly likely, the port is likely already taken.",re);
				logger.warn("please ensure that you do not have more than one instance of MailArchiva running");
			}
	 }
	
	 public void prepareShutdown() {
		 if (smtpServer!=null) {
				logger.debug("shutting down smtp server");
				smtpServer.stop();
				smtpServer = null;
		 }
		 serviceDelegate.prepareShutdown();
	 }
	 
	 public void shutdown() {
		 if (smtpServer!=null) {
				logger.debug("shutting down smtp server");
				smtpServer.stop();
				smtpServer = null;
		 }
		 serviceDelegate.shutdown();
	 }
	 
	 public void reloadConfig() {
		 if (smtpServer==null || smtpServer.getPort()!=port) {
			 prepareShutdown();
			 shutdown();
			 startup();
			 serviceDelegate.reloadConfig();
		 }
	 }
	 
	 @Override
	protected void finalize() throws Throwable {
		 shutdown();
	 }
	 
	 public Status getStatus() {
		 return serviceDelegate.getStatus();
	 }
	 
	
	
	 
    public void setSMTPPort(int smtpPort) {
    	this.port = smtpPort;
    }
    
    public int getSMTPPort() { return port; }
    
    public boolean getSMTPEnable() {
    	return smtpEnable;
    }
    
    public void setSMTPEnable(boolean smtpEnable) {
    	this.smtpEnable = smtpEnable;
    }
    
    
    public void setSMTPUsername(String username) { 
    	this.smtpUsername = username; 
    }
    
    public String getSMTPUsername() { return smtpUsername; }
    
    public void setSMTPPassword(String password) {
    	this.smtpPassword = password;
    }
    
    public String getSMTPPassword() { 
    	return smtpPassword;
    }
    
    public void setSMTPTLS(boolean enabled) {
    	this.smtpTLS = enabled;
    }
    
    public boolean getSMTPTLS() { return smtpTLS; }
    
    public void setSMTPAuth(boolean enabled) {
    	this.smtpAuth = enabled;
    }
    
    public boolean getSMTPAuth() { 
    	return smtpAuth;
    }
    
    
    public void setIpAddress(String ipAddress) {
    	this.ipAddress = ipAddress;
    }
    
    public String getIpAddress() { return ipAddress; }
    
    public boolean loadSettings(String prefix, Settings prop, String suffix) {
    	logger.debug("loading smtp server settings");
        setSMTPPort(ConfigUtil.getInteger(prop.getProperty(smtpPortKey),defaultSMTPPort));
        setSMTPAuth(ConfigUtil.getBoolean(prop.getProperty(smtpAuthKey),defaultSMTPAuth));
        setSMTPUsername(ConfigUtil.getString(prop.getProperty(smtpUsernameKey),defaultSMTPUsername));
        setSMTPPassword(ConfigUtil.getString(prop.getProperty(smtpPasswordKey),defaultSMTPPassword));
        setSMTPTLS(ConfigUtil.getBoolean(prop.getProperty(smtpTLSKey),defaultSMTPTLS));
        setSMTPEnable(ConfigUtil.getBoolean(prop.getProperty(smtpEnableKey),defaultSMTPEnable));
        setIpAddress(ConfigUtil.getString(prop.getProperty(smtpIpAddressKey), defaultSMTPIpAddress));
        // not editable via gui. advanced params for specialists
        smtpSocketBackLog = ConfigUtil.getInteger(prop.getProperty(smtpSocketBackLogKey),defaultSMTPSocketBackLog);
        smtpDeferredSize =  ConfigUtil.getInteger(prop.getProperty(smtpDeferredSizeKey),defaultSMTPDeferredSize);
        smtpMaxConnections =  ConfigUtil.getInteger(prop.getProperty(smtpMaxConnectionsKey),defaultSMTPMaxConnections);
        return true;
    }

    public void saveSettings(String prefix, Settings prop, String suffix) {
    	logger.debug("saving smtp server settings");
	    prop.setProperty(smtpAuthKey, ConfigUtil.getYesNo(getSMTPAuth()));
	    prop.setProperty(smtpTLSKey, ConfigUtil.getYesNo(getSMTPTLS()));
	    prop.setProperty(smtpUsernameKey, getSMTPUsername());
	    prop.setProperty(smtpPasswordKey, getSMTPPassword());
	    prop.setProperty(smtpPortKey, Integer.toString(getSMTPPort()));
	    prop.setProperty(smtpEnableKey, ConfigUtil.getYesNo(getSMTPEnable()));
	    prop.setProperty(smtpIpAddressKey, ipAddress);
    }
    
    public boolean accept(String from, String recipient, SocketAddress socket) {
    	InetAddress address = ((InetSocketAddress)socket).getAddress();
    	return Config.getConfig().getAgent().isAllowed(address);
    }

	public void deliver(String from, String recipient, InputStream data) throws TooMuchDataException, IOException {
		Config.getStopBlockFactory().detectBlock("smtp server",Thread.currentThread(),this,IDLE_TIMEOUT);
		try {
		   callback.store(data,from);	
	   } catch (ArchiveException ae) {
		   logger.error("failed to archive message:"+ae.getMessage(),ae);
		   if (ae.getRecoveryDirective()!=ArchiveException.RecoveryDirective.ACCEPT) {
			   throw new IOException(ae.getMessage());
		   }
	   } catch (Throwable t) {
		   logger.error("failed to archive message:"+t.getMessage(),t);
	   } finally {
			Config.getStopBlockFactory().endDetectBlock(Thread.currentThread());
		}
	}
	
	public class AuthHandlerFactory implements AuthenticationHandlerFactory
	{
		public AuthenticationHandler create()
		{
			PluginAuthenticationHandler ret = new PluginAuthenticationHandler();
			UsernamePasswordValidator validator = new UsernamePasswordValidator()
			{
				public void login(String username, String password) throws LoginFailedException {
					boolean authenticated = username.equalsIgnoreCase(smtpUsername) &&
										    password.equalsIgnoreCase(password);
					if (!authenticated) {
						throw new LoginFailedException("username and/or password is incorrect.");
					}
				}
			};
			if (!smtpAuth) {
				ret.addPlugin(new DummyAuthenticationHandler());
			} else {
				ret.addPlugin(new PlainAuthenticationHandler(validator));
				ret.addPlugin(new LoginAuthenticationHandler(validator));
			}
			return ret;
		}
	}
	
	 public void handleBlock(Thread thread) {

         synchronized (this) {
             if (thread != null) {
            	 logger.debug("interrupt thread()");
            	 thread.interrupt();
             }
         }
    }
}
