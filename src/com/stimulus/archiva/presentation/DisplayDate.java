/* Copyright (C) 2005-2009 Jamie Angus Band
 * MailArchiva Open Source Edition Copyright (c) 2005-2009 Jamie Angus Band
 * This program is free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation; either version
 * 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see http://www.gnu.org/licenses or write to the Free Software Foundation,Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

package com.stimulus.archiva.presentation;

import com.stimulus.util.*;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import com.stimulus.archiva.domain.fields.EmailFieldValue;
import java.util.*;

import org.apache.commons.logging.*;

public class DisplayDate extends DisplayField implements Serializable {

	protected static Log logger = LogFactory.getLog(DisplayDate.class.getName());
	private static final long serialVersionUID = 5168479608715082055L;
	Locale locale;

	public DisplayDate(EmailFieldValue efv, Locale locale) {
		super(efv);
		this.locale = locale;
	}

	@Override
	public String getDisplay() {
		if (efv.getValue()==null || efv.getValue().length()<1)
			return "";

		String dateStr = efv.getValue().substring(1,efv.getValue().length()-1);
		Date sent;
		try {
			sent = DateUtil.convertStringToDate(dateStr);
		} catch (Exception e) {
			logger.error("failed to convert string to date. "+e.getMessage(),e);
			sent = new Date();
		}
		return DateUtil.getShortDate(sent,locale);
	}




}
