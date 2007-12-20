/*
 * Subversion Infos:
 * $URL$
 * $Author$
 * $Date$
 * $Rev$
*/

		/* Copyright (C) 2005-2007 Jamie Angus Band 
 * MailArchiva Open Source Edition Copyright (c) 2005-2007 Jamie Angus Band
 * This program is free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation; either version
 * 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see http://www.gnu.org/licenses or write to the Free Software Foundation,Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

package com.stimulus.archiva.domain.fields;

import java.io.Serializable;

import org.apache.log4j.Logger;

import com.stimulus.archiva.domain.Email;
import com.stimulus.util.OrderedHashtable;

public class EmailField implements Serializable {

	 /**
	 * 
	 */
	private static final long serialVersionUID = -4149461360865169664L;
	protected static Logger logger = Logger.getLogger(Email.class);
	protected String  name;
	protected String resourceKey;
	protected String indexKey;
	protected int columnSize;
	
	protected AllowArchiveRule archiveRule;
	protected AllowViewMail viewmail;
	protected AllowSearch allowSearch;

	protected SearchMethod search;
	protected AllowExport export;
	protected ShowInResults showResults;
	
	public enum SearchMethod { TOKENIZED, STORED, TOKENIZED_AND_STORED, NONE };
	public enum AllowExport { EXPORT, NOEXPORT };
	public enum AllowViewMail { VIEWMAIL, NOVIEWMAIL };
	public enum AllowArchiveRule { ARCHIVERULE, NOARCHIVERULE };
	public enum AllowSearch { SEARCH, NOSEARCH };
	public enum StoreInIndex { INDEXSTORE, NOINDEXSTORE};
	public enum ShowInResults { RESULTS, NORESULTS, CONDITIONAL };
	
	public static OrderedHashtable emailFields;
	
	static {
		emailFields = new OrderedHashtable();
		
		emailFields.put("priority", new EmailField("priority","field_label_priority",SearchMethod.STORED,"priority",
                AllowExport.EXPORT,AllowViewMail.VIEWMAIL,AllowArchiveRule.ARCHIVERULE,AllowSearch.SEARCH,1,ShowInResults.RESULTS));
		
		emailFields.put("attach", new EmailField("attach","field_label_attach",SearchMethod.STORED,"attach",
                AllowExport.EXPORT,AllowViewMail.VIEWMAIL,AllowArchiveRule.NOARCHIVERULE,AllowSearch.NOSEARCH,1,ShowInResults.RESULTS));
		
		emailFields.put("score", new EmailField("score","field_label_score",SearchMethod.NONE,"score",
                AllowExport.NOEXPORT,AllowViewMail.NOVIEWMAIL,AllowArchiveRule.NOARCHIVERULE,AllowSearch.NOSEARCH,5,ShowInResults.RESULTS));
		
		emailFields.put("size", new EmailField("size","field_label_size",SearchMethod.STORED,"size",
                AllowExport.EXPORT,AllowViewMail.VIEWMAIL,AllowArchiveRule.ARCHIVERULE,AllowSearch.SEARCH,5,ShowInResults.RESULTS));
		
		// set to NOSEARCH, as will always be searchable but must not appear in search field drop down
		emailFields.put("sentdate", new EmailField("sentdate","field_label_sentdate",SearchMethod.STORED,"sentdate",
                AllowExport.EXPORT,AllowViewMail.VIEWMAIL,AllowArchiveRule.ARCHIVERULE,AllowSearch.NOSEARCH,12,ShowInResults.CONDITIONAL));
		
		emailFields.put("archivedate", new EmailField("archivedate","field_label_archivedate",SearchMethod.STORED,"archivedate",
                AllowExport.EXPORT,AllowViewMail.VIEWMAIL,AllowArchiveRule.ARCHIVERULE,AllowSearch.NOSEARCH,12,ShowInResults.CONDITIONAL));
		
		//emailFields.put("receivedate", new EmailField("receivedate","field_label_receivedate",SearchMethod.STORED,"receivedate",
       //        AllowExport.EXPORT,AllowViewMail.VIEWMAIL,AllowArchiveRule.ARCHIVERULE,AllowSearch.NOSEARCH,12,ShowInResults.CONDITIONAL));
		
		emailFields.put("from", new EmailField("from","field_label_from",SearchMethod.TOKENIZED_AND_STORED,"from",
                AllowExport.EXPORT,AllowViewMail.VIEWMAIL,AllowArchiveRule.ARCHIVERULE,AllowSearch.SEARCH,20,ShowInResults.RESULTS));
		
		emailFields.put("to", new EmailField("to","field_label_to",SearchMethod.TOKENIZED_AND_STORED,"to",
                AllowExport.EXPORT,AllowViewMail.VIEWMAIL,AllowArchiveRule.ARCHIVERULE,AllowSearch.SEARCH,30,ShowInResults.RESULTS));
		
		emailFields.put("subject", new EmailField("subject","field_label_subject",SearchMethod.TOKENIZED_AND_STORED,"subject",
                AllowExport.EXPORT,AllowViewMail.VIEWMAIL,AllowArchiveRule.ARCHIVERULE,AllowSearch.SEARCH,30,ShowInResults.RESULTS));
		
		emailFields.put("cc", new EmailField("cc","field_label_cc",SearchMethod.TOKENIZED,"cc",
                AllowExport.EXPORT,AllowViewMail.VIEWMAIL,AllowArchiveRule.ARCHIVERULE,AllowSearch.SEARCH,1,ShowInResults.NORESULTS));
		
		emailFields.put("bcc", new EmailField("bcc","field_label_bcc",SearchMethod.TOKENIZED,"bcc",
                AllowExport.EXPORT,AllowViewMail.VIEWMAIL,AllowArchiveRule.ARCHIVERULE,AllowSearch.SEARCH,1,ShowInResults.NORESULTS));
		
		emailFields.put("deliveredto", new EmailField("deliveredto","field_label_deliveredto",SearchMethod.TOKENIZED,"deliveredto",
                AllowExport.EXPORT,AllowViewMail.VIEWMAIL,AllowArchiveRule.ARCHIVERULE,AllowSearch.SEARCH,1,ShowInResults.NORESULTS));
		
		emailFields.put("flag", new EmailField("flag","field_label_flag",SearchMethod.TOKENIZED,"flag",
                AllowExport.EXPORT,AllowViewMail.VIEWMAIL,AllowArchiveRule.ARCHIVERULE,AllowSearch.SEARCH,1,ShowInResults.NORESULTS));
		
		emailFields.put("sender", new EmailField("sender","field_label_sender",SearchMethod.TOKENIZED,"sender",
                AllowExport.EXPORT,AllowViewMail.VIEWMAIL,AllowArchiveRule.ARCHIVERULE,AllowSearch.SEARCH,1,ShowInResults.NORESULTS));
		
		emailFields.put("recipient", new EmailField("recipient","field_label_recipient",SearchMethod.TOKENIZED,"recipient",
                AllowExport.EXPORT,AllowViewMail.VIEWMAIL,AllowArchiveRule.ARCHIVERULE,AllowSearch.SEARCH,1,ShowInResults.NORESULTS));
		
		emailFields.put("body", new EmailField("body","field_label_body",SearchMethod.TOKENIZED,"body",
                AllowExport.NOEXPORT,AllowViewMail.NOVIEWMAIL,AllowArchiveRule.ARCHIVERULE,AllowSearch.SEARCH,1,ShowInResults.NORESULTS));

		emailFields.put("attachments", new EmailField("attachments","field_label_attachments",SearchMethod.TOKENIZED,"attachments",
                AllowExport.NOEXPORT,AllowViewMail.NOVIEWMAIL,AllowArchiveRule.ARCHIVERULE,AllowSearch.SEARCH,1,ShowInResults.NORESULTS));
		
		emailFields.put("attachname", new EmailField("attachname","field_label_attachname",SearchMethod.TOKENIZED,"attachname",
                AllowExport.NOEXPORT,AllowViewMail.NOVIEWMAIL,AllowArchiveRule.ARCHIVERULE,AllowSearch.SEARCH,1,ShowInResults.NORESULTS));

	}
	
	public static OrderedHashtable getAvailableFields() {
		return emailFields;
	}
	public static EmailField get(String name) { 
		
		if (name==null) {
			logger.debug("name is null");
			
		}
		if (emailFields==null)
			logger.debug("emailfields is null");
		return (EmailField)emailFields.get(name); 	
	} 	


	public EmailField(String name, String resourceKey, SearchMethod search, String indexKey,  AllowExport export, 
					  AllowViewMail viewmail, AllowArchiveRule archiverule, AllowSearch allowSearch,  int columnSize, ShowInResults showResults) {
		this.name = name;
		this.resourceKey = resourceKey;
		this.search = search;
		this.indexKey = indexKey;
		this.export = export;
		this.viewmail = viewmail;
		this.archiveRule = archiverule;
		this.allowSearch = allowSearch;
		this.columnSize = columnSize;
		this.showResults = showResults;
		
		
	}
	
	public AllowExport getExport() { return export; }
	
	public String getName() { return name; }
	
	public SearchMethod getSearchMethod() { return search; }

	public AllowArchiveRule getArchiveRule() { return archiveRule; }
	
	public String getDisplayName() { return getName(); }

	public int getColumnSize() { return columnSize; }

	public AllowViewMail getViewEmail() { return viewmail; }
	
	public String getIndexKey() { return indexKey; }

	public String getResourceKey() { return resourceKey; }

	public boolean isStored() {
		return (search==SearchMethod.STORED || search==SearchMethod.TOKENIZED_AND_STORED);
	}
	
	public ShowInResults getShowInResults() { 
		return showResults;
	}
	
	public boolean getShowResults() { 
		return showResults.equals(ShowInResults.RESULTS);
	}
	
	public boolean getShowConditional() { 
		return showResults.equals(ShowInResults.CONDITIONAL);
	}
	
	public AllowSearch getAllowSearch() { return allowSearch; }
	
	public String toString() {
		return getName();
	}

	
}
