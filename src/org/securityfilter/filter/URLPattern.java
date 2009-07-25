/*
 * $Header: /cvsroot/securityfilter/securityfilter/src/share/org/securityfilter/filter/URLPattern.java,v 1.7 2007/11/05 18:46:22 chris_schultz Exp $
 * $Revision: 1.7 $
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

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;

import org.securityfilter.config.SecurityConstraint;
import org.securityfilter.config.WebResourceCollection;

/**
 * URLPattern - Contains matchable URL pattern and the associated
 * SecurityConstraint and WebResourceCollection objects for the pattern.
 * Also supports sorting according to the Servlet Spec v2.3.
 *
 * @author Max Cooper (max@maxcooper.com)
 * @version $Revision: 1.7 $ $Date: 2007/11/05 18:46:22 $
 */
public class URLPattern
    implements Comparable
{
    /**
     * Pattern type for patterns that do not meet the specifications for the
     * other pattern types.
     */
    public static final int EXACT_TYPE = 1;
    /**
     * Pattern type for PATH_TYPE mappings. Starts with '/' and ends with '/*'.
     */
    public static final int PATH_TYPE = 2;
    /**
     * Pattern type for EXTENSION_TYPE mappings. Starts with '*.'
     */
    public static final int EXTENSION_TYPE = 3;
    /**
     * Pattern type for EXTENSION_TYPE mappings. Starts with '*.'
     */
    public static final int DEFAULT_TYPE = 4;

    protected String pattern;
    protected String convertedPattern;
    protected Pattern compiledPattern;
    protected SecurityConstraint constraint;
    protected WebResourceCollection resourceCollection;
    protected int order;
    protected int patternType;
    protected int pathLength;

    /**
     * Construct a new URLPattern object.
     *
     * @param pattern the url pattern to match
     * @param constraint the SecurityConstraint associated with this pattern
     * @param resourceCollection the WebResourceCollection associated with this pattern
     * @param order the order in which this pattern occurred in the configuration file
     * @param compiler a PatternCompiler to use to compile this url pattern
     *
     * @see URLPatternFactory
     */
    public URLPattern(
                      String pattern,
                      SecurityConstraint constraint,
                      WebResourceCollection resourceCollection,
                      int order,
                      PatternCompiler compiler
                      )
        throws MalformedPatternException
    {
        this.pattern = pattern;
        this.constraint = constraint;
        this.resourceCollection = resourceCollection;
        this.order = order;
        initPatternType();
        initPathLength();
        initConvertedPattern();
        initCompiledPattern(compiler);
    }

    /**
     * Get the url pattern to match.
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Get the compiled version of this pattern.
     *
     * @return compiled version of this pattern
     */
    public Pattern getCompiledPattern() {
        return compiledPattern;
    }

    /**
     * Get the pattern type. The pattern type will be determined on the first call to this method.
     *
     * @return EXACT, PATH, or EXTENSION
     */
    public int getPatternType() {
        return patternType;
    }

    /**
     * Get the path length of the pattern. This is only valid when getPatternType() = PATH.<p>
     * Examples:
     * <ul>
     *    <li>/* = 0</li>
     *    <li>/path/* = 1</li>
     *    <li>/really/long/path/* = 3</li>
     * </ul>
     *
     * @return path length of this pattern
     */
    public int getPathLength() {
        return pathLength;
    }

    /**
     * Get the SecurityConstraint object associated with this pattern.
     */
    public SecurityConstraint getSecurityConstraint() {
        return constraint;
    }

    /**
     * Get the order value for this pattern (the order in which it appeared in the config file).
     */
    public int getOrder() {
        return order;
    }

    /**
     * Get the WebResourceCollection associated with this pattern.
     */
    public WebResourceCollection getWebResourceCollection() {
        return resourceCollection;
    }

    /**
     * Initialize the patternType protected member.
     */
    protected void initPatternType() {
        if ("/".equals(pattern)) {
            patternType = DEFAULT_TYPE;
        } else if (pattern.startsWith("*.")) {
            patternType = EXTENSION_TYPE;
        } else if (pattern.startsWith("/") && pattern.endsWith("/*")) {
            patternType = PATH_TYPE;
        } else {
            patternType = EXACT_TYPE;
        }
    }

    /**
     * Initialize the pathLength protected member.
     */
    protected void initPathLength() {
        pathLength = -1;
        int pos = pattern.indexOf('/');
        while (pos != -1) {
            pathLength++;
            pos = pattern.indexOf('/', pos + 1);
        }
    }

    /**
     * Initialize the convertedPattern protected member.
     */
    protected void initConvertedPattern() {
        if (patternType == DEFAULT_TYPE) {
            // match anything for default pattern
            convertedPattern = ".*";
        } else {
            StringBuffer buf = new StringBuffer(pattern);
            int pos;
            // escape '.' characters
            pos = buf.toString().indexOf('.');
            while (pos != -1) {
                buf.insert(pos, "\\");
                pos = buf.toString().indexOf('.', pos + 2);
            }
            // replace '*' chars in the compiledPattern with '.*'
            pos = buf.toString().indexOf('*');
            while (pos != -1) {
                buf.replace(pos, pos + 1, ".*");
                pos = buf.toString().indexOf('*', pos + 2);
            }
            // replace '/' chars with '/+' to match one or more consecutive slashes
            // the spec hints that containers are supposed to normalize the extra slashes out,
            // but testing revealed that sometimes the extra slashes are not normalized out
            pos = buf.toString().indexOf('/');
            while (pos != -1) {
                buf.replace(pos, pos + 1, "/+");
                pos = buf.toString().indexOf('/', pos + 2);
            }
            // adjustments for the different expression types
            switch (patternType) {
                case PATH_TYPE:
                    // make sure it matches from the start of the string
                    buf.insert(0, '^');
                    // make sure /foo/* matches /foo and /foo/morestuff, but not /foobar
                    buf.insert(buf.length()-4, "(");
                    buf.append(")?$");
                    break;
                case EXTENSION_TYPE:
                    buf.append('$');
                    break;
                case EXACT_TYPE:
                    buf.insert(0, '^');
                    buf.append('$');
                    break;
            }
            convertedPattern = buf.toString();
        }
    }

    /**
     * Initialize the compiledPattern protected member.
     *
     * @param compiler
     *
     * @throws MalformedPatternException If the current pattern has errors.
     */
    protected void initCompiledPattern(PatternCompiler compiler)
        throws MalformedPatternException
    {
        compiledPattern = compiler.compile(convertedPattern, Perl5Compiler.READ_ONLY_MASK);
    }

    /**
     * Test if this pattern is equivalent to another pattern.
     * This is implemented so that consistency with the compareTo method results can be maintained.
     *
     * @param obj the value to test equivalence with
     * @return true if the passed object is an equivalent URLPattern, false if it is not a URLPattern
     * or if it is not equivalent.
     */
    public boolean equals(Object obj) {
        if (obj instanceof URLPattern) {
            URLPattern otherPattern = (URLPattern) obj;
            return (
                    constraint.equals(otherPattern.getSecurityConstraint())
                    && resourceCollection.equals(otherPattern.getWebResourceCollection())
                    && pattern.equals(otherPattern.getPattern())
                    );
        }
        return false;
    }

    /**
     * Compares this URLPattern to obj to support sorting.<p>
     *
     * The sort order is dictated by the servlet spec. The ordering by type is:
     *    EXACT_TYPE
     *    PATH_TYPE
     *    EXTENTION_TYPE
     *    DEFAULT_TYPE
     * Ordering among PATH_TYPE patterns is determined by path length, with the
     * longer path coming first. If the path lengths are the same, or both patterns
     * are of the same type other than PATH_TYPE, ordering is determined by the order
     * in which the pattern appeared in the config file.
     *
     * Thanks to Chris Nokleberg for contributing code for this method.
     *
     * @param obj another URLPattern to compare to
     *
     * @return a negative integer, zero, or a positive integer as this object is
     * less than, equal to, or greater than the specified object.
     *
     * @exception ClassCastException thrown if obj is not a URLPattern instance
     */
    public int compareTo(Object obj) throws ClassCastException {
        URLPattern other = (URLPattern) obj;
        // return 0 if the other pattern is equivalent to this one
        if (this.equals(other)) {
            return 0;
        }
        int c = patternType - other.patternType;
        if (c == 0) {
            switch (patternType) {
                case PATH_TYPE:
                    c = other.pathLength - pathLength;
                    if (c != 0) {
                        break;
                    }
                    /* fall through */
                case EXACT_TYPE:
                    /* fall through */
                case EXTENSION_TYPE:
                    /* fall through */
                case DEFAULT_TYPE:
                    c = order - other.order;
            }
        }
        return c;
    }
}

// ----------------------------------------------------------------------------
// EOF
