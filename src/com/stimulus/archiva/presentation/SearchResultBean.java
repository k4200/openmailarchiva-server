
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
import com.stimulus.archiva.exception.*;

import java.io.Serializable;
import java.util.*;
import org.apache.commons.logging.*;
import com.stimulus.archiva.domain.*;
import com.stimulus.archiva.domain.fields.*;

public class SearchResultBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -837802320118584736L;
	protected Search.Result searchResult;
	protected static Log logger = LogFactory.getLog(SearchResultBean.class.getName());
	protected Locale locale;
	protected boolean display = false;
	
	
	public SearchResultBean() {
		display = false;
	}
	public SearchResultBean(Search.Result searchResult, Locale locale) {
		this.searchResult = searchResult;
		this.locale = locale;
		this.display = true;
	}
	
	public boolean getDisplay() {
		return display;
	}
	
	 public List<DisplayField> getFieldValues() {
		 ArrayList<DisplayField>  list = new ArrayList<DisplayField>();
		 EmailFields emailFields = Config.getConfig().getEmailFields();
		 for (EmailField field :  emailFields.getAvailableFields().values()) {
			 if (field.getShowInResults()!=EmailField.ShowInResults.NORESULTS) {
				 try {
					 EmailFieldValue efv = searchResult.getFieldValue(field.getName());
					 list.add(DisplayField.getDisplayField(efv , locale,false));
				 } catch (MessageSearchException mse) {
					 logger.debug("failed to retrieve field value from message: "+mse.getMessage()); 
				 }
			 }
		 }
		 return list;
	 }
	 
	public String getUniqueID() {
		try { 
			return searchResult.getEmailId().getUniqueID();
		} catch (MessageSearchException mse) {
			logger.debug("failed to retrieve unique message id: "+mse.getMessage(),mse);
			return null;
		}
	}
	
	
	public boolean getMessageExist() {
		try {
			EmailID emailID = searchResult.getEmailId();
			Volume volume = emailID.getVolume();
			return (volume!=null);
				
			/*if (volume!=null) {
				Archiver archiver = Config.getConfig().getArchiver();
				boolean exists = archiver.isMessageExist(emailID);
				if (!exists) {
					logger.debug("message is not accessible on disk");
				}
				return exists;
			} else {
				logger.debug("could not lookup volume. the index appears out of sync with volumeinfo ID field.");
			}*/
		} catch (Exception e) {
			logger.debug("failed to determine if message exists in store:"+e.getMessage(),e);
		}
		return false; 
	}
	
	public String getVolumeID() {
		try {
			EmailID emailID = searchResult.getEmailId();
			Volume volume = emailID.getVolume();
			if (volume!=null) {
				String volumeID = volume.getID();
				return volumeID;
			} else return null;
		} catch (MessageSearchException mse) {
			logger.debug("failed to retrieve volumeid: "+mse.getMessage(),mse);
			return null;
		}
		//return searchResult.getEmailId().getVolume().getID();
	}
	


    public static synchronized List<SearchResultBean> getSearchResultBeans(List<Search.Result> results,Locale locale) {
		  List<SearchResultBean> searchResultBeans = new LinkedList<SearchResultBean>();
		  try {
			  for (Search.Result result: results) {
				  searchResultBeans.add(new SearchResultBean(result,locale));
			  }
			  while (searchResultBeans.size()<Config.getConfig().getSearch().getMaxSearchResults()) {
				  searchResultBeans.add(new SearchResultBean());
			  }
		  } catch (java.util.ConcurrentModificationException ce) {
			  	// bit of a hack to say the least
			 
			  try { Thread.sleep(50); } catch (Exception e) {}
			  return getSearchResultBeans(results,locale);
		  }
		  return searchResultBeans;
	}
    
}
