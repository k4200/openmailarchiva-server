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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.logging.*;

public class Agent implements Props {

	 protected static final String allowedIPKey  = "agent.allowed.ipaddress";
 	 protected LinkedList<String> allowIPAddress = new LinkedList<String>();
 	 protected static Log logger = LogFactory.getLog(Agent.class.getName());
 	 
    public void addAllowedIPAddress(String ipAddress) {
    	allowIPAddress.add(ipAddress);
    }
    
    public String getIPAddress(int id) {
    	return allowIPAddress.get(id);
    }
    
    public void setIPAddress(int id, String value) {
    	String ipAddress = allowIPAddress.get(id);
   	 if (ipAddress!=null)
   		 allowIPAddress.set(id, value);
    }
    public void removeIPAddress(int id) {
    	String ipAddress = allowIPAddress.get(id);
    	 if (ipAddress!=null)
    		 allowIPAddress.remove(id);
    }
    
    public List<String> getIPAddresses() {
    	return allowIPAddress;
    }
    
    public boolean isAllowed(InetAddress address) {
    	
    	// if nothing defined, by default we allow
    	if (allowIPAddress.size()==0)
    		return true;
    	
    	for (String ip : allowIPAddress) {
    		try {
    			InetAddress newip = InetAddress.getByName(ip);
    			if (newip.equals(address))
    				return true;
    		} catch (UnknownHostException uhe) {
    			logger.debug("allowed agent ip address appears invalid");
    			
    		}
    	}
    	return false;
    }
    
    public boolean loadSettings(String prefix, Settings prop, String suffix) {
    	logger.debug("loading agent settings");
    	allowIPAddress.clear();
	  	int i = 1;
        do {
        		String allowedIP = prop.getProperty(allowedIPKey + "."+ Integer.toString(i++));
                if (allowedIP==null)
                	break;
                addAllowedIPAddress(allowedIP);
        } while (true);
       
        return true;
    }
    
    public void saveSettings(String prefix, Settings prop, String suffix) {
    	logger.debug("saving agent settings");
    	int c = 1;
    	for (String ipAddress : getIPAddresses()) {
    		prop.setProperty(allowedIPKey + "."+c++, ipAddress);
    	}
    }
    
}
