package com.stimulus.archiva.taglib.html;

/**
 * An extension of the struts form tag to allow arbitrary form attributes to be set.
 * Intended to be used primarily for introducing the autocomplete attribute supported
 * by most browsers.
 *
 */
public class FormTag extends org.apache.struts.taglib.html.FormTag {
	    /**
	 * 
	 */
	private static final long serialVersionUID = -8196803705160742679L;
		protected String autocomplete;
	    public String getAutocomplete() {
	        return autocomplete;
	    }
	    public void setAutocomplete(String autocomplete) {
	        this.autocomplete = autocomplete;
	    }
	    public void prepareOtherAttributes(StringBuffer sb) {
	        if (autocomplete != null) {
	            sb.append(" autocomplete=\""+autocomplete+"\"");
	        }
	    }
}