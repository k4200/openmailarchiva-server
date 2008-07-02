package com.stimulus.struts.httpmap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import java.io.Serializable;
import java.util.Enumeration;

/**
 * Map to wrap application scope attributes.
 * <p/>
 * Date: Mar 11, 2004 11:21:25 PM
 *
 * @author Clinton Begin
 */
public class ApplicationMap extends BaseHttpMap implements Serializable {

  /**
	 * 
	 */
	private static final long serialVersionUID = 617710054180051702L;
private ServletContext context;

  public ApplicationMap(HttpServletRequest request) {
    context = request.getSession().getServletContext();
  }

  @Override
protected Enumeration getNames() {
    return context.getAttributeNames();
  }

  @Override
protected Object getValue(Object key) {
    return context.getAttribute(String.valueOf(key));
  }

  @Override
protected void putValue(Object key, Object value) {
    context.setAttribute(String.valueOf(key), value);
  }

  @Override
protected void removeValue(Object key) {
    context.removeAttribute(String.valueOf(key));
  }

}
