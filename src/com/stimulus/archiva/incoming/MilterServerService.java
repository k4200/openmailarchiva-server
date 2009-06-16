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


import org.apache.commons.logging.*;
import com.stimulus.archiva.domain.*;
import com.stimulus.archiva.domain.Service.Status;
import com.stimulus.util.ConfigUtil;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

public class MilterServerService implements Service, Props {


    protected static final String defaultPort		   	= "8092";
    protected static final String defaultEnable	   		= "yes";
    protected static final String defaultincludeBCC  	= "no";
	protected static final String portKey 		   		= "agent.milter.port";
	protected static final String ipAddressKey 	   		= "agent.milter.ipaddress";
	protected static final String enableKey		   		= "agent.milter.enable";
	protected static final String includeBCCKey	   		= "agent.milter.includebcc";
	protected static final String includeBCCIgnoreAddressKey = "agent.milter.includebcc.ignoreaddress";
	protected static final String defaultipAddress   = "all";
	protected boolean enable 					= false;
	protected boolean includeBCC				= false;
	protected Server  server 					= null;
	protected static Log logger = LogFactory.getLog(MilterServerService.class);
	protected int port = 8091;
	protected String ipAddress = "";
	protected ServiceDelegate serviceDelegate;
	protected ArrayList<String> includeBCCIgnoreAddress = new ArrayList<String>();

	public MilterServerService() {
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
			server = new Server(ipAddress, port, 50, "com.stimulus.archiva.incoming.MilterRequestHandler",
								Config.getConfig().getFetchMessageCallback(), 2000, 1, archiveThreads,
								getServiceName());
			server.startup();
			serviceDelegate.startup();
		} catch (Exception e) {
			logger.error("failed to start milter server:"+e.getMessage(),e);
		}
	 }

	 public void prepareShutdown() {
		 if (isAlive()) {
			 serviceDelegate.prepareShutdown();
		 }
	 }

	 public void shutdown() {
		 if (isAlive()) {
			 if (server!=null) {
					server.shutdown();
					server = null;
			 }
			 serviceDelegate.shutdown();
		 }
	 }
	 public void reloadConfig() {
		 boolean started = serviceDelegate.getStatus()==Status.STARTED;

		 if (enable && !started) {
			 startup();
			 serviceDelegate.reloadConfig();
			 return;
		 }

		 if (enable && server.getPort()!=port) {
			 prepareShutdown();
			 shutdown();
			 startup();
			 serviceDelegate.reloadConfig();
			 return;
		 }

		 if (!enable && started) {
			 prepareShutdown();
			 shutdown();
			 serviceDelegate.reloadConfig();
			 return;
		 }


	 }

	 public Status getStatus() {
		 return serviceDelegate.getStatus();
	 }

	public void setPort(int port) {
    	this.port = port;
    }

    public int getPort() {
    	return port;
    }

    public boolean getEnable() {
    	return enable;
    }

    public void setEnable(boolean enable) {
    	this.enable = enable;
    }

    public void setIpAddress(String ipAddress) {
    	this.ipAddress = ipAddress;
    }

    public void setIncludeBCC(boolean includeBCC) {
    	this.includeBCC = includeBCC;
    }

    public boolean getIncludeBCC() {
    	return includeBCC;
    }

    public List<String> getIgnoreBCCAddress() {
    	return includeBCCIgnoreAddress;
    }

    protected void addIncludeBCCIgnoreAddress(String address) {
    	includeBCCIgnoreAddress.add(address.trim().toLowerCase(Locale.ENGLISH));
    }

    protected void clearIncludeBCCIgnoreAddresses() {
    	includeBCCIgnoreAddress.clear();
    }

    public String getIpAddress() { return ipAddress; }

    public boolean loadSettings(String prefix, Settings prop, String suffix) {
    	logger.debug("loading milter settings");
        setPort(ConfigUtil.getInteger(prop.getProperty(portKey),defaultPort));
        setEnable(ConfigUtil.getBoolean(prop.getProperty(enableKey),defaultEnable));
        setIpAddress(ConfigUtil.getString(prop.getProperty(ipAddressKey),defaultipAddress));
        setIncludeBCC(ConfigUtil.getBoolean(prop.getProperty(includeBCCKey),defaultincludeBCC));
        clearIncludeBCCIgnoreAddresses();
        int i = 1;
 	  	do {
 	  	    String includeBCCIgnoreAddress = prop.getProperty(includeBCCIgnoreAddressKey + "." + Integer.toString(i++));
 	  	    if (includeBCCIgnoreAddress==null) break;
 	  	    addIncludeBCCIgnoreAddress(includeBCCIgnoreAddress);
 	  	} while(true);

        return true;
    }

    public void saveSettings(String prefix, Settings prop, String suffix) {
    	logger.debug("saving milter settings");
	    prop.setProperty(portKey, Integer.toString(getPort()));
	    prop.setProperty(enableKey, ConfigUtil.getYesNo(getEnable()));
	    prop.setProperty(ipAddressKey, ipAddress);
	    prop.setProperty(includeBCCKey, ConfigUtil.getYesNo(getIncludeBCC()));
    }


}
