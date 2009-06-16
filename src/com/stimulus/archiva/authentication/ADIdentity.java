
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

 package com.stimulus.archiva.authentication;

import java.io.*;
import java.util.*;
import com.stimulus.archiva.authentication.LDAPIdentity.LDAPRoleMap;
import com.stimulus.archiva.domain.*;
import com.stimulus.archiva.domain.Identity.RoleMap;
import com.stimulus.archiva.exception.ConfigurationException;
import com.stimulus.archiva.exception.MessageStoreException;
import com.stimulus.util.*;

import java.net.*;
import java.util.*;
public class ADIdentity extends LDAPIdentity implements Serializable, Cloneable {

	//proxyAddresses","SMTP:(.*)@(.*)");
	private static final long serialVersionUID = 2961646156098906034L;
	public static final List<String> ATTRIBUTES;
	public static final List<String> ATTRIBUTE_LABELS;

	static {


		List<String> AttributeList = new LinkedList<String>();
		AttributeList.add("memberOf");
		AttributeList.add("userPrincipalName");
		AttributeList.add("sAMAccountName");
		AttributeList.add("distinguishedName");
		ATTRIBUTES = Collections.unmodifiableList(AttributeList);

		List<String> AttributeLabelList = new LinkedList<String>();
		AttributeLabelList.add("role.attribute.memberof");
		AttributeLabelList.add("role.attribute.userprincipalname");
		AttributeLabelList.add("role.attribute.samAccountname");
		AttributeLabelList.add("role.attribute.distinguishedname");
		ATTRIBUTE_LABELS = Collections.unmodifiableList(AttributeLabelList);

	}

	protected static final String adKDCAddressKey  				= "authentication.kdc.address";
    protected static final String defaultADKdcAddress 			= "activedirectory.company.com:88";
    protected static final String defaultADLdapAddress 			= "activedirectory.company.com:389";
    protected static final String defaultADLoginAttribute 		= ADIdentity.ATTRIBUTES.get(0);
    protected static final String defaultADLoginMatch 			= "";

    protected String kdcAddress;
	protected String kdcIPAddress;
    protected static final String LDAPAddressKey 			= "authentication.ldap.address";
	protected static final String baseDNKey  				= "authentication.base.dn";
	protected static final String serviceDNKey  			= "authentication.service.dn";
	protected static final String servicePasswordKey  		= "authentication.service.password";
	protected static final String bindAttributeKey			= "authentication.bind.attribute";
	protected static final String emailAttributeKey 		= "authentication.emailaddress.attribute";
	protected static final String roleMapKey  				= "authentication.role.assignment.role";
	protected static final String roleDomainKey 			= "authentication.role.assignment.domain";
	protected static final String roleAttributeKey 			= "authentication.role.assignment.attribute";
	protected static final String roleMatchKey 				= "authentication.role.assignment.criterion";

    protected static final String defaultLdapAddress 		= "activedirectory.company.com:389";
    protected static final String defaultBaseDN  			= "dc=company,dc=com";
    protected static final String defaultBindAttribute	  	= "SAMAccountName";
    protected static final String defaultEmailAttribute		= "ProxyAddresses";
    protected static final String defaultServicePassword    = "";
    protected static final String defaultServiceDN			= "admin@company.com";

	public ADIdentity() {

	}

	public ADIdentity(String ldapAddress, String kdcAddress, String kdcIPAddress) {
		this.ldapAddress = ldapAddress;
		this.kdcAddress = kdcAddress;
		this.kdcIPAddress = kdcIPAddress;
	}

	public String getEmailAttribute() {
		return "proxyAddresses";
	}

	public String getEmailValue() {
		return "SMTP:(.*)";
	}

	public String getKDCAddress() { return kdcAddress; }

    public void setKDCAddress(String kdcAddress) {
        this.kdcAddress = kdcAddress.toLowerCase(Locale.ENGLISH);
        if (this.kdcAddress.indexOf(':')==-1)
        	this.kdcAddress += ":88";
    }

    public void setKDCIPAddress(String kdcIPAddress) {
    	this.kdcIPAddress = kdcIPAddress.toLowerCase(Locale.ENGLISH);
    	int i = this.kdcIPAddress.indexOf(':');
    	 if (i!=-1) {
    		 this.kdcIPAddress = this.kdcIPAddress.substring(0,i).toLowerCase(Locale.ENGLISH);
    	 }
    }

    public String getKDCIPAddress() {
    	return kdcIPAddress;
    }

    public String getLDAPAddress() { return ldapAddress; }

    public void setLDAPAddress(String ldapAddress) {
        this.ldapAddress = ldapAddress.toLowerCase(Locale.ENGLISH);
        if (this.ldapAddress.indexOf(':')==-1)
            this.ldapAddress += ":389";
    }

    protected File getHostsFile() throws FileNotFoundException {
    	File hostsFile;
    	String os = System.getProperty("os.name");
        if (os.toLowerCase(Locale.ENGLISH).startsWith("windows")) {
        	 hostsFile = new File(System.getenv("WINDIR")+File.separator+"System32"+File.separator+"drivers"+
        						   File.separator + "etc" + File.separator + "hosts");
        } else {
        	 hostsFile = new File("/etc/hosts");
        }
        return hostsFile;
    }

    protected String getKDCFDQN() {
    	String kdcFQDN;
    	int i = kdcAddress.indexOf(':');
        if (i!=-1) {
        	kdcFQDN = kdcAddress.substring(0,i).toLowerCase(Locale.ENGLISH);
        } else {
        	kdcFQDN = kdcAddress.toLowerCase(Locale.ENGLISH);
        }
        return kdcFQDN;
    }

    protected String getKDCServerName() {
    	String kdcfdqn = getKDCFDQN();
    	int i = kdcAddress.indexOf('.');
    	if (i==-1) {
    		return "";
    	} else {
    		return kdcfdqn.substring(0,i).toLowerCase(Locale.ENGLISH);
    	}
    }

    public synchronized void clearHostsFileEntries() {
    	PrintStream pw = null;
    	try {
    		pw = new PrintStream(new FileOutputStream(getHostsFile()));
    		try { pw.close(); } catch (Exception e) {}
    	} catch (Exception e) {
          	logger.error("failed to clear entries from hosts file:"+e.getMessage(),e);
        } finally {
        	if (pw!=null)
    			try { pw.close(); } catch (Exception e) {}
        }
    }

    public synchronized void loadHostsFileEntry() {
    	kdcIPAddress = "127.0.0.1";
    	String kdcFQDN = getKDCFDQN().toLowerCase(Locale.ENGLISH);
    	BufferedReader br = null;
    	try {
	    	br = new BufferedReader(new InputStreamReader(new FileInputStream(getHostsFile())));
	    	String in = br.readLine( );
			while (in != null) {
				if (in.toLowerCase(Locale.ENGLISH).contains(kdcFQDN)) {
					StringTokenizer stoken = new StringTokenizer(in);
					kdcIPAddress = stoken.nextToken();
				}
				in = br.readLine();
			}
    	 } catch (Exception e) {
          	logger.error("failed to retrieve AD KDC IP address from hosts file:"+e.getMessage(),e);
         } finally {
        	 if (br!=null)
	    			try { br.close(); } catch (Exception e) {}
         }
    }

    public synchronized void saveHostsFileEntry() {

    	if (kdcIPAddress==null || kdcIPAddress.length()<1) {
    		loadHostsFileEntry();
    		if (kdcIPAddress==null || kdcIPAddress.length()<1) {
    			logger.debug("did not KDC IP address as it is not specified");
    			return;
    		}
    	}
    	String kdcFQDN = getKDCFDQN().toLowerCase(Locale.ENGLISH);
    	String kdcServerName = getKDCServerName();
    	BufferedReader br = null;
    	PrintStream pw = null;
    	try {
	    	br = new BufferedReader(new InputStreamReader(new FileInputStream(getHostsFile())));
	    	String in = br.readLine( );
	    	ArrayList<String> entries = new ArrayList<String>();
			while (in != null) {
				entries.add(in);
				in = br.readLine();
			}
			br.close();
			pw = new PrintStream(new FileOutputStream(getHostsFile()));
			boolean found = false;
			for (String entry : entries) {
				if (entry.toLowerCase(Locale.ENGLISH).contains(kdcFQDN)) {
					pw.println(kdcIPAddress+" "+kdcServerName.toUpperCase(Locale.ENGLISH)+" "+kdcFQDN.toUpperCase(Locale.ENGLISH));

					found = true;
				} else {
					pw.println(entry);
				}
			}
			if (!found) {
				pw.println(kdcIPAddress+" "+kdcFQDN.toUpperCase(Locale.ENGLISH));
			}

    	 } catch (Exception e) {
         	logger.error("failed to retrieve AD KDC IP address from hosts file:"+e.getMessage(),e);
         	return;
         } finally {
        	if (br!=null)
	    			try { br.close(); } catch (Exception e) {}
			if (pw!=null)
    			try { pw.close(); } catch (Exception e) {}

         }

    }

	public void newRoleMap() throws ConfigurationException {
		addRoleMap(new ADRoleMap(Roles.ADMINISTRATOR_ROLE.getName(), "", ""));
	}

	public void addRoleMap(String role, String attribute, String regex)
			throws ConfigurationException {
		addRoleMap(new ADRoleMap(role, attribute, regex));
	}

    public boolean loadSettings(String prefix, Settings prop, String suffix) {
    	logger.debug("loading ad identity");
	  	// active directory identity info

	 	setKDCAddress(ConfigUtil.getString(prop.getProperty(adKDCAddressKey),defaultADKdcAddress));
	 	setLDAPAddress(ConfigUtil.getString(prop.getProperty(LDAPAddressKey),defaultLdapAddress));
	 	setBaseDN(ConfigUtil.getString(prop.getProperty(baseDNKey),defaultBaseDN));
	 	setServiceDN(ConfigUtil.getString(prop.getProperty(serviceDNKey),defaultServiceDN));
	  	setBindAttribute(ConfigUtil.getString(prop.getProperty(bindAttributeKey),defaultBindAttribute));
	  	setEmailAttribute(ConfigUtil.getString(prop.getProperty(emailAttributeKey),defaultEmailAttribute));
	  	String encryptedPassword = ConfigUtil.getString(prop.getProperty(servicePasswordKey),defaultServicePassword);
    	if (!encryptedPassword.endsWith("=")) {
    		setServicePassword(encryptedPassword);
        } else {
        	try {
        		setServicePassword(Crypto.decryptPassword(encryptedPassword));
        	} catch (MessageStoreException mse) {
        		logger.error("failed to load active directory service account pass phrase",mse);
        	}
        }

	  	clearAllRoleMaps();

	  	int c = 1;
	  	boolean load = false;
	  	do {
	  		RoleMap rm = new ADRoleMap();
	  		load = rm.loadSettings(null,prop,"."+c++);
	  		if (load) addRoleMap(rm);
	  	} while (load);

        return true;
    }


    public void saveSettings(String prefix, Settings prop, String suffix) {
    	logger.debug("saving ad identity");
	  	prop.setProperty(adKDCAddressKey, getKDCAddress());
	  	prop.setProperty(LDAPAddressKey, getLDAPAddress());
	  	prop.setProperty(baseDNKey, getBaseDN());
	  	prop.setProperty(serviceDNKey, getServiceDN());
	  	prop.setProperty(bindAttributeKey, getBindAttribute());
	  	prop.setProperty(emailAttributeKey, getEmailAttribute());

	    if (getServicePassword()!=null) {
	        // if raw password is hash value, we know to see the passphrase
	        try {
	        		prop.setProperty(servicePasswordKey,Crypto.encryptPassword(getServicePassword()));
        	} catch (MessageStoreException mse) {
        		logger.error("failed to save active directory service account pass phrase",mse);
        	}

        }

	  	int c = 1;
	  	for (RoleMap rolemap : getRoleMaps()) {
	  		rolemap.saveSettings(null,prop,"."+c++);
	  	}

    }


    public class ADRoleMap extends LDAPIdentity.LDAPRoleMap implements Props, Serializable, Cloneable {


    	 private static final long serialVersionUID = 1161047232198102210L;

    	 public ADRoleMap() { }

 		public ADRoleMap(String role, String attribute, String regex) {
 			setRole(role);
 			setAttribute(attribute);
 			setRegEx(regex);
 		}


    	 public void saveSettings(String prefix, Settings prop, String suffix) {
    		  prop.setProperty(roleMapKey + suffix,getRole());
			  prop.setProperty(roleAttributeKey + suffix,getAttribute());
			  prop.setProperty(roleMatchKey + suffix,getRegEx());
    	 }


		 public boolean loadSettings(String prefix, Settings prop, String suffix) {
			 String lr = prop.getProperty(roleMapKey + suffix);
		  	     String la = prop.getProperty(roleAttributeKey + suffix);
		  	     String lm = prop.getProperty(roleMatchKey + suffix);

		  	     if (lr== null || la == null || lm == null)
		  	    	return false;

		  	    try {
		  	    	setRoleValue(ConfigUtil.getString(lr,defaultLoginRole));
		  	    } catch (ConfigurationException ce) {
		  	    	return false;
		  	    }

		  	    setAttribute(ConfigUtil.getString(la,defaultLoginAttribute));
		  	    setRegEx(ConfigUtil.getString(lm,defaultLoginMatch));

		  	    return true;

		 }

		 public ADRoleMap clone() {
			 return new ADRoleMap(role,attribute,regex);
		 }
    }

	public String toString() {
		 return "serviceDN='"+serviceDN+"',servicePassword='"+servicePassword+"',ldapAddress='"+ldapAddress+"',bindAttribute='"+bindAttribute+
		 		"',uidValue='"+uidValue+"',emailAttribute='"+emailAttribute+"'";
	 }

	public ADIdentity clone() {
		ADIdentity adIdentity = new ADIdentity();
		adIdentity.setLDAPAddress(ldapAddress);
		adIdentity.setKDCAddress(kdcAddress);
		adIdentity.setKDCIPAddress(kdcIPAddress);
		adIdentity.setBindAttribute(bindAttribute);
		adIdentity.setEmailAttribute(emailAttribute);
		adIdentity.setBaseDN(baseDN);
		adIdentity.setServiceDN(serviceDN);
		adIdentity.setServicePassword(servicePassword);
		for (RoleMap roleMap : getRoleMaps()) {
			adIdentity.addRoleMap(((ADRoleMap)roleMap).clone());
		}
		return adIdentity;
	}

}
