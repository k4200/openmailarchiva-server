package com.stimulus.archiva.domain;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
public class Agent implements Serializable {

	 /**
	 * 
	 */
	private static final long serialVersionUID = 5539086394563001688L;


	protected static Logger logger = Logger.getLogger(Agent.class);
	  

	LinkedList<String> allowIPAddress = new LinkedList<String>();
	int smtpPort    = 8091;
	boolean auth    = false;
	String username =  "";
	String password =  "";
	boolean tls		= false;
	int milterPort  = 8092;
    
    public void addAllowedIPAddress(String ipAddress) {
    	allowIPAddress.add(ipAddress);
    }
    
    public String getIPAddress(int id) {
    	return (String)allowIPAddress.get(id);
    }
    
    public void setIPAddress(int id, String value) {
    	String ipAddress = (String)allowIPAddress.get(id);
   	 if (ipAddress!=null)
   		 allowIPAddress.set(id, value);
    }
    public void removeIPAddress(int id) {
    	String ipAddress = (String)allowIPAddress.get(id);
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
    
    public void setSMTPPort(int smtpPort) {
    	this.smtpPort = smtpPort;
    }
    
    public int getSMTPPort() { return smtpPort; }
    
    public void setMilterPort(int milterPort) {
    	this.milterPort = milterPort;
    }
    
    public int getMilterPort() {
    	return milterPort;
    }
    
    public void setUsername(String username) { 
    	this.username = username; 
    }
    
    public String getUsername() { return username; }
    
    public void setPassword(String password) {
    	this.password = password;
    }
    
    public String getPassword() { 
    	return password;
    }
    
    public void setTLS(boolean enabled) {
    	this.tls = enabled;
    }
    
    public boolean getTLS() { return tls; }
    
    public void setAuth(boolean enabled) {
    	this.auth = enabled;
    }
    
    public boolean getAuth() { 
    	return auth;
    }
    
    
    
}



