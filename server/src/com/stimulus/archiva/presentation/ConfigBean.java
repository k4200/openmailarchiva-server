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
package com.stimulus.archiva.presentation;

import com.stimulus.archiva.domain.*;
import com.stimulus.archiva.exception.*;
import com.stimulus.archiva.security.realm.ADRealm;
import com.stimulus.archiva.service.*;
import com.stimulus.struts.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;
import java.util.Iterator;
import java.util.regex.*;

public class ConfigBean extends BaseBean {

  /* Constants */

  protected static final Logger logger = Logger.getLogger(MessageBean.class.getName());
  protected static final Logger audit = Logger.getLogger("com.stimulus.archiva.audit");

  protected static final List DEBUG_LIST;
  protected static final List DEBUG_LABEL_LIST;
  protected static final List RULE_FIELD_LIST;
  protected static final List RULE_FIELD_LABEL_LIST;
  protected static final List RULE_ACTION_LABEL_LIST;
  protected static final List RULE_ACTION_LIST;
  protected static final List ROLE_MAP_ATTRIBUTE_LIST;
  protected static final List ROLE_MAP_ATTRIBUTE_LABEL_LIST;
  protected static final List ROLE_MAP_ROLE_LIST;
  protected static final List ROLE_MAP_ROLE_LABEL_LIST;

  protected boolean inboundChecked = false;
  protected boolean outboundChecked = false;
  protected boolean internalChecked = false;
  protected boolean consoleAuthenticateChecked = false;
  protected String lookupPassword = "";
  protected String lookupUsername = "";
  protected String ldapAttributes = "";
  protected String testAuthenticate = "";
  
  protected IndexStatus status = new IndexStatus();

  protected Config config = null;

  static {
    List debugList = new ArrayList();
    debugList.add("FATAL");
    debugList.add("DEBUG");
    debugList.add("WARN");
    debugList.add("INFO");
    DEBUG_LIST = Collections.unmodifiableList(debugList);

    List debugLabelList = new ArrayList();
    debugLabelList.add("FATAL");
    debugLabelList.add("DEBUG");
    debugLabelList.add("WARN");
    debugLabelList.add("INFO");
    DEBUG_LABEL_LIST = Collections.unmodifiableList(debugLabelList);


    RULE_FIELD_LIST = ArchiveRules.RULE_FIELD_LIST;
    RULE_FIELD_LABEL_LIST = ArchiveRules.RULE_FIELD_LIST;
    RULE_ACTION_LIST = ArchiveRules.RULE_ACTION_LIST;
    RULE_ACTION_LABEL_LIST = ArchiveRules.RULE_ACTION_LIST;
    ROLE_MAP_ATTRIBUTE_LIST = RoleMaps.ATTRIBUTES;
    ROLE_MAP_ATTRIBUTE_LABEL_LIST = RoleMaps.ATTRIBUTES;
    ROLE_MAP_ROLE_LIST = RoleMaps.ROLES;
    ROLE_MAP_ROLE_LABEL_LIST = RoleMaps.ROLES;

  }

  public ConfigBean() {
  		config = new Config();
  }

  public Config getConfig() {
  	return config;
  }

  public String save() {
    return "success";
  }

  public String cancel() {
  	return "success";
  }

  public List getDebugFields() {
    return DEBUG_LIST;
  }

  public List getDebugLabels() {
    return DEBUG_LABEL_LIST;
  }

  public List getRuleFields() {
  	return RULE_FIELD_LIST;
  }

  public List getRuleFieldLabels() {
  	return RULE_FIELD_LABEL_LIST;
  }

  public List getRuleActionFields() {
  	return RULE_ACTION_LIST;
  }
  public List getRuleActionLabels() {
  	return RULE_ACTION_LABEL_LIST;
  }

  public List getRoleMapAttributeLabels() {
    	return ROLE_MAP_ATTRIBUTE_LABEL_LIST;
  }

  public List getRoleMapAttributes() {
  	return ROLE_MAP_ATTRIBUTE_LIST;
  }

  public List getRoleMapRoleLabels() {
     	return ROLE_MAP_ROLE_LABEL_LIST;
   }

   public List getRoleMapRoles() {
   	return ROLE_MAP_ROLE_LIST;
}



  public void setArchiveInbound(String archiveInbound) {  inboundChecked = true; config.getArchiveRules().setArchiveInbound(config.getBoolean(archiveInbound,"no")); };

  public void setArchiveOutbound(String archiveOutbound) {  config.getArchiveRules().setArchiveOutbound(config.getBoolean(archiveOutbound,"no")); outboundChecked=true;  };

  public void setArchiveInternal(String archiveInternal) { internalChecked=true; config.getArchiveRules().setArchiveInternal(config.getBoolean(archiveInternal,"no")); };

  public void setConsoleAuthenticate(String consoleAuthenticate) {   consoleAuthenticateChecked = true; config.setConsoleAuthenticate(config.getBoolean(consoleAuthenticate,"no")); };


  public String getArchiveInbound() {  return config.getYesNo(config.getArchiveRules().getArchiveInbound()); }

  public String getArchiveOutbound() {return config.getYesNo(config.getArchiveRules().getArchiveOutbound()); }

  public String getArchiveInternal() { return config.getYesNo(config.getArchiveRules().getArchiveInternal()); }

  public String getConsoleAuthenticate() { return config.getYesNo(config.getConsoleAuthenticate()); }

  public void setKDCAddress(String kdcAddress) { config.setKDCAddress(kdcAddress); }

  public String getKDCAddress() { return config.getKDCAddress(); }

  public void setLDAPAddress(String ldapAddress) { config.setLDAPAddress(ldapAddress); }

  public String getLDAPAddress() { return config.getLDAPAddress(); }


  /*
  public boolean getArchiveInbound() {return config.getArchiveInbound(); }

  public boolean getArchiveOutbound() {return config.getArchiveOutbound(); }

  public boolean getArchiveInternal() {return config.getArchiveInternal(); }
  */
  public void reset() {
  	outboundChecked=false;
  	inboundChecked=false;
  	internalChecked=false;
  	consoleAuthenticateChecked=false;

  }

  public String reload() throws ArchivaException {
      ldapAttributes = "";
      testAuthenticate = "";
      return "reload";
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
  	config.getVolumes().setVolumePriority(id, Volumes.PRIORITY_HIGHER);
  	return "reload";
  }
  public String dePrioritizeVolume(int id) throws ConfigurationException {
  	config.getVolumes().setVolumePriority(id, Volumes.PRIORITY_LOWER);
  	return "reload";
  }

  public String deleteRoleMap(int id) {
      config.getRoleMaps().deleteRoleMap(id);
      return "reload";
  }
  public String deleteArchiveRule(int id) throws ConfigurationException {
  	config.getArchiveRules().deleteArchiveRule(id);
  	return "reload";
  }

  public String newRoleMap() throws ConfigurationException {
    	config.getRoleMaps().addRoleMap((String)ROLE_MAP_ROLE_LIST.get(0),"" /*domain*/, (String)ROLE_MAP_ATTRIBUTE_LIST.get(0),"");
    	return "reload";
  }

  public String newArchiveRule() throws ConfigurationException {
  	config.getArchiveRules().addArchiveRule((String)RULE_ACTION_LIST.get(0),(String)RULE_FIELD_LIST.get(0),"");
  	return "reload";
  }

  public String newDomain() throws ConfigurationException {
    	config.getDomains().addDomain("");
    	return "reload";
  }

  public String deleteDomain(int id) throws ConfigurationException {
    	config.getDomains().deleteDomain(id);
    	return "reload";
  }

  public String prioritizeArchiveRule(int id) throws ConfigurationException {
  	config.getArchiveRules().setArchiveRulePriority(id, ArchiveRules.PRIORITY_HIGHER);
  	return "reload";
  }
  public String dePrioritizeArchiveRule(int id) throws ConfigurationException {
  	config.getArchiveRules().setArchiveRulePriority(id, ArchiveRules.PRIORITY_LOWER);
  	return "reload";
  }

  public void setVolumePriority(int id, int priority)  {
  	config.getVolumes().setVolumePriority(id, priority);
  }


  public List getVolumes() {
  	return config.getVolumes().getVolumes();
  }

  public List getDomains() {
    	return config.getDomains().getDomains();
    }

  public Volume getMessageStoreVolume(int index) {
  	return config.getVolumes().getVolume(index);
  }

  public List getArchiveRules() {
  	return config.getArchiveRules().getArchiveRules();
  }

  public ArchiveRules.Rule getArchiveRule(int index) {
  	return config.getArchiveRules().getArchiveRule(index);
  }

  public RoleMaps.RoleMap getRoleMap(int index) {
    	return config.getRoleMaps().getRoleMap(index);
  }

  public List getRoleMaps() {
      return config.getRoleMaps().getRoleMaps();
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
  	if (!consoleAuthenticateChecked)
  	    config.setConsoleAuthenticate(false);

  }

  public List getDomainLabels() {
      List domainList = new ArrayList();
      for (Iterator i=config.getDomains().getDomains().iterator();i.hasNext();) {
          domainList.add(((Domains.Domain)i.next()).getName());
      }
      return domainList;
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
	  	} else if (button.action.equals("newrolemap")) {
	  	    return newRoleMap();
	  	} else if (button.action.equals("deleterolemap")) {
	  	    return deleteRoleMap(Integer.parseInt(button.value));
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
	  	ConfigurationService.setConfig(config);
	  	MessageService.initCipherKeys(); // initialize cipher keys (for new password)
	  	audit.info("update config "+ config.getProperties().toString());

  	return "success";
  }
  
  public String lookuprolecriterion() {
      List attributeValues;
      try {
          attributeValues = ConfigurationService.getLDAPAttributeValues(config,lookupUsername, lookupPassword);
      } catch (ArchivaException ae) {
          ldapAttributes = "The LDAP attributes not be retrieved from Active Directory. Please check your AD settings."+ae.getMessage();
          return "success";
      }
      Iterator i = attributeValues.iterator();
      ldapAttributes = "Copy and paste the desired attribute value:<br><br>";
      while (i.hasNext()) {
         ADRealm.AttributeValue av = (ADRealm.AttributeValue)i.next();
         for (int j=0;j<ROLE_MAP_ATTRIBUTE_LIST.size();j++) {
		        String field = (String)ROLE_MAP_ATTRIBUTE_LIST.get(j);
		        if (av.getAttribute().compareToIgnoreCase(field)==0)
		              ldapAttributes += av.getAttribute() + " : " + av.getValue() + "<br>";
		 }
      }
      return "success";
  }

  public String testlogin() {
      testAuthenticate = ConfigurationService.testAuthenticate(config,lookupUsername,lookupPassword);
      return "success";
  }

  public void validate() {
      SubmitButton button = getSubmitButton();
      String action = button.action;
      if (action!=null && action.compareToIgnoreCase("save")==0) {
	      ActionContext ctx = ActionContext.getActionContext();
	      if (this.config.getConsoleAuthenticate()) {
	          validateRequiredField(config.getKDCAddress(), "The KDC Server Address is missing; it is required for console authentication");
	          validateRequiredField(config.getLDAPAddress(), "An LDAP Server Address is missing; it is required for console authentication");
	      }
	      Iterator i = config.getArchiveRules().getArchiveRules().iterator();
	      int j = 0;
	      while (i.hasNext()) {
	          ArchiveRules.Rule ar = (ArchiveRules.Rule)i.next();
	          validateRequiredField(ar.getAction(), "The Action must be selected in Archive Rule "+j+".");
	          validateRequiredField(ar.getField(), "The Field must be selected in Archive Rule "+j+".");
	          validateRequiredField(ar.getRegEx(), "The Match Criterion is missing in Archive Rule "+j+".");
	          try {
	              Pattern p = Pattern.compile(ar.getRegEx());
	          } catch (Exception e) {
	              ctx.addSimpleError("The criterion field in Archive Rule "+j+" contains an invalid regular expression (i.e. syntax is incorrect).");
	          }
	          j++;
	      }
	      j = 0;
	      i = config.getVolumes().getVolumes().iterator();
	      while (i.hasNext()) {
	          Volume v = (Volume)i.next();
	          validateRequiredField(v.getPath(), "The Store Path is missing in Volume "+j+".");
	          validateRequiredField(v.getIndexPath(), "The Index Path is missing in Volume "+j+".");
	          j++;
	      }
	      j = 0;
	      i = config.getRoleMaps().getRoleMaps().iterator();
	      while (i.hasNext()) {
	          RoleMaps.RoleMap r = (RoleMaps.RoleMap)i.next();
	          validateRequiredField(r.getRole(), "A Role must be selected in Role Assignment "+j+".");
	          validateRequiredField(r.getAttribute(), "The LDAP Attribute must be selected in Role Assignment "+j+".");
	          validateRequiredField(r.getRegEx(), "The Match Criterion must contain a valid regular expression in Role Assignment "+j+".");
	          try {
	              Pattern p = Pattern.compile(r.getRegEx());
	          } catch (Exception e) {
	              ctx.addSimpleError("The Match Criterion in Archive Rule "+j+" contains an invalid regular expression (i.e. syntax is incorrect).");
	          }
	          j++;
	      }

	      j = 0;
	      i = config.getDomains().getDomains().iterator();
	      while (i.hasNext()) {
	          Domains.Domain d = (Domains.Domain)i.next();
	          validateRequiredField(d.getName(), "The Domain Name (e.g. company.com) must be specified in Domain "+j+".");
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
