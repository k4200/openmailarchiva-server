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

  /**
	 * 
	 */
	private static final long serialVersionUID = -2852305438252800120L;
private HttpSession session;

  public SessionMap(HttpServletRequest request) {
    this.session = request.getSession();
  }

  @Override
protected Enumeration getNames() {
    return session.getAttributeNames();
  }

  @Override
protected Object getValue(Object key) {
    return session.getAttribute(String.valueOf(key));
  }

  @Override
protected void putValue(Object key, Object value) {
    session.setAttribute(String.valueOf(key), value);
  }

  @Override
protected void removeValue(Object key) {
    session.removeAttribute(String.valueOf(key));
  }

}
