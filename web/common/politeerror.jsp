<!-- MailArchiva Email Archiving Software 
	 Copyright Jamie Band 2005
-->
<%@ page language="java" import="java.io.*" %>
<%@ page isErrorPage="true" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="/WEB-INF/tld/c.tld" %>
<%@ taglib prefix="fmt" uri="/WEB-INF/tld/fmt.tld" %>
<%@ taglib prefix="fn" uri="/WEB-INF/tld/fn.tld" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<html>
<head>
    <title><bean:message key="errorpage.title"/></title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<link href="common/mailarchiva.css" rel="stylesheet" type="text/css">
</head>
<body>
<%@include file="../common/menu.jsp"%>
<table class="section1" width="100%" border="0" cellpadding="0" cellspacing="0">

  <tr> 
    <td width="1%" >&nbsp;</td>
    <td width="99%" align="left" valign="bottom"><strong><bean:message key="errorpage.header"/></strong></td>
    <td >&nbsp;<br></td>
  </tr>
   
</table>

<logic:present name="errors">
<table class="section2" width="100%" border="0" cellpadding="0" cellspacing="0" >
 
  <tr> 
    <td width="5%" >&nbsp;</td>
    <td width="95%" align="left"><strong><bean:message key="config.save_failed"/></strong>
  		<logic:iterate id="error" name="errors">
  			<br>&nbsp;*&nbsp;<strong><bean:write name="error"/></strong>
  		</logic:iterate>
    </td>
    <td >&nbsp;</td>
  </tr>
  
</table>
</logic:present>

<logic:present name="message">
<table class="section2" width="100%" border="0" cellpadding="0" cellspacing="0" >
  <tr> 
    <td colspan="3">&nbsp;</td>
  </tr>
   <tr> 
    <td colspan="3">&nbsp;</td>
  </tr>
  <tr> 
    <td width="5%" >&nbsp;</td>
    <td width="95%" align="left">
    	<strong><c:out value="${message}"/></strong>
    </td>
    <td >&nbsp;</td>
  </tr>
   <tr> 
    <td colspan="3">&nbsp;</td>
  </tr>
</table>
</logic:present>

<table class="section1" width="100%" border="0" cellpadding="0" cellspacing="0">
  
  <tr> 
    <td width="5%" >&nbsp;</td>
    <td width="95%" align="left">&nbsp;</td>
    <td >&nbsp;<br></td>

</table>

</body>
</html>