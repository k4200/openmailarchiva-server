
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.stimulus.archiva.domain.EmailID;
import com.stimulus.archiva.domain.Search;
import com.stimulus.archiva.domain.Volume;
import com.stimulus.archiva.domain.fields.EmailField;
import com.stimulus.archiva.domain.fields.EmailFieldValue;

public class SearchResultBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -837802320118584736L;
	protected Search.Result searchResult;
	protected static Logger logger =  Logger.getLogger(SearchResultBean.class.getName());
	protected Locale locale;
	
	public SearchResultBean(Search.Result searchResult, Locale locale) {
		this.searchResult = searchResult;
		this.locale = locale;
	}
	
	 public List<DisplayField> getFieldValues() {
		 ArrayList<DisplayField>  list = new ArrayList<DisplayField>();
		 Iterator i = EmailField.getAvailableFields().iterateValues();
		 while (i.hasNext()) {
			 EmailField field = (EmailField)i.next();
			 if (field.getShowInResults()!=EmailField.ShowInResults.NORESULTS) {
				 EmailFieldValue efv = searchResult.getFieldValue(field.getName());
				 list.add(DisplayField.getDisplayField(efv , locale,false));
			 }
		 }
		 return list;
	 }
	 
	public String getUniqueID() {
		return searchResult.getEmailId().getUniqueID();
	}
	
	public String getVolumeID() {
		EmailID emailID = searchResult.getEmailId();
		Volume volume = emailID.getVolume();
		if (volume!=null) {
			String volumeID = volume.getID();
			return volumeID;
		} else return null;
		//return searchResult.getEmailId().getVolume().getID();
	}


    public static List<SearchResultBean> getSearchResultBeans(List<Search.Result> results,Locale locale) {
		List<SearchResultBean> searchResultBeans = new LinkedList<SearchResultBean>();
		  for (Search.Result result: results)
			  searchResultBeans.add(new SearchResultBean(result,locale));
		  return searchResultBeans;
	}

}
