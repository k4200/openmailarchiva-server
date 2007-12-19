
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

import java.io.Serializable;
import java.security.Principal;
import java.util.*;

public class MailArchivaPrincipal implements Principal, Serializable
{

 /**
	 * 
	 */
	private static final long serialVersionUID = -53101827955389377L;
protected String name = "";
 protected String role = "user";
 protected List<String> emailAddresses = null;
 protected String ipAddress = null;


 public MailArchivaPrincipal(String name, String role) {
	 this(name,role,null);
 }
 public MailArchivaPrincipal(String name, String role,List<String> emailAddresses) {
	 this(name,role,emailAddresses,null);
 }
 public MailArchivaPrincipal(String name, String role,List<String> emailAddresses, String ipAddress)
 {
     this.name = name;
     this.role = role;
     this.emailAddresses = emailAddresses;
     this.ipAddress = ipAddress;
 }
 

 public String getName()
 {
     return name;
 }

 public String getRole()
 {
     return role;
 }
 
 public String getIpAddress() {
	 return ipAddress;
 }
 
 public void setIpAddress(String ipAddress) {
	 this.ipAddress = ipAddress;
 }
 
 public List<String> getEmailAddresses() {
	 return emailAddresses;
 }

 public boolean equals(Object obj)
 {
     if(obj instanceof MailArchivaPrincipal)
         return name.equals(((MailArchivaPrincipal)obj).getName());
     else
         return false;
 }

 public String toString()
 {
	 if (emailAddresses!=null && emailAddresses.size()>0)
     	return "name='"+name+"',role='"+role+"',ipAddress='"+ipAddress+"',emailAddress='"+emailAddresses.get(0)+"'";
	 else
		return"name='"+name+"',role='"+role+"',ipAddress='"+ipAddress;
 }
 

 public int hashCode()
 {
     return name.hashCode();
 }


}