 
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

package com.stimulus.archiva.search;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.Token;

import com.stimulus.archiva.language.AnalyzerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

/* Many thanks to Michael J. Prichard" <michael_prich...@mac.com> for the email filter code */

public class EmailFilter extends TokenFilter {
	
	protected static final Logger logger = Logger.getLogger(com.stimulus.archiva.search.EmailFilter.class.getName());
	    
	 
    public static final String TOKEN_TYPE_EMAIL = "EMAILPART";

    private Stack<Token> emailTokenStack;
   
    public EmailFilter(TokenStream in) {
        super(in);
        emailTokenStack = new Stack<Token>();
    }

    public Token next() throws IOException {

        if (emailTokenStack.size() > 0) {
            return (Token) emailTokenStack.pop();
        }   

        Token token = input.next();
        if (token == null) {
            return null;
        }


        addEmailPartsToStack(token);

        return token;
    }
   
    private void addEmailPartsToStack(Token token) throws IOException {
    	//logger.debug("email index token: {token='"+token.termText()+"'}");
        String[] parts = null;
        
        if (!token.termText().contains("@")) {
        	parts = new String[1];
        	parts[0] = token.termText();
        } else
        	parts = getEmailParts(token.termText());

        if (parts == null) return;

        for (int i = 0; i < parts.length; i++) {
        	if (parts[i]!=null) {
	            Token synToken = new Token(parts[i].toLowerCase(),
	                                 token.startOffset(),
	                                 token.endOffset(),
	                                 TOKEN_TYPE_EMAIL);
	            synToken.setPositionIncrement(0);
	            emailTokenStack.push(synToken);
        	}
        }
    }

    /*
     * Parses emails into its parts for tokenization.
     * For example john@foo.com would be broken into
     *
     *    [john@foo.com]
     *    [john]
     *    [foo.com]
     *    [foo]
     *    [com]
     *      
     */
    private String[] getEmailParts(String email) {

    	 // so i can add them before calling toArray
        ArrayList<String> partsList = new ArrayList<String>();

        /* let's do it */
        // split on the @
        String[] splitOnAmpersand = email.split("@");
        // add the username
        try {
            partsList.add(splitOnAmpersand[0]);
        } catch (ArrayIndexOutOfBoundsException ae) {
            // ignore
        }

        // add the full host name
        try {
            partsList.add(splitOnAmpersand[1]);
        } catch (ArrayIndexOutOfBoundsException ae) {
            // ignore
        }

        // split the host name into pieces
        if (splitOnAmpersand.length > 1) {
            String[] splitOnDot = splitOnAmpersand[1].split("\\.");
            // add all pieces from splitOnDot
            for (int i=0; i < splitOnDot.length; i++) {
                partsList.add(splitOnDot[i]);
            }

            /*
             *  if this is great than 2 then we need to add the domain name which
             *  should be the last two
             * 
             */
            if (splitOnDot.length > 2) {
                String domain = splitOnDot[splitOnDot.length-2] + "." + splitOnDot[splitOnDot.length-1];
                // add domain
                partsList.add(domain);
            }
        }
       
        return (String[]) partsList.toArray(new String[0]);       
    }

}
