package com.stimulus.archiva.extraction;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List; 
import org.apache.log4j.*;
import java.io.*;

import com.stimulus.util.*;

import java.nio.charset.Charset;
import com.stimulus.archiva.exception.ExtractionException;

public class OOExtractor implements TextExtractor, Serializable {

		private static final long serialVersionUID = 2312152111422112224L;
		protected static final Logger logger = Logger.getLogger(Extractor.class.getName());
		
	    public void processElement(StringBuffer textBuffer, Object o) {
	        
	        if (o instanceof Element) {
	           
	            Element e = (Element) o;
	            String elementName = e.getQualifiedName();
	           
	            if (elementName.startsWith("text")) {
	               
	                if (elementName.equals("text:tab")) // add tab for text:tab
	                    textBuffer.append("\t");
	                else if (elementName.equals("text:s"))  // add space for text:s
	                    textBuffer.append(" ");
	                else {
	                    List children = e.getContent();
	                    Iterator iterator = children.iterator();
	                   
	                    while (iterator.hasNext()) {
	                       
	                        Object child = iterator.next();
	                        //If Child is a Text Node, then append the text
	                        if (child instanceof Text) { 
	                            Text t = (Text) child;
	                            textBuffer.append(t.getValue());
	                        }
	                        else
	                        processElement(textBuffer, child); // Recursively process the child element                   
	                    }                   
	                }
	                if (elementName.equals("text:p"))
	                    textBuffer.append("\n");                   
	            }
	            else {
	                List non_text_list = e.getContent();
	                Iterator it = non_text_list.iterator();
	                while (it.hasNext()) {
	                    Object non_text_child = it.next();
	                    processElement(textBuffer, non_text_child);                   
	                }
	            }               
	        }
	    }
	                 
		 public Reader getText(InputStream is, TempFiles tempFiles, Charset charset) throws ExtractionException
		 {
		   try {
			   StringBuffer textBuffer = new StringBuffer();
			   ZipFile zipFile = new ZipFile(Extractor.writeTemp(is,tempFiles));
		       Enumeration entries = zipFile.entries();
		       ZipEntry entry;
		            while(entries.hasMoreElements()) {
		            	entry = (ZipEntry) entries.nextElement();
		                if (entry.getName().equals("content.xml")) {
		                            textBuffer = new StringBuffer();                             
		                            SAXBuilder sax = new SAXBuilder();
		                            Document doc = sax.build(zipFile.getInputStream(entry));
		                            Element rootElement = doc.getRootElement();
		                            processElement(textBuffer,rootElement);
		                            break;
		                }
		            }                       
		       return new StringReader(textBuffer.toString());
		   } catch (Exception io) {
			   throw new ExtractionException("failed to extract text from open office:"+io.getMessage(),io,logger,Level.DEBUG);
		   }
		 }

}
