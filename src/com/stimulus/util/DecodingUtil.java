
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

import java.io.UnsupportedEncodingException;

import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParseException;

public class DecodingUtil
{
	public static String decodeWord(String strWord)
	{
		if (strWord==null)
			return "";
		String strMimeAddresses = strWord;
		strMimeAddresses = strMimeAddresses.replaceAll("^=_", "=?");
		strMimeAddresses = strMimeAddresses.replaceFirst("_[Qq]_", "?Q?");
		strMimeAddresses = strMimeAddresses.replaceAll("_=$", "?=");
		try
		{

			strWord = MimeUtility.decodeWord(strMimeAddresses);
		}
		catch (UnsupportedEncodingException e)
		{
		}
		catch (ParseException e)
		{
		}
		return strWord;
	}

}
