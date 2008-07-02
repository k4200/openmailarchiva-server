
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

import javax.mail.*;
import java.util.concurrent.locks.*;
import com.stimulus.util.*;

import javax.mail.internet.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.*;
import java.util.*;

import org.apache.log4j.Logger;
import java.io.*;

public class EmailID implements Serializable {
	private static volatile SynchronizedDateFormat format = new SynchronizedDateFormat("yyyyMMddHHmmssSS");
	private static String hexits = "0123456789abcdef";
	private static final long serialVersionUID = 3048326535L;
	protected static Logger logger = Logger.getLogger(EmailID.class.getName());
    protected String uniqueId = null;
    protected Volume volume = null;
    public EmailID() {}

    protected EmailID(Volume volume, Email email) {
        uniqueId = generateUniqueID(email);
        this.volume = volume;
    }
    
    protected EmailID(Volume volume, String uniqueId) {
        this.uniqueId = uniqueId;
        this.volume = volume;
    }
    

    public static EmailID getEmailID(Volume volume,String uniqueID) {
    	return new EmailID(volume,uniqueID);
    }
    
    public static EmailID getEmailID(Volume volume, Email email) {
    	return new EmailID(volume,email);
    }
    
    protected EmailID(String uniqueId) {
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
    

    public static synchronized String generateUniqueID(Email email)
    {
    	String uuid =  UUID.randomUUID().toString().replaceAll("-","");
    	return uuid;
    }
    private static String toHex(byte[] block) {
		StringBuffer buf = new StringBuffer();

		for (int i = 0; i < block.length; ++i) {
			buf.append(hexits.charAt((block[i] >>> 4) & 0xf));
			buf.append(hexits.charAt(block[i] & 0xf));
		}
		return buf + "";
	}
    
    

    @Override
	public String toString() {
        return "uniqueId='"+uniqueId+"', " + volume;
    }
    
    public static class NullOutputStream extends OutputStream
	{
		@Override
		public void write (byte [] b) throws IOException {}
		
		@Override
		public void write (byte [] b, int off, int len) throws IOException {}
			
		@Override
		public void write (int b) throws IOException {}
			
	
		@Override
		public void flush () throws IOException {}
			
		@Override
		public void close () throws IOException {}
		
	}
}
