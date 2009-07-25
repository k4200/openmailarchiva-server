/*
 * $Header: /cvsroot/securityfilter/securityfilter/src/share/org/securityfilter/authenticator/persistent/PersistentLoginManagerInterface.java,v 1.2 2005/02/22 09:17:13 maxcooper Exp $
 * $Revision: 1.2 $
 * $Date: 2005/02/22 09:17:13 $
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

package org.securityfilter.authenticator.persistent;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;

/**
 * PersistentLoginManagerInterface - Interface for the "remember me" implementation class. Implement this interface to
 * provide a specific mechanism to persist login information to support the "remember me" SecurityFilter function.
 *
 * @author David Reed (dreed10@neo.rr.com.com)
 * @author Max Cooper (max@maxcooper.com)
 * @version $Revision: 1.2 $ $Date: 2005/02/22 09:17:13 $
 */
public interface PersistentLoginManagerInterface {

   /**
    * Remember a specific login
    *
    * @param request the servlet request
    * @param response the servlet response
    * @param username the username that's being remembered
    * @param password the password that's being remembered
    */
   public void rememberLogin(
      HttpServletRequest request,
      HttpServletResponse response,
      String username,
      String password
   ) throws IOException, ServletException;

   /**
    * Forget a login
    *
    * @param request the servlet request
    * @param response the servlet response
    */
   public void forgetLogin(
      HttpServletRequest request,
      HttpServletResponse response
   ) throws IOException, ServletException;

   /**
    * Ask "Are we remembering logins"
    *
    * @param request the servlet request
    * @return true if login cookie was found
    */
   public boolean rememberingLogin(HttpServletRequest request);

   /**
    * Get remembered username
    *
    * @param request the servlet request
    * @param response the servlet response
    * @return the username value or null if not found or a problem with security of cookie
    */
   public String getRememberedUsername(
      HttpServletRequest request,
      HttpServletResponse response
   ) throws IOException, ServletException;

   /**
    * Get remembered password
    *
    * @param request the servlet request
    * @param response the servlet response
    * @return the password value or null if not found or a problem with security of cookie
    */
   public String getRememberedPassword(
      HttpServletRequest request,
      HttpServletResponse response
   ) throws IOException, ServletException;
}

// ----------------------------------------------------------------------------
// EOF