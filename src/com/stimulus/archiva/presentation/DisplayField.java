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

import com.stimulus.archiva.domain.fields.EmailField;
import com.stimulus.archiva.domain.fields.EmailFieldValue;
import com.stimulus.util.EscapeUtil;

import java.io.Serializable;
import java.util.Locale;

public class DisplayField implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -5144163717134902765L;
	EmailFieldValue efv;

	public DisplayField(EmailFieldValue efv) {
		this.efv = efv;
	}

	public String getDisplay() {
		if (efv.getValue()!=null)
			return EscapeUtil.forHTML(efv.getValue());
			//return stripGarbage(efv.getValue());
		else return "";
	}

	public String getValue() {
		if (efv.getValue()!=null)
			return EscapeUtil.forHTML(efv.getValue());
		else return "";
	}

	public EmailField getField() {
		return efv.getField();
	}

	public String getTip() {
		return getDisplay();
	}


	public static DisplayField getDisplayField(EmailFieldValue efv, Locale locale, boolean raw) {
		if (efv.getField().getName().equals("sentdate"))
		 	return new DisplayDate(efv,locale);
		else if (efv.getField().getName().equals("receiveddate"))
			return new DisplayDate(efv,locale);
		else if (efv.getField().getName().equals("archivedate"))
			return new DisplayDate(efv,locale);
		else if (efv.getField().getName().equals("receiveddate"))
			return new DisplayDate(efv,locale);
		else if (efv.getField().getName().equals("size"))
			 return new DisplaySize(efv);
		 else if (efv.getField().getName().equals("to") ||
				  efv.getField().getName().equals("from") ||
				  efv.getField().getName().equals("bcc") ||
				  efv.getField().getName().equals("cc"))
			 return new DisplayAddress(efv,raw);
		 else if (efv.getField().getName().equals("from"))
			 return new DisplayAddress(efv,raw);
		 else if (efv.getField().getName().equals("priority"))
			 return new DisplayPriority(efv);
		 else
			 return new DisplayField(efv);
	}



}