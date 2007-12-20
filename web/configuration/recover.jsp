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
<title><bean:message key="recover.title"/></title>
<link href="common/mailarchiva.css" rel="stylesheet" type="text/css">
<script language="javascript" src="common/Ajax.js">
</script>
<script language="javascript" >

var complete = false;

function confirmQuarantine() 
{
		var answer = confirm ("<bean:message key="quarantine.confirm"/>");
		if (answer) {
			window.location = "quarantine.do"
		} else {
			window.close();
		}
		
}
   
function timerfunc() {
	      if (!complete) {
			  retrieveURL("recover.do",null);
			  textObj = document.getElementById('recoveryOutput');
			  textObj.scrollTop = textObj.scrollHeight;
		  }
}
</script>
</head>

<body onload="window.focus();timerfunc();setInterval('timerfunc()', 100);">

<html:form action="/recover.do" method="POST" styleId="configure">

<div class="section3" id="recoveryOutput" style="width : 580px; height : 349px; overflow : auto; ">
<span id="status">
<c:out value='${configBean.recoveryOutput}' escapeXml='false'/>
</span>
</div>

<table class="sectionheader" width="100%" border="0" cellpadding="3" cellspacing="0">
  <tr> 
    <td >&nbsp;</td>
    <td align="left">
     <input type="submit" name="submit.close" onClick="window.close();" value="<bean:message key="common.button.close"/>">
    </td>
    <td></td>
  </tr>
</table> 

</html:form>

<span id="quarantine">
<c:if test="${configBean.recoveryComplete==true && configBean.recoveryFailed>0}">
<script language="javascript" >
complete = true;
confirmQuarantine();
</script>
</c:if>
</span>
</body>
</html>
