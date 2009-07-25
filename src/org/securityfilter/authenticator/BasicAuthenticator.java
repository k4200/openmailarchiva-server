/*
 * $Header: /cvsroot/securityfilter/securityfilter/src/share/org/securityfilter/authenticator/BasicAuthenticator.java,v 1.6 2004/10/07 21:37:29 maxcooper Exp $
 * $Revision: 1.6 $
 * $Date: 2004/10/07 21:37:29 $
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

package org.securityfilter.authenticator;

import org.apache.commons.codec.binary.Base64;
import org.securityfilter.config.SecurityConfig;
import org.securityfilter.filter.*;
import org.securityfilter.realm.SecurityRealmInterface;

import javax.servlet.FilterConfig;
import javax.servlet.http.*;
import java.io.IOException;
import java.security.Principal;

/**
 * BasicAuthenticator - authenticator implementation for the BASIC auth method.
 *
 * @author Daya Sharma (iamdaya@yahoo.com, billydaya@sbcglobal.net)
 * @author Max Cooper (max@maxcooper.com)
 * @version $Revision: 1.6 $ $Date: 2004/10/07 21:37:29 $
 */
public class BasicAuthenticator implements Authenticator {
   public static final String LOGIN_ATTEMPTS = BasicAuthenticator.class.getName() + ".LOGIN_ATTEMPTS";

   // todo: allow this message to be configured, internationalized, etc.
   public static final String LOGIN_FAILED_MESSAGE = "Sorry you are having problems logging in, please try again";
   public static final int MAX_ATTEMPTS = 3;

   protected SecurityRealmInterface realm;
   protected String realmName;

   /**
    * Initialize this Authenticator.
    *
    * @param filterConfig
    * @param securityConfig
    */
   public void init(FilterConfig filterConfig, SecurityConfig securityConfig) throws Exception {
      realm = securityConfig.getRealm();
      realmName = securityConfig.getRealmName();
   }

   /**
    * Returns BASIC as the authentication method.
    *
    * @return BASIC
    */
   public String getAuthMethod() {
      return HttpServletRequest.BASIC_AUTH;
   }

   /**
    * Process any login information that was included in the request, if any.
    * Returns true if SecurityFilter should abort further processing after the method completes (for example, if a
    * redirect was sent as part of the login processing).
    *
    * @param request
    * @param response
    * @return true if the filter should return after this method ends, false otherwise
    */
   public boolean processLogin(SecurityRequestWrapper request, HttpServletResponse response) throws Exception {
      if (request.getUserPrincipal() == null) {
         // attempt to dig out authentication info only if the user has not yet been authenticated
         String authorizationHeader = request.getHeader("Authorization");
         HttpSession session = request.getSession();
         if (authorizationHeader != null) {
            String decoded = decodeBasicAuthorizationString(authorizationHeader);
            String username = parseUsername(decoded);
            String password = parsePassword(decoded);
            Principal principal = realm.authenticate(username, password);
            if (principal != null) {
               // login successful
               request.getSession().removeAttribute(LOGIN_ATTEMPTS);
               request.setUserPrincipal(principal);
            } else if (session.getAttribute(LOGIN_ATTEMPTS) != null) {
               // login failed
               // show the basic authentication window again.
               showLogin(request.getCurrentRequest(), response);
               return true;
            }
         }
      }
      return false;
   }

   /**
    * Show the login page.
    *
    * @param request the current request
    * @param response the current response
    */
   public void showLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
      // save this request
      SecurityFilter.saveRequestInformation(request);

      // determine the number of login attempts
      int loginAttempts;
      if (request.getSession().getAttribute(LOGIN_ATTEMPTS) != null) {
         loginAttempts = ((Integer) request.getSession().getAttribute(LOGIN_ATTEMPTS)).intValue();
         loginAttempts += 1;
      } else {
         loginAttempts = 1;
      }
      request.getSession().setAttribute(LOGIN_ATTEMPTS, new Integer(loginAttempts));

      if (loginAttempts <= MAX_ATTEMPTS) {
         response.setHeader("WWW-Authenticate", "BASIC realm=\"" + realmName + "\"");
         response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      } else {
         request.getSession().removeAttribute(LOGIN_ATTEMPTS);
         response.sendError(HttpServletResponse.SC_UNAUTHORIZED, LOGIN_FAILED_MESSAGE);
      }
   }

   /**
    * Return true if security checks should be bypassed for this request.
    * Always returns false for BASIC authenticator.
    *
    * @param request
    * @param patternMatcher
    * @return always returns false
    */
   public boolean bypassSecurityForThisRequest(SecurityRequestWrapper request, URLPatternMatcher patternMatcher) {
      return false;
   }

   /**
    * Return true if this is a logout request.
    * Always returns false for BASIC authenticator.
    *
    * @param request
    * @param response
    * @param patternMatcher
    * @return always returns false
    */
   public boolean processLogout(
      SecurityRequestWrapper request,
      HttpServletResponse response,
      URLPatternMatcher patternMatcher
   ) {
      return false;
   }

   /**
    * Parse the username out of the BASIC authorization header string.
    * @param decoded
    * @return username parsed out of decoded string
    */
   private String parseUsername(String decoded) {
      if (decoded == null) {
         return null;
      } else {
         int colon = decoded.indexOf(':');
         if (colon < 0) {
            return null;
         } else {
            return decoded.substring(0, colon).trim();
         }
      }
   }

   /**
    * Parse the password out of the decoded BASIC authorization header string.
    * @param decoded
    * @return password parsed out of decoded string
    */
   private String parsePassword(String decoded) {
      if (decoded == null) {
         return null;
      } else {
         int colon = decoded.indexOf(':');
         if (colon < 0) {
            return (null);
         } else {
            return decoded.substring(colon + 1).trim();
         }
      }
   }

   /**
    * Decode the BASIC authorization string.
    *
    * @param authorization
    * @return decoded string
    */
   private String decodeBasicAuthorizationString(String authorization) {
      if (authorization == null || !authorization.toLowerCase().startsWith("basic ")) {
         return null;
      } else {
         authorization = authorization.substring(6).trim();
         // Decode and parse the authorization credentials
         return new String(Base64.decodeBase64(authorization.getBytes()));
      }
   }
}

// ------------------------------------------------------------------------
// EOF
