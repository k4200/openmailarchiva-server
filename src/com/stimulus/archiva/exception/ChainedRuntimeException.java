
/* Copyright (C) 2005-2009 Jamie Angus Band
 * MailArchiva Open Source Edition Copyright (c) 2005-2009 Jamie Angus Band
 * This program is free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation; either version
 * 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see http://www.gnu.org/licenses or write to the Free Software Foundation,Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */


package com.stimulus.archiva.exception;

import java.io.Serializable;

import org.apache.commons.logging.*;

public class ChainedRuntimeException extends RuntimeException implements Serializable  {

	private static final long serialVersionUID = -8279414563840852024L;
	private Throwable cause = null;
  protected static Log logger = LogFactory.getLog(ChainedRuntimeException.class.getName());

  public ChainedRuntimeException() {
    super();
  }

  public ChainedRuntimeException(String message, Log logger) {
    super(message);
    logger.fatal(message);
  }

  public ChainedRuntimeException(String message, Throwable cause, Log logger) {
    super(message);
    this.cause = cause;
    logger.fatal(message,cause);
  }

  @Override
public Throwable getCause() {
    return cause;
  }

  @Override
public void printStackTrace() {
    super.printStackTrace();
    logger.fatal(cause);
  }

  @Override
public void printStackTrace(java.io.PrintStream ps) {
    super.printStackTrace(ps);
    logger.fatal(cause);
  }

  @Override
public void printStackTrace(java.io.PrintWriter pw) {
    super.printStackTrace(pw);
    logger.fatal(cause);
  }

}
