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
/*
 * Created on Feb 9, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */

/**
 * @author david
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */

import java.io.Serializable;
import java.util.Date;
import javax.mail.internet.MailDateFormat;

/**
 * A thread safe wrapper for the <code>javax.mail.internet.MailDateFormat</code> class.
 *
 */
public class RFC822DateFormat extends SynchronizedDateFormat implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = -7819456681541726149L;
	/**
     * A static instance of the RFC822DateFormat, used by toString
     */
    private static RFC822DateFormat instance; 

    static {
        instance = new RFC822DateFormat();
    }

    /**
     * This static method allows us to format RFC822 dates without
     * explicitly instantiating an RFC822DateFormat object.
     *
     * @return java.lang.String
     * @param d Date
     *
     * @deprecated This method is not necessary and is preserved for API
     *             backwards compatibility.  Users of this class should
     *             instantiate an instance and use it as they would any
     *             other DateFormat object.
     */
    public static String toString(Date d) {
        return instance.format(d);
    }

    /**
     * Constructor for RFC822DateFormat
     */
    public RFC822DateFormat() {
        super(new MailDateFormat());
    }
}