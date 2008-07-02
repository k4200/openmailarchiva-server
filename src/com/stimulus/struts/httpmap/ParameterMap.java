package com.stimulus.struts.httpmap;

import com.stimulus.struts.httpmap.BaseHttpMap;

import javax.servlet.http.HttpServletRequest;

import java.io.Serializable;
import java.util.Enumeration;

/**
 * Map to wrap form parameters.
 * <p/>
 * Date: Mar 11, 2004 10:35:52 PM
 *
 * @author Clinton Begin
 */
public class ParameterMap extends BaseHttpMap implements Serializable {

  /**
	 * 
	 */
	private static final long serialVersionUID = -5454082340605247771L;
private HttpServletRequest request;

  public ParameterMap(HttpServletRequest request) {
    this.request = request;
  }

  @Override
protected Enumeration getNames() {
    return request.getParameterNames();
  }

  @Override
protected Object getValue(Object key) {
    return request.getParameter(String.valueOf(key));
  }

  protected Object[] getValues(Object key) {
    return request.getParameterValues(String.valueOf(key));
  }

  @Override
protected void putValue(Object key, Object value) {
    throw new UnsupportedOperationException("Cannot put value to ParameterMap.");
  }

  @Override
protected void removeValue(Object key) {
    throw new UnsupportedOperationException("Cannot remove value from ParameterMap.");
  }

}
