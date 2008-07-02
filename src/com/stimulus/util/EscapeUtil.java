package com.stimulus.util;

import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


public final class EscapeUtil {
	
   public static String forHTML(String aText){
	   if (aText==null)
		   return "";
     final StringBuilder result = new StringBuilder();
     final StringCharacterIterator iterator = new StringCharacterIterator(aText);
     char character =  iterator.current();
     while (character != CharacterIterator.DONE ){
       if (character == '<') {
         result.append("&lt;");
       }
       else if (character == '>') {
         result.append("&gt;");
       }
       else if (character == '&') {
         result.append("&amp;");
      }
       else if (character == '\"') {
         result.append("&quot;");
       }
       else if (character == '\'') {
         result.append("&#039;");
       }
       else if (character == '(') {
         result.append("&#040;");
       }
       else if (character == ')') {
         result.append("&#041;");
       }
       else if (character == '#') {
         result.append("&#035;");
       }
       else if (character == '%') {
         result.append("&#037;");
       }
       else if (character == ';') {
         result.append("&#059;");
       }
       else if (character == '+') {
         result.append("&#043;");
       }
       else if (character == '-') {
         result.append("&#045;");
       }
       else {
         //the char is not a special one
         //add it to the result as is
         result.append(character);
       }
       character = iterator.next();
     }
     return result.toString();
  }
  
 
   public static String forURL(String aURLFragment){
     String result = null;
     try {
       result = URLEncoder.encode(aURLFragment, "UTF-8");
     }
     catch (UnsupportedEncodingException ex){
       throw new RuntimeException("UTF-8 not supported", ex);
     }
     return result;
   }

  public static String forXML(String aText){
    final StringBuilder result = new StringBuilder();
    final StringCharacterIterator iterator = new StringCharacterIterator(aText);
    char character =  iterator.current();
    while (character != CharacterIterator.DONE ){
      if (character == '<') {
        result.append("&lt;");
      }
      else if (character == '>') {
        result.append("&gt;");
      }
      else if (character == '\"') {
        result.append("&quot;");
      }
      else if (character == '\'') {
        result.append("&#039;");
      }
      else if (character == '&') {
         result.append("&amp;");
      }
      else {
        //the char is not a special one
        //add it to the result as is
        result.append(character);
      }
      character = iterator.next();
    }
    return result.toString();
  }

  public static String toDisableTags(String aText){
    final StringBuilder result = new StringBuilder();
    final StringCharacterIterator iterator = new StringCharacterIterator(aText);
    char character =  iterator.current();
    while (character != CharacterIterator.DONE ){
      if (character == '<') {
        result.append("&lt;");
      }
      else if (character == '>') {
        result.append("&gt;");
      }
      else {
        //the char is not a special one
        //add it to the result as is
        result.append(character);
      }
      character = iterator.next();
    }
    return result.toString();
  }
  
  public static String forRegex(String aRegexFragment){
    final StringBuilder result = new StringBuilder();

    final StringCharacterIterator iterator = new StringCharacterIterator(aRegexFragment);
    char character =  iterator.current();
    while (character != CharacterIterator.DONE ){
      /*
      * All literals need to have backslashes doubled.
      */
      if (character == '.') {
        result.append("\\.");
      }
      else if (character == '\\') {
        result.append("\\\\");
      }
      else if (character == '?') {
        result.append("\\?");
      }
      else if (character == '*') {
        result.append("\\*");
      }
      else if (character == '+') {
        result.append("\\+");
      }
      else if (character == '&') {
        result.append("\\&");
      }
      else if (character == ':') {
        result.append("\\:");
      }
      else if (character == '{') {
        result.append("\\{");
      }
      else if (character == '}') {
        result.append("\\}");
      }
      else if (character == '[') {
        result.append("\\[");
      }
      else if (character == ']') {
        result.append("\\]");
      }
      else if (character == '(') {
        result.append("\\(");
      }
      else if (character == ')') {
        result.append("\\)");
      }
      else if (character == '^') {
        result.append("\\^");
      }
      else if (character == '$') {
        result.append("\\$");
      }
      else {
        //the char is not a special one
        //add it to the result as is
        result.append(character);
      }
      character = iterator.next();
    }
    return result.toString();
  }
  
  public static String forScriptTagsOnly(String aText){
    String result = null;
    Matcher matcher = SCRIPT.matcher(aText);
    result = matcher.replaceAll("&lt;SCRIPT>");
    matcher = SCRIPT_END.matcher(result);
    result = matcher.replaceAll("&lt;/SCRIPT>");
    return result;
  }
  
  
  private static final Pattern SCRIPT = Pattern.compile(
    "<SCRIPT>", Pattern.CASE_INSENSITIVE
   );
  private static final Pattern SCRIPT_END = Pattern.compile(
    "</SCRIPT>", Pattern.CASE_INSENSITIVE
  );
  
  public static final String unescapeHTML(String s, int f){
	    String [][] escape =
	     {{  "&lt;"     , "<" } ,
	      {  "&gt;"     , ">" } ,
	      {  "&amp;"    , "&" } ,
	      {  "&quot;"   , "\"" } ,
	      {  "&agrave;" , "à" } ,
	      {  "&Agrave;" , "À" } ,
	      {  "&acirc;"  , "â" } ,
	      {  "&auml;"   , "ä" } ,
	      {  "&Auml;"   , "Ä" } ,
	      {  "&Acirc;"  , "Â" } ,
	      {  "&aring;"  , "å" } ,
	      {  "&Aring;"  , "Å" } , 
	      {  "&aelig;"  , "æ" } , 
	      {  "&AElig;"  , "Æ" } ,
	      {  "&ccedil;" , "ç" } ,
	      {  "&Ccedil;" , "Ç" } ,
	      {  "&eacute;" , "é" } ,
	      {  "&Eacute;" , "É" } ,
	      {  "&egrave;" , "è" } ,
	      {  "&Egrave;" , "È" } ,
	      {  "&ecirc;"  , "ê" } ,
	      {  "&Ecirc;"  , "Ê" } ,
	      {  "&euml;"   , "ë" } ,
	      {  "&Euml;"   , "Ë" } ,
	      {  "&iuml;"   , "ï" } , 
	      {  "&Iuml;"   , "Ï" } ,
	      {  "&ocirc;"  , "ô" } ,
	      {  "&Ocirc;"  , "Ô" } ,
	      {  "&ouml;"   , "ö" } ,
	      {  "&Ouml;"   , "Ö" } ,
	      {  "&oslash;" , "ø" } ,
	      {  "&Oslash;" , "Ø" } ,
	      {  "&szlig;"  , "ß" } ,
	      {  "&ugrave;" , "ù" } ,
	      {  "&Ugrave;" , "Ù" } ,
	      {  "&ucirc;"  , "û" } ,
	      {  "&Ucirc;"  , "Û" } , 
	      {  "&uuml;"   , "ü" } ,
	      {  "&Uuml;"   , "Ü" } ,
	      {  "&nbsp;"   , " " } ,
	      {  "&reg;"    , "\u00a9" } ,
	      {  "&copy;"   , "\u00ae" } ,
	      {  "&euro;"   , "\u20a0" } };
	     int i, j, k, l ;
	     
	     i = s.indexOf("&", f);
	     if (i > -1) {
	        j = s.indexOf(";" ,i);
	        // --------
	        // we don't start from the beginning 
	        // the next time, to handle the case of
	        // the &
	        // thanks to Pieter Hertogh for the bug fix!
	        f = i + 1;
	        // --------
	        if (j > i) {
	           // ok this is not most optimized way to
	           // do it, a StringBuffer would be better,
	           // this is left as an exercise to the reader!
	           String temp = s.substring(i , j + 1);
	           // search in escape[][] if temp is there
	           k = 0;
	           while (k < escape.length) {
	             if (escape[k][0].equals(temp)) break;
	             else k++;
	             }
	           if (k < escape.length) {
	             s = s.substring(0 , i) + escape[k][1] + s.substring(j + 1);
	             return unescapeHTML(s, f); // recursive call
	             }
	           }
	        }   
	     return s;
	     }
}
 