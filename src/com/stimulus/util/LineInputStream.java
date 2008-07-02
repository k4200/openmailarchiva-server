package com.stimulus.util;

/**
* The contents of this file are subject to the terms 
* of the Common Development and Distribution License 
* (the "License").  You may not use this file except 
* in compliance with the License.
* 
* You can obtain a copy of the license at 
* glassfish/bootstrap/legal/CDDLv1.0.txt or 
* https://glassfish.dev.java.net/public/CDDLv1.0.html. 
* See the License for the specific language governing 
* permissions and limitations under the License.
* 
* When distributing Covered Code, include this CDDL 
* HEADER in each file and include the License file at 
* glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
* add the following below this CDDL HEADER, with the 
* fields enclosed by brackets "[]" replaced with your 
* own identifying information: Portions Copyright [yyyy] 
* [name of copyright owner]
*/

/*
* @(#)LineInputStream.java	1.8 05/08/29
*
* Copyright 1997-2005 Sun Microsystems, Inc. All Rights Reserved.
*/

import java.io.*;

/**
* This class is to support reading CRLF terminated lines that
* contain only US-ASCII characters from an input stream. Provides
* functionality that is similar to the deprecated 
* <code>DataInputStream.readLine()</code>. Expected use is to read
* lines as String objects from a RFC822 stream.
*
* It is implemented as a FilterInputStream, so one can just wrap 
* this class around any input stream and read bytes from this filter.
* 
* @author John Mani
*/

public class LineInputStream extends FilterInputStream {

   private char[] lineBuffer = null; // reusable byte buffer

   public LineInputStream(InputStream in) {
	super(in);
   }

   /**
    * Read a line containing only ASCII characters from the input 
    * stream. A line is terminated by a CR or NL or CR-NL sequence.
    * A common error is a CR-CR-NL sequence, which will also terminate
    * a line.
    * The line terminator is not returned as part of the returned 
    * String. Returns null if no data is available. <p>
    *
    * This class is similar to the deprecated 
    * <code>DataInputStream.readLine()</code>
    */
   public String readLine() throws IOException {
	InputStream in = this.in;
	char[] buf = lineBuffer;

	if (buf == null)
	    buf = lineBuffer = new char[128];

	int c1;
	int room = buf.length;
	int offset = 0;

	while ((c1 = in.read()) != -1) {
	    if (c1 == '\n') // Got NL, outa here.
		break;
	    else if (c1 == '\r') {
		// Got CR, is the next char NL ?
		int c2 = in.read();
		if (c2 == '\r')		// discard extraneous CR
		    c2 = in.read();
		if (c2 != '\n') {
		    // If not NL, push it back
		    if (!(in instanceof PushbackInputStream))
			in = this.in = new PushbackInputStream(in);
		    ((PushbackInputStream)in).unread(c2);
		}
		break; // outa here.
	    }

	    // Not CR, NL or CR-NL ...
	    // .. Insert the byte into our byte buffer
	    if (--room < 0) { // No room, need to grow.
		buf = new char[offset + 128];
		room = buf.length - offset - 1;
		System.arraycopy(lineBuffer, 0, buf, 0, offset);
		lineBuffer = buf;
	    }
	    buf[offset++] = (char)c1;
	}

	if ((c1 == -1) && (offset == 0))
	    return null;
	
	return String.copyValueOf(buf, 0, offset);
   }
}