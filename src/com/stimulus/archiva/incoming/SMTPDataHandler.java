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
