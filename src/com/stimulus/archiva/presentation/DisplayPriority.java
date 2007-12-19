package com.stimulus.archiva.presentation;

import java.io.Serializable;
import java.util.Locale;

import com.stimulus.archiva.domain.fields.EmailFieldValue;

public class DisplayPriority extends DisplayField implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4615827437297129388L;

	public DisplayPriority(EmailFieldValue efv) {
		super(efv);
	}
	
	public String getDisplay() {
	  switch(Integer.valueOf(getValue())) {
          case 1: return "highest";
          case 2: return "high";
          case 3: return "normal";
          case 4: return "low";
          case 5: return "lowest";
          default: return "normal";
      }
	}
	
	
}
