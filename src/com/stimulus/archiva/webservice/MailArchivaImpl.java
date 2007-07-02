
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

package com.stimulus.archiva.webservice;
import com.stimulus.archiva.domain.*;
import com.stimulus.archiva.service.*;
import java.rmi.RemoteException;
import org.apache.log4j.Logger;
import com.stimulus.archiva.exception.*;
import org.apache.axis.MessageContext;
import org.apache.axis.Constants;


 public class MailArchivaImpl implements MailArchiva {
 	 protected static final Logger logger = Logger.getLogger(MailArchivaImpl.class.getName());

 	public MailArchivaImpl() {
 	   MessageContext context = MessageContext.getCurrentContext();
 	   context.setTimeout(Integer.MAX_VALUE);
 	}
 	/*
 	public Configuration getConfiguration() throws RemoteException {
 		Config config = ConfigurationService.getConfig();
 		Configuration configuration = new Configuration(config.getArchiveInbound(),config.getArchiveOutbound(),config.getArchiveInternal(),(ArchiveRule[])config.getArchiveRules().toArray());
 		return configuration;
 	}*/
 	/*
 	public boolean getArchiveInbound() throws RemoteException {
 		return ConfigurationService.getConfig().getArchiveRules().getArchiveInbound();
	}

	public boolean getArchiveOutbound() throws RemoteException {
		return ConfigurationService.getConfig().getArchiveRules().getArchiveOutbound();
	}

	public boolean getArchiveInternal() throws RemoteException {
		return ConfigurationService.getConfig().getArchiveRules().getArchiveInternal();
	}

	private static final String[] dests =
    {
        "Athens",
        "Crete"
    };

	public String[] getAction()  throws RemoteException {
		return dests;
	}

	public String[] getField()  throws RemoteException {
		return dests;
	}

	public String[] getRegEx() throws RemoteException {
		return dests;
	}*/

 	public String getServerVersion() { return Config.getApplicationVersion(); }

 	public void storeMessage(byte[] compressedMessage) throws RemoteException
 	{
 		 MessageContext context = MessageContext.getCurrentContext();
 		String remoteIP = context.getStrProp(Constants.MC_REMOTE_ADDR);
 		String soapURL = context.getSOAPActionURI();
 		String userName = context.getUsername();
 		if (userName==null) { userName="anon";}
 		
 		logger.debug("storeMessage (via web service)");

 		//BufferedReader in  = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(compressedMessage)));
 		 //figure out how to display binary data with log4j
 		/*int c;
 		int len = 0;
 		try {
	        while ((c = in.read()) != -1)
	        {
	           len++;
	        	System.out.write(c);
	        }
	        in.close();
 		} catch (IOException io) {System.out.println("error");}
 		*/

 		logger.info("message received for archival (via web service) {username='"+userName+"', client ip='"+remoteIP+"',url='"+soapURL+"',message data length='"+compressedMessage.length+"'}");
 	  	try {
 	  	    MessageService.storeMessage(compressedMessage,userName,remoteIP);
 	  	} catch (ArchivaException me) {
 	  	    logger.debug("failed to store message. Cause:",me);
 	  	    throw new RemoteException(me.getMessage());
 	  	}
 	}
 }


 /*

	Email message = null;
	try {

		message = new Email(new ByteArrayInputStream(compressedMessage),false);
		logger.info("message decoded successfully, where "+message.getFriendlyID());
	} catch (MessagingException me)
	{
		logger.error("retrieved message appears to be malformed",me);
		throw new RemoteException(me.toString());
	}*/