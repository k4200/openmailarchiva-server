/*
 * Subversion Infos:
 * $URL$
 * $Author$
 * $Date$
 * $Rev$
*/

		
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


package com.stimulus.archiva.search;

import java.io.Serializable;

import org.apache.log4j.Logger;

import com.stimulus.archiva.domain.Search;
import com.stimulus.archiva.exception.MessageSearchException;
import com.stimulus.archiva.security.realm.MailArchivaPrincipal;
import com.stimulus.archiva.store.MessageStore;
public class MessageSearch  implements Serializable
{
	  /**
	 * 
	 */
	private static final long serialVersionUID = 5639605574225342916L;
	protected static Logger logger = Logger.getLogger(MessageSearch.class.getName());
	  protected MessageStore ms = null;
	  
	  public MessageSearch(MessageStore ms) {
	      this.ms = ms;
	  
	  }

	  public void searchMessage(Search search) throws MessageSearchException {
		  if (search==null)
	           throw new MessageSearchException("assertion failure: null search",logger);
	      if (((MailArchivaPrincipal)search.getPrincipal()).getRole()==null || ((MailArchivaPrincipal)search.getPrincipal()).getName()==null)
	          throw new MessageSearchException("assertion failure: null userRole or userName",logger);
	      String queryStr = search.getSearchQuery();
	      logger.debug("search {searchquery='"+queryStr+"'}");
	      search.searchMessage();
	  }

}

