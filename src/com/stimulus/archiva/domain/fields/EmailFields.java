package com.stimulus.archiva.domain.fields;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Locale;
import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.*;
import com.stimulus.archiva.domain.*;
import com.stimulus.archiva.domain.fields.EmailField.*;
import com.stimulus.util.Compare;

public class EmailFields {
	
		protected static Logger logger = Logger.getLogger(EmailFields.class.getName());
		private static final long serialVersionUID = -4149161261465268322L;
		public String nameKey = "name";
		public String resourceKey = "resource";
		public String searchMethodKey = "searchmethod";
		public String indexKey = "index";
		public String allowExportKey = "allowexport";
		public String allowViewKey =  "allowview";
		public String allowArchiveRuleKey = "allowarchive";
		public String allowSearchKey = "allowsearch";
		public String columnSizeKey = "columnsize";
		public String showInResultsKey = "showinresults";
		public static LinkedHashMap<String,EmailField> emailFields;
		
		public LinkedHashMap<String,EmailField> getAvailableFields() {
			return emailFields;
		}
		public EmailField get(String name) { 
			
			if (name==null) {
				logger.debug("name is null");
				
			}
			if (emailFields==null)
				logger.debug("emailfields is null");
			return emailFields.get(name); 	
		} 	

	 public void setDefaults() {
		 
		 	emailFields = new LinkedHashMap<String,EmailField>();
		 	
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
			
			emailFields.put("receiveddate", new EmailField("receiveddate","field_label_receiveddate",SearchMethod.STORED,"receiveddate",
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
	 public boolean loadXMLFile() {
		  logger.debug("loading fields.conf");
		  emailFields = new LinkedHashMap<String,EmailField>();
		  String filename = Config.getFileSystem().getConfigurationPath() + File.separatorChar + "fields.conf";
	  	  String name = null;
	  	  String resource = null;
	  	  SearchMethod searchMethod = null;
	  	  String index = null;
	  	  AllowExport allowExport = null;
	  	  AllowViewMail allowView = null;
	  	  AllowArchiveRule allowArchiveRule = null;
	  	  AllowSearch allowSearch = null;
		  int columnSize = 0;
		  ShowInResults showInResults = null;
		  
		  DOMParser p = new DOMParser();
		  try {
			  p.parse(filename);
		  } catch (Exception e) {
			  setDefaults();
	       	  logger.debug("could not read from fields.conf. using defaults {fileName='"+filename+"'}");
	       	return true;
		  }
	      
	      Document doc = p.getDocument();
	      Element docEle = doc.getDocumentElement();
	      NodeList nl = docEle.getElementsByTagName("Fields");
		  if(nl != null && nl.getLength() > 0) {
				for(int i = 0 ; i < nl.getLength();i++) {
				  Element el = (Element)nl.item(i);
				  NamedNodeMap attrs = el.getAttributes();
			      int len = attrs.getLength();
		          for (int j=0; j<len; j++) {
		              Attr attr = (Attr)attrs.item(j);
	
		              if (Compare.equalsIgnoreCase(attr.getNodeName(),nameKey))
		            	  name = attr.getNodeValue();
		              if (Compare.equalsIgnoreCase(attr.getNodeName(),resourceKey))
		            	  resource = attr.getNodeValue();
		              if (Compare.equalsIgnoreCase(attr.getNodeName(),searchMethodKey))
		            	  searchMethod = SearchMethod.valueOf(attr.getNodeValue().toUpperCase(Locale.ENGLISH));
		              if (Compare.equalsIgnoreCase(attr.getNodeName(),indexKey))
		            	  index = attr.getNodeValue();
		              if (Compare.equalsIgnoreCase(attr.getNodeName(),allowExportKey))
		            	  allowExport = AllowExport.valueOf(attr.getNodeValue().toUpperCase(Locale.ENGLISH));
		              if (Compare.equalsIgnoreCase(attr.getNodeName(),allowViewKey))
		            	  allowView = AllowViewMail.valueOf(attr.getNodeValue().toUpperCase(Locale.ENGLISH));
		              if (Compare.equalsIgnoreCase(attr.getNodeName(),allowArchiveRuleKey))
		            	  allowArchiveRule = AllowArchiveRule.valueOf(attr.getNodeValue().toUpperCase(Locale.ENGLISH));
		              if (Compare.equalsIgnoreCase(attr.getNodeName(),allowSearchKey))
		            	  allowSearch = AllowSearch.valueOf(attr.getNodeName().toUpperCase(Locale.ENGLISH));
		              if (Compare.equalsIgnoreCase(attr.getNodeName(),columnSizeKey))
		            	  columnSize = Integer.valueOf(attr.getNodeName());
		              if (Compare.equalsIgnoreCase(attr.getNodeName(),showInResultsKey))
		            	  showInResults = ShowInResults.valueOf(attr.getNodeName().toUpperCase(Locale.ENGLISH));
		          }
		          if (name==null || resource==null || searchMethod==null || index==null || allowExport==null ||
		        	  allowView==null || allowArchiveRule==null || allowSearch==null || showInResults==null) {
		        	  logger.warn("failed to load email field data {name='"+name+"'}");
		        	  return false;
		       	  }
		          logger.debug("load email field data {name='"+name+"'}");
		       	  emailFields.put(name, new EmailField(name,resource,searchMethod,index,
		       				  		  allowExport,allowView,allowArchiveRule,allowSearch,columnSize,showInResults));
		       	  
				}
	      }
		  return true;
	 }
	
	 
}
