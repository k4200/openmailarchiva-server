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
package com.stimulus.archiva.security.realm;

import java.io.Serializable;
import java.security.Principal;

public class CorpGovPrincipal implements Principal, Serializable
{

 private String role;

 private CorpGovPrincipal()
 {
 }

 public CorpGovPrincipal(String name, String role)
 {
     this.name = name;
     this.role = role;
 }

 public String getName()
 {
     return name;
 }

 public String getRole()
 {
     return role;
 }

 public boolean equals(Object obj)
 {
     if(obj instanceof CorpGovPrincipal)
         return name.equals(((CorpGovPrincipal)obj).getName());
     else
         return false;
 }

 public String toString()
 {
     return "CorpGov Principal[name = '" + name + "']";
 }

 public int hashCode()
 {
     return name.hashCode();
 }

 private String name;
}