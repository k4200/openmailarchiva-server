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

import java.io.Serializable;
import java.util.*;

/**
 * <p/>
 * Date: Mar 11, 2004 10:39:51 PM
 *
 * @author Clinton Begin
 */
public abstract class BaseHttpMap implements Map,Serializable {

  public int size() {
    return keySet().size();
  }

  public boolean isEmpty() {
    return keySet().size() == 0;
  }

  public boolean containsKey(Object key) {
    return keySet().contains(key);
  }

  public boolean containsValue(Object value) {
    return values().contains(value);
  }

  public Object get(Object key) {
    return getValue(key);
  }

  public Object put(Object key, Object value) {
    Object old = getValue(key);
    putValue(key, value);
    return old;
  }

  public Object remove(Object key) {
    Object old = getValue(key);
    removeValue(key);
    return old;
  }

  public void putAll(Map map) {
    Iterator i = map.keySet().iterator();
    while (i.hasNext()) {
      Object key = i.next();
      putValue(key, map.get(key));
    }
  }

  public void clear() {
    Iterator i = keySet().iterator();
    while (i.hasNext()) {
      removeValue(i.next());
    }
  }

  public Set keySet() {
    Set keySet = new HashSet();
    Enumeration en = getNames();
    while (en.hasMoreElements()) {
      keySet.add(en.nextElement());
    }
    return keySet;
  }

  public Collection values() {
    List list = new ArrayList();
    Enumeration en = getNames();
    while (en.hasMoreElements()) {
      list.add(getValue(en.nextElement()));
    }
    return list;
  }

  public Set entrySet() {
    return new HashSet();
  }


  protected abstract Enumeration getNames();

  protected abstract Object getValue(Object key);

  protected abstract void putValue(Object key, Object value);

  protected abstract void removeValue(Object key);

}
