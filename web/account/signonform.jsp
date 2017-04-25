<%-- <!-- MailArchiva Email Archiving Software 
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
<link href="/common/mailarchiva.css" rel="stylesheet" type="text/css">
<title><bean:message key="signon.title"/></title>

</head>

<body class="signonbackground" onLoad="document.LoginForm.j_username.focus();">

<table width="100%" border="0" cellpadding="0" cellspacing="0">
  <tr> 
    <td height="163">&nbsp;</td>
  </tr>
</table>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  
  <tr align="center"> 
    
    <td colspan="3"><form name="LoginForm" id="LoginForm" action="j_security_check" autocomplete="false" method="POST">
      <table class="pagedialog"  width="400" border="0" cellpadding="0" cellspacing="2">
       <tr align="center"> 
    		<td colspan="3"><img align="center" src="<bean:message key="signon.image"/>" hspace="0" vspace="0" border="0"></td>
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
 --%>
 
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
<link href="/common/mailarchiva.css" rel="stylesheet" type="text/css">
<title><bean:message key="signon.title"/></title>

<!--추가시작   -->
 <meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="description" content="">
<meta name="author" content="">
<link rel="icon" href="./favicon.ico">
<link href="./css/bootstrap.min.css" rel="stylesheet">
<script src="./js/jquery.min.js"></script>
<script src="./js/bootstrap.js"></script>
<style>
        body {
          padding-top: 40px;
          padding-bottom: 40px;
          background-color: #eee;
        }

        .form-signin {
          max-width: 330px;
          padding: 15px;
          margin: 0 auto;
        }
        .form-signin .form-signin-heading,
        .form-signin .checkbox {
          margin-bottom: 10px;
        }
        .form-signin .checkbox {
          font-weight: normal;
        }
        .form-signin .form-control {
          position: relative;
          height: auto;
          -webkit-box-sizing: border-box;
             -moz-box-sizing: border-box;
                  box-sizing: border-box;
          padding: 10px;
          font-size: 16px;
        }
        .form-signin .form-control:focus {
          z-index: 2;
        }
        .form-signin input[type="email"] {
          margin-bottom: -1px;
          border-bottom-right-radius: 0;
          border-bottom-left-radius: 0;
        }
        .form-signin input[type="password"] {
          margin-bottom: 10px;
          border-top-left-radius: 0;
          border-top-right-radius: 0;
        }
    </style>
</head>

<%-- <body class="signonbackground" onLoad="document.LoginForm.j_username.focus();">

<table width="100%" border="0" cellpadding="0" cellspacing="0">
  <tr> 
    <td height="163">&nbsp;</td>
  </tr>
</table>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  
  <tr align="center"> 
    
    <td colspan="3"><form name="LoginForm" id="LoginForm" action="j_security_check" autocomplete="false" method="POST">
      <table class="pagedialog"  width="400" border="0" cellpadding="0" cellspacing="2">
       <tr align="center"> 
    		<td colspan="3"><img align="center" src="<bean:message key="signon.image"/>" hspace="0" vspace="0" border="0"></td>
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
</body> --%>
<body class="signonbackground" onLoad="document.LoginForm.j_username.focus();">
    <div class="container">
        <form class="form-signin" name="LoginForm" id="LoginForm" action="j_security_check" autocomplete="false" method="POST">
            <h2 class="form-signin-heading text-center"><img src="/images/logo.png" alt="mailarchiva"></h2>
            <label for="inputEmail" class="sr-only">Email address</label>
            <bean:message key="signon.username"/>
            <input type="text" name="j_username" class="form-control" placeholder="UserId" style="margin-bottom:10px;" autofocus>
            <label for="inputPassword" class="sr-only">Password</label>
            <bean:message key="signon.password"/>
            <input type="password" name="j_password"class="form-control" placeholder="Password">
            <button class="btn btn-lg btn-primary btn-block" type="submit"  name="Submit" ><span class="glyphicon glyphicon-log-in" style="margin-right:16px"></span>Login</button>
        </form>
    </div> <!-- /container -->
  </body>
</html>
 