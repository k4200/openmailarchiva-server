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

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


/**
 * This class is designed to be a synchronized wrapper for a
 * <code>java.text.DateFormat</code> subclass.  In general,
 * these subclasses (most notably the <code>java.text.SimpleDateFormat</code>
 * classes are not thread safe, so we need to synchronize on the
 * internal DateFormat for all delegated calls.
 *
 */
public class SynchronizedDateFormat implements SimplifiedDateFormat, Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = -731868761219972944L;
	private final DateFormat internalDateFormat;

    /**
     * Public constructor that mimics that of SimpleDateFormat.  See
     * java.text.SimpleDateFormat for more details.
     *
     * @param pattern the pattern that defines this DateFormat
     * @param locale the locale
     */
    public SynchronizedDateFormat(String pattern, Locale locale) {
        internalDateFormat = new SimpleDateFormat(pattern, locale);
    }

    public SynchronizedDateFormat(String pattern) {
        internalDateFormat = new SimpleDateFormat(pattern);
    }
    /**
     * <p>Wrapper method to allow child classes to synchronize a preexisting
     * DateFormat.</p>
     *
     * <p>TODO: Investigate replacing this with a factory method.</p>
     *
     * @param theDateFormat the DateFormat to synchronize
     */
    protected SynchronizedDateFormat(DateFormat theDateFormat) {
        internalDateFormat = theDateFormat;
    }

    /**
     * SimpleDateFormat will handle most of this for us, but we
     * want to ensure thread safety, so we wrap the call in a
     * synchronized block.
     *
     * @return java.lang.String
     * @param d Date
     */
    public String format(Date d) {
        synchronized (internalDateFormat) {
           return internalDateFormat.format(d);
        }
    }

    /**
     * Parses text from the beginning of the given string to produce a date.
     * The method may not use the entire text of the given string.
     * <p>
     * This method is designed to be thread safe, so we wrap our delegated
     * parse method in an appropriate synchronized block.
     *
     * @param source A <code>String</code> whose beginning should be parsed.
     * @return A <code>Date</code> parsed from the string.
     * @throws ParseException if the beginning of the specified string
     *         cannot be parsed.
     */
    public Date parse(String source) throws ParseException {
        synchronized (internalDateFormat) {
            return internalDateFormat.parse(source);
        }
    }

    /**
     * Sets the time zone of this SynchronizedDateFormat object.
     * @param zone the given new time zone.
     */
    public void setTimeZone(TimeZone zone) {
        synchronized(internalDateFormat) {
            internalDateFormat.setTimeZone(zone);
        }
    }

    /**
     * Gets the time zone.
     * @return the time zone associated with this SynchronizedDateFormat.
     */
    public TimeZone getTimeZone() {
        synchronized(internalDateFormat) {
            return internalDateFormat.getTimeZone();
        }
    }

    /**
     * Specify whether or not date/time parsing is to be lenient.  With
     * lenient parsing, the parser may use heuristics to interpret inputs that
     * do not precisely match this object's format.  With strict parsing,
     * inputs must match this object's format.
     * @param lenient when true, parsing is lenient
     * @see java.util.Calendar#setLenient
     */
    public void setLenient(boolean lenient)
    {
        synchronized(internalDateFormat) {
            internalDateFormat.setLenient(lenient);
        }
    }

    /**
     * Tell whether date/time parsing is to be lenient.
     * @return whether this SynchronizedDateFormat is lenient.
     */
    public boolean isLenient()
    {
        synchronized(internalDateFormat) {
            return internalDateFormat.isLenient();
        }
    }

    /**
     * Overrides hashCode
     */
    public int hashCode() {
        synchronized(internalDateFormat) {
            return internalDateFormat.hashCode();
        }
    }

    /**
     * Overrides equals
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        synchronized(internalDateFormat) {
            return internalDateFormat.equals(obj);
        }
    }
    
    
 

}
