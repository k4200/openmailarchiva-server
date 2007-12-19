
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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;

import com.stimulus.archiva.authentication.ADIdentity;
import com.stimulus.archiva.authentication.BasicIdentity;
import com.stimulus.archiva.authentication.LDAPIdentity;
import com.stimulus.archiva.exception.ConfigurationException;
import com.stimulus.util.Compare;
import com.stimulus.util.DateUtil;
import com.stimulus.util.TempFiles;

public class Config implements Serializable {

	static final long serialVersionUID = 4243937061206625954L;

  /* protected Fields */
   

     public static final List<String> VOLUME_STATUS;
     protected static final boolean THROW_ON_LOAD_FAILURE = true;
     protected static final boolean LOAD_AS_RESOURCE_BUNDLE = false;
     protected static final int SAVE_WAIT = 5000;
     protected static Logger logger = Logger.getLogger(Config.class);
  
     
     protected static final String agentAllowedIPKey 			   = "agent.allowed.ipaddress";
     protected static final String agentAuthKey 			   	   = "agent.authentication";
     protected static final String agentUsernameKey 			   = "agent.username";
     protected static final String agentPasswordKey 			   = "agent.password";
     protected static final String agentTLSKey 			   	   	   = "agent.tls";
     protected static final String agentPortKey 			   	   = "agent.port";
     protected static final String agentMilterPortKey 			   = "agent.milter.port";
    	 
     protected static final String exportMaxMessagesKey		   = "export.max.messages";
     protected static final String viewMaxMessagesKey		   = "view.max.messages";
     protected static final String deleteMaxMessagesKey		   = "delete.max.messages";
     protected static final String sendMaxMessagesKey		   = "send.max.messages";
     
     protected static final String retentionAutoPurgeKey 	= "retention.autopurge";
     protected static final String retentionKeepDaysKey     = "retention.autopurge.keepdays";
     protected static final String smtpServerAddressKey     = "smtp.serveraddress";
     protected static final String smtpUsernameKey	    	= "smtp.username";
     protected static final String smtpPasswordKey          = "smtp.password";
     protected static final String diskSpaceCheckWaitKey 	= "volume.diskspace.wait";
     protected static final String diskSpaceWarnKey  	  	= "volume.diskspace.warn";
     protected static final String diskSpaceThresholdKey 	= "volume.diskspace.threshold";
     protected static final String diskSpaceCheckKey		= "volume.diskspace.check";
     protected static final String archiveInboundKey 		= "archive.inbound";
     protected static final String archiveOutboundKey 		= "archive.outbound";
     protected static final String archiveInternalKey 		= "archive.internal";
     protected static final String archiveRuleActionKey 	= "archive.rule.action";
     protected static final String archiveRuleFieldKey 		= "archive.rule.field";
     protected static final String archiveRuleRegExKey 		= "archive.rule.criterion";
     protected static final String volumePathKey 			= "volume.store.path";
     protected static final String volumeNameKey 			= "volume.name";
     protected static final String volumeIndexPathKey 		= "volume.index.path";
     protected static final String volumeMaxSizeKey 		= "volume.max.size";
     protected static final String volumeRemoteSearchKey 	= "volume.remote.search";
     
     protected static final String adKDCAddressKey  			= "authentication.kdc.address";
     protected static final String adLDAPAddressKey 			= "authentication.ldap.address";
     protected static final String adRoleMapKey  				= "role.assignment.role";
     protected static final String adRoleDomainKey 				= "role.assignment.domain";
     protected static final String adRoleAttributeKey 			= "role.assignment.attribute";
     protected static final String adRoleMatchKey 				= "role.assignment.criterion";
     protected static final String adEmailMappingAttributeKey 	= "emailaddress.map.attribute";
     protected static final String adEmailMappingPatternKey 	= "emailaddress.map.pattern";
     
     protected static final String ldapLDAPAddressKey 			= "ldap.authentication.ldap.address";
     protected static final String ldapBindDNKey  				= "ldap.binddn";
     protected static final String ldapRoleMapKey  				= "role.assignment.role";
     protected static final String ldapRoleDomainKey 			= "ldap.role.assignment.domain";
     protected static final String ldapRoleAttributeKey 		= "ldap.role.assignment.attribute";
     protected static final String ldapRoleMatchKey 			= "ldap.role.assignment.criterion";
     protected static final String ldapEmailMappingAttributeKey = "ldap.emailaddress.map.attribute";
     protected static final String ldapEmailMappingPatternKey 	= "ldap.emailaddress.map.pattern";
     
     protected static final String domainKey 				= "domain";
     protected static final String saltKey 					= "security.salt";
     protected static final String passPhraseKey 			= "security.passhrase";
     protected static final String authMethodKey 			= "security.loginmethod";
     protected static final String pbeAlgorithmKey 			= "security.pbealgorithm";
     protected static final String maxSearchResultsKey 		= "search.maxresults";
     protected static final String searchAnalyzerLanguageKey	= "search.analyzer.language";
 
     protected static final String searchAnalyzerClassKey 		= "search.analyzer.class";
     protected static final String indexLanguageKey 			= "index.language";
     protected static final String indexLanguageDetectionKey 	= "index.language.detect";
     protected static final String smartAttachmentStorageKey    = "smart.attachment.storage";
     protected static final String smartAttachmentMinSizeKey    = "smart.attachment.minimum.size";
     protected static Map<String,String> defaultSearchAnalyzers;
     protected static final String permissionRoleKey 	 		= "permission.role";
     protected static final String permissionAllowDeleteKey 	= "permission.allow.delete";
     protected static final String permissionAllowViewKey 		= "permission.allow.view";
     protected static final String permissionAllowPrintKey 		= "permission.allow.print";
     protected static final String permissionAllowExportKey 	= "permission.allow.export";
     protected static final String permissionAllowSendKey 		= "permission.allow.send";
     protected static final String permissionAllowSaveKey       = "permission.allow.save";
  
     protected static final String indexAttachmentsKey			 	= "index.attachments";
     protected static final String indexMessageBodyKey				= "index.messagebody";
     protected static final String indexThreadsKey					= "index.threads";			
     
     protected static final String maxMessageSizeKey				= "max.message.size";	
     
     static {
         Map<String,String> analyzerMap = new LinkedHashMap<String,String>();
         analyzerMap.put("en","com.stimulus.archiva.search.ArchivaAnalyzer");
         analyzerMap.put("pt","org.apache.lucene.analysis.br.BrazilianAnalyzer");
         analyzerMap.put("zh","org.apache.lucene.analysis.cn.ChineseAnalyzer");    
         analyzerMap.put("cs","org.apache.lucene.analysis.cz.CzechAnalyzer");    
         analyzerMap.put("de","org.apache.lucene.analysis.de.GermanAnalyzer");    
         analyzerMap.put("el","org.apache.lucene.analysis.el.GreekAnalyzer");   
         analyzerMap.put("fr","org.apache.lucene.analysis.fr.FrenchAnalyzer");
         analyzerMap.put("nl","org.apache.lucene.analysis.nl.DutchAnalyzer");
         analyzerMap.put("ru","org.apache.lucene.analysis.ru.RussianAnalyzer");
         analyzerMap.put("ja","org.apache.lucene.analysis.cjk.CJKAnalyzer");
         analyzerMap.put("ko","org.apache.lucene.analysis.cjk.CJKAnalyzer");
         analyzerMap.put("th","org.apache.lucene.analysis.th.ThaiAnalyzer");
         analyzerMap.put("tr","org.apache.lucene.analysis.tr.TurkishAnalyzer");
         defaultSearchAnalyzers = Collections.unmodifiableMap(analyzerMap);
         
         ArrayList<String> status = new ArrayList<String>();
         status.add("CLOSED");
         status.add("ACTIVE");
         status.add("UNUSED");
         status.add("NEW");
         VOLUME_STATUS = Collections.unmodifiableList(status);
        
     }
     
     protected static final String defaultMaxMessageSize		= "100"; // mb 
     protected static final String defaultAgentAuth				= "no";
     protected static final String defaultAgentUsername  	    = "mailarchiva";
     protected static final String defaultAgentPassword			= "password";
     protected static final String defaultAgentTLS				= "no";
     protected static final String defaultAgentPort				= "8091";
     protected static final String defaultAgentMilterPort		= "8092";
     protected static final String defaultRetentionAutoPurge    = "no";
     protected static final String defaultRetentionKeepDays		= "2560";
     protected static final String defaultPermissionRole    	= Identity.getRoles().get(0);
     protected static final String defaultPermissionAllow	    = "yes";
     protected static final String defaultExportMaxMessages		= "100";
     protected static final String defaultViewMaxMessages		= "100";
     protected static final String defaultDeleteMaxMessages		= "100";
     protected static final String defaultSendMaxMessages		= "100";
     protected static final String defaultSmtpServerAddress	    = "localhost";
     protected static final String defaultSmtpUsername			= "";
     protected static final String defaultSmtpPassword          = "";
     protected static final String defaultDiskSpaceCheckWait	= "1800"; // seconds
     protected static final String defaultDiskSpaceWarn  	  	= "150"; // megabytes
     protected static final String defaultDiskSpaceThreshold 	= "1"; // megabytes
     protected static final String defaultDiskSpaceCheck		= "yes";
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
     protected static final String defaultVolumeRemoteSearch = "no";
     
     protected static final String defaultAuthMethod = "basic";
     
     protected static final String defaultADKdcAddress = "activedirectory.company.com:88";
     protected static final String defaultADLdapAddress = "activedirectory.company.com:389";
     protected static final String defaultADLoginRole = (String)Identity.ROLES.get(0);
     protected static final String defaultADLoginAttribute = (String)ADIdentity.ATTRIBUTES.get(0);
     protected static final String defaultADLoginMatch = "";
   
     protected static final String defaultLdapLdapAddress = "openldap.company.com:389";
     protected static final String defaultLdapLoginRole = (String)Identity.ROLES.get(0);
     protected static final String defaultLdapLoginAttribute = (String)ADIdentity.ATTRIBUTES.get(0);
     protected static final String defaultLdapLoginMatch = "";
     protected static final String defaultLdapPassword = "";
     protected static final String defaultldapBindDN  = "%w";
     protected static final String defaultConsoleAuthenticate = "no";
     protected static final String defaultUserConsoleAccess = "no";
     protected static final String defaultAuditorConsoleAccess = "yes";
     protected static final String defaultDomain = "company.com";
     protected static final String defaultPBEAlgorithm="PBEWithMD5AndDES";
     protected static final String defaultPassPhrase="changeme";
     protected static final String defaultMaxSearchResults="100000";
     protected static final String defaultIndexLanguage = "en";
     protected static final String defaultIndexLanguageDetection = "no";
     protected static final String defaultSmartAttachmentStorage = "yes";
     protected static final int defaultSmartAttachmentMinSize = 32768;
     protected static final String defaultIndexAttachments = "yes";
     protected static final String defaultIndexMessageBody = "yes";
     protected static final String defaultIndexThreads = "4";
  
    
     protected static TempFiles tempFiles = null;
     
     static {
    	 tempFiles = new TempFiles();
    	 tempFiles.startDaemon();
     }
     
     private static String hexits = "0123456789abcdef";
     protected static final String version="1.6.4";
     protected static final transient Object readWriteLock = new Object();
     protected MailArchivaProperties prop;
     protected String 	messageStorePath;
     protected String 	indexPath;
     protected String 	auditPath;
     protected String 	loggingPath;
     protected String 	loggingLevel;
     protected String   installDirectory;
     protected String 	adKDCAddress;
     protected String   ldapAddress;
     protected byte[]   salt;
     protected String   passPhrase;
     protected String   pbeAlgorithm;
     protected int      maxSearchResults;
     protected String   loginPassword;
     protected AuthMethod	authMethod = AuthMethod.BASIC;
	 public enum AuthMethod { BASIC, ACTIVEDIRECTORY};
     protected ArchiveRules archiveRules = new ArchiveRules();
     protected Volumes volumes = null;
     protected BasicIdentity basicIdentity = new BasicIdentity();
     protected ADIdentity adIdentity = new ADIdentity();
     protected LDAPIdentity ldapIdentity = new LDAPIdentity();
     protected Domains domains = new Domains();
     protected Volume volume = null;
     protected Map<String,String> searchAnalyzers;
     protected String indexLanguage = defaultIndexLanguage;
     protected boolean indexLanguageDetection = true;
     protected static Config config = null;
     protected static String applicationPath = null;
     protected boolean smartAttachmentStorage = false;
     protected int smartAttachmentMinSize = 0;
     protected String smtpUsername = "";
     protected String smtpPassword = "";
     protected String smtpServerAddress = "localhost";
     protected Agent agent = new Agent();
     protected boolean indexAttachments = true;
     protected boolean indexMessageBody = true;
     protected int indexThreads = 1;
     protected int maxMessageSize = 20;
     
     protected Permission permission = new Permission();
     protected int exportMaxMessages	= 100;
     protected int viewMaxMessages 		= 100;
     protected int deleteMaxMessages	= 100;	
     protected int sendMaxMessages 		= 100;
     
     public Config() {
         searchAnalyzers = new LinkedHashMap<String,String>(defaultSearchAnalyzers);
        
     }
     
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
      
      public static String getNoIndexPath() {
    	  return Config.applicationPath + File.separatorChar + "WEB-INF"+File.separatorChar + "noindex";
      }
      
      public static String getNoArchivePath() {
    	  return Config.applicationPath + File.separatorChar + "WEB-INF"+File.separatorChar + "noarchive";
      }
      
      public static String getQuarantinePath() {
    	  return Config.applicationPath + File.separatorChar + "WEB-INF"+File.separatorChar + "quarantine";
      }
    
      public static String getClassesPath() {
    	  return Config.applicationPath + File.separatorChar + "WEB-INF"+ File.separatorChar + "classes";
    	  
      }
    
      public static void clearViewDirectory() {
          if (applicationPath==null)
              return;
          logger.debug("clearing view directory {directory='"+getViewPath()+"'}");
          clearDirectory(getViewPath());
      }
      
      public static void clearTempDirectory() {
	    	  String tmpDir = getTempPath();
	    	  logger.debug("clearing temporary directory {directory='"+tmpDir+"'}");
	    	  clearDirectory(tmpDir);
      }
      
      public static String getTempPath() {
    	   String tmpDir = System.getProperty("java.io.tmpdir");
    	   if (tmpDir==null || tmpDir.length()<2)
    		   return File.separatorChar + "tmp";
    	   else
    		   return tmpDir;
      }
    
      protected String getDateStr(Date date) {
          if (date==null)
              return "";
          return DateUtil.convertDatetoString(date);
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
      	if (onoff.toLowerCase(Locale.ENGLISH).equals("yes")) return true;
      	else if (onoff.toLowerCase(Locale.ENGLISH).equals("no")) return false;
      	else return getBoolean(defaultValue,"yes");
      }
    
      private String getString(String str, String defaultValue) {
      	if (str==null) return getString(defaultValue,"");
      	return str;
      }
    
      private int getInteger(String str, String defaultValue) {
      	int i = Integer.parseInt(defaultValue);
      	if (str==null)
      		return i;
      	try {
      		i = Integer.parseInt(str);
      	} catch (Exception e) {
      		logger.error("failed to parse configuration value {str='"+str+"'}");
      	}
      	return i;
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
    		s = s.toLowerCase(Locale.ENGLISH);
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
        	  // we fake the salt for the moment, as there is too much of a risk that
        	  // the admin will lose their entire store because they forgot to 
        	  // copy the server.conf file (which contains their salt value)
    	  	  //salt = new byte[8];
              //new Random().nextBytes(salt);
        	  salt = fromHex("feadf944dd4d62a5");
    	  	} else {
    	  	  salt = fromHex(saltStr);
    	  	}
      }
    
      public void save() throws ConfigurationException {
    	synchronized(readWriteLock) {
    
    	    logger.debug("save()");
    	    
    	  	prop = new MailArchivaProperties();
    	
    	    
    	  	prop.setProperty(smtpServerAddressKey, getSmtpServerAddress());
    	  	prop.setProperty(smtpUsernameKey, getSmtpUsername());
    	  	prop.setProperty(smtpPasswordKey, getSmtpPassword());
    	  	
    	 
    	  	prop.setProperty(diskSpaceCheckWaitKey, Integer.toString(volumes.getDiskSpaceCheckWait()));
    	  	prop.setProperty(diskSpaceWarnKey,Integer.toString(volumes.getDiskSpaceWarnBytes()));
    	  	prop.setProperty(diskSpaceThresholdKey,Integer.toString(volumes.getDiskSpaceThresholdBytes()));
    	  	prop.setProperty(diskSpaceCheckKey,getYesNo(volumes.getDiskSpaceChecking()));
    	  			
    	
    	  	prop.setProperty(archiveInboundKey,getYesNo(archiveRules.getArchiveInbound()));
    	  	prop.setProperty(archiveOutboundKey,getYesNo(archiveRules.getArchiveOutbound()));
    	  	prop.setProperty(archiveInternalKey,getYesNo(archiveRules.getArchiveInternal()));
    	  	prop.setProperty(passPhraseKey,getPassPhrase());
            prop.setProperty(maxSearchResultsKey, Integer.toString(getMaxSearchResults()));
    	  	prop.setProperty(pbeAlgorithmKey,getPBEAlgorithm());
    		prop.setProperty(authMethodKey,authMethod.toString().toLowerCase(Locale.ENGLISH));
			
			
    	  	if (salt!=null)
    	  	    prop.setProperty(saltKey,toHex(getSalt()));
    
    		Iterator i = archiveRules.getArchiveRules().iterator();
    	  	int c = 1;
    
    		while (i.hasNext()) {
    		    ArchiveRules.Rule af = (ArchiveRules.Rule)i.next();
    			prop.setProperty(archiveRuleActionKey + "."+c,af.getAction().toString().toLowerCase(Locale.ENGLISH));
    			prop.setProperty(archiveRuleFieldKey + "."+c,af.getField().toString().toLowerCase(Locale.ENGLISH));
    			prop.setProperty(archiveRuleRegExKey + "."+c,af.getRegEx());
    			c++;
    		}
    		c = 1;
    
    		
			// Active Directory identity info
    		
    	  	prop.setProperty(adLDAPAddressKey, adIdentity.getLDAPAddress());
    	  	prop.setProperty(adKDCAddressKey, adIdentity.getKDCAddress());
    	  	
    		i = adIdentity.getRoleMaps().iterator();
    
    		while (i.hasNext()) {
    			LDAPIdentity.LDAPRoleMap lr = (LDAPIdentity.LDAPRoleMap)i.next();
    			prop.setProperty(adRoleMapKey + "."+c,lr.getRole());
    			prop.setProperty(adRoleAttributeKey + "."+c,lr.getAttribute());
    			prop.setProperty(adRoleMatchKey + "."+c,lr.getRegEx());
    			c++;
    		}
    		i = adIdentity.getEmailMappings().entrySet().iterator();
            c = 1;
            while (i.hasNext()) {
                Map.Entry adEmailMapping = (Map.Entry)i.next();
                prop.setProperty(adEmailMappingAttributeKey + "."+c, (String)adEmailMapping.getKey());
                prop.setProperty(adEmailMappingPatternKey + "."+c, (String)adEmailMapping.getValue());
                c++;
            }
            
            // Active Directory identity info
    		
    	  	prop.setProperty(ldapLDAPAddressKey, ldapIdentity.getLDAPAddress());
    	  	prop.setProperty(ldapBindDNKey, ldapIdentity.getBindDN());

    	  	
    		i = ldapIdentity.getRoleMaps().iterator();
    		c = 1;
    		while (i.hasNext()) {
    			LDAPIdentity.LDAPRoleMap lr = (LDAPIdentity.LDAPRoleMap)i.next();
    			prop.setProperty(ldapRoleMapKey + "."+c,lr.getRole());
    			prop.setProperty(ldapRoleAttributeKey + "."+c,lr.getAttribute());
    			prop.setProperty(ldapRoleMatchKey + "."+c,lr.getRegEx());
    			c++;
    		}
    		i = ldapIdentity.getEmailMappings().entrySet().iterator();
            c = 1;
            while (i.hasNext()) {
                Map.Entry ldapEmailMapping = (Map.Entry)i.next();
                prop.setProperty(ldapEmailMappingAttributeKey + "."+c, (String)ldapEmailMapping.getKey());
                prop.setProperty(ldapEmailMappingPatternKey + "."+c, (String)ldapEmailMapping.getValue());
                c++;
            }
            
            
            basicIdentity.save();
            
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
              
            i = searchAnalyzers.entrySet().iterator();
            c = 1;
            while (i.hasNext()) {
                Map.Entry searchAnalyzer = (Map.Entry)i.next();
                prop.setProperty(searchAnalyzerLanguageKey + "."+c, (String)searchAnalyzer.getKey());
                prop.setProperty(searchAnalyzerClassKey + "."+c, (String)searchAnalyzer.getValue());
                c++;
            }
         
            i = permission.getPermissions().iterator();
            c = 1;
            
            while (i.hasNext()) {
            	
            	Permission.PermissionRoleMap rolemap = (Permission.PermissionRoleMap)i.next();
            	prop.setProperty(permissionRoleKey + "."+c, (String)rolemap.getRole());
            	prop.setProperty(permissionAllowDeleteKey+ "."+c, getYesNo(rolemap.getAllowDelete()));
            	prop.setProperty(permissionAllowViewKey+ "."+c, getYesNo(rolemap.getAllowView()));
            	prop.setProperty(permissionAllowPrintKey+ "."+c, getYesNo(rolemap.getAllowPrint()));
            	prop.setProperty(permissionAllowExportKey+ "."+c, getYesNo(rolemap.getAllowExport()));
            	prop.setProperty(permissionAllowSaveKey+ "."+c, getYesNo(rolemap.getAllowSave()));
            	prop.setProperty(permissionAllowSendKey+ "."+c, getYesNo(rolemap.getAllowSend()));
            	c++;
            	
            }
    
    		String fileName = applicationPath + File.separatorChar + "WEB-INF"+File.separatorChar + "conf"+ File.separatorChar + "server.conf";
    	
    		prop.setProperty(indexLanguageDetectionKey,getYesNo(indexLanguageDetection));
    		prop.setProperty(indexLanguageKey,indexLanguage);
    		
    	
    		c = 1;
    		i = agent.getIPAddresses().iterator();
    		while (i.hasNext()) {
    		    String ipAddress  = (String)i.next();
    			prop.setProperty(agentAllowedIPKey + "."+c, ipAddress);
    			c++;
    		}
    		
    	    prop.setProperty(agentAuthKey, getYesNo(agent.getAuth()));
    	    prop.setProperty(agentTLSKey, getYesNo(agent.getTLS()));
    	    prop.setProperty(agentUsernameKey, agent.getUsername());
    	    prop.setProperty(agentPasswordKey, agent.getPassword());
    	    prop.setProperty(agentPortKey, Integer.toString(agent.getSMTPPort()));
    	    
    	   
    	     prop.setProperty(indexAttachmentsKey, getYesNo(indexAttachments));
    	     prop.setProperty(indexMessageBodyKey, getYesNo(indexMessageBody));
    	     prop.setProperty(indexThreadsKey, Integer.toString(indexThreads));
    	     
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
    	  	
    	
    	  	setSmtpServerAddress(getString(prop.getProperty(smtpServerAddressKey),defaultSmtpServerAddress));
    	  	setSmtpPassword(getString(prop.getProperty(smtpPasswordKey),defaultSmtpPassword));
    	  	setSmtpUsername(getString(prop.getProperty(smtpUsernameKey),defaultSmtpUsername));
    	 
    	    int diskSpaceCheckWait 	= getInteger(prop.getProperty(diskSpaceCheckWaitKey),defaultDiskSpaceCheckWait);
    	    int diskSpaceWarn 		= getInteger(prop.getProperty(diskSpaceWarnKey),defaultDiskSpaceWarn);
    	    int diskSpaceThreshold 	= getInteger(prop.getProperty(diskSpaceThresholdKey),defaultDiskSpaceThreshold);
    	    boolean diskSpaceCheck 	= getBoolean(prop.getProperty(diskSpaceCheckKey),defaultDiskSpaceCheck);
    	    
    	  	volumes = new Volumes(diskSpaceCheck, diskSpaceCheckWait, diskSpaceWarn, diskSpaceThreshold);
    	  
    	 
    	  	setSalt(prop.getProperty(saltKey));
    	  	setPassPhrase(getString(prop.getProperty(passPhraseKey),defaultPassPhrase));
            setMaxSearchResults(getInteger(prop.getProperty(maxSearchResultsKey),defaultMaxSearchResults));
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
    	  	    
    	  	    ara = getString(ara,defaultArchiveRuleAction).trim().toUpperCase(Locale.ENGLISH);
    	  	    arf = getString(arf,defaultArchiveRuleField).trim().toUpperCase(Locale.ENGLISH);
    	  	    arr = getString(arr,defaultArchiveRuleRegEx);    	  	 
    	  	    try {
    	  	    	archiveRules.addArchiveRule(ArchiveRules.Action.valueOf(ara),arf,getString(arr,defaultArchiveRuleRegEx));
    	  	    } catch (IllegalArgumentException iae) {
    	  	    	logger.error("failed to load archive rule in server.conf. action or field is set to an illegal value. {action='"+ara+"', field='"+arf+"', regex='"+arr+"'}");
    	  	    	break;
    	  	    }
    	  	  i++;
    	  	} while(true);
    
    	  	// active directory identity info
    	  	
    	 	adIdentity.setKDCAddress(getString(prop.getProperty(adKDCAddressKey),defaultADKdcAddress));
    	 	adIdentity.setLDAPAddress(getString(prop.getProperty(adLDAPAddressKey),defaultADLdapAddress));
    	  	
    	  	adIdentity.clearAllRoleMaps();
    	  	i = 1;
    	  	do {
    	  		String lr = prop.getProperty(adRoleMapKey + "." + Integer.toString(i));
    	  	    String la = prop.getProperty(adRoleAttributeKey + "." + Integer.toString(i));
    	  	    String lm = prop.getProperty(adRoleMatchKey + "." + Integer.toString(i));
    
    	  	    if (lr== null || la == null || lm == null)
    	  	    	break;
    
    	  	  adIdentity.addRoleMap(getString(lr,defaultADLoginRole),getString(la,defaultADLoginAttribute),getString(lm,defaultADLoginMatch));
    	  	  i++;
    	  	} while(true);
    	  	
    	  	// Active Directory email address mappings for user role functionality
    	
    	  	adIdentity.clearEmailMappings();
            i = 1;
            do {
                String attribute = prop.getProperty(adEmailMappingAttributeKey + "."+ Integer.toString(i));
                String pattern = prop.getProperty(adEmailMappingPatternKey + "."+ Integer.toString(i));
                if (attribute ==null || pattern ==null) break;
                Pattern pat;
                
                try {
                	pat = Pattern.compile(pattern);
                } catch (PatternSyntaxException pse) {
                	logger.error("failed to parse email mapping pattern (i.e. regular expression). skipping mapping. {attribute='"+attribute+"', pattern='"+pattern+"'}",pse);
                	break;
                }
                adIdentity.addEmailMapping(attribute, pattern);
                i++;
            } while (true);
            
            // if no email address mappings are found, use defaults
            if (adIdentity.getEmailMappings().size()==0) {
                logger.debug("no email address mappings found in server.conf. using defaults.");
                adIdentity.addEmailMapping("proxyAddresses","SMTP:(.*)@(.*)");
            }
            
            
    	  	// LDAP info
    	  	
    	 	ldapIdentity.setLDAPAddress(getString(prop.getProperty(ldapLDAPAddressKey),defaultLdapLdapAddress));
    	 	ldapIdentity.setBindDN(getString(prop.getProperty(ldapBindDNKey),defaultldapBindDN));
    	 	ldapIdentity.clearAllRoleMaps();
    	  	i = 1;
    	  	do {
    	  		String lr = prop.getProperty(ldapRoleMapKey + "." + Integer.toString(i));
    	  	    String la = prop.getProperty(ldapRoleAttributeKey + "." + Integer.toString(i));
    	  	    String lm = prop.getProperty(ldapRoleMatchKey + "." + Integer.toString(i));
    
    	  	    if (lr== null || la == null || lm == null)
    	  	    	break;
    
    	  	  ldapIdentity.addRoleMap(getString(lr,defaultLdapLoginRole),getString(la,defaultLdapLoginAttribute),getString(lm,defaultLdapLoginMatch));
    	  	  i++;
    	  	} while(true);
    	  	
    	  	// LDAP email address mappings for user role functionality
    	
    	  	ldapIdentity.clearEmailMappings();
            i = 1;
            do {
                String attribute = prop.getProperty(ldapEmailMappingAttributeKey + "."+ Integer.toString(i));
                String pattern = prop.getProperty(ldapEmailMappingPatternKey + "."+ Integer.toString(i));
                if (attribute ==null || pattern ==null) break;
                Pattern pat;
                
                try {
                	pat = Pattern.compile(pattern);
                } catch (PatternSyntaxException pse) {
                	logger.error("failed to parse email mapping pattern (i.e. regular expression). skipping mapping. {attribute='"+attribute+"', pattern='"+pattern+"'}",pse);
                	break;
                }
                ldapIdentity.addEmailMapping(attribute, pattern);
                i++;
            } while (true);
            
            // if no email address mappings are found, use defaults
            if (ldapIdentity.getEmailMappings().size()==0) {
                logger.debug("no LDAP email address mappings found in server.conf. using defaults.");
                ldapIdentity.addEmailMapping("mail","(.*)@(.*)");
            }
                       
    	  	volumes.clearAllVolumes();
    	  	i = 1;
    	  	do {
    	  	
    	  		String vp = prop.getProperty(volumePathKey + "." + Integer.toString(i));
    	  	    //String vs = prop.getProperty(volumeSize + "." + Integer.toString(i));
    	  	    String vip = prop.getProperty(volumeIndexPathKey + "." + Integer.toString(i));
    	  	    String vms = prop.getProperty(volumeMaxSizeKey + "." + Integer.toString(i));
    	  	    String vrs =  prop.getProperty(volumeRemoteSearchKey + "." + Integer.toString(i));
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
           
            searchAnalyzers = new LinkedHashMap<String,String>(); 
            i = 1;
            do {
                String className = prop.getProperty(searchAnalyzerClassKey + "."+ Integer.toString(i));
                String language = prop.getProperty(searchAnalyzerLanguageKey + "."+ Integer.toString(i));
                if (className ==null || language ==null) break;
                searchAnalyzers.put(language,className);
                i++;
            } while (true);
            
            // if no analyzers are found, load up the defaults
            if (searchAnalyzers.size()==0) {
                logger.debug("no search analyzers found in server.conf. using defaults.");
                searchAnalyzers = new LinkedHashMap<String,String>(defaultSearchAnalyzers);
            }
            String newAuthMethod = "basic";
        	try {
    	  		 newAuthMethod = prop.getProperty(authMethodKey);
    	  		 if (newAuthMethod==null) {
    	  			logger.info("config load: auth method was not found in server.conf. defaulting to basic.");
    	  			authMethod = AuthMethod.BASIC;
    	  		 } else
    	  			 authMethod = AuthMethod.valueOf(newAuthMethod.trim().toUpperCase(Locale.ENGLISH));
    	  	} catch (IllegalArgumentException iae) {
    	    		logger.error("failed to set auth method field. auth method is set to an illegal value {field='"+newAuthMethod+"'}");
    	    		logger.info("auth method field is set to 'to' by default (error recovery)");
    		}
        
		  	basicIdentity.load(); // load basic identity users
	        getVolumes().loadAllVolumeInfo(); // load all volumes infos
	       
			setIndexLanguageDetection(getBoolean(prop.getProperty(indexLanguageDetectionKey),defaultIndexLanguageDetection));
			setIndexLanguage(getString(prop.getProperty(indexLanguageKey),defaultIndexLanguage));
					
	        prop.setProperty("security.passhrase","<hidden>");

		  	i = 1;
		  	do {
		  		String pr = prop.getProperty(permissionRoleKey + "." + Integer.toString(i));
		  	    String pd = prop.getProperty(permissionAllowDeleteKey + "." + Integer.toString(i));
		  	    String pv = prop.getProperty(permissionAllowViewKey + "." + Integer.toString(i));
		  	    String pp = prop.getProperty(permissionAllowPrintKey + "." + Integer.toString(i));
		  	    String pe = prop.getProperty(permissionAllowExportKey + "." + Integer.toString(i));
		  	    String px = prop.getProperty(permissionAllowSaveKey + "." + Integer.toString(i));
		  	    String ps = prop.getProperty(permissionAllowSendKey + "." + Integer.toString(i));
		  	
		  	    if (pr== null && pd == null && pv == null && pp == null && pe == null && ps == null)
		  	    	break;
	
		  	  
		  	    	
		  	  	permission.setPermission(getString(pr,defaultPermissionRole),
		  			  				   getBoolean(pd,defaultPermissionAllow),
		  			  				   getBoolean(pv,defaultPermissionAllow),
		  			  				   getBoolean(pp,defaultPermissionAllow),
		  			  				   getBoolean(pe,defaultPermissionAllow),
		  			  				   getBoolean(px,defaultPermissionAllow),
		  			  				   getBoolean(ps,defaultPermissionAllow));
		  	   i++;
		  	} while(true);
		  	
		  
		  	agent = new Agent();
		  	i = 1;
	        do {
	        		String allowedIP = prop.getProperty(agentAllowedIPKey + "."+ Integer.toString(i));
	                if (allowedIP==null)
	                	break;
	                agent.addAllowedIPAddress(allowedIP);
	                i++;
	        } while (true);
	        
	        agent.setSMTPPort(getInteger(prop.getProperty(agentPortKey),defaultAgentPort));
	        agent.setMilterPort(getInteger(prop.getProperty(agentMilterPortKey),defaultAgentMilterPort));
	        agent.setAuth(getBoolean(prop.getProperty(agentAuthKey),defaultAgentAuth));
	        agent.setUsername(getString(prop.getProperty(agentUsernameKey),defaultAgentUsername));
	        agent.setPassword(getString(prop.getProperty(agentPasswordKey),defaultAgentPassword));
	        agent.setTLS(getBoolean(prop.getProperty(agentTLSKey),defaultAgentTLS));
	       
   	     
	        indexAttachments = getBoolean(prop.getProperty(indexAttachmentsKey),defaultIndexAttachments);
	        indexMessageBody = getBoolean(prop.getProperty(indexMessageBodyKey),defaultIndexMessageBody);
	      	indexThreads = getInteger(prop.getProperty(indexThreadsKey),defaultIndexThreads);
	        logger.debug(prop.toString());
      	
        }
      }
    
      protected static MailArchivaProperties loadProperties (final String name) throws ConfigurationException
      {
          MailArchivaProperties result = null;
          InputStream in = null;
    
          try {
    	      result = new MailArchivaProperties ();
    	      result.load(new FileInputStream(name));
    
          } catch (Exception e)
          {
          	  logger.info("configuration file not found, where location='WEB-INF/conf/server.conf'");
              result = new MailArchivaProperties ();
          }
          finally
          {
              if (in != null) try { in.close (); } catch (Throwable ignore) {}
          }
    
          return result;
      }
    
      protected static void saveProperties( final String name, MailArchivaProperties prop) throws ConfigurationException {
          try {
              File f = new File(name);
              prop.store(new FileOutputStream(f));
          } catch (Exception e) {
              throw new ConfigurationException("failed to save properties. cause:"+e.toString(),e, logger);
          }
      }
    
      public void setLoggingPath(String loggingPath) { this.loggingPath = loggingPath.toLowerCase(Locale.ENGLISH); }
    
      public String getLoggingPath() { return loggingPath; }
    
      public void setLoggingLevel(String loggingLevel) { this.loggingLevel = loggingLevel; }
    
      public String getLoggingLevel() { return loggingLevel; }
    
      public void setAuditPath(String auditPath) { this.auditPath = auditPath.toLowerCase(Locale.ENGLISH); }
    
      public String getAuditPath() { return auditPath; }
   
    
      public MailArchivaProperties getProperties() {
      	return prop;
      }
    
      public ArchiveRules getArchiveRules() { return archiveRules; }
    
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
      
      public int getMaxSearchResults() { 
          return this.maxSearchResults;
      }
      
      public void setMaxSearchResults(int maxSearchResults) {
          this.maxSearchResults = maxSearchResults;
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
    		if (Config.config==null) {
    			Config.config = new Config();
    		}
    		return Config.config;
      }
    
      public static void setConfig(Config newConfig) {
    	   if (Config.config!=null)
    		   config.getVolumes().stopDiskSpaceCheck();
    		Config.config = newConfig;
    		config.getVolumes().startDiskSpaceCheck();
      }
      
      public Map<String,String> getSearchAnalyzers() {
          return searchAnalyzers;
      }
      
      public void setAuthMethod(AuthMethod authMethod) {
    	  this.authMethod = authMethod;
      }
      
      public void setAuthMethod(String loginMethod) {
    		  	AuthMethod newAuthMethod = AuthMethod.BASIC;	
    		  	try {
    		  		newAuthMethod = AuthMethod.valueOf(loginMethod.trim().toUpperCase(Locale.ENGLISH));
    		  	} catch (IllegalArgumentException iae) {
    		    		logger.error("failed to set login method. login method is set to an illegal value {loginMethod='"+loginMethod+"'}");
    		    		logger.info("defaulting to basic login method");
    			}
    		  	setAuthMethod(newAuthMethod);
      }
      
      public AuthMethod getAuthMethod() {
      	  return authMethod;
      }
      
      public ADIdentity getADIdentity() {
    	  return adIdentity;
      }
      
      public LDAPIdentity getLDAPIdentity() {
    	  return ldapIdentity;
      }
      
      public BasicIdentity getBasicIdentity() {
    	  return basicIdentity;
      }
      
      public void setIndexLanguage(String lang) {
    	  this.indexLanguage = lang;
      }
      
      public String getIndexLanguage() {
    	  return indexLanguage;
      }
      
      public boolean getIndexLanguageDetection() {
    	  return indexLanguageDetection;
      }
      
      public void setIndexLanguageDetection(boolean detectLanguage) {
    	  this.indexLanguageDetection = detectLanguage;
      }
      
      public void setSmtpUsername(String smtpUsername) {
	    	this.smtpUsername = smtpUsername;
      }
	    
		public void setSmtpPassword(String smtpPassword) {
			this.smtpPassword = smtpPassword;
		}
		
		public void setSmtpServerAddress(String smtpServerAddress) {
			this.smtpServerAddress = smtpServerAddress;
		}
		
		public String getSmtpUsername() { return smtpUsername; }
		
		public String getSmtpPassword() { return smtpPassword; }
		
		public String getSmtpServerAddress() { return smtpServerAddress; }
	    
	    
  
      
 
       public static class MailArchivaProperties extends java.util.LinkedHashMap<String,String>{
  	    /**
		 * 
		 */
		private static final long serialVersionUID = 1598672170612192642L;
		private static final char keyValueSeparator='=';
  	    private static final String intro = "# MailArchiva Properties File\n" +
  	    									"# Copyright Jamie Band 2007\n" +
  	    									"version = 1.3\n";
  	    
  	    public void load(FileInputStream in) throws IOException {
  	       BufferedReader input= new BufferedReader(new InputStreamReader(in,"ISO-8859-1"));
  	       String line;
  	       boolean oldVersion = true; // deal with legacy properties file
  	       while((line=input.readLine())!=null) {
  	           int pos=line.indexOf(keyValueSeparator);
  	           if(!line.startsWith("#") && pos>=0) {
  	        	   String key = line.substring(0,pos).trim();
  	        	   String value = line.substring(pos+1).trim();
  	        	   if (Compare.equalsIgnoreCase(key, "version"))
  	        		   oldVersion = false;
  	        	   if (oldVersion)
  	        		   value = value.replace("\\:",":").replace("\\\\","\\").replace("\\=","="); // legacy
  	             put(key,value);
  	          }
  	       }
  	       input.close();    
  	    }
  	   
  	    public void store(OutputStream out) throws IOException {
  	       BufferedWriter output= new BufferedWriter(new OutputStreamWriter(out,"ISO-8859-1"));
  	       output.append(intro);
  	       
  	       for(Map.Entry<String,String> property:entrySet()){
  	          output.append(property.getKey());
  	          output.append(keyValueSeparator);
  	          output.append(property.getValue());
  	          output.newLine();
  	       }
  	       output.close();
  	    }
  	    
  	    public String getProperty(String key) { return get(key); }
  	    
  	    public void setProperty(String key, String value) { put(key, value); } 
  	    
  	  
  	}
    
 
       public static TempFiles getTempFiles() { return tempFiles; } 
       public Agent getAgent() { return agent; }
       
       public boolean getIndexAttachments() { return indexAttachments; }
       public boolean getIndexMessageBody() { return indexMessageBody; }
       public int getIndexThreadsKey() { return indexThreads; }
       
       public static String getDebugLogPath() {
    	   return System.getProperty("catalina.home")+File.separator+"logs"+File.separator+"debug.log";
       }
       
       public static String getAuditLogPath() {
    	   return System.getProperty("catalina.home")+File.separator+"logs"+File.separator+"audit.log";
       }
       
       public int getMaxMessageSize() {
    	   return maxMessageSize;
       }
       
       
}


  	
  	
  	