
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


package com.stimulus.archiva.presentation;
import java.io.File;
import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DownloadAction;

import com.stimulus.archiva.domain.Config;


public class DownloadBean extends DownloadAction implements Serializable {

    protected static Logger logger = Logger.getLogger(DownloadBean.class.getName());
    private static final long serialVersionUID = -7626204841615451485L;
    
    protected StreamInfo getStreamInfo(ActionMapping mapping, ActionForm form, 
            						   HttpServletRequest request, 
            						   HttpServletResponse response)
    									throws Exception {


    	String fileName = request.getParameter("attachment");
    	//String fileName = ((MessageBean)form).getAttachment();
    	 //File file = new File(((MessageBean)form).getAttachmentFilePath());
    	String filePath = Config.getViewPath() + File.separatorChar + fileName;
        File file = new File(filePath);
        logger.debug("download attachment {fileName='"+file.getPath()+"'");
        response.setHeader("Content-disposition", 
                           "attachment; filename=" + fileName.replace(' ','_'));
        String contentType = "application/download";
        response.setContentLength((int)file.length());
        return new FileStreamInfo(contentType, file);
    }

}
