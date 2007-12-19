package com.stimulus.archiva.domain.fields;
import java.io.Serializable;

public class EmailFieldValue implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5561933760479416519L;
	EmailField field;
	String value;
	
	public EmailFieldValue(EmailField field, String value) {
		this.field = field;
		this.value = value;
	}
	
	public EmailField getField() { return field; }
	public String getValue() { return value; }

	public String toString() {
		return getField()+"='"+getValue()+"'";
	}
}
