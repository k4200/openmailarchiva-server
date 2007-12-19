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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.NewsAddress;

import org.apache.log4j.Logger;

import com.stimulus.archiva.domain.fields.EmailField;
import com.stimulus.archiva.domain.fields.EmailFieldValue;
import com.stimulus.util.Compare;
import com.stimulus.util.DateUtil;
import com.stimulus.util.DecodingUtil;
import com.stimulus.util.OrderedHashtable;

public class Email extends AbstractEmail implements Serializable {

	private static final long serialVersionUID = 742813048326535L;
	protected static Logger logger = Logger.getLogger(EmailField.class);
	protected byte[] compressedOriginal;
	protected int size;
	protected int volumeIndex;
	protected boolean selected = false;

	public enum DisplayMode {
		ALL, EMAIL_ONLY, NAME_ONLY
	};

	protected OrderedHashtable applicationFields = new OrderedHashtable();
	protected EmailID emailId;

	public Email(){
		super();
	}
	
	public Email(EmailID emailId, InputStream is) throws MessagingException {
		super(getSession(), is);
		this.emailId = emailId;
		compileApplicationFields();
	}

	public OrderedHashtable getFields() {
		return applicationFields;
	}

	public static String[] getAvailableFields() {
		return new String[] { "to", "bcc", "cc", "from", "size", "flag",
				"priority", "subject", "deliveredto", "attach" };
	}

	public void compileApplicationFields() throws MessagingException {
		applicationFields.clear();

		// if (getFromAddress(DisplayMode.ALL).length()<1)
		// throw new MessagingException("badly formed message");
		putField("from", getFromAddress(DisplayMode.ALL));
		putField("to", getRecipientAddresses(Message.RecipientType.TO,
				DisplayMode.ALL));
		putField("bcc", getRecipientAddresses(Message.RecipientType.BCC,
				DisplayMode.ALL));
		putField("cc", getRecipientAddresses(Message.RecipientType.CC,
				DisplayMode.ALL));
		putField("flag", getFlagsStr());
		putField("priority", Integer.toString(getPriorityID()));
		String subject = getSubject();
		if (subject.length() > 0)
			putField("subject", subject);
		else
			putField("subject", "<no subject>");
		addHeaderToField("deliveredto", "Delivered-To");
		putField("attach", hasAttachment() ? "1" : "0");

		updateOriginalSize();

		try {
			Date sentDate = getSentDate();
			if (sentDate != null)
				putField("sentdate", "d"
						+ DateUtil.convertDatetoString(sentDate));
		} catch (Exception e) {
			logger
					.error("failed to set sent date on message. using current date.");
		}

		try {
			Date archiveDate = getArchiveDate();
			if (archiveDate != null)
				putField("archivedate", "d"
						+ DateUtil.convertDatetoString(archiveDate)); // fix
																		// this
		} catch (Exception e) {
			logger
					.error("failed to set archive date on message. using current date.");
		}

		/*
		 * try { Date receiveDate = getReceivedDate(); if (receiveDate!=null)
		 * putField("receivedate","d"+format.format(receiveDate)); // fix this }
		 * catch (Exception e) { logger.error("failed to set archive date on
		 * message. using current date."); }
		 */
	}

	protected void updateOriginalSize() { // bytes
		double sz = -1;
		try {
			String size[] = getHeader("X-MailArchiva-Message-Size");
			if (size != null && size.length > 0) {
				logger.debug("getOriginalSize() found size {size='" + size[0]
						+ "'}");
				try {
					sz = Double.parseDouble(size[0]);
				} catch (NumberFormatException fe) {
					sz = Integer.parseInt(size[0]);
				} catch (Exception e) {
				}
			}
			this.removeHeader("X-MailArchiva-Message-Size");
		} catch (Exception e) {
		}

		try {
			if (sz == -1)
				sz = getSize();
		} catch (Exception me) {
			sz = 0;
		}
		DecimalFormat df = new DecimalFormat("0.##");
		putField("size", df.format(sz / 1024.0));
	}

	protected void addHeaderToField(String specialHeaderName, String headerName) {
		try {
			String[] headerValue = getHeader(headerName);
			if (headerValue != null && headerValue.length > 0) {
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < headerValue.length; i++) {
					buffer.append(headerValue[i]);
					buffer.append(",");
				}
				putField(specialHeaderName, buffer.toString());
			}
		} catch (Exception e) {
		}
	}

	protected String getAddresses(Address[] recipients, DisplayMode displayMode) {
		if (recipients == null)
			return "";
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < recipients.length; i++) {
			if (recipients[i] instanceof InternetAddress) {
				InternetAddress address = (InternetAddress) recipients[i];
				switch (displayMode) {
				case ALL:
					if (address.getPersonal() != null)
						result.append(address.getPersonal());
					result.append(" <");
					result
							.append(DecodingUtil.decodeWord(address
									.getAddress()));
					result.append(">");
					break;
				case EMAIL_ONLY:
					result
							.append(DecodingUtil.decodeWord(address
									.getAddress()));
					break;
				case NAME_ONLY:
					if (address.getPersonal() != null)
						result.append(DecodingUtil.decodeWord(address
								.getPersonal()));
					else
						result.append(DecodingUtil.decodeWord(address
								.getAddress()));
					break;
				}
			} else if (recipients[i] instanceof NewsAddress) {
				result.append("newsgroup:");
				result.append(((NewsAddress) recipients[i]).getNewsgroup());

			} else
				result
						.append(DecodingUtil.decodeWord(recipients[i]
								.toString()));
			if (i < recipients.length - 1)
				result.append(", ");
		}
		return result.toString().trim();
	}

	protected String getRecipientAddresses(Message.RecipientType recipientType,
			DisplayMode displayMode) {
		Address[] recipients = null;
		try {
			recipients = getRecipients(recipientType);
		} catch (Exception e) {
			return "";
		}
		return getAddresses(recipients, displayMode);
	}

	protected String getDeliveredTo() {
		try {
			String[] deliveredTo = getHeader("Delivered-To");
			if (deliveredTo != null && deliveredTo.length > 0)
				return deliveredTo[0];
		} catch (Exception e) {
			return "";
		}
		return "";
	}

	protected String getFromAddress(DisplayMode displayMode) {
		Address[] from = null;
		try {
			from = getFrom();
		} catch (Exception e) {
			return "";
		}
		return getAddresses(from, displayMode);
	}

	protected int getPriorityID() {
		try {
			String priority[] = getHeader("X-Priority");
			if (priority != null && priority.length > 0) {
				if (priority[0].indexOf("1") != -1
						|| priority[0].indexOf("highest") != -1)
					return 1;
				if (priority[0].indexOf("2") != -1
						|| priority[0].indexOf("high") != -1)
					return 2;
				if (priority[0].indexOf("3") != -1
						|| priority[0].indexOf("normal") != -1)
					return 3;
				if (priority[0].indexOf("4") != -1
						|| priority[0].indexOf("low") != -1)
					return 4;
				if (priority[0].indexOf("5") != -1
						|| priority[0].indexOf("lowest") != -1)
					return 5;
			}
		} catch (javax.mail.MessagingException me) {
			logger.debug("failed to retreive priority from message {"
					+ toString() + "}", me);
		}
		return 3;
	}

	protected String getIHeaders(boolean journal, boolean showHidden)
			throws MessagingException, IOException {
		StringBuffer headerOut = new StringBuffer();
		Enumeration headers = null;
		if (journal && isJournalMessage())
			headers = getUnderlyingMessage().getAllHeaders();
		else
			headers = getAllHeaders();

		while (headers.hasMoreElements()) {
			Header header = (Header) headers.nextElement();

			if (!showHidden
					&& (header.getName().equalsIgnoreCase("bcc") || header
							.getName().equalsIgnoreCase("delivered-to")))
				continue;

			if (!header.getName().equalsIgnoreCase("MIME-Version")
					&& !header.getName().equalsIgnoreCase("X-mailer"))
				headerOut.append(header.getName());
			headerOut.append(": ");
			headerOut.append(header.getValue());
			headerOut.append("<br>");
		}
		return headerOut.toString();
	}

	public String getInternetHeaders(boolean showHidden)
			throws MessagingException, IOException {
		return getIHeaders(true, showHidden);
	}

	public String getJournalReport(boolean showHidden) throws Exception {
		String headers = getIHeaders(false, showHidden);
		String reportFields = processJournalReport(this.getContent());
		return headers + "\n" + reportFields;
	}

	public static String stripGarbage(String s) {
		String good = " abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789<>@,;./=:";
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			if (good.indexOf(s.charAt(i)) >= 0)
				result.append(s.charAt(i));
		}
		return result.toString();
	}

	public InputStream getPlainTextInputStream() {
		InputStream is = null;
		return is;
	}

	public void setEmailID(EmailID emailId) {
		this.emailId = emailId;
	}

	public EmailID getEmailID() {
		return emailId;
	}

	public String getSubject() {
		try {
			if (super.getSubject() == null || super.getSubject().length() < 1)
				return "";
			else
				return super.getSubject();
		} catch (Exception e) {
			return "";
		}
	}

	protected boolean hasAttachment() {
		try {
			boolean result = handlePart(getContent());
			logger.debug("hasAttachment() {result='" + result + "'}");
			return result;
		} catch (Exception e) {
			logger.error("failed to determine if message has attachment {"
					+ toString() + "}", e);

		}
		logger.debug("hasAttachment() {result='false'}");
		return false;
		// logger.debug("message attachment discovery
		// {attach='"+hasAttachment+"',"+toString());
	}

	protected static boolean handlePart(Object part) throws MessagingException,
			IOException {
		if (part instanceof Multipart) {
			Multipart multipart = (Multipart) part;
			for (int i = 0, n = multipart.getCount(); i < n; i++) {
				if (handlePart(multipart.getBodyPart(i)))
					return true;
			}
		} else if (part instanceof MimeMessage) {
			return handlePart(((MimeMessage) part).getContent());
		} else if (part instanceof Part) {
			Part p = (Part) part;
			String fileName = ((Part) part).getFileName();
			if (fileName != null)
				return true;
			String disposition = ((Part) part).getDisposition();
			if (disposition != null) { // When just body
				if (Compare.equalsIgnoreCase(disposition, Part.ATTACHMENT)) {
					logger.debug("hasAttachment() disposition is attachment");
					return true;
				}
			}
			if (p.isMimeType("multipart/*")) {
				return handlePart((Multipart) p.getContent());
			}
		}
		return false;
	}

	public String toString() {
		StringBuffer outStr = new StringBuffer(); // = emailId.toString();

		if (getEmailId() != null) {
			outStr.append(getEmailId().toString());
			outStr.append(",");
		}
		for (Iterator it = getFields().values().iterator(); it.hasNext();) {
			EmailFieldValue efv = (EmailFieldValue) it.next();
			outStr.append(efv);
			outStr.append(",");
		}
		outStr.setLength(outStr.length() - 1);
		return outStr.toString();
	}

	public EmailID getEmailId() {
		return emailId;
	}

	protected String getFlagsStr() {
		List<String> flags = getFlagList();
		StringBuffer allFlags = new StringBuffer();
		for (String flag : flags) {
			allFlags.append(flag);
			allFlags.append(" ");
		}
		return allFlags.toString().trim();
	}

	protected List<String> getFlagList() {
		List<String> flagList = new LinkedList<String>();
		try {
			Flags flags = getFlags();
			Flags.Flag[] sf = flags.getSystemFlags();
			for (int i = 0; i < sf.length; i++) {

				if (sf[i] == Flags.Flag.DELETED)
					flagList.add("deleted");
				else if (sf[i] == Flags.Flag.SEEN)
					flagList.add("seen");
				else if (sf[i] == Flags.Flag.ANSWERED)
					flagList.add("answered");
				else if (sf[i] == Flags.Flag.FLAGGED)
					flagList.add("flagged");
				else if (sf[i] == Flags.Flag.RECENT)
					flagList.add("recent");
				else if (sf[i] == Flags.Flag.DRAFT)
					flagList.add("draft");
			}
		} catch (MessagingException me) {
			logger.debug("failed to retreive flag from message {" + toString()
					+ "}");
		} catch (NullPointerException re) {
		}
		return flagList;
	}

	// Enterprise version

	public boolean isJournalMessage() throws MessagingException {
		return getHeader("x-ms-journal-report") != null;
	}

	protected String processJournalReport(Object part) throws Exception {
		if (part instanceof Multipart) {
			Multipart multipart = (Multipart) part;
			for (int i = 0, n = multipart.getCount(); i < n; i++) {
				String ret = processJournalReport(multipart.getBodyPart(i)
						.getContent());
				if (ret != null)
					return ret;
			}
		} else if (part instanceof String) {
			return part.toString();
		}
		return null;
	}

	protected void putField(String field, String value) {

		if (value == null || value.length() < 1 || field == null)
			return;
		value = value.trim().toLowerCase(Locale.ENGLISH);
		field = field.trim().toLowerCase(Locale.ENGLISH);

		if (EmailField.get(field) == null) // field not supported
			return;

		EmailFieldValue existingValue = (EmailFieldValue) applicationFields
				.get(field);
		if (existingValue == null)
			applicationFields.put(field, new EmailFieldValue(EmailField
					.get(field), value));
		else {
			String oldValue = existingValue.getValue();
			if (oldValue != null && oldValue.indexOf(value) == -1)
				applicationFields.put(field, new EmailFieldValue(EmailField
						.get(field), oldValue + ", " + value));
		}

	}

	public MimeMessage getUnderlyingMessage() throws MessagingException,
			IOException {

		return this;
	}

	public Date getArchiveDate() {
		try {
			String[] header = getHeader("X-MailArchiva-Archive-Date");
			if (header != null && header.length > 0) {
				return DateUtil.convertStringToDate(header[0]);
			} else if (emailId != null && emailId.getUniqueID() != null) { // legacy
				String uniqueid = emailId.getUniqueID();
				try {
					return DateUtil.convertStringToDate(uniqueid);
				} catch (Exception e) {
				}

			}
		} catch (Exception me) {
			logger.debug("getArchiveDate(). unable to parse archive date ", me);
		}
		return new Date();
	}

}
