<!-- MailArchiva Email Archiving Software 
	 Copyright Jamie Band 2005
-->
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="/WEB-INF/tld/c.tld" %>
<%@ taglib prefix="fmt" uri="/WEB-INF/tld/fmt.tld" %>
<%@ taglib prefix="fn" uri="/WEB-INF/tld/fn.tld" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<html>
<head>
<title><bean:message key="indexing.title"/></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
 <META HTTP-EQUIV=REFRESH CONTENT=5>
<link href="common/mailarchiva.css" rel="stylesheet" type="text/css">
</head>

<body onload="window.focus();">
<table class="sectionheader" width="100%" border="0" cellpadding="0" cellspacing="0" >
  <tr> 
    <td width="28%" height="20"><strong><bean:message key="indexing.status"/></strong></td>
    <td width="27%" align="left">&nbsp;</td>
    <td width="45%"></td>
  </tr>
</table>
<table class="section2" width="100%" border="0" cellpadding="0" cellspacing="0" >
  <tr>
    <td colspan="3">&nbsp;</td>
   </tr>
  <tr> 
    <td width="5%" height="20">&nbsp;</td>
    <td width="90%" align="left"><c:if test="${!configBean.indexStatus.error}">
	<c:if test="${!configBean.indexStatus.complete}">
		<b><bean:message key="indexing.progress"/></b><br>
		<c:if test="${configBean.indexStatus.initialized}">	
			<br/>${configBean.indexStatus.completeFileCount} <bean:message key="indexing.actual_indexed"/> ${configBean.indexStatus.totalFileCount} <bean:message key="indexing.remain_indexed"/>
		</c:if>
		<c:if test="${!configBean.indexStatus.initialized}">
			<br/><bean:message key="indexing.prepare_vol"/>
		</c:if>
	</c:if>
	<c:if test="${configBean.indexStatus.complete}">
		<br/><bean:message key="indexing.indexing_comlete"/> ${configBean.indexStatus.completeFileCount} <bean:message key="indexing.actual_indexed"/>
	</c:if>
</c:if>
<c:if test="${configBean.indexStatus.error}">
	<b><bean:message key="indexing.indexing_error"/></b><br/><br/><bean:message key="indexing.indexing_error_cause"/> ${configBean.indexStatus.errorMessage}
	<br/><bean:message key="indexing.indexing_error_comment"/>
</c:if></td>
    <td width="5%"></td>
  </tr>
  <tr>
    <td colspan="3">&nbsp;</td>
   </tr>
</table>
<table class="sectionheader" width="100%" border="0" cellpadding="3" cellspacing="0">
  <tr> 
    <td >&nbsp;</td>
    <td align="left"><c:if test="${configBean.indexStatus.complete}"><input type="submit" name="close" onClick="window.close();" value="<bean:message key="common.button.close"/>"></c:if>
    </td>
    <td></td>
  </tr>
  </table>


</body>
</html>
