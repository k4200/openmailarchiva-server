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
