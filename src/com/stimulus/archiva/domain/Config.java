
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

import com.stimulus.archiva.exception.*;
import com.stimulus.archiva.authentication.*;
import com.stimulus.util.*;
import com.stimulus.archiva.store.*;
import com.stimulus.archiva.search.*;
import com.stimulus.archiva.index.*;
import com.stimulus.archiva.incoming.*;
import com.stimulus.archiva.monitor.*;
import com.stimulus.archiva.domain.fields.*;

public class Config implements Serializable {

	 static final long serialVersionUID = 4243937061206625954L;

     protected static final boolean THROW_ON_LOAD_FAILURE = true;
     protected static final boolean LOAD_AS_RESOURCE_BUNDLE = false;
     protected static final Logger logger = Logger.getLogger(Config.class);
     protected static final Logger audit = Logger.getLogger("com.stimulus.archiva.audit");
     protected static HashMap<UpdateObserver,UpdateObserver> updateObservers; 
     
     // misc configuration options
     protected static final String applicationVersionKey       	= "application.version";
     protected static final String exportMaxMessagesKey		  	= "export.max.messages";
     protected static final String viewMaxMessagesKey		  	= "view.max.messages";
     protected static final String deleteMaxMessagesKey		   	= "delete.max.messages";
     protected static final String sendMaxMessagesKey		   	= "send.max.messages";
	 protected static final String saltKey 						= "security.salt";
     protected static final String pbeAlgorithmKey 				= "security.pbealgorithm";
     protected static final String exportCharsetKey				= "export.charset";
     protected static final String defaultExportMaxMessages		= "100";
     protected static final String defaultViewMaxMessages		= "100";
     protected static final String defaultDeleteMaxMessages		= "100";
     protected static final String defaultSendMaxMessages		= "100";
     protected static final String defaultApplicationVersion	= "1.7.5e";

	 protected static final String defaultPBEAlgorithm="PBEWithMD5AndDES";
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
     protected ArrayList<Props>     props;
     protected MilterServerService 	milterService;
     protected SMTPServerService	smtpService;
     protected IAPService			iapService;
     protected Agent				agent;
     protected Roles				roles;
     
	 protected EmailFields			emailFields;
     protected byte[]    salt;
     protected String    pbeAlgorithm;
     protected int exportMaxMessages	= 100;
     protected int viewMaxMessages 		= 100;
     protected int deleteMaxMessages	= 100;	
     protected int sendMaxMessages 		= 100;
  
   
	static {
    	 startup();
    	 System.setProperty("mail.mime.base64.ignoreerrors", "true");
    	 System.setProperty("mail.mime.applefilenames", "true");
     }
     public Config() {
    
     }

     public void init(FetchMessageCallback callback) {
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
	   	  search = new StandardSearch(this);
	   	  services = new Services();
	   	  indexer = new MessageIndex();
	   	  props = new ArrayList<Props>();
	   	  milterService = new MilterServerService(callback);
	   	  smtpService = new SMTPServerService(callback);
	   	  iapService = new IAPService(callback);
		  emailFields = new EmailFields();
	   	  registerProps();
     }
	 public String getApplicationVersion() { return applicationVersion; }
	
	 
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
	 }
	 
	 public void registerServices() {
		 services.registerService(smtpService);
		 services.registerService(milterService);
		 services.registerService(iapService);
		 services.registerService(indexer);
		 services.registerService(new VolumeIRService());
		 services.registerService(new ConfigAutoLoadService());
		 services.registerService(new VolumeInfoService()); 
		 services.registerService(new ReArchiveService()); 

	 }
	
	 public Settings loadSettings() throws ConfigurationException {
			String fileName = filesystem.getConfigurationPath() + File.separatorChar + "server.conf";
		  	logger.debug("loading server settings {location='"+fileName+"'}");
		  	Settings prop = Settings.loadProperties(fileName);
		  	return prop;
	 }
	 
	 public Settings getSettings() { 
		 	Settings prop = new Settings();
		    	prop.setProperty(pbeAlgorithmKey,getPBEAlgorithm());
			if (salt!=null)
				prop.setProperty(saltKey,ConfigUtil.toHex(getSalt()));
			for (Props settings : props) {
				settings.saveSettings(null,prop,null);
			}
			return prop;
	 }
	 
	 public void save(String prefix, Settings prop, String suffix) {
		 
	 }
	
	 public void saveConfigurationFile(MailArchivaPrincipal principal, Settings settings) throws ConfigurationException {
		synchronized(readWriteLock) {
			String fileName = filesystem.getConfigurationPath() + File.separatorChar + "server.conf";
			logger.debug("saving Settings {location='"+fileName+"'}");
		
			logger.debug(settings.toString());
			audit.info("update config "+ settings.toString()+", "+principal+"}");
			
			String intro =  "# MailArchiva Settings File\n" +
							"# Copyright Jamie Band 2007\n" +
							"version = "+getApplicationVersion()+"\n";
		
			Settings.saveProperties(fileName, intro, settings);
		}
	
	}

 	 
	
    public void loadSettings(MailArchivaPrincipal principal, Settings settings) throws ConfigurationException {
			synchronized(readWriteLock) {
				setSalt(settings.getProperty(saltKey));
	         	setPBEAlgorithm(ConfigUtil.getString(settings.getProperty(pbeAlgorithmKey),defaultPBEAlgorithm));
				for (Props prop : props) {
						prop.loadSettings(null,settings,null);
				}
			    settings.setProperty("security.passhrase","<hidden>");
			    audit.info("load config "+ settings.toString()+", "+principal+"}");
			    logger.debug(settings.toString());
			    basicIdentity.loadXMLFile();
				emailFields.loadXMLFile();
		    	volumes.loadAllVolumeInfo();
			}
      }

      public void load(MailArchivaPrincipal principal) throws ConfigurationException {
    	  loadSettings(principal);
      }
   // legacy
      public void save(MailArchivaPrincipal principal) throws ConfigurationException {
    	  saveSettings(principal);
      }
      
      public void loadSettings(MailArchivaPrincipal principal) throws ConfigurationException {
    	    Settings settings = loadSettings();
    	    loadSettings(principal,settings);
    	    try {
    		  archiver.init();
    	    } catch (MessageStoreException mse) {
    		  throw new ConfigurationException("failed to initialize message store.",mse,logger);
    	    }
    	    notifyUpdateObservers(); 
      }
      
      public void saveSettings(MailArchivaPrincipal principal) throws ConfigurationException {
    		Settings settings = new Settings();
		
			settings.setProperty(pbeAlgorithmKey,getPBEAlgorithm());
			if (salt!=null)
				settings.setProperty(saltKey,ConfigUtil.toHex(getSalt()));
		        
			for (Props prop : props) {
				prop.saveSettings(null,settings,null);
			}	
			
			basicIdentity.saveXMLFile();
			volumes.saveAllVolumeInfo(true);
			saveConfigurationFile(principal,settings);
      }
      
      public void modify(MailArchivaPrincipal principal, Config config) throws ConfigurationException {
    	 config.getVolumes().loadAllVolumeInfo();
    	 config.saveSettings(principal);
    	 Collections.sort(config.getVolumes().getVolumes()); 
    	 
    	 //loadSettings();
    	  try {
    		  archiver.init();
    	  } catch (MessageStoreException mse) {
    		  throw new ConfigurationException("failed to initialize message store.",mse,logger);
    	  }
      }
 
      public ArchiveFilter getArchiveFilter() { return archiveFilter; }
    
      public Volumes getVolumes() { return volumes; }
    
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
      
        
      public EmailFields getEmailFields() { return emailFields; }
      

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
		   updateObservers = new HashMap<UpdateObserver,UpdateObserver>();
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
		 
	
		 
		 public void cloneTo(MailArchivaPrincipal principal, Config newConfig) throws ConfigurationException {
			 newConfig.loadSettings(principal,getSettings());
			 for (Volume sourcevolume : getVolumes().getVolumes()) {
				 for (Volume destvolume : newConfig.getVolumes().getVolumes()) {
					 if (sourcevolume.equals(destvolume)) {
						 destvolume.setFreeArchiveSpace(sourcevolume.getFreeArchiveSpace());
						 destvolume.setFreeIndexSpace(sourcevolume.getFreeIndexSpace());
						 destvolume.setUsedArchiveSpace(sourcevolume.getUsedArchiveSpace());
						 destvolume.setUsedIndexSpace(sourcevolume.getUsedIndexSpace());
						 destvolume.setDiskSpaceChecked(sourcevolume.isDiskSpaceChecked());
					 }
				 }
			 }
		 }
}


  	
  	
  	