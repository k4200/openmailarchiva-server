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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import java.io.Serializable;
import java.util.Enumeration;

/**
 * Map to wrap cookie names and values (READ ONLY).
 * <p/>
 * Date: Mar 11, 2004 11:31:35 PM
 *
 * @author Clinton Begin
 */
public class CookieMap extends BaseHttpMap implements Serializable {

  private Cookie[] cookies;

  public CookieMap(HttpServletRequest request) {
    cookies = request.getCookies();
  }

  protected Enumeration getNames() {
    return new CookieEnumerator(cookies);
  }

  protected Object getValue(Object key) {
    for (int i = 0; i < cookies.length; i++) {
      if (key.equals(cookies[i].getName())) {
        return cookies[i].getValue();
      }
    }
    return null;
  }

  protected void putValue(Object key, Object value) {
    throw new UnsupportedOperationException();
  }

  protected void removeValue(Object key) {
    throw new UnsupportedOperationException();
  }

  /**
   * Cookie Enumerator Class
   */
  private class CookieEnumerator implements Enumeration {

    private int i = 0;

    private Cookie[] cookieArray;

    public CookieEnumerator(Cookie[] cookies) {
      this.cookieArray = cookies;
    }

    public synchronized boolean hasMoreElements() {
      return cookieArray.length > i;
    }

    public synchronized Object nextElement() {
      Object element = cookieArray[i];
      i++;
      return element;
    }

  }

}
