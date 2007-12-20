/*
 * Subversion Infos:
 * $URL$
 * $Author$
 * $Date$
 * $Rev$
*/

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

import java.io.Serializable;
import java.util.Date;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.action.PlugIn;
import org.apache.struts.config.ModuleConfig;

import com.stimulus.archiva.domain.Config;
import com.stimulus.archiva.service.ConfigurationService;
import com.stimulus.archiva.service.MessageService;

public class Startup  implements PlugIn, Serializable  {

    /**
	 * 
	 */
	private static final long serialVersionUID = 6415321408987920651L;
	protected static Logger logger = Logger.getLogger(Startup.class);

    public void destroy() {
	    logger.info("mailarchiva v"+Config.getApplicationVersion()+" shutdown at "+new Date());
	}

	public void init(ActionServlet actionServlet, ModuleConfig config) throws ServletException {
	  
		  
	    logger.info("mailarchiva open source edition v"+Config.getApplicationVersion()+" started at "+new Date());
	    System.setProperty("mail.mime.base64.ignoreerrors", "true");
	    Config conf = ConfigurationService.getConfig();
	    String appPath = actionServlet.getServletConfig().getServletContext().getRealPath("/");
	   	Config.setApplicationPath(appPath);
	   	Config.clearViewDirectory();
	   	Config.clearTempDirectory();
	  
	   	try {
	   	    conf.load();
	   	    conf.getVolumes().startDiskSpaceCheck();
	   	    String recover = System.getProperty("rearchive");
		    if (recover!=null && recover.equalsIgnoreCase("yes"))
		    	MessageService.recoverNoArchiveMessages(null);
	   	   
	   	} catch (Exception e) {
	   	    logger.error("failed to execute startup cause: ",e);
	   	    return;
	   	}
	
	    
	}
	
	public class RecoverInfo {
		
	}

}