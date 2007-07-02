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

package com.stimulus.archiva.domain;
import com.stimulus.archiva.exception.ConfigurationException;
import org.apache.log4j.Logger;
import java.util.*;

 public class Domains {

     protected static Logger logger = Logger.getLogger(Domains.class.getName());
     protected ArrayList domains = new ArrayList();

     public List getDomains() {
         return domains;
     }

     public Domains getDomains(int index) {
     	return (Domains)domains.get(index);
     }

     public void clearAllDomains() {
      domains.clear();
     }

     public void addDomain(String domain) throws ConfigurationException {
         domains.add(new Domain(domain.toLowerCase().trim()));
     }

     public void deleteDomain(int id) {
     	domains.remove(id);
     }

     public class Domain {

         protected String name;

         public Domain(String name) {
             setName(name);
         }

         public void setName(String name) {
             this.name = name;
         }

         public String getName() {
             return name;
         }
     }

}