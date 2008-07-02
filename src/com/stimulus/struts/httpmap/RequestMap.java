package com.stimulus.struts.httpmap;

import com.stimulus.struts.httpmap.BaseHttpMap;

import javax.servlet.http.HttpServletRequest;

import java.io.Serializable;
import java.util.Enumeration;

/**
 * Map to wrap request scope attributes.
 * <p/>
 * Date: Mar 11, 2004 10:35:34 PM
 *
 * @author Clinton Begin
 */
public class RequestMap extends BaseHttpMap implements Serializable {

  /**
	 * 
	 */
	private static final long serialVersionUID = -8947047031796747497L;
private HttpServletRequest request;

  public RequestMap(HttpServletRequest request) {
    this.request = request;
  }

  @Override
protected Enumeration getNames() {
    return request.getAttributeNames();
  }

  @Override
protected Object getValue(Object key) {
    return request.getAttribute(String.valueOf(key));
  }

  @Override
protected void putValue(Object key, Object value) {
    request.setAttribute(String.valueOf(key), value);
  }

  @Override
protected void removeValue(Object key) {
    request.removeAttribute(String.valueOf(key));
  }

}
