package com.stimulus.archiva.webservice;

import java.util.ArrayList;
import com.stimulus.archiva.domain.Config;
import com.stimulus.archiva.domain.Roles;
import com.stimulus.archiva.domain.Search.Result;
import com.stimulus.archiva.search.*;
import com.stimulus.archiva.domain.*;
import java.util.*;
import com.stimulus.archiva.service.*;
import java.io.*;
import org.apache.axis2.*;

public class SearchService {

	private static MailArchivaPrincipal principal = null;
	
	public static boolean login(String username, String password) throws AxisFault {
		if (username==null || password==null)
			return false;
		principal = ConfigurationService.authenticate(username,password);
		if (principal!=null) {
	  	    Roles.Role role = Config.getConfig().getRoles().getRole(principal.getRole());
	  	    if (role.getName().equalsIgnoreCase("administrator"))
	  	    	return true;	
		} 
		return false;
	}
	
	public  static void logout() {
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
	
	public static SearchResult[] searchMessage(String luceneQuery) throws AxisFault  {
		if (principal!=null) 
			throw new AxisFault("not logged in");
		ArrayList<SearchResult> searchResult = null;
		try {
			StandardSearch search = new StandardSearch(Config.getConfig());
			search.searchMessage();
			search.setQuery(luceneQuery);
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
			throw new AxisFault("failed to search for message:"+e.getMessage());
		}
		return (SearchResult[])searchResult.toArray();
	}
	
	
	
}
