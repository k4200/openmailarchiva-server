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
<title><bean:message key="viewlog.title"/></title>
<link href="common/mailarchiva.css" rel="stylesheet" type="text/css">
<script language="javascript" src="common/Ajax.js">
</script>
<script language="javascript" >

function scrolldown() 
{
		var textarea = document.getElementById("log");
		textarea.scrollTop = textarea.scrollHeight - textarea.clientHeight;
}


</script>

</head>

<body onload="window.focus();scrolldown();">
<table class="sectionheader" width="100%" border="0" cellpadding="0" cellspacing="0" >
  <tr> 
    <td width="28%" height="20"><strong><bean:message key="viewlog.title"/></strong></td>
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
    <td width="90%" align="left">
 	<span id="auditrefresh">
			<html:textarea styleId="log" name="configBean" property="auditLog" cols="90" rows="30" readonly="true"/>
	</span>   
	
    <td width="5%"></td>
  </tr>
  <tr>
    <td colspan="3">&nbsp;</td>
   </tr>
</table>
<table class="sectionheader" width="100%" border="0" cellpadding="3" cellspacing="0">
  <tr> 
    <td >&nbsp;</td>
    <td align="left">
    &nbsp;
    <input type="submit" name="close" onClick="window.close();" value="<bean:message key="common.button.close"/>">
    </td>
    <td></td>
  </tr>
</table>
</body>
</html>
