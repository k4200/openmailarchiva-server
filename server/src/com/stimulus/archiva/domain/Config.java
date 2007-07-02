/* Copyright (C) 2005 Jamie Angus Band 
 * This software program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.stimulus.archiva.domain;
import java.io.*;
import org.apache.log4j.Logger;
import java.util.*;
import java.text.SimpleDateFormat;
import com.stimulus.archiva.exception.ConfigurationException;
import com.stimulus.archiva.service.ConfigurationService;
//import java.util.List;

public class Config implements Serializable {

  /* protected Fields */

 protected static final boolean THROW_ON_LOAD_FAILURE = true;
 protected static final boolean LOAD_AS_RESOURCE_BUNDLE = false;
 protected static final int SAVE_WAIT = 5000;
 protected static final Logger logger = Logger.getLogger(Config.class);
 protected static final String archiveInboundKey = "archive.inbound";
 protected static final String archiveOutboundKey = "archive.outbound";
 protected static final String archiveInternalKey = "archive.internal";
 protected static final String archiveRuleActionKey = "archive.rule.action";
 protected static final String archiveRuleFieldKey = "archive.rule.field";
 protected static final String archiveRuleRegExKey = "archive.rule.criterion";
 protected static final String volumePathKey = "volume.store.path";
 protected static final String volumeIndexPathKey = "volume.index.path";
 protected static final String volumeMaxSizeKey = "volume.max.size";
 protected static final String kdcAddressKey  = "authentication.kdc.address";
 protected static final String ldapAddressKey = "authentication.ldap.address";
 protected static final String consoleAuthenticateKey = "console.authenticate";
 protected static final String roleMapKey  = "role.assignment.role";
 protected static final String roleDomainKey = "role.assignment.domain";
 protected static final String roleAttributeKey = "role.assignment.attribute";
 protected static final String roleMatchKey = "role.assignment.criterion";
 protected static final String domainKey = "domain";
 protected static final String saltKey = "security.salt";
 protected static final String passPhraseKey = "security.passhrase";
 protected static final String pbeAlgorithmKey = "security.pbealgorithm";
 protected static final String defaultMessageStorePath = Character.toString(File.separatorChar)+"store";
 protected static final String defaultSearchIndexPath = Character.toString(File.separatorChar)+"index";
 protected static final String defaultArchiveInbound = "yes";
 protected static final String defaultArchiveOutbound = "yes";
 protected static final String defaultArchiveInternal = "yes";
 protected static final String defaultArchiveRuleAction = "archive";
 protected static final String defaultArchiveRuleField = "subject";
 protected static final String defaultArchiveRuleRegEx = "";
 protected static final String defaultVolumePath = Character.toString(File.separatorChar);
 protected static final String defaultVolumeIndexPath = File.separatorChar + "index";
 protected static final int defaultVolumeMaxSize = 30000;
 protected static final String defaultKdcAddress = "activedirectory.company.com:88";
 protected static final String defaultLdapAddress = "activedirectory.company.com:389";
 protected static final String defaultConsoleAuthenticate = "no";
 protected static final String defaultLoginRole = (String)RoleMaps.ROLES.get(0);
 protected static final String defaultLoginDomain = "";
 protected static final String defaultLoginAttribute = (String)RoleMaps.ATTRIBUTES.get(0);
 protected static final String defaultLoginMatch = "";
 protected static final String defaultUserConsoleAccess = "no";
 protected static final String defaultAuditorConsoleAccess = "yes";
 protected static final String defaultDomain = "company.com";
 protected static final String defaultPBEAlgorithm="PBEWithMD5AndTripleDES";
 protected static final String defaultPassPhrase="changeme";
 private static String hexits = "0123456789abcdef";


 protected static final String version="1.1";

 protected static final Object readWriteLock = new Object();

 protected Properties prop;
 protected String 	messageStorePath;
 protected String 	indexPath;
 protected String 	auditPath;
 protected String 	loggingPath;
 protected String 	loggingLevel;
 protected String   installDirectory;
 protected String 	kdcAddress;
 protected String   ldapAddress;
 protected boolean  consoleAuthenticate;
 protected byte[]   salt;
 protected String   passPhrase;
 protected String   pbeAlgorithm;

 protected ArchiveRules archiveRules = new ArchiveRules();
 protected Volumes volumes = new Volumes();
 protected RoleMaps roleMaps = new RoleMaps();
 protected Domains domains = new Domains();
 protected Volume volume = null;
 
 
 protected static Config config = null;
 protected static String applicationPath = null;


  public static void getActiveVolume() {

  }

  public static void setApplicationPath(String applicationPath) {
      if (applicationPath.endsWith(Character.toString(File.separatorChar)))
          Config.applicationPath = applicationPath.substring(0,applicationPath.length()-1);
      else
          Config.applicationPath = applicationPath;
  }

  public static String getApplicationPath() {
      return Config.applicationPath;
  }

  public static String getConfigurationPath() {
      return Config.applicationPath + File.separatorChar + "WEB-INF"+File.separatorChar + "conf";
  }

  public static String getViewPath() {
      return Config.applicationPath + File.separatorChar + "temp";
  }


  public static String getBinPath() {
      return Config.applicationPath + File.separatorChar + "WEB-INF"+File.separatorChar + "bin";
  }


  public static void clearViewDirectory() {
      if (applicationPath==null)
          return;
      logger.debug("clearing view directory {directory='"+getViewPath()+"'}");
      clearDirectory(getViewPath());
  }

  protected String getDateStr(Date date) {
      if (date==null)
          return "";
      SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSS");
  	  return format.format(date);
  }


//deliberately non recursive
  public static void clearDirectory(String path) {
      File indexDir = new File(path);
      if (!indexDir.exists()) return;
      if (indexDir.isDirectory()) {
            String[] children = indexDir.list();
            for (int i=0; i<children.length; i++) {
                String filepath = path + File.separatorChar+children[i];
                logger.debug("deleting file {path='" + filepath +"'}");
                boolean success = new File(filepath).delete();
                if (!success)
                    logger.debug("failed to delete file {path='" + filepath +"'}");
                 else
                     logger.debug("deleted file successfully {path='" + filepath +"'}");
            }
      }
  }


  public static String getApplicationVersion() { return version; }

  public boolean getBoolean(String onoff, String defaultValue) {
  	if (onoff==null) return getBoolean(defaultValue,"yes");
  	if (onoff.toLowerCase().equals("yes")) return true;
  	else if (onoff.toLowerCase().equals("no")) return false;
  	else return getBoolean(defaultValue,"yes");
  }

  private String getString(String str, String defaultValue) {
  	if (str==null) return getString(defaultValue,"");
  	return str;
  }

  private int getInteger(String str, String defaultValue) {
  	if (str==null) return getInteger(defaultValue,"0");
  	return Integer.parseInt(str);
  }

  public String getYesNo(boolean b) {
  	if (b) return "yes"; else return "no";
  }

  /**
	* Convert a byte array to a hex encoded string
	*
	* @param block
	*      byte array to convert to hexString
	* @return String representation of byte array
	*/
	private static String toHex(byte[] block) {
		StringBuffer buf = new StringBuffer();

		for (int i = 0; i < block.length; ++i) {
			buf.append(hexits.charAt((block[i] >>> 4) & 0xf));
			buf.append(hexits.charAt(block[i] & 0xf));
		}

		return buf + "";
	}

	/**
	* Convert a String hex notation to a byte array
	*
	* @param s
	*      string to convert
	* @return byte array
	*/
	private static byte[] fromHex(String s) {
		s = s.toLowerCase();
		byte[] b = new byte[(s.length() + 1) / 2];
		int j = 0;
		int h;
		int nibble = -1;

		for (int i = 0; i < s.length(); ++i) {
			h = hexits.indexOf(s.charAt(i));
			if (h >= 0) {
				if (nibble < 0) {
					nibble = h;
				} else {
					b[j++] = (byte) ((nibble << 4) + h);
					nibble = -1;
				}
			}
		}

		if (nibble >= 0) {
			b[j++] = (byte) (nibble << 4);
		}

		if (j < b.length) {
			byte[] b2 = new byte[j];
			System.arraycopy(b, 0, b2, 0, j);
			b = b2;
		}

		return b;
	}


  private void setSalt(String saltStr) {
      if (saltStr==null) {
	  	  salt = new byte[8];
          new Random().nextBytes(salt);
	  	} else {
	  	  salt = fromHex(saltStr);
	  	}
  }

  public void save() throws ConfigurationException {
	synchronized(readWriteLock) {

	    logger.debug("save()");
	  	prop = new Properties();

	  	prop.setProperty(consoleAuthenticateKey,getYesNo(getConsoleAuthenticate()));
	  	prop.setProperty(ldapAddressKey, getLDAPAddress());
	  	prop.setProperty(kdcAddressKey, getKDCAddress());
	  	prop.setProperty(archiveInboundKey,getYesNo(archiveRules.getArchiveInbound()));
	  	prop.setProperty(archiveOutboundKey,getYesNo(archiveRules.getArchiveOutbound()));
	  	prop.setProperty(archiveInternalKey,getYesNo(archiveRules.getArchiveInternal()));
	  	prop.setProperty(passPhraseKey,getPassPhrase());
	  	prop.setProperty(pbeAlgorithmKey,getPBEAlgorithm());
	  	if (salt!=null)
	  	    prop.setProperty(saltKey,toHex(getSalt()));

		Iterator i = archiveRules.getArchiveRules().iterator();
	  	int c = 1;

		while (i.hasNext()) {
		    ArchiveRules.Rule af = (ArchiveRules.Rule)i.next();
			prop.setProperty(archiveRuleActionKey + "."+c,af.getAction());
			prop.setProperty(archiveRuleFieldKey + "."+c,af.getField());
			prop.setProperty(archiveRuleRegExKey + "."+c,af.getRegEx());
			c++;
		}
		c = 1;

		i = roleMaps.getRoleMaps().iterator();

		while (i.hasNext()) {
			RoleMaps.RoleMap lr = (RoleMaps.RoleMap)i.next();
			prop.setProperty(roleMapKey + "."+c,lr.getRole());
			prop.setProperty(roleDomainKey + "."+c,lr.getDomain());
			prop.setProperty(roleAttributeKey + "."+c,lr.getAttribute());
			prop.setProperty(roleMatchKey + "."+c,lr.getRegEx());
			c++;
		}

		i = volumes.getVolumes().iterator();
	  	c = 1;
		while (i.hasNext()) {
			Volume v = (Volume)i.next();
			prop.setProperty(volumePathKey + "."+c,v.getPath());
			prop.setProperty(volumeIndexPathKey + "."+c,v.getIndexPath());
			prop.setProperty(volumeMaxSizeKey + "."+c,Long.toString(v.getMaxSize()));
			c++;
		}

		i = domains.getDomains().iterator();
		c = 1;
		while (i.hasNext()) {
		    Domains.Domain d = (Domains.Domain)i.next();
			prop.setProperty(domainKey + "."+c, d.getName());
			c++;
		}

		String fileName = applicationPath + File.separatorChar + "WEB-INF"+File.separatorChar + "conf"+ File.separatorChar + "server.conf";

		logger.debug("saving properties {location='"+fileName+"'}");
		logger.debug(prop.toString());
		saveProperties(fileName,prop);
	}

  }

  public void load() throws ConfigurationException {
    synchronized(readWriteLock) {
	  	String fileName = applicationPath + File.separatorChar + "WEB-INF"+File.separatorChar + "conf"+ File.separatorChar + "server.conf";
	  	logger.debug("loading server properties {location='"+fileName+"'}");
	  	prop = loadProperties (fileName);
	  
	  	setKDCAddress(getString(prop.getProperty(kdcAddressKey),defaultKdcAddress));
	  	setLDAPAddress(getString(prop.getProperty(ldapAddressKey),defaultLdapAddress));
	  	setConsoleAuthenticate(getBoolean(prop.getProperty(consoleAuthenticateKey),defaultConsoleAuthenticate));
	  	setSalt(prop.getProperty(saltKey));
	  	setPassPhrase(getString(prop.getProperty(passPhraseKey),defaultPassPhrase));
	    setPBEAlgorithm(getString(prop.getProperty(pbeAlgorithmKey),defaultPBEAlgorithm));
	  	archiveRules.setArchiveInbound(getBoolean(prop.getProperty(archiveInboundKey),defaultArchiveInbound));
	  	archiveRules.setArchiveOutbound(getBoolean(prop.getProperty(archiveOutboundKey),defaultArchiveOutbound));
	  	archiveRules.setArchiveInternal(getBoolean(prop.getProperty(archiveInternalKey),defaultArchiveInternal));
	  	archiveRules.clearAllArchiveRules();

	  	int i = 1;
	  	do {
	  		String ara = prop.getProperty(archiveRuleActionKey + "." + Integer.toString(i));
	  	    String arf = prop.getProperty(archiveRuleFieldKey + "." + Integer.toString(i));
	  	    String arr = prop.getProperty(archiveRuleRegExKey + "." + Integer.toString(i));
	  	    if (ara== null && arf == null && arr == null)
	  	    	break;

	  	  archiveRules.addArchiveRule(getString(ara,defaultArchiveRuleAction),getString(arf,defaultArchiveRuleField),getString(arr,defaultArchiveRuleRegEx));
	  	  i++;
	  	} while(true);

	  	roleMaps.clearAllRoleMaps();
	  	i = 1;
	  	do {
	  		String lr = prop.getProperty(roleMapKey + "." + Integer.toString(i));
	  	    String ld = prop.getProperty(roleDomainKey + "." + Integer.toString(i));
	  	    String la = prop.getProperty(roleAttributeKey + "." + Integer.toString(i));
	  	    String lm = prop.getProperty(roleMatchKey + "." + Integer.toString(i));

	  	    if (lr== null && ld == null && la == null & lm == null)
	  	    	break;

	  	  roleMaps.addRoleMap(getString(lr,defaultLoginRole),getString(ld,defaultLoginDomain),getString(la,defaultLoginAttribute),getString(lm,defaultLoginMatch));
	  	  i++;
	  	} while(true);


	  	volumes.clearAllVolumes();
	  	i = 1;
	  	do {
	  		String vp = prop.getProperty(volumePathKey + "." + Integer.toString(i));
	  	    //String vs = prop.getProperty(volumeSize + "." + Integer.toString(i));
	  	    String vip = prop.getProperty(volumeIndexPathKey + "." + Integer.toString(i));
	  	    String vms = prop.getProperty(volumeMaxSizeKey + "." + Integer.toString(i));

	  	    if (vp== null && vip == null &&  vms == null )
	  	    	break;

	  	    volumes.addVolume(getString(vp,defaultVolumePath),getString(vip,defaultVolumeIndexPath),
	  	                      getInteger(vms, Integer.toString(defaultVolumeMaxSize)));
	  	  i++;
	  	} while(true);

	  	domains.clearAllDomains();
	  	i = 1;
	  	do {
	  	    String domain = prop.getProperty(domainKey + "." + Integer.toString(i));
	  	    if (domain==null) break;
	  	    domains.addDomain(domain);
	  	    i++;

	  	} while(true);
    }
    getVolumes().loadAllVolumeInfo();

    prop.setProperty("security.passhrase","<hidden>");
  	logger.debug(prop.toString());
  }

  protected static Properties loadProperties (final String name) throws ConfigurationException
  {
      Properties result = null;
      InputStream in = null;

      try {
	      result = new Properties ();
	      result.load(new FileInputStream(name));

      } catch (Exception e)
      {
      	  logger.error("failed to load server configuration file, where location='WEB-INF/conf/server.conf'");
          result = new Properties ();
      }
      finally
      {
          if (in != null) try { in.close (); } catch (Throwable ignore) {}
      }

      return result;
  }

  protected static void saveProperties( final String name, Properties prop) throws ConfigurationException {
      try {
          File f = new File(name);
          prop.store(new FileOutputStream(f), null);
      } catch (Exception e) {
          throw new ConfigurationException("failed to save properties. cause:"+e.toString(),e, logger);
      }
  }



  public void setLoggingPath(String loggingPath) { this.loggingPath = loggingPath.toLowerCase(); }

  public String getLoggingPath() { return loggingPath; }

  public void setLoggingLevel(String loggingLevel) { this.loggingLevel = loggingLevel; }

  public String getLoggingLevel() { return loggingLevel; }

  public void setAuditPath(String auditPath) { this.auditPath = auditPath.toLowerCase(); }

  public String getAuditPath() { return auditPath; }

  public void setKDCAddress(String kdcAddress) {
      this.kdcAddress = kdcAddress.toLowerCase();
      if (this.kdcAddress.indexOf(':')==-1)
       this.kdcAddress += ":88";
  }

  public String getKDCAddress() { return kdcAddress; }

  public void setLDAPAddress(String ldapAddress) {
      this.ldapAddress = ldapAddress.toLowerCase();
      if (this.ldapAddress.indexOf(':')==-1)
          this.ldapAddress += ":389";
  }

  public String getLDAPAddress() { return ldapAddress; }

  public boolean getConsoleAuthenticate() { return consoleAuthenticate; }

  public void setConsoleAuthenticate(boolean consoleAuthenticate) { this.consoleAuthenticate = consoleAuthenticate; }


  public Properties getProperties() {
  	return prop;
  }

  public ArchiveRules getArchiveRules() { return archiveRules; }

  public RoleMaps getRoleMaps() { return roleMaps; }

  public Volumes getVolumes() { return volumes; }

  public Domains getDomains() { return domains; }

  public byte[] getSalt() {
          return salt;
 }

  public String getPBEAlgorithm() { return pbeAlgorithm; }

  public String getPassPhrase() { return passPhrase; }

  public void setPassPhrase(String passPhrase) {
      if (passPhrase.trim().length()>0)
          this.passPhrase = passPhrase;
  }
  
  public boolean isDefaultPassPhraseModified() {
      logger.debug("default password {modified='"+(!passPhrase.equalsIgnoreCase("changeme")+"'}"));
      return (!passPhrase.equalsIgnoreCase("changeme"));
  }

  public void setPBEAlgorithm(String pbeAlgorithm) {
      this.pbeAlgorithm = pbeAlgorithm;
  }


  public String toString() {
      return prop.toString();
  }
  
  public int getDefaultVolumeMaxSize() {
      return defaultVolumeMaxSize;
  }
  
  public static synchronized Config getConfig() {
		if (config==null) {
			Config.config = new Config();
		}
		return config;
  }

  public static void setConfig(Config config) {
		Config.config = config;
  }
  
  



}
