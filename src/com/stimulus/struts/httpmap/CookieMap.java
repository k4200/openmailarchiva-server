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

  /**
	 * 
	 */
	private static final long serialVersionUID = -2304553481644165127L;
private Cookie[] cookies;

  public CookieMap(HttpServletRequest request) {
    cookies = request.getCookies();
  }

  @Override
protected Enumeration getNames() {
    return new CookieEnumerator(cookies);
  }

  @Override
protected Object getValue(Object key) {
    for (int i = 0; i < cookies.length; i++) {
      if (key.equals(cookies[i].getName())) {
        return cookies[i].getValue();
      }
    }
    return null;
  }

  @Override
protected void putValue(Object key, Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
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
