
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

import org.apache.log4j.Logger;

import com.stimulus.archiva.domain.Email;
import com.stimulus.archiva.domain.Search;
import com.stimulus.archiva.service.MessageService;

//this function needed for backwards compatibility with older indexes which did not store the various email
// attributes. 

public class LegacyFetchMessage extends Search.FetchMessage {
	
	protected static final Logger logger = Logger.getLogger(LegacyFetchMessage.class.getName());
     public void fetchMessage(Search.Result result) {
         try {
       	
           Email email = MessageService.getMessageByID(result.getEmailId(),true);
	       result.setSubject(email.getSubject());
	       result.setFromAddress(email.getFromAddress(Email.DisplayMode.NAME_ONLY));
	       result.setToAddresses(email.getToAddresses(Email.DisplayMode.NAME_ONLY));
	       result.setSize(email.getSize());
	       result.setSentDate(email.getSentDate());
           result.setHasAttachment(email.hasAttachment());
           result.setPriority(email.getPriorityID());
            
         } catch (Exception e) {
   	        logger.error("failed to retrieve message during construction of search results. Encryption password correct?  Cause:" + e.getMessage());
   	        logger.debug("failed to retrieve message during construction of search results. Encryption password correct? Cause:", e);
   	    
         }
     }
 }