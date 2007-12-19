
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


package com.stimulus.archiva.presentation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.stimulus.archiva.authentication.ADIdentity;
import com.stimulus.archiva.authentication.LDAPIdentity;
import com.stimulus.archiva.domain.ArchiveRules;
import com.stimulus.archiva.domain.Config;
import com.stimulus.archiva.domain.Domains;
import com.stimulus.archiva.domain.Email;
import com.stimulus.archiva.domain.Identity;
import com.stimulus.archiva.domain.Volume;
import com.stimulus.archiva.domain.Volumes;
import com.stimulus.archiva.domain.fields.EmailField;
import com.stimulus.archiva.exception.ArchivaException;
import com.stimulus.archiva.exception.ChainedRuntimeException;
import com.stimulus.archiva.exception.ConfigurationException;
import com.stimulus.archiva.security.realm.ADRealm;
import com.stimulus.archiva.service.ConfigurationService;
import com.stimulus.archiva.service.MessageService;
import com.stimulus.struts.ActionContext;
import com.stimulus.struts.BaseBean;
import com.stimulus.util.Compare;
import com.stimulus.util.EnumUtil;
public class ConfigBean extends BaseBean implements Serializable {

  private static final long serialVersionUID = 2275642295115995805L;
  protected static Logger logger = Logger.getLogger(MessageBean.class.getName());
  protected static final Logger audit = Logger.getLogger("com.stimulus.archiva.audit");
  
  protected String lookupPassword = "";
  protected String lookupUsername = "";
  protected String ldapAttributes = "";
  protected String testAuthenticate = "";
  protected List<PermissionBean> permissions = null;
  protected IndexStatus status = new IndexStatus();
  protected String logLevel = "DEBUG";
  protected String recoveryOutput = "";
  protected boolean recoveryComplete = false;
  protected Config config = null;
  protected int recoveryFailed = 0;
  protected boolean portChange = false;
  
  public ConfigBean() {
  		config = new Config();
  		logLevel = ConfigurationService.getLoggingLevel();
  		configurationform();
  		
  }

  public Config getConfig() {
  	return config;
  }
  /* public List<EmailField> getFields() {
		 ArrayList<EmailField>  list = new ArrayList<EmailField>();
		 Iterator i = EmailField.getAvailableFields().values().iterator();
		 while (i.hasNext()) {
			 EmailField ef = (EmailField)i.next();
			 if (ef.isStored()) 
				 list.add(ef);			 
		 }
		 return list;
	 }
	 */
  public List getRuleFields() {
	  	ArrayList<String>  list = new ArrayList<String>();
	  
		 Iterator i = EmailField.getAvailableFields().values().iterator();
		 while (i.hasNext()) {
			 EmailField ef = (EmailField)i.next();
			 list.add(ef.getName());			 
			 
		 }
		 Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
		 return list;
  }

  public List getRuleFieldLabels() {
	  	 ArrayList<String>  list = new ArrayList<String>();
		 Iterator i = EmailField.getAvailableFields().values().iterator();
		
		 while (i.hasNext()) {
			 
			 EmailField ef = (EmailField)i.next();
			 list.add(ef.getResourceKey());			 
		 }
		 Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
		 return translateList(list);
  }

  public List getRuleActionFields() {
  	return ArchiveRuleBean.getActions();
  }
  
  public List getRuleActionLabels() {
  	return translateList(ArchiveRuleBean.getActionLabels());
  }

  public List getADRoleMapAttributes() {
	return ADIdentity.ATTRIBUTES;
  }

  public List getADRoleMapAttributeLabels() {
    return translateList(ADIdentity.ATTRIBUTE_LABELS);
  }
  
  public List getRoleMapRoles() {
   	return Identity.getRoles();
  }

  public List getRoleMapRoleLabels() {
    	return translateList(Identity.getRoleLabels());
  }

  public List getDebugLoggingLevelLabels() {
	  List<String> loggingLevels = new LinkedList<String>();
	  loggingLevels.add("config.log_level_all");
	  loggingLevels.add("config.log_level_debug");
	  loggingLevels.add("config.log_level_warn");
	  loggingLevels.add("config.log_level_error");
	  loggingLevels.add("config.log_level_fatal");
	  loggingLevels.add("config.log_level_info");
	  loggingLevels.add("config.log_level_off");
	  return translateList(Collections.unmodifiableList(loggingLevels));
  }

  public List getDebugLoggingLevels() {

	  List<String> loggingLevels = new LinkedList<String>();
	  loggingLevels.add("ALL");
	  loggingLevels.add("DEBUG");
	  loggingLevels.add("WARN");
	  loggingLevels.add("ERROR");
	  loggingLevels.add("FATAL");
	  loggingLevels.add("INFO");
	  loggingLevels.add("OFF");
	  return Collections.unmodifiableList(loggingLevels);
  }
  
  public void setDebugLoggingLevel(String level) {
	  logLevel = level;
  }
  
  public String getDebugLoggingLevel() {
	  return logLevel; 
  }
  
  public String getSmtpServerAddress() { return config.getSmtpServerAddress(); }
  
  public String getSmtpPassword() { return config.getSmtpPassword(); }
  
  public String getSmtpUsername() { return config.getSmtpUsername(); }
  
  public void setSmtpServerAddress(String smtpServerAddress) { config.setSmtpServerAddress(smtpServerAddress); }
  
  public void setSmtpUsername(String smtpUsername) { config.setSmtpUsername(smtpUsername); }
  
  public void setSmtpPassword(String smtpPassword) { config.setSmtpPassword(smtpPassword); }
  
  public void setArchiveInbound(boolean archiveInbound) { config.getArchiveRules().setArchiveInbound(archiveInbound); };

  public void setArchiveOutbound(boolean archiveOutbound) {  config.getArchiveRules().setArchiveOutbound(archiveOutbound);  };

  public void setArchiveInternal(boolean archiveInternal) { config.getArchiveRules().setArchiveInternal(archiveInternal); };

  public boolean getArchiveInbound() {  return config.getArchiveRules().getArchiveInbound(); }

  public boolean getArchiveOutbound() {return config.getArchiveRules().getArchiveOutbound(); }

  public boolean getArchiveInternal() { return config.getArchiveRules().getArchiveInternal(); }


  public void setAuthMethod(String authMethod) { config.setAuthMethod(authMethod); }
  
  public String getAuthMethod() { return config.getAuthMethod().toString().toLowerCase(Locale.ENGLISH); }

  public List getAuthMethods() {
  	return EnumUtil.enumToList(Config.AuthMethod.values());
  }
  
  public List getAuthMethodLabels() {
  	return translateList(EnumUtil.enumToList(Config.AuthMethod.values(),"config.sec_auth_method_"));
  }
 
  public void reset() {
  	
  }

  public String reload() {
      ldapAttributes = "";
      testAuthenticate = "";
      recoveryOutput = "";
      recoveryComplete = false;
      recoveryFailed = 0;
      portChange = false;
      return "reload";
  }
  
  /* agent ip addresses */
  
  public List<String> getAgentIPAddresses() {
	  return config.getAgent().getIPAddresses();
  }
  
  public String getAgentIPAddress(int index) {
	  return config.getAgent().getIPAddress(index);
  }
  
  public void setAgentIPAddress(int index, String value) {
	  config.getAgent().setIPAddress(index, value);
  }
  public String newAgentIPAddress() {
	  config.getAgent().addAllowedIPAddress("127.0.0.1");
	  return "reload";
  }
  
  public String deleteAgentIPAddress(int id) {
	  config.getAgent().removeIPAddress(id);
	  return "reload";
  }
  
  public String getAgentSMTPPort() {
	  return Integer.toString(config.getAgent().getSMTPPort());
  }
  
  public void setAgentSMTPPort(String port) {
	  int portVal = Integer.valueOf(port);
	  if (portVal!=config.getAgent().getSMTPPort()) {
		  portChange = true;
		  config.getAgent().setSMTPPort(portVal);
	  }
  }
  
  public String getAgentMilterPort() {
	  return Integer.toString(config.getAgent().getMilterPort());
  }
  
  public void setAgentMilterAgentPort(String port) {
	  int portVal = Integer.valueOf(port);
	  if (portVal!=config.getAgent().getMilterPort()) {
		  portChange = true;
		  config.getAgent().setMilterPort(portVal);
	  }
  }

  /* volumes */
  
  public List getVolumes() {
  	return VolumeBean.getVolumeBeans(config.getVolumes());
  }
  
  public Volume getMessageStoreVolume(int index) {
	  	return config.getVolumes().getVolume(index);
  }
	  
  public String indexAllVolumes() throws ArchivaException {
      logger.debug("indexAllVolumes()");
      MessageService.indexAllVolumes(getMailArchivaPrincipal(),status);
      return "reload";
  }

  public String indexVolume(int volumeIndex) throws ArchivaException {
      logger.debug("indexVolume() {volumeIndex='"+volumeIndex+"'}");
      MessageService.indexVolume(getMailArchivaPrincipal(),volumeIndex,status);
      return "reload";
  }

  public String closeVolume(int volumeIndex) throws ConfigurationException {
      logger.debug("closeVolume()");
      config.getVolumes().closeVolume(volumeIndex);
      return "reload";
  }
  
  public String unmountVolume(int volumeIndex) throws ConfigurationException {
      logger.debug("unmountVolume()");
      config.getVolumes().unmountVolume(volumeIndex);
      return "reload";
  }

  public String deleteVolume(int id) throws ConfigurationException {
    logger.debug("deleteVolume() {volumeIndex='"+id+"'}");
  	config.getVolumes().removeVolume(id);
  	return "reload";
  }
  public String newVolume() throws ConfigurationException {
  	logger.debug("newVolume()");
  	config.getVolumes().addVolume("","",config.getDefaultVolumeMaxSize());
  	
  	return "reload";
  }

  public String prioritizeVolume(int id) throws ConfigurationException {
  	config.getVolumes().setVolumePriority(id, Volumes.Priority.PRIORITY_LOWER);
  	return "reload";
  }
  public String dePrioritizeVolume(int id) throws ConfigurationException {
  	config.getVolumes().setVolumePriority(id, Volumes.Priority.PRIORITY_LOWER);
  	return "reload";
  }

  /* domains */

  public String newDomain() throws ConfigurationException {
    	config.getDomains().addDomain();
    	return "reload";
  }

  public String deleteDomain(int id) throws ConfigurationException {
    	config.getDomains().deleteDomain(id);
    	return "reload";
  }
  
  public List getDomains() {
  	return DomainBean.getDomainBeans(config.getDomains().getDomains());
  }

  public List getDomainLabels() {
	  List<String> domainList = new LinkedList<String>();
      List<Domains.Domain> domains = config.getDomains().getDomains();
      for (Domains.Domain domain : domains) {
    	  domainList.add(domain.getName());
      }
      return domainList;
  }

  /* ad role maps */
  
  public String deleteAdRoleMap(int id) {
      config.getADIdentity().deleteRoleMap(id);
      return "reload";
  }

  public String newAdRoleMap() throws ConfigurationException {
	  	config.getADIdentity().newRoleMap();
    	return "reload";
  }
  
  public Identity.RoleMap getAdRoleMap(int index) {
    	return config.getADIdentity().getRoleMap(index);
  }

  public List getADRoleMaps() {
      return LDAPRoleMapBean.getLDAPRoleMapBeans(config.getADIdentity().getRoleMaps());
  }
  
  public String deleteLDAPRoleMap(int id) {
      config.getLDAPIdentity().deleteRoleMap(id);
      return "reload";
  }

  public String newLDAPRoleMap() throws ConfigurationException {
	  	config.getLDAPIdentity().newRoleMap();
    	return "reload";
  }
  
  public List getLDAPRoleMaps() {
      return LDAPRoleMapBean.getLDAPRoleMapBeans(config.getLDAPIdentity().getRoleMaps());
  }
  
  public static List<String> getAdRoles() {
  	return Identity.getRoles();
  }
  
  public static List<String> getAdRoleLabels() {
	return Identity.getRoleLabels();
  }

  public ADIdentity getADIdentity() {
	  return config.getADIdentity();
  }
  
  public LDAPIdentity getLDAPIdentity() {
	  return config.getLDAPIdentity();
  }
  /* basic role map */
  
/* ad role maps */
  
  public String deleteBasicRoleMap(int id) {
      config.getBasicIdentity().deleteRoleMap(id);
      return "reload";
  }

  public String newBasicRoleMap() throws ConfigurationException {
	  	config.getBasicIdentity().newRoleMap();
    	return "reload";
  }
  
  public Identity.RoleMap getBasicRoleMap(int index) {
    	return config.getBasicIdentity().getRoleMap(index);
  }

  public List getBasicRoleMaps() {
      return BasicRoleMapBean.getBasicRoleMapBeans(config.getBasicIdentity().getRoleMaps());
  }
  
  public static List<String> getBasicRoles() {
  	return Identity.getRoles();
  }
  
  public static List<String> getBasicRoleLabels() {
	return Identity.getRoleLabels();
  }
  /* archive rules */

  public String deleteArchiveRule(int id) throws ConfigurationException {
  	config.getArchiveRules().deleteArchiveRule(id);
  	return "reload";
  }
  
  public String newArchiveRule() throws ConfigurationException {
  	config.getArchiveRules().addArchiveRule();
  	return "reload";
  }

  public String prioritizeArchiveRule(int id) throws ConfigurationException {
  	config.getArchiveRules().setArchiveRulePriority(id, ArchiveRules.Priority.HIGHER);
  	return "reload";
  }
  public String dePrioritizeArchiveRule(int id) throws ConfigurationException {
  	config.getArchiveRules().setArchiveRulePriority(id, ArchiveRules.Priority.LOWER);
  	return "reload";
  }

  public List getArchiveRules() {
  	return ArchiveRuleBean.getArchiveRuleBeans(config.getArchiveRules());
  }

  public ArchiveRuleBean getArchiveRule(int index) {
  	return new ArchiveRuleBean(config.getArchiveRules().getArchiveRule(index));
  }

  public void setPassPhrase(String passPhrase) {
      //logger.debug("setPassPhrase {passPhrase='"+passPhrase+"'}");
      if (passPhrase.trim().length()>0)
          config.setPassPhrase(passPhrase);
  }
  
  public boolean getDefaultPassPhraseModified() {
      return config.isDefaultPassPhraseModified();
  }
  
  public String getLookupPassword() {
      return lookupPassword;
  }
  
  public String getLookupUsername() {
      return lookupUsername;
  }
  public void setLookupPassword(String password) {
      this.lookupPassword = password;
  }
  
  public void setLookupUsername(String username) {
      this.lookupUsername = username;
  }
  
  public String getLdapAttributes() {
      return ldapAttributes;
  }

  public void setLdapAttributes(String ldapAttributes) {
      this.ldapAttributes = ldapAttributes;
  }
  
  public void setTestAuthenticate(String testAuthenticate) {
      this.testAuthenticate = testAuthenticate;
  }
  
  public String getTestAuthenticate() {
      return testAuthenticate;
  }
  


  public String configurationform() {
  	try {
  		config.load();
  		if (getNoWaitingMessagesInNoArchiveQueue()>0) {
	  		ActionContext ctx = ActionContext.getActionContext();
	  		
	  		if (getServlet()!=null)
	  			setSimpleMessage(getMessage("config.no_archive_warning")+" "+getNoWaitingMessagesInNoArchiveQueue()+".");
  		}
  	} catch (ConfigurationException ce) {
 		logger.error("failed to load configuration",ce);
 		throw new ChainedRuntimeException(ce.toString(),ce,logger);
 	}
  	return "success";
  }
  
  public String recoverEmails() {
	  recoveryComplete = false;
	  recoveryOutput = "";
	  recoveryFailed = 0;
	  MessageService.recoverNoArchiveMessages(new RecoverInfo());
	  return "reload";
  }
  
  public String recover() {
	 
	  return "success";
  }
  
  
  public String getRecoveryOutput() {
	  return recoveryOutput;
  }

  public boolean getRecoveryComplete() {
	  return recoveryComplete;
  }
  
  public int getRecoveryFailed() {
	  return recoveryFailed;
  }
  
  public int getNoWaitingMessagesInNoArchiveQueue() {
	  	return MessageService.getNoWaitingMessagesInNoArchiveQueue();
  }
  
  public String quarantine() {
	  MessageService.quarantineEmails();
	  return "success";
  }
  
  public int getNoQuarantinedEmails() {
	  return MessageService.getNoQuarantinedEmails();
  }

  public String configure() throws ArchivaException
  {
    SubmitButton button = getSubmitButton();
    
    if (button==null | button.action==null)
        return "reload";
    
  	logger.debug("configure() {action ='"+button.action+"', value='"+button.value+"'}");

  	
	  	if (button.action.equals("newvolume")) {
	  		return newVolume();
		} else if (button.action.equals("deletevolume")) {
	  		return deleteVolume(Integer.parseInt(button.value));
	  	} else if (button.action.equals("prioritizevolume")) {
	  		return prioritizeVolume(Integer.parseInt(button.value));
	  	} else if (button.action.equals("deprioritizevolume")) {
	  		return dePrioritizeVolume(Integer.parseInt(button.value));
	  	} else if (button.action.equals("newarchiverule")) {
	  		return newArchiveRule();
		} else if (button.action.equals("deletearchiverule")) {
			return deleteArchiveRule(Integer.parseInt(button.value));
		} else if (button.action.equals("prioritizearchiverule")) {
	  		return prioritizeArchiveRule(Integer.parseInt(button.value));
	  	} else if (button.action.equals("deprioritizearchiverule")) {
	  		return dePrioritizeArchiveRule(Integer.parseInt(button.value));
	  	} else if (button.action.equals("cancel")) {
	  		return cancel();
	  	} else if (button.action.equals("indexallvolumes")) {
	  	    return indexAllVolumes();
	  	} else if (button.action.equals("indexvolume")) {
	  	    return indexVolume(Integer.parseInt(button.value));
	  	} else if (button.action.equals("newadrolemap")) {
	  	    return newAdRoleMap();
	  	} else if (button.action.equals("deleteadrolemap")) {
	  	    return deleteAdRoleMap(Integer.parseInt(button.value));
	  	} else if (button.action.equals("newbasicrolemap")) {
	  	    return newBasicRoleMap();
	  	} else if (button.action.equals("deletebasicrolemap")) {
	  	    return deleteBasicRoleMap(Integer.parseInt(button.value));
	  	} else if (button.action.equals("newdomain")) {
	  	    return newDomain();
	  	} else if (button.action.equals("deletedomain")) {
	  	    return deleteDomain(Integer.parseInt(button.value));
	  	} else if (button.action.equals("closevolume")) {
	  	    return closeVolume(Integer.parseInt(button.value));
	  	} else if (button.action.equals("unmountvolume")) {
	  	    return unmountVolume(Integer.parseInt(button.value));
	  	} else if (button.action.equals("newagentipaddress")) {
	  		return newAgentIPAddress();
	  	} else if (button.action.equals("deleteagentipaddress")) {
	  		return deleteAgentIPAddress(Integer.parseInt(button.value));
	  	} else if (button.action.equals("reload")) {
	  	    return reload();
	  	} else if (button.action.equals("recoveremails")) {
	  		return recoverEmails();
	  	} else if (button.action.equals("quarantineemails")) {
	  		return quarantine();
	  	}
	  	
	  	ActionContext ctx = ActionContext.getActionContext();
	  	
	  	
	  	/*  
	  	
	  
  public List<String> getAgentIPAddresses() {
	  return config.getAgent().getIPAddresses();
  }
  
  public void newAgentIPAddress() {
	  config.getAgent().addAllowedIPAddress("127.0.0.1");
  }
  
  public void deleteAgentIPAddress(int id) {
	  config.getAgent().removeIPAddress(id);
  }
  
  public String getAgentPort() {
	  return Integer.toString(config.getAgent().getPort());
  }
  
  public void setAgentPort(String port) {
	  config.getAgent().setPort(Integer.valueOf(port));
  }
  
	  	 */
	  	if (!createVolumeDirectories())
	  		return reload();
	  	config.getVolumes().saveAllVolumeInfo(false);
	  	config.getVolumes().loadAllVolumeInfo();
	  	
	  	ConfigurationService.setConfig(config);
	  	
	  
	  	
	  	config.save();
	  	setSimpleMessage(getMessage("config.saved"));
	  	MessageService.initCipherKeys(); // initialize cipher keys (for new password)
	  	if (portChange) {
	  		MessageService.restartIncomingListeners(); // restart incoming SMTP and milter listeners
	  		portChange = false;
	  	}
	  
	  	audit.info("update config "+ config.getProperties().toString());
	  	ConfigurationService.setLoggingLevel(logLevel);
      	return save();
  }
  
  protected boolean createVolumeDirectories() {
	      List volumes = config.getVolumes().getVolumes();
	      boolean success = true;
	      ActionContext ctx = ActionContext.getActionContext();
	      Iterator i = volumes.iterator();
	      int c = 0;
	      while (i.hasNext()) {
	          Volume v = (Volume)i.next();
	          if (!MessageService.createVolumeDirectories(v)) {
	        	  ctx.addSimpleError(getMessage("config.volume_create_failed")+" "+c+".");
	        	  success=false;
	          }
	          c++;
	      }
	      return success;
  }

  public String lookuprolecriterion() {
	 
	  SubmitButton button = getSubmitButton();
      String action = button.action;
      ldapAttributes = "";
      if (action!=null && Compare.equalsIgnoreCase(action, "lookup")) {    	 
	      try {
	    	  if (config.getAuthMethod()==Config.AuthMethod.ACTIVEDIRECTORY)
	    		  ldapAttributes = getAttributeValues(config.getADIdentity(),true);
	    	  else
	    		  ldapAttributes = getAttributeValues(config.getLDAPIdentity(),false);
	      } catch (ArchivaException ae) {
	          ldapAttributes = getMessage("lookup_role.failure")+"." + ae.getMessage();
	          return "success";
	      }
      } 
      return "success";
  }
  
  
  protected String getAttributeValues(LDAPIdentity identity,boolean constrain) throws ArchivaException {
      String ldapAttributes = "";
      ArrayList<ADRealm.AttributeValue> attributeValues;
	  attributeValues = ConfigurationService.getLDAPAttributeValues(identity,lookupUsername, lookupPassword);
      for (ADRealm.AttributeValue attributeValue: attributeValues) {
    	  if (constrain) {
	    	  for (String key: ADIdentity.ATTRIBUTES) {
	    		 if (Compare.equalsIgnoreCase(attributeValue.getAttribute(), key)) {
	    			 ldapAttributes += attributeValue.getAttribute() + " : " + attributeValue.getValue() + "<br>";  
	    	     }
	    	  }
    	  } else ldapAttributes += attributeValue.getAttribute() + " : " + attributeValue.getValue() + "<br>";  
      } 
      return ldapAttributes;
  }
  
  public String testlogin() {
      testAuthenticate = ConfigurationService.testAuthenticate(config,lookupUsername,lookupPassword);
      return "success";
  }
  

  public String save() {
    return "save";
  }

  public String cancel() {
      try {	  	
            config.load();
            setSimpleMessage(getMessage("config.cancelled"));
      } catch (ConfigurationException ce) {
        logger.error("failed to load configuration",ce);
        throw new ChainedRuntimeException(ce.toString(),ce,logger);
      }
      reload();
      reset();
      return "cancel";
  }


  public void validate() {
      SubmitButton button = getSubmitButton();
      String action = button.action;
      if (action!=null && Compare.equalsIgnoreCase(action, "save")) {
	      ActionContext ctx = ActionContext.getActionContext();
	     
	      Iterator i = config.getArchiveRules().getArchiveRules().iterator();
	      int j = 0;
	      while (i.hasNext()) {
	          ArchiveRules.Rule ar = (ArchiveRules.Rule)i.next();
	          ArchiveRuleBean arb = new ArchiveRuleBean(ar);
	          validateRequiredField(arb.getAction(), getMessage("config.sec_rules_action_missing")+" "+j+".");
	          validateRequiredField(arb.getField(), getMessage("config.sec_rules_field_missing")+" "+j+".");
	          validateRequiredField(arb.getRegEx(), getMessage("config.sec_rules_match_crit_missing")+" "+j+".");
	          try {
	              Pattern p = Pattern.compile(arb.getRegEx());
	          } catch (Exception e) {
	              ctx.addSimpleError(getMessage("config.rules_sec_match_crit_invalid")+" "+j+".");
	          }
	          j++;
	      }
	      j = 0;
	      i = config.getVolumes().getVolumes().iterator();
	      while (i.hasNext()) {
	          Volume v = (Volume)i.next();
	          validateRequiredField(v.getPath(), getMessage("config.volume_store_path_missing")+" "+j+".");
	          validateRequiredField(v.getIndexPath(), getMessage("config.volume_index_path_missing")+" "+j+".");
	          j++;
	      }
	      
	      // active directory
	      
	      if (this.config.getAuthMethod()==Config.AuthMethod.ACTIVEDIRECTORY) {
	          validateRequiredField(config.getADIdentity().getKDCAddress(), getMessage("config.sec_kdc_missing"));
	          validateRequiredField(config.getADIdentity().getLDAPAddress(), getMessage("config.sec_ldap_missing"));
	      }
	      
	      j = 0;
	      i = config.getADIdentity().getRoleMaps().iterator();
	      while (i.hasNext()) {
	          LDAPIdentity.LDAPRoleMap r = (LDAPIdentity.LDAPRoleMap)i.next();
	          validateRequiredField(r.getRole(), getMessage("config.sec_role_no_select")+" "+j+".");
	          validateRequiredField(r.getAttribute(), getMessage("config.sec_ldap_no_select")+" "+j+".");
	          validateRequiredField(r.getRegEx(), getMessage("config.sec_role_match_crit_invalid")+" "+j+".");
	          try {
	              Pattern p = Pattern.compile(r.getRegEx());
	          } catch (Exception e) {
	              ctx.addSimpleError(getMessage("config.sec_role_match_crit_invalid")+" "+j+".");
	          }
	          j++;
	      }
	      
	    

      }
  }

 

  public class RecoverInfo extends MessageService.Recovery implements Serializable {
	  
	  /**
	 * 
	 */
	private static final long serialVersionUID = -6960318442102586086L;

	public RecoverInfo() {
		  recoveryOutput = "";
		  recoveryComplete = false;
		  recoveryFailed = 0;
	  }
	  
	  public void update(Email email, boolean success, String output) {
		  if (success)
			  recoveryOutput += email.getEmailId().getUniqueID()+" ok<br>";
		  else
			  recoveryOutput += email.getEmailId().getUniqueID()+" failed ("+output+")<br>";
	  }
	  
	  public void start() {
		  recoveryOutput += "message recovery initiated..<br>";
		  
	  };
	  	
	  public void end(int failed, int success, int total) { 
		  if (total<1) {
			  recoveryOutput += "there are no messages to process<br>";
		  } else {
			  recoveryOutput += "recovery complete (failed:"+failed+" success:"+success+" total:"+total+")<br>";
		  }
		  recoveryComplete = true;
		  recoveryFailed = failed;
	  }

  }

  public IndexStatus getIndexStatus() {
      return status;
  }

  public static class IndexStatus implements MessageService.IndexStatus, Serializable {

      /**
	 * 
	 */
	private static final long serialVersionUID = 8430809249326795262L;
	protected long completeSize=0;
      protected long totalSize=0;
      protected long completeFileCount=0;
      protected long totalFileCount = 0;
      protected boolean initialized = false;
      protected boolean complete = false;
      protected boolean error = false;
      protected String errorMessage = "unknown";

      public void start() {
          completeSize = 0; totalSize = 0; completeFileCount = 0; totalFileCount = 0;
          initialized = false; complete = false; error = false;
          logger.debug("start(). starting to process messages for indexing.");
      }

      public void finish() {
          logger.debug("finished(). indexing of messages complete. {"+toString()+"}");
          this.complete = true;
          }

      public void update(long completeSize, long totalSize, long completeFileCount, long totalFileCount) {

          this.completeSize = completeSize;
          this.totalSize = totalSize;
          this.completeFileCount = completeFileCount;
          this.totalFileCount = totalFileCount;
          initialized = true;
          logger.debug("index updated {"+toString()+"}");
      }
      public long getCompleteSize() { return completeSize / 1024; }
      public long getTotalSize() { return totalSize / 1024; }
      public long getCompleteFileCount() { return completeFileCount; }
      public long getTotalFileCount() { return totalFileCount; }
      public long getPercentComplete() { return totalFileCount!=0 ? (completeFileCount / totalFileCount) * 100 : 0; }
      public boolean getInitialized() { return initialized; }
      public boolean getComplete() { return complete; }
      public boolean getError() { return error;}
      public String getErrorMessage() { return errorMessage; }
      public void setErrorMessage(String errorMessage) {
          logger.debug("failed to index message {error='"+errorMessage+"',"+toString()+"}");
          this.errorMessage = errorMessage;
          error = true;
      }

      public String toString() {
          return "totalSize='"+totalSize+"',completeSize='"+completeSize+"',totalFileCount='"+totalFileCount+"',completeFileCount='"+completeFileCount+"'";
      }
  }
  
  public String getDebugLog() {
	  return ConfigurationService.getDebugLog();
  }
  
  public String getAuditLog() {
	  return ConfigurationService.getAuditLog();
  }
  
  public String getVersion() {
	  return ConfigurationService.getConfig().getApplicationVersion();
  }

}
