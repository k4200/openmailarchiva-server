
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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author jamie
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */


public class TempFiles {
    
    protected ArrayList<File> fileDeleteList = new ArrayList<File>();
    
    public void markForDeletion(File file) {
	     fileDeleteList.add(file);
	 }
	 
	 protected void finalize() throws Throwable {
	     Iterator it = fileDeleteList.iterator();
	     while (it.hasNext()) {
	         File f = (File)it.next();
	         try { f.delete(); } catch(Exception e) {}
	     }
	     fileDeleteList.clear();
	     super.finalize();
	 }
}
