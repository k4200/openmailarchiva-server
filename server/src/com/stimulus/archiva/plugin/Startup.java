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

package com.stimulus.archiva.plugin;

import org.apache.struts.action.ActionServlet;
import org.apache.struts.action.PlugIn;
import org.apache.struts.config.ModuleConfig;
import javax.servlet.ServletException;
import com.stimulus.archiva.service.*;
import com.stimulus.archiva.domain.*;
import java.util.*;
import org.apache.log4j.Logger;

public class Startup  implements PlugIn {

    protected static final Logger logger = Logger.getLogger(Startup.class);

    public void destroy() {
	    logger.info("mailarchiva v"+Config.getApplicationVersion()+" shutdown at "+new Date());
	}

	public void init(ActionServlet actionServlet, ModuleConfig config) throws ServletException {

	    logger.info("mailarchiva v"+Config.getApplicationVersion()+" started at "+new Date());
	    Config conf = ConfigurationService.getConfig();
	    String appPath = actionServlet.getServletConfig().getServletContext().getRealPath("/");
	   	Config.setApplicationPath(appPath);
	   	Config.clearViewDirectory();
	  
	   	try {
	   	    conf.load();
	   	    conf.getVolumes().startDiskSpaceCheck();
	   	} catch (Exception e) {
	   	    logger.error("failed to load configuration. cause: "+e.toString());
	   	    return;
	   	}
	}

}