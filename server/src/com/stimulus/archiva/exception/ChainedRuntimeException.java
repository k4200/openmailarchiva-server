/* Copyright (C) 2005 Jamie Angus Band 
 * This software program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.stimulus.archiva.exception;

import org.apache.log4j.Logger;

public class ChainedRuntimeException extends RuntimeException {
  private Throwable cause = null;
  protected static Logger logger = Logger.getLogger(ChainedRuntimeException.class.getName());

  public ChainedRuntimeException() {
    super();
  }

  public ChainedRuntimeException(String message, Logger logger) {
    super(message);
    logger.fatal(message);
  }

  public ChainedRuntimeException(String message, Throwable cause, Logger logger) {
    super(message);
    this.cause = cause;
    logger.fatal(message,cause);
  }

  public Throwable getCause() {
    return cause;
  }

  public void printStackTrace() {
    super.printStackTrace();
    logger.fatal(cause);
  }

  public void printStackTrace(java.io.PrintStream ps) {
    super.printStackTrace(ps);
    logger.fatal(cause);
  }

  public void printStackTrace(java.io.PrintWriter pw) {
    super.printStackTrace(pw);
    logger.fatal(cause);
  }
}
