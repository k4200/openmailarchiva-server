/*
 * $Header: /cvsroot/securityfilter/securityfilter/src/share/org/securityfilter/config/WebResourceCollection.java,v 1.5 2003/01/06 00:17:25 maxcooper Exp $
 * $Revision: 1.5 $
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

package org.securityfilter.config;

import java.util.*;

/**
 * WebResourceCollection represents a web-resource-collection from the security config file.
 * It has a list of url patterns, and a list of http methods.
 *
 * @author Max Cooper (max@maxcooper.com)
 * @version $Revision: 1.5 $ $Date: 2003/01/06 00:17:25 $
 */
public class WebResourceCollection {
   private List urlPatterns;
   private Collection httpMethods;

   /**
    * Constructor
    */
   public WebResourceCollection() {
      urlPatterns = new ArrayList();
      httpMethods = new HashSet();
   }

   /**
    * Add a url pattern to this WebResourceCollection.
    *
    * @param pattern url pattern to add
    */
   public void addURLPattern(String pattern) {
      urlPatterns.add(pattern);
   }

   /**
    * Get a list of url patterns in the order they were added to this WebResourceCollection.
    */
   public List getURLPatterns() {
      return Collections.unmodifiableList(urlPatterns);
   }

   /**
    * Add an http method to this WebResourceCollection.
    *
    * @param method http method to add
    */
   public void addHttpMethod(String method) {
      httpMethods.add(method.toUpperCase());
   }

   /**
    * Get a collection of http methods for this WebResourceCollection.
    */
   public Collection getHttpMethods() {
      return Collections.unmodifiableCollection(httpMethods);
   }
}

// ----------------------------------------------------------------------------
// EOF
