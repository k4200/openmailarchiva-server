/*
 * $Header: /cvsroot/securityfilter/securityfilter/src/share/org/securityfilter/authenticator/persistent/DefaultPersistentLoginManager.java,v 1.1 2004/01/26 09:23:01 maxcooper Exp $
 * $Revision: 1.1 $
 * $Date: 2004/01/26 09:23:01 $
 *
 * ====================================================================
 * The SecurityFilter Software License, Version 1.1
 *
 * (this license is derived and fully compatible with the Apache Software
 * License - see http://www.apache.org/LICENSE.txt)
 *
 * Copyright (c) 2002 SecurityFilter.org. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by
 *        SecurityFilter.org (http://www.securityfilter.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The name "SecurityFilter" must not be used to endorse or promote
 *    products derived from this software without prior written permission.
 *    For written permission, please contact license@securityfilter.org .
 *
 * 5. Products derived from this software may not be called "SecurityFilter",
 *    nor may "SecurityFilter" appear in their name, without prior written
 *    permission of SecurityFilter.org.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE SECURITY FILTER PROJECT OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 */

package org.securityfilter.authenticator.persistent;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.*;
import java.security.*;

/**
 * <code>DefaultPersistentLoginManager</code> manages the saving and retrieving of the
 * persistant logins for the "remember me" functionality.  This implementation uses
 * client cookies to save the login information and incroporates cookie validation
 * and encryption
 *
 * @author David Reed (dreed10@neo.rr.com)
 * @author Max Cooper (max@maxcooper.com)
 * @version $Revision: 1.1 $ $Date: 2004/01/26 09:23:01 $
 */
public class DefaultPersistentLoginManager implements Serializable, PersistentLoginManagerInterface {

   protected String cookieLife = "15";
   protected String protection = "all";
   protected String validationKey;
   protected String encryptionKey;
   protected String useIP = "true";
   protected String encryptionAlgorithm = "DES";
   protected String encryptionMode = "ECB";
   protected String encryptionPadding = "PKCS5Padding";
   protected String cipherParameters;

   protected String valueBeforeMD5 = "";
   protected String valueAfterMD5 = "";
   protected SecretKey secretKey;
   protected static final String COOKIE_USERNAME = "username";
   protected static final String COOKIE_PASSWORD = "password";
   protected static final String COOKIE_REMEMBERME = "rememberme";
   protected static final String COOKIE_VALIDATION = "validation";
   protected static final String PROTECTION_ALL = "all";
   protected static final String PROTECTION_VALIDATION = "validation";
   protected static final String PROTECTION_ENCRYPTION = "encryption";
   protected static final String PROTECTION_NONE = "none";

   /**
    * The constructor method initializes the cipherParameters variable in case the
    * implementation intends to use encryption to secure user names and passwords
    */
   public DefaultPersistentLoginManager() {
      initCypherParameters();
   }

   /**
    * Remember a specific login
    *
    * @param request the servlet request
    * @param response the servlet response
    * @param username the username tha's being remembered
    * @param password the password that's being remembered
    */
   public void rememberLogin(
      HttpServletRequest request,
      HttpServletResponse response,
      String username,
      String password
   ) throws IOException, ServletException {
      if (protection.equals(PROTECTION_ALL) || protection.equals(PROTECTION_ENCRYPTION)) {
         username = encryptText(username);
         password = encryptText(password);
         if (username == null || password == null) {
            System.out.println("ERROR!!");
            System.out.println("There was a problem encrypting the username or password!!");
            System.out.println("Remember Me function will be disabled!!");
            return;
         }
      }
      // create client cookie to store username and password
      Cookie usernameCookie = new Cookie(COOKIE_USERNAME, username);
      usernameCookie.setMaxAge(60 * 60 * 24 * Integer.parseInt(cookieLife));
      response.addCookie(usernameCookie);
      Cookie passwdCookie = new Cookie(COOKIE_PASSWORD, password);
      passwdCookie.setMaxAge(60 * 60 * 24 * Integer.parseInt(cookieLife));
      response.addCookie(passwdCookie);
      Cookie rememberCookie = new Cookie(COOKIE_REMEMBERME, "true");
      rememberCookie.setMaxAge(60 * 60 * 24 * Integer.parseInt(cookieLife));
      response.addCookie(rememberCookie);
      if (protection.equals(PROTECTION_ALL) || protection.equals(PROTECTION_VALIDATION)) {
         String validationHash = getValidationHash(username, password, request.getRemoteAddr());
         if (validationHash != null) {
            Cookie validationCookie = new Cookie(COOKIE_VALIDATION, validationHash);
            validationCookie.setMaxAge(60 * 60 * 24 * Integer.parseInt(cookieLife));
            response.addCookie(validationCookie);
         } else {
            System.out.println("WARNING!!! WARNING!!!");
            System.out.println("PROTECTION=ALL or PROTECTION=VALIDATION was specified");
            System.out.println("but Validation Hash could NOT be generated");
            System.out.println("Validation has been disabled!!!!");
         }
      }
      return;
   }

   /**
    * Forget a login
    *
    * @param request the servlet request
    * @param response the servlet response
    */
   public void forgetLogin(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
      removeCookie(request, response, COOKIE_USERNAME);
      removeCookie(request, response, COOKIE_PASSWORD);
      removeCookie(request, response, COOKIE_REMEMBERME);
      removeCookie(request, response, COOKIE_VALIDATION);
      return;
   }

   /**
    * Ask "Are we remembering logins"
    *
    * @param request the servlet request
    * @return true if login cookie was found
    */
   public boolean rememberingLogin(HttpServletRequest request) {
      if (getCookieValue(request.getCookies(), COOKIE_REMEMBERME, "false").equals("true")) {
         return true;
      } else {
         return false;
      }
   }

   /**
    * Get remembered username
    *
    * @param request the servlet request
    * @param response the servlet response
    * @return the username value or null if not found or a problem with security of cookie
    */
   public String getRememberedUsername(
      HttpServletRequest request,
      HttpServletResponse response
   ) throws IOException, ServletException {
      String username = getCookieValue(request.getCookies(), COOKIE_USERNAME, "false");
      String password = getCookieValue(request.getCookies(), COOKIE_PASSWORD, "false");

      String validationHash = getCookieValue(request.getCookies(), COOKIE_VALIDATION, "false");
      if (!username.equals("false")) {
         if (!validationHash.equals("false")) {
            //check hash
            String calculatedHash = getValidationHash(username, password, request.getRemoteAddr());
            if (validationHash.equals(calculatedHash)) {
               if (protection.equals(PROTECTION_ALL) || protection.equals(PROTECTION_ENCRYPTION)) {
                  username = decryptText(username);
               }
               return username;
            } else {
               System.out.println("!remember-me cookie validation hash mismatch! ");
               System.out.println("!remember-me cookie has been tampered with! ");
               System.out.println("!remember-me cookie is being deleted! ");
               removeCookie(request, response, COOKIE_USERNAME);
               removeCookie(request, response, COOKIE_PASSWORD);
               removeCookie(request, response, COOKIE_REMEMBERME);
               removeCookie(request, response, COOKIE_VALIDATION);
               return null;
            }
         } else {
            if (protection.equals(PROTECTION_ALL) || protection.equals(PROTECTION_ENCRYPTION)) {
               username = decryptText(username);
            }
            return username;
         }
      } else {
         return null;
      }
   }

   /**
    * Get remembered password
    *
    * @param request the servlet request
    * @param response the servlet response
    * @return the password value or null if not found or a problem with security of cookie
    */
   public String getRememberedPassword(
      HttpServletRequest request,
      HttpServletResponse response
   ) throws IOException, ServletException {
      String username = getCookieValue(request.getCookies(), COOKIE_USERNAME, "false");
      String password = getCookieValue(request.getCookies(), COOKIE_PASSWORD, "false");

      String validationHash = getCookieValue(request.getCookies(), COOKIE_VALIDATION, "false");
      if (!password.equals("false")) {
         if (!validationHash.equals("false")) {
            String calculatedHash = getValidationHash(username, password, request.getRemoteAddr());
            if (validationHash.equals(calculatedHash)) {
               if (protection.equals(PROTECTION_ALL) || protection.equals(PROTECTION_ENCRYPTION)) {
                  password = decryptText(password);
               }
               return password;
            } else {
               System.out.println("!remember-me cookie validation hash mismatch! ");
               System.out.println("!remember-me cookie has been tampered with! ");
               System.out.println("!remember-me cookie is being deleted! ");
               removeCookie(request, response, COOKIE_USERNAME);
               removeCookie(request, response, COOKIE_PASSWORD);
               removeCookie(request, response, COOKIE_REMEMBERME);
               removeCookie(request, response, COOKIE_VALIDATION);
               return null;
            }
         } else {
            if (protection.equals(PROTECTION_ALL) || protection.equals(PROTECTION_ENCRYPTION)) {
               password = decryptText(password);
            }
            return password;
         }
      } else {
         return null;
      }
   }

   /**
    * Set Cookie Life
    *
    * @param cookieLife The cookie life in days as defined in config file
    */
   public void setCookieLife(String cookieLife) {
      this.cookieLife = cookieLife;
   }

   /**
    * Set protection level for the "Remember Me" functionality
    *
    * @param protection The protection level as defined in config file. <br>
    *                   valid values are: <br>
    *                                    ALL (this is the default)<br>
    *                                    VALIDATION<br>
    *                                    ENCRYPTION<br>
    *                                    NONE<br>
    */
   public void setProtection(String protection) {
      if (
         protection.toLowerCase().trim().equals(PROTECTION_ALL)
         || protection.toLowerCase().trim().equals(PROTECTION_VALIDATION)
         || protection.toLowerCase().trim().equals(PROTECTION_ENCRYPTION)
         || protection.toLowerCase().trim().equals(PROTECTION_NONE)
      ) {
         this.protection = protection.toLowerCase().trim();
      } else {
         System.out.println("*ERROR - Invalid protection setting!!!" + protection);
         System.out.println("*ERROR - setting protection to default -->=" + PROTECTION_ALL);
         this.protection = PROTECTION_ALL;
      }
   }

   /**
    * Set the Validation Key used to generate hash value, the hash value is stored
    * with the cookie and used to verify that the cookie has not been tampered with
    *
    * @param validationkey The validation key as defined in config file.
    *                        This is a required config entry if protection is set
    *                        to ALL or VALIDATION
    */
   public void setValidationKey(String validationkey) {
      this.validationKey = validationkey;
   }

   /**
    * Set the Encryptin Key used to create a secret key, the secret key is passed
    * to the Cipher object to be used during encryption and decryption of cookie
    * values.
    * <p>
    * <i>NOTE: This entry in the config file must NOT appear before any of the other
    * encryption config entries</i>
    *
    * @param encryptionkey          A String containing the encryption key as
    *                               defined in config file. This is a required
    *                               config entry if protection is set to ALL or ENCRYPTION.
    */
   public void setEncryptionKey(String encryptionkey) {
      this.encryptionKey = encryptionkey;
      try {
         SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(encryptionAlgorithm);
         byte[] desKeyData = encryptionkey.getBytes();
         DESKeySpec desKeySpec = new DESKeySpec(desKeyData);
         secretKey = keyFactory.generateSecret(desKeySpec);
      } catch (Exception e) {
         System.out.println("Error: " + e);
         e.printStackTrace();
      }
   }

   /**
    * Set the UseIP variable used to determine if the client's IP address
    * should be included in the hash calculation when using validation
    *
    * @param useIP The UseIP as defined in config file.
    */
   public void setUseIP(String useIP) {
      this.useIP = useIP.toLowerCase().trim();
   }

   /**
    * Set the Encryption Algorithm used to encrypt and decrypt cookies
    *
    * @param encryptionAlgorithm The encryptionAlgorithm as defined in config file.
    */
   public void setEncryptionAlgorithm(String encryptionAlgorithm) {
      this.encryptionAlgorithm = encryptionAlgorithm.trim();
      initCypherParameters();
   }

   /**
    * Set the Encryption Mode used to encrypt and decrypt cookies
    *
    * @param encryptionMode The encryptionMode as defined in config file.
    */
   public void setEncryptionMode(String encryptionMode) {
      this.encryptionMode = encryptionMode.trim();
      initCypherParameters();
   }

   /**
    * Set the Encryption Padding used to encrypt and decrypt cookies
    *
    * @param encryptionPadding The encryptionPadding as defined in config file.
    */
   public void setEncryptionPadding(String encryptionPadding) {
      this.encryptionPadding = encryptionPadding.trim();
      initCypherParameters();
   }

   /**
    * Initialize the cypherParameters variable.
    */
   private void initCypherParameters() {
      cipherParameters = encryptionAlgorithm + "/" + encryptionMode + "/" + encryptionPadding;
   }

   /**
    * Given an array of Cookies, a name, and a default value,
    * this method tries to find the value of the cookie with
    * the given name. If there is no cookie matching the name
    * in the array, then the default value is returned instead.
    */
   private static String getCookieValue(Cookie[] cookies, String cookieName, String defaultValue) {
      if (cookies != null) {
         for (int i = 0; i < cookies.length; i++) {
            Cookie cookie = cookies[i];
            if (cookieName.equals(cookie.getName())) {
               return (cookie.getValue());
            }
         }
      }
      return (defaultValue);
   }

   /**
    * Given an array of cookies and a name, this method tries
    * to find and return the cookie from the array that has
    * the given name. If there is no cookie matching the name
    * in the array, null is returned.
    */
   private static Cookie getCookie(Cookie[] cookies, String cookieName) {
      if (cookies != null) {
         for (int i = 0; i < cookies.length; i++) {
            Cookie cookie = cookies[i];
            if (cookieName.equals(cookie.getName())) {
               return (cookie);
            }
         }
      }
      return null;
   }

   /**
    * Remove a cookie.
    *
    * @param request
    * @param response
    * @param cookieName
    */
   private void removeCookie(HttpServletRequest request, HttpServletResponse response, String cookieName) {
      Cookie cookie = getCookie(request.getCookies(), cookieName);
      if (cookie != null) {
         cookie.setMaxAge(0);
         response.addCookie(cookie);
      }
   }

   /**
    * Get validation hash for the specified parameters.
    *
    * @param username
    * @param password
    * @param clientIP
    * @return validation hash
    */
   private String getValidationHash(String username, String password, String clientIP) {
      if (validationKey == null) {
         System.out.println("ERROR! >> validationKey not spcified....");
         System.out.println("ERROR! >> you are REQUIRED to specify the validatonkey in the config xml");
         return null;
      }
      MessageDigest md5 = null;
      StringBuffer sbValueBeforeMD5 = new StringBuffer();

      try {
         md5 = MessageDigest.getInstance("MD5");
      } catch (NoSuchAlgorithmException e) {
         System.out.println("Error: " + e);
      }

      try {
         sbValueBeforeMD5.append(username.toString());
         sbValueBeforeMD5.append(":");
         sbValueBeforeMD5.append(password.toString());
         sbValueBeforeMD5.append(":");
         if (useIP.equals("true")) {
            sbValueBeforeMD5.append(clientIP.toString());
            sbValueBeforeMD5.append(":");
         }
         sbValueBeforeMD5.append(validationKey.toString());

         valueBeforeMD5 = sbValueBeforeMD5.toString();
         md5.update(valueBeforeMD5.getBytes());

         byte[] array = md5.digest();
         StringBuffer sb = new StringBuffer();
         for (int j = 0; j < array.length; ++j) {
            int b = array[j] & 0xFF;
            if (b < 0x10) sb.append('0');
            sb.append(Integer.toHexString(b));
         }
         valueAfterMD5 = sb.toString();
      } catch (Exception e) {
         System.out.println("Error:" + e);
      }
      return valueAfterMD5;
   }

   /**
    * Encrypt a string.
    *
    * @param clearText
    * @return clearText, encrypted
    */
   private String encryptText(String clearText) {
      sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
      try {
         Cipher c1 = Cipher.getInstance(cipherParameters);
         if (secretKey != null) {
            c1.init(c1.ENCRYPT_MODE, secretKey);
            byte clearTextBytes[];
            clearTextBytes = clearText.getBytes();
            byte encryptedText[] = c1.doFinal(clearTextBytes);
            String encryptedEncodedText = encoder.encode(encryptedText);
            return encryptedEncodedText;
         } else {
            System.out.println("ERROR! >> SecretKey not generated ....");
            System.out.println("ERROR! >> you are REQUIRED to specify the encryptionKey in the config xml");
            return null;
         }
      } catch (Exception e) {
         System.out.println("Error: " + e);
         e.printStackTrace();
         return null;
      }
   }

   /**
    * Decrypt a string.
    *
    * @param encryptedText
    * @return encryptedText, decrypted
    */
   private String decryptText(String encryptedText) {
      sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();
      try {
         byte decodedEncryptedText[] = decoder.decodeBuffer(encryptedText);
         Cipher c1 = Cipher.getInstance(cipherParameters);
         c1.init(c1.DECRYPT_MODE, secretKey);
         byte[] decryptedText = c1.doFinal(decodedEncryptedText);
         String decryptedTextString = new String(decryptedText);
         return decryptedTextString;
      } catch (Exception e) {
         System.out.println("Error: " + e);
         e.printStackTrace();
         return null;
      }
   }
}

// ----------------------------------------------------------------------------
// EOF