
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

import com.stimulus.archiva.domain.*;
import com.stimulus.archiva.domain.Identity.RoleMap;
import com.stimulus.archiva.exception.*;
import com.stimulus.archiva.security.realm.ADRealm;
import com.stimulus.archiva.service.*;
import com.stimulus.archiva.authentication.*;
import com.stimulus.archiva.authentication.ADIdentity.ADRoleMap;

import com.stimulus.struts.*;
import com.stimulus.util.EnumUtil;

import org.apache.log4j.Logger;
import java.util.*;
import java.util.regex.*;

public class ConfigBean extends BaseBean {

  private static final long serialVersionUID = 2275642295115995805L;
  protected static final Logger logger = Logger.getLogger(MessageBean.class.getName());
  protected static final Logger audit = Logger.getLogger("com.stimulus.archiva.audit");

  protected boolean inboundChecked = false;
  protected boolean outboundChecked = false;
  protected boolean internalChecked = false;
  protected String lookupPassword = "";
  protected String lookupUsername = "";
  protected String ldapAttributes = "";
  protected String testAuthenticate = "";
  
  protected IndexStatus status = new IndexStatus();

  protected Config config = null;

  public ConfigBean() {
  		config = new Config();
  }

  public Config getConfig() {
  	return config;
  }

  public List getRuleFields() {
  	return ArchiveRuleBean.getFields();
  }

  public List getRuleFieldLabels() {
	  return translateList(ArchiveRuleBean.getFieldLabels());
  }

  public List getRuleActionFields() {
  	return ArchiveRuleBean.getActions();
  }
  
  public List getRuleActionLabels() {
  	return translateList(ArchiveRuleBean.getActionLabels());
  }

  public List getRoleMapAttributes() {
	return ADRoleMapBean.getAttributes();
  }

  public List getRoleMapAttributeLabels() {
    return translateList(ADRoleMapBean.getAttributeLabels());
  }
  
  public List getRoleMapRoles() {
   	return Identity.getRoles();
  }

  public List getRoleMapRoleLabels() {
    	return translateList(Identity.getRoleLabels());
  }

  public void setArchiveInbound(String archiveInbound) {  inboundChecked = true; config.getArchiveRules().setArchiveInbound(config.getBoolean(archiveInbound,"no")); };

  public void setArchiveOutbound(String archiveOutbound) {  config.getArchiveRules().setArchiveOutbound(config.getBoolean(archiveOutbound,"no")); outboundChecked=true;  };

  public void setArchiveInternal(String archiveInternal) { internalChecked=true; config.getArchiveRules().setArchiveInternal(config.getBoolean(archiveInternal,"no")); };

  
  public String getArchiveInbound() {  return config.getYesNo(config.getArchiveRules().getArchiveInbound()); }

  public String getArchiveOutbound() {return config.getYesNo(config.getArchiveRules().getArchiveOutbound()); }

  public String getArchiveInternal() { return config.getYesNo(config.getArchiveRules().getArchiveInternal()); }

  public void setKDCAddress(String kdcAddress) { config.setKDCAddress(kdcAddress); }

  public String getKDCAddress() { return config.getKDCAddress(); }

  public void setLDAPAddress(String ldapAddress) { config.setLDAPAddress(ldapAddress); }

  public String getLDAPAddress() { return config.getLDAPAddress(); }

  public void setAuthMethod(String authMethod) { config.setAuthMethod(authMethod); }
  
  public String getAuthMethod() { return config.getAuthMethod().toString().toLowerCase(); }

  public List getAuthMethods() {
  	return EnumUtil.enumToList(Config.AuthMethod.values());
  }
  
  public List getAuthMethodLabels() {
  	return translateList(EnumUtil.enumToList(Config.AuthMethod.values(),"config.sec_auth_method_"));
  }
 
  public void reset() {
  	outboundChecked=false;
  	inboundChecked=false;
  	internalChecked=false;
  
  }

  public String reload() {
      ldapAttributes = "";
      testAuthenticate = "";
      return "reload";
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
      MessageService.indexAllVolumes(status);
      return "reload";
  }

  public String indexVolume(int volumeIndex) throws ArchivaException {
      logger.debug("indexVolume() {volumeIndex='"+volumeIndex+"'}");
      MessageService.indexVolume(volumeIndex,status);
      return "reload";
  }

  public String closeVolume(int volumeIndex) throws ConfigurationException {
      logger.debug("closeVolume()");
      config.getVolumes().closeVolume(volumeIndex);
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

  public List getAdRoleMaps() {
      return ADRoleMapBean.getADRoleMapBeans(config.getADIdentity().getRoleMaps());
  }
  
  public static List<String> getAdRoles() {
  	return Identity.getRoles();
  }
  
  public static List<String> getAdRoleLabels() {
	return Identity.getRoleLabels();
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
  
  protected void CheckUncheckedFields() {
  	if (!outboundChecked)
  		config.getArchiveRules().setArchiveOutbound(false);
  	if (!inboundChecked)
  		config.getArchiveRules().setArchiveInbound(false);
  	if (!internalChecked)
  		config.getArchiveRules().setArchiveInternal(false);


  }

  public String configurationform() {
  	try {
  		config.load();

  	} catch (ConfigurationException ce) {
 		logger.error("failed to load configuration",ce);
 		throw new ChainedRuntimeException(ce.toString(),ce,logger);
 	}
  	return "success";
  }

  public String configure() throws ArchivaException
  {
    SubmitButton button = getSubmitButton();
    
    if (button==null | button.action==null)
        return "reload";
    
  	logger.debug("configure() {action ='"+button.action+"', value='"+button.value+"'}");

  		CheckUncheckedFields();

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
	  	} else if (button.action.equals("reload"))
	  	    return reload();
	  	MessageService.createMessageStoreDirectories(config);
	  	config.getVolumes().loadAllVolumeInfo();
	  	config.save();
	  	setSimpleMessage(getMessage("config.saved"));
	  	ConfigurationService.setConfig(config);
	  	MessageService.initCipherKeys(); // initialize cipher keys (for new password)
	  	audit.info("update config "+ config.getProperties().toString());
      	return save();
  }
  
  public String lookuprolecriterion() {
	  SubmitButton button = getSubmitButton();
      String action = button.action;
      ldapAttributes = "";
      if (action!=null && action.compareToIgnoreCase("lookup")==0) {
	      ArrayList<ADRealm.AttributeValue> attributeValues;
	      try {
	          attributeValues = ConfigurationService.getLDAPAttributeValues(config,lookupUsername, lookupPassword);
	      } catch (ArchivaException ae) {
	          ldapAttributes = getMessage("lookup_role.failure")+"." + ae.getMessage();
	          return "success";
	      }
	      for (ADRealm.AttributeValue attributeValue: attributeValues) {
	    	  for (String key: ADIdentity.ATTRIBUTES) {
	    	     if (attributeValue.getAttribute().equalsIgnoreCase(key)) {
	    	    	  ldapAttributes += attributeValue.getAttribute() + " : " + attributeValue.getValue() + "<br>";  
	    	     }
	    	  }
	      } 
      } 
    	  
      return "success";
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
      if (action!=null && action.compareToIgnoreCase("save")==0) {
	      ActionContext ctx = ActionContext.getActionContext();
	      if (this.config.getAuthMethod()==Config.AuthMethod.ACTIVEDIRECTORY) {
	          validateRequiredField(config.getKDCAddress(), getMessage("config.sec_kdc_missing"));
	          validateRequiredField(config.getLDAPAddress(), getMessage("config.sec_ldap_missing"));
	      }
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
	      j = 0;
	      i = config.getADIdentity().getRoleMaps().iterator();
	      while (i.hasNext()) {
	          ADIdentity.ADRoleMap r = (ADIdentity.ADRoleMap)i.next();
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
	      
	      j = 0;
	      i = config.getBasicIdentity().getRoleMaps().iterator();
	      while (i.hasNext()) {
	    	  BasicIdentity.BasicRoleMap r = (BasicIdentity.BasicRoleMap)i.next();
	          validateRequiredField(r.getRole(), getMessage("config.sec_role_no_select")+" "+j+".");
	          validateRequiredField(r.getUsername(), getMessage("config.sec_role_match_username_invalid")+" "+j+".");
	          validateRequiredField(r.getLoginPassword(), getMessage("config.sec_role_match_password_invalid")+" "+j+".");
	          
	          String username = r.getUsername();
		      int at = username.lastIndexOf('@');
		      if (at==-1) {
		    	   validateRequiredField(null, getMessage("config.sec_role_match_username_format_invalid")+" "+j+".");
		      }
	          j++;
	      }
	  

	      j = 0;
	      i = config.getDomains().getDomains().iterator();
	      while (i.hasNext()) {
	          Domains.Domain d = (Domains.Domain)i.next();
	          validateRequiredField(d.getName(), getMessage("config.sec_domain_missing")+" "+j+".");
	          j++;
	      }

      }
  }


  public IndexStatus getIndexStatus() {
      return status;
  }



  public static class IndexStatus implements MessageService.IndexStatus {

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

}
