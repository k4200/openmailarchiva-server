
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

import org.apache.commons.logging.*;
import java.io.Serializable;
import java.util.*;

 public class Domains implements Serializable, Props  {

	 protected static final String domainKey = "domain";
	 
	 private static final long serialVersionUID = 78168850033322406L;
	 protected static Log logger = LogFactory.getLog(Domains.class.getName());
     protected ArrayList<Domain> domains = new ArrayList<Domain>();

     public List<Domain> getDomains() {
         return domains;
     }

     public Domain getDomain(int index) {
     	return domains.get(index);
     }

     public void clearAllDomains() {
      domains.clear();
     }

     public void addDomain(String domain) {
         domains.add(new Domain(domain.toLowerCase(Locale.ENGLISH).trim()));
     }
     
     public void addDomain() {
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
     
     public boolean loadSettings(String prefix, Settings prop, String suffix) {
    	 logger.debug("loading domain settings");
    		clearAllDomains();
     	  	int i = 1;
     	  	do {
     	  	    String domain = prop.getProperty(domainKey + "." + Integer.toString(i++));
     	  	    if (domain==null) break;
     	  	    addDomain(domain);
     	  	} while(true);
     	  	return true;
     }
   
     public void saveSettings(String prefix, Settings prop, String suffix) {
    	 logger.debug("saving domain settings");
    	 int c = 1;
    	 for (Domain domain : domains) {
    		 prop.setProperty(domainKey + "."+c++, domain.getName());
    	 }
     }
 }

    

		
