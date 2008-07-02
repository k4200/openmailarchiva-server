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
<title><bean:message key="lookup_role.titel"/></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<META HTTP-EQUIV="Expires" CONTENT="-1">
<META HTTP-EQUIV="Cache-Control" CONTENT="no-cache">


<link href="common/mailarchiva.css" rel="stylesheet" type="text/css">
<script language="javascript" src="common/Ajax.js"></script>
<script language="javascript">

function uncache(url,form){
    var d = new Date();
    var time = d.getTime();
    url += '?time='+time;
    retrieveURL(url,form);
}

function reloadstatus() {
	uncache("testmailboxstatus.do",null);
}

</script>
</head>


<body topmargin="0" leftmargin="0" onload="window.focus();setInterval('reloadstatus()', 200);">

<table class="pageheading" width="100%" border="0" cellpadding="0" cellspacing="0" >
  <tr > 
    <td width="50%" ><strong><bean:message key="config.mailbox_connections_test_title"/></strong></td>
    <td width="50%" align="left"></td>
  </tr>
</table>

      <table class="pagetext" width="100%" border="0" cellpadding="0" cellspacing="0" >
       
        <tr> 
          <td width="100%" colspan="3">&nbsp;</td>
        </tr>
        <tr> 
          <td width="5%">&nbsp;</td>
          <td width="90%" align="left"><span id="status"><c:out value="${configBean.mailboxTestOutput}" escapeXml="false"/></span></td>
          <td width="5%">&nbsp;</td>
        </tr>
         <tr> 
          <td width="100%" colspan="3">&nbsp;</td>
        </tr>
      </table>
      <table class="pageend" width="100%" border="0" cellpadding="3" cellspacing="0">
	  <tr> 
	    <td >&nbsp;</td>
	    <td align="left"><input type="submit" name="submit.close" onClick="window.close()" value="<bean:message key="lookup_testconnection.close"/>">
	    </td>
	    <td></td>
	  </tr>
	  </table>
<p>&nbsp;</p>

</body>
</html>
