/*
 * Subversion Infos:
 * $URL$
 * $Author$
 * $Date$
 * $Rev$
*/

/* Copyright (C) 2005-2007 Jamie Angus Band 
 * MailArchiva Open Source Edition Copyright (c) 2005-2007 Jamie Angus Band
 * This program is free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation; either version
 * 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see http://www.gnu.org/licenses or write to the Free Software Foundation,Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

package com.stimulus.util;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.Writer;

/**
 * Writes to a wrapped Writer class, ensuring that all line separators are '\r\n', regardless
 * of platform.
 */
public class InternetPrintWriter
    extends PrintWriter implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -2299789527868929096L;

	/**
     * The line separator to use.
     */
    private static String lineSeparator = "\r\n";

    /**
     * Whether the Writer autoflushes on line feeds
     */
    private final boolean autoFlush;

    /**
     * Constructor that takes a writer to wrap.
     *
     * @param out the wrapped Writer
     */
    public InternetPrintWriter (Writer out) {
        super (out);
        autoFlush = false;
    }

    /**
     * Constructor that takes a writer to wrap.
     *
     * @param out the wrapped Writer
     * @param autoFlush whether to flush after each print call
     */
    public InternetPrintWriter (Writer out, boolean autoFlush) {
        super (out, autoFlush);
        this.autoFlush = autoFlush;
    }

    /**
     * Constructor that takes a stream to wrap.
     *
     * @param out the wrapped OutputStream
     */
    public InternetPrintWriter (OutputStream out) {
        super (out);
        autoFlush = false;
    }

    /**
     * Constructor that takes a stream to wrap.
     *
     * @param out the wrapped OutputStream
     * @param autoFlush whether to flush after each print call
     */
    public InternetPrintWriter (OutputStream out, boolean autoFlush) {
        super (out, autoFlush);
        this.autoFlush = autoFlush;
    }

    /**
     * Print a line separator.
     */
    public void println () {
        synchronized (lock) {
            write(lineSeparator);
            if (autoFlush) {
                flush();
            }
        }
    }

    /**
     * Print a boolean followed by a line separator.
     *
     * @param x the boolean to print
     */
    public void println(boolean x) {
        synchronized (lock) {
            print(x);
            println();
        }
    }

    /**
     * Print a char followed by a line separator.
     *
     * @param x the char to print
     */
    public void println(char x) {
        synchronized (lock) {
            print (x);
            println ();
        }
    }

    /**
     * Print a int followed by a line separator.
     *
     * @param x the int to print
     */
    public void println (int x) {
        synchronized (lock) {
            print (x);
            println ();
        }
    }

    /**
     * Print a long followed by a line separator.
     *
     * @param x the long to print
     */
    public void println (long x) {
        synchronized (lock) {
            print (x);
            println ();
        }
    }

    /**
     * Print a float followed by a line separator.
     *
     * @param x the float to print
     */
    public void println (float x) {
        synchronized (lock) {
            print (x);
            println ();
        }
    }

    /**
     * Print a double followed by a line separator.
     *
     * @param x the double to print
     */
    public void println (double x) {
        synchronized (lock) {
            print (x);
            println ();
        }
    }

    /**
     * Print a character array followed by a line separator.
     *
     * @param x the character array to print
     */
    public void println (char[] x) {
        synchronized (lock) {
            print (x);
            println ();
        }
    }

    /**
     * Print a String followed by a line separator.
     *
     * @param x the String to print
     */
    public void println (String x) {
        synchronized (lock) {
            print (x);
            println ();
        }
    }

    /**
     * Print an Object followed by a line separator.
     *
     * @param x the Object to print
     */
    public void println (Object x) {
        synchronized (lock) {
            print (x);
            println ();
        }
    }
}
