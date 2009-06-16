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

import java.io.Serializable;
import java.util.StringTokenizer;
import com.stimulus.archiva.domain.fields.EmailField;
import com.stimulus.archiva.domain.fields.EmailFields;
import com.stimulus.util.Compare;
import java.util.Locale;
public class Criteria implements Serializable {

	private static final long serialVersionUID = 5689502035159175796L;
	String field = "all";
	Method method = Method.ALL;
	String query = "";
	Operator operator = Operator.AND;
	char[] specialChars = {':','+','-','&','|','!','(',')','{','}','[',']','^','~','\\'};
	public enum Operator { AND, OR };
	public enum Method { ANY, ALL, EXACT, NONE };


	public Criteria(String field) {
	  this.field = field;
	}

	public Criteria(String field, Method method, String query) {
	  this.method = method;
	  this.query = query;
	  this.field = field;
	}

	public Criteria(String field, Method method, String query, Operator operator) {
		this(field,method,query);
		this.operator = operator;
	}
	public String getQuery() { return query; }
	public void setQuery(String query) { this.query = query; }

	public Method getMethod() { return method; }
	public void setMethod(Method method) { this.method = method; }

	public void  setOperator(Operator operator) { this.operator = operator; }
	public Operator getOperator() { return this.operator; }

	public String getField() { return field; }
	public void setField(String field) { this.field = field; }


	public String escapeToken(String token) {
		StringBuffer newToken = new StringBuffer();
		for (int c = 0; c < token.length(); c++) {
			for (int i = 0; i <specialChars.length; i++) {
				if (token.charAt(c)==specialChars[i])
						newToken.append('\\');
			}
			newToken.append(token.charAt(c));
		}
		return newToken.toString();
	}

	public String allFields(String not, String token) {
	    StringBuffer constructedQuery = new StringBuffer();
	    //constructedQuery.append("(");
	    EmailFields emailFields = Config.getConfig().getEmailFields();
	    constructedQuery.append(not);
	    constructedQuery.append("(");
	    for (EmailField ef : emailFields.getAvailableFields().values()) {
	    	String field = ef.getName();
	    	constructedQuery.append(field);
	    	constructedQuery.append(":");
	    	constructedQuery.append(escapeToken(token));
	    	constructedQuery.append(" ");
	    }
	    constructedQuery.append(")");
	  return constructedQuery.toString().trim(); // + ") ";
	}

	public String anyAddress(String token) {
		return "( to:" + escapeToken(token) +" from:" + escapeToken(token) + " cc:" + escapeToken(token) +
			   " bcc:" + escapeToken(token)+" )";
	}


	public String getConstructedQuery() {
	    StringBuffer constructedQuery = new StringBuffer();
	    if (Compare.equalsIgnoreCase(method.toString(), "all")) {
	    	boolean start = true;
	    	StringTokenizer allWordsTokenizer = new StringTokenizer(query);
	    	while (allWordsTokenizer.hasMoreTokens()) {
	    		if (!start) constructedQuery.append("AND ");
		  		    if (Compare.equalsIgnoreCase(field, "all")) {
		  		    	constructedQuery.append(allFields("",allWordsTokenizer.nextToken()).toLowerCase(Locale.ENGLISH));
		  		    	constructedQuery.append(" ");
		  		    } else if(Compare.equalsIgnoreCase(field, "addresses")) {
		  		    	constructedQuery.append(anyAddress(allWordsTokenizer.nextToken()).toLowerCase(Locale.ENGLISH));
		  		    	constructedQuery.append(" ");
		  		    } else {

		  	   			constructedQuery.append(field);
		  	   			constructedQuery.append(":");
		  	   			constructedQuery.append(escapeToken(allWordsTokenizer.nextToken().toLowerCase(Locale.ENGLISH)));
		  	   			constructedQuery.append(" ");
		  		    }
		  	   		start=false;
	  			}
		} else if (Compare.equalsIgnoreCase(method.toString(), "exact")) {

		 	if (Compare.equalsIgnoreCase(field, "all")) {
				constructedQuery.append(allFields("","\"" + query + "\" "));
		 	} else if (Compare.equalsIgnoreCase(field, "addresses")) {
  		    	constructedQuery.append(anyAddress("\"" + query + "\" "));
  		    } else {
				constructedQuery.append(field);
				constructedQuery.append(":\"");
				constructedQuery.append(query);
				constructedQuery.append("\" ");
		 	}
	 	} else if (Compare.equalsIgnoreCase(method.toString(), "any")) {
	 		StringTokenizer anyWordsTokenizer = new StringTokenizer(query);
	 		while (anyWordsTokenizer.hasMoreTokens()) {
		  	   		if (Compare.equalsIgnoreCase(field, "all")) {
		  	   			constructedQuery.append(allFields("",anyWordsTokenizer.nextToken()).toLowerCase(Locale.ENGLISH));
		  	   			constructedQuery.append(" ");
			  	   	} else if (Compare.equalsIgnoreCase(field, "addresses")) {
		  		    	constructedQuery.append(anyAddress(anyWordsTokenizer.nextToken()).toLowerCase(Locale.ENGLISH));
		  		    	constructedQuery.append(" ");
		  		    } else {
			  	   			constructedQuery.append(field);
			  	   			constructedQuery.append(":");
			  	   			constructedQuery.append(escapeToken(anyWordsTokenizer.nextToken()).toLowerCase(Locale.ENGLISH));
			  	   			constructedQuery.append(" ");
			  	   		}
			  	   	}

	 	} else if (Compare.equalsIgnoreCase(method.toString(), "none")) {
	 		StringTokenizer noWordsTokenizer = new StringTokenizer(query);
	 		while (noWordsTokenizer.hasMoreTokens()) {
		 		if (Compare.equalsIgnoreCase(field, "all")) {
		 			constructedQuery.append(allFields("!",noWordsTokenizer.nextToken()).toLowerCase(Locale.ENGLISH));
		 			constructedQuery.append(" ");
		 		} else if (Compare.equalsIgnoreCase(field, "addresses")) {
	  		    	constructedQuery.append(anyAddress(noWordsTokenizer.nextToken()).toLowerCase(Locale.ENGLISH));
	  		    	constructedQuery.append(" ");
	  		   } else {
		 			constructedQuery.append("!");
		 			constructedQuery.append(field);
		 			constructedQuery.append(":");
		 			constructedQuery.append(noWordsTokenizer.nextToken().toLowerCase(Locale.ENGLISH));
		 			constructedQuery.append(" ");
		 		}
	 		}
	 	}
	    return constructedQuery.toString().trim();
	}
}
