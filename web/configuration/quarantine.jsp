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
<title><bean:message key="quarantine.title"/></title>
<link href="common/mailarchiva.css" rel="stylesheet" type="text/css">
</head>

<body topmargin="0" leftmargin="0">

<html:form action="/recover.do" method="POST" styleId="configure">

<table class="pageheading" width="100%" border="0" cellpadding="0" cellspacing="0" >
  <tr> 
    <td width="50%" height="20"><strong><bean:message key="quarantine.title"/></strong></td>
    <td width="50%" align="left">&nbsp;</td>
    <td ></td>
  </tr>
</table>

<div class="pagedialog" id="recoveryOutput" style="width : 100%; height : 349px; overflow : auto; ">
<bean:message key="quarantine.ok"/>
</div>

<table class="pageend" width="100%" border="0" cellpadding="3" cellspacing="0">
  <tr> 
    <td >&nbsp;</td>
    <td align="left">
     <input type="submit" name="submit.close" onClick="window.close();" value="<bean:message key="common.button.close"/>">
    </td>
    <td></td>
  </tr>
</table> 

</html:form>

</body>
</html>
