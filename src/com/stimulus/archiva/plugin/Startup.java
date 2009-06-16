

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

package com.stimulus.archiva.plugin;

import org.apache.struts.action.ActionServlet;
import org.apache.struts.action.PlugIn;
import org.apache.struts.config.ModuleConfig;
import javax.servlet.ServletException;

import com.stimulus.archiva.monitor.Event;
import com.stimulus.archiva.monitor.Event.Category;
import com.stimulus.archiva.service.*;
import com.stimulus.archiva.domain.*;

import java.io.Serializable;
import java.util.*;
import org.apache.commons.logging.*;
import com.stimulus.archiva.security.*;
import java.io.*;

public class Startup  implements PlugIn, Serializable  {

    /**
	 *
	 */
	private static final long serialVersionUID = 6415321408987920651L;
	protected static final Log logger = LogFactory.getLog(Startup.class);

    public void destroy() {
    	Config.shutdown();
	    logger.info(Config.getConfig().getProductName()+" v"+Config.getConfig().getApplicationVersion()+" shutdown at "+new Date());
	}

	public void init(ActionServlet actionServlet, ModuleConfig config) throws ServletException {

	    logger.info(Config.getConfig().getProductName()+" v"+Config.getConfig().getApplicationVersion()+" started at "+new Date());

		try {
			Config conf = Config.getConfig();

			String appPath;
			int retries = 0;

			do {
				try { Thread.sleep(100); } catch (Exception e) {}
				appPath = actionServlet.getServletConfig().getServletContext().getRealPath("/");
				retries++;
			} while (appPath==null && retries<10);

			if (appPath==null) {
				logger.debug("failed to retrieve application path from servlet context.");
				String catalinaPath = System.getenv("CATALINA_HOME");
				String contextPath = actionServlet.getServletConfig().getServletContext().getContextPath();
				if (catalinaPath!=null && contextPath!=null) {
					if (catalinaPath.endsWith(File.separator))
						catalinaPath = catalinaPath.substring(0,catalinaPath.length()-1);
					appPath = catalinaPath + File.separator + "webapps" + contextPath;
					logger.debug("constructed application path from catalina.home {appPath='"+appPath+"'}");
				} else {
					logger.fatal("failed to retrieve application path from servlet context or reconstruct from catalina home.");
					logger.fatal("please remove the entire server/work/Catalina directory, browser cache, and restart the server.");
					throw new ServletException("failed to retrieve application path from servlet context.");
				}
			}
			FileSystem fs = Config.getFileSystem();
			fs.outputSystemInfo();
	   	    fs.setApplicationPath(appPath);
	   	    if (!fs.checkAllSystemPaths()) {
	   	    	logger.fatal("server cannot find one or more required system paths.");
	   	    	System.exit(1);
	   	    }
		   	if (!fs.checkStartupPermissions()) {
				 logger.fatal("failed to startup. directory and file read/write permissions not defined correctly.");
				System.exit(1);
			}
	   	    fs.initLogging();
	   	    fs.initCrypto();
		    fs.clearViewDirectory();
			fs.clearTempDirectory();

			conf.init(MessageService.getFetchMessageCallback());
			conf.loadSettings(MailArchivaPrincipal.SYSTEM_PRINCIPAL);
	   	    conf.registerServices();
	   	    conf.getServices().startAll();
			logger.debug("startup sequence is complete");
	   	} catch (Exception e) {
	   	    logger.fatal("failed to execute startup cause: ",e);
	   	    System.exit(1);
	   	    return;
	   	}


	}

	public class RecoverInfo {

	}

}
