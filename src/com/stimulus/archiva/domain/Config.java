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
package com.stimulus.archiva.domain;

import java.io.*;
import org.apache.commons.logging.*;
import java.util.*;
import com.stimulus.archiva.log.*;
import com.stimulus.archiva.exception.*;
import com.stimulus.archiva.authentication.*;
import com.stimulus.util.*;
import com.stimulus.archiva.store.*;
import com.stimulus.archiva.search.*;
import com.stimulus.archiva.index.*;
import com.stimulus.archiva.incoming.*;
import com.stimulus.archiva.monitor.*;
import com.stimulus.archiva.domain.fields.*;
import com.stimulus.archiva.service.*;

public class Config implements Serializable, Cloneable {

	 static final long serialVersionUID = 4243937061206625954L;

     protected static final boolean THROW_ON_LOAD_FAILURE = true;
     protected static final boolean LOAD_AS_RESOURCE_BUNDLE = false;
     protected static final Log logger = LogFactory.getLog(Config.class);
     protected static final Log audit = LogFactory.getLog("com.stimulus.archiva.audit");
     protected static WeakHashMap<UpdateObserver,UpdateObserver> updateObservers;

     // misc configuration options
     protected static final String applicationVersionKey       	= "application.version";
     protected static final String exportMaxMessagesKey		  	= "export.max.messages";
     protected static final String viewMaxMessagesKey		  	= "view.max.messages";
     protected static final String deleteMaxMessagesKey		   	= "delete.max.messages";
     protected static final String sendMaxMessagesKey		   	= "send.max.messages";
	 protected static final String saltKey 						= "security.salt";
	 protected static final String tempDirKey				   = "temp.dir";
     protected static final String pbeAlgorithmKey 				= "security.pbealgorithm";
     protected static final String exportCharsetKey				= "export.charset";
     protected static final String defaultExportMaxMessages		= "1000";
     protected static final String defaultViewMaxMessages		= "1000";
     protected static final String defaultDeleteMaxMessages		= "1000";
     protected static final String defaultSendMaxMessages		= "1000";
     protected static final String defaultApplicationVersion	= "1.9.13";

	 protected static final String defaultPBEAlgorithm="PBEWithMD5AndTripleDES";
     protected static Config config = null;
     protected static boolean shutdown = false;

     protected static final Object readWriteLock = new Object();
     protected static FileSystem filesystem;
     protected static StopBlockFactory stopBlockFactory;

     protected ArchiveFilter 		archiveFilter;
     protected Authentication 		authentication;
     protected Domains 				domains;
     protected String				applicationVersion = defaultApplicationVersion;
     protected ADIdentity 			adIdentity;
     protected BasicIdentity 		basicIdentity;
     protected MailboxConnections 	mailboxConnections;

     protected Archiver				archiver;
     protected Indexer				indexer;
     protected Volumes 				volumes;
     protected Search				search;
     protected Services				services;
     protected ArrayList<Props>     props = new ArrayList<Props>();
     protected MilterServerService 	milterService;
     protected SMTPServerService	smtpService;
     protected IAPService			iapService;
     protected VolumeInfoService	volumeInfoService;
     protected Agent				agent;
     protected Roles				roles;
     protected EmailFields			emailFields;
     protected LogFiles				logFiles;
	 protected VolumeIRService 		volumeIRService;
	 protected CPUThrottleService	throttleService;


     protected byte[]    salt;
     protected String    pbeAlgorithm;
     protected static ConfigAutoLoadService configAutoLoad = new ConfigAutoLoadService();

     protected int exportMaxMessages	= 1000;
     protected int viewMaxMessages 		= 1000;
     protected int deleteMaxMessages	= 1000;
     protected int sendMaxMessages 		= 1000;
     protected String tempDir;

    protected FetchMessageCallback callback;
	static {

    	 startup();
    	 System.setProperty("mail.mime.base64.ignoreerrors", "true");
    	 System.setProperty("mail.mime.applefilenames", "true");
     }

     public Config() {

     }

     public void init(FetchMessageCallback callback) {

    	  if (callback==null) {
    		  logger.debug("call back is null.");
    		  callback = MessageService.getFetchMessageCallback();
    	  }
    	  this.callback = callback;
    	  domains = new Domains();
    	  roles = new Roles();
    	  agent = new Agent();
	   	  archiveFilter = new ArchiveFilter();
	   	  authentication = new Authentication();
	   	  adIdentity = new ADIdentity();
	   	  basicIdentity = new BasicIdentity();
	   	  mailboxConnections = new MailboxConnections();
	   	  archiver = new MessageStore();
	   	  volumes = new Volumes();
	   	  search = new StandardSearch();
	   	  services = new Services();
	   	  indexer = new MessageIndex();
	   	  props = new ArrayList<Props>();
	   	  milterService = new MilterServerService();
	   	  smtpService = new SMTPServerService();
	   	  iapService = new IAPService();
	   	  //autoUpdateService = new AutoUpdateService();
	   	  emailFields = new EmailFields();
	   	  volumeInfoService = new VolumeInfoService();
	   	  logFiles = new LogFiles();
	   	  volumeIRService = new VolumeIRService();
	   	  throttleService = new CPUThrottleService();
	   	  registerProps();
     }
	 public String getApplicationVersion() { return applicationVersion; }

	 public String getProductName() { return FileSystem.getProductName(); }

	 public FetchMessageCallback getFetchMessageCallback() {
		 if (callback==null) {
			 logger.debug("call back is null.");
   		  	callback = MessageService.getFetchMessageCallback();
   	  	 }
		 return callback;
	 }


	 protected void registerProps() {
		 props = new ArrayList<Props>();
		 props.add(agent);
		 props.add(milterService);
		 props.add(smtpService);
		 props.add(archiveFilter);
		 props.add(authentication);
		 props.add(domains);
		 props.add(adIdentity);
		 props.add(basicIdentity);
		 props.add(mailboxConnections);

		 props.add(archiver);
		 props.add(volumes);
		 props.add(search);
		 props.add(indexer);
		 props.add(roles);
		 props.add(throttleService);
	 }

	 public void registerServices() {
		 services.registerService(smtpService);
		 services.registerService(milterService);
		 services.registerService(iapService);
		 services.registerService(indexer);
		 services.registerService(volumeIRService);
		 services.registerService(configAutoLoad);
		 services.registerService(volumeInfoService);
		 services.registerService(new ReArchiveService());
		 //services.registerService(throttleService);
}
 	public void save(String prefix, Settings prop, String suffix) {

	 }

	 public String getServerConfFile() {
		 return filesystem.getConfigurationPath() + File.separatorChar + "server.conf";
	 }

	 public Settings loadConfigurationFile(MailArchivaPrincipal principal) throws ConfigurationException {
			String fileName = getServerConfFile();
		  	logger.debug("loading server settings {location='"+fileName+"'}");
		  	Settings prop = Settings.loadProperties(fileName,"UTF-8");

		  	// record this event
		  	Settings debugoutput = (Settings)prop.clone();
		  	debugoutput.setProperty("security.passhrase","<hidden>");
		    audit.info("load config "+ debugoutput.toString()+", "+principal+"}");
		    logger.debug(debugoutput.toString());
		  	return prop;
	 }

	 public void saveConfigurationFile(MailArchivaPrincipal principal, Settings settings) throws ConfigurationException {

		synchronized(readWriteLock) {
			String fileName = getServerConfFile();
			logger.debug("saving Settings {location='"+fileName+"'}");

			logger.debug(settings.toString());
			audit.info("update config "+ settings.toString()+", "+principal+"}");

			String intro =  "# "+Config.getConfig().getProductName().toUpperCase(Locale.ENGLISH)+" Settings File" + System.getProperty("line.separator")+
							"# Copyright Jamie Band 2008" + System.getProperty("line.separator")+
							"version = "+getApplicationVersion()+System.getProperty("line.separator");

			Settings.saveProperties(fileName, intro, settings,"UTF-8");
		}

	}

	private String newTempDir() {
		 String tmpDir = System.getProperty("java.io.tmpdir");
	  	  if (tmpDir.charAt(tmpDir.length()-1)==File.separatorChar)
	  		  tmpDir = tmpDir.substring(0,tmpDir.length()-1);
	  	  if (tmpDir==null || tmpDir.length()<2) {
	  		  tmpDir = File.separatorChar + "tmp" + File.separatorChar + FileSystem.getProductName().toLowerCase(Locale.ENGLISH);
	  	  } else {
	  		  if (!tmpDir.contains(FileSystem.getProductName().toLowerCase(Locale.ENGLISH))) {
	  			tmpDir = tmpDir + File.separatorChar + FileSystem.getProductName().toLowerCase(Locale.ENGLISH);
	  		  }
	  	  }
	  	  return tmpDir;
	}


    public synchronized Settings loadConfiguration(MailArchivaPrincipal principal) throws ConfigurationException {
    	Settings prop = loadConfigurationFile(principal);
    	setSalt(prop.getProperty(saltKey));
     	setPBEAlgorithm(ConfigUtil.getString(prop.getProperty(pbeAlgorithmKey),defaultPBEAlgorithm));
		//applicationVersion = ConfigUtil.getString(settings.getProperty(applicationVersionKey),defaultApplicationVersion);
		String tmpDir = prop.getProperty(tempDirKey);

	  	if (tmpDir==null) {
	  		tempDir = newTempDir();
	  	} else {
	  		tempDir = tmpDir;
	  	}
	  	File tempDirFile = new File(tempDir);

	  	if (!tempDirFile.exists())
	  		tempDirFile.mkdirs();

	  	System.setProperty("java.io.tmpdir",tempDir);

	  	logger.debug("initializing temp directory");

		for (Props setting : props) {
			setting.loadSettings(null,prop,null);
		}
    	return prop;
    }

    public synchronized Settings saveConfiguration(MailArchivaPrincipal principal) throws ConfigurationException {
	    Settings settings = getSettings();
		saveConfigurationFile(principal,settings);
		return settings;
    }

	 public Settings getSettings() {
		 	Settings currentConfiguration = new Settings();
		 	currentConfiguration.setProperty(pbeAlgorithmKey,getPBEAlgorithm());
			if (salt!=null)
				currentConfiguration.setProperty(saltKey,ConfigUtil.toHex(getSalt()));
			currentConfiguration.setProperty(tempDirKey, tempDir);
			for (Props settings : props) {
				settings.saveSettings(null,currentConfiguration,null);
			}

			return currentConfiguration;

	 }

    // legacy
      public void load(MailArchivaPrincipal principal) throws ConfigurationException {
    	  loadSettings(principal);
      }
   // legacy
      public void save(MailArchivaPrincipal principal) throws ConfigurationException {
    	  saveSettings(principal,true);
      }

      public synchronized void loadSettings(MailArchivaPrincipal principal) throws ConfigurationException {
    	  try {
    		configAutoLoad.block();
    		volumeIRService.block();
    	    loadConfiguration(principal);
    	    try {
    		  archiver.init();
    	    } catch (MessageStoreException mse) {
    		  throw new ConfigurationException("failed to initialize message store.",mse,logger);
    	    }
    	    adIdentity.loadHostsFileEntry();
    		basicIdentity.loadXMLFile();
    		emailFields.loadXMLFile();
    		try { volumes.loadAllVolumeInfo(); } catch (Exception e) {}
    	    notifyUpdateObservers();
    	  } finally {
    		  configAutoLoad.unblock();
    		  volumeIRService.unblock();
    	  }
      }

      public synchronized void saveSettings(MailArchivaPrincipal principal, boolean updateRemoteServers) throws ConfigurationException {
    	  	// we dont want changes to trigger configautoload service
    	  try {
			configAutoLoad.block();
			volumeIRService.block();
    	  	volumes.saveAllVolumeInfo(true);
    	    saveConfiguration(principal);
			basicIdentity.saveXMLFile();
			adIdentity.saveHostsFileEntry();
    	  } finally {
    		  configAutoLoad.unblock();
    		  volumeIRService.unblock();
    	  }
      }

      public ArchiveFilter getArchiveFilter() { return archiveFilter; }

      public Volumes getVolumes() { return volumes; }

      public VolumeIRService getVolumeIRService() { return volumeIRService; }

      public Domains getDomains() { return domains; }

      public Authentication getAuthentication() { return authentication; }

      public ADIdentity getADIdentity() {  return adIdentity; }


      public BasicIdentity getBasicIdentity() { return basicIdentity; }

      public static FileSystem getFileSystem() { return filesystem; }

      public Indexer getIndex() { return indexer; }

      public Archiver getArchiver() { return archiver; }

      public Search getSearch() { return search; }

      public Services getServices() { return services; }

      public SMTPServerService getSMTPServerService() { return smtpService; }

      public MilterServerService getMilterServerService() { return milterService; }

      public Agent getAgent() { return agent; }

      public Roles getRoles() { return roles; }


 	 public ConfigAutoLoadService getConfigAutoLoadService() { return configAutoLoad; }

      public EmailFields getEmailFields() { return emailFields; }

      public LogFiles getLogFiles() { return logFiles; }


      public String getTempDir() { return tempDir; }

      public void setTempDir(String tempDir) { this.tempDir = tempDir; }

	public int getExportMaxMessages() { return exportMaxMessages; }
	public int getViewMaxMessages() { return viewMaxMessages; }
	public int getSendMaxMessages() { return sendMaxMessages; }
	public int getDeleteMaxMessages() { return deleteMaxMessages; }



    public MailboxConnections getMailboxConnections() {
    	   return mailboxConnections;
    }


	   // static methods

	   public static synchronized Config getConfig() {
	 		if (Config.config==null) {
	 			Config.config = new Config();
	 		}
	 		return Config.config;
	   }


	   public void registerUpdateObserver(UpdateObserver observer) {
		   updateObservers.put(observer,observer);
	   }

	   public void unregisterUpdateObserver(UpdateObserver observer) {
		   updateObservers.remove(observer);
	   }

	   public void notifyUpdateObservers() {
		   for (UpdateObserver observer : updateObservers.values()) {
			   observer.updateConfig();
		   }
	   }

	   public interface UpdateObserver {

		   public void updateConfig();
	   }


	   public static void shutdown() {
		   if (Config.config!=null) {
			   Config.config.getServices().stopAll();
		   }
		   shutdown = true;
		   Config.config = null;
		   Config.filesystem.shutdown();
		   Config.filesystem = null;
		   Config.updateObservers = null;
		   Config.stopBlockFactory.shutdown();
		   Config.stopBlockFactory = null;
	   }

	   public static void startup() {
		   filesystem = new FileSystem();
		   stopBlockFactory = new StopBlockFactory();
		   updateObservers = new WeakHashMap<UpdateObserver,UpdateObserver>();
	   }

	   public static boolean getShutdown() {
		   return shutdown;
	   }

	   public static StopBlockFactory getStopBlockFactory() {
		   return stopBlockFactory;
	   }

	   public static String getEncKey() {
		   return "tQwe4rZdfjerosd23912As23z";
	   }

	   public void setPBEAlgorithm(String pbeAlgorithm) {
	        this.pbeAlgorithm = pbeAlgorithm;
	    }

	    public byte[] getSalt() {
	        return salt;
	    }

		public String getPBEAlgorithm() { return pbeAlgorithm; }

		 private void setSalt(String saltStr) {
		        if (saltStr==null) {
		      	  // we fake the salt for the moment, as there is too much of a risk that
		      	  // the admin will lose their entire store because they forgot to
		      	  // copy the server.conf file (which contains their salt value)
		  	  	  //salt = new byte[8];
		            //new Random().nextBytes(salt);
		      	  salt = ConfigUtil.fromHex("feadf944dd4d62a5");
		  	  	} else {
		  	  	  salt = ConfigUtil.fromHex(saltStr);
		  	  	}
		}

		 public VolumeInfoService getVolumeInfoService() {
			 return volumeInfoService;
		 }

		 public Config clone(MailArchivaPrincipal principal)  throws ConfigurationException {
			 Config newConfig = new Config();
			 // for most part we can use properties to clone objects
			 newConfig.init(callback);
			 newConfig.loadConfiguration(principal);
			 // there are a few exceptional cases, where an outside config file is maintained
			 newConfig.volumes = volumes.clone(); // volumeinfo file
			 newConfig.emailFields = emailFields.clone(); // email fields
			 newConfig.basicIdentity = basicIdentity.clone(); // basic identity;
			 newConfig.adIdentity.loadHostsFileEntry();
			 newConfig.registerProps(); // re-register properties as we cloned them since config was initialized
			 return newConfig;
		 }

}





