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
<%@page isErrorPage="true"%>
<html>
<head>
<title><bean:message key="config.title"/></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="common/mailarchiva.css" rel="stylesheet" type="text/css">

<body>

<div class="pagedialog">

<table width="100%" border="0" cellpadding="0" cellspacing="0" >

  <tr> 
    <td width="5%" >&nbsp;</td>
    <td ><h2><bean:message key="errorpage.friendly.notice"/></h2></td>
    <td >&nbsp;</td>
  </tr>
 
  <tr> 
    <td width="5%" >&nbsp;</td>
    <td width="95%" align="left"><br><html:errors/></td>
    <td >&nbsp;</td>
  </tr>

 <tr> 
   <td colspan="3" >&nbsp;</td>
 </tr>
 
 <tr> 
  <td width="5%" >&nbsp;</td>
   <td ><h3><a href="javascript:history.back()">back</a><h3></td>
   <td >&nbsp;</td>
 </tr>


 <tr> 
   <td colspan="3" >&nbsp;</td>
 </tr>
 
 
</table>
</div>
</body>
</html>