package com.stimulus.archiva.search;

import java.io.Reader;
import java.io.Serializable;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;

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

		  @Override
		public TokenStream tokenStream(String fieldName, Reader reader) {

		    TokenStream result = new CharTokenizer(reader) {

		      @Override
			protected boolean isTokenChar(char c) {
		        return c != fDelim;
		      }
		    };
		    result = new LowerCaseFilter(result);
		    return result;
		  }
	}
	
	  @Override
	public final TokenStream tokenStream(String fieldName,final Reader reader)
      {
          TokenStream result = new LowercaseDelimiterAnalyzer('.').tokenStream(fieldName, reader);
          return result;
     }

	
}
