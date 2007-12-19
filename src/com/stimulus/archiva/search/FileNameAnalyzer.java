package com.stimulus.archiva.search;

import java.io.Reader;
import java.io.Serializable;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;

import com.stimulus.archiva.search.EmailAnalyzer.LowercaseDelimiterAnalyzer;

public class FileNameAnalyzer extends Analyzer  implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5719768857698540941L;

	public class LowercaseDelimiterAnalyzer extends Analyzer {
		  
		private final char fDelim;

		  public LowercaseDelimiterAnalyzer(char delim) {
		    fDelim = delim;
		  }

		  public TokenStream tokenStream(String fieldName, Reader reader) {

		    TokenStream result = new CharTokenizer(reader) {

		      protected boolean isTokenChar(char c) {
		        return c != fDelim;
		      }
		    };
		    result = new LowerCaseFilter(result);
		    return result;
		  }
	}
	
	  public final TokenStream tokenStream(String fieldName,final Reader reader)
      {
          TokenStream result = new LowercaseDelimiterAnalyzer('.').tokenStream(fieldName, reader);
          return result;
     }

	
}
