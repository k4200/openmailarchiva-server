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

package com.stimulus.archiva.monitor;

import java.text.DateFormat;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.stimulus.archiva.domain.Config;
public class Event implements Comparable<Event> {

	  public enum Category { EXCEPTION, SPACE, MAILFLOW, VOLSTATUS, LICENSE, NOARCHIVE, SIGNATURE, UPDATE }
	  public enum Priority { LOW, NORMAL, HIGH };

	  protected Priority priority = null;
	  protected String message = null;
	  protected Date time = null;
	  protected Category category = null;
	  protected static final Log logger = LogFactory.getLog(Event.class);
	  protected static DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
	  protected int id = 0;

	  public static final int OUT_OF_MEMORY = 5;
	  public static final int VOLUME_INDEX_STORAGE_LOW = 6;
	  public static final int VOLUME_INDEX_THRESHOLD_REACHED = 7;
	  public static final int VOLUME_STORE_STORAGE_LOW = 8;
	  public static final int VOLUME_STORE_THRESHOLD_REACHED = 9;
	  public static final int VOLUME_ACTIVATED = 10;
	  public static final int VOLUME_CLOSED_NO_SPACE = 11;
	  public static final int NO_VOLUMES_AVAILABLE_USAGE = 12;
	  public static final int ARCHIVA_EXCEPTION = 13;
	  public static final int MAX_MESSAGE_SIZE_EXCEEDED = 14;
	  public static final int FAILED_COPY_MESSAGE_NO_ARCHIVE = 15;
	  public static final int LICENSE_INVALID = 16;
	  public static final int LICENSE_NEAR_EXPIRY = 17;
	  public static final int LICENSE_MAILBOX_QUOTA_NEAR_DEPLETION = 18;
	  public static final int FAILED_TO_ARCHIVE_ENCRYPTION_PASSWORD_NOT_SET = 19;
	  public static final int FAILED_TO_ARCHIVE_ACTIVE_VOLUME_EJECTED = 20;
	  public static final int FAILED_TO_ARCHIVE_NO_WRITE_STORAGE = 21;
	  public static final int VOLUME_NOT_ACCESSIBLE_EJECTED = 22;
	  public static final int VOLUME_ACCESSIBLE_INSERTED = 23;
	  public static final int VOLUME_SIGNATURE_VERIFY_FAILED = 24;
	  public static final int VOLUME_SIGNATURE_VERIFY_OK = 25;
	  public static final int MANIFEST_ENTRY_VERIFY_FAILED = 26;
	  public static final int MANIFEST_SIGNED = 27;
	  public static final int VOLUME_SIGNATURE_ORPHAN = 28;
	  public static final int VOLUME_REINDEX_COMPLETE = 29;
	  public static final int VOLUME_REINDEX_ERROR = 30;
	  public static final int SIGNING_CERTIFICATE_NOT_VALID = 31;
	  public static final int VOLUME_NO_CERTIFICATE_SELECTED = 32;
	  public static final int METAFEST_FAILED_VERIFICATION = 33;
	  public static final int UPDATES_AVAILABLE = 34;
	  public static final int SUPPORT_CONTRACT_EXPIRED = 35;
	  public static final int COULD_NOT_SAVE_VOL_INFO = 36;
	  public static final int NO_INDEX_READ_WRITE_PERMISSIONS = 37;
	  public static final int NO_STORE_READ_WRITE_PERMISSIONS = 38;
	  public static final int NO_CREATE_INDEX_DIR_PERMISSION = 39;
	  public static final int NO_CREATE_STORE_DIR_PERMISSION = 40;

	 public Event(int id, String message,Category category, Priority priority) {
		  this.message = message;
		  time = new Date();
		  this.category = category;
		  this.id = id;
	  }

	  public int getID() { return id; }

	  public String getMessage() { return message; }

	  public Date getTime() { return time; }

	  public Category getCategory() { return category; }

	  public Priority getPriority() { return priority; }

	  public int compareTo(Event e) throws ClassCastException {
		  return (getTime().compareTo(e.getTime()));
	  }

}