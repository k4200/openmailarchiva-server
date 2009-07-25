/*
 * $Header: /cvsroot/securityfilter/securityfilter/src/share/org/securityfilter/filter/SecurityRequestWrapper.java,v 1.10 2006/02/14 09:28:27 maxcooper Exp $
 * $Revision: 1.10 $
 * $Date: 2006/02/14 09:28:27 $
 *
 * ====================================================================
 * The SecurityFilter Software License, Version 1.1
 *
 * (this license is derived and fully compatible with the Apache Software
 * License - see http://www.apache.org/LICENSE.txt)
 *
 * Copyright (c) 2002 SecurityFilter.org. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by
 *        SecurityFilter.org (http://www.securityfilter.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The name "SecurityFilter" must not be used to endorse or promote
 *    products derived from this software without prior written permission.
 *    For written permission, please contact license@securityfilter.org .
 *
 * 5. Products derived from this software may not be called "SecurityFilter",
 *    nor may "SecurityFilter" appear in their name, without prior written
 *    permission of SecurityFilter.org.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE SECURITY FILTER PROJECT OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 */

package org.securityfilter.filter;

import org.securityfilter.realm.SecurityRealmInterface;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.security.Principal;
import java.util.*;

/**
 * SecurityRequestWrapper
 *
 * @author Max Cooper (max@maxcooper.com)
 * @author Daya Sharma (iamdaya@yahoo.com, billydaya@sbcglobal.net)
 * @author Torgeir Veimo (torgeir@pobox.com)
 * @version $Revision: 1.10 $ $Date: 2006/02/14 09:28:27 $
 */
public class SecurityRequestWrapper extends HttpServletRequestWrapper {
   public static final String PRINCIPAL_SESSION_KEY = SecurityRequestWrapper.class.getName() + ".PRINCIPAL";

   private HttpServletRequest currentRequest;
   private SecurityRealmInterface realm;
   private SavedRequest savedRequest;
   private String authType;
   private String matchableURL;

	/**
    * Construct a new SecurityRequestWrapper.
    *
    * @param request the request to wrap
    * @param realm the SecurityRealmInterface implementation
    * @param savedRequest SavedRequest (usually null, unless this is the request
    * that invoked the authorization sequence)
    */
   public SecurityRequestWrapper(
      HttpServletRequest request,
      SavedRequest savedRequest,
      SecurityRealmInterface realm,
      String authType
   ) {
      super(request);
      this.currentRequest = request;
      this.realm = realm;
      this.savedRequest = savedRequest;
      this.authType = authType;
      initMatchableURL();
   }

	/**
	 * Get the original HttpServletRequest object.
	 */
	public HttpServletRequest getCurrentRequest() {
		return currentRequest;
	}

   /**
    * Get a parameter value by name. If multiple values are available, the first value is returned.
    *
    * @param s parameter name
    */
   public String getParameter(String s) {
      if (savedRequest == null) {
         return currentRequest.getParameter(s);
      } else {
         String value = currentRequest.getParameter(s);
         if (value == null) {
            String[] valueArray = (String[]) savedRequest.getParameterMap().get(s);
            if (valueArray != null) {
               value = valueArray[0];
            }
         }
         return value;
      }
   }

   /**
    * Get a map of parameter values for this request.
    */
   public Map getParameterMap() {
      if (savedRequest == null) {
         return currentRequest.getParameterMap();
      } else {
         Map map = new HashMap(savedRequest.getParameterMap());
         map.putAll(currentRequest.getParameterMap());
         return Collections.unmodifiableMap(map);
      }
   }

   /**
    * Get an enumeration of paramaeter names for this request.
    */
   public Enumeration getParameterNames() {
      if (savedRequest == null) {
         return currentRequest.getParameterNames();
      } else {
         return Collections.enumeration(getParameterMap().keySet());
      }
   }

   /**
    * Get an array of values for a parameter.
    *
    * @param s parameter name
    */
   public String[] getParameterValues(String s) {
      if (savedRequest == null) {
         return currentRequest.getParameterValues(s);
      } else {
         String[] values = currentRequest.getParameterValues(s);
         if (values == null) {
            values = (String[]) savedRequest.getParameterMap().get(s);
         }
         return values;
      }
   }

   /**
    * Set the request that is to be wrapped.
    *
    * @param request wrap this request
    */
   public void setRequest(ServletRequest request) {
      super.setRequest(request);
      this.currentRequest = (HttpServletRequest) request;
   }

   /**
    * Check if a user is in a role.
    *
    * @param role name of role to check
    */
   public boolean isUserInRole(String role) {
      return realm.isUserInRole(getUserPrincipal(), role);
   }

   /**
    * Get the remote user's login name
    */
   public String getRemoteUser() {
      String username = null;
      Principal principal = getUserPrincipal();
      if (principal != null) {
         username = principal.getName();
      }
      return username;
   }

   /**
    * Get a Principal object for the current user.
    */
   public Principal getUserPrincipal() {
      Principal principal = null;
      HttpSession session = currentRequest.getSession(false);
      if (session != null) {
         principal = (Principal) session.getAttribute(PRINCIPAL_SESSION_KEY);
      }
      return principal;
   }

   /**
    * This method is provided to restore functionality of this method in case the wrapper class we are extending
    * has disabled it. This method is needed to process multi-part requests downstream, and it appears that some
    * wrapper implementations just return null. WebLogic 6.1.2.0 is one such implementation.
    *
    * @exception IOException
    */
   public ServletInputStream getInputStream() throws IOException {
      ServletInputStream stream = super.getInputStream();
      if (stream == null) {
         stream = currentRequest.getInputStream();
      }
      return stream;
   }

   /**
    * Set the username of the current user.
    * WARNING: Calling this method will set the user for this session -- authenticate the user before calling
    * this method.
    *
    * @param principal the user Principal object
    */
   public void setUserPrincipal(Principal principal) {
      currentRequest.getSession().setAttribute(PRINCIPAL_SESSION_KEY, principal);
   }

   /**
    * Returns the auth type (e.g. FORM, BASIC, etc.).
    */
   public String getAuthType() {
      if (getUserPrincipal() != null) {
         return authType;
      } else {
         return null;
      }
   }

   /**
    * Returns the HTTP method used to make this request. If the savedRequest is non-null,
    * the HTTP method of the saved request will be returned.
    */
   public String getMethod() {
      if (savedRequest != null) {
         return savedRequest.getMethod();
      } else {
         return super.getMethod();
      }
   }

   /**
    * Get a URL that can be matched against security URL patterns.
    *
    * This is the part after the contextPath, with the pathInfo, but without the query string.
    * http://server:8080/contextPath/someURL.jsp?param=value becomes /someURL.jsp
    */
   public String getMatchableURL() {
      return matchableURL;
   }

   /**
    * Initilize the matchableURL.
    */
   private void initMatchableURL() {
      // extract the servlet path portion that needs to be checked
      matchableURL = currentRequest.getServletPath();
      // add the pathInfo, as it needs to be part of the URL we check
      String pathInfo = currentRequest.getPathInfo();
      if (pathInfo != null) {
         matchableURL = matchableURL + pathInfo;
      }
   }
}

// ------------------------------------------------------------------------
// EOF
