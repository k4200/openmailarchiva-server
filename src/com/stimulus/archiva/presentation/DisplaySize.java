package com.stimulus.archiva.presentation;

import java.io.Serializable;
import java.util.Locale;

import com.stimulus.archiva.domain.fields.EmailFieldValue;

public class DisplaySize extends DisplayField implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2076962167816755817L;

	public DisplaySize(EmailFieldValue efv) {
		super(efv);
	}
	
	public String getDisplay() {
	  return getValue()+"k";
	}
}
