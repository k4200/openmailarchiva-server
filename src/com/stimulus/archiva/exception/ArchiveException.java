
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

import org.apache.commons.logging.*;

public class ArchiveException extends ChainedException implements Serializable {

  private static final long serialVersionUID = 878237372698342322L;

  public static enum RecoveryDirective { ACCEPT, REJECT, RETRYLATER };
  
  protected RecoveryDirective recovery;
  
  public ArchiveException(String message, RecoveryDirective recovery) {
      super(message);
      this.recovery = recovery;
  }
  
  public ArchiveException(String message, Log logger, ChainedException.Level level, RecoveryDirective recovery) {
      super(message, logger,level);
  }

  public ArchiveException(String message, Throwable cause, Log logger, ChainedException.Level level, RecoveryDirective recovery) {
      super(message,cause, logger,level);
      this.recovery = recovery;
  }
  
  public ArchiveException(String message, Log logger, RecoveryDirective recovery) {
      super(message, logger,Level.ERROR);
      this.recovery = recovery;
  }

  public ArchiveException(String message, Throwable cause, Log logger, RecoveryDirective recovery) {
      super(message,cause, logger,Level.ERROR);
      this.recovery = recovery;
  }
  
  public RecoveryDirective getRecoveryDirective() {
	  return recovery;
  }

}
