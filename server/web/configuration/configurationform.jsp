<!-- MailArchiva Email Archiving Software 
	 Copyright Jamie Band 2005
-->
<%@ page language="java" %>
<%@ taglib prefix="c" uri="/WEB-INF/tld/c.tld" %>
<%@ taglib prefix="fmt" uri="/WEB-INF/tld/fmt.tld" %>
<%@ taglib prefix="fn" uri="/WEB-INF/tld/fn.tld" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<html>
<head>
<title>Configuration</title>
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
<style type="text/css">

#tablist{
padding: 3px 0;
margin-left: 0;
margin-bottom: 0;
margin-top: 0.1em;
font: bold 12px Verdana;

}

#tablist li{
list-style: none;
display: inline;
margin: 0;
}

#tablist li a{
text-decoration: none;
padding: 3px 0.5em;
margin-left: 3px;
border: 1px solid #778;
border-bottom: none;
background: #B7BFD9;
}
#tablist li a:link, #tablist li a:visited{
color: black;
}

#tablist li a.current{
background: #CED4E9;
}

#tabcontentcontainer{
width:100%;
background: #CED4E9;
}

.tabcontent{
display:none;
background: #CED4E9;
}

</style>

<script type="text/javascript">

/***********************************************
* DD Tab Menu script- © Dynamic Drive DHTML code library (www.dynamicdrive.com)
* This notice MUST stay intact for legal use
* Visit Dynamic Drive at http://www.dynamicdrive.com/ for full source code
***********************************************/

//Set tab to intially be selected when page loads:
//[which tab (1=first tab), ID of tab content to display]:
var initialtab=[2, "archiverules"]

//Turn menu into single level image tabs (completely hides 2nd level)?
var turntosingle=0 //0 for no (default), 1 for yes

//Disable hyperlinks in 1st level tab images?
var disabletablinks=1 //1 for no (default), 1 for yes

////////Stop editting////////////////

var previoustab=""
var position = 1

if (turntosingle==1)
document.write('<style type="text/css">\n#tabcontentcontainer{display: none;}\n</style>')

function indexAllVolumes() 
{
	var answer = confirm ("All volume indexes will be deleted. Are you sure you would like to proceed?");
	if (answer)
	 window.open('indexstatus.do','index','width=400,height=200');
	return answer;
}

function indexVolume() 
{
	var answer = confirm ("The volume's index will be deleted. Are you sure you would like to proceed?");
	if (answer)
	 window.open('indexstatus.do','index','width=400,height=200');
	return answer;
}


function testPassword(passwd)
{
		var intScore   = 0
		var strVerdict = "weak"
		
		// PASSWORD LENGTH
		if (passwd.length<5)                         // length 4 or less
		 intScore = (intScore+3)
		else if (passwd.length>4 && passwd.length<8) // length between 5 and 7
		 intScore = (intScore+6)
		else if (passwd.length>7 && passwd.length<16)// length between 8 and 15
		 intScore = (intScore+12)
		else if (passwd.length>15)                    // length 16 or more
		intScore = (intScore+18)
		
		// LETTERS (Not exactly implemented as dictacted above because of my limited understanding of Regex)
		if (passwd.match(/[a-z]/))                              // [verified] at least one lower case letter
		  intScore = (intScore+1)
		if (passwd.match(/[A-Z]/))                              // [verified] at least one upper case letter
		  intScore = (intScore+5)
		
		// NUMBERS
		if (passwd.match(/\d+/))                                 // [verified] at least one number
		   intScore = (intScore+5)
		
		if (passwd.match(/(.*[0-9].*[0-9].*[0-9])/))             // [verified] at least three numbers
			intScore = (intScore+5)
		
		// SPECIAL CHAR
		if (passwd.match(/.[!,@,#,$,%,^,&,*,?,_,~]/))            // [verified] at least one special character
			intScore = (intScore+5)
															 // [verified] at least two special characters
		if (passwd.match(/(.*[!,@,#,$,%,^,&,*,?,_,~].*[!,@,#,$,%,^,&,*,?,_,~])/))
			intScore = (intScore+5)
		
		// COMBOS
		if (passwd.match(/([a-z].*[A-Z])|([A-Z].*[a-z])/))        // [verified] both upper and lower case
			intScore = (intScore+2)
		
		if (passwd.match(/(\d.*\D)|(\D.*\d)/))                    // [FAILED] both letters and numbers, almost works because an additional character is required
			intScore = (intScore+2)
															  // [verified] letters, numbers, and special characters
		if (passwd.match(/([a-zA-Z0-9].*[!,@,#,$,%,^,&,*,?,_,~])|([!,@,#,$,%,^,&,*,?,_,~].*[a-zA-Z0-9])/))
			intScore = (intScore+2)
		
		if(intScore < 16)
		{
		   strVerdict = "very weak"
		}
		else if (intScore > 15 && intScore < 25)
		{
		   strVerdict = "weak"
		}
		else if (intScore > 24 && intScore < 35)
		{
		   strVerdict = "mediocre"
		}
		else if (intScore > 34 && intScore < 45)
		{
		   strVerdict = "strong"
		}
		else
		{
		   strVerdict = "stronger"
		}
	document.getElementById("passwordtext").innerHTML = "(password strength is " + strVerdict + ")";
}

function expandcontent(cid, aobject,pos){
if (disabletablinks==1)
aobject.onclick=new Function("return false")
if (document.getElementById){
highlighttab(aobject)
if (turntosingle==0){
if (previoustab!="")
document.getElementById(previoustab).style.display="none"
document.getElementById(cid).style.display="block"
previoustab=cid
position=pos
}
}
}

function highlighttab(aobject){
if (typeof tabobjlinks=="undefined")
collecttablinks()
for (i=0; i<tabobjlinks.length; i++)
tabobjlinks[i].className=""
aobject.className="current"
}

function collecttablinks(){
var tabobj=document.getElementById("tablist")
tabobjlinks=tabobj.getElementsByTagName("A")
}
function get_cookie(Name) { 
var search = Name + "="
var returnvalue = "";
if (document.cookie.length > 0) {
offset = document.cookie.indexOf(search)
if (offset != -1) { 
offset += search.length
end = document.cookie.indexOf(";", offset);
if (end == -1) end = document.cookie.length;
returnvalue=unescape(document.cookie.substring(offset, end))
}
}
return returnvalue;
}

function do_onload(){
collecttablinks()
position = get_cookie("tab");
previoustab = get_cookie("content")
//alert("onload:tab"+previoustab)
//alert("onload:pos="+position)
if (previoustab=="")
	expandcontent(initialtab[1], tabobjlinks[initialtab[0]-1],initialtab[0])
else
    expandcontent(previoustab,tabobjlinks[position-1],position)
}
function savemenustate(){
document.cookie="content="+previoustab
document.cookie="tab="+position;
//alert("onsave:"+"tab="+previoustab)
//alert("onsave:"+"pos="+position);
}

if (window.addEventListener)
window.addEventListener("load", do_onload, false)
else if (window.attachEvent)
window.attachEvent("onload", do_onload)
else if (document.getElementById) {
window.onload=do_onload
}
window.onunload=savemenustate

</script>
</head>

<body>
<%@include file="../common/menu.jsp"%>
<table class="sectionheader" width="100%" border="0" cellpadding="0" cellspacing="0" >
  <tr > 
    <td width="28%" ><font size="4"><strong><font size=-1>Configuration</font></strong></font></td>
    <td width="27%" align="left"></td>
    <td width="45%"></td>
  </tr>
</table>



<table class="section1" width="100%" border="0" cellpadding="0" cellspacing="0" >
  <tr> 
    <td width="100%" >
<ul id="tablist">

<li><a href="" onMouseover="expandcontent('archiverules', this, 1)">Archive Rules</a></li>
<li><a href="" onMouseover="expandcontent('volumes', this, 2)">Volumes</a></li>
<li><a href="" onMouseover="expandcontent('domains', this, 3)">Domains</a></li>
<li><a href="" onMouseover="expandcontent('access', this, 4)">Security</a></li>
</ul>
</td>
  </tr>
</table>

<table class="section1" width="100%" border="0" cellpadding="0" cellspacing="0" >
  <tr > 
    <td width="100%"></td>
  </tr>
</table>
<html:form action="/configure" method="POST">
<logic:present name="errors">
<table class="errorheader" width="100%" border="0" cellpadding="0" cellspacing="0" >
  <tr> 
    <td width="5%" >&nbsp;</td>
    <td width="95%" align="left"><font size=-1><strong>Your configuration changes cannot be saved.</strong></font>
  		<logic:iterate id="error" name="errors">
  			<font size=-1><br>&nbsp;*&nbsp;<bean:write name="error"/></font>
  		</logic:iterate>
    </td>
    <td >&nbsp;</td>
  </tr>
</table>
</font>
</logic:present>

<DIV id="tabcontentcontainer">

<div id="volumes" class="tabcontent">
<table  width="100%" border="0" cellpadding="0" cellspacing="0">
  <tr> 
    <td colspan="3">&nbsp;</td>
  </tr>
    <tr> 
      <td width="10%" align="right" nowrap></td>
      <td width="80%">
          <input type="submit"  name="submit.newvolume" value="New Volume"> 
          <input type="submit" onClick="return indexAllVolumes()" name="submit.indexallvolumes" value="Re-Index All">

        <table width="100%" border="0" cellspacing="2" cellpadding="0">
          <logic:iterate id="volumes" name="configBean" property="volumes" indexId="volumeIndex">
              <tr> 
                <td width="100%%" colspan="4" nowrap><hr></td>
              </tr>
              <tr> 
                <td width="16%" nowrap ><font size=-1>Volume ${volumeIndex}:</font></td>
                <td width="20%" align="right" nowrap><font size=-1>Status:</font> </td>
                <td width="58%"><font size=-1><c:out value="${volumes.status}"/></font></td>
                <td width="6%">&nbsp;</td>
              </tr>
              <c:if test="${volumes.statusID<2}">
              <tr> 
                <td width="16%" nowrap ></td>
                <td width="20%" align="right" nowrap><font size=-1>Last Modified:</font> </td>
                <td width="58%"><font size=-1><c:out value="${volumes.modifiedStr}"/></font></td>
                <td width="6%">&nbsp;</td>
              </tr>
              <tr> 
                <td width="16%" nowrap ></td>
                <td width="20%" align="right" nowrap><font size=-1>Created:</font> </td>
                <td width="58%"><font size=-1><c:out value="${volumes.createdStr}"/></font></td>
                <td width="6%">&nbsp;</td>
              </tr>
              </c:if>
              <tr> 
                <td width="16%" nowrap ></td>
                <td width="20%" align="right" nowrap><font size=-1>Store Path:</font> </td>
                <td width="58%">
                <c:if test="${volumes.statusID>1}">
                	<html:text name="volumes" indexed="true" size="45" property="path" /> 
                </c:if>
                <c:if test="${volumes.statusID<2}">
                	<html:text name="volumes" indexed="true" readonly="true" size="45" property="path" /> 
                </c:if>
                </td>
                <td width="6%">&nbsp;</td>
              </tr>
              <tr> 
                <td width="16%" ></td>
                <td width="20%" align="right" nowrap><font size=-1>Index Path:</font> </td>
                <td width="58%">
                <c:if test="${volumes.statusID>1}">
                	<html:text name="volumes" indexed="true" size="45" property="indexPath" /> 
                </c:if>
                
                <c:if test="${volumes.statusID<2}">
                	<html:text name="volumes" indexed="true" readonly="true" size="45" property="indexPath" /> 
                </c:if>
                
                </td>
                <td width="6%">&nbsp;</td>
              </tr>
            
             
              <tr> 
                <td width="16%" nowrap ></td>
                <td width="20%" align="right" nowrap><font size=-1>Maximum Size (MB):</font> </td>
                <td width="58%"><font size=-1><html:text name="volumes" indexed="true" size="6" property="maxSize"/></font></td>
                <td width="6%">&nbsp;</td>
              </tr>
              
              <tr> 
                <td >&nbsp;</td>
                <td align="right"><font size=-1>Actions:</font></td>
                <td><c:if test="${volumes.statusID==0}">
                <input type="submit" onClick="return indexVolume()" name="submit.indexvolume.${volumeIndex}" value="Re-Index"> 
                </c:if>
                  <c:if test="${volumes.statusID!=1}">
                  	<input type="submit" name="submit.deletevolume.${volumeIndex}" value="Delete">
                  </c:if>
                  <c:if test="${volumes.statusID==1}">
                  	<input type="submit" name="submit.closevolume.${volumeIndex}" value="Close">
                  </c:if>
                  <c:if test="${volumes.statusID==2}">
                  <input type="submit" name="submit.prioritizevolume.${volumeIndex}" value="Up">
                  <input type="submit" name="submit.deprioritizevolume.${volumeIndex}" value="Down">
                   </c:if></td>
                <td>&nbsp;</td>
              </tr>
              
          </logic:iterate>
        </table></td>
        <td width="10%" align="right" nowrap></td>
      </tr>
      <tr> 
	    <td colspan="3">&nbsp;</td>
	  </tr>
    </table>
</div>


<div id="domains" class="tabcontent">
	  <table  width="100%" border="0" cellpadding="0" cellspacing="0">
  	  <tr> 
       <td colspan="3">&nbsp;</td>
      </tr>
	  <tr> 
          <td width="10%" align="right" nowrap></td>
         
          <td width="80%" valign="top">
            <font size=-1>New Windows Domain/s:</font>&nbsp;
            <input type="submit"  name="submit.newdomain" value="New Domain"> 
            <table width="100%" border="0" cellspacing="2" cellpadding="0">
              <logic:iterate id="domains" name="configBean" property="domains" indexId="domainIndex">
	              <tr> 
                	<td width="100%%" colspan="4" nowrap><hr></td>
              	  </tr>
	              <tr> 
	                <td width="16%" nowrap ><font size=-1>Domain ${domainIndex}:</font></td>
	                <td width="20%" align="right" nowrap><font size=-1>Domain Name:</font> </td>
	                <td width="58%"><html:text name="domains" indexed="true" size="45" property="name" /> </td>
	                <td width="6%">&nbsp;</td>
	              </tr>
	              <tr> 
	                <td >&nbsp;</td>
	                <td align="right"><font size=-1>Actions:</font></td>
	                <td>
	                  <input type="submit" name="submit.deletedomain.${domainIndex}" value="Delete">
	                <td>&nbsp;</td>
	              </tr>
              </logic:iterate>
            </table></td>
             <td width="10%">&nbsp;</td>
        </tr>
        <tr> 
	    	<td colspan="3">&nbsp;</td>
	  	</tr>
       </table>
</div>

<div id="access" class="tabcontent">
	  <table width="100%" border="0" cellpadding="0" cellspacing="0">
  	  <tr> 
       <td colspan="3">&nbsp;</td>
      </tr>
      <tr> 
          	<td width="10%" align="right" nowrap></td>
          	<td width="80%">
          		<table width="100%" border="0" cellspacing="2" cellpadding="0">
	   
	            	<td align="right" width="30%"><font size=-1>Email Encryption Password:</font> </td>
	            	<td valign="top">
	            	<c:if test="${configBean.defaultPassPhraseModified==false}">
	            		<html:password name="configBean" size="25" redisplay="false" property="passPhrase" onkeyup="testPassword(document.forms.configBean.passPhrase.value)"/>
	            		<font size=-1><div ID="passwordtext">(default password is set, you must change it!)</div></font>
	            	</c:if>
	            	<c:if test="${configBean.defaultPassPhraseModified==true}">
	            		<html:password name="configBean" size="25" redisplay="false" readonly="true" property="passPhrase"/>
	            		<font size=-1><div ID="passwordtext">(password is set)</div></font>
	            	</c:if>
	            	</td>
	            
	            </table>
            </td>
         	<td width="10%">&nbsp;</td>
      </tr>    
      <tr> 
  		<td nowrap>&nbsp;</td>
        <td nowrap><hr></td>
        <td nowrap>&nbsp;</td>
      </tr>
	  <tr> 
          <td width="10%" align="right" nowrap></td>
          
          <td width="80%"><table width="100%" border="0" cellspacing="2" cellpadding="0">
              <tr> 
                <td align="right" width="5%"><html:checkbox name="configBean"  property="consoleAuthenticate" value="yes"/></td>
                <td width="95%" align="left"><font size=-1>Authenticate users at console login (disable for testing only)</font></td>
              </tr>
            </table>
         </td>
         <td width="10%">&nbsp;</td>
      </tr>
      <tr> 
          <td  nowrap>&nbsp;</td>
          <td><table width="100%" border="0" cellspacing="2" cellpadding="0">
              <tr> 
	                <td >&nbsp;</td>
	                <td align="right" nowrap><font size=-1>Kerberos Server (KDC) Address:</font></td>
	                <td><html:text name="configBean" property="KDCAddress" size="60"/>&nbsp;<font size=-1>(FQDN:port)</font></td>
	                <td align="left"></td>
	          </tr>
	          <tr> 
	                <td >&nbsp;</td>
	                <td align="right" nowrap><font size=-1>LDAP Server Address:</font></td>
	                <td><html:text name="configBean" property="LDAPAddress" size="60"/>&nbsp;<font size=-1>(FQDN:port)</font></td>
	                <td align="left"></td>
	          </tr>
            </table>
         </td>
          <td>&nbsp;</td>
      <tr> 
      		<td nowrap>&nbsp;</td>
            <td nowrap><hr></td>
            <td nowrap>&nbsp;</td>
      </tr>
      </tr>
         <tr> 
          <td  align="right" valign="top"></td>
          <td valign="top">
          	<font size=-1>Assign Roles to User/s:</font>
            	<input type="submit" name="submit.newrolemap" value="New Role Assignment"> 
            	<input type="submit" onClick="window.open('testloginform.do','testauth','width=600,height=250')" name="submit.reload" value="Test Login">
            
            <table width="100%" border="0" cellspacing="2" cellpadding="0">
	            <logic:iterate id="roleMaps" name="configBean" property="roleMaps" indexId="userRoleAssignmentIndex">
	              <tr> 
                    <td width="100%%" colspan="4" nowrap><hr></td>
              	  </tr>
	              <tr> 
	                <td nowrap><font size=-1>Assignment ${userRoleAssignmentIndex}:</font></td>
	                <td align="right" nowrap><font size=-1>Role: </font></td>
	                <td>
	                <html:select indexed="true" name="roleMaps" property="role">
		  				<html:options name="configBean" property="roleMapRoles" labelName="configBean" labelProperty="roleMapRoleLabels" />
				  	</html:select>
	                </td>
	                <td width="6%">&nbsp;</td>
	              </tr>
	              <tr> 
	                <td >&nbsp;</td>
	                <td align="right" nowrap><font size=-1>Windows Domain:</font></td>
	                <td><html:select indexed="true" name="roleMaps" property="domain">
		  				<html:options name="configBean" property="domainLabels" labelName="configBean" labelProperty="domainLabels" />
				  	</html:select></td>
	                <td>&nbsp;</td>
	              </tr>
	              <tr> 
	                <td >&nbsp;</td>
	                <td  align="right" nowrap><font size=-1>LDAP Attribute:</font></td>
	                <td >
	                <html:select indexed="true" name="roleMaps" property="attribute">
		  				<html:options name="configBean" property="roleMapAttributes" labelName="configBean" labelProperty="roleMapAttributeLabels" />
				  	</html:select>
	                </td>
	                <td >&nbsp;</td>
	              </tr>
	              <tr> 
	                <td >&nbsp;</td>
	                <td align="right" nowrap><font size=-1>Match Criterion:</font></td>
	                <td><html:text indexed="true" name="roleMaps" property="regEx" size="45"/>
	               <input type="submit" onClick="window.open('lookuprolecriterionform.do','mywindow','width=600,height=250')" name="reload" value="Lookup"><font size=-1>&nbsp;(regular expression)</font></td>
	                <td align="left"></font></td>
	              </tr>
	           
	              <tr> 
	                <td >&nbsp;</td>
	                <td align="right"><font size=-1>Actions:</font></td>
	                <td>
	                  <input type="submit" name="submit.deleterolemap.${userRoleAssignmentIndex}" value="Delete">
	                </td>
	                <td>&nbsp;</td>
	              </tr></logic:iterate>
            </table>
          </td>
          <td>&nbsp;</td>
        </tr>
        <tr> 
	    	<td colspan="3">&nbsp;</td>
	  	</tr>
	 </table>
</div>

<div id="archiverules" class="tabcontent">
  <table width="100%" border="0" cellpadding="0" cellspacing="0">
	  <tr> 
	    <td colspan="3">&nbsp;</td>
	  </tr>
	  <tr> 
	    <td width="10%" align="right" >&nbsp;</td>
	    <td width="80%" >
	       <table width="100%" border="0" cellpadding="0" cellspacing="3">
	        <tr> 
	          <td  align="right" valign="top" nowrap></td>
	          <td>&nbsp;</td>
	          <td>
	            <table width="100%" border="0" cellspacing="2" cellpadding="0">
	              <tr> 
	                <td width="4%"><html:checkbox name="configBean"  property="archiveInbound" value="yes"/></td>
	                <td width="5%">&nbsp;</td>
	                <td width="91%"><font size=-1>Archive inbound emails</font></td>
	              </tr>
	              <tr> 
	                <td><html:checkbox name="configBean" property="archiveOutbound" value="yes"/></td>
	                <td>&nbsp;</td>
	                <td><font size=-1>Archive outbound emails</font></td>
	              </tr>
	              <tr> 
	                <td><html:checkbox name="configBean" property="archiveInternal" value="yes"/></td>
	                <td>&nbsp;</td>
	                <td><font size=-1>Archive internal emails</font></td>
	              </tr>
	            </table>
	          </td>
	        </tr>
	       
	        <tr> 
	          <td  align="right" valign="top"></td>
	          <td>&nbsp;</td>
	          <td valign="top"><font size=-1>Add Archive Rule/s:&nbsp;</font>
	          <input type="submit" name="submit.newarchiverule" value="New Archive Rule"> 
	            <table width="100%" border="0" cellspacing="2" cellpadding="0">
		            <logic:iterate id="archiveRules" name="configBean" property="archiveRules" indexId="archiveFilterIndex">
		              <tr> 
                		 <td width="100%%" colspan="4" nowrap><hr></td>
              		  </tr>
		              <tr> 
		                <td width="20%" nowrap><font size=-1>Archive Rule ${archiveFilterIndex}:</font></td>
		                <td width="20%" align="right" nowrap><font size=-1>Field: </font></td>
		                <td width="54%">
		                <html:select indexed="true" name="archiveRules" property="field">
			  				<html:options name="configBean" property="ruleFields" labelName="configBean" labelProperty="ruleFieldLabels" />
					  	</html:select>
		                </td>
		                <td width="6%">&nbsp;</td>
		              </tr>
		              <tr> 
		                <td >&nbsp;</td>
		                <td align="right" nowrap><font size=-1>Match Criterion:</font></td>
		                <td><html:text indexed="true" name="archiveRules" property="regEx" size="45"/>&nbsp;<font size=-1>(regular expression)</font></td>
		                <td></td>
		              </tr>
		              <tr> 
		                <td >&nbsp;</td>
		                <td align="right"><font size=-1>Behaviour:</font></td>
		                <td>
		                <html:select indexed="true" name="archiveRules" property="action">
			  				<html:options name="configBean" property="ruleActionFields" labelName="configBean" labelProperty="ruleActionLabels" />
					  	</html:select>
		                </td>
		                <td>&nbsp;</td>
		              </tr> 
		              <tr> 
		                <td >&nbsp;</td>
		                <td align="right"><font size=-1>Actions:</font></td>
		                <td>
		                  <input type="submit" name="submit.deletearchiverule.${archiveFilterIndex}" value="Delete">
		                  <input type="submit" name="submit.prioritizearchiverule.${archiveFilterIndex}" value="Up">
		                  <input type="submit" name="submit.deprioritizearchiverule.${archiveFilterIndex}" value="Down">
		                </td>
		                <td>&nbsp;</td>
		              </tr>
		              </logic:iterate>
	            </table>
	          </td>
	        </tr>
	     </table>
	   </td>
	   <td width="10%">&nbsp;</td>
	  </tr>
	  <tr> 
	    <td colspan="3">&nbsp;</td>
	  </tr>
   </table>
</div>
</DIV>


<table class="sectionheader" width="100%" border="0" cellpadding="3" cellspacing="0">
  <tr> 
    <td >&nbsp;</td>
    <td align="left"><input type="submit" name="submit.save" value="Save"><input type="submit" name="submit.cancel" value="Cancel"> 
    </td>
    <td></td>
  </tr>
</table>
<%@include file="../common/bottom.jsp"%>
</html:form>

</body>
</html>
