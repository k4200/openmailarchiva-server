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
<link href="common/mailarchiva.css" rel="stylesheet" type="text/css">

<script language="JavaScript"><!--

function updateParent(dropdown) {
	var myindex  = dropdown.selectedIndex
	var selvalue = dropdown.options[myindex].value
	var delim = selvalue.indexOf(":",0);
	if (delim>-1) {
		var index = <bean:write name="configBean" property="lookupIndex"/>;
		window.opener.document.getElementsByName("ADRoleMaps["+index+"].attribute")[0].value=selvalue.substring(0,delim-1);
		window.opener.document.getElementsByName("ADRoleMaps["+index+"].regEx")[0].value=selvalue.substring(delim+1);
	}
}
</script>
</head>

<body topmargin="0" leftmargin="0" onload="window.focus();">

<html:form action="/lookuprolecriterion" method="POST" autocomplete="false">
<table class="pageheading" width="100%" border="0" cellpadding="0" cellspacing="0" >
  <tr > 
    <td width="50%" ><strong><bean:message key="lookup_role.titel"/></strong></td>
    <td width="50%" align="left"></td>
  </tr>
</table>

      <table class="pagedialog" width="100%" border="0" cellpadding="0" cellspacing="3" >
        
        <tr> 
          <td width="5%">&nbsp;</td>
          <td align="right"><bean:message key="lookup_role.username"/></td>
          <td width="2%">&nbsp;</td>
          <td align="left"><html:text name="configBean" size="25" property="lookupUsername" /> (username@company.com) </td>
          <td width="5%" align="left">&nbsp;</td>
        </tr>
        <tr> 
          <td width="5%">&nbsp;</td>
          <td align="right" align="left"><bean:message key="lookup_role.password"/></td>
          <td width="2%">&nbsp;</td>
          <td align="left"><html:password name="configBean" size="27" redisplay="false" property="lookupPassword"/>&nbsp;<input type="submit" name="submit.lookup" value="<bean:message key="lookup_role.lookup"/>"></td>
          <td width="5%" align="left">&nbsp;</td>
        </tr>
      </table>
      <table class="pagetext" width="100%" border="0" cellpadding="0" cellspacing="0" >
       
        <tr> 
          <td width="100%" colspan="3">&nbsp;</td>
        </tr>
        <tr> 
          <td width="5%">&nbsp;</td>
          <td width="90%" align="left">
         <c:if test="${configBean.LDAPAttributeListSize>0}">
            <html:select name="configBean" property="lookupValue" onchange='javascript:updateParent(this);'>
			  				<html:options name="configBean" property="LDAPAttributes" labelName="configBean" labelProperty="LDAPAttributeLabels"/>
			</html:select>
	     </c:if>
	     <c:if test="${configBean.lookupError!=''}">
            <c:out value="${configBean.lookupError}" escapeXml="false"/>
         </c:if></td>
          <td width="5%">&nbsp;</td>
        </tr>
         <tr> 
          <td width="100%" colspan="3">&nbsp;</td>
        </tr>
      </table>
      <table class="pageend" width="100%" border="0" cellpadding="3" cellspacing="0">
	  <tr> 
	    <td >&nbsp;</td>
	    <td align="left"><input type="submit" name="submit.close" onClick="window.close()" value="<bean:message key="lookup_role.close"/>">
	    </td>
	    <td></td>
	  </tr>
	  </table>
</html:form>
<p>&nbsp;</p>
</body>
</html>
