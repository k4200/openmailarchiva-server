
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

import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import com.stimulus.archiva.domain.ArchiveRules;
import com.stimulus.archiva.domain.ArchiveRules.Action;
import com.stimulus.archiva.domain.ArchiveRules.Field;
import com.stimulus.archiva.exception.ConfigurationException;
import com.stimulus.util.*;
import com.stimulus.struts.BaseBean;

public class ArchiveRuleBean extends BaseBean {

	private static final long serialVersionUID = -2975630165623482789L;
	protected static Logger logger = Logger.getLogger(ArchiveRules.class.getName());
	protected ArchiveRules.Rule archiveRule;
	
    public ArchiveRuleBean(ArchiveRules.Rule archiveRule) {
        this.archiveRule = archiveRule;
    }
    
  	public void setAction(String action) throws ConfigurationException {
  		 Action newAction = Action.ARCHIVE;
  		 try {
  			newAction = Action.valueOf(action.trim().toUpperCase());
  		 } catch (IllegalArgumentException iae) {
 	    		logger.error("failed to set archive rule action. action is set to an illegal value {action='"+action+"'}");
 	    		logger.info("archive rule is automatically set to archive the message (error recovery)");
  		 }
  		 archiveRule.setAction(newAction);
	}
	  	
	public String getAction() { return archiveRule.getAction().toString().toLowerCase(); }
	  	
	public String getField() { return archiveRule.getField().toString().toLowerCase(); }

	public void setField(String field) throws ConfigurationException {
	  	Field newField = Field.TO;	
	  	try {
	  		newField = Field.valueOf(field.trim().toUpperCase());
	  	} catch (IllegalArgumentException iae) {
	    		logger.error("failed to set archive rule field. field is set to an illegal value {field='"+field+"'}");
	    		logger.info("archive rule field is set to 'to' by default (error recovery)");
		}
	    archiveRule.setField(newField);
	}
	
	public String getRegEx() { return archiveRule.getRegEx(); }
	  	
	public void setRegEx(String regex) { 
		archiveRule.setRegEx(regex); 
	}
	 
    public static List<ArchiveRuleBean> getArchiveRuleBeans(List<ArchiveRules.Rule> archiveRules) {
		  List<ArchiveRuleBean> ArchiveRuleBeans = new LinkedList<ArchiveRuleBean>();
		  for (ArchiveRules.Rule archiveRule: archiveRules)
			  ArchiveRuleBeans.add(new ArchiveRuleBean(archiveRule));
		  return ArchiveRuleBeans;
	}
    
    public static List<ArchiveRuleBean> getArchiveRuleBeans(ArchiveRules archiveRules) {
    	return getArchiveRuleBeans(archiveRules.getArchiveRules());
    }
    
    public static List<String> getFields() {
    	return EnumUtil.enumToList(ArchiveRules.Field.values());
    }
    
    public static List<String> getFieldLabels() {
    	return EnumUtil.enumToList(ArchiveRules.Field.values(),"field_label_");
    }
    
    protected static List<Enum> getFieldEnums() {
    	return (List<Enum>)EnumUtil.enumToListEnums(ArchiveRules.Field.values());
    }
    
    public static List<String> getActions() {
    	return EnumUtil.enumToList(ArchiveRules.Action.values());
    }
    
    public static List<String> getActionLabels() {
    	return EnumUtil.enumToList(ArchiveRules.Action.values(),"action_label_");
    }
    
    protected static List<Enum> getActionEnums() {
    	return (List<Enum>)EnumUtil.enumToListEnums(ArchiveRules.Action.values());
    }
    
    public static List<String> getPriorities() {
    	return EnumUtil.enumToList(ArchiveRules.Priority.values());
    }
    
    
    
}