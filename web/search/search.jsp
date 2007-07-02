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
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><bean:message key="searchresults.title"/></title>
<link href="common/mailarchiva.css" rel="stylesheet" type="text/css">
<script language="JavaScript" src="common/CalendarPopup.js"></script>

<script type="text/javascript">
  function outss(str,maxLen) {
     if (str.length>maxLen) 
         document.writeln(str.substring(0,maxLen-1)+"..");
     else 
         document.writeln(str);
  }
  
  function showhide(id) {
	if (document.getElementById){
		obj = document.getElementById(id);
		if (obj.style.display == "none"){
			obj.style.display = "";
		} else {
			obj.style.display = "none";
		}
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

<body onkeydown="if(event.keyCode == 13){document.getElementById('search').click();}">


<html:form action="/search" method="POST">
<%@include file="../common/menu.jsp"%>

<table class="sectionheader" width="100%" border="0" cellpadding="0" cellspacing="0" >
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

<table width="100%" border="0" cellspacing="0" cellpadding="0">
        <tr>
    	 <td><table class="section1"  width="100%" border="0" cellspacing="0" cellpadding="0">
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
	                      <td align="left"> <html:select indexed="true" name="criteria" property="method"> 
	                        <html:options name="searchBean" property="methods" labelName="searchBean" labelProperty="methodLabels" /> 
	                        </html:select> </td>
	                      <td align="left">&nbsp;</td>
	                      <td align="left"> <html:text indexed="true" name="criteria" property="query" size="45"/> 
	                      </td>
	                      <td align="left"><c:if test="${fn:length(searchBean.criteria)<6}"> 
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
	                      <td  nowrap align="right" width="10%"><bean:message key="searchresults.sent_after"/></td>
	                      <td  nowrap align="left"><html:text styleId="sentAfter" name="searchBean" property="sentAfter" size="20"/></td>
	                      <td  nowrap align="left"><a href="javascript:cal1.select(document.forms[0].sentAfter,'anchor1','<c:out value="${searchBean.dateFormat}"/>');"><img src="images/cal.gif" width="16" height="16" border="0" alt="Click Here to Pick up the date" name="anchor1" id="anchor1"></a></td>
	                      <td  nowrap align="right"><bean:message key="searchresults.sent_before"/></td>
	                      <td  nowrap align="left"><html:text styleId="sentBefore" name="searchBean" property="sentBefore" size="20"/></td>
	                      <td  nowrap align="left"><a href="javascript:cal2.select(document.forms[0].sentBefore,'anchor2','<c:out value="${searchBean.dateFormat}"/>');"><img src="images/cal.gif" width="16" height="16" border="0" alt="Click Here to Pick up the date" name="anchor2" id="anchor2"></a></td> 
	                      <td  nowrap>(<c:out value="${searchBean.localizedDateFormat}"/>)</td>
	    				  <td  nowrap align="left" width="95%">&nbsp;</td>
	                    </tr>
	                  </table></td>
	                <td></td>
	              </tr>
	              <tr valign="bottom"> 
	                <td>&nbsp;</td>
	                <td ><table width="70%" border="0" align="left" cellpadding="0" cellspacing="3">
	                	<tr>
	                	  <td nowrap align="right">&nbsp;</td>
	                	  <td  nowrap align="right" width="10%"><bean:message key="searchresults.attachment"/></td>
	                	  <td  nowrap align="left">
		                      <html:select name="searchBean" property="attachment">
				  				<html:options name="searchBean" property="attachments" labelName="searchBean" labelProperty="attachmentLabels" />
						      </html:select>
					  	  </td>
					  	  
					  	  <td  nowrap align="right"><bean:message key="searchresults.priority"/></td>
	                      <td  nowrap align="left">
		                      <html:select name="searchBean" property="priority">
				  				<html:options name="searchBean" property="priorities" labelName="searchBean" labelProperty="priorityLabels" />
						      </html:select>
					  	  </td>
					  	  
					  	  <td  nowrap align="right"><bean:message key="searchresults.flag"/></td>
	                      <td  nowrap align="left">
		                      <html:select name="searchBean" property="flag">
				  				<html:options name="searchBean" property="flags" labelName="searchBean" labelProperty="flagLabels" />
						      </html:select>
					  	  </td>
					  	  
					  	  <td  nowrap align="left"><input type="submit" name="search" id="search" value="<bean:message key="searchresults.submit"/>"></td>
	    				  <td  nowrap align="left"><input type="submit" name="showOptions" id="showOptions" value="<bean:message key="searchresults.options"/>" onclick="showhide('options'); return(false);"></td>
	    				  <td  nowrap align="left"><input type="submit" name="submit.reset" id="submit.reset" value="<bean:message key="searchresults.reset"/>"></td>
					  	  <td  nowrap align="left" width="95%">&nbsp;</td>
					  	 </tr></table></td>
	                <td ></td>
	              </tr>
	              </td>  
	            </table></td>
          <td >&nbsp;</td>
        </tr>
  </table></td></tr>
  <tr><td>
  			<div style="display: none;" id="options">
  			<table class="section1" width="100%" border="0" align="left" cellpadding="0" cellspacing="3"><tr><td width="5%">&nbsp;</td><td> 
					  	<table width="70%" border="0" align="left" cellpadding="0" cellspacing="3">
	                	<tr>
	                	  <td >&nbsp;</td>
	                	  <td  nowrap align="right"><bean:message key="searchresults.result_per_page"/></td>
	                      <td  nowrap align="left">
		                      <html:select name="searchBean" property="pageSize">
				  				<html:options name="searchBean" property="pageSizes" labelName="searchBean" labelProperty="pageSizeLabels" />
						      </html:select>
					  	  </td>
					  	  <td  nowrap align="right"><bean:message key="searchresults.max_results"/></td>
	                      <td  nowrap align="left">
		                      <html:select name="searchBean" property="maxResult">
				  				<html:options name="searchBean" property="maxResults" labelName="searchBean" labelProperty="maxResultLabels" />
						      </html:select>
					  	  </td>
					  	  <td  nowrap align="right"><bean:message key="searchresults.language"/></td>
	                      <td  nowrap align="left">
		                      <html:select name="searchBean" property="language">
				  				<html:options name="searchBean" property="languages" labelName="searchBean" labelProperty="languageLabels" />
						      </html:select>
					  	  </td>
					  	  <td  nowrap align="right"><bean:message key="searchresults.type"/></td>
	                      <td  nowrap align="left">
		                      <html:select name="searchBean" property="searchType">
				  				<html:options name="searchBean" property="searchTypes" labelName="searchBean" labelProperty="searchTypeLabels" />
						      </html:select>
					  	  </td>
					  	  
					  	  <td width="25%" nowrap align="left">&nbsp;</td>
					  	 </tr>
					 	</table>
					 	</td></tr></table>
	        </div>
  
  
  
  </td></tr>
  <tr>
    <td><c:if test="${searchBean.totalHits>0}"> 
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr class="sectionheader2" > 
	<td width="1%" class="columnspacing"><div align="center" valign="top"><a href="search.do?orderBy=priority" class="columnheadertext">
		<img style="border: none;" src="images/priority.gif" alt="Priority"/>
		</a></div></td>
  	<td width="1%" class="columnspacing"><div align="center" valign="top"><a href="search.do?orderBy=attach" class="columnheadertext">
  		<img style="border: none;" src="images/attach.gif" alt="Attach"/>
  		</div></td>
  	<td width="5%" class="columnspacing"><div align="center" valign="top"><a href="search.do?orderBy=score" class="columnheadertext">
  		<bean:message key="searchresults.score"/>
  		<c:if test="${searchBean.orderBy=='score'}">
    		<c:if test="${searchBean.sortOrder==true}">\</c:if>
  			<c:if test="${searchBean.sortOrder==false}">\</c:if>
  		 </c:if></a></div></td>
    <td width="4%" class="columnspacing"><div align="center" valign="top"><a href="search.do?orderBy=size" class="columnheadertext">
    	<bean:message key="searchresults.size"/>
    	<c:if test="${searchBean.orderBy=='size'}">
    		<c:if test="${searchBean.sortOrder==true}">\</c:if>
  			<c:if test="${searchBean.sortOrder==false}">/</c:if>
    	</c:if></a></div></td>
    <td width="12%" class="columnspacing"><div align="center" valign="top"><a href="search.do?orderBy=sentdate" class="columnheadertext">
    	<bean:message key="searchresults.sent_date"/>
    	<c:if test="${searchBean.orderBy=='sentdate'}">
    		<c:if test="${searchBean.sortOrder==true}">\</c:if>
  			<c:if test="${searchBean.sortOrder==false}">/</c:if>
    	</c:if></a></div></td>
    <td width="20%" class="columnspacing"><div align="center" valign="top"><a href="search.do?orderBy=from" class="columnheadertext">
    	<bean:message key="searchresults.from"/>
    	<c:if test="${searchBean.orderBy=='from'}">
    		<c:if test="${searchBean.sortOrder==true}">\</c:if>
  			<c:if test="${searchBean.sortOrder==false}">/</c:if>
    	</c:if></a></div></td>
    <td width="30%" class="columnspacing"><div align="center" valign="top"><a href="search.do?orderBy=to" class="columnheadertext">
    	<bean:message key="searchresults.to"/>
    	<c:if test="${searchBean.orderBy=='to'}">
    		<c:if test="${searchBean.sortOrder==true}">\</c:if>
  			<c:if test="${searchBean.sortOrder==false}">/</c:if>
    	 </c:if></a></div></td>
    <td width="30%" class="columnspacing"><div align="center" valign="top"><a href="search.do?orderBy=subject" class="columnheadertext">
    	<bean:message key="searchresults.subject"/>
    	<c:if test="${searchBean.orderBy=='subject'}">
    		<c:if test="${searchBean.sortOrder==true}">\</c:if>
  			<c:if test="${searchBean.sortOrder==false}">/</c:if>
    	</c:if></a></div></td>
  </tr>
 </table>
 
<table class="section2" width="100%" border="0" cellspacing="1" cellpadding="0">
<logic:iterate id="results" offset="${searchBean.firstHitIndex}" length="${searchBean.pageSize}" name="searchBean" property="searchResults" indexId="resultsIndex">         	
  <tr> 
  	<td width="1%" valign="top">
	  	<c:if test="${results.priority<3}">
		  	<img src="images/priority.gif" alt="Priority"/>
		</c:if>
		<c:if test="${results.priority>=3}">
			&nbsp;&nbsp;
		</c:if>
	</td>
  	<td width="1%" valign="top">
	  	<c:if test="${results.hasAttachment==true}">
		  	<img src="images/attach.gif" alt="Attach"/>
		</c:if>
		<c:if test="${results.hasAttachment==false}">
			&nbsp;&nbsp;
		</c:if>
	</td>
  	<td width="5%" valign="top"><c:out value="${results.score}"/></td>
  	<td width="4%" valign="top"><c:out value="${results.size}"/></td>
  	<td width="12%" valign="top"><c:out value="${results.sentDate}"/></td>
    <td width="20%" valign="top">
    	<t:tooltip>
  			<t:text><script type="text/javascript">outss("<c:out value='${results.fromAddress}'/>",250)</script></t:text>
  			<t:help style="position:absolute;visibility:hidden;background-color:#d3e3f6;padding:1px;border-style:solid;border-color:black;border-width:1px;margin-top:1px;" width="300"><c:out value='${results.fromAddressT}'/></t:help>
		</t:tooltip>
    </td>
    <td width="30%" valign="top">
    	<t:tooltip>
    		<t:text><script type="text/javascript">outss("<c:out value='${results.toAddresses}'/>",250)</script></t:text>
    		<t:help style="position:absolute;visibility:hidden;background-color:#d3e3f6;padding:1px;border-style:solid;border-color:black;border-width:1px;margin-top:1px;" width="300"><c:out value='${results.toAddressesT}'/></t:help>
    	</t:tooltip>
    </td>
    <td width="30%" valign="top">
		<jsp:useBean id="detailParams" class="java.util.HashMap" type="java.util.HashMap" />
		<c:set target="${detailParams}" property="messageID" value="${results.uniqueID}" />
		<t:tooltip>
    		<t:text><html:link  name="detailParams" scope="page"  page="/viewmail.do">
	    	 		<script type="text/javascript">outss("<c:out value='${results.subject}'/>",250)</script></html:link></t:text>
	    	<t:help style="position:absolute;visibility:hidden;background-color:#d3e3f6;padding:1px;border-style:solid;border-color:black;border-width:1px;margin-top:1px;" width="300"><c:out value='${results.subject}'/></t:help>
	    </t:tooltip>
	 </td>	 
    
  </tr>
  </logic:iterate>
</table>

<table class="sectionheader2" width="100%" border="0" cellspacing="1" cellpadding="0">
  <tr > 
    <td width="100%"><c:if test="${searchBean.noPages>1}">
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td width="33%">&nbsp;</td>
    <td width="58%"><strong>  <bean:message key="searchresults.pages"/> 
     <html:link paramId="page" paramName="searchBean" paramProperty="previousPage" page="/searchpage.do"> <bean:message key="searchresults.previous"/></html:link>
      [
    <c:forEach var="pages" begin="${searchBean.minViewPage}" end="${searchBean.maxViewPage}" step="1">
	    <c:if test="${pages==searchBean.page}">
	    	<c:out value="${pages}"/>
	    </c:if>
	    <c:if test="${pages!=searchBean.page}">
		    <html:link paramId="page" paramName="pages" page="/searchpage.do">
		    <c:out value="${pages}"/>
		    </html:link>
		</c:if>
		<c:if test="${pages!=searchBean.noPages}">
		    	 - 
		</c:if>    	
    </c:forEach>
    ]
    <html:link paramId="page" paramName="searchBean" paramProperty="nextPage" page="/searchpage.do"> <bean:message key="searchresults.next"/></html:link>
    </strong></td>
    <td width="29%">&nbsp;</td>
  </tr>
</table>
</c:if><c:if test="${searchBean.noPages==1}">&nbsp;</c:if></td>
  </tr>
</table>  
</c:if>

<c:if test="${searchBean.totalHits<1}">
<table class="section2" width="100%" border="0" cellspacing="1" cellpadding="0">
  <tr > 
    <td width="100%"><br> <strong><bean:message key="searchresults.no_emails"/> <c:out value="${results.searchQuery}"/></strong> <br>
<br><bean:message key="searchresults.no_emails_comment"/>
</td>
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
