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

package com.stimulus.archiva.incoming;

import com.stimulus.archiva.exception.*;
import com.stimulus.archiva.domain.*;
import com.stimulus.util.ConfigUtil;
import com.stimulus.util.Crypto;
import com.stimulus.util.StopBlockTarget;
import org.apache.commons.logging.*;
import org.subethamail.smtp.server.*;
import org.subethamail.smtp.*;
import org.subethamail.smtp.auth.*;
import org.subethamail.smtp.helper.*;
import java.io.*;
import java.util.*;
import java.net.*;

public class SMTPServerService implements Service, Props, SimpleMessageListener, StopBlockTarget {

	static List<String> MECHANISMS = new ArrayList<String>(1);

	static {
		MECHANISMS.add("PLAIN");
	}

	private static final int IDLE_TIMEOUT = 300000; // 5 minutes
	protected static final String smtpAuthKey 			   	   	= "agent.authentication";
	protected static final String smtpUsernameKey 			   	= "agent.username";
	protected static final String smtpPasswordKey 			  	 = "agent.password";
	protected static final String smtpTLSKey 			   	   	= "agent.tls";
	protected static final String smtpPortKey 			   	   	= "agent.port";
	protected static final String smtpIpAddressKey 	   			= "agent.milter.ipaddress";
	protected static final String smtpEnableKey 			   	= "agent.enable";
	protected static final String smtpSocketBackLogKey			= "subsmtp.socket.backlog";
	protected static final String smtpMaxConnectionsKey			= "subsmtp.maxconnections";
	protected static final String smtpDeferredSizeKey			= "subsmtp.deferredsz";

	protected static final String defaultSMTPAuth				= "no";
	protected static final String defaultSMTPUsername  	    	= "admin";
	protected static final String defaultSMTPPassword			= "password";
	protected static final String defaultSMTPTLS				= "no";
	protected static final String defaultSMTPEnable				= "yes";
	protected static final String defaultSMTPPort				= "8091";
	protected static final String defaultSMTPIpAddress			= "all";
	protected static final String defaultSMTPDeferredSize           = "4194304";
	protected static final String defaultSMTPMaxConnections			= "1000";
	protected static final String defaultSMTPSocketBackLog			= "50";
	protected static final String defaultSMTPCertAlias			= null;
	protected static final String defaultSMTPAuthCerts			= "no";

	protected static Log logger = LogFactory.getLog(SMTPServerService.class);
	protected int port							= 8091;
	protected boolean smtpAuth    				= false;
	protected String  smtpUsername 				=  "";
	protected String  smtpPassword 				=  "";
	protected boolean smtpTLS					= false;
	protected boolean smtpEnable 				= false;

	protected SMTPServer  smtpServer 			= null;
	protected String ipAddress = "";
	protected ServiceDelegate serviceDelegate;
	protected int smtpSocketBackLog = 50;
	protected int smtpDeferredSize = 5242880;
	protected int smtpMaxConnections = 1000;
	protected String certAlias;




 	public SMTPServerService() {
	    serviceDelegate = new ServiceDelegate("smtp", this, logger);
	}

	 public String getServiceName() {
		 return serviceDelegate.getServiceName();
	 }

	 public boolean isAlive() {
		 boolean alive = (smtpServer!=null && smtpServer.isRunning());
		 return serviceDelegate.isAlive(alive);
	}

	 public void startup() {
		 if (!getSMTPEnable()) {
				logger.info("smtp server not started. it is disabled. not listening.");
				return;
			}
			//int archiveThreads = Config.getConfig().getArchiver().getArchiveThreads();
			logger.debug(getServiceName()+" started");
			smtpServer = new SMTPServer(new SimpleMessageListenerAdapter(this));

			//smtpServer.setName(Config.getConfig().getProductName()+Config.getConfig().getApplicationVersion());
			UsernamePasswordValidator validator = new MailArchivaUsernamePasswordValidator();
			EasyAuthenticationHandlerFactory fact = new EasyAuthenticationHandlerFactory(validator);
			smtpServer.setAuthenticationHandlerFactory(fact);
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

			smtpServer.setPort(port);
			smtpServer.setHideTLS(!smtpTLS);
			smtpServer.setBacklog(smtpSocketBackLog);
			smtpServer.setMaxConnections(smtpMaxConnections);
			//smtpServer.setDataDeferredSize(smtpDeferredSize);
			smtpServer.setMaxRecipients(10000000);

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
		 if (isAlive()) {
			 if (smtpServer!=null) {
					logger.debug("shutting down smtp server");
					smtpServer.stop();
			 }
			 serviceDelegate.prepareShutdown();
		 }
	 }

	 public void shutdown() {
		 if (isAlive()) {
			 if (smtpServer!=null) {
					logger.debug("shutting down smtp server");
					smtpServer.stop();
					smtpServer = null;
			 }
			 serviceDelegate.shutdown();
		 }
	 }

	 public void reloadConfig() {
		 boolean started = serviceDelegate.getStatus()==Status.STARTED;

		 if (smtpEnable && !started) {
			 startup();
			 serviceDelegate.reloadConfig();
			 return;
		 }
		 // check this
		 if (smtpEnable && (smtpServer.getPort()!=port || smtpServer.getHideTLS()==smtpTLS)) {
			 prepareShutdown();
			 shutdown();
			 startup();
			 serviceDelegate.reloadConfig();
			 return;
		 }

		 if (!smtpEnable && started) {
			 prepareShutdown();
			 shutdown();
			 serviceDelegate.reloadConfig();
			 return;
		 }


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
        String encryptedPassword = ConfigUtil.getString(prop.getProperty(smtpPasswordKey),defaultSMTPPassword);
        if (!encryptedPassword.endsWith("=")) {
        	setSMTPPassword(encryptedPassword);
        } else {
        	try {
        		setSMTPPassword(Crypto.decryptPassword(encryptedPassword));
        	} catch (MessageStoreException mse) {
        		logger.error("failed to load ldap service account pass phrase",mse);
        	}
        }
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
	    prop.setProperty(smtpPortKey, Integer.toString(getSMTPPort()));
	    prop.setProperty(smtpEnableKey, ConfigUtil.getYesNo(getSMTPEnable()));
	    prop.setProperty(smtpIpAddressKey, ipAddress);


	    if (getSMTPPassword()!=null) {
	        // if raw password is hash value, we know to see the passphrase
	        try {
	        		prop.setProperty(smtpPasswordKey,Crypto.encryptPassword(getSMTPPassword()));
        	} catch (MessageStoreException mse) {
        		logger.error("failed to save active directory service account pass phrase",mse);
        	}

        }

    }

    public boolean accept(String from, String recipient, SocketAddress socket) {
    	InetAddress address = ((InetSocketAddress)socket).getAddress();
    	return Config.getConfig().getAgent().isAllowed(address);
    }

	public void deliver(String from, String recipient, InputStream data) throws TooMuchDataException, IOException {
		Config.getStopBlockFactory().detectBlock("smtp server",Thread.currentThread(),this,IDLE_TIMEOUT);
		try {
		   Config.getConfig().getFetchMessageCallback().store(data,from);
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



	class MailArchivaUsernamePasswordValidator implements UsernamePasswordValidator
	{
		public void login(String username, String password) throws LoginFailedException
		{
			if (!smtpAuth)
				return;

			boolean authenticated = username.equalsIgnoreCase(smtpUsername) && password.equalsIgnoreCase(password);
			if (!authenticated) {
				throw new LoginFailedException("username and/or password is incorrect.");
			}
		}
	}

	/** Always accept everything */
	public boolean accept(String from, String recipient)
	{
		return true;
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
