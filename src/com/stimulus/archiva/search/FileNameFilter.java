package com.stimulus.archiva.search;


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

import org.apache.commons.logging.*;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.Token;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Stack;


public class FileNameFilter extends TokenFilter  implements Serializable {

	private static final long serialVersionUID = -5280125822406627625L;

	protected static final Log logger = LogFactory.getLog(com.stimulus.archiva.search.FileNameFilter.class.getName());

    private Stack<Token> filenameTokenStack;
   
    public FileNameFilter(TokenStream in) {
        super(in);
        filenameTokenStack = new Stack<Token>();
    }

    @Override
	public Token next() throws IOException {

        if (filenameTokenStack.size() > 0) 
            return filenameTokenStack.pop();

        Token token = input.next();
       
        if (token == null) 
            return null;

        putPart(token);

        return token;
    }
     
    private void putPart(Token token) throws IOException {
    	String fileName = new String(token.termBuffer(),0,token.termLength());
    	if (fileName.indexOf('.')!=-1) {
    		String[] splitOnDot = fileName.split("\\.");
    		Token nameToken = new Token(splitOnDot[0].trim(),token.startOffset(),token.endOffset());
    		Token extToken = new Token(splitOnDot[1].trim(),token.startOffset(),token.endOffset());
    		filenameTokenStack.push(nameToken);
    		filenameTokenStack.push(extToken);
    	}
    }
    

}
