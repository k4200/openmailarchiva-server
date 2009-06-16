

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

import org.apache.commons.logging.*;
import org.apache.struts.actions.DownloadAction;
import org.apache.struts.action.*;
import com.stimulus.archiva.domain.Config;
import javax.servlet.http.*;
import java.io.*;
import java.net.*;
import javax.mail.internet.MimeUtility;

public class DownloadMessageBean extends DownloadAction implements Serializable {

    /**
	 *
	 */
	private static final long serialVersionUID = -778698565924172868L;
	protected static final Log logger = LogFactory.getLog(DownloadMessageBean.class.getName());

    @Override
	protected StreamInfo getStreamInfo(ActionMapping mapping, ActionForm form,
            						   HttpServletRequest request,
            						   HttpServletResponse response)
    									throws Exception {

    	String fileName = request.getParameter("name");
    	//String fileName = ((MessageBean)form).getOriginalMessageFileName();
        logger.debug("download original email {fileName='"+fileName+"'}");
        String agent = request.getHeader("USER-AGENT");
        if (null != agent && -1 != agent.indexOf("MSIE"))  {
        	String codedfilename = URLEncoder.encode(fileName, "UTF8");
        	response.setContentType("application/x-download");
        	response.setHeader("Content-Disposition","attachment;filename=" + codedfilename);
        } else if (null != agent && -1 != agent.indexOf("Mozilla")) {
        	String codedfilename = MimeUtility.encodeText(fileName, "UTF8", "B");
        	response.setContentType("application/x-download");
        	response.setHeader("Content-Disposition","attachment;filename=" + codedfilename);
        } else {
        	response.setHeader("Content-Disposition","attachment;filename=" + fileName);
        }
        String contentType = "message/rfc822";
        //String filePath = ((MessageBean)form).getOriginalMessageFilePath();
        String filePath = Config.getFileSystem().getViewPath() + File.separatorChar + fileName;
        File file          = new File(filePath);
        response.setContentLength((int)file.length());
        Config.getFileSystem().getTempFiles().markForDeletion(file);
        return new FileStreamInfo(contentType, file);
    }

}
