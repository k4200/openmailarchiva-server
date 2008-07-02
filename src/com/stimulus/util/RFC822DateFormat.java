package com.stimulus.util;
/*
 * Created on Feb 9, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */

/**
 * @author david
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */

import java.io.Serializable;
import java.util.Date;
import javax.mail.internet.MailDateFormat;

/**
 * A thread safe wrapper for the <code>javax.mail.internet.MailDateFormat</code> class.
 *
 */
public class RFC822DateFormat extends SynchronizedDateFormat implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = -7819456681541726149L;
	/**
     * A static instance of the RFC822DateFormat, used by toString
     */
    private static RFC822DateFormat instance; 

    static {
        instance = new RFC822DateFormat();
    }

    /**
     * This static method allows us to format RFC822 dates without
     * explicitly instantiating an RFC822DateFormat object.
     *
     * @return java.lang.String
     * @param d Date
     *
     * @deprecated This method is not necessary and is preserved for API
     *             backwards compatibility.  Users of this class should
     *             instantiate an instance and use it as they would any
     *             other DateFormat object.
     */
    @Deprecated
	public static String toString(Date d) {
        return instance.format(d);
    }

    /**
     * Constructor for RFC822DateFormat
     */
    public RFC822DateFormat() {
        super(new MailDateFormat());
    }
}