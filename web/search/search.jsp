<!-- MailArchiva Email Archiving Software 
	 Copyright Jamie Band 2005
-->
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="/WEB-INF/tld/c.tld" %>
<%@ taglib prefix="fmt" uri="/WEB-INF/tld/fmt.tld" %>
<%@ taglib prefix="fn" uri="/WEB-INF/tld/fn.tld" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/taglib186.tld" prefix="t" %>
<html>
<head>
<meta http-equiv="expires" content="-1">
<meta http-equiv="no-cache"> 
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><bean:message key="searchresults.title"/></title>
<link href="common/mailarchiva.css" rel="stylesheet" type="text/css">
<% int nocheckboxes = 0; %>
<script language="JavaScript" src="common/CalendarPopup.js"></script>
<script language="JavaScript" src="common/jquery-1.7.1.min.js"></script>

<script type="text/javascript">
	
	function switchPreview(obj) {
		var el = document.getElementById(obj);
		if ( el.style.display != 'none' ) {
			el.style.display = 'none';
		} else {
			el.style.display = '';
		}
	}
    function gotoPage(newpage) {
		var currentPage = document.getElementById("currentPage");
		currentPage.value=newpage;
		var searchForm = document.getElementById("searchForm");
		searchForm.submit();
		return false;
    }
  

  function outss(str,maxLen) {
	 adjust = 0;
	 for (i = 0; i < str.length; i++) {
	 	if (str[i]=="#" && i+4<str.length && str[i+4] == ";") {
	 		adjust += 5;
	 	}
	 }
     if (str.length-adjust>maxLen) {
         document.writeln(str.substring(0,maxLen-1+adjust)+"..");
     } else { 
         document.writeln(str);
     }
     
  }
  	
  	 function showhide() {
		if (document.getElementById){
			obj = document.getElementById("options");
			if (obj.style.display == "none"){
				obj.style.display = "block";
			} else {
				obj.style.display = "none";
			}
		}
	}
	function setcursoratend(obj) { 
		if (obj.createTextRange) { 
			var r = (obj.createTextRange()); 
			r.moveStart('character', (obj.value.length)); 
			r.collapse(); 
			r.select(); 
		}
	}
	
    function setfocus() 
    {
    	obj = document.getElementById("criteriaquery0");
    	obj.focus();
    	setcursoratend(obj);
    }

var NS = (document.layers) ? 1 : 0;
var IE = (document.all)    ? 1 : 0;
if (NS) document.captureEvents(Event.KEYPRESS); // uncomment if you wish to cancel the key
document.onkeypress = keyhandler;
function keyhandler(e) {
  if (NS) {
    Key = e.which;
  } else {
    Key = window.event.keyCode;
  }
  if (Key==13) {
    document.forms[0].search.click();
    return false; //swallow it if we processed it
  }
}

 </script>
 
<script language="JavaScript">
			
var cal1 = new CalendarPopup();
cal1.setMonthNames('<bean:message key="calendar.jan"/>',
				   '<bean:message key="calendar.feb"/>',
				   '<bean:message key="calendar.mar"/>',
				   '<bean:message key="calendar.apr"/>',
				   '<bean:message key="calendar.may"/>',
				   '<bean:message key="calendar.jun"/>',
				   '<bean:message key="calendar.jul"/>',
				   '<bean:message key="calendar.aug"/>',
				   '<bean:message key="calendar.sep"/>',
				   '<bean:message key="calendar.oct"/>',
				   '<bean:message key="calendar.nov"/>',
				   '<bean:message key="calendar.dec"/>');

cal1.setDayHeaders('<bean:message key="calendar.su"/>',
				   '<bean:message key="calendar.mo"/>',
				   '<bean:message key="calendar.tu"/>',
				   '<bean:message key="calendar.we"/>',
				   '<bean:message key="calendar.th"/>',
				   '<bean:message key="calendar.fr"/>',
				   '<bean:message key="calendar.sa"/>');
cal1.setWeekStartDay(1);
cal1.setTodayText("<bean:message key="calendar.today"/>");

var cal2 = new CalendarPopup();
cal2.setMonthNames('<bean:message key="calendar.jan"/>',
				   '<bean:message key="calendar.feb"/>',
				   '<bean:message key="calendar.mar"/>',
				   '<bean:message key="calendar.apr"/>',
				   '<bean:message key="calendar.may"/>',
				   '<bean:message key="calendar.jun"/>',
				   '<bean:message key="calendar.jul"/>',
				   '<bean:message key="calendar.aug"/>',
				   '<bean:message key="calendar.sep"/>',
				   '<bean:message key="calendar.oct"/>',
				   '<bean:message key="calendar.nov"/>',
				   '<bean:message key="calendar.dec"/>');

cal2.setDayHeaders('<bean:message key="calendar.su"/>',
				   '<bean:message key="calendar.mo"/>',
				   '<bean:message key="calendar.tu"/>',
				   '<bean:message key="calendar.we"/>',
				   '<bean:message key="calendar.th"/>',
				   '<bean:message key="calendar.fr"/>',
				   '<bean:message key="calendar.sa"/>');
cal2.setWeekStartDay(1);
cal2.setTodayText("<bean:message key="calendar.today"/>");
			
		
</script>



</head>

<body onload="setfocus();" onkeydown="if(event.keyCode == 13){document.getElementById('search').click();}">

<html:form  styleId="searchForm" action="/search" method="POST" autocomplete="false">
<%@include file="../common/menu.jsp"%>

<table class="pageheading" width="100%" border="0" cellpadding="0" cellspacing="0" >
  <tr > 
    <td width="28%"><strong><bean:message key="searchresults.email_search"/></strong></td>
    <td align="left">&nbsp;</td>
    <td width="72%"> 
      <div align="right"> <c:if test="${searchBean.totalHits>0}">
          	<div align="right"><bean:message key="searchresults.results"/><strong>
             
             <c:out value="${searchBean.firstHitIndex+1}"/>
              - <c:out value="${searchBean.lastHitIndex}"/>
             </strong> <bean:message key="searchresults.results_of"/><strong>
				<c:out value="${searchBean.totalHits}"/>
			  </strong>(<strong><c:out value="${searchBean.searchTime}"/>
              </strong><bean:message key="searchresults.seconds"/>)
              </div>          
		</c:if></div>
      </td>
  </tr>
</table>

<table class="pagedialog" width="100%" border="0" cellspacing="0" cellpadding="0">
        <tr>
    	 <td><table class="pagedialog"  width="100%" border="0" cellspacing="0" cellpadding="0">
       		 <tr> 
	          <td width="5%">&nbsp;</td>
	          <td colspan="2" align="center" valign="top"> 
	              <table width="100%" border="0" cellspacing="0" cellpadding="0">
	              <tr valign="bottom"> 
	                <td >&nbsp;</td>
	                <td ><table width="70%" border="0" align="left" cellpadding="0" cellspacing="3">
	                    <logic:iterate id="criteria" name="searchBean" property="criteria" indexId="criteriaIndex"> 
	                    <tr > 
	                   	  <td  nowrap align="right">&nbsp;</td>
	                      <td align="right" width="10%" nowrap><c:if test="${criteriaIndex>0}"> 
	                          <html:select indexed="true" name="criteria" property="operator"> 
	                          <html:options name="searchBean" property="operators" labelName="searchBean" labelProperty="operatorLabels" /> 
	                          </html:select> </c:if> <c:if test="${criteriaIndex==0}">
	                          <bean:message key="searchresults.field"/></c:if></td>
	                      <td align="left"> <html:select indexed="true" name="criteria" property="field"> 
	                        <html:options name="searchBean" property="fields" labelName="searchBean" labelProperty="fieldLabels" /> 
	                        </html:select> </td>
	                      <td align="left"><bean:message key="searchresults.matches"/></td>
	                      <td align="left"> 
	                      
	                      <html:select indexed="true" name="criteria" property="method"> 
	                        <html:options name="searchBean" property="methods" labelName="searchBean" labelProperty="methodLabels" /> 
	                        </html:select> </td>
	                      <td align="left">&nbsp;</td>
	                      <td align="left"> <html:text styleId="criteriaquery${criteriaIndex}" indexed="true" name="criteria" property="query" size="45"/> 
	                      </td>
	                      <td align="left"><c:if test="${fn:length(searchBean.criteria)<25}"> 
	                        <input type="submit" name="submit.newcriteria.${criteriaIndex}" value="&nbsp;+&nbsp;">
	                        </c:if> </td>
	                      <td align="left"><c:if test="${fn:length(searchBean.criteria)>1}"> 
	                        <input type="submit" name="submit.deletecriteria.${criteriaIndex}" value="&nbsp;-&nbsp;">
	                        </c:if> </td>
	                      <td  nowrap align="left" width="95%">&nbsp;</td>
	                    </tr>
	                </logic:iterate>
	                </table></td>
	                <td></td>
	              </tr>
	              <tr valign="bottom"> 
	                <td>&nbsp;</td>
	                <td ><table width="70%" border="0" align="left" cellpadding="0" cellspacing="3">
	                    <tr >
	                      <td  nowrap align="right">&nbsp;</td>
	                      <td  nowrap align="right" width="10%"><html:select name="searchBean" property="dateType">
				  				<html:options name="searchBean" property="dateTypes" labelName="searchBean" labelProperty="dateTypeLabels" />
						   </html:select>&nbsp;<bean:message key="searchresults.after"/></td>
	                      <td  nowrap align="left"><html:text styleId="after" name="searchBean" property="after" size="20"/></td>
	                      <td  nowrap align="left"><a href="javascript:cal1.select(document.forms[0].after,'anchor1','<c:out value="${searchBean.dateFormat}"/>');"><img src="images/cal.gif" width="16" height="16" border="0" alt="Click Here to Pick up the date" name="anchor1" id="anchor1"></a></td>
	                      <td  nowrap align="right"><bean:message key="searchresults.before"/></td>
	                      <td  nowrap align="left"><html:text styleId="before" name="searchBean" property="before" size="20"/></td>
	                      <td  nowrap align="left"><a href="javascript:cal2.select(document.forms[0].before,'anchor2','<c:out value="${searchBean.dateFormat}"/>');"><img src="images/cal.gif" width="16" height="16" border="0" alt="Click Here to Pick up the date" name="anchor2" id="anchor2"></a></td> 
	                      <td  nowrap>(<c:out value="${searchBean.localizedDateFormat}"/>)</td>
	                      <td  nowrap align="left"><input type="submit" name="submit.search" id="search" value="<bean:message key="searchresults.submit"/>"></td>
	    				  <td  nowrap align="left"><input type="submit" name="submit.showOptions" id="showOptions" value="<bean:message key="searchresults.options"/>" onclick="showhide(); return(false);"></td>
	    				  <td  nowrap align="left"><input type="submit" name="submit.reset" id="submit.reset" value="<bean:message key="searchresults.reset"/>"></td>
					  	  <td  nowrap align="left" width="95%">&nbsp;</td>
	    				  <td  nowrap align="left" width="95%">&nbsp;</td>
	    				  
	                    </tr>
	                  </table></td>
	                <td></td>
	              </tr>
	              
	            </table></td>
          <td >&nbsp;</td>
        </tr>
  </table></td></tr>
  <tr><td>
  			<div style="display: none;" id="options" >
  			<table class="pagedialog" width="100%" border="0" align="left" cellpadding="0" cellspacing="3"><tr><td width="5%">&nbsp;</td><td> 
					  	<table width="70%" border="0" align="left" cellpadding="0" cellspacing="3">
	                	<tr>
	                	  <td nowrap align="right">&nbsp;</td>
	                	  <td  nowrap align="right" width="10%"><bean:message key="searchresults.attachment"/>
	                	  	&nbsp;
		                      <html:select name="searchBean" property="attachment">
				  				<html:options name="searchBean" property="attachments" labelName="searchBean" labelProperty="attachmentLabels" />
						      </html:select>
					  	  </td>
					  	  <td  width="10%" nowrap align="right"><bean:message key="searchresults.priority"/>
	                      		&nbsp;<html:select name="searchBean" property="priority">
				  						<html:options name="searchBean" property="priorities" labelName="searchBean" labelProperty="priorityLabels" />
						      			</html:select>
					  	  </td>
					  	  <td  width="10%" nowrap align="right"><bean:message key="searchresults.flag"/>
	                      		&nbsp;<html:select name="searchBean" property="flag">
				  				<html:options name="searchBean" property="flags" labelName="searchBean" labelProperty="flagLabels" />
						      </html:select>
					  	  </td>
	                	  <td >&nbsp;</td>
	                	  <td width="10%" nowrap align="right"><bean:message key="searchresults.result_per_page"/>
	                      &nbsp;<html:select name="searchBean" property="pageSize">
				  				<html:options name="searchBean" property="pageSizes" labelName="searchBean" labelProperty="pageSizeLabels" />
						      </html:select>
					  	  </td>
					  	  <td  width="10%"  nowrap align="right"><bean:message key="searchresults.language"/>
	                      	&nbsp;<html:select name="searchBean" property="language">
				  				<html:options name="searchBean" property="languages" labelName="searchBean" labelProperty="languageLabels" />
						      </html:select>
					  	  </td>
					  	  
					  	  <td  width="60%" nowrap align="left">&nbsp;</td>
					  	 </tr>
					 	</table>
					 	</td></tr></table>
	        </div>
  
  
  
  </td></tr>

  <tr>
    <td><c:if test="${searchBean.totalHits>0}"> 
   
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr class="columnheadings" > 

	<logic:iterate id="field" name="searchBean" property="availableFields" indexId="fieldsIndex"> 
			<c:if test="${field.showConditional==true}">
				<c:choose>
					<c:when test='${field.name == "archivedate" && searchBean.dateType=="archivedate"}'>
		  						<td width="<c:out value='${field.columnSize}'/>%" class="columnspacing">
		  							<div align="center" valign="top"><a href="searchsort.do?orderBy=<c:out value='${field.name}' escapeXml='false'/>" class="columnheadertext">
		  						<bean:message key="${field.resource}"/>
		  						<c:if test="${searchBean.orderBy==field.name}">
			    				<c:if test="${searchBean.sortOrder=='descending'}">\</c:if>
			  					<c:if test="${searchBean.sortOrder=='ascending'}">/</c:if>
			    	 			</c:if></a></div></td>
		  			</c:when>	
		  			<c:when test='${field.name == "sentdate"  && searchBean.dateType=="sentdate"}'>
		  						<td width="<c:out value='${field.columnSize}'/>%" class="columnspacing">
		  							<div align="center" valign="top"><a href="searchsort.do?orderBy=<c:out value='${field.name}' escapeXml='false'/>" class="columnheadertext">
		  						<bean:message key="${field.resource}"/>
		  						<c:if test="${searchBean.orderBy==field.name}">
			    				<c:if test="${searchBean.sortOrder=='descending'}">\</c:if>
			  					<c:if test="${searchBean.sortOrder=='ascending'}">/</c:if>
			    	 			</c:if></a></div></td>
		  			</c:when>	
		  			<c:when test='${field.name == "receiveddate"  && searchBean.dateType=="receiveddate"}'>
		  						<td width="<c:out value='${field.columnSize}'/>%" class="columnspacing">
		  							<div align="center" valign="top"><a href="searchsort.do?orderBy=<c:out value='${field.name}' escapeXml='false'/>" class="columnheadertext">
		  						<bean:message key="${field.resource}"/>
		  						<c:if test="${searchBean.orderBy==field.name}">
			    				<c:if test="${searchBean.sortOrder=='descending'}">\</c:if>
			  					<c:if test="${searchBean.sortOrder=='ascending'}">/</c:if>
			    	 			</c:if></a></div></td>
		  			</c:when>	
	  			</c:choose>		
			</c:if>	
			<c:if test="${field.showResults==true}">
				<td width="<c:out value='${field.columnSize}'/>%" class="columnspacing">
				  <div align="center" valign="top"><a href="searchsort.do?orderBy=<c:out value='${field.name}'/>" class="columnheadertext">
					<c:choose>
				        <c:when test='${field.name == "priority"}'>
				         	<img style="border: none;" src="images/priority.gif" alt="Priority"/>
				        </c:when>
				        <c:when test='${field.name == "attach"}'>
					        <img style="border: none;" src="images/attach.gif" alt="Attach"/>
						</c:when>
				        <c:otherwise>
				        	<bean:message key="${field.resource}"/>
				        </c:otherwise>
			    	</c:choose>
	    		<c:if test="${searchBean.orderBy==field.name}">
	    			<c:if test="${searchBean.sortOrder=='descending'}">\</c:if>
	  				<c:if test="${searchBean.sortOrder=='ascending'}">/</c:if>
	    	 	</c:if></a></div></td>
	       </c:if>
	</logic:iterate>
	
  </tr>
 </table>
 
<table class="pagetext" width="100%" border="0" cellspacing="1" cellpadding="0">
  <logic:iterate id="searchResults" offset="${searchBean.firstHitIndex}" length="${searchBean.pageSize}" name="searchBean" property="searchResults" indexId="resultsIndex" type="com.stimulus.archiva.presentation.SearchResultBean">             
    <c:if test="${searchResults.display==true}">
      <tr>

        <logic:iterate id="value" name="searchResults" property="fieldValues" indexId="valueIndex"> 
          <c:if test="${value.field.showConditional==true}">
            <td>
              <c:choose>
                <c:when test='${value.field.name == "sentdate" && searchBean.dateType=="sentdate"}'>
                  <td width="<c:out value='${value.field.columnSize}'/>%" valign="top">
                    <script type="text/javascript">outss("<c:out value='${value.display}'/>",250)</script>
                  </td>
                </c:when>
                <c:when test='${value.field.name == "archivedate" && searchBean.dateType=="archivedate"}'>
                  <td width="<c:out value='${value.field.columnSize}'/>%" valign="top">
                    <script type="text/javascript">outss("<c:out value='${value.display}'/>",250)</script>
                  </td>
                </c:when>    
                <c:when test='${value.field.name == "receiveddate" && searchBean.dateType=="receiveddate"}'>
                  <td width="<c:out value='${value.field.columnSize}'/>%" valign="top">
                    <script type="text/javascript">outss("<c:out value='${value.display}'/>",250)</script>
                  </td>
                </c:when>
              </c:choose>
            </c:if>
            <c:if test="${value.field.showResults==true}">
              <td width="<c:out value='${value.field.columnSize}'/>%" valign="top">
              <c:choose>
                <c:when test='${value.field.name == "priority"}'>
                  <c:if test="${value.value<3}">
                    <img src="images/priority.gif" alt="Priority"/>
                  </c:if>
                  <c:if test="${value.value>=3}">
                    &nbsp;
                  </c:if>
                </c:when>
                <c:when test='${value.field.name == "attach"}'>
                  <c:if test="${value.value=='1'}">
                    <img src="images/attach.gif" alt="Attach"/>
                  </c:if>
                  <c:if test="${value.value=='0'}">
                    &nbsp;
                  </c:if>
                </c:when>
                <c:when test='${value.field.name == "subject"}'>
                  <jsp:useBean id="detailParams" class="java.util.HashMap" type="java.util.HashMap" />
                  <c:set target="${detailParams}" property="messageID" value="${searchResults.uniqueID}" />
                  <c:set target="${detailParams}" property="volumeID" value="${searchResults.volumeID}" />
                  <c:set target="${detailParams}" property="resultsIndex" value="${resultsIndex}" />
                  <c:if test="${searchResults.messageExist==true}">
                    <t:tooltip>
                      <t:text>
                        <html:link  name="detailParams" scope="page"  page="/viewmail.do">
                          <script type="text/javascript">outss("<c:out value='${value.display}' escapeXml='false'/>",50)</script></html:link>
                      </t:text>
                      <t:help style="position:absolute;visibility:hidden;background-color:#d3e3f6;padding:1px;border-style:solid;border-color:black;border-width:1px;margin-top:1px;" width="300"><c:out value='${value.display}' escapeXml='false'/></t:help>
                    </t:tooltip>
                  </c:if>
                  <c:if test="${searchResults.messageExist==false}">
                    <t:tooltip>
                      <t:text>
                        <script type="text/javascript">outss("<c:out value='${value.display}' escapeXml='false'/>",50)</script>
                      </t:text>
                      <t:help style="position:absolute;visibility:hidden;background-color:#d3e3f6;padding:1px;border-style:solid;border-color:black;border-width:1px;margin-top:1px;" width="300"><bean:message key="searchresults.volume_not_accessible"/></t:help>
                    </t:tooltip>
                  </c:if>
                </c:when>
                
                <c:when test='${value.field.name == "to"}'>
                  <t:tooltip>
                    <t:text><script type="text/javascript">outss("<c:out value='${value.display}' escapeXml='false'/>",200)</script></t:text>
                    <t:help style="position:absolute;visibility:hidden;background-color:#d3e3f6;padding:1px;border-style:solid;border-color:black;border-width:1px;margin-top:1px;" width="300"><script type="text/javascript">outss("<c:out value='${value.tip}' escapeXml='false'/>",1000)</script></t:help>
                  </t:tooltip>  
                </c:when>
                <c:otherwise>
                  <c:if test="${value.display==null}">
                     &nbsp;
                  </c:if>
                  <c:if test="${value.display!=null}">
                    <t:tooltip>
                      <t:text><script type="text/javascript">outss("<c:out value='${value.display}' escapeXml='false'/>",35)</script></t:text>
                      <t:help style="position:absolute;visibility:hidden;background-color:#d3e3f6;padding:1px;border-style:solid;border-color:black;border-width:1px;margin-top:1px;" width="300"><script type="text/javascript">outss("<c:out value='${value.tip}' escapeXml='false'/>",1000)</script></t:help>
                    </t:tooltip>  
                  </c:if>
                </c:otherwise>
              </c:choose>
            </td>
          </c:if>
        </logic:iterate>
      </tr>
    </c:if>
  </logic:iterate>
</table>
<html:hidden styleId="currentPage" name="searchBean" property="page"/>

<table class="columnfooter" width="100%" border="0" cellspacing="1" cellpadding="0">
  <tr > 
    <td width="100%"><c:if test="${searchBean.noPages>1}">
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td width="33%">&nbsp;</td>
    <td width="58%"><strong>  <bean:message key="searchresults.pages"/> 
     <html:link href="" onclick="return gotoPage('${searchBean.firstPage}');"> <bean:message key="searchresults.first"/></html:link>
     &nbsp;
     <html:link href="" onclick="return gotoPage('${searchBean.previousPage}');"> <bean:message key="searchresults.previous"/></html:link>
      [
    <c:forEach var="pages" begin="${searchBean.minViewPage}" end="${searchBean.maxViewPage}" step="1">
	    <c:if test="${pages==searchBean.page}">
	    	<c:out value="${pages}"/>
	    </c:if>
	    <c:if test="${pages!=searchBean.page}">
		    <html:link href="" onclick="return gotoPage('${pages}');">
		    <c:out value="${pages}"/>
		    </html:link>
		</c:if>
		<c:if test="${pages!=searchBean.noPages}">
		    	 - 
		</c:if>    	
    </c:forEach>
    ]
    <html:link href="" onclick="return gotoPage('${searchBean.nextPage}');"> <bean:message key="searchresults.next"/></html:link>
    &nbsp;
    <html:link href="" onclick="return gotoPage('${searchBean.lastPage}');"> <bean:message key="searchresults.last"/></html:link> 
    </strong></td>
    <td width="29%">&nbsp;</td>
  </tr>
</table>
</c:if><c:if test="${searchBean.noPages==1}">&nbsp;</c:if></td>
  </tr>
</table>  
</c:if>

<c:if test="${searchBean.totalHits<1 && searchBean.notSearched==false}">
<table class="pagetext" width="100%" border="0" cellspacing="1" cellpadding="0">
  <tr > 
    <td width="100%"><br><bean:message key="searchresults.no_emails"/> <c:out value="${searchResults.searchQuery}"/><br>
<br><bean:message key="searchresults.no_emails_comment"/>
</td>
  </tr>
</table>
</c:if>

<c:if test="${searchBean.notSearched==true}">
<table class="pagetext" width="100%" border="0" cellspacing="1" cellpadding="0">
  <tr > 
    <td width="100%"><br><bean:message key="searchresults.no_search"/><br><br></td>
  </tr>
</table>

</c:if>

</td>
  </tr>
</table>


<%@include file="../common/bottom.jsp"%>
</html:form>

</body>
</html>
