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

import org.apache.commons.logging.*;

import com.stimulus.archiva.exception.MessageSearchException;
import com.stimulus.util.*;

public abstract class Indexer implements Service, Props {

    	protected static final String indexLanguageKey 					= "index.language";
    	protected static final String indexLanguageDetectionKey 		= "index.language.detect";
        protected static final String indexAttachmentsKey			 	= "index.attachments";
        protected static final String indexMessageBodyKey				= "index.messagebody";
        protected static final String indexThreadsKey					= "index.threads";
        protected static final String indexMaxSizeKey					= "index.max.size";
        protected static final String indexMultipleProcessesKey    		= "index.multiple.processes";
        protected static final String indexDefaultCharSetKey			= "index.default.charset";
        protected static final String indexDetectCharSetKey				= "index.detect.charset";
        protected static final String indexZipFileNameCharSetKey		= "index.zip.filename.charset";

        protected static final String defaultIndexLanguage = "en";
        protected static final String defaultIndexLanguageDetection = "no";
        protected static final String defaultIndexAttachments = "yes";
        protected static final String defaultIndexMessageBody = "yes";
        protected static final String defaultIndexThreads = "10";
        protected static final String defaultIndexMaxSize = "2048"; // megabytes
        protected static final String defaultMultipleProcesses = "no";
        protected static final String defaultIndexDetectCharSet = "no";
        protected static final String defaultIndexDefaultCharSet = "UTF-8";
        protected static final String defaultZipFileNameCharSetKey = "UTF-8";

        protected String indexPath;
        protected String indexLanguage = defaultIndexLanguage;
        protected boolean indexLanguageDetection = true;
        protected boolean indexAttachments = true;
        protected boolean indexMessageBody = true;
        protected int indexThreads = 1;
        protected int indexMaxSize = 1024;
        protected static Log logger = LogFactory.getLog(Indexer.class.getName());
        protected String indexDefaultCharSet;
        protected boolean indexDetectCharSet;
        protected String indexZipFileNameCharSet;


    	public void saveSettings(String prefix, Settings prop, String suffix) {
    		 logger.debug("saving indexer settings");
    	     prop.setProperty(indexLanguageDetectionKey,ConfigUtil.getYesNo(indexLanguageDetection));
      		 prop.setProperty(indexLanguageKey,indexLanguage);
      		 prop.setProperty(indexAttachmentsKey, ConfigUtil.getYesNo(indexAttachments));
      	     prop.setProperty(indexMessageBodyKey, ConfigUtil.getYesNo(indexMessageBody));
      	     prop.setProperty(indexThreadsKey, Integer.toString(indexThreads));
      	     prop.setProperty(indexMaxSizeKey, Integer.toString(indexMaxSize));
      	     prop.setProperty(indexDetectCharSetKey,ConfigUtil.getYesNo(indexDetectCharSet));
      	     prop.setProperty(indexDefaultCharSetKey,indexDefaultCharSet);
      	     prop.setProperty(indexZipFileNameCharSetKey,indexZipFileNameCharSet);
    	}

    	public boolean loadSettings(String prefix, Settings prop, String suffix) {
    		logger.debug("loading indexer settings");
    	    setIndexLanguageDetection(ConfigUtil.getBoolean(prop.getProperty(indexLanguageDetectionKey),defaultIndexLanguageDetection));
			setIndexLanguage(ConfigUtil.getString(prop.getProperty(indexLanguageKey),defaultIndexLanguage));
			setIndexAttachments(ConfigUtil.getBoolean(prop.getProperty(indexAttachmentsKey),defaultIndexAttachments));
			setIndexMessageBody(ConfigUtil.getBoolean(prop.getProperty(indexMessageBodyKey),defaultIndexMessageBody));
			setIndexThreads(ConfigUtil.getInteger(prop.getProperty(indexThreadsKey),defaultIndexThreads));
	      	setIndexMaxSize(ConfigUtil.getInteger(prop.getProperty(indexMaxSizeKey),defaultIndexMaxSize));
			setIndexDefaultCharSet(ConfigUtil.getString(prop.getProperty(indexDefaultCharSetKey),defaultIndexDefaultCharSet));
			setIndexDetectCharSet(ConfigUtil.getBoolean(prop.getProperty(indexDetectCharSetKey),defaultIndexDetectCharSet));
			setIndexZipFileNameCharSet(ConfigUtil.getString(prop.getProperty(indexZipFileNameCharSet),defaultZipFileNameCharSetKey));
	      	return true;
    	}


    	public void setIndexMaxSize(int indexMaxSize) {
    		this.indexMaxSize = indexMaxSize;
    	}

    	public int getIndexMaxSize() {
    		return indexMaxSize;
    	}

        public void setIndexLanguage(String lang) {
      	  this.indexLanguage = lang;
        }

        public String getIndexLanguage() {
      	  return indexLanguage;
        }

        public boolean getIndexLanguageDetection() {
      	  return indexLanguageDetection;
        }

        public void setIndexLanguageDetection(boolean detectLanguage) {
      	  this.indexLanguageDetection = detectLanguage;
        }

        public void setIndexDetectCharSet(boolean detectCharSet) {
        	this.indexDetectCharSet = detectCharSet;
        }

        public boolean getIndexDetectCharSet() {
        	return indexDetectCharSet;
        }

        public boolean getIndexAttachments() { return indexAttachments; }
        public boolean getIndexMessageBody() { return indexMessageBody; }

        public void setIndexAttachments(boolean indexAttachments) {
     	   this.indexAttachments = indexAttachments;
        }

        public void setIndexMessageBody(boolean indexMessageBody) {
     	   this.indexMessageBody = indexMessageBody;
        }

        public void setIndexThreads(int indexThreads) {
     	   this.indexThreads = indexThreads;
        }

        public int getIndexThreads() {
     	   return indexThreads;
        }

        public void setIndexDefaultCharSet(String indexDefaultCharSet) {
        	this.indexDefaultCharSet = indexDefaultCharSet;
        }

        public String getIndexDefaultCharSet() {
        	return indexDefaultCharSet;
        }


        public void setIndexZipFileNameCharSet(String indexZipFileNameCharSet) {
        	this.indexZipFileNameCharSet = indexZipFileNameCharSet;
        }

        public String getIndexZipFileNameCharSet() {
        	return indexZipFileNameCharSet;
        }

        public abstract void indexMessage(Email emailID) throws MessageSearchException;

    	public abstract void deleteMessage(EmailID emailID) throws MessageSearchException;

    	public abstract void deleteIndex(Volume volume) throws MessageSearchException;

    	public abstract void prepareIndex(Volume v) throws MessageSearchException;


}

