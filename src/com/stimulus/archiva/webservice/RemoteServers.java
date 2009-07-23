
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
import java.io.*;
import java.io.Serializable;
import java.util.Locale;
import java.util.Comparator;
import org.apache.commons.logging.*;
import com.stimulus.archiva.domain.*;
import com.stimulus.archiva.exception.MessageSearchException;
import com.stimulus.util.*;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RemoteServers implements Serializable, Props {

	private static final long serialVersionUID = 5805774214063729997L;
	protected static final Log logger = LogFactory.getLog(RemoteServers.class.getName());

	protected static final String remoteSearchURLKey = "remotesearch.server.url";
	protected static final String remoteSearchUsernameKey = "remotesearch.server.username";
	protected static final String remoteSearchPasswordKey = "remotesearch.server.password";
	protected static final String remoteSearchActiveKey = "remotesearch.server.active";
    protected static final String defaultRemoteSearchURL="http://192.168.0.1:8090/mailarchiva";
    protected static final String defaultRemoteSearchUsername="admin";
    protected static final String defaultRemoteSearchPassword="";
    protected static final String defaultRemoteSearchActive="yes";
    protected static final String urlSuffix = "/services/SimpleAPI";
    protected ExecutorService archivePool = Executors.newFixedThreadPool(Config.getConfig().getArchiver().getArchiveThreads());
    protected ArrayList<RemoteServer> remoteServers = new ArrayList<RemoteServer>();


    public void updateConfiguration(Settings settings) {
    	for (RemoteServer remoteServer : remoteServers) {
    		if (!validateRemoteServer(remoteServer))
				continue;

    		try {
	    		SimpleAPIStub simpleAPIStub = login(remoteServer);

				if (simpleAPIStub==null) {
					continue;
				}

				SimpleAPIStub.UpdateConfiguration updateConfiguration = new SimpleAPIStub.UpdateConfiguration();
				StringBuffer sb = new StringBuffer();
				StringBufferOutputStream out = new StringBufferOutputStream(sb);
				updateConfiguration.setNewSettings(sb.toString());
				simpleAPIStub.updateConfiguration(updateConfiguration);
    		} catch (Exception e) {
    			logger.debug("failed to update configuration:"+e.getMessage(),e);
    		}
    	}

    }

    public boolean validateRemoteServer(RemoteServer remoteServer) {
    	if (remoteServer.getUsername()==null || remoteServer.getUsername().length()<1) {
			logger.error("cannot search remote server as username is null {"+remoteServer+"}");
			return false;
		}

		if (remoteServer.getPassword()==null || remoteServer.getUsername().length()<1) {
			logger.error("cannot search remote server as password is null {"+remoteServer+"}");
			return false;
		}

		if (remoteServer.getURL()==null || remoteServer.getURL().length()<1) {
			logger.error("cannot search remote server as web service URL is null {"+remoteServer+"}");
			return false;
		}

		return true;
    }


    protected SimpleAPIStub login(RemoteServer remoteServer) throws Exception  {

	    	SimpleAPIStub simpleAPIStub = new SimpleAPIStub(getWebServiceURL(remoteServer));
			SimpleAPIStub.Login login = new SimpleAPIStub.Login();
			login.setUsername(remoteServer.getUsername());
			login.setPassword(remoteServer.getPassword());
			SimpleAPIStub.LoginResponse response = simpleAPIStub.login(login);
			if (!response.get_return()) {
				logger.error("failed to login to remote server. username, password incorrect? {username="+remoteServer.getUsername()+"',"+remoteServer.toString()+"}");
				return null;
			}
			return simpleAPIStub;


    }
    protected String getWebServiceURL(RemoteServer remoteServer) {
    	String url = remoteServer.getURL();
		if (!url.toLowerCase(Locale.ENGLISH).contains(urlSuffix)) {
			url = url.concat(urlSuffix);
		}
		if (!url.startsWith("http://")) {
			url = "http://" + url;
		}
		return url;
    }


	public int searchRemoteServers(String queryString, String filterQuery, String sortField, Search.SortOrder sortOrder, Search.DateType dateType, Date after, Date before, List<Search.Result> results, int maxResults) {

			if (remoteServers.size()<1)
				return 0;

			int hits = 0;

			maxResults = getNoResultsPerServer(maxResults);
			for (RemoteServer remoteServer : remoteServers) {

				if (!validateRemoteServer(remoteServer))
					continue;

				logger.debug("searching remote server {"+remoteServer+",query='"+queryString+"',filterQuery='"+filterQuery+"',sortField='"+sortField+",sortOrder='"+sortOrder+"',after='"+after+"',before='"+before+"'}");

				try {

					SimpleAPIStub simpleAPIStub = login(remoteServer);

					if (simpleAPIStub==null) {
						continue;
					}

					SimpleAPIStub.SearchMessage searchMessage = new SimpleAPIStub.SearchMessage();
					searchMessage.setLuceneQuery(queryString);
					searchMessage.setFilterQuery(filterQuery);
					searchMessage.setSortField(sortField);
					searchMessage.setSortOrder(sortOrder.toString());
					searchMessage.setMaxResults(maxResults);

					String afterStr = DateUtil.convertDatetoString(after);
					String beforeStr = DateUtil.convertDatetoString(before);
					searchMessage.setAfter(afterStr);
					searchMessage.setBefore(beforeStr);
					searchMessage.setDateType(dateType.toString());
					SimpleAPIStub.SearchMessageResponse searchResponse = simpleAPIStub.searchMessage(searchMessage);
					SimpleAPIStub.SearchResult[] remoteResults = searchResponse.get_return();
					for (SimpleAPIStub.SearchResult result : remoteResults) {
						results.add(new RemoteResult(result));
						hits++;
					}

				} catch (Exception e) {
					logger.error("failed to execute remote search:"+e.getMessage(),e);
					continue;
				}
			}
			if (hits>0) {
				// if we have hits, make sure they are sorted
				Comparator<Search.Result> comparator = new SearchResultComparator(sortField,sortOrder);
				Collections.sort(results, comparator);
			}
			return hits;
	}

	public void saveSettings(String prefix, Settings prop, String suffix) {
		logger.debug("saving remote search settings");
		int c = 1;
		for (RemoteServer remoteServer : remoteServers) {
			prop.setProperty(remoteSearchActiveKey+"."+c, ConfigUtil.getYesNo(remoteServer.getActive()));
			prop.setProperty(remoteSearchURLKey+"."+c,remoteServer.getURL());
			prop.setProperty(remoteSearchUsernameKey+"."+c,remoteServer.getUsername());
			prop.setProperty(remoteSearchPasswordKey+"."+c,remoteServer.getPassword());
			c++;
		}
	}

	public boolean loadSettings(String prefix, Settings prop, String suffix) {
	   logger.debug("loading remote search settings");
	   remoteServers.clear();
	   int i = 1;
        do {
        	String act = prop.getProperty(remoteSearchActiveKey+"."+Integer.toString(i));
        	String url = prop.getProperty(remoteSearchURLKey+"."+Integer.toString(i));
        	String username = prop.getProperty(remoteSearchUsernameKey+"."+Integer.toString(i));
        	String password = prop.getProperty(remoteSearchPasswordKey+"."+Integer.toString(i));
        	if (act == null || url ==null || username ==null || password == null)
            	break;
        	boolean active = ConfigUtil.getBoolean(act,defaultRemoteSearchActive);
        	RemoteServer remoteServer = new RemoteServer(active,url,username,password);
        	remoteServers.add(remoteServer);
            i++;
        } while (true);
        return true;
	}

	public int getNoResultsPerServer(int maxResults) {
		return maxResults / (remoteServers.size()+1);
	}
	public static class RemoteServer {

		protected boolean active;
		protected String url;
		protected String username;
		protected String password;

		public RemoteServer(boolean active, String url, String username, String password) {
			this.active = active;
			this.url = url;
			this.username = username;
			this.password = password;
		}
		public void setActive(boolean active) { this.active = active; }
		public boolean getActive() { return active; }
		public void setURL(String url) { this.url = url; }
		public String getURL() { return url; }
		public void setUsername(String username) { this.username = username; }
		public String getUsername() { return username; }
		public void setPassword(String password) { this.password = password; }
		public String getPassword() { return password; }

		public String toString() {
			return "url='"+url+"',username='"+username+"',password='"+password+"'";
		}

	}


	  public class SearchResultComparator implements Comparator<Search.Result> {
			/*Arrays.sort(persons, new SearchResultComparator());*/
			protected String fieldName;
			protected Search.SortOrder sortOrder;

			public SearchResultComparator(String fieldName, Search.SortOrder sortOrder) {
				this.fieldName = fieldName;
				this.sortOrder = sortOrder;
			}

			public int compare(Search.Result searchResult1, Search.Result searchResult2) {
			  try {
				  String value1 = searchResult1.getFieldValue(fieldName).getValue();
				  String value2 = searchResult2.getFieldValue(fieldName).getValue();
				  if (sortOrder==Search.SortOrder.DESCENDING)
					  return value1.compareTo(value2);
				  else
					  return value2.compareTo(value1);
			  } catch (MessageSearchException mse) {
				  logger.error("failed to compare search objects:"+mse.getMessage(),mse);
				  return 0;
			  }
			}
		}




}
