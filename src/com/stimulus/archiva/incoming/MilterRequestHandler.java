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

import java.io.*;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import org.apache.log4j.Logger;
import com.sendmail.jilter.JilterEOMActions;
import com.sendmail.jilter.JilterHandler;
import com.sendmail.jilter.JilterProcessor;
import com.sendmail.jilter.JilterStatus;
import com.stimulus.archiva.domain.Config;
import com.stimulus.archiva.domain.FetchMessageCallback;
import java.util.*;
import java.util.regex.*;
import com.stimulus.archiva.exception.*;
import com.stimulus.util.StopBlockTarget;

public class MilterRequestHandler implements RequestHandler, JilterHandler, StopBlockTarget {

	 protected static Logger logger = Logger.getLogger(MilterRequestHandler.class.getName());
	 protected SocketChannel socket = null;
	 protected FetchMessageCallback callback;
	 protected String host = "";
	 protected JilterStatus status = null;
	 protected ByteArrayOutputStream bos = new ByteArrayOutputStream();
	 private static final int IDLE_TIMEOUT = 300000; // 5 minutes
	
	public void handleRequest( SocketChannel socket, FetchMessageCallback callback ) {
		this.socket = socket;
		this.callback = callback;
		bos = new ByteArrayOutputStream();
		
		InetAddress address = socket.socket().getInetAddress();
		boolean isAllowed = Config.getConfig().getAgent().isAllowed(address);
		if (!isAllowed) {
			logger.debug("attempted milter connection from disallowed address. force disconnect {address='"+address.getHostAddress()+"'}");
			try { socket.close(); } catch (IOException io) {
				logger.error("failed to close milter socket.",io);
			}
			return;
		}
	
		ByteBuffer dataBuffer = ByteBuffer.allocateDirect(4096);
		JilterProcessor processor = new JilterProcessor(this);
        try {
            while (processor.process(socket, (ByteBuffer) dataBuffer.flip())) {
                dataBuffer.compact();
                if (this.socket.read(dataBuffer) == -1) {
                    logger.debug("socket reports EOF, exiting read loop");
                    break;
                }
            }
        }
        catch (IOException e) {
            logger.debug("Unexpected exception, connection will be closed", e);
        } finally {
            logger.debug("closing processor");
            processor.close();
            logger.debug("processor closed");
            try {
                logger.debug("closing socket");
                this.socket.close();
                logger.debug("socket closed");
            } catch (IOException e) {
                logger.debug("Unexpected exception", e);
            }
        }
	}
	
	public JilterStatus abort() {
		logger.debug("abort");
		return JilterStatus.SMFIS_CONTINUE;
	}

	public JilterStatus body(ByteBuffer bodyp) {
		logger.debug("jilter body()");
		long maxMessageSizeMB = Config.getConfig().getArchiver().getMaxMessageSize();
		long maxMessageSizeBytes = maxMessageSizeMB * 1024 * 1024;
		if (bodyp.array().length > maxMessageSizeBytes) {
			logger.warn("milter maximum message size exceeded { size='"+bodyp.array().length+" bytes'}");
			return JilterStatus.SMFIS_REJECT;
		}
		try {
			bos.write("\n".getBytes());
			bos.write(bodyp.array());
		} catch (IOException io) {
			logger.error("jilter failed to write milter body data to byte buffer",io);
		}
		logger.debug("jilter body written");
		return JilterStatus.SMFIS_CONTINUE;
	}

	public JilterStatus close() {
		logger.debug("jilter close()");
		return JilterStatus.SMFIS_CONTINUE;
	}

	public JilterStatus connect(String hostname, InetAddress hostaddr, Properties properties) {

		if (hostaddr!=null) {
			host = hostaddr.toString();
		} else if (host!=null) {
			host = hostname;
		} else {
			host = "localhost";
		}
		logger.debug("jilter connect() {from='"+hostname+"',host='"+host+"'}");
		return JilterStatus.SMFIS_CONTINUE;
	}

	public JilterStatus envfrom(String[] argv, Properties properties) {
		for (int i = 0 ; i < argv.length ; i++) {
			logger.debug("jilter envfrom() {from='"+argv[i]+"'}");
		}
		return JilterStatus.SMFIS_CONTINUE;
	}

	public JilterStatus envrcpt(String[] argv, Properties properties) {
		return JilterStatus.SMFIS_CONTINUE;
	}

	public JilterStatus eoh() {
		logger.debug("jilter eoh()");
		// includeBCC is false if RCPT TO does not contain at least one field in TO, FROM and CC
		// this is a safety check as sometimes, RCPT TO is something differently entirely 
		// and does not contain the actual recipients in the email
		return JilterStatus.SMFIS_CONTINUE;
	}

	public JilterStatus eom(JilterEOMActions eomActions, Properties properties) {
		logger.debug("jilter eom()");
		try {
			bos.close(); // close stream
		} catch (IOException io) {
			logger.error("jilter failed to close io stream during eom",io);
		}
		byte[] messageBytes = bos.toByteArray();
		bos = new ByteArrayOutputStream();
		ByteArrayInputStream bis = new ByteArrayInputStream(messageBytes);
		try {	
			logger.debug("jilter store callback execute");
			Config.getStopBlockFactory().detectBlock("milter server",Thread.currentThread(),this,IDLE_TIMEOUT);
    		callback.store(bis,host);
    		logger.debug("jilter store callback finished");
		} catch (ArchiveException e) {
			logger.error("failed to store the message via milter",e);
			if (e.getRecoveryDirective()==ArchiveException.RecoveryDirective.REJECT) {
				logger.debug("jilter reject");
				return JilterStatus.SMFIS_REJECT;
			} else  if (e.getRecoveryDirective()==ArchiveException.RecoveryDirective.RETRYLATER) {
				logger.debug("jilter temp fail");
				return JilterStatus.SMFIS_TEMPFAIL;
			}  
		} catch (Throwable oome) {
			logger.error("failed to store message:"+oome.getMessage(),oome);
			return JilterStatus.SMFIS_REJECT;
		}  finally {
			Config.getStopBlockFactory().endDetectBlock(Thread.currentThread());
		}
		return JilterStatus.SMFIS_CONTINUE;
	}

	public int getRequiredModifications() {
		logger.debug("jilter requiredmodifications()");
		return SMFIF_NONE;
	}

	public int getSupportedProcesses() {
		logger.debug("jilter getsupportedprocesses()");
		return PROCESS_CONNECT|PROCESS_ENVRCPT|PROCESS_HEADER|PROCESS_BODY;
	}

	public JilterStatus header(String headerf, String headerv) {
		logger.debug("jilter header {name='"+headerf+"',value='"+ headerv+"'}");
		StringBuffer header = new StringBuffer();
		header.append(headerf);
		header.append(": ");
		header.append(headerv);
		header.append("\n");
		try {
			bos.write(header.toString().getBytes());
		} catch (IOException io) {
			logger.error("jilter failed to write header field",io);
		}
		return JilterStatus.SMFIS_CONTINUE;
	}
	
	
	public JilterStatus helo(String helohost, Properties properties) {
		logger.debug("jilter helo() "+helohost);
		return JilterStatus.SMFIS_CONTINUE;
	}
	
	 public void handleBlock(Thread thread) {

    	 try {
             if (socket != null) {
            	 logger.debug("close socket()");
                 socket.close();
             }
         } catch (Exception e) {
             // ignored
         } 
         synchronized (this) {
             if (thread != null) {
            	 logger.debug("interrupt thread()");
            	 thread.interrupt();
             }
         }
    }
    
	
}
