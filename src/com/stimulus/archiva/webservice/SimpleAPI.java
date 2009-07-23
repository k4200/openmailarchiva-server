
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
 package com.stimulus.archiva.webservice;

import java.util.ArrayList;
import com.stimulus.archiva.domain.Config;
import com.stimulus.archiva.domain.Roles;
import com.stimulus.archiva.domain.Search.Result;
import com.stimulus.archiva.search.*;
import com.stimulus.archiva.domain.*;
import com.stimulus.util.*;
import java.util.*;
import com.stimulus.archiva.service.*;
import java.io.*;
import org.apache.axis2.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SimpleAPI {

	private static MailArchivaPrincipal principal = null;
	protected static final Log logger = LogFactory.getLog(SearchService.class.getName());
	protected static final Log audit = LogFactory.getLog("com.stimulus.archiva.audit");


	public static boolean login(String username, String password) throws AxisFault {
		if (username==null || password==null)
			return false;
		principal = ConfigurationService.authenticate(username,password);
		if (principal!=null) {
	  	    Roles.Role role = Config.getConfig().getRoles().getRole(principal.getRole());
	  	    return true;
		} else {
			audit.warn("failed to login to web service. could not authenticate user. password incorrect? {username='"+username+"'}");
		}

		return false;
	}

	public  static void logout() {
		audit.info("web service logout {"+principal+"'}");
		principal = null;

	}
	public static byte[] getMessageByID(String volumeId, String emailId) throws AxisFault {
		if (principal!=null)
			throw new AxisFault("not logged in");
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Email email =  MessageService.getMessageByID(volumeId,emailId,false);
			email.writeTo(baos);
			return baos.toByteArray();
		} catch (Exception ae) {
			throw new AxisFault("failed to retrieve email:"+ae.getMessage());
		}
	}


	public static void updateConfiguration(String newSettings) throws AxisFault {
		if (principal==null) {
			audit.warn("attempt to update configuration while not logged in");
			throw new AxisFault("not logged in");

		}
		Settings settings = new Settings();
		try {
			settings.load(new BufferedReader(new StringReader(newSettings)));
			Config.getConfig().saveConfigurationFile(principal,settings);
		} catch (IOException io) {
			logger.error("failed to parse settings received from web service:"+io.getMessage());
		} catch (Exception ce) {
			logger.error("failed to save new configuration:"+ce.getMessage());
		}
	}
	public static SearchResult[] searchMessage(String luceneQuery, String filterQuery, String sortField, String sortOrder, String dateType, String after, String before, int maxResults) throws AxisFault  {
		if (principal==null) {
			audit.warn("attempt to search message via web service interface while not logged in {query='"+luceneQuery+"'}");
			throw new AxisFault("not logged in");
		}

		audit.info("executing search via web service interface {"+principal+",query='"+luceneQuery+"'}");
		ArrayList<SearchResult> searchResult = null;
		try {
			StandardSearch search = new StandardSearch();
			// we dont want to perform remote searches otherwise we can get into a cyclical loop
			search.init();
		    if (luceneQuery!=null) {
		    	search.setSearchQuery(luceneQuery);
		    }
			if (sortField!=null && sortOrder!=null) {
				search.setSortField(sortField);
				search.setSortOrder(Search.SortOrder.valueOf(sortOrder.toUpperCase(Locale.ENGLISH)));
			}
			if (filterQuery!=null) {
				search.setFilterQuery(filterQuery);
			}
			if (after!=null) {
				search.setAfter(DateUtil.convertStringToDate(after));
			}
			if (before!=null) {
				search.setBefore(DateUtil.convertStringToDate(before));
			}
			if (dateType!=null) {
				search.setDateType(Search.DateType.valueOf(dateType.toUpperCase(Locale.ENGLISH)));
			}
			search.setMaxSearchResults(maxResults);
			search.searchMessage();
			searchResult = new ArrayList<SearchResult>();
			List<Result> results = search.getResults();
			int i = 0;
			for (Result result : results) {
				searchResult.add(new SearchResult(result));
				i++;
				if (i>Config.getConfig().getSearch().getMaxSearchResults())
					break;
			}
		} catch (Exception e) {
			logger.error("failed to search for message:"+e.getMessage());
			throw new AxisFault("failed to search for message:"+e.getMessage());
		}
		int i = 0;
		SearchResult[] out = new SearchResult[searchResult.size()];
		for (SearchResult sr : searchResult) {
			out[i] = sr;
			i++;
		}
		return out;
		//return (SearchResult[])searchResult.toArray();
	}


}
