
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
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.mail.internet.*;
import org.apache.log4j.Logger;

import com.stimulus.archiva.domain.*;
import com.stimulus.util.DecodingUtil;
import com.stimulus.struts.BaseBean;

public class SearchResultBean extends BaseBean {

	protected Search.Result searchResult;
	protected static Logger logger = Logger.getLogger(SearchResultBean.class.getName());
	
	public SearchResultBean(Search.Result searchResult) {
		this.searchResult = searchResult;
	}
	
	public String getUniqueID() {
		return searchResult.getEmailId().getUniqueID();
	}
	public String getSubject()
	{
		String subject = searchResult.getSubject();
    	if (subject==null || subject.trim().length()<2) 
    		return "<no subject>";
    	return subject;
	}

    public boolean getHasAttachment() {
        return searchResult.getHasAttachment();
    }
    
    public int getPriority() {
        return searchResult.getPriority();
    }
    
    protected String parseAddress(String address) {
    	InternetAddress[] iaddress = null;
    	try {
    	 iaddress = InternetAddress.parse(address,false);
    	} catch (Exception e) {
    		return address;
    	}
    	String result = "";
    	for (int i=0;i<iaddress.length;i++) {
    		if (iaddress[i]!=null) {
    			String personal = iaddress[i].getPersonal();
    			if (personal==null)
    				return address;
    			else result += personal;
    		}
    		if (i<iaddress.length-1)
    			result += ", ";
    	}
    	return result;
    	
    } 
    public String getToAddressesT()
	{
		return searchResult.getToAddresses();
		//return DecodingUtil.decodeWord(searchResult.getToAddresses());
	}

	public String getFromAddressT()
	{
		return searchResult.getFromAddress();
		//return DecodingUtil.decodeWord(searchResult.getFromAddress());
	}
	
	public String getToAddresses()
	{
		return parseAddress(searchResult.getToAddresses());
		//return DecodingUtil.decodeWord(searchResult.getToAddresses());
	}

	public String getFromAddress()
	{
		return parseAddress(searchResult.getFromAddress());
		//return DecodingUtil.decodeWord(searchResult.getFromAddress());
	}

	public String getSentDate()
	{
		Date sentDate = searchResult.getSentDate();
		if (sentDate==null)
			return "";
		else {
			DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT,getLocale());
			return formatter.format(sentDate);
		}
	}

	public String getSize()
	{
		return Integer.toString(searchResult.getSize()) + "k";
	}

	public String getScore()
	{
		float score = searchResult.getScore();
		return Float.toString(round(score * 100, 2)) + "%";
	}
	
	private static double round(double val, int places)
	{
		long factor = (long) Math.pow(10, places);
		val = val * factor;
		long tmp = Math.round(val);
		return (double) tmp / factor;
	}

	private static float round(float val, int places)
	{
		return (float) round((double) val, places);
	}
	  
    public static List<SearchResultBean> getSearchResultBeans(List<Search.Result> results) {
		  List<SearchResultBean> searchResultBeans = new LinkedList<SearchResultBean>();
		  for (Search.Result result: results)
			  searchResultBeans.add(new SearchResultBean(result));
		  return searchResultBeans;
	}

	
}
