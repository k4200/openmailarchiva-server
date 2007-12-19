package org.apache.lucene.analysis.tr;



/**
 * A stemmer for Turkish words.
 * Based on brazillian stemmer
 * @author Emre Bayram
 */
public class TurkishStemmer {

	/**
	 * Changed term
	 */

	private   String CT ;	



	public TurkishStemmer() {
	}

	/**
	 * Stemms the given term to an unique <tt>discriminator</tt>.
	 *
	 * @param term  The term that should be stemmed.
	 * @return      Discriminator for <tt>term</tt>
	 */
	protected String stem( String term ) {
	boolean altered = false ; // altered the term

	// creates CT
	createCT(term) ;

		if ( !isIndexable( CT ) ) {
			return null;
		}
		if ( !isStemmable( CT ) ) {
			return CT ;
		}



	return CT ;
	}
	private boolean isStemmable( String term ) {
		for ( int c = 0; c < term.length(); c++ ) {
			// Discard terms that contain non-letter characters.
			if ( !Character.isLetter(term.charAt(c))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks a term if it can be processed indexed.
	 *
	 * @return  true if it can be indexed
	 */
	private boolean isIndexable( String term ) {
		return (term.length() < 30) && (term.length() > 2) ;
	}

	/**
	 * See if string is 'a','e','i','o','u'
   *
   * @return true if is vowel
	 */
	private boolean isVowel( char value ) {
	return (value == 'a') ||
		   (value == 'e') ||
		   (value == 'i') ||
		   (value == 'o') ||
		   (value == 'u') ;
  }

	
	/**
   * 1) Turn to lowercase
   * 2) Remove accents
   * 3) Ç -> c; Ö -> o
   * 4) ? -> s
   *
   * @return null or a string transformed
	 */
	private String changeTerm( String value ) {
	int     j;
	String  r = "" ;

	// be-safe !!!
	if (value == null) {
	  return null ;
	}


	value = value.toLowerCase() ;
	for (j=0 ; j < value.length() ; j++) {
	  if ((value.charAt(j) == 'ç') ||
		  (value.charAt(j) == 'Ç') ) {
		r= r + "c" ; continue ;
	  }
	  if ((value.charAt(j) == '\u0130') ||
		  (value.charAt(j) == '\u0131')) {
		r= r + "i" ; continue ;
	  }
	  if ((value.charAt(j) == '\u015f') ||
		   (value.charAt(j) == '\u015e')) {
		 r= r + "s" ; continue ;
	   }
	   if ((value.charAt(j) == '\u011e') ||
		   (value.charAt(j) == '\u011f')) {
		 r= r + "g" ; continue ;
	   }
	  if ((value.charAt(j) == 'ü') ||
		  (value.charAt(j) == 'Ü')) {
		r= r + "u" ; continue ;
	  }
	  if ((value.charAt(j) == 'ö') ||
				(value.charAt(j) == 'Ö')) {
			  r= r + "o" ; continue ;
			}

	  r= r+ value.charAt(j) ;
	}

	return r ;
  }


	/**
	 */
	private void createCT( String term ) {
	CT = changeTerm(term) ;

	if (CT.length() < 2) return ;

	// if the first character is ... , remove it
	if ((CT.charAt(0) == '"')  ||
		(CT.charAt(0) == '\'') ||
		(CT.charAt(0) == '-')  ||
		(CT.charAt(0) == ',')  ||
		(CT.charAt(0) == ';')  ||
		(CT.charAt(0) == '.')  ||
		(CT.charAt(0) == '?')  ||
		(CT.charAt(0) == '!')
		) {
		CT = CT.substring(1);
	}

	if (CT.length() < 2) return ;

	// if the last character is ... , remove it
	if ((CT.charAt(CT.length()-1) == '-') ||
		(CT.charAt(CT.length()-1) == ',') ||
		(CT.charAt(CT.length()-1) == ';') ||
		(CT.charAt(CT.length()-1) == '.') ||
		(CT.charAt(CT.length()-1) == '?') ||
		(CT.charAt(CT.length()-1) == '!') ||
		(CT.charAt(CT.length()-1) == '\'') ||
		(CT.charAt(CT.length()-1) == '"')
		) {
		CT = CT.substring(0,CT.length()-1);
	}
  }


	/**
	 * for future improvements
	 */
	private boolean step1() {
	return false ;
  }


  

	

}


