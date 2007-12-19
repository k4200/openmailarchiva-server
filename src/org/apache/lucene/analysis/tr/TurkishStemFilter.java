package org.apache.lucene.analysis.tr;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

/**
 * Based on GermanStemFilter
 * @author Emre Bayram
 */
public final class TurkishStemFilter extends TokenFilter {

  /**
   * The actual token in the input stream.
   */
  private Token token = null;
  private TurkishStemmer stemmer = null;
  private Set exclusions = null;

  public TurkishStemFilter(TokenStream in) {
	super(in);
	stemmer = new TurkishStemmer();
  }

  public TurkishStemFilter(TokenStream in, Set exclusiontable) {
	this(in);
	this.exclusions = exclusiontable;
  }

  /**
   * @return Returns the next token in the stream, or null at EOS.
   */
  public final Token next()
	  throws IOException {
	if ((token = input.next()) == null) {
	  return null;
	}
	// Check the exclusiontable.
	else if (exclusions != null && exclusions.contains(token.termText())) {
	  return token;
	} else {
	  String s = stemmer.stem(token.termText());
	  // If not stemmed, dont waste the time creating a new token.
	  if ((s != null) && !s.equals(token.termText())) {
		return new Token(s, token.startOffset(), token.endOffset(), token.type());
	  }
	  return token;
	}
  }
}



