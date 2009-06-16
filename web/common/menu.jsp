
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>

<table class="logo" width="100%" border="0" cellpadding="0" cellspacing="0">
  <tr>
    <td width="10%" valign="middle"><html:link page="/search.do"><img align="left" src="<bean:message key="menu.image_left"/>" hspace="0" vspace="0" border="0"></html:link></td>
    <td width="80%" align="left"></td>
  	<td width="5%" align="center">
  	<!--
  		<a href="http://www.mailarchiva.com"><img align="left" src="<bean:message key="menu.image_right"/>" hspace="0" vspace="0" border="0"></a>
  		-->
  	</td>
  	<td width="5%" align="left"></td>
  </tr>
</table>
<table class="menu" width="100%" border="0" cellpadding="0" cellspacing="0">
  <tr> 
    <td width="10%">&nbsp;</td>
    <td>
    	<table width="100%" border="0" cellpadding="0" cellspacing="0">
	        <tr align="right" > 
	          <td >&nbsp;</td>
	          <td width="6%"><font color="#FFFFFF" size=-1>
	          <% if (request.isUserInRole("configure")) {%>
	          <html:link page="/configurationform.do"  styleClass="topmenuitems"><bean:message key="menu.config"/></html:link>
	          <% } %></font></td>
	        
	          <td width="3%">&nbsp;</td>
	           <td width="6%" width="5%"><font color="#FFFFFF" size=-1><html:link page="/search.do" styleClass="topmenuitems"><bean:message key="menu.search"/></html:link></font></td>
	          <td width="3%">&nbsp;</td>
	          <td width="5%"><font color="#FFFFFF" size=-1><html:link page="/signoff.do" styleClass="topmenuitems"><bean:message key="menu.logout"/></html:link></font></td>
	          <td width="3%">&nbsp;</td>
	        </tr>
      </table></td>
  </tr>
</table>
