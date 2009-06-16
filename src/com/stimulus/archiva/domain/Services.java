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

package com.stimulus.archiva.domain;

import java.util.*;

import org.apache.commons.logging.*;

public class Services {


	ArrayList<Service> services = new ArrayList<Service>();

	public Services() { }
	protected static final Log logger = LogFactory.getLog(Service.class);

	public void registerService(Service service) {
		logger.debug("registering service {service='"+service.getServiceName()+"'}");
		services.add(service);
	}

	public void deregisterService(Service service) {
		logger.debug("deregistering service {service='"+service.getServiceName()+"'}");
		services.remove(service);
	}

	public void startAll() {
	 logger.debug("startall services");
		for (Service service : services) {
			logger.debug("starting service {service='"+service.getServiceName()+"'}");
			try {
				service.startup();
			} catch (Throwable t) {
				logger.debug("error startup:"+t);
			}
		}
	}

	public void stopAll() {
		for (Service service : services) {
			logger.debug("stopping service {service='"+service.getServiceName()+"'}");
			service.prepareShutdown();
		}
		for (Service service : services) {
			logger.debug("stopping service {service='"+service.getServiceName()+"'}");
			service.shutdown();
		}
	}

	public void start(Class c) {
		for (Service service : services) {
			if (service.getClass().equals(c)) {
				logger.debug("starting service {service='"+service.getServiceName()+"'}");
				service.startup();
			}
		}
	}

	public void stop(Class c) {
		for (Service service : services) {
			if (service.getClass().equals(c)) {
				logger.debug("stopping service {service='"+service.getServiceName()+"'}");
				service.shutdown();
			}
		}
	}

	public void reloadConfig(Class c) {
		for (Service service : services) {
			if (service.getClass().equals(c)) {
				logger.debug("restart service {service='"+service.getServiceName()+"'}");
				service.reloadConfig();
			}
		}
	}

	public void reloadConfigAll() {
		for (Service service : services) {
			logger.debug("restart service {service='"+service.getServiceName()+"'}");
			service.reloadConfig();
		}
	}

	public void monitorAll() {
		for (Service service : services) {
			logger.debug("monitor service {service='"+service.getServiceName()+"'}");
			if (!service.isAlive()) {
				logger.warn(service.getServiceName()+" service is currently stopped. starting it.");
				service.startup();
			}
		}
	}
}
