package com.stimulus.archiva.domain.fields;

import java.io.Serializable;
import org.apache.log4j.Logger;

public class EmailField implements Serializable {

	private static final long serialVersionUID = -4149461360865169664L;
	protected static Logger logger = Logger.getLogger(EmailField.class.getName());
	protected String  name;
	protected String resource;
	protected String index;
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
	
	
	


	public EmailField(String name, String resource, SearchMethod search, String index,  AllowExport export, 
					  AllowViewMail viewmail, AllowArchiveRule archiverule, AllowSearch allowSearch,  int columnSize, ShowInResults showResults) {
		this.name = name;
		this.resource = resource;
		this.search = search;
		this.index = index;
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
	
	public String getIndex() { return index; }

	public String getResource() { return resource; }

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
	
	@Override
	public String toString() {
		return getName();
	}

	
}
