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

package com.stimulus.archiva.incoming;

import java.io.IOException;
import java.io.Reader;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.stimulus.archiva.domain.Config;
import com.stimulus.archiva.exception.ArchiveException;
import com.stimulus.archiva.exception.SMTPEndStreamException;

public class SMTPDataHandler  extends Thread {

	  Reader isr;
	  StoreMessageCallback callback;
	  boolean error = false;
	  ArchiveException cause = null;
	  Socket socket = null;
	  protected static Logger logger = Logger.getLogger(SMTPDataHandler.class);
	  
	  public SMTPDataHandler(String name, Socket socket, Reader isr, StoreMessageCallback callback) {
		this.isr = isr;
		this.callback = callback;
		this.socket = socket;
		setDaemon(true);
		setName(name);
	  }
	  
	  public void run() {
	      
	        SMTPInputStream in = null;
	        try { 	
	        	try { 
	        		int maxSizeInMB = Config.getConfig().getMaxMessageSize();
	        		in = new SMTPInputStream(isr,maxSizeInMB);
	        	} catch (SMTPEndStreamException smtpse) {
	        		return;
	        	} catch (IOException io) {
	        		throw new ArchiveException(io.getMessage(), io, logger, ArchiveException.RecoveryDirective.RETRYLATER); 
	        	}
	        	callback.store(in,socket.getInetAddress().getHostAddress());
	        
	        } catch ( ArchiveException se ) {
	        	error = true;
	        	cause = se;
	        }
	  }
	  
	  boolean getError() { return error; }
	  ArchiveException getCause() { return cause; }
}
