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
import org.apache.log4j.Logger;

import com.stimulus.archiva.domain.Service.Status;

public class ServiceDelegate implements java.io.Serializable {
	

		 private static final long serialVersionUID = -1311431164001372521L;
		 protected Logger logger;
		 protected Status status = Status.STOPPED;
		 protected String serviceName;
		 protected Service service;
		 
		 
		 public ServiceDelegate(String serviceName, Service service, Logger logger) {
			 this.serviceName = serviceName;
			 this.service = service;
			 this.logger = logger;
		 }
		 public String getServiceName() {
			return serviceName;
		 }
		
		 public boolean isAlive(boolean reallyAlive) {
			 boolean alive = false;
			 alive = (status==Status.STOPPED) ? false : reallyAlive;
			 logger.debug("isAlive {return='"+alive+"'}");
			 return alive;
		 }
		 
		 public void startup() {
			 if (status==Status.STARTED)
				 	return;
			 logger.debug(getServiceName()+" started");
			 status = Status.STARTED;
		 }
		
		 public void prepareShutdown() {
			 status = Status.STOPPING;
		 }
		 
		 public void shutdown() {
			 if (status==Status.STOPPED) {
				 return;
			 }
			 logger.debug(getServiceName()+" is stopped");
			 status = Status.STOPPED;
		 }
		 
		 public void reloadConfig() {
			 logger.debug(getServiceName()+" is reloading its configuration");
		 }
		 
		 @Override
		protected void finalize() throws Throwable {
			 shutdown();
		 }
		 
		 public Status getStatus() {
			 return status;
		 }
		 
	    
}
