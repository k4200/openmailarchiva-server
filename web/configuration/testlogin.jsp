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
<title><bean:message key="testlogin.title"/></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="common/mailarchiva.css" rel="stylesheet" type="text/css">
</head>

<body onload="window.focus();">
	<html:form action="/testlogin" method="POST">
	<table class="sectionheader" width="100%" border="0" cellpadding="0" cellspacing="0" >
	  <tr > 
	    <td width="28%" ><strong><bean:message key="testlogin.title"/></strong></td>
	    <td width="27%" align="left"></td>
	    <td width="45%"></td>
	  </tr>
	</table>

      <table class="section1" width="100%" border="0" cellpadding="0" cellspacing="3" >
        
        <tr> 
          <td width="5%">&nbsp;</td>
          <td align="right"><bean:message key="testlogin.username"/></td>
          <td width="2%">&nbsp;</td>
          <td align="left"><html:text name="configBean" size="25" property="lookupUsername" /> (e.g. username@company.com)  </td>
          <td width="5%" align="left" >&nbsp;</td>
        </tr>
        <tr> 
          <td width="5%">&nbsp;</td>
          <td align="right" align="left"><bean:message key="testlogin.password"/></td>
          <td width="2%">&nbsp;</td>
          <td align="left"><html:password name="configBean" size="27" redisplay="false" property="lookupPassword"/>&nbsp;<input type="submit" name="Submit" value="<bean:message key="testlogin.submit"/>"></td>
          <td width="5%" align="left">&nbsp;</td>
        </tr>
      </table>
      <table class="section2" width="100%" border="0" cellpadding="0" cellspacing="0" >
       
        <tr> 
          <td width="100%" colspan="3">&nbsp;</td>
        </tr>
        <tr> 
          <td width="5%">&nbsp;</td>
          <td width="90%" align="left"><c:out value="${configBean.testAuthenticate}"/></td>
          <td width="5%">&nbsp;</td>
        </tr>
         <tr> 
          <td width="100%" colspan="3">&nbsp;</td>
        </tr>
      </table>
      <table class="sectionheader" width="100%" border="0" cellpadding="3" cellspacing="0">
	  <tr> 
	    <td >&nbsp;</td>
	    <td align="left"><input type="submit" name="close" onClick="window.close();" value="<bean:message key="testlogin.close"/>">
	    </td>
	    <td></td>
	  </tr>
	  </table>
</html:form>
<p>&nbsp;</p>
</body>
</html>
