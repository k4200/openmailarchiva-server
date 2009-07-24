package com.stimulus.archiva.extraction;

import java.io.*;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import com.stimulus.archiva.exception.ExtractionException;
import com.stimulus.archiva.index.IndexInfo;


public class MS2007Extractor implements TextExtractor, Serializable
{
 private static final long serialVersionUID = 2121152151457122351L;
 protected static final Log logger = LogFactory.getLog(MS2007Extractor.class.getName());
 
 
 public Reader getText(InputStream is, Charset charset, IndexInfo indexInfo) throws ExtractionException {
	  ZipInputStream zis  		 = null; 
	  ZipEntry entry   			 = null;
	  File extractFile 			 = null;
	  OutputStreamWriter writer  = null;
	  Reader reader   			 = null;
	  try {
	   extractFile = File.createTempFile("extract",".tmp");
	   zis = new ZipInputStream(is);
	   SAXParserFactory factory = SAXParserFactory.newInstance();
	   SAXParser parser = factory.newSAXParser();
	   writer = new OutputStreamWriter(new FileOutputStream(extractFile),"UTF-8");
	   SaxHandler handler = new SaxHandler(writer);
	   while (( entry = zis.getNextEntry() ) != null) {
	    String name = entry.getName();
	    if (name.endsWith("sharedStrings.xml") ||
           name.endsWith("document.xml") ||
	        name.startsWith("ppt/slides/slide")) { 
	    	InputStream entryis = new FixedLengthInputStream(zis,(int)entry.getSize());
	        Reader read = new InputStreamReader(entryis,"UTF-8");
	        InputSource isource = new InputSource(read);
	        isource.setEncoding("UTF-8");
	        parser.parse(isource, handler);
	        writer.write('\n');
	     }
	   }
	   zis.close();
	   writer.close();
	   reader = new InputStreamReader(new FileInputStream(extractFile),"UTF-8");
	  } catch (Exception e) {
		  throw new ExtractionException("failed to extract text from microsoft 2007 document:"+e.getMessage(),e,logger);
	  } catch (OutOfMemoryError ome) {
		  throw new ExtractionException("failed to extract text from microsoft 2007 document:"+ome.getMessage(),ome,logger);
	  } finally {
		   try { if (zis!=null) zis.close(); } catch (Exception e) { System.out.println(e); }
		   try { if (writer!=null) writer.close(); } catch (Exception e) { System.out.println(e); }
		  if (extractFile!=null) indexInfo.addDeleteFile(extractFile);
		   if (reader!=null) indexInfo.addReader(reader);
	  }
	  return reader;
} 

protected class SaxHandler extends DefaultHandler {
 Writer writer;
 
 public SaxHandler(Writer writer) {
  this.writer = writer;
 }
 
 public void characters(char[] ch, int start, int length) throws SAXException {
  try {
   writer.write(' ');
   writer.write(ch,start,length);
   writer.write(' ');
  } catch (IOException e) {
   System.out.println("failed to write characters to temp file");
  }
 }
}

public static class FixedLengthInputStream extends InputStream {
	    private InputStream mIn;
	    private int mLength;
	    private int mCount;

	    public FixedLengthInputStream(InputStream in, int length) {
	        this.mIn = in;
	        this.mLength = length;
	    }

	    @Override
	    public int available() throws IOException {
	        return mLength - mCount;
	    }

	    @Override
	    public int read() throws IOException {
	        if (mCount < mLength) {
	            mCount++;
	            return mIn.read();
	        } else {
	            return -1;
	        }
	    }

	    @Override
	    public int read(byte[] b, int offset, int length) throws IOException {
	        if (mCount < mLength) {
	            int d = mIn.read(b, offset, Math.min(mLength - mCount, length));
	            if (d == -1) {
	                return -1;
	            } else {
	                mCount += d;
	                return d;
	            }
	        } else {
	            return -1;
	        }
	    }

	    @Override
	    public int read(byte[] b) throws IOException {
	        return read(b, 0, b.length);
	    }

	    public String toString() {
	        return String.format("FixedLengthInputStream(in=%s, length=%d)", mIn.toString(), mLength);
	    }
	}

}