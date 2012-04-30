
/* 
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
import com.stimulus.archiva.domain.Email;
import com.stimulus.archiva.extraction.MessageExtraction;
import com.stimulus.archiva.service.MessageService;
import com.stimulus.struts.ActionContext;

import javax.servlet.http.*;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.mail.internet.MimeUtility;

public class ExportBean extends DownloadAction implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 6195837217970705967L;
	/**
	 * 
	 */
	protected static final Log logger = LogFactory.getLog(ExportBean.class.getName());
    
    @Override
	protected StreamInfo getStreamInfo(ActionMapping mapping, ActionForm form, 
            						   HttpServletRequest request, 
            						   HttpServletResponse response)
    									throws Exception {
    	SearchBean searchBean = (SearchBean)form;
    	
    	String outputDir = Config.getFileSystem().getViewPath() + File.separatorChar;
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    	String zipFileName = "export-" + sdf.format(new Date()) + ".zip";
    	File zipFile = new File(outputDir + zipFileName);
    	
        String agent = request.getHeader("USER-AGENT");
        if (null != agent && -1 != agent.indexOf("MSIE"))  {
        	String codedfilename = URLEncoder.encode(zipFileName, "UTF8");
        	response.setContentType("application/x-download");
        	response.setHeader("Content-Disposition","attachment;filename=" + codedfilename);
        } else if (null != agent && -1 != agent.indexOf("Mozilla")) {    
        	String codedfilename = MimeUtility.encodeText(zipFileName, "UTF8", "B");
        	response.setContentType("application/x-download");
        	response.setHeader("Content-Disposition","attachment;filename=" + codedfilename);
        } else {
        	response.setHeader("Content-Disposition","attachment;filename=" + zipFileName);
        }

        logger.debug("size of searchResult = " + searchBean.getSearchResults().size());
        //MessageBean.viewMessage
        List<File> files = new ArrayList<File>();
        for(SearchResultBean searchResult : searchBean.getSearchResults()) {
        	if (searchResult.getSelected()) {
        		Email email = MessageService.getMessageByID(searchResult.getVolumeID(), searchResult.getUniqueID(), false);

        		HttpServletRequest hsr = ActionContext.getActionContext().getRequest();
    	  		String baseURL = hsr.getRequestURL().substring(0,hsr.getRequestURL().lastIndexOf(hsr.getServletPath()));
    	  		MessageExtraction messageExtraction = MessageService.extractMessage(email, baseURL, true); // can take a while to extract message

//        		MessageBean mbean = new MessageBean();
//        		mbean.setMessageID(searchResult.getUniqueID());
//        		mbean.setVolumeID(searchResult.getVolumeID());
//        		writer.println(searchResult.toString());
//        		writer.println(messageExtraction.getFileName());
    	  		
    	  		//TODO Check duplicate entry
    	  		files.add(new File(outputDir, messageExtraction.getFileName()));
        	}
        }
        
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
		try {
			byte[] buf = new byte[1024];
			for (File f : files) {
				ZipEntry ze = new ZipEntry(f.getName());
				logger.debug("Adding file " + f.getName());
				zos.putNextEntry(ze);
				InputStream is = new BufferedInputStream(new FileInputStream(f));
				for (;;) {
					int len = is.read(buf);
					if (len < 0)
						break;
					zos.write(buf, 0, len);
				}
				is.close();
				Config.getFileSystem().getTempFiles().markForDeletion(f);
			}
		} finally {
			zos.close();
		}
        logger.debug("download zipped emails {fileName='"+zipFileName+"'}");
        
        String contentType = "application/zip";
        Config.getFileSystem().getTempFiles().markForDeletion(zipFile);
        return new FileStreamInfo(contentType, zipFile);
    }

}
