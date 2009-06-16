
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
 package com.stimulus.archiva.webservice;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.*;
import com.stimulus.archiva.domain.Search.Result;
import com.stimulus.archiva.domain.fields.*;
import java.util.*;
import com.stimulus.archiva.exception.*;

public class SearchResult {

	protected static final Log logger = LogFactory.getLog(SearchResult.class.getName());
	protected Result result;

	protected SearchResult(Result result) {
		this.result = result;
	}

	public String getEmailId() throws AxisFault {
		try {
			return result.getEmailId().getUniqueID();
		} catch (Exception e) {
			throw new AxisFault("failed to retrieve email id:"+e.getMessage());
		}
	}
	public String getVolumeId() throws AxisFault {
		try {
			return result.getEmailId().getVolume().getID();
		} catch (Exception e) {
			throw new AxisFault("failed to retrieve volume id:"+e.getMessage());
		}
	}

	public String[] getFields() throws AxisFault {
		ArrayList<String> emailFields = new ArrayList<String>();
		for (EmailField ef : EmailFields.emailFields.values()) {
			try {
				if (ef.isStored() || ef.getName().equalsIgnoreCase("score")) {
					emailFields.add(ef.getName());
				}
			} catch (Exception e) {
				logger.debug("failed to retrieve field:"+e.getMessage(),e);
			};
		}
		return emailFields.toArray(new String[]{});
	}

	public String[] getFieldValues() throws AxisFault {
		ArrayList<String> emailFields = new ArrayList<String>();
		for (EmailField ef : EmailFields.emailFields.values()) {
			try {
				if (ef.isStored() || ef.getName().equalsIgnoreCase("score")) {
					emailFields.add(result.getFieldValue(ef.getName()).getValue());
				}
			} catch (Exception e) {
				logger.debug("failed to retrieve field value:"+e.getMessage(),e);
			};
		}
		return emailFields.toArray(new String[]{});
	}



}