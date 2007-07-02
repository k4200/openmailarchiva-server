
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

package com.stimulus.archiva.service;
import com.stimulus.archiva.search.*;
import com.stimulus.archiva.exception.*;
import com.stimulus.archiva.domain.*;
import org.apache.log4j.*;
//import java.util.List;

public class SearchService {

  /* Constants */
  protected static final Logger logger = Logger.getLogger(SearchService.class.getName());
  protected static MessageSearch messageSearch = new MessageSearch(MessageService.getMessageStore());

  /* protected Fields */

  /* Constructors */

  public static Search searchMessage(Search search) throws ArchivaException {
  	logger.debug("searchMessage {querystring='"+search.getSearchQuery()+"'}");

  	messageSearch.searchMessage(search);
  	return search;

  }

 

 
}
