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

package com.stimulus.struts.httpmap;

import com.stimulus.struts.httpmap.BaseHttpMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.io.Serializable;
import java.util.Enumeration;

/**
 * Map to wrap session scope attributes.
 * <p/>
 * Date: Mar 11, 2004 10:35:42 PM
 *
 * @author Clinton Begin
 */
public class SessionMap extends BaseHttpMap implements Serializable {

  private HttpSession session;

  public SessionMap(HttpServletRequest request) {
    this.session = request.getSession();
  }

  protected Enumeration getNames() {
    return session.getAttributeNames();
  }

  protected Object getValue(Object key) {
    return session.getAttribute(String.valueOf(key));
  }

  protected void putValue(Object key, Object value) {
    session.setAttribute(String.valueOf(key), value);
  }

  protected void removeValue(Object key) {
    session.removeAttribute(String.valueOf(key));
  }

}
