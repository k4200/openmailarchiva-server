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

package com.stimulus.archiva.service;
import com.stimulus.archiva.persistence.*;
import com.stimulus.archiva.exception.*;
import com.stimulus.archiva.domain.*;

import org.apache.lucene.document.*;
import org.apache.log4j.*;
import java.util.*;
//import java.util.List;

public class SearchService {

  /* Constants */
  protected static final Logger logger = Logger.getLogger(SearchService.class.getName());
  protected static MessageSearch messageSearch = new MessageSearch(MessageService.getMessageStore());

  /* protected Fields */

  /* Constructors */

  public static Search searchMessage(Search search) throws ArchivaException {
  	logger.debug("searchMessage {querystring='"+search.getSearchQuery()+"'}");

  	search = messageSearch.searchMessage(search);
  	search.setFetchMessage(new FetchMessage());
  	return search;

  }

  public static class FetchMessage extends Search.FetchMessage {
      public void fetchMessage(Search.Result result) {
          try {
            Email email = MessageService.getMessageByID(result.getEmailId(),true);
	        result.setSubject(email.getSubject());
	        result.setFromAddress(email.getFromAddress());
	        result.setToAddresses(email.getToAddresses());
	        result.setSize(email.getSize());
	        result.setSentDate(email.getSentDate());
          } catch (Exception e) {
    	        logger.error("failed to retrieve message during construction of search results. Encryption password correct?  Cause:" + e.getMessage());
    	        logger.debug("failed to retrieve message during construction of search results. Encryption password correct? Cause:", e);
    	    
          }
      }
  }

  // converts dates to lucene string equivalent
  public static String dateToString(Date date) {
  	return DateField.dateToString(date);
  }
}
