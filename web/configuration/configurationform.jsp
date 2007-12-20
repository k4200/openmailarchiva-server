<!-- MailArchiva Email Archiving Software 
	 Copyright Jamie Band 2005
-->

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="/WEB-INF/tld/c.tld" %>
<%@ taglib prefix="fmt" uri="/WEB-INF/tld/fmt.tld" %>
<%@ taglib prefix="fn" uri="/WEB-INF/tld/fn.tld" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<html>
<head>
<title><bean:message key="config.title"/></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="common/mailarchiva.css" rel="stylesheet" type="text/css">

<script type="text/javascript">

/***********************************************
* DD Tab Menu script- � Dynamic Drive DHTML code library (www.dynamicdrive.com)
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
	var answer = confirm ("<bean:message key="config.confirm_delete_all_index"/>");
	if (answer)
	 window.open('indexstatus.do','index','width=400,height=200');
	return answer;
}

function indexVolume() 
{
	var answer = confirm ("<bean:message key="config.confirm_delete_index"/>");
	if (answer)
	  window.open('indexstatus.do','index','width=400,height=200');
	return answer;
}

function viewDebugLog() {
	window.open('viewdebuglog.do','log','width=800,height=600');
}

function viewAuditLog() {
	window.open('viewauditlog.do','log','width=800,height=600');
}
function testPassword(passwd)
{
		var intScore   = 0
		var strVerdict = "<bean:message key="config.password_strength_weak"/>"
		
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
		   strVerdict = "<bean:message key="config.password_strength_very_weak"/>"
		}
		else if (intScore > 15 && intScore < 25)
		{
		   strVerdict = "<bean:message key="config.password_strength_weak"/>"
		}
		else if (intScore > 24 && intScore < 35)
		{
		   strVerdict = "<bean:message key="config.password_strength_mediocre"/>"
		}
		else if (intScore > 34 && intScore < 45)
		{
		   strVerdict = "<bean:message key="config.password_strength_strong"/>"
		}
		else
		{
		   strVerdict = "<bean:message key="config.password_strength_stronger"/>"
		}
	document.getElementById("passwordtext").innerHTML = "(<bean:message key="config.password_strength"/> " + strVerdict + ")";
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


function show(id) {
	obj = document.getElementById(id);
	obj.style.display = "";
}

function hide(id) {
	obj = document.getElementById(id);
	obj.style.display = "none";
}

function OnChange(dropdown)
{
	var myindex  = dropdown.selectedIndex
	var SelValue = dropdown.options[myindex].value
	
	if (SelValue=="activedirectory") {
		show('activedirectory');
		hide('basic');
	} else {
		show('basic');
		hide('activedirectory');
	}
	return true;
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


function recover() 
{
	window.open('recover.do','send','width=600,height=400');
	return true;
}
	
</script>
</head>

<body onLoad="OnChange(document.getElementById('authMethod'));">
<%@include file="../common/menu.jsp"%>
<table class="sectionheader" width="100%" border="0" cellpadding="0" cellspacing="0" >
  <tr > 
    <td width="28%" ><strong><bean:message key="config.header"/></strong></td>
    <td width="27%" align="left"></td>
    <td width="45%"></td>
  </tr>
</table>



<table class="section1" width="100%" border="0" cellpadding="0" cellspacing="0" >
  <tr> 
    <td width="100%" >
<ul id="tablist">

<li><a href="" onMouseover="expandcontent('access', this, 1)"><bean:message key="config.security"/></a></li>
<li><a href="" onMouseover="expandcontent('domains', this, 2)"><bean:message key="config.domains"/></a></li>
<li><a href="" onMouseover="expandcontent('volumes', this, 3)"><bean:message key="config.volumes"/></a></li>
<li><a href="" onMouseover="expandcontent('archiverules', this, 4)"><bean:message key="config.archive_rules"/></a></li>
<li><a href="" onMouseover="expandcontent('agents', this, 5)"><bean:message key="config.agent"/></a></li>
<li><a href="" onMouseover="expandcontent('logging', this, 6)"><bean:message key="config.log"/></a></li>
<li><a href="" onMouseover="expandcontent('about', this, 7)"><bean:message key="config.about"/></a></li>
</ul>
</td>
  </tr>
</table>
<DIV id="tabcontentcontainer">
<html:form action="/configure" method="POST" styleId="configure">
<logic:present name="errors">
<table class="errorheader" width="100%" border="0" cellpadding="0" cellspacing="0" >
  <tr> 
    <td width="5%" >&nbsp;</td>
    <td width="95%" align="left"><strong><bean:message key="config.save_failed"/></strong>
  		<logic:iterate id="error" name="errors">
  			<br>&nbsp;*&nbsp;<bean:write name="error"/>
  		</logic:iterate>
    </td>
    <td >&nbsp;</td>
  </tr>
</table>

</logic:present>

<logic:present name="message">
<table class="messageheader" width="100%" border="0" cellpadding="0" cellspacing="0" >
  <tr> 
    <td width="5%" >&nbsp;</td>
    <td width="95%" align="left">
    	<c:out value="${message}"/>
    </td>
    <td >&nbsp;</td>
  </tr>
</table>
</logic:present>

<div id="volumes" class="tabcontent">
<table  width="100%" border="0" cellpadding="0" cellspacing="0">
  <tr> 
    <td colspan="3">&nbsp;</td>
  </tr>
    <tr> 
      <td width="10%" align="right" nowrap></td>
      <td width="80%">
          <input type="submit"  name="submit.newvolume" value="<bean:message key="config.new_volume"/>"> 
          
        <table width="100%" border="0" cellspacing="2" cellpadding="0">
          <logic:iterate id="volumes" name="configBean" property="volumes" indexId="volumeIndex">
              <tr> 
                <td width="100%%" colspan="4" nowrap><hr></td>
              </tr>
              <tr> 
                <td width="16%" nowrap ><bean:message key="config.volume"/> ${volumeIndex}:</td>
                <td width="20%" align="right" nowrap><bean:message key="config.volume_status"/>: </td>
                <td width="58%"><c:out value="${volumes.status}"/></td>
                <td width="6%">&nbsp;</td>
              </tr>
              <c:if test="${volumes.statusID<2}">
              <tr> 
                <td width="16%" nowrap ></td>
                <td width="20%" align="right" nowrap><bean:message key="config.volume_last_mod"/>: </td>
                <td width="58%"><c:out value="${volumes.modified}"/></td>
                <td width="6%">&nbsp;</td>
              </tr>
              <tr> 
                <td width="16%" nowrap ></td>
                <td width="20%" align="right" nowrap><bean:message key="config.volume_created"/>: </td>
                <td width="58%"><c:out value="${volumes.created}"/></td>
                <td width="6%">&nbsp;</td>
              </tr>
              <tr> 
                <td width="16%" nowrap ></td>
                <td width="20%" align="right" nowrap><bean:message key="config.volume_doc_count"/>: </td>
                <td width="58%"><c:out value="${volumes.totalMessageCount}"/></td>
                <td width="6%">&nbsp;</td>
              </tr>
              </c:if>
              <tr> 
                <td width="16%" nowrap ></td>
                <td width="20%" align="right" nowrap><bean:message key="config.volume_store_path"/>: </td>
                <td width="58%">
                <c:if test="${volumes.statusID>1}">
                	<html:text name="volumes" indexed="true" size="45" property="path" /> 
                </c:if>
                <c:if test="${volumes.statusID<2}">
                	<html:text name="volumes" indexed="true" readonly="true" size="45" property="path" /> 
                </c:if>
				<c:if test="${volumes.freeArchiveSpace!=''}">
                <c:out value="${volumes.freeArchiveSpace}"/>&nbsp;<bean:message key="config.volume_free_space"/>&nbsp;&nbsp;
                </c:if>
                <c:if test="${volumes.usedArchiveSpace!=''}">
                <c:out value="${volumes.usedArchiveSpace}"/>&nbsp;<bean:message key="config.volume_used_space"/>&nbsp;&nbsp;
                </c:if>
                </td>
                <td width="6%">&nbsp;</td>
              </tr>
             
              <tr> 
                <td width="16%" ></td>
                <td width="20%" align="right" nowrap><bean:message key="config.volume_index_path"/>: </td>
                <td width="58%">
                <c:if test="${volumes.statusID>1}">
                	<html:text name="volumes" indexed="true" size="45" property="indexPath" /> 
                </c:if>
                
                <c:if test="${volumes.statusID<2}">
                	<html:text name="volumes" indexed="true" readonly="true" size="45" property="indexPath" /> 
                </c:if>
                <c:if test="${volumes.freeIndexSpace!=''}">
                <c:out value="${volumes.freeIndexSpace}"/>&nbsp;<bean:message key="config.volume_free_space"/>&nbsp;&nbsp;
                </c:if>
                <c:if test="${volumes.usedIndexSpace!=''}">
                <c:out value="${volumes.usedIndexSpace}"/>&nbsp;<bean:message key="config.volume_used_space"/>&nbsp;&nbsp;
                </c:if>
                </td>
                <td width="6%">&nbsp;</td>
              </tr>
            
             
              <tr> 
                <td width="16%" nowrap ></td>
                <td width="20%" align="right" nowrap><bean:message key="config.volume_max_size"/>: </td>
                <td width="58%"><html:text name="volumes" indexed="true" size="6" property="maxSize"/></td>
                <td width="6%">&nbsp;</td>
              </tr>
               
              <tr> 
                <td >&nbsp;</td>
                <td align="right"><bean:message key="config.volume_actions"/>:</td>
                <td><c:if test="${volumes.statusID==0 || volumes.statusID==1}">
                <input type="submit" onClick="return indexVolume()" name="submit.indexvolume.${volumeIndex}" value="<bean:message key="config.volume_re_index"/>"> 
                </c:if>
                  <c:if test="${volumes.statusID==0}">
                	 	<input type="submit" name="submit.unmountvolume.${volumeIndex}" value="<bean:message key="config.volume_unmount"/>">
                  </c:if>
                   <c:if test="${volumes.statusID==4}">
                	 	<input type="submit" name="submit.closevolume.${volumeIndex}" value="<bean:message key="config.volume_mount"/>">
                  </c:if>
                  <c:if test="${volumes.statusID!=1}">
                  	<input type="submit" name="submit.deletevolume.${volumeIndex}" value="<bean:message key="config.volume_delete"/>">
                  </c:if>
                  <c:if test="${volumes.statusID==1}">
                  	<input type="submit" name="submit.closevolume.${volumeIndex}" value="<bean:message key="config.volume_close"/>">
                  </c:if>
                  <c:if test="${volumes.statusID==2}">
                  <input type="submit" name="submit.prioritizevolume.${volumeIndex}" value="<bean:message key="config.volume_up"/>">
                  <input type="submit" name="submit.deprioritizevolume.${volumeIndex}" value="<bean:message key="config.volume_down"/>">
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
         
          <td width="80%">
            <bean:message key="config.doms_new_label"/>:&nbsp;
            <input type="submit"  name="submit.newdomain" value="<bean:message key="config.doms_new"/>"> 
            <table width="100%" border="0" cellspacing="2" cellpadding="0">
              <logic:iterate id="domains" name="configBean" property="domains" indexId="domainIndex">
	              <tr> 
                	<td width="100%%" colspan="4" nowrap><hr></td>
              	  </tr>
	              <tr> 
	                <td width="16%" nowrap ><bean:message key="config.doms_domain"/> ${domainIndex}:</td>
	                <td width="20%" align="right" nowrap><bean:message key="config.doms_domain_name"/>: </td>
	                <td width="58%"><html:text name="domains" indexed="true" size="45" property="name" /> </td>
	                <td width="6%">&nbsp;</td>
	              </tr>
	              <tr> 
	                <td >&nbsp;</td>
	                <td align="right"><bean:message key="config.doms_domain_actions"/>:</td>
	                <td>
	                  <input type="submit" name="submit.deletedomain.${domainIndex}" value="<bean:message key="config.doms_domain_delete"/>">
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
	   
	            	<td align="right" width="30%"><bean:message key="config.sec_enc_passwd"/>: </td>
	            	<td valign="top">
	            	<c:if test="${configBean.defaultPassPhraseModified==false}">
	            		<html:password name="configBean" size="25" redisplay="false" property="passPhrase" onkeyup="testPassword(document.forms.configBean.passPhrase.value)"/>
	            		<div ID="passwordtext"><bean:message key="config.sec_enc_passwd_not_set"/></div>
	            	</c:if>
	            	<c:if test="${configBean.defaultPassPhraseModified==true}">
	            		<html:password name="configBean" size="25" redisplay="false" readonly="true" property="passPhrase"/>
	            		<div ID="passwordtext"><bean:message key="config.sec_enc_passwd_is_set"/></div>
	            	</c:if>
	            	</td>
	            </table>
            </td>
         	<td width="10%">&nbsp;</td>
      </tr>
	  	 <tr> 
  			<td width="10%" align="right" nowrap></td>
        	<td nowrap><hr></td>
        	<td nowrap>&nbsp;</td>
      	 </tr>
     	 <tr> 
	    	<td colspan="3">&nbsp;</td>
	  	</tr>
    
      <tr> 
  		<td width="10%" align="right" nowrap></td>
        <td nowrap><hr></td>
        <td nowrap>&nbsp;</td>
      </tr>
	  <tr> 
	      <td width="10%" align="right" nowrap></td>
	      <td width="80%">
	          <table width="100%" border="0" cellspacing="2" cellpadding="0">
	          <tr> 
	            <td align="right" width="30%"><bean:message key="config.sec_auth_method"/></td>  
	            <td align="left">
	                <html:select styleId="authMethod" name="configBean" property="authMethod"  onchange='OnChange(this);'>
			  				<html:options name="configBean" property="authMethods" labelName="configBean" labelProperty="authMethodLabels"/>
					</html:select>
	            </td>
	          </tr>
	        </table>
	     </td>
	     <td width="10%">&nbsp;</td>
      </tr>
      </table>
 
  
  <div id="basic">
      <table width="100%" border="0" cellpadding="0" cellspacing="0"> 
         <tr>
         <td width="10%" align="right" nowrap></td> 
          <td width="80%">   
            <table width="100%" border="0" cellspacing="2" cellpadding="0">
            
                <tr><td align="right" width="30%"><bean:message key="config.sec_role_assign"/></td>
                     <td colspan="2">
                     <input type="submit" name="submit.newbasicrolemap" value="<bean:message key="config.sec_new_role"/>"> 
                	</td>
                </tr>
            	
	            <logic:iterate id="basicRoleMaps" name="configBean" property="basicRoleMaps" indexId="userRoleAssignmentIndex2">
	              <tr> 
	                <td width="30%" nowrap align="right"><bean:message key="config.sec_assign"/> ${userRoleAssignmentIndex2}:</td>
	                <td align="right" nowrap><bean:message key="config.sec_role"/>: </td>
	                <td>
	                	<table width="100%" border="0" cellspacing="2" cellpadding="0">
	                	<tr><td>
			                <html:select indexed="true" name="basicRoleMaps" property="role">
				  				<html:options name="configBean" property="roleMapRoles" labelName="configBean" labelProperty="roleMapRoleLabels" />
						  	</html:select>
			                </td>
	                		<td width="5%">&nbsp;</td>
	             			<td align="right" nowrap><bean:message key="config.sec_rules_basic_username"/></td>
	                		<td><html:text indexed="true" name="basicRoleMaps" property="username" size="25"/></td>
	                		<td width="5%">&nbsp;</td>
	              			<td align="right" nowrap><bean:message key="config.sec_rules_method_basic_password"/></td>
	                		<td><html:password indexed="true" name="basicRoleMaps" size="25" redisplay="true" property="loginPassword"/></td>
	             			<td><input type="submit" name="submit.deletebasicrolemap.${userRoleAssignmentIndex2}" value="<bean:message key="config.sec_delete"/>"></td>
	             		</tr></table>
	             	</td>
	             </tr>
	           </logic:iterate>
            </table>
          </td>
          <td>&nbsp;</td>
        </tr>
	 </table>
	 <br/>
</div>

<div style="display: none;" id="activedirectory">
      <table width="100%" border="0" cellpadding="0" cellspacing="0"> 
	      <tr> 
	          <td width="10%" align="right" nowrap>&nbsp;</td>
	          <td width="80%">
	              <table width="100%" border="0" cellspacing="2" cellpadding="0">
		              <tr> 
			                <td align="right" width="30%"><bean:message key="config.sec_kerberos_server"/>:</td>
			                <td align="left"><html:text name="configBean" property="ADIdentity.KDCAddress" size="60"/>&nbsp;<bean:message key="config.sec_fqdn"/></td>
			          </tr>
			          <tr> 
			                <td align="right" width="30%"><bean:message key="config.sec_ldap_server"/>:</td>
			                <td align="left"><html:text name="configBean" property="ADIdentity.LDAPAddress" size="60"/>&nbsp;<bean:message key="config.sec_fqdn"/></td>
			          </tr>
	              </table>
	         </td>
	          <td>&nbsp;</td>
	      </tr>
       <tr> 
          <td width="10%" align="right" nowrap></td>
          <td width="80%">   	
                <table width="100%" border="0" cellspacing="2" cellpadding="0">
	                <tr><td align="right" width="30%"><bean:message key="config.sec_role_assign"/></td>
		                <td colspan="2">
		                <input type="submit" name="submit.newadrolemap" value="<bean:message key="config.sec_new_role"/>"> 
		            	<input type="submit" onClick="window.open('testloginform.do','testauth','width=600,height=250')" name="submit.reload" value="<bean:message key="config.sec_test_login"/>">
		            	</td>
		            </tr>
		            <logic:iterate id="ADRoleMaps" name="configBean" property="ADRoleMaps" indexId="userRoleAssignmentIndex">
		              <tr> 
		                <td align="right" nowrap><bean:message key="config.sec_assign"/> ${userRoleAssignmentIndex}:</td>
		                <td align="right" nowrap><bean:message key="config.sec_role"/>: </td>
		                <td>
		                <html:select indexed="true" name="ADRoleMaps" property="role">
			  				<html:options name="configBean" property="roleMapRoles" labelName="configBean" labelProperty="roleMapRoleLabels" />
					  	</html:select>
		                </td>
		              </tr>
		              <tr> 
		                <td >&nbsp;</td>
		                <td  align="right" nowrap>
		                	<bean:message key="config.sec_rules_ad_ldap_attr"/>:
		                </td>
		                <td>
			                <html:select indexed="true" name="ADRoleMaps" property="attribute">
				  				<html:options name="configBean" property="ADRoleMapAttributes" labelName="configBean" labelProperty="ADRoleMapAttributeLabels" />
						  	</html:select>
		                </td>
		              </tr>
		              <tr> 
		                <td >&nbsp;</td>
		                <td align="right" nowrap><bean:message key="config.sec_rules_ad_match_crit"/>:</td>
		                <td><html:text indexed="true" name="ADRoleMaps" property="regEx" size="45"/>
		               		<input type="submit" onClick="window.open('lookuprolecriterionform.do','mywindow','width=600,height=250')" name="reload" value="<bean:message key="config.sec_match_crit_lookup"/>">&nbsp;<bean:message key="config.sec_regexpr"/>
		                </td>
		              </tr>
		              <tr> 
		                <td >&nbsp;</td>
		                <td align="right"><bean:message key="config.sec_rules_actions"/>:</td>
		                <td>
		                  <input type="submit" name="submit.deleteadrolemap.${userRoleAssignmentIndex}" value="<bean:message key="config.sec_delete"/>">
		                </td>
		              </tr>
		             </logic:iterate>
            	</table>
          </td>
          <td>&nbsp;</td>
        </tr>
	 </table>
</div>
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
	                <td width="4%"><html:checkbox name="configBean"  property="archiveInbound"/><html:hidden name="configBean" property="archiveInbound" value="false"/></td>
	                <td width="5%">&nbsp;</td>
	                <td width="91%"><bean:message key="config.sec_rules_inbound"/></td>
	              </tr>
	              <tr> 
	                <td><html:checkbox name="configBean" property="archiveOutbound"/><html:hidden name="configBean" property="archiveOutbound" value="false"/></td>
	                <td>&nbsp;</td>
	                <td><bean:message key="config.sec_rules_outbound"/></td>
	              </tr>
	              <tr> 
	                <td><html:checkbox name="configBean" property="archiveInternal"/><html:hidden name="configBean" property="archiveInternal" value="false"/></td>
	                <td>&nbsp;</td>
	                <td><bean:message key="config.sec_rules_internal"/></td>
	              </tr>
	            </table>
	          </td>
	        </tr>
	       
	        <tr> 
	          <td  align="right" valign="top"></td>
	          <td>&nbsp;</td>
	          <td valign="top"><bean:message key="config.sec_rules_add"/>&nbsp;
	          <input type="submit" name="submit.newarchiverule" value="<bean:message key="config.sec_rules_new"/>"> 
	            <table width="100%" border="0" cellspacing="2" cellpadding="0">
		            <logic:iterate id="archiveRules" name="configBean" property="archiveRules" indexId="archiveFilterIndex">
		              <tr> 
                		 <td width="100%%" colspan="4" nowrap><hr></td>
              		  </tr>
		              <tr> 
		                <td width="20%" nowrap><bean:message key="config.sec_rules_rule"/> ${archiveFilterIndex}:</td>
		                <td width="20%" align="right" nowrap><bean:message key="config.sec_rules_ad_field"/>: </td>
		                <td width="70%">
		                <html:select indexed="true" name="archiveRules" property="field">
			  				<html:options name="configBean" property="ruleFields" labelName="configBean" labelProperty="ruleFieldLabels" />
					  	</html:select>
		                </td>
		                <td width="6%">&nbsp;</td>
		              </tr>
		              <tr> 
		                <td >&nbsp;</td>
		                <td align="right" nowrap><bean:message key="config.sec_rules_ad_match_crit"/>:</td>
		                <td><html:text indexed="true" name="archiveRules" property="regEx" size="45"/>&nbsp;<bean:message key="config.sec_regexpr"/></td>
		                <td></td>
		              </tr>
		              <tr> 
		                <td >&nbsp;</td>
		                <td align="right"><bean:message key="config.sec_rules_behavior"/>:</td>
		                <td>
		                <html:select indexed="true" name="archiveRules" property="action">
			  				<html:options name="configBean" property="ruleActionFields" labelName="configBean" labelProperty="ruleActionLabels" />
					  	</html:select>
		                </td>
		                <td>&nbsp;</td>
		              </tr> 
		              <tr> 
		                <td >&nbsp;</td>
		                <td align="right"><bean:message key="config.sec_rules_actions"/>:</td>
		                <td>
		                  <input type="submit" name="submit.deletearchiverule.${archiveFilterIndex}" value="<bean:message key="config.sec_rules_delete"/>">
		                  <input type="submit" name="submit.prioritizearchiverule.${archiveFilterIndex}" value="<bean:message key="config.sec_rules_up"/>">
		                  <input type="submit" name="submit.deprioritizearchiverule.${archiveFilterIndex}" value="<bean:message key="config.sec_rules_down"/>">
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




 <div id="agents" class="tabcontent">
      <table width="100%" border="0" cellpadding="0" cellspacing="0"> 
       <tr> 
	    	<td colspan="3">&nbsp;</td>
	  </tr>
      <tr> 
          <td width="10%" align="left" nowrap>&nbsp;</td>
          <td width="80%" align="left">
              <table width="100%" border="0" cellspacing="0" cellpadding="3">
	              <tr> 
		                <td align="right" width="20%"><bean:message key="config.agent_smtp_listen_port"/>:</td>
		                <td align="left"  width="20%"><html:text name="configBean" property="agentSMTPPort" size="5"/></td>
		          		<td align="right" width="70%">&nbsp;</td>
		          </tr>
		           <tr> 
		                <td align="right" width="20%"><bean:message key="config.agent_milter_listen_port"/>:</td>
		                <td align="left"  width="20%"><html:text name="configBean" property="agentMilterPort" size="5"/></td>
		          		<td align="right" width="70%">&nbsp;</td>
		          </tr>
              </table>
         </td>
          <td width="10%">&nbsp;</td>
      </tr>
       <tr> 
          <td width="10%" align="left" nowrap></td>
          <td width="80%">   	
                <table width="100%" border="0" cellspacing="0" cellpadding="3">
                	<tr>
		                <td align="right" width="20%"><bean:message key="config.agent_allow_incoming_connections"/></td>
		                <td align="left" width="20%">
		                <input type="submit" name="submit.newagentipaddress" value="<bean:message key="config.agent_new_ipaddress"/>"> 
		            	</td>
		            	<td align="right" width="70%">&nbsp;</td>
            		</tr>
	            <logic:iterate id="agentIPAddress" name="configBean" property="agentIPAddresses" indexId="agentIPAddressIndex">
	              <tr>
	             	<td align="left">&nbsp;</td> 
	                <td align="right"><bean:message key="config.agent_ipaddress"/> ${agentIPAddressIndex}</td>
	                <td align="left"><html:text property="agentIPAddress[${agentIPAddressIndex}]" size="20"/>
	                &nbsp;<input type="submit" name="submit.deleteagentipaddress.${agentIPAddressIndex}" value="<bean:message key="config.agent_ipaddress_delete"/>">
	                </td>
	                 
	              </tr>
	            </logic:iterate>
            </table>
          </td>
          <td>&nbsp;</td>
        </tr>
        
       <tr> 
	    <td colspan="3">&nbsp;</td>
	  </tr>
	 </table>
</div>


<div id="about" class="tabcontent">
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
	          <td align="center">
	            <bean:message key="config.application_title"/>&nbsp;v<c:out value="${configBean.version}"/><br><br>
	            <bean:message key="config.about_copyright"/>&nbsp;<br>
	            &nbsp;<bean:message key="config.application_internet"/>&nbsp;<a href="http://www.mailarchiva.com">www.mailarchiva.com</a><br>
	            <bean:message key="config.application_email"/>&nbsp;<a href="mailto:info@mailarchiva.com">info@mailarchiva.com</a><br><br>	          
	          	&nbsp;<a href="http://www.mailarchiva.com"><bean:message key="config.about_upgrade"/></a><br>
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

<div id="logging" class="tabcontent">
  <table width="100%" border="0" cellpadding="0" cellspacing="0">
	  <tr> 
	    <td colspan="3">&nbsp;</td>
	  </tr>
	  <tr> 
	    <td width="10%" align="right" >&nbsp;</td>
	    <td width="80%" >
	       <table width="100%" border="0" cellpadding="0" cellspacing="3">
	        <tr> 
	          <td align="right" valign="top" nowrap></td>
	          <td align="right"><input type="submit" onclick="viewAuditLog('audit');" value="<bean:message key="config.log_audit_view"/>"/></td>
	          <td align="left"><bean:message key="config.log_audit_view_description"/></td>
	          <td width="10%">&nbsp;</td>
 			</tr>
 			<tr><td colspan="4">&nbsp;</td></tr>
	        <tr>  
	          <td align="right" valign="top" nowrap></td>
	          <td align="right"><input type="submit" onclick="viewDebugLog('debug');" value="<bean:message key="config.log_debug_view"/>"/></td>
	          <td align="left"><bean:message key="config.log_debug_view_description"/></td>
	          <td width="10%">&nbsp;</td>
 			</tr>
 			<tr><td colspan="4">&nbsp;</td></tr>
 			 <tr>  
	          <td align="right" valign="top" nowrap></td>  		
	          <td align="right"><bean:message key="config.log_level_description"/></td>
	           <td align="left">	
	          		<html:select name="configBean" property="debugLoggingLevel"> 
	                      <html:options name="configBean" property="debugLoggingLevels" labelName="configBean" labelProperty="debugLoggingLevelLabels" /> 
	                </html:select>
	          </td> 
	          <td width="10%">&nbsp;</td>   
 			</tr>
 			              
   		</table>
   		</td>
   		<tr><td colspan="3">&nbsp;</td></tr>
   	</table>
</div>

<table class="sectionheader" width="100%" border="0" cellpadding="3" cellspacing="0">
  <tr> 
    <td >&nbsp;</td>
    <td align="left"><input type="submit" name="submit.save" value="<bean:message key="config.button.save"/>"><input type="submit" name="submit.cancel" value="<bean:message key="config.button.cancel"/>"> 
    </td>
  </tr>
</table>
<%@include file="../common/bottom.jsp"%>
</html:form>
</DIV>
</body>
</html>
