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

package com.stimulus.util;

import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Enumeration;


/**
 * This interface defines a container for mail headers. Each header must use
 * MIME format: <pre>name: value</pre>.
 *
 */
public class MailHeaders extends InternetHeaders implements Serializable, Cloneable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -1152547998642653316L;

	/**
     * No argument constructor
     *
     * @throws MessagingException if the super class cannot be properly instantiated
     */
    public MailHeaders() throws MessagingException {
        super();
    }

    /**
     * Constructor that takes an InputStream containing the contents
     * of the set of mail headers.
     *
     * @param in the InputStream containing the header data
     *
     * @throws MessagingException if the super class cannot be properly instantiated
     *                            based on the stream
     */
    public MailHeaders(InputStream in) throws MessagingException {
        super();
        load(in);
    }

    /**
     * Write the headers to an output stream
     *
     * @param out the OutputStream to which to write the headers
     */
    public void writeTo(OutputStream out) {
        PrintStream pout;
        if (out instanceof PrintStream) {
            pout = (PrintStream)out;
        } else {
            pout = new PrintStream(out);
        }
        for (Enumeration e = super.getAllHeaderLines(); e.hasMoreElements(); ) {
            pout.print((String) e.nextElement());
            pout.print("\r\n");
        }
        // Print trailing CRLF
        pout.print("\r\n");
    }

    /**
     * Generate a representation of the headers as a series of bytes.
     *
     * @return the byte array containing the headers
     */
    public byte[] toByteArray() {
        ByteArrayOutputStream headersBytes = new ByteArrayOutputStream();
        writeTo(headersBytes);
        return headersBytes.toByteArray();
    }

    /**
     * Check if a particular header is present.
     *
     * @return true if the header is present, false otherwise
     */
    public boolean isSet(String name) {
        String[] value = super.getHeader(name);
        return (value != null && value.length != 0);
    }

    /**
     * If the new header is a Return-Path we get sure that we add it to the top
     * Javamail, at least until 1.4.0 does the wrong thing if it loaded a stream with 
     * a return-path in the middle.
     *
     * @see javax.mail.internet.InternetHeaders#addHeader(java.lang.String, java.lang.String)
     */
    public void addHeader(String arg0, String arg1) {
        /*if (RFC2822Headers.RETURN_PATH.equalsIgnoreCase(arg0)) {
            headers.add(0, new InternetHeaders.InternetHeader(arg0, arg1));
        } else {*/
            super.addHeader(arg0, arg1);
        //}
    }

    /**
     * If the new header is a Return-Path we get sure that we add it to the top
     * Javamail, at least until 1.4.0 does the wrong thing if it loaded a stream with 
     * a return-path in the middle.
     *
     * @see javax.mail.internet.InternetHeaders#setHeader(java.lang.String, java.lang.String)
     */
    public void setHeader(String arg0, String arg1) {
        if (RFC2822Headers.RETURN_PATH.equalsIgnoreCase(arg0)) {
            super.removeHeader(arg0);
        }
        super.setHeader(arg0, arg1);
    }

    protected Object clone() throws CloneNotSupportedException {
        // TODO Auto-generated method stub
        return super.clone();
    }

    /**
     * Check if all REQUIRED headers fields as specified in RFC 822
     * are present.
     *
     * @return true if the headers are present, false otherwise
     */
    public boolean isValid() {
        return (isSet(RFC2822Headers.DATE) && isSet(RFC2822Headers.TO) && isSet(RFC2822Headers.FROM));
    }
}
