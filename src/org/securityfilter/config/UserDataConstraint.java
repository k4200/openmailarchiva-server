/*
 * $Header: /cvsroot/securityfilter/securityfilter/src/share/org/securityfilter/config/UserDataConstraint.java,v 1.1 2007/11/07 17:22:38 chris_schultz Exp $
 * $Revision: 1.1 $
 * $Date: 2007/11/07 17:22:38 $
 *
 * ====================================================================
 * The SecurityFilter Software License, Version 1.1
 *
 * (this license is derived and fully compatible with the Apache Software
 * License - see http://www.apache.org/LICENSE.txt)
 *
 * Copyright (c) 2007 SecurityFilter.org. All rights reserved.
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

/**
 * UserDataConstraint models the &lt;user-data-constraint&gt; element
 * in a web application's deployment descriptor.
 *
 * <pre>
 * &lt;user-data-constraint&gt;
 *    &lt;description^gt;This is the user data constraint.&lt;/description&gt;
 *    &lt;transport-guarantee&gt;
 *        <i><code>NONE</code>
 *        or <code>INTEGRAL</code>
 *        or <code>CONFIDENTIAL</code></i>
 *    &lt;/transport-guarantee^gt;
 * &gt;/user-data-constraint&gt;
 * </pre>
 * 
 * @author Chris Schultz (chris@christopherschultz.net)
 * @version $Revision: 1.1 $ $Date: 2007/11/07 17:22:38 $
 */
public class UserDataConstraint
{
    /**
     * Constant for transport guarantee that indicates no guarantees.
     *
     * @see #setTransportGuarantee(String)
     */
    public static final String TRANSPORT_GUARANTEE_NONE = "NONE";

    /**
     * Constant for transport guarantee that indicates data sent between
     * the client and server are sent in such a way that they cannot be changed
     * in transit.
     *
     * @see #setTransportGuarantee(String)
     */
    public static final String TRANSPORT_GUARANTEE_INTEGRAL = "INTEGRAL";

    /**
     * Constant for transport guarantee that indicates data sent between
     * the client and server are sent in such a way that they cannot be
     * observed by third-parties while in transit.
     *
     * @see #setTransportGuarantee(String)
     */
    public static final String TRANSPORT_GUARANTEE_CONFIDENTIAL = "CONFIDENTIAL";

    /**
     * The transport-guarantee for this UserDataConstraint.
     */
    private String _transportGuarantee = TRANSPORT_GUARANTEE_NONE;

    public UserDataConstraint()
    {
    }

    /**
     * Sets the transport-guarantee required by this UserDataConstraint.
     *
     * @param guarantee Valid values (case sensitive) are <code>NONE</code>,
     *                  <code>INTEGRAL</code>, and <code>CONFIDENTIAL</code>.
     *
     * @throws IllegalArgumentException If <code>guarantee</code> is neither
     *         <code>NONE</code> nor <code>INTEGRAL</code>
     *         nor <code>CONFIDENTIAL</code>.
     *
     * @see #getTransportGuarantee()
     * @see #TRANSPORT_GUARANTEE_NONE
     * @see #TRANSPORT_GUARANTEE_INTEGRAL
     * @see #TRANSPORT_GUARANTEE_CONFIDENTIAL
     */
    public void setTransportGuarantee(String guarantee)
        throws IllegalArgumentException
    {
        if(null == guarantee)
        {
            _transportGuarantee = null;
        }
        else
        {
            guarantee = guarantee.trim();

            if(!(TRANSPORT_GUARANTEE_NONE.equals(guarantee)
                 || TRANSPORT_GUARANTEE_INTEGRAL.equals(guarantee)
                 || TRANSPORT_GUARANTEE_CONFIDENTIAL.equals(guarantee)))
                throw new IllegalArgumentException("Unknown transport guarantee: " + guarantee);

            _transportGuarantee = guarantee;
        }
    }

    /**
     * Returns the transport guarantee for this UserDataConstraint.
     *
     * @see #setTransportGuarantee
     */
    public String getTransportGuarantee()
    {
        return _transportGuarantee;
    }
}

// ----------------------------------------------------------------------------
// EOF
