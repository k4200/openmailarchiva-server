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

package com.stimulus.archiva.index;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import com.stimulus.archiva.domain.Config;
import com.stimulus.util.StreamUtil;
import org.apache.tools.zip.*;

public class IndexInfo {

	  protected Charset charset;
	  protected LinkedList<InputStream> sourceStreams;
	  protected LinkedList<ZipFile> zipFiles;
	  protected LinkedList<Reader> readers;
	  protected LinkedList<File> deleteFiles;

	  public IndexInfo() {
		  reset();
	  }

	  public void reset() {
		  String charname = Config.getConfig().getIndex().getIndexDefaultCharSet();
		  charset = Charset.forName(charname);
		  sourceStreams = new LinkedList<InputStream>();
		  zipFiles = new LinkedList<ZipFile>();
		  readers = new LinkedList<Reader>();
		  deleteFiles = new LinkedList<File>();
	  }

	  public Charset getCharset() { return charset; }

	  public void setCharset(Charset charset) { this.charset = charset; }

	  public void addSourceStream(InputStream sourceStream) {
		  if (sourceStream!=null) {
			  sourceStreams.add(sourceStream);
		  }
	  }

	  public void addReader(Reader reader) {
		  if (reader!=null) {
			  readers.add(reader);
		  }
	  }

	  public void addZipFile(ZipFile zipFile) {
		  if (zipFile!=null) {
			  zipFiles.add(zipFile);
		  }
	  }

	  public void addDeleteFile(File deleteFile) {
		  if (deleteFile!=null) {
			  deleteFiles.add(deleteFile);
		  }
	  }

	  protected void finalize() throws Throwable {
		  cleanup();
	  }

	  public void cleanup() { // workaround for java mail bug

		  if (sourceStreams!=null) {
			  for (InputStream sourceStream : sourceStreams) {
				   	 if (sourceStreams!=null) {
					  	 StreamUtil.emptyStream(sourceStream);
						 try { sourceStream.close(); } catch (Exception e) {};
						 sourceStream = null;
				   	 }
			  }
		  }
		  if (zipFiles!=null) {
			  for (ZipFile zipFile : zipFiles) {
				  if (zipFile!=null) {
					  try { zipFile.close(); } catch (Exception e) {};
					  zipFile = null;
				  }
			  }
		  }

		  if (readers!=null) {
			  for (Reader reader : readers) {
				   	 if (reader!=null) {
						 try { reader.close(); } catch (Exception e) {};
						 reader = null;
				   	 }
			  }
		  }

		  if (deleteFiles!=null) {
			  for (File deleteFile : deleteFiles) {
				  deleteFile.delete();
			  }
		  }

		  reset();
	  }

}