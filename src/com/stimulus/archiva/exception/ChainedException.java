
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

public class ChainedException extends Exception implements Serializable {


  public enum Level { DEBUG, WARN, INFO, FATAL, TRACE, ERROR };

  private static final long serialVersionUID = 7945849551205785581L;
  private Throwable cause = null;

  protected static Log logger = LogFactory.getLog(ChainedException.class.getName());

  public ChainedException() {
    super();
  }

  public ChainedException(String message) {
    super(message);
  }

  public ChainedException(String message, Throwable cause) {

    super(message);
    this.cause = cause;
  }

  public ChainedException(String message, Log logger, ChainedException.Level level) {
    super(message);
    logger.debug(message,cause);
    log(level, message);
  }

  public ChainedException(String message, Throwable cause, Log logger,ChainedException.Level level) {
    super(message);
    this.cause = cause;
    logger.debug(message,cause);
    log(level, message);
  }

  protected static void log(Level level, String message) {
	 if (level==Level.DEBUG) {
		  logger.debug(message);
	  } else if (level==Level.ERROR) {
		  logger.error(message);
	  } else if (level==Level.FATAL) {
		  logger.fatal(message);
	  } else if (level==Level.INFO) {
		  logger.info(message);
	  } else if (level==Level.TRACE) {
		  logger.trace(message);
	  } else if (level==Level.WARN) {
		  logger.warn(message);
	  }
  }

  public static Level getLoggingLevel(Log logger) {
	  if (logger.isDebugEnabled())
		  return Level.DEBUG;
	  else if (logger.isInfoEnabled())
		  return Level.INFO;
	  else if (logger.isWarnEnabled())
		  return Level.WARN;
	  else if (logger.isErrorEnabled())
	  	  return Level.ERROR;
	  else if (logger.isFatalEnabled())
		  return Level.FATAL;

	  return Level.DEBUG;
  }

  @Override
public Throwable getCause() {
    return cause;
  }

  @Override
public void printStackTrace() {
    super.printStackTrace();
    logger.debug(cause);
  }

  @Override
public void printStackTrace(java.io.PrintStream ps) {
    super.printStackTrace(ps);
    logger.debug(cause);
  }

  @Override
public void printStackTrace(java.io.PrintWriter pw) {
    super.printStackTrace(pw);
    logger.debug(cause);
  }

}

