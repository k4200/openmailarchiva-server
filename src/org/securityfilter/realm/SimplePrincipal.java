/*
 * $Header: /cvsroot/securityfilter/securityfilter/src/share/org/securityfilter/realm/SimplePrincipal.java,v 1.4 2003/01/06 00:17:25 maxcooper Exp $
 * $Revision: 1.4 $
 * $Date: 2003/01/06 00:17:25 $
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

package org.securityfilter.realm;

import java.io.Serializable;
import java.security.Principal;

/**
 * SimplePrincipal - a simple, serializable Principal.
 *
 * @author Max Cooper (max@maxcooper.com)
 * @version $Revision: 1.4 $ $Date: 2003/01/06 00:17:25 $
 */
public class SimplePrincipal implements Principal, Serializable {

   private String name;

   /**
    * Default constructor is private. Use SimplePrincipal(String name) constructor.
    */
   private SimplePrincipal() {
   }

   /**
    * Constructor
    */
   public SimplePrincipal(String name) {
      this.name = name;
   }

   /**
    * Returns the name of this principal.
    *
    * @return the name of this principal.
    */
   public String getName() {
      return name;
   }

   /**
    * Compares this principal to the specified object.
    *
    * @param obj object to compare with.
    *
    * @return true if the object passed in is a SimplePrincipal with the same name.
    */
   public boolean equals(Object obj) {
      if (obj instanceof SimplePrincipal) {
         return name.equals(((SimplePrincipal)obj).getName());
      }
      return false;
   }

   /**
    * Returns a string representation of this principal.
    *
    * @return a string representation of this principal.
    */
   public String toString() {
      return "SimplePrincipal[name = \'" + name + "\']";
   }

   /**
    * Returns a hashcode for this principal.
    *
    * @return a hashcode for this principal.
    */
   public int hashCode() {
      return name.hashCode();
   }
}

// ----------------------------------------------------------------------------
// EOF
