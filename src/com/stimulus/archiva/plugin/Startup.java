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
import java.io.Serializable;
import java.util.*;
import org.apache.log4j.Logger;

public class Startup  implements PlugIn, Serializable  {

    /**
	 * 
	 */
	private static final long serialVersionUID = 6415321408987920651L;
	protected static final Logger logger = Logger.getLogger(Startup.class);

    public void destroy() {
    	Config.shutdown();
	    logger.info("mailarchiva v"+Config.getConfig().getApplicationVersion()+" shutdown at "+new Date());
	}

	public void init(ActionServlet actionServlet, ModuleConfig config) throws ServletException {
	  
		  
	    logger.info("mailarchiva open source edition v"+Config.getConfig().getApplicationVersion()+" started at "+new Date());
	    
	    
	    System.setProperty("mail.mime.base64.ignoreerrors", "true");
	    
		try {
			Config conf = Config.getConfig();
			
			String appPath = actionServlet.getServletConfig().getServletContext().getRealPath("/");
			if (appPath==null) {
				logger.fatal("failed to retrieve application path from servlet context.");
				logger.fatal("please clear out the work directory, browser cache, and restart the server.");
				throw new ServletException("failed to retrieve application path from servlet context.");
			}
			FileSystem fs = Config.getFileSystem();
			fs.outputSystemInfo();
	   	    fs.setApplicationPath(appPath);
	   	    if (!fs.checkAllSystemPaths()) {
	   	    	logger.error("mailarchiva cannot find one or more required system paths.");
	   	    	logger.error("the server will proceed with startup but there is a serious risk of system malfunction.");
	   	    }
	   	    
		    fs.initTempDirectory();
		    fs.clearViewDirectory();
			fs.clearTempDirectory();
			conf.init(MessageService.getFetchMessageCallback());
			conf.loadSettings(MailArchivaPrincipal.SYSTEM_PRINCIPAL);
	   	    conf.registerServices();
	   	    conf.getServices().startAll();
			logger.debug("startup sequence is complete");
	   	} catch (Exception e) {
	   	    logger.error("failed to execute startup cause: ",e);
	   	    return;
	   	}
	
	    
	}
	
	public class RecoverInfo {
		
	}

}