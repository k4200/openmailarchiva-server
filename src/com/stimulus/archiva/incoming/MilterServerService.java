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


import org.apache.log4j.Logger;
import com.stimulus.archiva.domain.*;
import com.stimulus.util.ConfigUtil;

public class MilterServerService implements Service, Props {

	
    protected static final String defaultMilterPort		   = "8092";
    protected static final String defaultMilterEnable	   = "yes";
	protected static final String milterPortKey 		   = "agent.milter.port";  
	protected static final String milterIpAddressKey 	   = "agent.milter.ipaddress";  
	protected static final String milterEnableKey		   = "agent.milter.enable";
	protected static final String defaultMilterIpAddress   = "all";
	protected boolean milterEnable 				= false;
	protected Server  server 					= null;
	protected FetchMessageCallback callback 	= null;
	protected static Logger logger = Logger.getLogger(MilterServerService.class);
	protected int port = 8091;
	protected String ipAddress = "";
	protected ServiceDelegate serviceDelegate;
	
	public MilterServerService(FetchMessageCallback callback) {
		this.callback = callback;
		serviceDelegate = new ServiceDelegate("milter service", this, logger);
    }
	
	
	 public String getServiceName() {
		 return serviceDelegate.getServiceName();
	 }
	
	 public boolean isAlive() {
		 return serviceDelegate.isAlive(server.isAlive());
	}
	 
	 public void startup() {
	 	try {
	 		int archiveThreads = Config.getConfig().getArchiver().getArchiveThreads();
			server = new Server(ipAddress, port, 50, "com.stimulus.archiva.incoming.MilterRequestHandler", callback, 2000, 1, archiveThreads );
			server.startup();
			serviceDelegate.startup();
		} catch (Exception e) {
			logger.error("failed to start milter server:"+e.getMessage(),e);
		}
	 }
	
	 public void prepareShutdown() {
		 serviceDelegate.prepareShutdown();
	 }
	 
	 public void shutdown() {
		 if (server!=null) {
				server.shutdown();
				server = null;
		 }
		 serviceDelegate.shutdown();
	 }
	 
	 public void reloadConfig() {
		 if (server==null || server.getPort()!=port) {
			 serviceDelegate.reloadConfig();
			 prepareShutdown();
			 shutdown();
			 startup();
		 }
	 }

	 public Status getStatus() {
		 return serviceDelegate.getStatus();
	 }
	
	public void setMilterPort(int milterPort) {
    	this.port = milterPort;
    }
    
    public int getMilterPort() {
    	return port;
    }
    
    public boolean getMilterEnable() {
    	return milterEnable;
    }
    
    public void setMilterEnable(boolean milterEnable) {
    	this.milterEnable = milterEnable;
    }
	    
    public void setIpAddress(String ipAddress) {
    	this.ipAddress = ipAddress;
    }
    
    public String getIpAddress() { return ipAddress; }
    
    public boolean loadSettings(String prefix, Settings prop, String suffix) {
    	logger.debug("loading milter settings");
        setMilterPort(ConfigUtil.getInteger(prop.getProperty(milterPortKey),defaultMilterPort));
        setMilterEnable(ConfigUtil.getBoolean(prop.getProperty(milterEnableKey),defaultMilterEnable));
        setIpAddress(ConfigUtil.getString(prop.getProperty(milterIpAddressKey),defaultMilterIpAddress));
        return true;
    }
    
    public void saveSettings(String prefix, Settings prop, String suffix) {
    	logger.debug("saving milter settings");
	    prop.setProperty(milterPortKey, Integer.toString(getMilterPort()));
	    prop.setProperty(milterEnableKey, ConfigUtil.getYesNo(getMilterEnable()));
	    prop.setProperty(milterIpAddressKey, ipAddress);
    }
    
    @Override
	protected void finalize() throws Throwable {
    	shutdown();
        super.finalize();
    }
    
    
	
}
