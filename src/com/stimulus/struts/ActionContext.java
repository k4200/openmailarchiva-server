
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
package com.stimulus.struts;

import com.stimulus.struts.httpmap.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.Serializable;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * The ActionContext class gives simplified, thread-safe access to
 * the request and response, as well as form parameters, request
 * attributes, session attributes, application attributes.  Much
 * of this can be accopmplished without using the Struts or even
 * the Servlet API, therefore isolating your application from
 * presentation framework details.
 * <p/>
 * This class also provides facilities for simpler message and error
 * message handling.  Although not as powerful as that provided by
 * Struts, it is great for simple applications that don't require
 * internationalization or the flexibility of resource bundles.
 * <p/>
 * <i>Note: A more complete error and message handling API will be implemented.</i>
 * <p/>
 * Date: Mar 9, 2004 9:57:39 PM
 *
 * @author Clinton Begin
 */
public class ActionContext implements Serializable {

  /**
	 * 
	 */
	private static final long serialVersionUID = -2643241956613163150L;

private static final ThreadLocal localContext = new ThreadLocal();

  private HttpServletRequest request;
  private HttpServletResponse response;

  private Map cookieMap;
  private Map parameterMap;
  private Map requestMap;
  private Map sessionMap;
  private Map applicationMap;

  private ActionContext() {
  }

  protected static void initialize(HttpServletRequest request, HttpServletResponse response) {
    ActionContext ctx = getActionContext();
    ctx.request = request;
    ctx.response = response;
    ctx.cookieMap = null;
    ctx.parameterMap = null;
    ctx.requestMap = null;
    ctx.sessionMap = null;
    ctx.applicationMap = null;
  }

  public void setSimpleMessage(String message) {
    getRequestMap().put("message", message);
  }

  public void addSimpleError(String message) {
    List errors = (List) getRequestMap().get("errors");
    if (errors == null) {
      errors = new ArrayList();
      getRequestMap().put("errors", errors);
    }
    errors.add(message);
  }

  public boolean isSimpleErrorsExist () {
    List errors = (List) getRequestMap().get("errors");
    return errors != null && errors.size() > 0;
  }

  public Map getCookieMap() {
    if (cookieMap == null) {
      cookieMap = new CookieMap(request);
    }
    return cookieMap;
  }

  public Map getParameterMap() {
    if (parameterMap == null) {
      parameterMap = new ParameterMap(request);
    }
    return parameterMap;
  }

  public Map getRequestMap() {
    if (requestMap == null) {
      requestMap = new RequestMap(request);
    }
    return requestMap;
  }

  public Map getSessionMap() {
    if (sessionMap == null) {
      sessionMap = new SessionMap(request);
    }
    return sessionMap;
  }

  public Map getApplicationMap() {
    if (applicationMap == null) {
      applicationMap = new ApplicationMap(request);
    }
    return applicationMap;
  }

  public HttpServletRequest getRequest() {
    return request;
  }

  public HttpServletResponse getResponse() {
    return response;
  }

  public static ActionContext getActionContext() {
    ActionContext ctx = (ActionContext) localContext.get();
    if (ctx == null) {
      ctx = new ActionContext();
      localContext.set(ctx);
    }
    return ctx;
    
  }
}
