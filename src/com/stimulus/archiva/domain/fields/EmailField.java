
/* Copyright (C) 2005-2009 Jamie Angus Band
 * MailArchiva Open Source Edition Copyright (c) 2005-2009 Jamie Angus Band
 * This program is free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation; either version
 * 3 of the License, or (at your option) any later version.
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
import org.apache.commons.logging.*;

public class EmailField implements Serializable, Cloneable {

	private static final long serialVersionUID = -4149461360865169664L;
	protected static Log logger = LogFactory.getLog(EmailField.class.getName());
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

	public EmailField clone() {
		return new EmailField(name,resource,search,index,export,viewmail,archiveRule,allowSearch,columnSize,showResults);

	}


}
