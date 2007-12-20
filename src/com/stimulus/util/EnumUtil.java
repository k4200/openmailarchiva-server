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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class EnumUtil implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5691455601372019921L;

	public static List<String> enumToList(Enum[] enums, String prefix) {
	    	List<String> list = new LinkedList<String>();
	    	for (int i=0;i<enums.length;i++)
	    		list.add(prefix+enums[i].toString().toLowerCase(Locale.ENGLISH));
	    	return list;
	}
	
	public static List<String> enumToList(Enum[] enums) {
		return enumToList(enums,"");
	}
	
	public static List<Enum> enumToListEnums(Enum[] enums) {
    	List<Enum> list = new LinkedList<Enum>();
    	for (int i=0;i<enums.length;i++)
    		list.add(enums[i]);
    	return list;
}
}
