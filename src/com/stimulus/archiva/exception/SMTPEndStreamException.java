
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

import java.io.Serializable;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class SMTPEndStreamException extends SMTPServerException implements Serializable {
  
  /**
	 * 
	 */
	private static final long serialVersionUID = 5897406374158245888L;

public SMTPEndStreamException(String message,Logger logger) {
    super(message, logger);
  }

  public SMTPEndStreamException(String message, Throwable cause, Logger logger) {
    super(message, cause, logger);
  }
  
  public SMTPEndStreamException(String message, Logger logger, Level level) {
      super(message, logger,level);
  }

  public SMTPEndStreamException(String message, Throwable cause, Logger logger, Level level) {
      super(message,cause, logger,level);
  }
  
}
