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
import org.apache.lucene.analysis.*;
import java.io.*;
import java.util.*;
import org.apache.lucene.analysis.standard.*;

public class ArchivaAnalyzer extends Analyzer {

      /*
       * An array containing some common words that
       * are not usually useful for searching.
       */
      private static final String[] STOP_WORDS =
      {
         "a"       , "and"     , "are"     , "as"      ,
         "at"      , "be"      , "but"     , "by"      ,
         "for"     , "if"      , "in"      , "into"    ,
         "is"      , "it"      , "no"      , "not"     ,
         "of"      , "on"      , "or"      , "s"       ,
         "such"    , "t"       , "that"    , "the"     ,
         "their"   , "then"    , "there"   , "these"   ,
         "they"    , "this"    , "to"      , "was"     ,
         "will"    ,
         "with"
      };

      /*
       * Stop table
       */
      final static private Set stopTable = StopFilter.makeStopSet(STOP_WORDS);

      /*
       * Create a token stream for this analyzer.
       */
      public final TokenStream tokenStream(final Reader reader)
      {
        TokenStream result = new StandardTokenizer(reader);

        result = new StandardFilter(result);
        result = new LowerCaseFilter(result);
        result = new StopFilter(result, stopTable);
        result = new PorterStemFilter(result);

        return result;
      }

}

