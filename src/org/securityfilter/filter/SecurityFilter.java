/*
 * $Header: /cvsroot/securityfilter/securityfilter/src/share/org/securityfilter/filter/SecurityFilter.java,v 1.25 2007/11/07 17:22:38 chris_schultz Exp $
 * $Revision: 1.25 $
 * $Date: 2007/11/07 17:22:38 $
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

import org.securityfilter.authenticator.*;
import org.securityfilter.config.*;
import org.securityfilter.realm.SecurityRealmInterface;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.net.URL;
import java.security.Principal;
import java.util.*;

/**
 * SecurityFilter provides authentication and authorization services.
 *
 * @author Max Cooper (max@maxcooper.com)
 * @author Daya Sharma (iamdaya@yahoo.com, billydaya@sbcglobal.net)
 * @author Torgeir Veimo (torgeir@pobox.com)
 * @version $Revision: 1.25 $ $Date: 2007/11/07 17:22:38 $
 */
public class SecurityFilter implements Filter {
   public static final String CONFIG_FILE_KEY = "config";
   public static final String DEFAULT_CONFIG_FILE = "/WEB-INF/securityfilter-config.xml";
   public static final String VALIDATE_KEY = "validate";
   public static final String SSL_PORT_INIT_PARAMETER_KEY = "ssl-redirect-port";

   public static final String TRUE = "true";

   public static final String ALREADY_PROCESSED = SecurityFilter.class.getName() + ".ALREADY_PROCESSED";

   public static final String SAVED_REQUEST_URL = SecurityFilter.class.getName() + ".SAVED_REQUEST_URL";
   public static final String SAVED_REQUEST = SecurityFilter.class.getName() + ".SAVED_REQUEST";

   protected FilterConfig config;
   protected SecurityRealmInterface realm;
   protected List patternList;
   protected URLPatternFactory patternFactory;
   protected Authenticator authenticator;

    /**
     * The port to be used when upgrading connections to HTTPS.
     */
    protected int sslPort = 443;

   /**
    * Perform filtering operation, and optionally pass the request down the chain.
    *
    * @param request the current request
    * @param response the current response
    * @param chain request handler chain
    * @exception IOException
    * @exception ServletException
    */
   public void doFilter(
      ServletRequest request,
      ServletResponse response,
      FilterChain chain
   ) throws IOException, ServletException {

      HttpServletRequest hReq = (HttpServletRequest) request;
      HttpServletResponse hRes = (HttpServletResponse) response;
      SecurityRequestWrapper wrappedRequest;

      // if the request has already been processed by the filter, pass it through unchecked
      if (!TRUE.equals(request.getAttribute(ALREADY_PROCESSED))) {
         // set an attribute on this request to indicate that it has already been processed
         request.setAttribute(ALREADY_PROCESSED, TRUE);

         // get a URLPatternMatcher to use for this thread
         URLPatternMatcher patternMatcher = patternFactory.createURLPatternMatcher();

         // get saved request, if any (returns null if not applicable)
         SavedRequest savedRequest = getSavedRequest(hReq);

         // wrap request
         wrappedRequest = new SecurityRequestWrapper(hReq, savedRequest, realm, authenticator.getAuthMethod());

         URLPattern match = null;
         try {
            // check if this is a logout request
            if (authenticator.processLogout(wrappedRequest, hRes, patternMatcher)) {
               // If logging out destroy and recreate session
               hReq.getSession().invalidate();
               hReq.getSession(true);
            }

            // check if this request includes login info
            if (authenticator.processLogin(wrappedRequest, hRes)) {
               return;
            }

            // match the url if the authenticator does not indicate that security should be bypassed
            if (!authenticator.bypassSecurityForThisRequest(wrappedRequest, patternMatcher)) {
               // check if request matches security constraint
               match = matchPattern(wrappedRequest.getMatchableURL(), wrappedRequest.getMethod(), patternMatcher);
            }
         } catch (Exception e) {
            throw new ServletException("Error matching patterns", e);
         }

         // check security constraint, if any
         if (match != null) {

             // Check user-data-constraint (transport-guarantee)
             UserDataConstraint userDataConstraint
                 = match.getSecurityConstraint().getUserDataConstraint();
             if(null != userDataConstraint)
             {
                 String tg = userDataConstraint.getTransportGuarantee();
                 if((tg.equals(UserDataConstraint.TRANSPORT_GUARANTEE_INTEGRAL)
                     || tg.equals(UserDataConstraint.TRANSPORT_GUARANTEE_CONFIDENTIAL))
                    && !request.isSecure())
                 {
                     // Servlet Specification Note:
                     //
                     // The Servlet Specification does not specify what ought
                     // to be done when the connection must be "upgraded"
                     // in order to satisfy the transport-guarantee.
                     //
                     // This implementation matches that of the Apache Tomcat
                     // servlet container (as of version 5.5).

                     // Switch from HTTP to HTTPS via redirection.
                     if(0 <= sslPort)
                     {
                         String url = getSecureURL(wrappedRequest);

                         hRes.sendRedirect(hRes.encodeRedirectURL(url));
                     }
                     else
                     {
                         // SSL port set to a negative: disable redirection.
                         hRes.sendError(HttpServletResponse.SC_FORBIDDEN,
                                        hReq.getRequestURI());
                     }

                     return;
                 }
             }

            // check auth constraint
            AuthConstraint authConstraint = match.getSecurityConstraint().getAuthConstraint();
            if (authConstraint != null) {
               Collection roles = authConstraint.getRoles();
               Principal principal = wrappedRequest.getUserPrincipal();
               // if roles is empty, access will be blocked no matter who the user is, so skip the login
               if (!roles.isEmpty() && principal == null) {
                  // user needs to be authenticated
                  authenticator.showLogin(hReq, hRes);
                  return;
               } else {
                  boolean authorized = false;
                  for (Iterator i = roles.iterator(); i.hasNext() && !authorized;) {
                     String role = (String) i.next();
                     // TODO: if *, do you need to have at least one role to be authorized?
                     // if so, we need to iterate through the roles defined in config file or change the
                     // realm inteface to get a list of roles for the user (both solutions are undesireable)
                     if ("*".equals(role) || realm.isUserInRole(principal, role)) {
                        authorized = true;
                     }
                  }
                  if (!authorized) {
                     // user does not meet role constraint
                     hRes.sendError(HttpServletResponse.SC_FORBIDDEN);
                     return;
                  }
               }
            }
         }
         // send wrapped request down the chain
         request = wrappedRequest;
      }

      // pass the request down the filter chain
      chain.doFilter(request, response);
   }

   /**
    * Initialize the SecurityFilter.
    *
    * @param config filter configuration object
    */
   public void init(FilterConfig config) throws ServletException {
      this.config = config;

      String sslPortString = config.getInitParameter(SSL_PORT_INIT_PARAMETER_KEY);
      if(null != sslPortString)
      {
          try
          {
              this.sslPort = Integer.parseInt(sslPortString);

              if(this.sslPort > 65535)
              {
                  System.err.println("ERROR: Invalid "
                                     + SSL_PORT_INIT_PARAMETER_KEY
                                     + ": " + sslPortString);
                  System.err.println("WARN: SSL port redirection is disabled.");
                  this.sslPort = -1;
              }
              else if(this.sslPort < 0)
              {
                  System.err.println("INFO: SSL port redirection is disabled (was set to " + this.sslPort + ")");
              }
          }
          catch (NumberFormatException nfe)
          {
              System.err.println("ERROR: Invalid "
                                 + SSL_PORT_INIT_PARAMETER_KEY
                                 + ": " + sslPortString);
              System.err.println("WARN: SSL port redirection is disabled.");
              nfe.printStackTrace();

              this.sslPort = -1;
          }
      }

      try {
         // parse config file

         // config file name
         String configFile = config.getInitParameter(CONFIG_FILE_KEY);
         if (configFile == null) {
            configFile = DEFAULT_CONFIG_FILE;
         }
         URL configURL = config.getServletContext().getResource(configFile);

         // validate config file?
         boolean validate = TRUE.equalsIgnoreCase(config.getInitParameter(VALIDATE_KEY));

         SecurityConfig securityConfig = new SecurityConfig(validate);
         securityConfig.loadConfig(configURL);

         // get the realm
         realm = securityConfig.getRealm();

         // create an Authenticator
         authenticator = AuthenticatorFactory.createAuthenticator(config, securityConfig);

         // create pattern list
         patternFactory = new URLPatternFactory();
         patternList = new ArrayList();
         int order = 1;
         List constraints = securityConfig.getSecurityConstraints();
         for (Iterator cIter = constraints.iterator(); cIter.hasNext();) {
            SecurityConstraint constraint = (SecurityConstraint) cIter.next();
            for (Iterator rIter = constraint.getWebResourceCollections().iterator(); rIter.hasNext();) {
               WebResourceCollection resourceCollection = (WebResourceCollection) rIter.next();
               for (Iterator pIter = resourceCollection.getURLPatterns().iterator(); pIter.hasNext();) {
                  URLPattern pattern = patternFactory.createURLPattern(
                     (String) pIter.next(),
                     constraint,
                     resourceCollection,
                     order++
                  );
                  patternList.add(pattern);
               }
            }
         }
         Collections.sort(patternList);

      } catch (java.io.IOException ioe) {
         System.err.println("unable to parse input: " + ioe);
      } catch (org.xml.sax.SAXException se) {
         System.err.println("unable to parse input: " + se);
      } catch (Exception e) {
         System.err.println("error: " + e);
         e.printStackTrace();
      }
   }

   /**
    * Destroy the filter, releasing resources.
    */
   public void destroy() {
   }

   /**
    * Find a match for the requested pattern & method, if any.
    *
    * @param pattern the pattern to match
    * @param httpMethod the HTTP Method to match
    * @param matcher the thread-local URLPatternMatcher object
    * @return the matching URLPattern object, or null if there is no match.
    */
   protected URLPattern matchPattern(String pattern, String httpMethod, URLPatternMatcher matcher) {
      // PERFORMANCE IMPROVEMENT OPPORTUNITY: cache pattern matches
      Iterator i = patternList.iterator();
      while (i.hasNext()) {
         URLPattern urlPattern = (URLPattern) i.next();
         if (matcher.match(pattern, httpMethod, urlPattern)) {
            return urlPattern;
         }
      }
      return null;
   }

   /**
    * If this request matches the one we saved, return the SavedRequest and remove it from the session.
    *
    * @param request the current request
    * @return usually null, but when the request matches the posted URL that initiated the login sequence a
    * SavedRequest object is returned.
    */
   protected SavedRequest getSavedRequest(HttpServletRequest request) {
      HttpSession session = request.getSession(false);
      if (session == null) {
          return null;
      }
      String savedURL = (String) session.getAttribute(SecurityFilter.SAVED_REQUEST_URL);
      if (savedURL != null && savedURL.equals(getSaveableURL(request))) {
         // this is a request for the request that caused the login,
         // get the SavedRequest from the session
         SavedRequest saved = (SavedRequest) session.getAttribute(SecurityFilter.SAVED_REQUEST);
         // remove the saved request info from the session
         session.removeAttribute(SecurityFilter.SAVED_REQUEST_URL);
         session.removeAttribute(SecurityFilter.SAVED_REQUEST);
         // and return the SavedRequest
         return saved;
      } else {
         return null;
      }
   }

   /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   // The following methods are provided as static utilities for use by SecurityFilter and other classes.             //
   /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

   /**
    * Get the URL to continue to after successful login. This may be the SAVED_REQUEST_URL if the authorization
    * sequence was initiated by the filter, or the default URL (as specified in the config file) if a login
    * request was spontaneously submitted.
    *
    * @param request the current request
    */
   public static String getContinueToURL(HttpServletRequest request) {
       HttpSession currentSession = request.getSession(false);
       if (currentSession == null) {
           return null;
       }
       return (String) currentSession.getAttribute(SAVED_REQUEST_URL);
   }

   /**
    * Save request information to re-use when the user is successfully authenticated.
    *
    * @param request the current request
    */
   public static void saveRequestInformation(HttpServletRequest request) {
      HttpSession session = request.getSession();
      session.setAttribute(SecurityFilter.SAVED_REQUEST_URL, getSaveableURL(request));
      session.setAttribute(SecurityFilter.SAVED_REQUEST, new SavedRequest(request));
   }

   /**
    * Return a URL suitable for saving or matching against a saved URL.<p>
    *
    * This is the whole URL, plus the query string.
    *
    * @param request the request to construct a saveable URL for
    */
   private static String getSaveableURL(HttpServletRequest request) {
      StringBuffer saveableURL = null;
      try {
         saveableURL = request.getRequestURL();
      } catch (NoSuchMethodError e) {
         saveableURL = getRequestURL(request);
      }
      // fix the protocol
      fixProtocol(saveableURL, request);
      // add the query string, if any
      String queryString = request.getQueryString();
      if (queryString != null) {
         saveableURL.append("?" + queryString);
      }
      return saveableURL.toString();
   }

    protected String getSecureURL(HttpServletRequest request)
    {
        StringBuffer url = new StringBuffer();
        url.append("https://")
            .append(request.getServerName())
            ;

        if(443 != sslPort)
            url.append(':')
                .append(sslPort)
                ;

        url.append(request.getRequestURI());

        String queryString = request.getQueryString();
        if(null != queryString)
            url.append('?')
                .append(queryString)
                ;

        return url.toString();
    }

   /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   // The following methods are provided for compatibility with various app servers.                                  //
   /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

   /**
    * Set the filter configuration, included for WebLogic 6 compatibility.
    *
    * @param config filter configuration object
    */
   public void setFilterConfig(FilterConfig config) throws ServletException {
      init(config);
   }

   /**
    * Get the filter config object, included for WebLogic 6 compatibility.
    */
   public FilterConfig getFilterConfig() {
      return config;
   }

   /**
    * Get the requestURL.
    * This method is called when the app server fails to implement HttpServletRequest.getRequestURL().
    * Orion 1.5.2 is one such server.
    */
   private static StringBuffer getRequestURL(HttpServletRequest request) {
      String protocol = request.getProtocol();
      int port = request.getServerPort();
      String portString = ":" + port;

      // todo: this needs to be tested to see if it still an issue; remove it if it is not needed
      // Set the portString to the empty string if the requrest came in on the default port.
      // This will keep Netscape from dropping the session, which happens when the port is added where it wasn't before.
      // This is not perfect, but most requests on the default ports will not be made with an explicit port number.
      if (protocol.equals("HTTP/1.1")) {
         if (!request.isSecure()) {
            if (port == 80) {
               portString = "";
            }
         } else {
            if (port == 443) {
               portString = "";
            }
         }
      }

      // construct the saveable URL string
      return new StringBuffer(protocol + request.getServerName() + portString + request.getRequestURI());
   }

   /**
    * Fix the protocol portion of an absolute url. Often, the protocol will be http: even for https: requests.
    *
    * todo: needs testing to make sure this is proper in all circumstances
    *
    * @param url
    * @param request
    */
   private static void fixProtocol(StringBuffer url, HttpServletRequest request) {
      // fix protocol, if needed (since HTTP is the same regardless of whether it runs on TCP or on SSL/TCP)
      if (
         request.getProtocol().equals("HTTP/1.1")
         && request.isSecure()
         && url.toString().startsWith("http://")
      ) {
         url.replace(0, 4, "https");
      }
   }
}

// ------------------------------------------------------------------------
// EOF
