<!-- MailArchiva Email Archiving Software 
	 Copyright Jamie Band 2005
-->

<%@ page language="java" %>
<%@ taglib prefix="c" uri="/WEB-INF/tld/c.tld" %>
<%@ taglib prefix="fmt" uri="/WEB-INF/tld/fmt.tld" %>
<%@ taglib prefix="fn" uri="/WEB-INF/tld/fn.tld" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<html>
<head>
<title>Archiva Search Results</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<link href="common/mailex.css" rel="stylesheet" type="text/css">
<script language="JavaScript" src="common/calendar1.js"></script>
<style><!--
body,td,div,.p,a{font-family:arial,sans-serif }
div,td{color:#000}
.f,.fl:link{color:#6f6f6f}
a:link,.w,a.w:link,.w a:link{color:#00c}
a:visited,.fl:visited{color:#551a8b}
a:active,.fl:active{color:#f00}
.t a:link,.t a:active,.t a:visited,.t{color:#000}
.t{background-color:#e5ecf9}
.k{background-color:#36c}
.j{width:34em}
.h{color:#36c}
.i,.i:link{color:#a90a08}
.a,.a:link{color:#008000}
.z{display:none}
div.n {margin-top: 1ex}
.n a{font-size:10pt; color:#000}
.n .i{font-size:10pt; font-weight:bold}
.q a:visited,.q a:link,.q a:active,.q {color: #00c; }
.b{font-size: 10pt; color:#00c; font-weight:bold}
.ch{cursor:pointer;cursor:hand}
.e{margin-top: .75em; margin-bottom: .75em}
.g{margin-top: 1em; margin-bottom: 1em}
//-->
</style>

</head>

<body onkeydown="if(event.keyCode == 13){document.getElementById('search').click();}">
<html:form action="/search" method="POST">
<%@include file="../common/menu.jsp"%>

<table class="sectionheader" width="100%" border="0" cellpadding="0" cellspacing="0" >
  <tr > 
    <td width="28%"><strong><font size=-1> Email Search</font></strong></td>
    <td align="left">&nbsp;</td>
    <td width="72%"> 
      <div align="right"> <c:if test="${searchBean.totalHits>0}">
          	<div align="right"><font size="-1">Results<strong>
             
             <c:out value="${searchBean.firstHitIndex+1}"/>
              - <c:out value="${searchBean.lastHitIndex}"/>
             </strong> of <strong>
				<c:out value="${searchBean.totalHits}"/>
			  </strong>(<strong><c:out value="${searchBean.searchTime}"/>
              </strong>seconds)
              </font></div>          
		</c:if></div>
      </td>
  </tr>
</table>

<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td><table class="section1"  width="100%" border="0" cellspacing="0" cellpadding="0">
        <tr> 
          <td width="10%">&nbsp;</td>
          <td colspan="2" align="center" valign="top"> <table width="100%" border="0" cellspacing="0" cellpadding="0">
              <tr valign="bottom"> 
                <td >&nbsp;</td>
                <td ><table width="100%" border="0" cellspacing="1" cellpadding="0">
                    <logic:iterate id="criteria" name="searchBean" property="criteria" indexId="criteriaIndex"> 
                    <tr > 
                      <td align="right" width="10%" nowrap><c:if test="${criteriaIndex>0}"> 
                          <html:select indexed="true" name="criteria" property="operator"> 
                          <html:options name="searchBean" property="operators" labelName="searchBean" labelProperty="operatorLabels" /> 
                          </html:select> </c:if> <c:if test="${criteriaIndex==0}"><font size=-1>Search 
                          Field:</font></c:if></td>
                      <td align="left"> <html:select indexed="true" name="criteria" property="field"> 
                        <html:options name="searchBean" property="fields" labelName="searchBean" labelProperty="fieldLabels" /> 
                        </html:select> </td>
                      <td align="left"><font size=-1>matches</font></td>
                      <td align="left"> <html:select indexed="true" name="criteria" property="method"> 
                        <html:options name="searchBean" property="methods" labelName="searchBean" labelProperty="methodLabels" /> 
                        </html:select> </td>
                      <td align="left">&nbsp;</td>
                      <td align="left"> <html:text indexed="true" name="criteria" property="query" size="45"/> 
                      </td>
                      <td align="left"><c:if test="${fn:length(searchBean.criteria)<6}"> 
                        <input type="submit" name="submit.newcriteria.${criteriaIndex}" value="+">
                        </c:if> </td>
                      <td align="left"><c:if test="${fn:length(searchBean.criteria)>1}"> 
                        <input type="submit" name="submit.deletecriteria.${criteriaIndex}" value="-">
                        </c:if> </td>
                    </tr>
                    </logic:iterate> </table></td>
                <td width="90%"></td>
              </tr>
              <tr valign="bottom"> 
                <td>&nbsp;</td>
                <td ><table width="100%" border="0" align="left" cellpadding="0" cellspacing="3">
                    <tr > 
                      <td  height="19"  nowrap><div align="right"><font size=-1>Sent 
                          After:</font></div></td>
                      <td   nowrap><html:text styleId="sentAfter" name="searchBean" property="sentAfter" size="20"/></td>
                      <td  nowrap><div align="left"><a href="javascript:cal1.popup();"><img src="images/cal.gif" width="16" height="16" border="0" alt="Click Here to Pick up the date"></a></div></td>
                      <td  nowrap><font size=-1> 
                        <div align="left"><font size=-1>Sent Before:</font></div>
                        </font></td>
                      <td  nowrap><html:text styleId="sentBefore" name="searchBean" property="sentBefore" size="20"/></td>
                      <td  nowrap><div align="left"><a href="javascript:cal2.popup();"><img src="images/cal.gif" width="16" height="16" border="0" alt="Click Here to Pick up the date"></a></div></td>
                      <td  nowrap>&nbsp;</td>
                      <td  nowrap><font size=-1>Results per Page:</font></td>
                      <td  nowrap>
	                      <html:select name="searchBean" property="pageSize">
			  				<html:options name="searchBean" property="pageSizes" labelName="searchBean" labelProperty="pageSizeLabels" />
					      </html:select>
				  	  </td>
                      <td  nowrap><input type="submit" name="search" id="search" value="Search"></td>
                    </tr>
                  </table></td>
                <td></td>
              </tr>
            </table></td>
          <td >&nbsp;</td>
        </tr>
      </table></td>
  </tr>
  <tr>
    <td><c:if test="${searchBean.totalHits>0}"> 
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr class="sectionheader2" > 
  	<td width="5%" class="columnspacing"><div align="center" ><a href="search.do?orderBy=score" class="columnheadertext"><font size="-1">Score<c:if test="${searchBean.orderBy=='score'}"> \</c:if></font></a></div></td>
    <td width="4%" class="columnspacing"><div align="center" ><a href="search.do?orderBy=size" class="columnheadertext"><font size="-1">Size<c:if test="${searchBean.orderBy=='size'}"> \/</c:if></font></a></div></td>
    <td width="12%" class="columnspacing"><div align="center" ><a href="search.do?orderBy=sentdate" class="columnheadertext"><font  size="-1">Sent Date<c:if test="${searchBean.orderBy=='sentdate'}"> \/</c:if></font></a></div></td>
    <td width="26%" class="columnspacing"><div align="center"><a href="search.do?orderBy=from" class="columnheadertext"><font size="-1">From<c:if test="${searchBean.orderBy=='from'}"> \/</c:if></font></a></div></td>
    <td width="26%" class="columnspacing"><div align="center"><a href="search.do?orderBy=to" class="columnheadertext"><font size="-1">To<c:if test="${searchBean.orderBy=='to'}"> \/</c:if></font></a></div></td>
    <td width="27%" class="columnspacing"><div align="center" class="columnheadertext"><font size="-1">Subject</font></div></td>
   
  </tr>
 </table>
 
<table class="section2" width="100%" border="0" cellspacing="1" cellpadding="0">
<logic:iterate id="results" offset="${searchBean.firstHitIndex}" length="${searchBean.pageSize}" name="searchBean" property="searchResults" indexId="resultsIndex">

  <tr> 
  	<td width="5%"><font size="-1"><c:out value="${results.score}"/></font></td>
  	<td width="4%"><font size="-1"><c:out value="${results.size}"/></font></td>
  	<td width="12%"><font size="-1"><c:out value="${results.sentDate}"/></font></td>
    <td width="26%"><font size="-1"><c:out value="${results.fromAddress}"/></font></td>
    <td width="26%"><font size="-1"><c:out value="${results.toAddresses}"/></font></td>
    <td width="25%"><font size="-1">
		<jsp:useBean id="detailParams" class="java.util.HashMap" type="java.util.HashMap" />
		<c:set target="${detailParams}" property="messageID" value="${results.emailId.uniqueID}" />
		<html:link  name="detailParams" scope="page"  page="/viewmail.do">
	    	 <c:out value="${results.subject}"/>
		</html:link>
	 </font></td>	 
    
  </tr>
  </logic:iterate>
</table>

<table class="sectionheader2" width="100%" border="0" cellspacing="1" cellpadding="0">
  <tr > 
    <td width="100%"><c:if test="${searchBean.noPages>1}">
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td width="33%">&nbsp;</td>
    <td width="58%"><font size="-1"><strong>  Pages: 
     <html:link paramId="page" paramName="searchBean" paramProperty="previousPage" page="/searchpage.do"> Previous</html:link>
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
    <html:link paramId="page" paramName="searchBean" paramProperty="nextPage" page="/searchpage.do"> Next...</html:link>
    </strong></font ></td>
    <td width="29%">&nbsp;</td>
  </tr>
</table>
</c:if><c:if test="${searchBean.noPages==1}"><font size="-1">&nbsp;</font></c:if></td>
  </tr>
</table>  
</c:if>

<c:if test="${searchBean.totalHits<1}">
<table class="section2" width="100%" border="0" cellspacing="1" cellpadding="0">
  <tr > 
    <td width="100%"><font size="-1" ><strong><br> No emails were found to match the search criteria <c:out value="${results.searchQuery}"/> <br>
<br>Suggestions:<br></strong>
    - Check your spelling.<br>
    - Try more general words.<br>
    - Try different words that mean the same thing.<br>
    - Broaden your search by using fewer words.<br><br>
</font></td>
  </tr>
</table>
</c:if>

</td>
  </tr>
</table>
<script language="JavaScript">
			
				var cal1 = new calendar1(document.forms[0].elements['sentAfter']);
				cal1.year_scroll = true;
				cal1.time_comp = true;
				var cal2 = new calendar1(document.forms[0].elements['sentBefore']);
				cal2.year_scroll = true;
				cal2.time_comp = true;
			
</script>

<SCRIPT>


</SCRIPT>
<%@include file="../common/bottom.jsp"%>
</html:form>
</body>
</html>
