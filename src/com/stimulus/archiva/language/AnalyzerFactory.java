/*
 * Subversion Infos:
 * $URL$
 * $Author$
 * $Date$
 * $Rev$
*/

		
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

package com.stimulus.archiva.language;
import java.io.Serializable;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.WhitespaceAnalyzer;

import com.stimulus.archiva.domain.Config;
import com.stimulus.archiva.search.ArchivaAnalyzer;
import com.stimulus.archiva.search.EmailAnalyzer;
import com.stimulus.archiva.search.FileNameAnalyzer;

public class AnalyzerFactory implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1078168764623051823L;

	public enum Operation { INDEX, SEARCH };
	
    protected static Logger logger = Logger.getLogger(AnalyzerFactory.class.getName());
    
    public static Analyzer getAnalyzer(String language, Operation operation) {
        Analyzer analyzer = null;
        logger.debug("getAnalyzer() {language='"+language+"'}");
        String className = null;
        
        try {
            Map analyzers = Config.getConfig().getSearchAnalyzers();
            if (analyzers.containsKey(language)) {
                className = (String)analyzers.get(language);
                logger.debug("successfully obtained class name for search analyzer {language='"+language+"', class='"+className+"'}");
            } else {
                className = (String)analyzers.get(Config.getConfig().getIndexLanguage());
                logger.debug("email language is unsupported. using default language.  {language='"+language+"', class='"+className+"'}");
            }
            Class analyzerClass = Class.forName(className);
            if (analyzerClass==null) {
                logger.error("failed to return search analyzer. are the analyzers specified correctly in server.conf? {language='"+language+"', class='"+className+"'}");
                logger.error("recovering by returning default archiver analyzer. indexing and search may be impaired. urgent fix required.");
                analyzer = new ArchivaAnalyzer();
            } else {
                logger.debug("retrieved analyzer class {language='"+language+"', class='"+className+"'}");
                analyzer = (Analyzer)analyzerClass.newInstance();
                logger.debug("analyzer class instance created {language='"+language+"', class='"+className+"'}");
            }
            logger.debug("successfully returned search analyzer {language='"+language+"', class='"+className+"'}");
        } catch (Exception e) {
            logger.error("failed to return search analyzer. are the analyzers specified correctly in server.conf? {language='"+language+"', class='"+className+"'}",e);
            logger.error("recovering by returning default archiver analyzer. indexing and search may be impaired. urgent fix required.");
            analyzer = new ArchivaAnalyzer();
        } 
        PerFieldAnalyzerWrapper wrapper = new PerFieldAnalyzerWrapper(analyzer);
        
        if (operation==Operation.INDEX) {
	        wrapper.addAnalyzer("to", new EmailAnalyzer());
	        wrapper.addAnalyzer("from", new EmailAnalyzer());
	        wrapper.addAnalyzer("cc", new EmailAnalyzer());
	        wrapper.addAnalyzer("bcc", new EmailAnalyzer());
	        wrapper.addAnalyzer("deliveredto", new EmailAnalyzer());
	        wrapper.addAnalyzer("attachname", new FileNameAnalyzer());
        } else {
        	wrapper.addAnalyzer("to", new WhitespaceAnalyzer());
 	        wrapper.addAnalyzer("from", new WhitespaceAnalyzer());
 	        wrapper.addAnalyzer("cc", new WhitespaceAnalyzer());
 	        wrapper.addAnalyzer("bcc", new WhitespaceAnalyzer());
 	        wrapper.addAnalyzer("deliveredto", new WhitespaceAnalyzer());
 	        wrapper.addAnalyzer("attachname", new FileNameAnalyzer());
        }
        return wrapper;
    }
}
