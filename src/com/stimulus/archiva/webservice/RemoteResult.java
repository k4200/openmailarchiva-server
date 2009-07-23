
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

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;
import com.stimulus.archiva.domain.Config;
import com.stimulus.archiva.domain.EmailID;
import com.stimulus.archiva.domain.Search;
import com.stimulus.archiva.domain.Volume;
import com.stimulus.archiva.domain.fields.EmailField;
import com.stimulus.archiva.domain.fields.EmailFieldValue;
import com.stimulus.archiva.domain.fields.EmailFields;
import com.stimulus.archiva.exception.ChainedException;
import com.stimulus.archiva.exception.MessageSearchException;
import com.stimulus.util.Compare;
import com.stimulus.archiva.webservice.*;


public class RemoteResult extends Search.Result {


  	protected static final Log logger = LogFactory.getLog(RemoteResult.class.getName());
  	SimpleAPIStub.SearchResult searchResult;

  	public RemoteResult(SimpleAPIStub.SearchResult searchResult) {
  		this.searchResult = searchResult;
  	}

  	public EmailID getEmailId() throws MessageSearchException {

  		String uid = null;
  		String volid = null;

  		uid = searchResult.getEmailId();
  		volid = searchResult.getVolumeId();

  		if (uid==null)  {
			  logger.warn("found message with null ID during construction of search results");
			  return null;
		  	}
  		Volume volume = null;
		  try {
			 if (volid==null) // legacy
				 volume = Config.getConfig().getVolumes().getLegacyVolume(uid);
			 else
				 volume = Config.getConfig().getVolumes().getNewVolume(volid);
		  } catch (Exception ce) {
			  logger.error("failed to lookup the volume from index information");
		  }
	  	  return EmailID.getEmailID(volume,uid);

  	}

  	public EmailFieldValue getFieldValue(String key) throws MessageSearchException {
  		String[] fields = searchResult.getFields();
  		String[] values = searchResult.getFieldValues();
  		EmailFields emailFields = Config.getConfig().getEmailFields();
  		for (int i=0;i<fields.length;i++) {
  			if (fields[i].equalsIgnoreCase(key)) {
  				return new EmailFieldValue(emailFields.get(fields[i]),values[i]);
  			}
  		}
  		logger.error("failed to retrieve email field {key='"+key+"'}");
  		return null;
  	}



  }

