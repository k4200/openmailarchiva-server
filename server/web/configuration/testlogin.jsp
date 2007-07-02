<!-- MailArchiva Email Archiving Software 
	 Copyright Jamie Band 2005
-->
<%@ page language="java" %>
<%@ taglib prefix="c" uri="/WEB-INF/tld/c.tld" %>
<%@ taglib prefix="fmt" uri="/WEB-INF/tld/fmt.tld" %>
<%@ taglib prefix="fn" uri="/WEB-INF/tld/fn.tld" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<html>
<head>
<title>Test Login</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<link href="common/mailex.css" rel="stylesheet" type="text/css">
<style><!--
body,td,div,.p,a{font-family:arial,sans-serif }
div,td{color:#000}
.f,.fl:link{color:#6f6f6f}
a:link,.w,a.w:link,.w a:link{color:#00c}
a:visited,.fl:visited{color:#551a8b}
a:active,.fl:active{color:#f00}
.t a:link,.t a:active,.t a:visited,.t{color:#000}
.t{background-color:#e5ecf9}
.k{background-color:#36c}
.j{width:34em}
.h{color:#36c}
.i,.i:link{color:#a90a08}
.a,.a:link{color:#008000}
.z{display:none}
div.n {margin-top: 1ex}
.n a{font-size:10pt; color:#000}
.n .i{font-size:10pt; font-weight:bold}
.q a:visited,.q a:link,.q a:active,.q {color: #00c; }
.b{font-size: 12pt; color:#00c; font-weight:bold}
.e{margin-top: .75em; margin-bottom: .75em}
.g{margin-top: 1em; margin-bottom: 1em}
//-->
</style>
</head>

<body onload="window.focus();">
<table class="sectionheader" width="100%" border="0" cellpadding="0" cellspacing="0" >
  <tr > 
    <td width="28%" ><strong><font size=-1>Test Login</font></strong></td>
    <td width="27%" align="left"></td>
    <td width="45%"></td>
  </tr>
</table>


<html:form action="/testlogin" method="POST">

      <table class="section1" width="100%" border="0" cellpadding="0" cellspacing="3" >
        
        <tr> 
          <td width="5%">&nbsp;</td>
          <td align="right"><font size="-1">Username</font></td>
          <td width="2%">&nbsp;</td>
          <td align="left"><html:text name="configBean" size="25" property="lookupUsername" /> <font size="-1">(e.g. username@company.com)</font>  </td>
          <td width="5%" align="left" >&nbsp;</td>
        </tr>
        <tr> 
          <td width="5%">&nbsp;</td>
          <td align="right" align="left"><font size="-1">Password</font></td>
          <td width="2%">&nbsp;</td>
          <td align="left"><html:password name="configBean" size="25" redisplay="false" property="lookupPassword"/><input type="submit" name="Submit" value="Lookup"></td>
          <td width="5%" align="left">&nbsp;</td>
        </tr>
      </table>
      <table class="section2" width="100%" border="0" cellpadding="0" cellspacing="0" >
       
        <tr> 
          <td width="100%" colspan="3">&nbsp;</td>
        </tr>
        <tr> 
          <td width="5%">&nbsp;</td>
          <td width="90%" align="left"><font size="-1"><c:out value="${configBean.testAuthenticate}"/></font></td>
          <td width="5%">&nbsp;</td>
        </tr>
         <tr> 
          <td width="100%" colspan="3">&nbsp;</td>
        </tr>
      </table>
      <table class="sectionheader" width="100%" border="0" cellpadding="3" cellspacing="0">
	  <tr> 
	    <td >&nbsp;</td>
	    <td align="left"><input type="submit" name="close" onClick="window.close();" value="Close">
	    </td>
	    <td></td>
	  </tr>
	  </table>
</html:form>
<p>&nbsp;</p>
</body>
</html>
