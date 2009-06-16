
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

package com.stimulus.archiva.service;
import com.stimulus.archiva.exception.*;
import com.stimulus.archiva.domain.*;
import org.apache.commons.logging.*;

import java.io.Serializable;

public class SearchService implements Serializable {

  /**
	 *
	 */
	private static final long serialVersionUID = 3070046454950483002L;
/* Constants */
  protected static final Log logger = LogFactory.getLog(SearchService.class.getName());
  protected static final Log audit = LogFactory.getLog("com.stimulus.archiva.audit");
  /* protected Fields */

  /* Constructors */

  public static void searchMessage(Search search) throws ArchivaException {
  	logger.debug("searchMessage {querystring='"+search.getSearchQuery()+"'}");
  	audit.info("search emails {"+search.getPrincipal()+",query='"+search.getSearchQuery()+"'}");
    logger.debug("search email {"+search.getPrincipal()+"}");
  	search.searchMessage();
  }

}
