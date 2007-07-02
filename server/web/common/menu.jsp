<table class="logo" width="100%" border="0" cellpadding="0" cellspacing="0">
  <tr> 
    <td width="10%"><img src="images/title.gif" hspace="0" vspace="0" border="0"></td>
    <td>&nbsp;</td>
  </tr>
</table>
<table class="topheader" width="100%" border="0" cellpadding="0" cellspacing="0">
  <tr> 
    <td width="10%">&nbsp;</td>
    <td>
    	<table width="100%" border="0" cellpadding="0" cellspacing="0">
	        <tr align="right" > 
	          <td >&nbsp;</td>
	          <td width="6%"><font color="#FFFFFF" size=-1>
	          <% if (request.isUserInRole("administrator")) {%>
	          <html:link page="/configurationform.do"  styleClass="topmenuitems">Configuration</html:link>
	          <% } %></font></td>
	        
	          <td width="3%">&nbsp;</td>
	           <td width="6%" width="5%"><font color="#FFFFFF" size=-1><html:link page="/search.do" styleClass="topmenuitems">Search</html:link></font></td>
	          <td width="3%">&nbsp;</td>
	          <td width="5%"><font color="#FFFFFF" size=-1><html:link page="/signoff.do" styleClass="topmenuitems">Logout</html:link></font></td>
	          <td width="3%">&nbsp;</td>
	        </tr>
      </table></td>
  </tr>
</table>
