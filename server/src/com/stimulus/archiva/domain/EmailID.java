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

package com.stimulus.archiva.domain;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

public class EmailID {

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
	  		logger.error("failed to generate a uniqud id for a message");
	  		return null;
	  	}
    }

    public String toString() {
        return "uniqueId='"+uniqueId+"', " + volume;
    }
}
