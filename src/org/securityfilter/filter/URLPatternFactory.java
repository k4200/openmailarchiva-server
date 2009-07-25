/*
 * $Header: /cvsroot/securityfilter/securityfilter/src/share/org/securityfilter/filter/URLPatternFactory.java,v 1.3 2004/01/26 09:30:07 maxcooper Exp $
 * $Revision: 1.3 $
 * $Date: 2004/01/26 09:30:07 $
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

import org.apache.oro.text.regex.*;
import org.securityfilter.config.*;

/**
 * URLPatternFactory creates URLPattern instances. It keeps a Perl5PatternCompiler to use
 * for the creation of a set of instances.
 *
 * @author Max Cooper (max@maxcooper.com)
 * @version $Revision: 1.3 $ $Date: 2004/01/26 09:30:07 $
 */
public class URLPatternFactory {
   protected PatternCompiler compiler;

   /**
    * Constructor
    */
   public URLPatternFactory() {
      compiler = new Perl5Compiler();
   }

   /**
    * Create a URLPatternMatcher object that is compatible with the URLPattern
    * objects created by this Facotry class.
    *
    * @return a URLPatternMatcher object compatible with the URLPatterns created by this class
    */
   public URLPatternMatcher createURLPatternMatcher() {
      return new URLPatternMatcher();
   }

   /**
    * Create a URLPattern instance.
    *
    * @param pattern url pattern in config file syntax
    * @param constraint SecurityConstraint object to associate with this pattern
    * @param resourceCollection WebResourceCollection to associate with this pattern
    * @param order order in which this pattern appeared in the config file
    *
    * @exception Exception
    */
   public URLPattern createURLPattern(
      String pattern,
      SecurityConstraint constraint,
      WebResourceCollection resourceCollection,
      int order
   ) throws Exception {
      return new URLPattern(pattern, constraint, resourceCollection, order, compiler);
   }
}

// ----------------------------------------------------------------------------
// EOF
