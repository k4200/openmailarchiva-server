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
<title>Index Status</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
 <META HTTP-EQUIV=REFRESH CONTENT=5>
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
.ch{cursor:pointer;cursor:hand}
.e{margin-top: .75em; margin-bottom: .75em}
.g{margin-top: 1em; margin-bottom: 1em}
//-->
</style>
</head>

<body onload="window.focus();">
<table class="sectionheader" width="100%" border="0" cellpadding="0" cellspacing="0" >
  <tr> 
    <td width="28%" height="20"><strong><font size=-1>Indexing Status</font></strong></td>
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
    <td width="90%" align="left"><c:if test="${!configBean.indexStatus.error}">
	<c:if test="${!configBean.indexStatus.complete}">
		<font size=-1><b>Please wait, indexing in progress..</b></font><br>
		<c:if test="${configBean.indexStatus.initialized}">	
			<font size=-1><br/>${configBean.indexStatus.completeFileCount} emails indexed. ${configBean.indexStatus.totalFileCount} emails remaining.</font>
		</c:if>
		<c:if test="${!configBean.indexStatus.initialized}">
			<font size=-1><br/>Preparing volumes for indexing...</font>
		</c:if>
	</c:if>
	<c:if test="${configBean.indexStatus.complete}">
		<font size=-1><br/>Indexing of messages complete. ${configBean.indexStatus.completeFileCount} emails indexed.</font>
	</c:if>
</c:if>
<c:if test="${configBean.indexStatus.error}">
	<font size=-1><b>An error occurred while re-indexing messages.</b><br/><br/>Cause: ${configBean.indexStatus.errorMessage}</font>
	<br/>Refer to the logs for more details.
</c:if></td>
    <td width="5%"></td>
  </tr>
  <tr>
    <td colspan="3">&nbsp;</td>
   </tr>
</table>
<table class="sectionheader" width="100%" border="0" cellpadding="3" cellspacing="0">
  <tr> 
    <td >&nbsp;</td>
    <td align="left"><c:if test="${configBean.indexStatus.complete}"><input type="submit" name="close" onClick="window.close();" value="Close"></c:if>
    </td>
    <td></td>
  </tr>
  </table>


</body>
</html>
