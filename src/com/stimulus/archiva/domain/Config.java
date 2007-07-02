
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
import java.io.*;
import org.apache.log4j.Logger;
import java.util.*;
import java.util.regex.*;
import java.text.SimpleDateFormat;
import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import com.stimulus.archiva.exception.ConfigurationException;
import com.stimulus.archiva.authentication.ADIdentity;
import com.stimulus.archiva.authentication.BasicIdentity;
import com.stimulus.archiva.authentication.BasicIdentity.BasicRoleMap;

public class Config {

	static final long serialVersionUID = 4243937061206625954L;

  /* protected Fields */
   

    
     public static final List<String> VOLUME_STATUS;
     protected static final boolean THROW_ON_LOAD_FAILURE = true;
     protected static final boolean LOAD_AS_RESOURCE_BUNDLE = false;
     protected static final int SAVE_WAIT = 5000;
     protected static final Logger logger = Logger.getLogger(Config.class);
     
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
     protected static final String volumeIndexPathKey 		= "volume.index.path";
     protected static final String volumeMaxSizeKey 		= "volume.max.size";
     protected static final String kdcAddressKey  			= "authentication.kdc.address";
     protected static final String ldapAddressKey 			= "authentication.ldap.address";
     protected static final String adRoleMapKey  			= "role.assignment.role";
     protected static final String adRoleDomainKey 			= "role.assignment.domain";
     protected static final String adRoleAttributeKey 		= "role.assignment.attribute";
     protected static final String adRoleMatchKey 			= "role.assignment.criterion";
     protected static final String domainKey 				= "domain";
     protected static final String saltKey 					= "security.salt";
     protected static final String passPhraseKey 			= "security.passhrase";
     protected static final String authMethodKey 			= "security.loginmethod";
     protected static final String pbeAlgorithmKey 			= "security.pbealgorithm";
     protected static final String maxSearchResultsKey 		= "search.maxresults";
     protected static final String searchAnalyzerLanguageKey	= "search.analyzer.language";
     protected static final String adEmailMappingAttributeKey 	= "emailaddress.map.attribute";
     protected static final String adEmailMappingPatternKey 	= "emailaddress.map.pattern";
     protected static final String searchAnalyzerClassKey 		= "search.analyzer.class";
     protected static final String indexLanguageKey 			= "index.language";
     protected static final String indexLanguageDetectionKey 	= "index.language.detect";
     
     protected static Map<String,String> defaultSearchAnalyzers;
   
 
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
         defaultSearchAnalyzers = Collections.unmodifiableMap(analyzerMap);
         
         ArrayList<String> status = new ArrayList<String>();
         status.add("CLOSED");
         status.add("ACTIVE");
         status.add("UNUSED");
         status.add("NEW");
         VOLUME_STATUS = Collections.unmodifiableList(status);
        
     }
     
     protected static final String defaultDiskSpaceCheckWait	= "600"; // seconds
     protected static final String defaultDiskSpaceWarn  	  	= "150"; // megabytes
     protected static final String defaultDiskSpaceThreshold 	= "10"; // megabytes
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
     protected static final String defaultKdcAddress = "activedirectory.company.com:88";
     protected static final String defaultLdapAddress = "activedirectory.company.com:389";
     protected static final String defaultConsoleAuthenticate = "no";
     protected static final String defaultADLoginRole = (String)Identity.ROLES.get(0);
     protected static final String defaultADLoginAttribute = (String)ADIdentity.ATTRIBUTES.get(0);
     protected static final String defaultADLoginMatch = "";
     protected static final String defaultAuthMethod = "basic";
     protected static final String defaultUserConsoleAccess = "no";
     protected static final String defaultAuditorConsoleAccess = "yes";
     protected static final String defaultDomain = "company.com";
     protected static final String defaultPBEAlgorithm="PBEWithMD5AndTripleDES";
     protected static final String defaultPassPhrase="changeme";
     protected static final String defaultMaxSearchResults="10000";
     protected static final String defaultIndexLanguage = "en";
     protected static final String defaultIndexLanguageDetection = "yes";
     
     private static String hexits = "0123456789abcdef";
     protected static final String version="1.3";
     protected static final Object readWriteLock = new Object();
     protected MailArchivaProperties prop;
     protected String 	messageStorePath;
     protected String 	indexPath;
     protected String 	auditPath;
     protected String 	loggingPath;
     protected String 	loggingLevel;
     protected String   installDirectory;
     protected String 	kdcAddress;
     protected String   ldapAddress;
     protected byte[]   salt;
     protected String   passPhrase;
     protected String   pbeAlgorithm;
     protected int      maxSearchResults;
     protected String   loginPassword;
     protected AuthMethod	authMethod = AuthMethod.BASIC;
	 public enum AuthMethod { BASIC, ACTIVEDIRECTORY };
     protected ArchiveRules archiveRules = new ArchiveRules();
     protected Volumes volumes = null;
     protected BasicIdentity basicIdentity = new BasicIdentity();
     protected ADIdentity adIdentity = new ADIdentity();
     protected Domains domains = new Domains();
     protected Volume volume = null;
     protected Map<String,String> searchAnalyzers;
     protected String indexLanguage = defaultIndexLanguage;
     protected boolean indexLanguageDetection = true;
     protected static Config config = null;
     protected static String applicationPath = null;

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
    	    
    	  	prop = new MailArchivaProperties();
    
    	  	prop.setProperty(ldapAddressKey, getLDAPAddress());
    	  	prop.setProperty(kdcAddressKey, getKDCAddress());
    	  	prop.setProperty(archiveInboundKey,getYesNo(archiveRules.getArchiveInbound()));
    	  	prop.setProperty(archiveOutboundKey,getYesNo(archiveRules.getArchiveOutbound()));
    	  	prop.setProperty(archiveInternalKey,getYesNo(archiveRules.getArchiveInternal()));
    	  	prop.setProperty(passPhraseKey,getPassPhrase());
            prop.setProperty(maxSearchResultsKey, Integer.toString(getMaxSearchResults()));
    	  	prop.setProperty(pbeAlgorithmKey,getPBEAlgorithm());
    		prop.setProperty(authMethodKey,authMethod.toString().toLowerCase());
			
			
    	  	if (salt!=null)
    	  	    prop.setProperty(saltKey,toHex(getSalt()));
    
    		Iterator i = archiveRules.getArchiveRules().iterator();
    	  	int c = 1;
    
    		while (i.hasNext()) {
    		    ArchiveRules.Rule af = (ArchiveRules.Rule)i.next();
    			prop.setProperty(archiveRuleActionKey + "."+c,af.getAction().toString().toLowerCase());
    			prop.setProperty(archiveRuleFieldKey + "."+c,af.getField().toString().toLowerCase());
    			prop.setProperty(archiveRuleRegExKey + "."+c,af.getRegEx());
    			c++;
    		}
    		c = 1;
    
    	
			// Active Directory identity info
    		
    		i = adIdentity.getRoleMaps().iterator();
    
    		while (i.hasNext()) {
    			ADIdentity.ADRoleMap lr = (ADIdentity.ADRoleMap)i.next();
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
            
    
    		String fileName = applicationPath + File.separatorChar + "WEB-INF"+File.separatorChar + "conf"+ File.separatorChar + "server.conf";
    	
    		prop.setProperty(indexLanguageDetectionKey,getYesNo(indexLanguageDetection));
    		prop.setProperty(indexLanguageKey,indexLanguage);
    	      
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
    	  	
    	    int diskSpaceCheckWait 	= getInteger(prop.getProperty(diskSpaceCheckWaitKey),defaultDiskSpaceCheckWait);
    	    int diskSpaceWarn 		= getInteger(prop.getProperty(diskSpaceWarnKey),defaultDiskSpaceWarn);
    	    int diskSpaceThreshold 	= getInteger(prop.getProperty(diskSpaceThresholdKey),defaultDiskSpaceThreshold);
    	    boolean diskSpaceCheck 	= getBoolean(prop.getProperty(diskSpaceCheckKey),defaultDiskSpaceCheck);
    	    
    	  	volumes = new Volumes(diskSpaceCheck, diskSpaceCheckWait, diskSpaceWarn, diskSpaceThreshold);
    	  
    	  	setKDCAddress(getString(prop.getProperty(kdcAddressKey),defaultKdcAddress));
    	  	setLDAPAddress(getString(prop.getProperty(ldapAddressKey),defaultLdapAddress));
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
    	  	    ara = getString(ara,defaultArchiveRuleAction).trim().toUpperCase();
    	  	    arf = getString(arf,defaultArchiveRuleField).trim().toUpperCase();
    	  	    arr = getString(arr,defaultArchiveRuleRegEx);    	  	 
    	  	    try {
    	  	    	archiveRules.addArchiveRule(ArchiveRules.Action.valueOf(ara),ArchiveRules.Field.valueOf(arf),getString(arr,defaultArchiveRuleRegEx));
    	  	    } catch (IllegalArgumentException iae) {
    	  	    	logger.error("failed to load archive rule in server.conf. action or field is set to an illegal value. {action='"+ara+"', field='"+arf+"', regex='"+arr+"'}");
    	  	    	break;
    	  	    }
    	  	  i++;
    	  	} while(true);
    
    	  	// active directory identity info
    	  	
    	  	adIdentity.clearAllRoleMaps();
    	  	i = 1;
    	  	do {
    	  		String lr = prop.getProperty(adRoleMapKey + "." + Integer.toString(i));
    	  	    String la = prop.getProperty(adRoleAttributeKey + "." + Integer.toString(i));
    	  	    String lm = prop.getProperty(adRoleMatchKey + "." + Integer.toString(i));
    
    	  	    if (lr== null && la == null & lm == null)
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
    	  			 authMethod = AuthMethod.valueOf(newAuthMethod.trim().toUpperCase());
    	  	} catch (IllegalArgumentException iae) {
    	    		logger.error("failed to set auth method field. auth method is set to an illegal value {field='"+newAuthMethod+"'}");
    	    		logger.info("auth method field is set to 'to' by default (error recovery)");
    		}
        }
	  	basicIdentity.load(); // load basic identity users
        getVolumes().loadAllVolumeInfo(); // load all volumes infos
       
		setKDCAddress(getString(prop.getProperty(kdcAddressKey),defaultKdcAddress));
		
		setIndexLanguageDetection(getBoolean(prop.getProperty(indexLanguageDetectionKey),defaultIndexLanguageDetection));
		setIndexLanguage(getString(prop.getProperty(indexLanguageKey),defaultIndexLanguage));
				
        prop.setProperty("security.passhrase","<hidden>");
      	logger.debug(prop.toString());
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
          	  logger.error("failed to load server configuration file, where location='WEB-INF/conf/server.conf'");
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
    		if (config==null) {
    			Config.config = new Config();
    		}
    		return config;
      }
    
      public static void setConfig(Config config) {
    		Config.config = config;
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
    		  		newAuthMethod = AuthMethod.valueOf(loginMethod.trim().toUpperCase());
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
      
     
      
 
       public static class MailArchivaProperties extends java.util.LinkedHashMap<String,String>{
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
  	        	   if (key.equalsIgnoreCase("version"))
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

    
    
}
