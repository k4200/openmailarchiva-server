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

import java.io.InputStream;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeMessage;

public class AbstractEmail extends MimeMessage {
	protected static Session session;

	static {
		Properties system_properties = System.getProperties();
		session = Session.getDefaultInstance(system_properties, null);
		setSession(session);
	};

	public AbstractEmail() {
		super(session);
	}

	public static void setSession(Session newSession) {
		session = newSession;
	}

	public static Session getSession() {
		return session;
	}

	public AbstractEmail(Session arg0) {
		super(arg0);
	}

	public AbstractEmail(MimeMessage arg0) throws MessagingException {
		super(arg0);
	}

	public AbstractEmail(Session arg0, InputStream arg1)
			throws MessagingException {
		super(arg0, arg1);
	}

	public AbstractEmail(Folder arg0, int arg1) {
		super(arg0, arg1);
	}

	public AbstractEmail(Folder arg0, InputStream arg1, int arg2)
			throws MessagingException {
		super(arg0, arg1, arg2);
	}

	public AbstractEmail(Folder arg0, InternetHeaders arg1, byte[] arg2,
			int arg3) throws MessagingException {
		super(arg0, arg1, arg2, arg3);
	}

}
