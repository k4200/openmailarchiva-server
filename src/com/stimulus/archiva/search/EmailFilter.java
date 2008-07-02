 
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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Stack;

/* Many thanks to Michael J. Prichard" <michael_prich...@mac.com> for his
 * original the email filter code. It is rewritten. */

public class EmailFilter extends TokenFilter  implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5280145822406627625L;


	protected static final Logger logger = Logger.getLogger(com.stimulus.archiva.search.EmailFilter.class.getName());
	    
	 
    public static final String TOKEN_TYPE_EMAIL = "EMAILPART";

    private Stack<Token> emailTokenStack;
   
    public EmailFilter(TokenStream in) {
        super(in);
        emailTokenStack = new Stack<Token>();
    }

    @Override
	public Token next() throws IOException {

        if (emailTokenStack.size() > 0) 
            return emailTokenStack.pop();

        Token token = input.next();
       
        if (token == null) 
            return null;

        putPart(token);

        return token;
    }
   
    private void putPart(Token token) throws IOException {
    	String emailAddress = token.termText();
    	emailAddress = emailAddress.replaceAll("<", "");
    	emailAddress = emailAddress.replaceAll(">", "");
    	emailAddress = emailAddress.replaceAll("\"", "");
    	
    	String [] parts = extractEmailParts(emailAddress);
    
        String partout ="";
        for (int i = 0; i < parts.length; i++) {
        	partout += parts[i]+"_";
        	if (parts[i]!=null) {
	            Token subToken = new Token(parts[i].trim(),
	                                 token.startOffset(),
	                                 token.endOffset(),
	                                 TOKEN_TYPE_EMAIL);
	            subToken.setPositionIncrement(0);
	            emailTokenStack.push(subToken);
        	}
        }
    }

   
    private String[] extractWhitespaceParts(String email) {
    	String[] whitespaceParts = email.split(" ");
    	ArrayList<String> partsList = new ArrayList<String>();
    	for (int i=0; i < whitespaceParts.length; i++) {
    		partsList.add(whitespaceParts[i]);
    	}
    	return whitespaceParts;
    }
    
    private String[] extractEmailParts(String email) {

    	if (email.indexOf('@')==-1)
    		return extractWhitespaceParts(email);
    	
    	ArrayList<String> partsList = new ArrayList<String>();
    	
    	String[] whitespaceParts = extractWhitespaceParts(email);
    	
    	 for (int w=0;w<whitespaceParts.length;w++) {
    		 
    		 if (whitespaceParts[w].indexOf('@')==-1)
    			 partsList.add(whitespaceParts[w]);
    		 else {
    			 partsList.add(whitespaceParts[w]);
    			 String[] splitOnAmpersand = whitespaceParts[w].split("@");
    			 try {
    				 partsList.add(splitOnAmpersand[0]);
    		            partsList.add(splitOnAmpersand[1]);
    		     } catch (ArrayIndexOutOfBoundsException ae) {}

		        if (splitOnAmpersand.length > 1) {
		            String[] splitOnDot = splitOnAmpersand[1].split("\\.");
		            for (int i=0; i < splitOnDot.length; i++) {
		                partsList.add(splitOnDot[i]);
		            }

		            if (splitOnDot.length > 2) {
		                String domain = splitOnDot[splitOnDot.length-2] + "." + splitOnDot[splitOnDot.length-1];
		                partsList.add(domain);
		            }
		        }
    		 }	 
    	 }
        return partsList.toArray(new String[0]);       
    }

}
