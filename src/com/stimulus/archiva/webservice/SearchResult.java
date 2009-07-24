package com.stimulus.archiva.webservice;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.*;
import com.stimulus.archiva.domain.Search.Result;
import com.stimulus.archiva.domain.fields.*;
import java.util.*;
import com.stimulus.archiva.exception.*;

public class SearchResult {
	
	protected static final Log logger = LogFactory.getLog(SearchResult.class.getName());
	protected Result result;
	
	protected SearchResult(Result result) {
		this.result = result;
	}
	
	public String getEmailId() throws AxisFault { 
		try {
			return result.getEmailId().getUniqueID(); 
		} catch (Exception e) {
			throw new AxisFault("failed to retrieve email id:"+e.getMessage());
		}
	}
	public String getVolumeId() throws AxisFault { 
		try {
			return result.getEmailId().getVolume().getID();
		} catch (Exception e) {
			throw new AxisFault("failed to retrieve volume id:"+e.getMessage());
		}
	}
	
	public String[] getFields() throws AxisFault { 
		ArrayList<String> emailFields = new ArrayList<String>();
		for (EmailField ef : EmailFields.emailFields.values()) {
			try {
				if (ef.isStored() || ef.getName().equalsIgnoreCase("score")) {
					emailFields.add(ef.getName());
				}
			} catch (Exception e) {
				logger.debug("failed to retrieve field:"+e.getMessage(),e);
			};
		}
		return emailFields.toArray(new String[]{});
	}
	
	public String[] getFieldValues() throws AxisFault {
		ArrayList<String> emailFields = new ArrayList<String>();
		for (EmailField ef : EmailFields.emailFields.values()) {
			try {
				if (ef.isStored() || ef.getName().equalsIgnoreCase("score")) {
					emailFields.add(result.getFieldValue(ef.getName()).getValue());
				}
			} catch (Exception e) {
				logger.debug("failed to retrieve field value:"+e.getMessage(),e);
			};
		}
		return emailFields.toArray(new String[]{});
	}
	
	

}