<!-- MailArchiva Email Archiving Software 
	 Copyright Jamie Band 2005
-->
<%@ page language="java" import="java.io.*" %>
<%@ page isErrorPage="true" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>

<html>
<head>
    <title><bean:message key="errorpage.title"/></title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<link href="common/mailex.css" rel="stylesheet" type="text/css">
</head>
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
.b{font-size: 10pt; color:#00c; font-weight:bold}
.ch{cursor:pointer;cursor:hand}
.e{margin-top: .75em; margin-bottom: .75em}
.g{margin-top: 1em; margin-bottom: 1em}

body {
	background-image:  url(images/gradient.jpg);
	background-repeat:repeat-x;
	background-color: #C6CFD0;
}
-->

</style>
<body>
<h2><bean:message key="errorpage.header"/></h2>
<br><b><bean:message key="errorpage.message"/>
</b>
<hr>
</body>
</html>