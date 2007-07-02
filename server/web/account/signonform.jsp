<!-- MailArchiva Email Archiving Software 
	 Copyright Jamie Band 2005
-->
<%@ page language="java" %>
<%@ taglib prefix="c" uri="/WEB-INF/tld/c.tld" %>
<%@ taglib prefix="fmt" uri="/WEB-INF/tld/fmt.tld" %>
<%@ taglib prefix="fn" uri="/WEB-INF/tld/fn.tld" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<title>Archiva Login</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<style type="text/css">
<!--
.style1 {
	border-top-style: none;
	border-right-style: none;
	border-bottom-style: groove;
	border-left-style: none;
	background-color: #336699;
}
.boxborder {
	border: thin groove #000000;
}
body {
	background-image:  url(images/gradient.jpg);
	background-repeat:repeat-x;
	background-color: #C6CFD0;
}
-->
</style>
</head>

<body>

<table width="100%" border="0" cellpadding="0" cellspacing="0">
  <tr class="style2"> 
    <td height="163">&nbsp;</td>
  </tr>
</table>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr align="center"> 
    <td colspan="3"><h2>MailArchiva Login</h2></td>
  </tr>
  <tr align="center"> 
    
    <td height="75" colspan="4"><form id="LoginForm" action="j_security_check" method="POST">
      <table width="96" border="0" cellpadding="0" cellspacing="3" class="boxborder">
        <tr> 
          <td>&nbsp;</td>
          <td align="left">&nbsp;</td>
          <td>&nbsp;</td>
          <td align="left">&nbsp;</td>
          <td>&nbsp;</td>
        </tr>
        <tr> 
          <td width="1%">&nbsp;</td>
          <td width="19%" align="left"><font size="-1">Username</font></td>
          <td width="6%">&nbsp;</td>
          <td width="42%" align="left"><input type="text" name="j_username" ><br><font size="-1">(username@company.com)</font>  </td>
          <td width="14%">&nbsp;</td>
        </tr>
        <tr> 
          <td>&nbsp;</td>
          <td height="23" align="left"><font size="-1">Password</font></td>
          <td>&nbsp;</td>
          <td align="left"><input type="password" name="j_password" > </td>
          <td><input type="submit" name="Submit" value="Login"></td>
        </tr>
        <tr> 
          <td>&nbsp;</td>
          <td height="23" align="left">&nbsp;</td>
          <td>&nbsp;</td>
          <td align="right"><font size="-2">Optimized for FireFox browser</font></td>
          <td>&nbsp;</td>
        </tr>
      </table>
      <p>&nbsp;</p>
      <p>&nbsp;</p></form> 
  </tr>
  <tr> 
    <td width="5%"><font size="2">&nbsp;</font></td>
    <td width="90%" align="center"><font size="2">MailArchiva - Open Source Edition<br/>(for commercial use email <a href="mailto:info@mailarchiva.com">info@mailarchiva.com</a>)</font></td>
    <td width="5%"><div align="right"></div></td>
  </tr>
</table>
<p>&nbsp;</p>
</body>
</html>
