
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
package com.stimulus.archiva.domain;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.stimulus.archiva.exception.ConfigurationException;

 public class Domains implements Serializable  {

	 private static final long serialVersionUID = 78168850033322406L;
	 protected static Logger logger = Logger.getLogger(Domains.class);
     protected ArrayList<Domain> domains = new ArrayList<Domain>();

     public List<Domain> getDomains() {
         return domains;
     }

     public Domain getDomain(int index) {
     	return (Domain)domains.get(index);
     }

     public void clearAllDomains() {
      domains.clear();
     }

     public void addDomain(String domain) throws ConfigurationException {
         domains.add(new Domain(domain.toLowerCase(Locale.ENGLISH).trim()));
     }
     
     public void addDomain() throws ConfigurationException {
    	 addDomain("");
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