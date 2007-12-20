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

package com.stimulus.archiva.security.realm;

public interface Constants {

   // shared constants
   public static final String COMMON_TITLE_BASE = "MailArchiva: ";
   public static final String VALID_USERNAME = "username";
   public static final String VALID_PASSWORD = "password";
   public static final String VALID_USERNAME2 = "username2";
   public static final String VALID_PASSWORD2 = "password2";
   public static final String VALID_ROLE = "inthisrole";

   // home page constants
   public static final String HOME_TITLE = COMMON_TITLE_BASE + "Home";
   public static final String HOME_FORM_ID = "homeForm";
   public static final String HOME_POST_FIELD = "postMe";

   // login form constants
   public static final String LOGIN_TITLE = COMMON_TITLE_BASE + "Login Page";
   public static final String LOGIN_FORM_ID = "loginForm";
   public static final String LOGIN_FORM_ACTION = "j_security_check";
   public static final String LOGIN_USERNAME_FIELD = "j_username";
   public static final String LOGIN_PASSWORD_FIELD = "j_password";
   public static final String LOGIN_REMEMBERME_FIELD = "j_rememberme";

   // secure page constants
   public static final String SECURE_TITLE = COMMON_TITLE_BASE + "Secure Page";
   public static final String SECURE_POSTED_VALUE_FORM = "postedValueForm";
   public static final String SECURE_POSTED_VALUE_FIELD = "postedValue";
   public static final String SECURE_LAST_POSTED_VALUE_FIELD = "lastPostedValue";

   // logout page constants
   public static final String LOGOUT_TITLE = COMMON_TITLE_BASE + "Logout";
}
