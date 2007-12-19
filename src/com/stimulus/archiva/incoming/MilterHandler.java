package com.stimulus.archiva.incoming;

import java.io.ByteArrayInputStream;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message;

import org.apache.log4j.Logger;

import com.sendmail.jilter.JilterEOMActions;
import com.sendmail.jilter.JilterHandler;
import com.sendmail.jilter.JilterStatus;
import com.stimulus.archiva.domain.Config;
import com.stimulus.archiva.exception.ArchiveException;

public class MilterHandler implements JilterHandler {
	
    private static Logger logger = Logger.getLogger(MilterHandler.class);

    Message eMail = null;
    ArrayList<String> rcpts = new ArrayList<String>();
    StoreMessageCallback callback;
    InetAddress hostaddr;
    StringBuffer strEmail = new StringBuffer();
    
    
    public MilterHandler(StoreMessageCallback callback) {
    	this.callback = callback;
    }
    
    public void setCallback(StoreMessageCallback callback) {
    	this.callback = callback;
    }

	public JilterStatus abort() {
		logger.debug("abort");
		return JilterStatus.SMFIS_CONTINUE;
	}

	public JilterStatus body(ByteBuffer bodyp) {
		if (bodyp.array().length > Config.getConfig().getMaxMessageSize() * 1024 * 1024) {
			logger.warn("milter maximum message size exceeded { size='"+bodyp.array().length+" bytes'}");
			return JilterStatus.SMFIS_REJECT;
		}
		
		logger.debug("body received");
		strEmail.append("\n");
		String strBody = new String(bodyp.array());
		strEmail.append(strBody);
		return JilterStatus.SMFIS_CONTINUE;
	}

	public JilterStatus close() {
		logger.debug("closing");
		return JilterStatus.SMFIS_CONTINUE;
	}

	public JilterStatus connect(String hostname, InetAddress hostaddr, Properties properties) {
		logger.debug("connected {from='"+hostname+"',hostaddr='"+hostaddr.toString()+"'}");
		this.hostaddr = hostaddr;
		return JilterStatus.SMFIS_CONTINUE;
	}

	public JilterStatus envfrom(String[] argv, Properties properties) {
		for (int i = 0 ; i < argv.length ; i++) {
			logger.debug("email {from='"+argv[i]+"'}");
		}
		return JilterStatus.SMFIS_CONTINUE;
	}

	public JilterStatus envrcpt(String[] argv, Properties properties) {
		for (int i = 0 ; i < argv.length ; i++)	{
			String strRecipient = argv[i];
			boolean orcptFlag = strRecipient.toLowerCase(Locale.ENGLISH).trim().contains("orcpt=");
			if (!orcptFlag)
			{
				logger.debug("email {to='"+strRecipient+"'}");
				rcpts.add(strRecipient.toLowerCase(Locale.ENGLISH).trim().replaceAll("<","").replaceAll(">",""));
			}
		}
 		return JilterStatus.SMFIS_CONTINUE;
	}

	public JilterStatus eoh() {
		logger.debug("jilter endofheader");
		for (int i = 0; i < rcpts.size(); i++) 	{
			if (i == 0)
				strEmail.append("bcc: ");
			else
				strEmail.append(",");
			strEmail.append(rcpts.get(i));
		}
		if (rcpts.size() > 0) 		{
			strEmail.append("\n");
		}
		return JilterStatus.SMFIS_CONTINUE;
	}

	public JilterStatus eom(JilterEOMActions eomActions, Properties properties) {
		logger.debug("received message:"+strEmail.toString());
		
		try {	
			callback.store(new ByteArrayInputStream(strEmail.toString().getBytes()),hostaddr.getHostAddress());
		} catch (ArchiveException e) {
			if (e.getRecoveryDirective()==ArchiveException.RecoveryDirective.REJECT)
				return JilterStatus.SMFIS_REJECT;
			else  if (e.getRecoveryDirective()==ArchiveException.RecoveryDirective.RETRYLATER);
				return JilterStatus.SMFIS_TEMPFAIL;
		} finally {
			strEmail = new StringBuffer();
			rcpts.clear();
		}	
		return JilterStatus.SMFIS_CONTINUE;
	}

	public int getRequiredModifications() {
		logger.debug("RequiredModifications");
		return SMFIF_NONE;
	}

	public int getSupportedProcesses() {
		logger.debug("getSupportedProcesses()");
		return PROCESS_CONNECT|PROCESS_ENVRCPT|PROCESS_HEADER|PROCESS_BODY;
	}

	public JilterStatus header(String headerf, String headerv) {
		logger.debug("header {name='"+headerf+"',value='"+ headerv+"'}");
		strEmail.append(headerf);
		strEmail.append(": ");
		strEmail.append(headerv);
		strEmail.append("\n");
		Pattern pattern = Pattern.compile("^cc|^to|^bcc");
		Matcher m = pattern.matcher(headerf.toLowerCase(Locale.ENGLISH).trim());
		if (m.matches()) {
			logger.debug("to/bcc/cc header found");
			String [] addresses = headerv.split(",");
			for (int i = 0; i < addresses.length; i++) {
				rcpts.remove(addresses[i].toLowerCase(Locale.ENGLISH).trim());
				pattern = Pattern.compile(".*<([-.+_\\d\\w]*@[-.+_\\d\\w]*)>");
				m = pattern.matcher(addresses[i].toLowerCase(Locale.ENGLISH).trim());
				if (m.matches()) {
					String mailAddress = m.group(1);
					rcpts.remove(mailAddress);
				} else {
					pattern = Pattern.compile("([-.+_\\d\\w]*@[-.+_\\d\\w]*)");
					m = pattern.matcher(addresses[i].toLowerCase(Locale.ENGLISH).trim());
					if (m.matches()) {
						String mailAddress = m.group(1);
						rcpts.remove(mailAddress);
					}
				}
				
			}
		}
		return JilterStatus.SMFIS_CONTINUE;
	}

	public JilterStatus helo(String helohost, Properties properties) {
		logger.debug("ehlo/helo: "+helohost);
		return JilterStatus.SMFIS_CONTINUE;
	}

}
