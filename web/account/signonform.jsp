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
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="common/mailarchiva.css" rel="stylesheet" type="text/css">
<title><bean:message key="signon.title"/></title>

<style type="text/css">
<!--

.boxborder {
	border: 1px groove #000000;
}
body {
	background-image:  url(images/gradient.jpg);
	background-repeat:repeat-x;
	background-color: #C6CFD0;
}
-->
</style>
</head>

<body onLoad="document.LoginForm.j_username.focus();">

<table width="100%" border="0" cellpadding="0" cellspacing="0">
  <tr> 
    <td height="163">&nbsp;</td>
  </tr>
</table>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  
  <tr align="center"> 
    
    <td colspan="3"><form name="LoginForm" id="LoginForm" action="j_security_check" method="POST">
      <table class="section1"  width="400" border="0" cellpadding="0" cellspacing="6">
        <tr> 
          <td align="center" colspan="3" nowrap></td>
        </tr>
        <tr align="center"> 
    		<td colspan="3"><h2><bean:message key="signon.title"/></h2></td>
  		</tr>
        <tr> 
          <td align="right" nowrap><bean:message key="signon.username"/></td>
          <td align="left" nowrap><input type="text" name="j_username" ><br><font size="-2">(e.g. username@company.local)</font></td>
          <td align="left">&nbsp;</td>
        </tr>
        <tr> 
          <td align="right" width="35%" nowrap><bean:message key="signon.password"/></td>
          <td align="left" width="40%" nowrap><input type="password" name="j_password" > </td>
          <td align="left" width="25%"><input type="submit" name="Submit" value="<bean:message key="signon.submit"/>"></td>
        </tr>
        <tr> 
          <td align="center" colspan="3" nowrap>&nbsp;</td>
        </tr>
       
      </table>
     </form> 
  </tr>
  <tr> 
    <td width="5%"><font size="2">&nbsp;</td>
    <td width="90%" align="center"><font size="2"><bean:message key="signon.software_title"/><br/></td>
    <td width="5%"><div align="right"></div></td>
  </tr>
</table>
<p>&nbsp;</p>
</body>
</html>
