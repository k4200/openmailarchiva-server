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

import java.io.Serializable;
import com.stimulus.archiva.domain.fields.EmailFieldValue;

public class DisplayPriority extends DisplayField implements Serializable {


	/**
	 *
	 */
	private static final long serialVersionUID = 4615827437297129388L;

	public DisplayPriority(EmailFieldValue efv) {
		super(efv);
	}

	@Override
	public String getDisplay() {
	  switch(Integer.valueOf(getValue())) {
          case 1: return "highest";
          case 2: return "high";
          case 3: return "normal";
          case 4: return "low";
          case 5: return "lowest";
          default: return "normal";
      }
	}


}
