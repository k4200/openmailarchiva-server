/*
 * $Header: /cvsroot/securityfilter/securityfilter/src/share/org/securityfilter/config/SecurityConfig.java,v 1.18 2007/11/07 17:22:38 chris_schultz Exp $
 * $Revision: 1.18 $
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

package org.securityfilter.config;

import org.apache.commons.digester.Digester;
import org.securityfilter.authenticator.persistent.PersistentLoginManagerInterface;
import org.securityfilter.realm.SecurityRealmInterface;
import org.xml.sax.*;

import java.io.IOException;
import java.lang.reflect.*;
import java.net.URL;
import java.util.*;

/**
 * SecurityConfig gathers information from the security-config.xml file to be used by the filter.
 *
 * @author Torgeir Veimo (torgeir@pobox.com)
 * @author Max Cooper (max@maxcooper.com)
 * @author Daya Sharma (iamdaya@yahoo.com, billydaya@sbcglobal.net)
 * @author David Reed (dreed10@neo.rr.com)
 * @version $Revision: 1.18 $ $Date: 2007/11/07 17:22:38 $
 */
public class SecurityConfig {

   private String loginPage = null;
   private String errorPage = null;
   private String logoutPage = null;
   private String defaultPage = null;
   private ArrayList securityConstraints = null;
   private SecurityRealmInterface realm = null;
   private PersistentLoginManagerInterface persistentLoginManager;
   private Object lastRealm = null;
   private boolean validating;
   private String authMethod;
   private String realmName;

   /**
    * Constructor that takes the validating flag and debug level to be used while parsing.
    *
    * @param validating validate the input file, true = validate, false = don't validate
    */
   public SecurityConfig(boolean validating) {
      this.validating = validating;
   }

   /**
    * Return the login page URL.
    */
   public String getLoginPage() {
      return loginPage;
   }

   /**
    * Set the login page URL. This is the page the user will be sent to to log in (i.e. the login form).
    *
    * @param loginPage The login page url (relative to site root)
    */
   public void setLoginPage(String loginPage) {
      this.loginPage = loginPage;
   }

   /**
    * Return the error page URL.
    */
   public String getErrorPage() {
      return errorPage;
   }

   /**
    * Set the error page URL. This is the page the user will be sent to if login request fails.
    *
    * @param errorPage The login page URL (relative to site root)
    */
   public void setErrorPage(String errorPage) {
      this.errorPage = errorPage;
   }

   /**
    * Return the logout page URL.
    */
   public String getLogoutPage() {
      return logoutPage;
   }

   /**
    * Set the logout page URL.
    *
    * @param logoutPage The logout page url (relative to site root)
    */
   public void setLogoutPage(String logoutPage) {
      this.logoutPage = logoutPage;
   }

   /**
    * Return the default page URL.
    */
   public String getDefaultPage() {
      return defaultPage;
   }

   /**
    * Set the default page URL. This is the page the user will be sent to if they submit a login request without
    * being forced to the login page by the filter.
    *
    * @param defaultPage The default page URL (relative to site root)
    */
   public void setDefaultPage(String defaultPage) {
      this.defaultPage = defaultPage;
   }

   /**
    * Get the authentication method being used to challenge the user.
    * Currently, only BASIC and FORM based are supported.
    *
    * @return BASIC or FORM
    */
   public String getAuthMethod() {
      return authMethod;
   }

   /**
    * Set the authentication method being used to challenge the user.
    * Currently, only BASIC and FORM based are supported.
    *
    * @param authMethod The authentication method to be used by the filter
    */
   public void setAuthMethod(String authMethod) {
      this.authMethod = authMethod;
   }

   /**
    * Get the authentication realm name.
    * This is used for BASIC authentication.
    *
    * @return the realm-name configured by the application developer
    */
   public String getRealmName() {
      return realmName;
   }

   /**
    * Set the authentication realm name.
    * This is used for BASIC authentication.
    *
    * @param realmName the realm name to be used for BASIC authentication
    */
   public void setRealmName(String realmName) {
      this.realmName = realmName;
   }

   /**
    * Return the realm to use for authentication. This is the outer-most realm if nested realms are used.
    * The outer-most realm must be listed first in the configuration file.
    */
   public SecurityRealmInterface getRealm() {
      return realm;
   }

   /**
    * Adds a realm to use for authentication.
    *
    * The first time this method is called, the realm must implement SecurityRealmInterface.
    * Subsequent calls can be any kind of object, and setRealm(realm) will be called on the
    * last realm passed to this method. This allows nesting of realms for caching or when a
    * realm adapter is used.
    *
    * @param realm The realm to use, or nest in deeper realm
    */
   public synchronized void addRealm(
      Object realm
   ) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
      if (this.realm == null) {
         this.realm = (SecurityRealmInterface) realm;
         lastRealm = realm;
      } else {
         // TODO: allow addRealm signaure to take types besides Object -- will commons-beanutils help?
         // call lastRealm.setRealm(realm)
         Method addMethod = lastRealm.getClass().getMethod("setRealm", new Class[]{Object.class});
         addMethod.invoke(lastRealm, new Object[]{realm});
         lastRealm = realm;
      }
   }

   /**
    * Return the configured SecurityConstraints.
    */
   public List getSecurityConstraints() {
      return this.securityConstraints;
   }

   /**
    * Adds a SecurityConstraint.
    *
    * @param constraint The SecurityConstraint to add
    */
   public void addSecurityConstraint(SecurityConstraint constraint) {
      securityConstraints.add(constraint);
   }

   /**
    * Set the PersistentLoginManager to be used for persisting logins.
    *
    * @param persistentLoginManager StickyLoginManager to use for this implementation
    */
   public void setPersistentLoginManager(PersistentLoginManagerInterface persistentLoginManager) {
      this.persistentLoginManager = persistentLoginManager;
   }

   /**
    * Return the StickyLoginManager used for this implementation
    */
   public PersistentLoginManagerInterface getPersistentLoginManager() {
      return persistentLoginManager;
   }

    public void loadConfig(URL configURL)
        throws IOException, SAXException
    {
        loadConfig(new InputSource(configURL.openStream()));
    }

   /**
    * Loads configuration from the specifued configURL.
    *
    * @param configURL The url to load.
    *
    * @exception IOException if an input/output error occurs
    * @exception SAXException if the file has invalid xml syntax
    */
    public void loadConfig(InputSource input)
        throws IOException, SAXException
    {
      securityConstraints = new ArrayList();

      Digester digester = new Digester();
      digester.setValidating(false);

      // only register the DTDs if we will be validating
      registerLocalDTDs(digester);

      digester.push(this);
      digester.setUseContextClassLoader(true);
      digester.setValidating(validating);

      // realms
      digester.addObjectCreate("securityfilter-config/realm", null, "className");
      digester.addSetProperty("securityfilter-config/realm/realm-param", "name", "value");
      digester.addSetNext("securityfilter-config/realm", "addRealm", "java.lang.Object");

      // auth method, realm name
      digester.addCallMethod("securityfilter-config/login-config/auth-method", "setAuthMethod", 0);
      digester.addCallMethod("securityfilter-config/login-config/realm-name", "setRealmName", 0);

      // login, error, logout, and default pages
      digester.addCallMethod("securityfilter-config/login-config/form-login-config/form-login-page", "setLoginPage", 0);
      digester.addCallMethod("securityfilter-config/login-config/form-login-config/form-error-page", "setErrorPage", 0);
      digester.addCallMethod(
         "securityfilter-config/login-config/form-login-config/form-logout-page",
         "setLogoutPage",
         0
      );
      digester.addCallMethod(
         "securityfilter-config/login-config/form-login-config/form-default-page",
         "setDefaultPage",
         0
      );

      // persistent login manager
      digester.addObjectCreate("securityfilter-config/login-config/form-login-config/remember-me", null, "className");
      digester.addSetProperty(
         "securityfilter-config/login-config/form-login-config/remember-me/remember-me-param",
         "name",
         "value"
      );
      digester.addSetNext(
         "securityfilter-config/login-config/form-login-config/remember-me",
         "setPersistentLoginManager",
         "org.securityfilter.authenticator.persistent.PersistentLoginManagerInterface"
      );

      // security-constraint
      digester.addObjectCreate(
         "securityfilter-config/security-constraint",
         "org.securityfilter.config.SecurityConstraint"
      );
      digester.addSetNext(
         "securityfilter-config/security-constraint",
         "addSecurityConstraint",
         "org.securityfilter.config.SecurityConstraint"
      );

      // auth-constraint
      digester.addObjectCreate(
         "securityfilter-config/security-constraint/auth-constraint",
         "org.securityfilter.config.AuthConstraint"
      );
      digester.addSetNext(
         "securityfilter-config/security-constraint/auth-constraint",
         "setAuthConstraint",
         "org.securityfilter.config.AuthConstraint"
      );
      digester.addCallMethod(
         "securityfilter-config/security-constraint/auth-constraint/role-name",
         "addRole",
         0
      );

      // user-data-constraint
      digester.addObjectCreate(
         "securityfilter-config/security-constraint/user-data-constraint",
         "org.securityfilter.config.UserDataConstraint"
      );
      digester.addSetNext(
         "securityfilter-config/security-constraint/user-data-constraint",
         "setUserDataConstraint",
         "org.securityfilter.config.UserDataConstraint"
      );
      digester.addCallMethod(
         "securityfilter-config/security-constraint/user-data-constraint/transport-guarantee",
         "setTransportGuarantee",
         0
      );

      // web-resource-collection
      digester.addObjectCreate(
         "securityfilter-config/security-constraint/web-resource-collection",
         "org.securityfilter.config.WebResourceCollection"
      );
      digester.addSetNext(
         "securityfilter-config/security-constraint/web-resource-collection",
         "addWebResourceCollection",
         "org.securityfilter.config.WebResourceCollection"
      );
      digester.addCallMethod(
         "securityfilter-config/security-constraint/web-resource-collection/url-pattern",
         "addURLPattern",
         0
      );
      digester.addCallMethod(
         "securityfilter-config/security-constraint/web-resource-collection/http-method",
         "addHttpMethod",
         0
      );

      digester.parse(input);
   }

   /**
    * Register local copies of the SecurityFilter DTD files.
    *
    * @param digester
    */
   protected void registerLocalDTDs(Digester digester) {
      // register the local version of the 1.0 DTD, if it is available
      URL dtd1_0 = this.getClass().getResource("/org/securityfilter/resources/securityfilter-config_1_0.dtd");
      if (dtd1_0 != null) {
         digester.register("-//SecurityFilter.org//DTD Security Filter Configuration 1.0//EN", dtd1_0.toString());
      }

      // register the local version of the 1.1 DTD, if it is available
      URL dtd1_1 = this.getClass().getResource("/org/securityfilter/resources/securityfilter-config_1_1.dtd");
      if (dtd1_1 != null) {
         digester.register("-//SecurityFilter.org//DTD Security Filter Configuration 1.1//EN", dtd1_1.toString());
      }

      // register the local version of the 2.0 DTD, if it is available
      URL dtd2_0 = this.getClass().getResource("/org/securityfilter/resources/securityfilter-config_2_0.dtd");
      if (dtd2_0 != null) {
         digester.register("-//SecurityFilter.org//DTD Security Filter Configuration 2.0//EN", dtd2_0.toString());
      }
   }
}

// ------------------------------------------------------------------------
// EOF
