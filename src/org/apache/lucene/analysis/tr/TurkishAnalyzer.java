package org.apache.lucene.analysis.tr;



import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WordlistLoader;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Set;

/**
 * Analyzer for Turkish. 
 * @author    Emre Bayram
 */
public final class TurkishAnalyzer extends Analyzer {

	/**
	 * List of typical Turkish stopwords.
	 */

	
	public final static String[] TURKISH_STOP_WORDS = {
	  "acaba",
"altm\u0131\u015f",
"alt\u0131",
"ama",
"bana",
"baz\u0131",
"belki",
"ben",
"benden",
"beni",
"benim",
"be\u015f",
"bin",
"bir",
"biri",
"birkaç",
"birkez",
"bir\u015fey",
"bir\u015feyi",
"biz",
"bizden",
"bizi",
"bizim",
"bu",
"buna",
"bunda",
"bundan",
"bunu",
"bunun",
"da",
"daha",
"dahi",
"de",
"defa",
"diye",
"doksan",
"dokuz",
"dört",
"elli",
"en", 
"gibi",
"hem",
"hep",
"hepsi",
"her",
"hiç",
"iki",
"ile",
"ise",
"için",
"katrilyon",
"kez",
"ki",
"kim",
"kimden",
"kime",
"kimi",
"k\u0131rk",
"milyar",
"milyon",
"mu",
"mü",
"m\u0131",
"nas\u0131l",
"ne",
"neden",
"nerde",
"nerede",
"nereye",
"niye",
"niçin",
"on",
"ona",
"ondan",
"onlar",
"onlardan",
"onlar\u0131n",
"onu",
"otuz",
"sanki",
"sekiz",
"seksen",
"sen",
"senden",
"seni",
"senin",
"siz",
"sizden",
"sizi",
"sizin",
"trilyon",
"tüm",
"ve",
"veya",
"ya",
"yani",
"yedi",
"yetmi\u015f",
"yirmi",
"yüz",
"çok",
"çünkü",
"üç",
"\u015fey",
"\u015feyden",
"\u015feyi",
"\u015feyler",
"\u015fu",
"\u015funa",
"\u015funda",
"\u015fundan",
"\u015funu"};


	/**
	 * Contains the stopwords used with the StopFilter.
	 */
	private Set stoptable = new HashSet();
	
	/**
	 * Contains words that should be indexed but not stemmed.
	 */
	private Set excltable = new HashSet();

	/**
	 * Builds an analyzer with the default stop words ({@link #BRAZILIAN_STOP_WORDS}).
	 */
	public TurkishAnalyzer() {
		stoptable = StopFilter.makeStopSet( TURKISH_STOP_WORDS );
	}

	/**
	 * Builds an analyzer with the given stop words.
	 */
	public TurkishAnalyzer( String[] stopwords ) {
		stoptable = StopFilter.makeStopSet( stopwords );
	}

	/**
	 * Builds an analyzer with the given stop words.
	 */
	public TurkishAnalyzer( Hashtable stopwords ) {
		stoptable = new HashSet(stopwords.keySet());
	}

	/**
	 * Builds an analyzer with the given stop words.
	 */
	public TurkishAnalyzer( File stopwords ) throws IOException {
		stoptable = WordlistLoader.getWordSet( stopwords );
	}

	/**
	 * Builds an exclusionlist from an array of Strings.
	 */
	public void setStemExclusionTable( String[] exclusionlist ) {
		excltable = StopFilter.makeStopSet( exclusionlist );
	}
	/**
	 * Builds an exclusionlist from a Hashtable.
	 */
	public void setStemExclusionTable( Hashtable exclusionlist ) {
		excltable = new HashSet(exclusionlist.keySet());
	}
	/**
	 * Builds an exclusionlist from the words contained in the given file.
	 */
	public void setStemExclusionTable( File exclusionlist ) throws IOException {
		excltable = WordlistLoader.getWordSet( exclusionlist );
	}

	/**
	 * Creates a TokenStream which tokenizes all the text in the provided Reader.
	 *
	 * @return  A TokenStream build from a StandardTokenizer filtered with
	 * 			StandardFilter, StopFilter, GermanStemFilter and LowerCaseFilter.
	 */
	public final TokenStream tokenStream(String fieldName, Reader reader) {
		TokenStream result = new StandardTokenizer( reader );
		result = new StandardFilter( result );
		result = new StopFilter( result, stoptable );
		result = new TurkishStemFilter( result, excltable );
		// Convert to lowercase after stemming!
		result = new LowerCaseFilter( result );
		return result;
	}
}


