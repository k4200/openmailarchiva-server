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

package com.stimulus.archiva.persistence;

import com.stimulus.archiva.domain.EmailID;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;
import java.io.*;

public class LuceneEmailID extends EmailID {

    int index;
    Hits hits;
    float score;
    boolean fetchDoc = false;
    protected static final Logger logger = Logger.getLogger(MessageSearch.class.getName());

    public LuceneEmailID(int index, Hits hits) {
        this.index = index;
        this.hits = hits;
    }

    public String getUniqueID() {
        fetchDocument();
        return uniqueId;
    }

    protected void fetchDocument() {
        try {
	        if (!fetchDoc) {
	            Document doc = hits.doc(index);
	            uniqueId = doc.get("uid");
	            fetchDoc = true;
	        }
        } catch(IOException io) {
            logger.error("failed to retrieve uniqueId from index");
        }
    }
}
