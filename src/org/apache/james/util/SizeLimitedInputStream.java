package org.apache.james.util;
/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

import org.apache.commons.logging.*;

import java.io.IOException;
import java.io.InputStream;

import com.stimulus.archiva.exception.MaxMessageSizeException;
/** 
  * Wraps an underlying input stream, limiting the allowable size
  * of incoming data. The size limit is configured in the conf file,
  * and when the limit is reached, a MessageSizeException is thrown.
  */
public class SizeLimitedInputStream extends InputStream {
	 protected static Log logger = LogFactory.getLog(SizeLimitedInputStream.class.getName());
	/**
     * Maximum number of bytes to read.
     */
    private long maxmessagesize = 0;
    /**
     * Running total of bytes read from wrapped stream.
     */
    private long bytesread = 0;

    /**
     * InputStream that will be wrapped.
     */
    private InputStream in = null;

    /**
     * Constructor for the stream. Wraps an underlying stream.
     * @param in InputStream to use as basis for new Stream.
     * @param maxmessagesize Message size limit, in Kilobytes
     */
    public SizeLimitedInputStream(InputStream in, long maxmessagesize) {
        this.in = in;
        this.maxmessagesize = maxmessagesize;
    }

    /**
     * Overrides the read method of InputStream to call the read() method of the
     * wrapped input stream.
     * @throws IOException Throws a MessageSizeException, which is a sub-type of IOException
     * @return Returns the number of bytes read.
     */
    public int read(byte[] b, int off, int len) throws IOException {
        int l = in.read(b, off, len);

        bytesread += l;

        if (maxmessagesize > 0 && bytesread > maxmessagesize) {
            throw new MaxMessageSizeException("message size exceeded {maxmessagesize='"+maxmessagesize+"'}",logger);
        }

        return l;
    }

    /**
     * Overrides the read method of InputStream to call the read() method of the
     * wrapped input stream.
     * @throws IOException Throws a MessageSizeException, which is a sub-type of IOException.
     * @return Returns the int character value of the byte read.
     */
    public int read() throws IOException {
        if (maxmessagesize > 0 && bytesread <= maxmessagesize) {
            bytesread++;
            return in.read();
        } else {
        	 throw new MaxMessageSizeException("message size exceeded {maxmessagesize='"+maxmessagesize+"'}",logger);
        }
    }
}
