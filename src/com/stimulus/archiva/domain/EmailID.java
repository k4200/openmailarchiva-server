
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
package com.stimulus.archiva.domain;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

public class EmailID {

	private static final long serialVersionUID = 3048326535L;
	protected static Logger logger = Logger.getLogger(EmailID.class.getName());
    protected String uniqueId = null;
    protected Volume volume = null;

    public EmailID() {}

    public EmailID(Volume volume) {
        uniqueId = generateUniqueID();
        this.volume = volume;
    }
    public EmailID(String uniqueId, Volume volume) {
        this.uniqueId = uniqueId;
        this.volume = volume;
    }

    public EmailID(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getUniqueID() {
    	return uniqueId;
    }

    public void setUniqueID(String uniqueId) {
    	this.uniqueId = uniqueId;
    }


    public Volume getVolume() {
    	return volume;
    }


    public void setVolume(Volume volume) {
    	this.volume = volume;
    }


    public static synchronized String generateUniqueID()
    {
    	logger.debug("generateUniqueID()");
	    try
	  	{
	    		Date sentDate = new Date();
	  	  	SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSS");
	  	  	return format.format(sentDate);
	  	} catch (Exception e)
	  	{
	  		logger.error("failed to generate a uniqueid for a message");
	  		return null;
	  	}
    }

    public String toString() {
        return "uniqueId='"+uniqueId+"', " + volume;
    }
}
