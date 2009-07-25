/*
 * $Header: /cvsroot/securityfilter/securityfilter/src/share/org/securityfilter/filter/URLPatternMatcher.java,v 1.5 2007/11/05 18:46:22 chris_schultz Exp $
 * $Revision: 1.5 $
 * $Date: 2007/11/05 18:46:22 $
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

import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Matcher;

import java.util.Collection;

/**
 * URLPatternMatcher - A non-thread safe object to be used to match a request
 * pattern with URLPattern objects.
 *
 * @author Max Cooper (max@maxcooper.com)
 * @version $Revision: 1.5 $ $Date: 2007/11/05 18:46:22 $
 */
public class URLPatternMatcher
{
    private PatternMatcher patternMatcher;

    /**
     * Constructor
     */
    public URLPatternMatcher() {
        patternMatcher = new Perl5Matcher();
    }

    /**
     * Test to see if a string pattern matches a URLPattern.
     *
     * @param pattern a String pattern to check for a match
     * @param urlPattern a URLPattern object to match against
     * @return true if the pattern matched the urlPattern, false otherwise
     */
    public boolean match(String pattern, URLPattern urlPattern)
    {
        return patternMatcher.matches(pattern, urlPattern.getCompiledPattern());
    }

    /**
     * Test to see if a string pattern and HTTP method matches a URLPattern.
     *
     * @param pattern a String pattern to check for a match
     * @param httpMethod an HTTP pattern to check for a match
     * @param urlPattern a URLPattern object to match against
     * @return true if the pattern matched the urlPattern, false otherwise
     */
    public boolean match(String pattern, String httpMethod, URLPattern urlPattern)
    {
        if (match(pattern, urlPattern)) {
            Collection methods = urlPattern.getWebResourceCollection().getHttpMethods();
            if (methods.isEmpty() || methods.contains(httpMethod.toUpperCase())) {
                return true;
            }
        }
        return false;
    }
}

// ----------------------------------------------------------------------------
// EOF
