<!-- MailArchiva Email Archiving Software 
	 Copyright Jamie Band 2005
-->
<%
response.setHeader("Pragma", "no-cache");
response.setHeader("Cache-Control", "must-revalidate");
response.setHeader("Cache-Control", "no-cache");
response.setHeader("Cache-Control", "no-store");
response.setDateHeader("Expires", 0);        
%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="/WEB-INF/tld/c.tld" %>
<%@ taglib prefix="fmt" uri="/WEB-INF/tld/fmt.tld" %>
<%@ taglib prefix="fn" uri="/WEB-INF/tld/fn.tld" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="common/mailarchiva.css" rel="stylesheet" type="text/css">
<script type="text/javascript">

//ID of Daily Iframe tag:
var iframeids=["ifrm"]

var getFFVersion=navigator.userAgent.substring(navigator.userAgent.indexOf("Firefox")).split("/")[1]
var FFextraHeight=50 //extra height in px to add to iframe in FireFox 1.0+ browsers


function getWindowWidth() {
  var myWidth = 0;
  if( typeof( window.innerWidth ) == 'number' ) {
    //Non-IE
    myWidth = window.innerWidth;
  } else if( document.documentElement &&
      ( document.documentElement.clientWidth  ) ) {
    //IE 6+ in 'standards compliant mode'
    myWidth = document.documentElement.clientWidth;
  } else if( document.body && ( document.body.clientWidth ) ) {
    //IE 4 compatible
    myWidth = document.body.clientWidth;
  }
  return myWidth - 20;
}


function startdyncode(){
	dyniframesize()
}

function dyniframesize() {
	var dyniframe=new Array()
	for (i=0; i<iframeids.length; i++){
		if (document.getElementById){ //begin resizing iframe procedure
			dyniframe[dyniframe.length] = document.getElementById(iframeids[i]);
			if (dyniframe[i] && !window.opera) {
				dyniframe[i].style.display="block"
				if (dyniframe[i].contentDocument && dyniframe[i].contentDocument.body.offsetHeight) //ns6 syntax
				dyniframe[i].height = dyniframe[i].contentDocument.body.offsetHeight+FFextraHeight; 
				else if (dyniframe[i].Document && dyniframe[i].Document.body.scrollHeight) //ie5+ syntax
				dyniframe[i].height = dyniframe[i].Document.body.scrollHeight;
				dyniframe[i].width = getWindowWidth();
				}
		}
	}
}

if (window.addEventListener)
	window.addEventListener("load", startdyncode, false)
else if (window.attachEvent)
	window.attachEvent("onload", startdyncode)



</script>

<script type="text/javascript">

/***********************************************
* Switch Content script- © Dynamic Drive (www.dynamicdrive.com)
* This notice must stay intact for legal use. Last updated April 2nd, 2005.
* Visit http://www.dynamicdrive.com/ for full source code
***********************************************/

var enablepersist="on" //Enable saving state of content structure using session cookies? (on/off)
var collapseprevious="no" //Collapse previously open content when opening present? (yes/no)

var contractsymbol='- ' //HTML for contract symbol. For image, use: <img src="whatever.gif">
var expandsymbol='+ ' //HTML for expand symbol.


if (document.getElementById){
document.write('<style type="text/css">')
document.write('.switchcontent{display:none;}')
document.write('</style>')
}

function getElementbyClass(rootobj, classname){
var temparray=new Array()
var inc=0
for (i=0; i<rootobj.length; i++){
if (rootobj[i].className==classname)
temparray[inc++]=rootobj[i]
}
return temparray
}

function sweeptoggle(ec){
var thestate=(ec=="expand")? "block" : "none"
var inc=0
while (ccollect[inc]){
ccollect[inc].style.display=thestate
inc++
}
revivestatus()
}


function contractcontent(omit){
var inc=0
while (ccollect[inc]){
if (ccollect[inc].id!=omit)
ccollect[inc].style.display="none"
inc++
}
}

function expandcontent(curobj, cid){
var spantags=curobj.getElementsByTagName("SPAN")
var showstateobj=getElementbyClass(spantags, "showstate")
if (ccollect.length>0){
if (collapseprevious=="yes")
contractcontent(cid)
document.getElementById(cid).style.display=(document.getElementById(cid).style.display!="block")? "block" : "none"
if (showstateobj.length>0){ //if "showstate" span exists in header
if (collapseprevious=="no")
showstateobj[0].innerHTML=(document.getElementById(cid).style.display=="block")? contractsymbol : expandsymbol
else
revivestatus()
}
}
}

function revivecontent(){
contractcontent("omitnothing")
selectedItem=getselectedItem()
selectedComponents=selectedItem.split("|")
for (i=0; i<selectedComponents.length-1; i++)
document.getElementById(selectedComponents[i]).style.display="block"
}

function revivestatus(){
var inc=0
while (statecollect[inc]){
if (ccollect[inc].style.display=="block")
statecollect[inc].innerHTML=contractsymbol
else
statecollect[inc].innerHTML=expandsymbol
inc++
}
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

function getselectedItem(){
if (get_cookie(window.location.pathname) != ""){
selectedItem=get_cookie(window.location.pathname)
return selectedItem
}
else
return ""
}

function saveswitchstate(){
var inc=0, selectedItem=""
while (ccollect[inc]){
if (ccollect[inc].style.display=="block")
selectedItem+=ccollect[inc].id+"|"
inc++
}

document.cookie=window.location.pathname+"="+selectedItem
}

function do_onload(){
uniqueidn=window.location.pathname+"firsttimeload"
var alltags=document.all? document.all : document.getElementsByTagName("*")
ccollect=getElementbyClass(alltags, "switchcontent")
statecollect=getElementbyClass(alltags, "showstate")
if (enablepersist=="on" && ccollect.length>0){
document.cookie=(get_cookie(uniqueidn)=="")? uniqueidn+"=1" : uniqueidn+"=0" 
firsttimeload=(get_cookie(uniqueidn)==1)? 1 : 0 //check if this is 1st page load
if (!firsttimeload)
revivecontent()
}
if (ccollect.length>0 && statecollect.length>0)
revivestatus()
}

if (window.addEventListener)
window.addEventListener("load", do_onload, false)
else if (window.attachEvent)
window.attachEvent("onload", do_onload)
else if (document.getElementById)
window.onload=do_onload

if (enablepersist=="on" && document.getElementById)
window.onunload=saveswitchstate

</script>

<title><bean:message key="viewmail.email"/> (<c:out value="${messageBean.message.subject}"/>)</title>
<link href="common/mailex.css" rel="stylesheet" type="text/css">
</head>

<body >
<%@include file="../common/menu.jsp"%>
<table class="sectionheader" width="100%" border="0" cellpadding="0" cellspacing="0">
  <tr > 
    <td height="20" width="100%"><strong><strong><bean:message key="viewmail.email"/> (<c:out value="${messageBean.message.subject}"/>)</strong></td>
  </tr>
</table>

<table class="section1" width="100%" border="0" cellspacing="3" bgcolor="#EFEFEF" cellpadding="0">
  <c:if test="${messageBean.originalMessageFileName != null}">
  <tr> 
    <td>&nbsp;</td>
    <td valign="top"><div align="right"><strong><bean:message key="viewmail.archived_message"/></strong></div></td>
    <td>&nbsp;</td>
    <td><a class="emailaddress" href="downloadmessage.do"><c:out value='${messageBean.originalMessageFileName}'/></a>&nbsp;(<c:out value='${messageBean.originalMessageFileSize}'/>)</td>
  </tr>
  </c:if>
  <tr> 
    <td width="1%">&nbsp;</td>
    <td width="15%"><div align="right"><strong><bean:message key="viewmail.subject"/></strong></div></td>
    <td width="2%">&nbsp;</td>
    <td width="85%"><strong><c:out value="${messageBean.message.subject}"/></strong></td>
  </tr>
  <tr> 
    <td height="20">&nbsp;</td>
    <td valign="top"><div align="right"><strong><bean:message key="viewmail.sentdate"/></strong></div></td>
    <td>&nbsp;</td>
    <td><c:out value="${messageBean.message.sentDate}"/><font></td>
  </tr>
  <c:if test="${fn:length(messageBean.message.receivedDate)>0}">
  <tr> 
    <td height="20">&nbsp;</td>
    <td valign="top"><div align="right"><strong><bean:message key="viewmail.receiveddate"/></strong></div></td>
    <td>&nbsp;</td>
    <td><c:out value="${messageBean.message.receivedDate}"/><font></td>
  </tr>
  </c:if>
  <tr> 
    <td>&nbsp;</td>
    <td valign="top"><div align="right"><strong><bean:message key="viewmail.from"/></strong></div></td>
    <td>&nbsp;</td>
    <td><a class="emailaddress" href="mailto:<c:out value='${messageBean.message.fromAddress}'/>"><c:out value='${messageBean.message.fromAddress}'/></a></td>
  </tr>
  <tr> 
    <td>&nbsp;</td>
    <td valign="top"><div align="right"><strong><bean:message key="viewmail.to"/></strong></div></td>
    <td>&nbsp;</td>
    <td><a class="emailaddress" href="mailto:<c:out value='${messageBean.message.toAddresses}'/>"><c:out value='${messageBean.message.toAddresses}'/></a></td>
  </tr>
  <c:if test="${fn:length(messageBean.message.CCAddresses)>0}">
  <tr> 
    <td>&nbsp;</td>
    <td valign="top"><div align="right"><strong><bean:message key="viewmail.cc"/></strong></div></td>
    <td>&nbsp;</td>
    <td><a class="emailaddress" href="mailto:<c:out value='${messageBean.message.CCAddresses}'/>"><c:out value='${messageBean.message.CCAddresses}'/></a></td>
  </tr>
  </c:if>
  <c:if test="${fn:length(messageBean.message.BCCAddresses)>0}">
  <tr> 
    <td>&nbsp;</td>
    <td valign="top"><div align="right"><strong><bean:message key="viewmail.bcc"/></strong></div></td>
    <td>&nbsp;</td>
    <td><a class="emailaddress" href="mailto:<c:out value='${messageBean.message.BCCAddresses}'/>"><c:out value='${messageBean.message.BCCAddresses}'/></a></td>
  </tr>
  </c:if>
  <c:if test="${fn:length(messageBean.message.flags)>0}">
   <tr> 
    <td>&nbsp;</td>
    <td valign="top"><div align="right"><strong><bean:message key="viewmail.flags"/></strong></div></td>
    <td>&nbsp;</td>
    <td><c:out value="${messageBean.message.friendlyFlags}"/></td>
  </tr>
  </c:if>
  <c:if test="${messageBean.message.priorityID!=3}">
   <tr> 
    <td>&nbsp;</td>
    <td valign="top"><div align="right"><strong><bean:message key="viewmail.priority"/></strong></div></td>
    <td>&nbsp;</td>
    <td><font size="-1" color="red"><strong><c:out value="${messageBean.message.priority}"/></strong></td>
  </tr>
  </c:if>
  
  <tr> 
    <td>&nbsp;</td>
    <td valign="top" align="right"><strong><bean:message key="viewmail.attachements"/></strong></td>
    <td>&nbsp;</td>
    <td><c:forEach var="attachments" items="${messageBean.attachments}">
    	
    	<c:if test="${attachments.isEmail==false}">
         	<a class="emailaddress" href="downloadattachment.do?attachment=<c:out value='${attachments.name}'/>"><c:out value='${attachments.name}'/></a>&nbsp;(<c:out value='${attachments.fileSize}'/>)
    	</c:if>
    	<c:if test="${attachments.isEmail==true}">
    		<a class="emailaddress" href="viewattachment.do?attachment=<c:out value='${attachments.name}'/>"><c:out value='${attachments.name}'/></a>&nbsp;(<c:out value='${attachments.fileSize}'/>)
    	</c:if>
    	&nbsp;
    </c:forEach>
    </td>
  </tr>
  <tr> 
    <td>&nbsp;</td>
    <td valign="top"><div align="right" onClick="expandcontent(this, 'sc1')"><span class="showstate"></span><strong><bean:message key="viewmail.internet_headers"/></strong></div></td>
    <td>&nbsp;</td>
    <td><div id="sc1" class="switchcontent"><c:out value='${messageBean.message.internetHeaders}' escapeXml='false'/></div></td>
  </tr>
</table>
 

<iframe width="%100" scrolling="no" id=ifrm src="<c:out value='${messageBean.view}' escapeXml='false'/>"></iframe>

 
<table class="sectionheader" width="100%" border="0" cellspacing="1" cellpadding="0">
  <tr> 
    <td width="100%">&nbsp;</td>
  </tr>
</table>
<%@include file="../common/bottom.jsp"%>
</body>
</html>
