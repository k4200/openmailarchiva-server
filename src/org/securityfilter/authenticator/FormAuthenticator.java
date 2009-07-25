/*
 * $Header: /cvsroot/securityfilter/securityfilter/src/share/org/securityfilter/authenticator/FormAuthenticator.java,v 1.13 2008/04/18 13:08:03 chris_schultz Exp $
 * $Revision: 1.13 $
 * $Date: 2008/04/18 13:08:03 $
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

import org.securityfilter.authenticator.persistent.PersistentLoginManagerInterface;
import org.securityfilter.config.SecurityConfig;
import org.securityfilter.filter.*;
import org.securityfilter.realm.SecurityRealmInterface;
import org.securityfilter.realm.FlexibleRealmInterface;

import javax.servlet.FilterConfig;
import javax.servlet.http.*;
import java.io.IOException;
import java.security.Principal;

import java.util.Enumeration;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

/**
 * FormAuthenticator - authenticator implementation for the FORM auth method.
 *
 * @author Max Cooper (max@maxcooper.com)
 * @author Chris Schultz (chris@christopherschultz.net)
 * @version $Revision: 1.13 $ $Date: 2008/04/18 13:08:03 $
 */
public class FormAuthenticator implements Authenticator {

   public static final String LOGIN_SUBMIT_PATTERN_KEY = "loginSubmitPattern";
   public static final String DEFAULT_LOGIN_SUBMIT_PATTERN = "/j_security_check";

   protected String loginSubmitPattern;

   protected static final String FORM_USERNAME = "j_username";
   protected static final String FORM_PASSWORD = "j_password";
   protected static final String FORM_REMEMBERME = "j_rememberme";

   protected String loginPage;
   protected URLPattern loginPagePattern;
   protected String errorPage;
   protected URLPattern errorPagePattern;
   protected String defaultPage;

   protected PersistentLoginManagerInterface persistentLoginManager;
   protected URLPattern logoutPagePattern;

   protected SecurityRealmInterface realm;


    /**
     * The key that will be used to look up the filter init parameter
     * that specifies the "forward" parameter used for post-login forward
     * requests.
     *
     * @see #forwardParameterName
     */
    public static final String FORWARD_PARAMETER_KEY = "forwardParameter";

    /**
     * The key that will be used to look up the filter init parameter
     * that specifies the "forwardMode" parameter used for post-login forward
     * requests.
     *
     * @see #forwardModeParameterName
     */
    public static final String FORWARD_MODE_PARAMETER_KEY = "forwardModeParameter";

    /**
     * The key that will be used to look up the filter init parameter
     * that specifies the "forwardParameters" parameter used for post-login
     * forward requests.
     *
     * @see #forwardParametersParameterName
     */
    public static final String FORWARD_PARAMETERS_PARAMETER_KEY = "forwardParametersParameter";


    /**
     * The key that will be used to look the filter init parameter
     * that specifies the character encoding to be used for parameters
     * forwarded from the login form.
     */
    public static final String FORWARD_PARAMETERS_ENCODING_KEY = "forwardParametersEncoding";

    /**
     * The default value for {@link #forwardParameterName}.
     */
    public static final String DEFAULT_FORWARD_PARAMETER_NAME = "forward";
    /**
     * The default value for {@link #forwardModeParameterName}.
     */
    public static final String DEFAULT_FORWARD_MODE_PARAMETER_NAME = "forward-mode";
    /**
     * The default value for {@link #forwardParametersParameterName}.
     */
    public static final String DEFAULT_FORWARD_PARAMETERS_PARAMETER_NAME = "forward-parameters";

    /**
     * The default encoding to be used for forwarded parameters.
     */
    public static final String DEFAULT_FORWARD_PARAMETERS_ENCODING = "UTF-8";

    /**
     * The name of the request parameter that will be recognized as a
     * post-login forward request.
     *
     * @see #forwardParameterName
     * @see #DEFAULT_FORWARD_PARAMETER_NAME
     */
    protected String forwardParameterName;

    /**
     * The name of the request parameter that will be checked for
     * either "forward" or "redirect" when processing a post-login forward
     * request. The default is "redirect".
     *
     * @see #forwardModeParameterName
     * @see #DEFAULT_FORWARD_MODE_PARAMETER_NAME
     */
    protected String forwardModeParameterName;

    /**
     * The name of the request parameter that will be checked to see
     * whether the login request's request parameters should be forwarded
     * to the destination URI when processing a post-login forward request.
     * The options are "true" (to forward the request parameters) or "false"
     * (to forward to the destination URI with no request parameter
     * pass-through. The default is "false".
     *
     * @see #forwardParameterName
     * @see #DEFAULT_FORWARD_PARAMETERS_PARAMETER_NAME
     */
    protected String forwardParametersParameterName;

    /**
     * The character encoding that will be used to encode URL parameters
     * forwarded through the login page.
     */
    protected String forwardParametersEncoding;

   /**
    * Initilize this Authenticator.
    *
    * @param filterConfig
    * @param securityConfig
    */
   public void init(FilterConfig filterConfig, SecurityConfig securityConfig) throws Exception {

      realm = securityConfig.getRealm();

      // login submit pattern
      loginSubmitPattern = filterConfig.getInitParameter(LOGIN_SUBMIT_PATTERN_KEY);
      if (loginSubmitPattern == null) {
         loginSubmitPattern = DEFAULT_LOGIN_SUBMIT_PATTERN;
      }

      // "forward" parameter
      forwardParameterName = filterConfig.getInitParameter(FORWARD_PARAMETER_KEY);
      if(null == forwardParameterName)
	  forwardParameterName = DEFAULT_FORWARD_PARAMETER_NAME;

      // "forward-mode" parameter name
      forwardModeParameterName = filterConfig.getInitParameter(FORWARD_MODE_PARAMETER_KEY);
      if(null == forwardModeParameterName)
	  forwardModeParameterName = DEFAULT_FORWARD_MODE_PARAMETER_NAME;

      // "forward-parameters" parameter name
      forwardParametersParameterName = filterConfig.getInitParameter(FORWARD_PARAMETERS_PARAMETER_KEY);
      if(null == forwardParametersParameterName)
	  forwardParametersParameterName = DEFAULT_FORWARD_PARAMETERS_PARAMETER_NAME;

      //
      forwardParametersEncoding = filterConfig.getInitParameter(FORWARD_PARAMETERS_ENCODING_KEY);
      if(null == forwardParametersEncoding)
          forwardParametersEncoding = DEFAULT_FORWARD_PARAMETERS_ENCODING;

      // default page
      defaultPage = securityConfig.getDefaultPage();

      URLPatternFactory patternFactory = new URLPatternFactory();

      // login page
      loginPage = securityConfig.getLoginPage();
      loginPagePattern = patternFactory.createURLPattern(stripQueryString(loginPage), null, null, 0);

      // error page
      errorPage = securityConfig.getErrorPage();
      errorPagePattern = patternFactory.createURLPattern(stripQueryString(errorPage), null, null, 0);

      // -- Persistent Login Info --------------------------------------------------------------------------------------

      // logout page
      String logoutPage = securityConfig.getLogoutPage();
      if (logoutPage != null) {
         logoutPagePattern = patternFactory.createURLPattern(stripQueryString(logoutPage), null, null, 0);
      }

      // persistent login manager class
      persistentLoginManager = securityConfig.getPersistentLoginManager();
   }

   /**
    * Returns FORM as the authentication method.
    *
    * @return FORM
    */
   public String getAuthMethod() {
      return HttpServletRequest.FORM_AUTH;
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

      // process any persistent login information, if user is not already logged in,
      // persistent logins are enabled, and the persistent login info is present in this request
      if (
         request.getRemoteUser() == null
         && persistentLoginManager != null
         && persistentLoginManager.rememberingLogin(request)
      ) {
         String username = persistentLoginManager.getRememberedUsername(request, response);
         String password = persistentLoginManager.getRememberedPassword(request, response);
         Principal principal = realm.authenticate(username, password);
         if (principal != null) {
            request.setUserPrincipal(principal);
         } else {
            // failed authentication with remembered login, better forget login now
            persistentLoginManager.forgetLogin(request, response);
         }
      }

      // process login form submittal
      if (request.getMatchableURL().endsWith(loginSubmitPattern)) {
          String username = request.getParameter(FORM_USERNAME);
          String password = request.getParameter(FORM_PASSWORD);
          Principal principal = realm instanceof FlexibleRealmInterface ?
              ((FlexibleRealmInterface) realm).authenticate(request)
              : realm.authenticate(username, password);
          if (principal != null) {
              // login successful

              // invalidate old session if the user was already authenticated, and they logged in as a different user
              if (request.getUserPrincipal() != null
                  && false == request.getUserPrincipal().equals(principal)) {
                  request.getSession().invalidate();
              }

              // manage persistent login info, if persistent login management is enabled
              // and username/password are passed as part of logon
              if (persistentLoginManager != null
                  && username != null && password != null) {
                  String rememberme = request.getParameter(FORM_REMEMBERME);
                  // did the user request that their login be persistent?
                  if (rememberme != null) {
                      // remember login
                      persistentLoginManager.rememberLogin(request, response, username, password);
                  } else {
                      // forget login
                      persistentLoginManager.forgetLogin(request, response);
                  }
              }

              request.setUserPrincipal(principal);


              Forward fwd = getForward(request);

              if(fwd.redirect)
              {
                  String uri = response.encodeRedirectURL(fwd.uri);

                  // Parameters only need to be explicitly forwarded
                  // when we're doing a redirect.
                  if(fwd.forwardParameters)
                  {
                      StringBuffer q = this.getFilteredQueryString(request);
                      if(null != q)
                          uri += q;
                  }

                  response.sendRedirect(uri);
              }
              else
                  request.getRequestDispatcher(fwd.uri).
                      forward(request, response);
          } else {
              // login failed - forward to error page
              request.getRequestDispatcher(errorPage).forward(request, response);
          }
          return true;
      }

      return false;
   }

   /**
    * Show the login page.
    *
    * @param request the current request
    * @param response the current response
    */
   public void showLogin(
      HttpServletRequest request,
      HttpServletResponse response
   ) throws IOException {
      // save this request
      SecurityFilter.saveRequestInformation(request);

      // redirect to login page
      response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + loginPage));
      return;
   }

   /**
    * Return true if this is a logout request.
    *
    * @param request
    * @param response
    * @param patternMatcher
    * @return true if this is a logout request, false otherwise
    */
   public boolean processLogout(
      SecurityRequestWrapper request,
      HttpServletResponse response,
      URLPatternMatcher patternMatcher
   ) throws Exception {
      String requestURL = request.getMatchableURL();
      // check if this is a logout request
      if (matchesLogoutPattern(requestURL, patternMatcher)) {
         // if remembering this login call forgetLogin() method to forget it
         if (persistentLoginManager != null && persistentLoginManager.rememberingLogin(request)) {
            persistentLoginManager.forgetLogin(request, response);
         }
         return true;
      }
      return false;
   }

   /**
    * The login and error pages should be viewable, even if they would otherwise be blocked by a security constraint.
    *
    * @param request
    * @return
    */
   public boolean bypassSecurityForThisRequest(
      SecurityRequestWrapper request,
      URLPatternMatcher patternMatcher
   ) throws Exception {
      String requestURL = request.getMatchableURL();
      return (
         patternMatcher.match(requestURL, loginPagePattern)
         || patternMatcher.match(requestURL, errorPagePattern)
         || matchesLogoutPattern(requestURL, patternMatcher)
      );
   }

   /**
    * Returns true if the logout pattern is not null and the request URL string passed in matches it.
    *
    * @param requestURL
    * @param patternMatcher
    * @return true if the logout page is defined and the request URL matches it
    * @throws Exception
    */
   private boolean matchesLogoutPattern(String requestURL, URLPatternMatcher patternMatcher) throws Exception {
      if (logoutPagePattern != null) {
         return patternMatcher.match(requestURL, logoutPagePattern);
      }
      return false;
   }

   /**
    * FormAuthenticator has a special case where the user should be sent to a default page if the user
    * spontaneously submits a login request.
    *
    * @param request
    * @return a URL to send the user to after logging in
    */
   private String getContinueToURL(HttpServletRequest request) {
      String savedURL = SecurityFilter.getContinueToURL(request);
      if (savedURL != null) {
         return savedURL;
      } else {
         return request.getContextPath() + defaultPage;
      }
   }

   /**
    * Utility method to strip the query string from a uri.
    *
    * @param uri
    * @return uri with query string removed (if it had one)
    */
   private String stripQueryString(String uri) {
      if (uri != null) {
         int queryStart = uri.indexOf('?');
         if (queryStart != -1) {
            uri = uri.substring(0, queryStart);
         }
      }
      return uri;
   }

    /**
     * A class to represent information about the destination after login.
     */
    private static class Forward
    {
        /**
         * The destination URI.
         */
        String uri;

        /**
         * <code>true</code> if this Forward should be redirected through the
         * client.
         */
        boolean redirect;

        /**
         * <code>true</code> if the forward should include all the parameters
         * from the current request.
         */
        boolean forwardParameters;

        Forward(String uri, boolean redirect, boolean forwardParameters)
        {
            this.uri = uri;
            this.redirect = redirect;
            this.forwardParameters = forwardParameters;
        }
    }

    /**
     * Gets post-login destination information.
     */
    private Forward getForward(HttpServletRequest request)
    {
        String uri  = request.getParameter(forwardParameterName);
        boolean redirect;
        boolean forwardParameters;

        // Was there a request to forward somewhere else after login?
        if(null != uri && 0 < uri.trim().length())
        {
            // Default to redirect
            redirect = !"forward".equalsIgnoreCase(request.getParameter(forwardModeParameterName));
            // Default to do-not-forward-parameters
            forwardParameters = "true".equalsIgnoreCase(request.getParameter(forwardParametersParameterName));
        }
        else
        {
            // No forward request: go to the "continue URL" which is either
            // the user's original request or the default page to hit after login.
            uri = getContinueToURL(request);
            redirect = true;
            forwardParameters = false;
        }

        return new Forward(uri, redirect, forwardParameters);
    }

    /**
     * Gets the query string that will be used when a login request
     * has included a "forward" directive. We don't want to include
     * username and password information in the resulting URL, so we
     * re-build the query string by stripping-out the sensitive
     * parameters. We also strip-out the "forward" parameter information
     * because it has served its purpose.
     *
     * @param request The request being processed.
     *
     * @return A StringBuffer containing the query string (starting with '?')
     *         with all of the current request's parameters except for
     *         the username, password, and forward-related parameters.
     */
    private StringBuffer getFilteredQueryString(HttpServletRequest request)
        throws UnsupportedEncodingException
    {
        Enumeration e = request.getParameterNames();

        StringBuffer queryString = null;

        if(e.hasMoreElements())
        {
            boolean first = true;
            queryString = new StringBuffer();

            while(e.hasMoreElements())
            {
                String name = (String)e.nextElement();

                // Filter-out login-related parameters
                if(!(FORM_USERNAME.equals(name)
                     || FORM_PASSWORD.equals(name)
                     || forwardParameterName.equals(name)
                     || forwardModeParameterName.equals(name)
                     || forwardParametersParameterName.equals(name)))
                {
                    String[] values = request.getParameterValues(name);

                    for(int i=0; i<values.length; ++i)
                    {
                        if(first)
                        {
                            queryString.append('?');
                            first = false;
                        }
                        else
                            queryString.append('&');

                        queryString
                            .append(URLEncoder.encode(name, forwardParametersEncoding))
                            .append('=')
                            .append(URLEncoder.encode(values[i], forwardParametersEncoding));
                    }
                }
            }
        }

        return queryString;
    }
}

// ------------------------------------------------------------------------
// EOF
