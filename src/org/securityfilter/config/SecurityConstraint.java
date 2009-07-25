/*
 * $Header: /cvsroot/securityfilter/securityfilter/src/share/org/securityfilter/config/SecurityConstraint.java,v 1.7 2007/11/07 17:22:38 chris_schultz Exp $
 * $Revision: 1.7 $
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

import java.util.*;

/**
 * SecurityConstraint models the &lt;security-constraint&gt; element
 * in a web application's deployment descriptor.
 *
 * @author Max Cooper (max@maxcooper.com)
 * @author Torgeir Veimo (torgeir@pobox.com)
 * @author Chris Schultz (chris@christopherschultz.net)
 *
 * @version $Revision: 1.7 $ $Date: 2007/11/07 17:22:38 $
 */
public class SecurityConstraint {
   private List resourceCollections;
   private AuthConstraint authConstraint = null;
    private UserDataConstraint userDataConstraint;

   /**
    * Constructor
    */
   public SecurityConstraint() {
      this.resourceCollections = new ArrayList();
   }

   /**
    * Add a WebResourceCollection to this SecurityConstraint.
    *
    * @param resourceCollection the WebResourceCollection to add
    */
   public void addWebResourceCollection(WebResourceCollection resourceCollection) {
      resourceCollections.add(resourceCollection);
   }

   /**
    * Get the WebResourceCollections for this SecurityConstraint. The order of the list is the order in which the
    * WebResourceCollections appeared in the config file.
    *
    * @return a List of WebResourceCollections, an empty list if none were added
    */
   public List getWebResourceCollections() {
      return this.resourceCollections;
   }

   /**
    * Set the AuthConstraint.
    *
    * @param authConstraint the AuthConstraint
    */
   public void setAuthConstraint(AuthConstraint authConstraint) {
      this.authConstraint = authConstraint;
   }

   /**
    * Get the AuthConstraint.
    *
    * @return an AuthConstraint object, or null if none has been set
    */
   public AuthConstraint getAuthConstraint() {
      return authConstraint;
   }

   /**
    * Sets the UserDataConstraint for this SecurityConstraint.
    *
    * @param userDataConstraint The UserDataConstraint.
    */
    public void setUserDataConstraint(UserDataConstraint userDataConstraint)
    {
        this.userDataConstraint = userDataConstraint;
    }

   /**
    * Gets the UserDataConstraint for this SecurityConstraint.
    *
    * @return The UserDataConstraint for this SecurityConstraint, or
    *         <code>null</code> if none has been set.
    */
    public UserDataConstraint getUserDataConstraint()
    {
        return userDataConstraint;
    }
}

// ------------------------------------------------------------------------
// EOF
