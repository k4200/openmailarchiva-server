
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

package com.stimulus.archiva.exception;

import org.apache.log4j.Logger;

import com.stimulus.archiva.domain.Email;




public class ChainedException extends Exception {
  private Throwable cause = null;
  protected static Logger logger = Logger.getLogger(ChainedException.class.getName());
  
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

  public ChainedException(String message, Logger logger) {
    super(message);
    logger.debug(message,cause);
    logger.error(message); 
  }

  public ChainedException(String message, Throwable cause, Logger logger) {
    super(message);
    this.cause = cause;
    logger.debug(message,cause);
    logger.error(message); 
  }

  public Throwable getCause() {
    return cause;
  }

  public void printStackTrace() {
    super.printStackTrace();
    logger.debug(cause);
  }

  public void printStackTrace(java.io.PrintStream ps) {
    super.printStackTrace(ps);
    logger.debug(cause);
  }

  public void printStackTrace(java.io.PrintWriter pw) {
    super.printStackTrace(pw);
    logger.debug(cause);
  }
}