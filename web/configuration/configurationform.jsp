<%-- <!-- MailArchiva Email Archiving Software 
	 Copyright Jamie Band 2008
-->

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="/WEB-INF/tld/c.tld" %>
<%@ taglib prefix="fmt" uri="/WEB-INF/tld/fmt.tld" %>
<%@ taglib prefix="fn" uri="/WEB-INF/tld/fn.tld" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<html>d
<head>
<meta http-equiv="expires" content="-1">
<meta http-equiv="no-cache"> 
<title><bean:message key="config.title"/></title>

<link href="common/mailarchiva.css" rel="stylesheet" type="text/css">
<meta http-equiv="expires" content="0">
<meta http-equiv="no-cache"> 
<meta http-equiv="Cache-Control" content="no-cache">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">

<script type="text/javascript" src="common/jquery-1.7.1.js"></script>
<script type="text/javascript" src="common/tabcontent.js">

/***********************************************
* Tab Content script v2.2- Â© Dynamic Drive DHTML code library (www.dynamicdrive.com)
* This notice MUST stay intact for legal use
* Visit Dynamic Drive at http://www.dynamicdrive.com/ for full source code
***********************************************/

</script>


<script type="text/javascript">

function show(id) {
	obj = document.getElementById(id);
	obj.style.display = "";
}

function hide(id) {
	obj = document.getElementById(id);
	obj.style.display = "none";
}



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

function viewLog(logfile) {
	window.open('viewlog.do?logFile='+logfile,'log','width=800,height=600');
}

function testPassword(passwd) {
	strVerdict = getPasswordStrengthVerdict(passwd);
	document.getElementById("passwordtext").innerHTML = "(<bean:message key="config.password_strength"/> " + strVerdict + ")";
}

function testPassword2(passwd) {
	strVerdict = getPasswordStrengthVerdict(passwd);
	document.getElementById("passwordtext2").innerHTML = "(<bean:message key="config.password_strength"/> " + strVerdict + ")";
}

function getPasswordStrengthVerdict(passwd)
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
		return strVerdict;
	
}


function recover() 
{
	window.open('recover.do','send','width=600,height=400');
	return true;
}


function OnChange(dropdown)
{
	var myindex  = dropdown.selectedIndex
	var SelValue = dropdown.options[myindex].value
	
	if (SelValue=="activedirectory") {
		// Mod Start HyunSeok.Shin 2013-05-14
		// Active Directory사용 안함
		$('#authMethod').val("basic");
		alert('Active Directory는 지원하지 않습니다.');
		//show('activedirectory');
		//hide('basic');
		return false;
        // Mod End HyunSeok.Shin 2013-05-14
	} else if (SelValue=="basic") {
		show('basic');
		hide('activedirectory');
	}
	return true;
}

function changePort(selectedindex) {
	portElem = document.getElementById('port');
	sslPortElem = document.getElementById('sslport');	
	if (selectedindex==0) {
		portElem.value = 110;
		sslPortElem.value = 995;
	} else {
		username.disabled = true;
	}
}

function testMailboxConnection() {
	window.open('testmailboxstatus.do','send','width=600,height=400');
	return true;
}

function lookup(index) {
	var attribute = document.getElementsByName("ADRoleMaps["+index+"].attribute")[0].value; 
	window.open("lookuprolecriterionform.do?lookupIndex="+index,'mywindow','width=600,height=250')
}

</script>
</head>

<body onLoad="OnChange(document.getElementById('authMethod'));">
<html:form action="/configure" method="POST" styleId="configure" autocomplete="false">
<%@include file="../common/menu.jsp"%>

<table class="pageheading" width="100%" border="0" cellpadding="0" cellspacing="0" >
  <tr > 
    <td width="28%"><strong><bean:message key="config.title"/></strong></td>
    <td align="left">&nbsp;</td>
    <td width="72%"></td>
      
  </tr>
</table>

<table class="pagedialog" width="100%" border="0" cellpadding="0" cellspacing="0" >
  <tr> 
    <td width="100%" >
<ul id="configtabs" class="shadetabs">
<li><a href="#" rel="domains" class="selected"><bean:message key="config.domains"/></a></li>
<li><a href="#" rel="volumes"><bean:message key="config.volumes"/></a></li>
<li><a href="#" rel="authentication" ><bean:message key="config.login"/></a></li>
<!-- # Del Start HyunSeok.Shin 2013-05-13 -->
<!--li><a href="#" rel="mailboxes" ><bean:message key="config.mailbox_connections"/></a></li-->
<!-- # Del End HyunSeok.Shin 2013-05-13 -->
<li><a href="#" rel="archiverules" ><bean:message key="config.archive_rules"/></a></li>
<li><a href="#" rel="listeners" ><bean:message key="config.listeners"/></a></li>
<li><a href="#" rel="general" ><bean:message key="config.general"/></a></li>
<li><a href="#" rel="logging" ><bean:message key="config.log"/></a></li>
<!-- # Del Start HyunSeok.Shin 2013-05-13 -->
<!--li><a href="#" rel="about" ><bean:message key="config.about"/></a></li-->
<!-- # Del End HyunSeok.Shin 2013-05-13 -->
</ul>

</td>
  </tr>
</table>
<DIV id="tabcontentcontainer">

<logic:present name="errors">
<table class="validationtext" width="100%" border="0" cellpadding="0" cellspacing="0" >
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
<table class="infotext" width="100%" border="0" cellpadding="0" cellspacing="0" >
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
  				<table width="100%" border="0" cellspacing="2" cellpadding="0">
	   				<tr>
	            		<td align="right" width="20%" nowrap><bean:message key="config.sec_enc_passwd"/>: </td>
	            		<td nowrap >
	            		<c:if test="${configBean.defaultPassPhraseModified==false}">
	            			<html:password name="configBean" size="25" redisplay="false" property="passPhrase" onkeyup="testPassword(document.forms.configBean.passPhrase.value)"/>
	            			&nbsp;<bean:message key="config.sec_enc_passwd_again"/>&nbsp;<html:password name="configBean" size="25" redisplay="false" readonly="false" property="passPhraseAgain"/>
	            			<div ID="passwordtext"><bean:message key="config.sec_enc_passwd_not_set"/></div>
	            		</c:if>
	            		<c:if test="${configBean.defaultPassPhraseModified==true}">
	            			<html:password name="configBean" size="25" redisplay="false" readonly="true" property="passPhrase"/>
	            			<div ID="passwordtext" align><bean:message key="config.sec_enc_passwd_is_set"/></div>
	            		</c:if>
	            		</td>
	            	</tr>
	            </table>
	    </td>
	    <td width="30%" align="right"></td>
	</tr>          
         
    <tr> 
      <td align="right" nowrap></td>
      <td >
      			<table width="100%" border="0" cellspacing="2" cellpadding="0">
	   				<tr>
	            	<td align="right" width="20%" nowrap><bean:message key="config.new_volume"/>: </td>
	            	<td nowrap>
	            		<input type="submit"  name="submit.newvolume" value="<bean:message key="config.new_volume"/>"> 
	            	</td>
	            	</tr>
	            </table>
          
      </td>
      	<td align="right"></td>
    </tr>
    
    <tr> 
      <td colspan="3">&nbsp;</td>
    </tr>
   <tr><td align="right" nowrap>&nbsp;</td>
      	<td >
      	
   			<c:if test="${fn:length(configBean.volumes)>0}">
   			<table class="tableborder" width="100%" border="0" cellspacing="2" cellpadding="0">
        		<thead align="center" class="tablehead">
					<tr>
          	   		<td width="2%" align="center" nowrap><bean:message key="config.volume"/>&nbsp;</td>
          	    	<td width="2%" align="center" nowrap><bean:message key="config.volume_status"/>&nbsp;</td>
          	    	<td width="8%" align="center" nowrap><bean:message key="config.volume_created"/>&nbsp;</td>
          	  		<td width="8%" align="center" nowrap><bean:message key="config.volume_last_mod"/>&nbsp;</td>
          	  		<td width="4%" align="center" nowrap><bean:message key="config.volume_store_path"/>&nbsp;</td>
          	  		<td width="8%" align="center" nowrap><bean:message key="config.volume_max_size"/>&nbsp;</td>
          	  		<td width="8%" align="center" nowrap><bean:message key="config.volume_free_space"/>&nbsp;</td>
          	  		<td width="8%" align="center" nowrap><bean:message key="config.volume_doc_count"/>&nbsp;</td>
          	  		<td width="4%" align="center" nowrap><bean:message key="config.volume_index_path"/>&nbsp;</td>
          	  		<td width="8%" align="center" nowrap><bean:message key="config.volume_free_space"/>&nbsp;</td>
          	  		<td width="8%" align="center" nowrap><bean:message key="config.volume_actions"/>&nbsp;</td>
          	 		</tr>
          	   </thead>
          	 <tbody>
          <logic:iterate id="volumes" name="configBean" property="volumes" indexId="volumeIndex">
              <tr>
          	    <td align="left" nowrap><c:out value="${volumeIndex}"/>&nbsp;</td>
          	    <td align="left" nowrap><c:out value="${volumes.status}"/>&nbsp;</td>
          	    <td align="left" nowrap><c:if test="${volumes.created!=''}"><c:out value="${volumes.created}"/>&nbsp;</c:if></td>
          	    <td align="left" nowrap><c:if test="${volumes.closed!=''}"><c:out value="${volumes.closed}"/>&nbsp;</c:if></td>
               
          	    <td align="center" nowrap>
          	    	<c:if test="${volumes.statusID>1}">
                		<html:text name="volumes" indexed="true" size="15" property="path" />&nbsp; 
               		 </c:if>
               		 <c:if test="${volumes.statusID<2}">
                		<html:text name="volumes" indexed="true" readonly="true" size="15" property="path" />&nbsp; 
               		 </c:if>
               	</td>
               <td align="left" nowrap><html:text name="volumes" indexed="true" size="6" property="maxSize"/>&nbsp;</td>
               <td align="left" nowrap>
               		<c:if test="${volumes.freeArchiveSpace!=''}">
                		<c:out value="${volumes.freeArchiveSpace}"/>&nbsp;
                	</c:if>
               	</td>
          	    <td align="left" nowrap><c:out value="${volumes.totalMessageCount}"/>&nbsp;</td>
               	<td align="left" nowrap>
               		<c:if test="${volumes.statusID>1}">
                		<html:text name="volumes" indexed="true" size="15" property="indexPath" />&nbsp; 
                	</c:if>
                	<c:if test="${volumes.statusID<2}">
                		<html:text name="volumes" indexed="true" readonly="true" size="20" property="indexPath" />&nbsp; 
               		</c:if>
               	</td>
               	<td align="left" nowrap>
               		<c:if test="${volumes.freeIndexSpace!=''}">
                		<c:out value="${volumes.freeIndexSpace}"/>&nbsp;
                	</c:if>
               	</td>
               
          	   <td align="left" nowrap><c:if test="${volumes.statusID==0 || volumes.statusID==1}">
                <!--input type="submit" onClick="return indexVolume()" name="submit.indexvolume.${volumeIndex}" value="<bean:message key="config.volume_re_index"/>"--> 
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
                  	<input type="submit" name="submit.closevolume.${volumeIndex}" value="<bean:message key="config.volume_close"/>"  onclick="return confirm ('<bean:message key="config.confirm_close_volume"/>');">
                  </c:if>
                  <c:if test="${volumes.statusID==2}">
                  <input type="submit" name="submit.prioritizevolume.${volumeIndex}" value="<bean:message key="config.volume_up"/>">
                  <input type="submit" name="submit.deprioritizevolume.${volumeIndex}" value="<bean:message key="config.volume_down"/>">
                   </c:if>
                </td>
              </tr>
          </logic:iterate>
          </tbody>
        </table></c:if></td>
        <td align="right"></td>
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
	                <td width="5%" nowrap><bean:message key="config.doms_domain"/> ${domainIndex}:</td>
	                <td width="10%" align="right" nowrap><bean:message key="config.doms_domain_name"/>: </td>
	                <td align="left"><html:text name="domains" indexed="true" size="45" property="name" /> </td>
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
             <td>&nbsp;</td>
        </tr>
        <tr> 
	    	<td colspan="3">&nbsp;</td>
	  	</tr>
       </table>
</div>

<div id="authentication" class="tabcontent">

	  <table width="100%" border="0" cellpadding="0" cellspacing="0">
      <tr> 
  		<td width="10%" align="right" nowrap></td>
        <td width="80%" nowrap></td>
        <td nowrap>&nbsp;</td>
      </tr>
      <c:if test="${configBean.allowMasterPasswordEntry==true}">
       <tr>
  	 		<td width="10%" align="right" nowrap></td>
  	 		<td width="80%">
  				<table width="100%" border="0" cellspacing="2" cellpadding="0">
	   				<tr>
	            		<td align="right" width="20%" nowrap><bean:message key="config.sec_master_login_passwd"/>: </td>
	            		<td width="80%" nowrap >
	            		<c:if test="${configBean.defaultMasterLoginPasswordModified==false}">
	            			<html:password name="configBean" size="25" redisplay="false" property="masterLoginPassword" onkeyup="testPassword2(document.forms.configBean.masterLoginPassword.value)"/>
	            			&nbsp;<bean:message key="config.sec_master_login_passwd_again"/>&nbsp;<html:password name="configBean" size="25" redisplay="false" readonly="false" property="masterLoginPasswordAgain"/>
	            			<div ID="passwordtext2"><bean:message key="config.sec_master_login_passwd_not_set"/></div>
	            		</c:if>
	            		<c:if test="${configBean.defaultMasterLoginPasswordModified==true}">
	            			<html:password name="configBean" size="25" redisplay="false" property="masterLoginPassword" onkeyup="testPassword2(document.forms.configBean.masterLoginPassword.value)"/>
	            			&nbsp;<bean:message key="config.sec_master_login_passwd_again"/>&nbsp;<html:password name="configBean" size="25" redisplay="false" readonly="false" property="masterLoginPasswordAgain"/>
	            			<div ID="passwordtext2" align><bean:message key="config.sec_master_login_passwd_is_set"/></div>
	            		</c:if>
	            		</td>
	            	</tr>
	            </table>
	    	</td>
	    	<td width="30%" align="right"></td>
		</tr>
		</c:if>
	  	<tr> 
	      <td align="right" nowrap></td>
	      <td >
	          <table width="100%" border="0" cellspacing="2" cellpadding="0">
	          <tr> 
	            <td align="right" width="20%" nowrap><bean:message key="config.login_default_domain"/></td>  
	            <td align="left" width="80%" nowrap>
	              <html:text name="configBean" property="defaultLoginDomain" size="25"/>
	              &nbsp;<bean:message key="config.config.login_default_domain_hint"/>
	            </td>
	            <td nowrap>&nbsp;</td>
	          </tr>
	          <tr> 
	            <td align="right" nowrap><bean:message key="config.sec_auth_method"/></td>  
	            <td align="left" nowrap>
	                <html:select styleId="authMethod" name="configBean" property="authMethod"  onchange='OnChange(this);'>
			  				<html:options name="configBean" property="authMethods" labelName="configBean" labelProperty="authMethodLabels"/>
					</html:select>
	            </td>
	            <td nowrap>&nbsp;</td>
	          </tr>
	        </table>
	     </td>
	     <td >&nbsp;</td>
      </tr>
      </table>
 
  
  <div id="basic">
      <table width="100%" border="0" cellpadding="0" cellspacing="0"> 
         <tr>
         <td width="10%" align="right" nowrap></td> 
          <td width="80%">   
            <table width="100%" border="0" cellspacing="2" cellpadding="0">
            
                <tr><td width="20%" align="right" nowrap><bean:message key="config.sec_role_assign"/></td>
                     <td colspan="2">
                     <input type="submit" name="submit.newbasicrolemap" value="<bean:message key="config.sec_new_role"/>"> 
                	</td>
                	<td align="right" nowrap>&nbsp;</td>
                </tr>
            	
	            <logic:iterate id="basicRoleMaps" name="configBean" property="basicRoleMaps" indexId="userRoleAssignmentIndex2">
	              <tr> 
	                <td nowrap align="right"><bean:message key="config.sec_assign"/> ${userRoleAssignmentIndex2}:</td>
	                <td align="right" nowrap><bean:message key="config.sec_role"/>: </td>
	                <td>
	                	<table  border="0" cellspacing="2" cellpadding="0">
	                	<tr><td>
			                <html:select indexed="true" name="basicRoleMaps" property="role">
				  				<html:options name="configBean" property="roleValues" labelName="configBean" labelProperty="roleValues" />
						  	</html:select>
			                </td>
	                		<td width="1%">&nbsp;</td>
	             			<td align="right" nowrap><bean:message key="config.sec_rules_basic_username"/></td>
	                		<td><html:text indexed="true" name="basicRoleMaps" property="username" size="25"/></td>
	                		<td width="1%">&nbsp;</td>
	              			<td align="right" nowrap><bean:message key="config.sec_rules_method_basic_password"/></td>
	                		<td><html:password indexed="true" name="basicRoleMaps" size="25" redisplay="true" property="loginPassword"/></td>
	             			<td><input type="submit" name="submit.deletebasicrolemap.${userRoleAssignmentIndex2}" value="<bean:message key="config.sec_delete"/>"></td>
	             		</tr></table>
	             	</td>
	             	<td align="right" nowrap>&nbsp;</td>
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
			                <td align="right" width="30%" nowrap><bean:message key="config.sec_kerberos_server"/>:</td>
			                <td align="left" nowrap><html:text name="configBean" property="ADIdentity.KDCAddress" size="60"/>&nbsp;<bean:message key="config.sec_fqdn"/></td>
			          </tr>
			          
			          <tr> 
			                <td align="right" width="30%" nowrap><bean:message key="config.sec_kerberos_server_ipaddress"/>:</td>
			                <td align="left" nowrap><html:text name="configBean" property="ADIdentity.KDCIPAddress" size="60"/>&nbsp;<bean:message key="config.sec_kerberos_server_ipaddress_hint"/></td>
			          </tr>
			          
			          <tr> 
			                <td align="right" width="30%" nowrap><bean:message key="config.sec_ldap_server"/>:</td>
			                <td align="left" nowrap><html:text name="configBean" property="ADIdentity.LDAPAddress" size="60"/>&nbsp;<bean:message key="config.sec_fqdn"/></td>
			          </tr>
			          <tr> 
		                <td align="right" width="30%" nowrap><bean:message key="config.sec_ldap_basedn"/>:</td>
		                <td align="left" nowrap><html:text name="configBean" property="ADIdentity.baseDN" size="60"/></td>
		         	 </tr>
		          	 <tr> 
		                <td align="right" width="30%" nowrap><bean:message key="config.sec_ad_servicelogin"/>:</td>
		                <td align="left" nowrap><html:text name="configBean" property="ADIdentity.serviceDN" size="60"/></td>
		          	</tr>
		           	<tr> 
		                <td align="right" width="30%" nowrap><bean:message key="config.sec_ldap_servicepassword"/>:</td>
		                <td align="left" nowrap><html:password name="configBean" property="ADIdentity.servicePassword" size="60"/></td>
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
		                <td colspan="2" nowrap>
		                <input type="submit" name="submit.newadrolemap" value="<bean:message key="config.sec_new_role"/>"> 
		            	
		            	</td>
		            </tr>
		            <logic:iterate id="ADRoleMaps" name="configBean" property="ADRoleMaps" indexId="userRoleAssignmentIndex">
		              <tr> 
		                <td align="right" nowrap><bean:message key="config.sec_assign"/> ${userRoleAssignmentIndex}:</td>
		                <td align="right" nowrap><bean:message key="config.sec_role"/>: </td>
		                <td>
		                <html:select indexed="true" name="ADRoleMaps" property="role">
			  				<html:options name="configBean" property="roleValues" labelName="configBean" labelProperty="roleValues" />
					  	</html:select>
		                </td>
		              </tr>
		              <tr> 
		                <td >&nbsp;</td>
		                <td  align="right" nowrap>
		                	<bean:message key="config.sec_rules_ad_ldap_attr"/>:
		                </td>
		                <td nowrap>
			                <html:text indexed="true" name="ADRoleMaps" property="attribute" size="45"/>
						  	<input type="submit" onClick="javascript:lookup(${userRoleAssignmentIndex});" name="reload" value="<bean:message key="config.sec_match_crit_lookup"/>">
						  	
		                </td>
		              </tr>
		              <tr> 
		                <td >&nbsp;</td>
		                <td align="right" nowrap><bean:message key="config.sec_matches"/></td>
		                <td><html:text indexed="true" name="ADRoleMaps" property="regEx" size="45"/>
		               	
		                </td>
		              </tr>
		              <tr> 
		                <td >&nbsp;</td>
		                <td align="right" nowrap><bean:message key="config.sec_rules_actions"/>:</td>
		                <td nowrap>
		                  <input type="submit" name="submit.deleteadrolemap.${userRoleAssignmentIndex}" value="<bean:message key="config.sec_delete"/>">
		                  <input type="submit" onClick="window.open('testloginform.do','testauth','width=600,height=250')" name="submit.reload" value="<bean:message key="config.sec_test_login"/>">
		                
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
	            <html:hidden name="configBean" property="archiveInbound" value="false"/>
	              <!-- # Del Start HyunSeok.Shin 2013-05-13 -->
	              <!--tr> 
	                <td width="4%" align="right"><html:checkbox name="configBean"  property="archiveInbound"/><html:hidden name="configBean" property="archiveInbound" value="false"/></td>
	                <td width="5%">&nbsp;</td>
	                <td width="91%" align="left"><bean:message key="config.sec_rules_inbound"/></td>
	              </tr-->
                  <!-- # Del End HyunSeok.Shin 2013-05-13 -->
	              <tr> 
	                <td  align="right"><html:checkbox name="configBean" property="archiveOutbound"/><html:hidden name="configBean" property="archiveOutbound" value="false"/></td>
	                <td>&nbsp;</td>
	                <td align="left"><bean:message key="config.sec_rules_outbound"/></td>
	              </tr>
                  <!-- # Del Start HyunSeok.Shin 2013-05-13 -->
	              <!--tr> 
	                <td  align="right"><html:checkbox name="configBean" property="archiveInternal"/><html:hidden name="configBean" property="archiveInternal" value="false"/></td>
	                <td>&nbsp;</td>
	                <td align="left"><bean:message key="config.sec_rules_internal"/></td>
	              </tr-->
                  <!-- # Del End HyunSeok.Shin 2013-05-13 -->
                  
                  <!-- # Add Start HyunSeok.Shin 2013-05-13 -->
	              <html:hidden name="configBean" property="archiveInbound" value="false"/>
                  <html:hidden name="configBean" property="archiveInternal" value="false"/>
                  <!-- # Add End HyunSeok.Shin 2013-05-13 -->
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
						<td width="20%" align="right" nowrap><i><bean:message key="config.sec_rules_rule"/> ${archiveFilterIndex}:</i></td>
						<td width="10%" align="right" nowrap><bean:message key="config.sec_rules_operator"/></td>
						<td width="70%">
							<html:select indexed="true" name="archiveRules" property="operator">
								<html:options name="configBean" property="ruleOperatorFields" labelName="configBean" labelProperty="ruleOperatorLabels" />
							</html:select>
						</td>
						<td width="6%">&nbsp;</td>
		          	 </tr>   
					  <tr> 
						<td width="20%" nowrap></td>
						<td width="10%" align="right" nowrap></td>
						<td width="70%">
						  	<table width="100%" border="0" cellspacing="2" cellpadding="0">
							  	<logic:iterate id="archiveClauses" name="archiveRules" property="archiveClauses" indexId="archiveClauseIndex">
									<tr> 
										<td nowrap><bean:message key="config.sec_rules_clauses"/> ${archiveClauseIndex}:</td>
										<td align="right" nowrap>
											<html:select name="configBean" property="archiveRules[${archiveFilterIndex}].archiveClauses[${archiveClauseIndex}].field">
												<html:options name="configBean" property="ruleFields" labelName="configBean" labelProperty="ruleFieldLabels" />
											</html:select>
										</td>
										<td align="right" nowrap>
											<html:select name="configBean" property="archiveRules[${archiveFilterIndex}].archiveClauses[${archiveClauseIndex}].condition">
												<html:options name="configBean" property="ruleClauseConditionFields" labelName="configBean" labelProperty="ruleClauseConditionLabels" />
											</html:select>
										</td>
										<td align="right" nowrap><html:text name="configBean" property="archiveRules[${archiveFilterIndex}].archiveClauses[${archiveClauseIndex}].value" size="20"/></td>
										<td align="left"><input type="submit" name="submit.newarchiveruleclause.${archiveFilterIndex}" value="&nbsp;+&nbsp;"></td>
										<td align="left"><c:if test="${fn:length(archiveRules.archiveClauses)>1}"><input type="submit" name="submit.deletearchiveruleclause.${archiveFilterIndex}.${archiveClauseIndex}" value="&nbsp;-&nbsp;"></c:if></td>
	                        			<td width="50%">&nbsp;</td>
	                        		</tr>
								</logic:iterate>
							</table>
						</td>
						<td width="6%">&nbsp;</td>
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

 <div id="listeners" class="tabcontent">
 
      <table width="100%" border="0" cellpadding="0" cellspacing="0"> 
       <tr> 
	    	<td colspan="3">&nbsp;</td>
	   </tr>
	   <tr>
	     <td width="10%" align="left" nowrap></td>
	     <td width="80%" align="left"></td> 
	     <td width="10%"></td>
	   </tr>
       <tr> 
          <td width="10%" align="left" nowrap>&nbsp;</td>
          <td width="80%" align="left">
             <table width="100%" border="0" cellspacing="0" cellpadding="3">
              	   <tr> 
	                <td width="5%"></td>
	         		<td width="85%"><html:checkbox name="configBean"  property="agentSMTPEnable"/><html:hidden name="configBean" property="agentSMTPEnable" value="false"/>&nbsp;<bean:message key="config.agent_smtp_listen_port_enable"/></td>
	               	<td width="10%"></td>
	               </tr>
	          </table>
              <table width="100%" border="0" cellspacing="0" cellpadding="3">
              	  <tr> 
		                <td align="right" width="10%"></td>
		                <td align="left"  width="70%"><bean:message key="config.agent_smtp_listen_port"/>:&nbsp;<html:text name="configBean" property="agentSMTPPort" size="5"/>
		                								&nbsp;<bean:message key="config.agent_smtp_listen_ipaddress"/>:&nbsp;
		                								<html:select name="configBean" property="agentSMTPAddress">
				  											<html:options name="configBean" property="bindIPAddresses" labelName="configBean" labelProperty="bindIPAddressLabels" />
						  								</html:select>
		                
		                </td>
		                <td align="left"  width="20%"></td> 
		          </tr>
		          <tr> 
		                <td align="right">&nbsp;</td>
		              	<td><html:checkbox name="configBean"  property="agentSMTPAuth"/><html:hidden name="configBean" property="agentSMTPAuth" value="false"/>&nbsp;<bean:message key="config.agent_smtp_authentication"/></td>
		                <td align="left"></td> 
		          </tr>
		          <tr> 
		                <td align="right"></td>
		              	<td>&nbsp;<bean:message key="config.agent_smtp_username"/>:&nbsp;<html:text name="configBean" property="agentSMTPUsername" size="20"/>&nbsp;
		              	<bean:message key="config.agent_smtp_password"/>:&nbsp;<html:password name="configBean" property="agentSMTPPassword" size="20"/>
		              	</td>
		                <td align="left"></td> 
		          </tr>
		          <tr> 
		                <td align="right">&nbsp;</td>
		              	<td><html:checkbox name="configBean"  property="agentSMTPTLS"/><html:hidden name="configBean" property="agentSMTPTLS" value="false"/>&nbsp;<bean:message key="config_agent_smtp_tls"/></td>
		                <td align="left"></td> 
		          </tr>
	          </table>
	          <table width="100%" border="0" cellspacing="0" cellpadding="3">
              	  <tr> 
	                <td width="5%"></td>
	         		<td width="85%"><html:checkbox name="configBean"  property="agentMilterEnable"/><html:hidden name="configBean" property="agentMilterEnable" value="false"/>&nbsp;<bean:message key="config.agent_milter_listen_port_enable"/></td>
	               	<td width="10%"></td>
	               </tr>
	          </table> 
	           <table width="100%" border="0" cellspacing="0" cellpadding="3">  
		           <tr> 
		                <td align="right" width="10%"></td>
		                <td align="left"  width="70%"><bean:message key="config.agent_milter_listen_port"/>:&nbsp;<html:text name="configBean" property="agentMilterPort" size="5"/>
		                								&nbsp;<bean:message key="config.agent_smtp_listen_ipaddress"/>:&nbsp;
		                								<html:select name="configBean" property="agentMilterAddress">
				  											<html:options name="configBean" property="bindIPAddresses" labelName="configBean" labelProperty="bindIPAddressLabels" />
						  								</html:select>
						  								</td>
		          		<td align="left"  width="20%"></td> 
		          </tr>
              </table>
         </td>
          <td width="10%">&nbsp;</td>
      </tr>
      </table>
        <table width="100%" border="0" cellspacing="0" cellpadding="3">
                	<tr>
                		<td align="right" width="15%">&nbsp;</td>
		                <td align="left" width="85%"><bean:message key="config.agent_allow_incoming_connections"/>&nbsp;
		                <input type="submit" name="submit.newagentipaddress" value="<bean:message key="config.agent_new_ipaddress"/>"> 
		            	</td>
            		</tr>
	               
                </table>
     			<table width="100%" border="0" cellspacing="0" cellpadding="3">
                	<logic:iterate id="agentIPAddress" name="configBean" property="agentIPAddresses" indexId="agentIPAddressIndex">
	                  <tr>
	                    <td align="right"  width="20%">&nbsp;</td>
	                    <td align="left"><bean:message key="config.agent_ipaddress"/> ${agentIPAddressIndex}:&nbsp;<html:text property="agentIPAddress[${agentIPAddressIndex}]" size="20"/>
	                    <input type="submit" name="submit.deleteagentipaddress.${agentIPAddressIndex}" value="<bean:message key="config.agent_ipaddress_delete"/>">
	                    </td>    
	                  </tr>
	                </logic:iterate>
                </table>  
          <table width="100%" border="0" cellspacing="0" cellpadding="3">
              	  <tr> 
	                <td width="5%"></td>
	         		<td width="85%"></td>
	               	<td width="10%"></td>
	               </tr>
	          </table>          
     
</div>

 <div id="mailboxes" class="tabcontent">
 	<table width="100%" border="0" cellpadding="0" cellspacing="0"> 
       <tr>
	     <td width="10%" align="left" nowrap></td>
	     <td width="80%" align="left"></td> 
	     <td >&nbsp;</td>
	  </tr>
	    <tr>
	     <td align="left" nowrap></td>
	     <td align="left">
	     			<table width="100%" border="0" cellspacing="0" cellpadding="3">
                	<tr>
		                <td align="right" width="30%" nowrap><bean:message key="config.mailbox_connections_polling_interval_secs"/></td>
		                <td align="left" width="40%" nowrap>
		                <html:text name="configBean" property="pollingIntervalSecs" size="4" />
		            	</td>
		            	<td align="right">&nbsp;</td>
            		</tr></table></td> 
	     <td>&nbsp;</td>
	  </tr>
	    <tr>
	     <td align="left" nowrap></td>
	     <td align="left">
	     			<table width="100%" border="0" cellspacing="0" cellpadding="3">
                	<tr>
		                <td align="right" width="30%" nowrap><bean:message key="config.mailbox_connections_max_messages"/></td>
		                <td align="left" width="40%" nowrap>
		                 <html:text name="configBean" property="mailboxMaxMessages" size="4" />&nbsp;
		                 <bean:message key="config.mailbox_connections_max_messages_hint"/>
		            	</td>
		            	<td align="right">&nbsp;</td>
            		</tr></table></td> 
	     <td>&nbsp;</td>
	  </tr>
	  
	   <tr>
	     <td align="left" nowrap>&nbsp;</td>
	     <td align="left">
	     			<table width="100%" border="0" cellspacing="0" cellpadding="0">
                		
            			
            			<tr>
		                	<td align="right" colspan="3"><hr></td>
						</tr>
            			<tr>
                		 <td align="left" valign="top"><bean:message key="config.mailbox_connections_connection"/> ${mailboxConnectionIndex}:</td>
                		 <td align="left" colspan="2">&nbsp;
                		 	<table width="100%" border="0" cellspacing="0" cellpadding="1">
                		 	<tr><td align="right"><bean:message key="config.mailbox_connections_enabled"/></td><td align="left"><html:checkbox name="configBean" property="mailboxConnection.enabled"/><html:hidden name="configBean" property="mailboxConnection.enabled" value="false"/>&nbsp;
                		 	
                		 	<td></tr>	
                		 	<tr><td align="right" width="20%">
									<bean:message key="config.mailbox_connections_protocol"/>
								</td>
								<td align="left" width="80%">
									<html:select name="configBean" property="mailboxConnection.protocol"  onchange="changePort(this.options.selectedIndex);">
										<html:options name="configBean" property="protocols" labelName="configBean" labelProperty="protocolLabels"/>
									</html:select>
								</td>
							</tr>
							<tr><td align="right"><bean:message key="config.mailbox_connections_server"/></td><td align="left"><html:text name="configBean" property="mailboxConnection.serverName" size="25"/>&nbsp;<td></tr>		
							<tr><td align="right"><bean:message key="config.mailbox_connections_port"/></td><td align="left"><html:text styleId="port" size="5" name="configBean" property="mailboxConnection.port"/>&nbsp;<bean:message key="config.mailbox_connections_sslport"/>&nbsp;<html:text styleId="sslport" name="configBean" size="5" property="mailboxConnection.SSLPort"/></td></tr>			
							<tr><td align="right"><bean:message key="config.mailbox_connections_username"/></td><td align="left"><html:text size="25" name="configBean" property="mailboxConnection.username"/>&nbsp;<td></tr>	
							<tr><td align="right"><bean:message key="config.mailbox_connections_password"/></td><td align="left"><html:password size="25" name="configBean" property="mailboxConnection.password"/>&nbsp;<td></tr>	
							<tr><td align="right" width="20%">
									<bean:message key="config.mailbox_connections_connection_mode"/>
								</td>
								<td align="left" width="80%">
									<html:select name="configBean" property="mailboxConnection.connectionMode">
										<html:options name="configBean" property="connectionModes" labelName="configBean" labelProperty="connectionModeLabels"/>
									</html:select>
								</td>
							</tr>
							<tr><td align="right"><html:checkbox name="configBean" property="mailboxConnection.unread"/><html:hidden name="configBean" property="mailboxConnection.unread" value="false"/></td><td align="left"><bean:message key="config.mailbox_connections_unread"/><td></tr>
							<tr><td align="right"><html:checkbox name="configBean" property="mailboxConnection.idle"/><html:hidden name="configBean" property="mailboxConnection.idle" value="false"/></td><td align="left"><bean:message key="config.mailbox_connections_idle"/><td></tr>
							<tr><td align="right"><bean:message key="config.mailbox_connections_actions"/>:</td><td align="left">
								<input type="submit" onClick="return testMailboxConnection()" name="submit.testmailboxconnection" value="<bean:message key="config.mailbox_connections_test"/>"> 
							</td></tr>
							</table>
						 </td>
						 <td align="right">&nbsp;</td>
						</tr>
            		</table>
	     </td>
		</tr>
   		 <tr>
	     	<td align="left" nowrap></td>
	     	<td align="left"></td> 
	     	<td >&nbsp;</td>
	  	</tr>
	  </table>
</div>


<div id="general" class="tabcontent">
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
	          <td><bean:message key="config.gen_archive_settings"/></td>
	       </tr>
	       
	       <tr> 
	          <td  align="right" valign="top" nowrap></td>
	          <td>&nbsp;</td>
	          <td align="center">
	           	  <table width="100%" border="0" cellspacing="2" cellpadding="0"> 
	        		
					<tr> 
	          			<td  align="right" valign="top" nowrap></td>
	          			<td>&nbsp;</td>
	          			<td align="center">
	           	  			<table width="100%" border="0" cellspacing="2" cellpadding="0">
	              				<tr> 
	                				<td width="4%"></td>
	                				<td width="91%">&nbsp;<bean:message key="config.gen_max_message_size"/>&nbsp;<html:text name="configBean" property="maxMessageSize" size="4"/>&nbsp;<bean:message key="config.gen_max_message_size_suffix"/></td>
	              				</tr>
	            			</table>	 
	          			</td>
	         		</tr>
	         		<tr> 
	          			<td  align="right" valign="top" nowrap></td>
	          			<td>&nbsp;</td>
	          			<td align="center">
	           	  			<table width="100%" border="0" cellspacing="2" cellpadding="0">
	              				<tr> 
	                				<td width="4%"></td>
	                				<td width="91%">&nbsp;<bean:message key="config.archive_threads"/>&nbsp;<html:text name="configBean" property="archiveThreads" size="4"/></td>
	              				</tr>
	            			</table>	 
	          			</td>
	         		</tr>
	         		<tr> 
	          			<td  align="right" valign="top" nowrap></td>
	          			<td>&nbsp;</td>
	          			<td align="center">
	           	  			<table width="100%" border="0" cellspacing="2" cellpadding="0">
	              				<tr> 
	                				<td width="4%"><html:checkbox name="configBean" property="diskSpaceChecking"/><html:hidden name="configBean" property="diskSpaceChecking" value="no"/></td>
	                				<td width="91%">&nbsp;<bean:message key="config.gen_disk_space_checking"/>&nbsp;</td>
	              				</tr>
	            			</table>	 
	          			</td>
	         		</tr>
	         		<tr> 
	          			<td  align="right" valign="top" nowrap></td>
	          			<td>&nbsp;</td>
	          			<td align="center">
	           	  			<table width="100%" border="0" cellspacing="2" cellpadding="0">
	              				<tr> 
	                				<td width="4%"></td>
	                				<td width="91%">&nbsp;<input type="submit" name="submit.recoveremails" value="<bean:message key="config.button.recover_emails"/>" onClick="return recover();">&nbsp;<bean:message key="config.recover_emails"/></td>
	              				</tr>
	            			</table>	 
	          			</td>
	         		</tr>
	         		
	         		<tr> 
	          			<td  align="right" valign="top" nowrap></td>
	          			<td>&nbsp;</td>
	          			<td align="center">
	           	  			<table width="100%" border="0" cellspacing="2" cellpadding="0">
	              				<tr> 
	                				<td width="4%"><html:checkbox name="configBean" property="processMalformedMessages"/><html:hidden name="configBean" property="processMalformedMessages" value="no"/></td>
	                				<td width="91%">&nbsp;<bean:message key="config.gen_process_malformed_messages"/>&nbsp;</td>
	              				</tr>
	            			</table>	 
	          			</td>
	         		</tr>
	         		
	         	  </table>
	          </td>
	        </tr>
	         
	         <tr>
	          <td  align="right" valign="top" nowrap></td>
	          <td>&nbsp;</td> 
	          <td><hr/><bean:message key="config.gen_index_settings"/></td>
	         </tr>
	        
	         <tr> 
	          <td  align="right" valign="top" nowrap></td>
	          <td>&nbsp;</td>
	          <td align="center">
	           	  <table width="100%" border="0" cellspacing="2" cellpadding="0">
					<tr> 
	         		 <td  align="right" valign="top" nowrap></td>
	         		 <td>&nbsp;</td>
	          		 <td align="center">
	           		  <table width="100%" border="0" cellspacing="2" cellpadding="0">
	              		
	              		<tr> 
	                		<td width="4%"><html:checkbox name="configBean" property="indexMessageBody"/><html:hidden name="configBean" property="indexMessageBody" value="no"/></td>
	                		<td width="91%"><bean:message key="config.gen_index_message_body"/></td>
	              		</tr>
	              		<tr> 
	               			<td><html:checkbox name="configBean" property="indexAttachments"/><html:hidden name="configBean" property="indexAttachments" value="no"/></td>
	                		<td><bean:message key="config.gen_index_attachments"/></td>
	              		</tr>
	              		<tr> 
	                		<td><html:checkbox name="configBean" property="indexLanguageDetection"/><html:hidden name="configBean" property="indexLanguageDetection" value="no"/></td>
	                		<td><bean:message key="config.gen_detect_index_language"/></td>
	              		</tr>
	              		<tr> 
	                		<td></td>
	                		<td><bean:message key="config.gen_default_index_language"/>:
	                									<html:select name="configBean" property="indexLanguage">
				  											<html:options name="configBean" property="indexLanguages" labelName="configBean" labelProperty="indexLanguageLabels" />
														 </html:select>
	                		</td>
	                	</tr>
	                
	                  </table>	 
	          		</td>
	         	   </tr>
				  
	        	   </table>
	          </td>
	        </tr>
	        
	         <tr>
	          <td  align="right" valign="top" nowrap></td>
	          <td>&nbsp;</td> 
	          <td><hr/><bean:message key="config.gen_search_settings"/></td>
	        </tr>
	        
	        <tr> 
	          <td  align="right" valign="top" nowrap></td>
	          <td>&nbsp;</td>
	          <td align="center">
	           	  <table width="100%" border="0" cellspacing="2" cellpadding="0">
	              <tr> 
	                <td width="15%" align="right"><bean:message key="searchresults.max_results"/></td>
	                <td width="85%">
	                		<html:select name="configBean" property="maxSearchResults">
				  				<html:options name="configBean" property="maxSearchResultsList" labelName="configBean" labelProperty="maxSearchResultsLabels" />
						  	</html:select>
					</td>
	              </tr>
	              <tr> 
	                <td width="15%" align="right"><bean:message key="config.gen_search_initial_sort_order"/></td>
	                <td width="85%">
	                		<html:select name="configBean" property="initialSortOrder">
				  				<html:options name="configBean" property="initialSortOrders" labelName="configBean" labelProperty="initialSortOrderLabels" />
						  	</html:select>
					</td>
	              </tr>
	              <tr>
	                <td width="15%" align="right"><bean:message key="config.gen_search_initial_sort_field"/></td>
	                <td width="85%">
	                		<html:select name="configBean" property="initialSortField">
				  				<html:options name="configBean" property="initialSortFields" labelName="configBean" labelProperty="initialSortFieldLabels" />
						  	</html:select>
					</td>
	              </tr>
	              <tr>
	                <td width="15%" align="right"><bean:message key="config.gen_search_initial_date_type"/></td>
	                <td width="85%">
	                		<html:select name="configBean" property="initialDateType">
				  				<html:options name="configBean" property="initialDateTypes" labelName="configBean" labelProperty="initialDateTypeLabels" />
						  	</html:select>
					</td>
	              </tr>
	              <tr>
	                <td width="15%" align="right"><bean:message key="config.gen_search_open_index"/></td>
	                <td width="85%">
	                		<html:select name="configBean" property="openIndex">
				  				<html:options name="configBean" property="openIndexes" labelName="configBean" labelProperty="openIndexLabels" />
						  	</html:select>
					</td>
	              </tr>
	              
	              
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
	   <td >&nbsp;</td>
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
	        <tr><td colspan="3">&nbsp;</td></tr>
 			 <tr>   		
	          <td align="right"><bean:message key="config.log_level_description"/></td>
	           <td align="left">	
	          		<html:select name="configBean" property="debugLoggingLevel"> 
	                      <html:options name="configBean" property="debugLoggingLevels" labelName="configBean" labelProperty="debugLoggingLevelLabels" /> 
	                </html:select>
	          </td> 
	          <td >&nbsp;</td>   
 			</tr>  
 			<tr>
 			 <td align="right"></td>
 			 <td align="right">
 			 	<table class="tableborder" width="100%" border="0" cellpadding="0" cellspacing="0">
	 			 	<tr><td>
	 			 	<table class="tablehead" width="100%" border="0" cellpadding="0" cellspacing="0">
	 			  		<tr >
	 			  			<td nowrap width="35%" align="center" class="columnspacing">&nbsp;<bean:message key="config.log_filename"/></td>
	 			  			<td nowrap width="25%" align="center" class="columnspacing">&nbsp;<bean:message key="config.log_lastmodified"/></td>
	 			  			<td nowrap width="10%" align="center" class="columnspacing">&nbsp;<bean:message key="config.log_size"/></td>
	 			  			<td nowrap width="39%" align="center">&nbsp;<bean:message key="config.log_actions"/></td>
	 			  		</tr>
	 			  	</table>
	 			  	<div style="height:500px;overflow:auto;">
	 			  	<table width="100%" border="0" cellpadding="0" cellspacing="0">
	 			  		
	 			  		<logic:iterate id="logFiles" name="configBean" property="logFiles" indexId="logFileIndex">
	 			  		<tr>
	 			 				<td nowrap width="35%" align="left">&nbsp;<c:out value="${logFiles.name}"/></td>
	            				<td nowrap width="25%" align="left">&nbsp;<c:out value="${logFiles.modified}"/></td>
	            				<td nowrap width="10%" align="left">&nbsp;<c:out value="${logFiles.size}"/></td>
	            				<td nowrap width="39%" align="left" >&nbsp;
	            				 <input type="submit" name="submit" onclick="viewLog('${logFiles.name}');return false;" value="<bean:message key="config.log_view"/>">&nbsp;
	            				 <input type="submit" name="submit.downloadlog.${logFiles.name}" value="<bean:message key="config.log_download"/>">
	            				 <input type="submit" name="submit.deletelog.${logFiles.name}" value="<bean:message key="config.log_delete"/>" onClick="return confirmDelete()">
	            				</td>
	            		</tr>
	            		</logic:iterate>
	 			 	</table>
 			 		</div>
 			 	</td></tr></table>
 			 </td> 
 			 <td >&nbsp;</td>	
 			</tr>        
   			</table>
   		</td>
   		 <td align="right" >&nbsp;</td>
   		<tr><td colspan="3">&nbsp;</td></tr>
   	</table>
</div>


<table class="pageend" width="100%" border="0" cellpadding="0" cellspacing="0">
  <tr> 
    <td width="10%">&nbsp;</td>
    <td align="left"><input type="submit" name="submit.save" value="<bean:message key="config.button.save"/>">&nbsp;<input type="submit" name="submit.cancel" value="<bean:message key="config.button.cancel"/>"></td>
  </tr>
</table>

<%@include file="../common/bottom.jsp"%>

</DIV>
<script type="text/javascript">

var configtabs=new ddtabcontent("configtabs") //enter ID of Tab Container
configtabs.setpersist(true) //toogle persistence of the tabs' state
configtabs.setselectedClassTarget("link") //"link" or "linkparent"
configtabs.init()

</script>
</html:form>
</body>
</html>
 --%>
 
 <%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="/WEB-INF/tld/c.tld" %>
<%@ taglib prefix="fmt" uri="/WEB-INF/tld/fmt.tld" %>
<%@ taglib prefix="fn" uri="/WEB-INF/tld/fn.tld" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="description" content="">
<meta name="author" content="">
<meta http-equiv="expires" content="0">
<meta http-equiv="no-cache"> 
<meta http-equiv="Cache-Control" content="no-cache">
<link rel="icon" href="./favicon.ico">
<title><bean:message key="config.title"/></title>
<link rel="stylesheet" type="text/css" href="css/bootstrap.min.css">
<link rel="stylesheet" type="text/css"  href="css/mav.css">
<link rel="stylesheet" type="text/css" href="common/mailarchiva.css">
<script type="text/javascript" src="js/jquery.min.js"></script>
<script type="text/javascript" src="js/bootstrap.js"></script>
<script type="text/javascript" src="common/tabcontent.js"></script>
<!--[if lt IE 9]>
<script type="text/javascript" src="./js/html5shiv.min.js"></script>
<script type="text/javascript" src="./js/respond.min.js"></script>
<![endif]-->
<script type="text/javascript">
    function show(id) {
        obj = document.getElementById(id);
        obj.style.display = "";
    }

    function hide(id) {
        obj = document.getElementById(id);
        obj.style.display = "none";
    }

    function indexAllVolumes(){
        var answer = confirm ("<bean:message key="config.confirm_delete_all_index"/>");
        if (answer)
         window.open('indexstatus.do','index','width=400,height=200');
        return answer;
    }

    function indexVolume(){ 
        var answer = confirm ("<bean:message key="config.confirm_delete_index"/>");
        if (answer)
          window.open('indexstatus.do','index','width=400,height=200');
        return answer;
    }

    function viewLog(logfile) {
        window.open('viewlog.do?logFile='+logfile,'log','width=800,height=600');
    }

    function testPassword(passwd) {
        strVerdict = getPasswordStrengthVerdict(passwd);
        document.getElementById("passwordtext").innerHTML = "(<bean:message key="config.password_strength"/> " + strVerdict + ")";
    }

    function testPassword2(passwd) {
        strVerdict = getPasswordStrengthVerdict(passwd);
        document.getElementById("passwordtext2").innerHTML = "(<bean:message key="config.password_strength"/> " + strVerdict + ")";
    }

    function getPasswordStrengthVerdict(passwd){
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
        
        if(intScore < 16){
           strVerdict = "<bean:message key="config.password_strength_very_weak"/>"
        }else if (intScore > 15 && intScore < 25){
           strVerdict = "<bean:message key="config.password_strength_weak"/>"
        }else if (intScore > 24 && intScore < 35){
           strVerdict = "<bean:message key="config.password_strength_mediocre"/>"
        }else if (intScore > 34 && intScore < 45){
           strVerdict = "<bean:message key="config.password_strength_strong"/>"
        }else{
           strVerdict = "<bean:message key="config.password_strength_stronger"/>"
        }
    
        return strVerdict;
    }

    function recover() {
        window.open('recover.do','send','width=600,height=400');
        return true;
    }

    function OnChange(dropdown){
        var myindex  = dropdown.selectedIndex
        var SelValue = dropdown.options[myindex].value
        
        if (SelValue=="activedirectory") {
            // Mod Start HyunSeok.Shin 2013-05-14
            // Active Directory사용 안함
            $('#authMethod').val("basic");
            alert('Active Directory는 지원하지 않습니다.');
            //show('activedirectory');
            //hide('basic');
            return false;
            // Mod End HyunSeok.Shin 2013-05-14
        } else if (SelValue=="basic") {
            show('basic');
            hide('activedirectory');
        }
        
        return true;
    }

    function changePort(selectedindex) {
        portElem = document.getElementById('port');
        sslPortElem = document.getElementById('sslport');   
        if (selectedindex==0) {
            portElem.value = 110;
            sslPortElem.value = 995;
        } else {
            username.disabled = true;
        }
    }

    function testMailboxConnection() {
        window.open('testmailboxstatus.do','send','width=600,height=400');
        return true;
    }

    function lookup(index) {
        var attribute = document.getElementsByName("ADRoleMaps["+index+"].attribute")[0].value; 
        window.open("lookuprolecriterionform.do?lookupIndex="+index,'mywindow','width=600,height=250')
    }
    
    window.onload = function(e) {
        OnChange(document.getElementById('authMethod'));
    }

    
</script>
</head>
<body onLoad="OnChange(document.getElementById('authMethod'));">
<html:form action="/configure" method="POST" styleId="configure" autocomplete="false">
<div class="container">
    <%@include file="../common/menu.jsp"%>
    <logic:present name="errors">
    <table class="table table-condensed">
        <tr> 
            <td width="5%" >&nbsp;</td>
            <td width="95%" align="left">
                <strong><bean:message key="config.save_failed"/></strong>
                <logic:iterate id="error" name="errors">
                    <br>&nbsp;*&nbsp;<bean:write name="error"/>
                </logic:iterate>
            </td>
            <td >&nbsp;</td>
        </tr>
    </table>
    </logic:present>

    <logic:present name="message">
    <table class="table table-condensed">
        <tr> 
            <td width="5%" >&nbsp;</td>
            <td width="95%" align="left"><c:out value="${message}"/></td>
            <td >&nbsp;</td>
        </tr>
    </table>
    </logic:present>
    
    <div class="row">
        <div class="col-md-2">
            <div id="configtabs" class="list-group shadetabs">
               <a href="#" rel="domains" id="left_domains" class="list-group-item active"><span class="glyphicon glyphicon-tag" style="margin-right:10px"></span><bean:message key="config.domains"/></a>
               <a href="#" rel="volumes" id="left_volumes" class="list-group-item"><span class="glyphicon glyphicon-hdd" style="margin-right:10px"></span><bean:message key="config.volumes"/></a>
               <a href="#" rel="authentication" id="left_members" class="list-group-item"><span class="glyphicon glyphicon-log-in" style="margin-right:10px"></span><bean:message key="config.login"/></a>
               <a href="#" rel="mailboxes" id="left_accounts" class="list-group-item"><span class="glyphicon glyphicon-log-in" style="margin-right:10px"></span><bean:message key="config.mailbox_connections"/></a>
               <a href="#" rel="archiverules" id="left_rules" class="list-group-item"><span class="glyphicon glyphicon-blackboard" style="margin-right:10px"></span><bean:message key="config.archive_rules"/></a>
               <a href="#" rel="listeners" id="left_listeners" class="list-group-item"><span class="glyphicon glyphicon-import" style="margin-right:10px"></span><bean:message key="config.listeners"/></a>
               <a href="#" rel="general" id="left_indexs" class="list-group-item"><span class="glyphicon glyphicon-list" style="margin-right:10px"></span><bean:message key="config.general"/></a>
               <a href="#" rel="logging" id="left_logs" class="list-group-item"><span class="glyphicon glyphicon-list-alt" style="margin-right:10px"></span><bean:message key="config.log"/></a>
               <a href="#" rel="about" id="left_about" class="list-group-item"><span class="glyphicon glyphicon-copyright-mark" style="margin-right:10px"></span><bean:message key="config.about"/></a>
            </div>
        </div>
        <div class="col-md-10">
            
            <!-- Mod Start Yanghoon.Jung 2017-03-24 -->
            
            <div id="domains" class="tabcontent">
            	<div class="panel panel-default">
                   <div class="panel-heading">
                   <span class="glyphicon glyphicon-tag" style="margin-right:10px"></span>Domains
                   <button type="submit"  name="submit.newdomain" class="btn btn-info btn-xs pull-right"><span class="glyphicon glyphicon-plus" style="margin-right:10px"></span>Add</button>
                   </div>
                 <div class="panel-body">
                      <table class="table table-bordered table-hover">
                      <logic:iterate id="domains" name="configBean" property="domains" indexId="domainIndex">
                         <tr>
                             <th style="">#</th>
                             <th>도메인명</th>
                             <th>관리</th>
                         </tr>
                         <tr>
                             <td class="text-center"> ${domainIndex}</td>
                         <td> 
                              <!--<input type="text" class="form-control"> -->
                               <bean:message key="config.doms_domain_name"/>: 
	                		   <html:text name="domains" indexed="true" size="45" property="name" />
                         </td>
                         <td class="text-center">
                        	<button type="submit" name="submit.deletedomain.${domainIndex}"  class="btn btn-info"><span class="glyphicon glyphicon-trash" style="margin-right:10px"></span>Delete</button>
                    	</td>
                		</tr>
            		 </logic:iterate>   
         			  </table>
        		</div>
     				</div>
 				 </div>
 				 
            <div id="volumes" class="tabcontent">
                <div class="panel panel-default">
                    <div class="panel-heading"><span class="glyphicon glyphicon-hdd" style="margin-right:10px"></span>Volumes
                     <button type="submit"  name="submit.newvolume" class="btn btn-info btn-xs pull-right"><span class="glyphicon glyphicon-plus" style="margin-right:4px;"></span>Add (New Volume)</button>
                    </div>
                    <div class="panel-body">
                        <div class="text-center">
                          <div class="form-inline">
                            <%--  <div class="form-group">
                             <c:if test="${configBean.defaultPassPhraseModified==false}">
                               <label for="passPhrase" style="font-weight:500">Email 암호화 패스워드:</label>
                               <input type="password" class="form-control input-sm" id="passPhrase" name="passPhrase">
                               </c:if>
                             </div>
                             <div class="form-group">
                             <c:if test="${configBean.defaultPassPhraseModified==false}">
                               <label for="passPhraseAgain" style="font-weight:500">패스워드(Again)</label>
                               <input type="password" class="form-control input-sm" id="passPhraseAgain" name="passPhraseAgain">
                               </c:if>
                             </div> --%>
                             <c:if test="${configBean.defaultPassPhraseModified==false}">
	            			Email 암호화 패스워드:<html:password name="configBean" size="25" redisplay="false" property="passPhrase" onkeyup="testPassword(document.forms.configBean.passPhrase.value)" />
	            			&nbsp;패스워드(Again):&nbsp;<html:password name="configBean" size="25" redisplay="false" readonly="false" property="passPhraseAgain" />
	            		</c:if>
	            		<c:if test="${configBean.defaultPassPhraseModified==true}">
	            			<html:password name="configBean" size="25" redisplay="false" readonly="true" property="passPhrase" styleClass="form-control"/>
	            		</c:if>
                             <div id="passwordtext" >(기본 패스워드 설정됨, 패스워드를 변경하세요!)</div>
                        </div>
                      </div>
                      <div class="table-responsive" style="margin-bottom:10px; border-bottom:1px solid #eeeeee;">
                    <c:if test="${fn:length(configBean.volumes)>0}">
                        <table class="table table-condensed table-bordered">
                            <tr style="background-color:#eeeeee">
                                <th>볼륨</th>
                                <th>상태</th>
                                <th>생성됨</th>
                                <th>닫힘</th>
                                <th>볼륨 경로</th>
                                <th>Max Size(MB)</th>
                                <th>남은용량</th>
                                <th>문서갯수</th>
                                <th>인덱스 경로</th>
                                <th>남은용량</th>
                                <th>삭제</th>
                            </tr>
                             <logic:iterate id="volumes" name="configBean" property="volumes" indexId="volumeIndex">
                            <tr>                           
                                <td><c:out value="${volumeIndex}"/></td>
                                <td><c:out value="${volumes.status}"/></td>
                                <td><c:if test="${volumes.created!=''}"><c:out value="${volumes.created}"/>&nbsp;</c:if></td>
                                <td><c:if test="${volumes.closed!=''}"><c:out value="${volumes.closed}"/>&nbsp;</c:if></td>
                                
                                <td>
                                <c:if test="${volumes.statusID>1}">
                                <html:text name="volumes" indexed="true" size="15" property="path" styleClass="form-control input-sm"/>&nbsp;
                                </c:if>
                                <c:if test="${volumes.statusID<2}">
                                  <html:text name="volumes" indexed="true" readonly="true" size="15" property="path" styleClass="form-control input-sm"  />
                                </c:if>
                                </td> 
                                                               
                                <td>
                                <html:text name="volumes" indexed="true" size="6" property="maxSize" styleClass="form-control input-sm"/>&nbsp;
                               </td>
                               
                               <td>
                               <c:if test="${volumes.freeArchiveSpace!=''}">
               		           <c:out value="${volumes.freeArchiveSpace}"/>&nbsp;
                	           </c:if>                	           
                                </td>
                                
                                <td><c:out value="${volumes.totalMessageCount}"/>&nbsp;
                                </td>
                                
                                <td>
                                 <c:if test="${volumes.statusID>1}">
                		         <html:text name="volumes" indexed="true" size="15" property="indexPath" styleClass="form-control input-sm" />&nbsp; 
                	             </c:if>
                	             <c:if test="${volumes.statusID<2}">
                		         <html:text name="volumes" indexed="true" readonly="true" size="20" property="indexPath" styleClass="form-control input-sm" />&nbsp; 
               		             </c:if>
                                </td>
                                
                                <td>
                                <c:if test="${volumes.freeIndexSpace!=''}">
                		        <c:out value="${volumes.freeIndexSpace}"/>&nbsp;
                	            </c:if>
                                </td>
                                
                                <td>
                               <%--  <c:if test="${volumes.statusID!=1}">
                                <button type="submit" name="submit.deletevolume.${volumeIndex}"class="btn btn-info btn-xs">Delete</button>
                                </c:if> --%>
                                
                                <c:if test="${volumes.statusID==0 || volumes.statusID==1}">
                	<button type="submit" onClick="return indexVolume()" name="submit.indexvolume.${volumeIndex}"  class="btn btn-info btn-xs"> <bean:message key="config.volume_re_index"/> </button> 
               		 </c:if>
                  <c:if test="${volumes.statusID==0}">
                	 	<button type="submit" name="submit.unmountvolume.${volumeIndex}" class="btn btn-info btn-xs"> <bean:message key="config.volume_unmount"/></button>
                  </c:if>
                   <c:if test="${volumes.statusID==4}">
                	 	<button type="submit" name="submit.closevolume.${volumeIndex}" class="btn btn-info btn-xs"> <bean:message key="config.volume_mount"/> </button>
                  </c:if>
                  <c:if test="${volumes.statusID!=1}">
                  	<button type="submit" name="submit.deletevolume.${volumeIndex}" class="btn btn-info btn-xs"> <bean:message key="config.volume_delete"/> </button>
                  </c:if>
                  <c:if test="${volumes.statusID==1}">
                  	<button type="submit" name="submit.closevolume.${volumeIndex}" class="btn btn-info btn-xs"  onclick="return confirm ('<bean:message key="config.confirm_close_volume"/>');"> <bean:message key="config.volume_close"/></button>
                  </c:if>
                  <c:if test="${volumes.statusID==2}">
                  <button type="submit" name="submit.prioritizevolume.${volumeIndex}" class="btn btn-info btn-xs"> <bean:message key="config.volume_up"/></button>
                  <button type="submit" name="submit.deprioritizevolume.${volumeIndex}" class="btn btn-info btn-xs"> <bean:message key="config.volume_down"/></button>
                   </c:if>
                   
                                
                           </td>
                        </tr>
                       </logic:iterate>
                    </table>
                   </c:if>
               </div>
             </div>
           </div>
          </div>  
          
            <div id="authentication" class="tabcontent">
                <div class="panel panel-default">
                    <div class="panel-heading"><span class="glyphicon glyphicon-log-in" style="margin-right:10px"></span>Members</div>
                    <div class="panel-body">
                        <table class="table table-condensed">
                        <c:if test="${configBean.allowMasterPasswordEntry==true}">
                            <tr>
                                <c:if test="${configBean.defaultMasterLoginPasswordModified==false}">
                                <td style="text-align:right">Admin 패스워드:</td>
                                <td>
                                <html:password name="configBean" size="25" redisplay="false" property="masterLoginPassword" onkeyup="testPassword2(document.forms.configBean.masterLoginPassword.value)"  styleClass="form-control" />
                                </td>
                                <td style="text-align:right">패스워드확인:</td>
                                <td>
                                <html:password name="configBean" size="25" redisplay="false" readonly="false" property="masterLoginPasswordAgain" styleClass="form-control" />
                                </td>
                                </c:if>
                                <c:if test="${configBean.defaultMasterLoginPasswordModified==true}">
                                <td style="text-align:right">Admin 패스워드:</td>
                                <td>
                                <html:password name="configBean" size="25" redisplay="false" property="masterLoginPassword" onkeyup="testPassword2(document.forms.configBean.masterLoginPassword.value)"  styleClass="form-control" />
                                </td>
                                <td style="text-align:right">패스워드확인:</td>
                                <td>
                                <html:password name="configBean" size="25" redisplay="false" readonly="false" property="masterLoginPasswordAgain" styleClass="form-control" />
                                </td>
                                </c:if>
                            </tr>
                            <tr>
                                <td style="text-align:right">기본 설정 도메인:</td>
                                <td>
                                <html:text name="configBean" property="defaultLoginDomain" size="25" styleClass="form-control" /></td>
                                <td colspan="2" style="text-align:left;"> (e.g. company.com)</td>
                            </tr>
                       </c:if>     
                            <tr>
                                <td style="text-align:right">콘솔 로그인 메소드:
                               <html:select styleId="authMethod" name="configBean"  property="authMethod" styleClass="form-control input-sm"  onchange='OnChange(this);' style="width:15%; display:inline;">
                              <html:options name="configBean" property="authMethods" labelName="configBean" labelProperty="authMethodLabels" />
					           </html:select>
                                </td>
                             </div>   
                              </div>
                            </tr>
                            <tr> 
                                <td style="text-align:right" colspan="3"> Assign Roles to User/s:
                                <input type="submit" name="submit.newbasicrolemap" class="btn btn-default btn-sm" style="margin-bottom:4px;" >                                  
                                    <!-- 할당시 아래 추가 -->
                               </td>
           
                       <div id="basic">
                            <table width="100%" border="0" cellpadding="0" cellspacing="0"> 
                               <tr>
                               <td width="10%" align="right" nowrap></td> 
                                <td width="80%">   
                                  <table width="100%" border="0" cellspacing="2" cellpadding="0">
                                  
                                      <tr><td width="20%" align="right" nowrap></td>
                                           <td colspan="2">
                                           
                                      	</td>
                                      	<td align="right" nowrap>&nbsp;</td>
                                      </tr>
                                  	
	                                  <logic:iterate id="basicRoleMaps" name="configBean" property="basicRoleMaps" indexId="userRoleAssignmentIndex2">
	                                    <tr> 
	                                      <td nowrap align="right"> ${userRoleAssignmentIndex2}:</td>
	                                      <td align="right" nowrap>롤할당: </td>
	                                      <td>
	                                      	<table  border="0" cellspacing="2" cellpadding="0">
	                                    <tr><td>
		                      	            <html:select indexed="true" name="basicRoleMaps" property="role" styleClass="form-control input-sm">
		                      		  			<html:options name="configBean" property="roleValues" labelName="configBean" labelProperty="roleValues" />
		                      			  	</html:select>
		                      	            </td>
	                                    	<td width="1%">&nbsp;</td>
	                                   		<td align="right" nowrap>이메일주소:</td>
	                                    	<td><html:text indexed="true" name="basicRoleMaps" property="username" size="25" styleClass="form-control input-sm"/></td>
	                                    	<td width="1%">&nbsp;</td>
	                                    		<td align="right" nowrap>패스워드:</td>
	                                    	<td><html:password indexed="true" name="basicRoleMaps" size="25" redisplay="true"   styleClass="form-control input-sm" property="loginPassword"/></td>
	                                   		<td><input type="submit" name="submit.deletebasicrolemap.${userRoleAssignmentIndex2}"class="btn btn-default btn-sm" value="delete"></td>
	                                   	</tr></table>
	                              	</td>
	                              	<td align="right" nowrap>&nbsp;</td>
	                              </tr>
	                            </logic:iterate>
                             </table>
                            </td>
                           <td>&nbsp;</td>
                        </tr>
	                   </table>
                    </tr>
            </table>
                    </div>
                </div>
            </div>
            
            <div id="mailboxes" class="tabcontent">
                <div class="panel panel-default">
                    <div class="panel-heading"><span class="glyphicon glyphicon-log-in" style="margin-right:10px"></span>Journal Accounts</div>
                    <div class="panel-body">
                        <table class="table table-condensed">
                            <tr>
                                <td style="text-align:right">Polling Wait Interval (Secs):</td>
                                <td colspan="2"><html:text name="configBean" property="pollingIntervalSecs" styleClass="form-control" /></td>
                            </tr>
                            <tr>
                                <td style="text-align:right">Maximum Messages To Process:</td>
                                <td><html:text name="configBean" property="mailboxMaxMessages" styleClass="form-control" /></td>
                                <td colspan="2" style="text-align:left;">&nbsp;(when polling)</td>
                            </tr>
                            <tr>
                                <td colspan="2">Connection :</td>
                            </tr>
                            <tr>
                                <td style="text-align:right">Enabled:</td>
                                <td colspan="2">
                                    <html:checkbox name="configBean" property="mailboxConnection.enabled"/>
                                    <html:hidden name="configBean" property="mailboxConnection.enabled" value="false"/>
                                </td>
                            </tr>
                            <tr>
                                <td style="text-align:right">Protocol:</td>
                                <td colspan="2">
                                    <html:select name="configBean" property="mailboxConnection.protocol"  onchange="changePort(this.options.selectedIndex);" styleClass="form-control input-sm">
                                        <html:options name="configBean" property="protocols" labelName="configBean" labelProperty="protocolLabels"/>
                                    </html:select>
                                </td>
                            </tr>
                            <tr>
                                <td style="text-align:right">Server:</td>
                                <td colspan="2"><html:text name="configBean" property="mailboxConnection.serverName"  styleClass="form-control" /></td>
                            </tr>
                            <tr>
                                <td style="text-align:right">Port:</td>
                                <td colspan="2"><html:text styleId="port" size="5" name="configBean" property="mailboxConnection.port" styleClass="form-control" /></td>
                            </tr>
                            <tr>
                                <td style="text-align:right">SSL Port:</td>
                                <td colspan="2"><html:text styleId="sslport" name="configBean" size="5" property="mailboxConnection.SSLPort" styleClass="form-control" /></td>
                            </tr>
                            <tr>
                                <td style="text-align:right">Username:</td>
                                <td colspan="2"><html:text size="25" name="configBean" property="mailboxConnection.username" styleClass="form-control" /></td>
                            </tr>
                            <tr>
                                <td style="text-align:right">Password:</td>
                                <td colspan="2"><html:password size="25" name="configBean" property="mailboxConnection.password" styleClass="form-control" /></td>
                            </tr>
                            <tr>
                                <td style="text-align:right">Connection Mode:</td>
                                <td colspan="2">
                                    <html:select name="configBean" property="mailboxConnection.connectionMode" styleClass="form-control input-sm">
                                        <html:options name="configBean" property="connectionModes" labelName="configBean" labelProperty="connectionModeLabels"/>
                                    </html:select>
                                </td>
                            </tr>
                            <tr>
                                <td style="text-align:right">
                                    <html:checkbox name="configBean" property="mailboxConnection.unread"/>
                                    <html:hidden name="configBean" property="mailboxConnection.unread" />
                                </td>
                                <td colspan="2">During IMAP retrieval, process unread messages only (enable recommended)</td>
                            </tr>
                            <tr>
                                <td style="text-align:right">
                                    <html:checkbox name="configBean" property="mailboxConnection.idle"/>
                                    <html:hidden name="configBean" property="mailboxConnection.idle"/>
                                </td>
                                <td colspan="2">Listen for message arrival notifications from server (IMAP Idle)</td>
                            </tr>
                            <tr>
                                <td style="text-align:right">Actions:</td>
                                <td colspan="2">
                                    <button type="submit" onClick="return testMailboxConnection()" name="submit.testmailboxconnection" class="btn btn-default btn-sm" style="margin-bottom:4px;">Test Journal Account Connection</button>
                                </td>
                            </tr>
                        </table>
                    </div>
                </div>
            </div>
            
            <!-- Mod Start Hana.Jung 2017-03-24 -->
            
             <div id="archiverules" class="tabcontent">
                <div class="panel panel-default">
                    <div class="panel-heading"><span class="glyphicon glyphicon-blackboard" style="margin-right:10px"></span>Archive Rules</div>
                    <div class="panel-body">
                        <div>
                               <div class="form-group" style="font-weight: 500; ">   
                                    <html:checkbox name="configBean" property="archiveInbound" /><html:hidden name="configBean" property="archiveInbound" value="false"/>
                                    <bean:message key="config.sec_rules_inbound" /><br>
                                    <html:checkbox name="configBean" property="archiveOutbound"/><html:hidden name="configBean" property="archiveOutbound" value="false"/>
                                    <bean:message key="config.sec_rules_outbound"/><br>
                                    <html:checkbox name="configBean" property="archiveInternal"/><html:hidden name="configBean" property="archiveInternal" value="false"/>
                                    <bean:message key="config.sec_rules_internal"/><br>                          
                                </div>
                        </div>
                    
                         <br>
                        <div style="margin-bottom:4px;"> <button type="submit" class="btn btn-default btn-sm" name="submit.newarchiverule">새로운 아카이브 룰 추가 </button></div>
                        <div>
                             <table class="table table-condensed">
                                <logic:iterate id="archiveRules" name="configBean" property="archiveRules" indexId="archiveFilterIndex">
                                    <tr>
                                        <td style="width:16%; text-align:right"><bean:message key="config.sec_rules_rule"/> ${archiveFilterIndex}:</td>
                                        <td>
                                            <table class="table table-condensed">
                                                <tr>
                                                    <td style="width:8%; text-align:right; border:none;"><bean:message key="config.sec_rules_operator"/></td>
                                                    <td style="border:none;">
                                                        <html:select indexed="true" name="archiveRules" property="operator" styleClass="form-control input-sm"  style="width:15%; display:inline;">
                                                            <html:options name="configBean" property="ruleOperatorFields" labelName="configBean" labelProperty="ruleOperatorLabels" />
                                                        </html:select>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td style="width:8%; text-align:right; border:none;">&nbsp;</td>
                                                    <td style="border:none;">
                                                            <logic:iterate id="archiveClauses" name="archiveRules" property="archiveClauses" indexId="archiveClauseIndex">
                                                                <div class="form-group" style="margin-bottom:4px;">                                                        
                                                                    <bean:message key="config.sec_rules_clauses"/> ${archiveClauseIndex}:
                                                                        <html:select name="configBean" property="archiveRules[${archiveFilterIndex}].archiveClauses[${archiveClauseIndex}].field" styleClass="form-control input-sm"  style="width:15%; display:inline;">
                                                                            <html:options name="configBean" property="ruleFields" labelName="configBean" labelProperty="ruleFieldLabels" />
                                                                        </html:select>                                                                  
                                                                        <html:select name="configBean" property="archiveRules[${archiveFilterIndex}].archiveClauses[${archiveClauseIndex}].condition" styleClass="form-control input-sm" style="width:15%; display:inline;">
                                                                            <html:options name="configBean" property="ruleClauseConditionFields" labelName="configBean" labelProperty="ruleClauseConditionLabels" />
                                                                        </html:select>
                                                                   <html:text name="configBean" property="archiveRules[${archiveFilterIndex}].archiveClauses[${archiveClauseIndex}].value" styleClass="form-control input-sm" style="font-size:0.9em; width:25%; display:inline;"/>                                                            
                                                                
                                                                    <input type="submit" name="submit.newarchiveruleclause.${archiveFilterIndex}" value="&nbsp;+&nbsp;" class="btn btn-default btn-sm">
                                                                    <input type="submit" name="submit.deletearchiveruleclause.${archiveFilterIndex}.${archiveClauseIndex}" value="&nbsp;-&nbsp;" class="btn btn-default btn-sm">
                                                                </div>   
                                                            </logic:iterate> 
                                                    </td>
                                                </tr>
                                                <tr>
                                                  <td style="width:8%; text-align:right; border:none;"><bean:message key="config.sec_rules_behavior"/>:</td>
                                                  <td style="border:none;">
                                                    <html:select indexed="true" name="archiveRules" property="action" styleClass="form-control input-sm" style="width:15%; display:inline;">
                                                        <html:options name="configBean" property="ruleActionFields" labelName="configBean" labelProperty="ruleActionLabels" />
                                                    </html:select>
                                                 </td>
                                              </tr>
                                              <tr>
                                                <td style="width:8%; text-align:right; border:none;">&nbsp;</td>
                                                <td style="border:none;">
                                                    <button type="submit" name="submit.deletearchiverule.${archiveFilterIndex}" class="btn btn-default btn-sm" style="background-color:#efefef; border:2px solid #eeeeee">Delete</button>
                                                    <button type="submit" name="submit.prioritizearchiverule.${archiveFilterIndex}" class="btn btn-default btn-sm" style="background-color:#efefef; border:2px solid #eeeeee">Up</button>
                                                    <button type="submit" name="submit.deprioritizearchiverule.${archiveFilterIndex}" class="btn btn-default btn-sm" style="background-color:#efefef; border:2px solid #eeeeee">Down</button>                                                   
                                                </td>    
                                             </tr>
                                         </table>
                                       </td> 
                                    </tr>
                               </logic:iterate>
                            </table>
                      </div>
                    </div>  
                </div>
            </div> 
            
            <div id="listeners" class="tabcontent">
                <div class="panel panel-default">
                    <div class="panel-heading"><span class="glyphicon glyphicon-import" style="margin-right:10px"></span>Listeners</div>
                    <div class="panel-body">
                        <table class="table table-condensed">
                            <tr>
                                <td style="width:6%; text-align:center">
                                    <html:checkbox name="configBean"  property="agentSMTPEnable"/>
                                    <html:hidden name="configBean" property="agentSMTPEnable" value="false"/>
                                    &nbsp;                                            
                                </td>
                                <td>
                                    <div class="form-inline">
                                        <div class="form-group">
                                            <bean:message key="config.agent_smtp_listen_port_enable"/>
                                        </div>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td style="width:6%; text-align:center">&nbsp;</td>
                                <td>
                                    <table class="table table-condensed">
                                        <tr>
                                            <td style="border:none;">&nbsp;</td>
                                            <td style="border:none;">
                                                    <div class="form-inline">
                                                        <div class="form-group">
                                                            <bean:message key="config.agent_smtp_listen_port"/>:&nbsp;
                                                            <html:text name="configBean" property="agentSMTPPort" styleClass="form-control input-sm"/>
                                                        </div>
                                                        <div class="form-group">
                                                             <bean:message key="config.agent_smtp_listen_ipaddress"/>:&nbsp;
                                                             <html:select name="configBean" property="agentSMTPAddress" styleClass="form-control input-sm">
                                                                 <html:options name="configBean" property="bindIPAddresses"  labelName="configBean" labelProperty="bindIPAddressLabels" />
                                                             </html:select>                                
                                                       </div>
                                                    </div>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td style="width:6%; text-align:center; border:none;">
                                                <html:checkbox name="configBean"  property="agentSMTPAuth"/>
                                                  <html:hidden name="configBean" property="agentSMTPAuth" value="false"/>&nbsp;
                                            </td>
                                            <td style="border:none;">
                                                <div class="form-inline">
                                                    <div class="form-group">
                                                        <bean:message key="config.agent_smtp_authentication"/>
                                                    </div>
                                                </div>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td style="border:none;">&nbsp;</td>
                                            <td style="border:none;">
                                                <div class="form-inline">
                                                    <div class="form-group">
                                                        <bean:message key="config.agent_smtp_username"/>:&nbsp;
                                                        <html:text name="configBean" property="agentSMTPUsername" styleClass="form-control input-sm"/>&nbsp;
                                                    </div>
                                                    <div class="form-group">
                                                        <bean:message key="config.agent_smtp_password"/>:&nbsp;
                                                        <html:password name="configBean" property="agentSMTPPassword" styleClass="form-control input-sm"/>
                                                    </div>
                                                </div>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td style="width:6%; text-align:center; border:none;">
                                                  <html:checkbox name="configBean"  property="agentSMTPTLS"/>
                                                  <html:hidden name="configBean" property="agentSMTPTLS" value="false"/>&nbsp;
                                            </td>
                                            <td style="border:none;">
                                                <div class="form-inline">
                                                    <div class="form-group">
                                                        <bean:message key="config_agent_smtp_tls"/>
                                                    </div>
                                                </div>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                            <tr>
                                <td style="width:6%; text-align:right">
                                     <html:checkbox name="configBean"  property="agentMilterEnable"/>
                                     <html:hidden name="configBean" property="agentMilterEnable" value="false"/>
                                </td>
                                <td>
                                    <div class="form-inline">
                                        <div class="form-group">
                                            <bean:message key="config.agent_milter_listen_port_enable"/>
                                        </div>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td style="width:6%;">&nbsp;</td>
                                <td>
                                    <table class="table table-condensed">
                                        <tr>
                                            <td style="border:none;">
                                                <div class="form-inline">
                                                    <div class="form-group">
                                                        <bean:message key="config.agent_milter_listen_port"/>:&nbsp;
                                                        <html:text name="configBean" property="agentMilterPort" size="5" styleClass="form-control input-sm"/>
                                                    </div>
                                                    <div class="form-group">
                                                        <bean:message key="config.agent_smtp_listen_ipaddress"/>:&nbsp;
                                                          <html:select name="configBean" property="agentMilterAddress" styleClass="form-control input-sm">
                                                              <html:options name="configBean" property="bindIPAddresses" labelName="configBean" labelProperty="bindIPAddressLabels" />
                                                          </html:select>
                                                    </div>
                                                </div>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2">
                                  <table class="table table-condensed">
                                        <tr>
                                            <td align="left" width="2%">&nbsp;</td>
                                            <td align="left">
                                                <bean:message key="config.agent_allow_incoming_connections"/>&nbsp;
                                                <button type="submit" name="submit.newagentipaddress" class="btn btn-default btn-sm">New IP Adress</button>
                                            </td>
                                        </tr>
                                        <logic:iterate id="agentIPAddress" name="configBean" property="agentIPAddresses" indexId="agentIPAddressIndex">
                                        <tr>
                                            <td align="left"  width="2%">&nbsp;</td>
                                            <td align="left">
                                                <div class="form-inline">
                                                    <div class="form-group">
                                                        <bean:message key="config.agent_ipaddress"/> ${agentIPAddressIndex}:&nbsp;
                                                        <html:text property="agentIPAddress[${agentIPAddressIndex}]" styleClass="form-control"/>
                                                        <button type="submit" name="submit.deleteagentipaddress.${agentIPAddressIndex}" class="btn btn-default btn-sm">Delete</button>
                                                    </div>
                                                </div>
                                            </td>    
                                        </tr>
                                        </logic:iterate>
                                    </table>
                               </td>
                            </tr>
                        </table>
                    </div> 
                </div>
            </div> 
            
              <!-- Mod Start Seolhwa.Kim 2017-03-24 -->
           
            <div id="general" class="tabcontent">
                <div class="panel panel-default">
                    <div class="panel-heading"><span class="glyphicon glyphicon-list" style="margin-right:10px"></span>Index</div>
                    <div class="panel-body">

			            <!-- 아카이브 설정 -->
			            <div class="panel panel-default">
			                <div class="panel-heading">아카이브 설정</div>
			                <div class="panel-body">
			                    <table class="table table-condensed">
			                        <tr>
			                            <td style="width:10%; text-align:right">&nbsp;</td>
			                            <td>
			                                    <div class="form-group">
			                                        <label for="maxMessageSize" style="font-weight:500;">Do not archive messages larger than : </label>
			                                        <html:text name="configBean" property="maxMessageSize" styleClass="form-control" style="width:15%; display:inline;"/>
			                                        &nbsp;(mb)
  			                                    </div>
			                            </td>
			                        </tr>
			                        <tr>
			                            <td style="width:10%; text-align:right">&nbsp;</td>
			                            <td>
			                                    <div class="form-group">
			                                        <label for="archiveThreads" style="font-weight:500;">Maximum number of archiving threads: </label>
			                                        <html:text name="configBean" property="archiveThreads" styleClass="form-control" style="width:10%; display:inline;"/>                                                
			                                    </div>
			                            </td>
			                        </tr>
			                        
			                        <tr>
			                        
			                            <td style="width:10%; text-align:right">&nbsp;</td>
			                            <td>
			                            
			                            <div class="form-group">
			                                <html:checkbox name="configBean" property="diskSpaceChecking"/>
			                                <html:hidden name="configBean" property="diskSpaceChecking" value="no"/>&nbsp;
			                                <bean:message key="config.gen_disk_space_checking"/>
			                                
			                            </div>
			                            </td>
			                        </tr>
			                        <tr>
			                            <td style="width:10%; text-align:right">&nbsp;</td>
			                            <td>
			                                    <div class="form-group">
			                                        <input type="submit" class="btn btn-default btn-sm" name="submit.recoveremails" value="<bean:message key="config.button.recover_emails"/>" onClick="return recover();">&nbsp;
			                                        <label style="font-weight:500;">Recover all emails in the no archive queue.</label>                                                    
			                                    </div>
			                            </td>
			                        </tr>
			                        <tr>
			                        
			                             <td style="width:10%; text-align:right">&nbsp;</td>
			                             <td>
			                            
			                            <div class="form-group">
			                            
			                                <html:checkbox name="configBean" property="processMalformedMessages"/>
			                                <html:hidden name="configBean" property="processMalformedMessages" value="no"/>
			                                &nbsp;<bean:message key="config.gen_process_malformed_messages"/>
			                            
			                            </div>
			                            
			                            </td>
			                         
			                        </tr>
			                    </table>
			                </div>
			            </div>

                        <!-- 인덱스 설정 -->
                        <div class="panel panel-default">
                            <div class="panel-heading">인덱스 설정</div>
                            <div class="panel-body">
                          <table class="table table-condensed" style ="text-align = "left">
                             <tr>
                             <td style="width:10%; text-align:right">&nbsp;</td>
                                 <td style="text-align:left">
                                 <html:checkbox name="configBean" property="indexMessageBody"/>
                                 <html:hidden name="configBean" property="indexMessageBody" value="no"/>                                 
                                 &nbsp;&nbsp;
                                 <bean:message key="config.gen_index_message_body"/>
                                 </td>
                             </tr>
                             <tr>
                             <td style="width:10%; text-align:right">&nbsp;</td>
                                 <td style="text-align:left">
                                     <html:checkbox name="configBean" property="indexAttachments"/>
                                     <html:hidden name="configBean" property="indexAttachments" value="no"/>
                                     &nbsp;&nbsp;
                                     <bean:message key="config.gen_index_attachments"/>
                                 </td>
                             </tr>
                             <tr>
                             <td style="width:10%; text-align:right">&nbsp;</td>
                             
                                 <td style="text-align:left">
                                     <html:checkbox name="configBean" property="indexLanguageDetection"/>
                                     <html:hidden name="configBean" property="indexLanguageDetection" value="no"/>
                                 	 &nbsp;&nbsp;
                                 	 <bean:message key="config.gen_detect_index_language"/>
                                 </td>
                             </tr>
                             <tr>
                             <td style="width:10%; text-align:right">&nbsp;</td>
                                 <td>
                                     <bean:message key="config.gen_default_index_language"/>:
                                     <html:select name="configBean" property="indexLanguage">
                                         <html:options name="configBean" property="indexLanguages" labelName="configBean" labelProperty="indexLanguageLabels" />
                                     </html:select>
                                 </td>
                             </tr>
                                </table>
                            </div>
                        </div>
                        </div>

                        <!-- 검색 설정 -->
                        <div class="panel panel-default">
                            <div class="panel-heading">검색 설정</div>
                            <div class="panel-body">
                                <table class="table table-condensed">
                                    <tr>
                                        <td style="width:30%; text-align:right">Max Results:</td>
                                        <td>
                                        <html:select name="configBean" property="maxSearchResults" styleClass="form-control input-sm">
                                        <html:options name="configBean" property="maxSearchResultsList" labelName="configBean" labelProperty="maxSearchResultsLabels" />
                                        </html:select>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="width:30%; text-align:right">Initial sort order:</td>
                                        <td>
                                        <html:select name="configBean" property="initialSortOrder" styleClass="form-control input-sm">
                                        <html:options name="configBean" property="initialSortOrders" labelName="configBean" labelProperty="initialSortOrderLabels" />
                                        </html:select>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="width:30%; text-align:right">Initial sort field:</td>
                                        <td>
                                        <html:select name="configBean" property="initialSortField" styleClass="form-control input-sm">
                                        <html:options name="configBean" property="initialSortFields" labelName="configBean" labelProperty="initialSortFieldLabels" />
                                        </html:select>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="width:30%; text-align:right">Default date type:</td>
                                        <td>
                                        
                                        <html:select name="configBean" property="initialDateType" styleClass="form-control input-sm">
                                        <html:options name="configBean" property="initialDateTypes" labelName="configBean" labelProperty="initialDateTypeLabels" />
                                        </html:select>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="width:30%; text-align:right">Read index:</td>
                                        <td>
                                            <html:select name="configBean" property="openIndex" styleClass="form-control input-sm">
                                            <html:options name="configBean" property="openIndexes" labelName="configBean" labelProperty="openIndexLabels" styleClass="form-control input-sm"/>
                                        </html:select>
                                        </td>
                                    </tr>
                                </table>
                            </div>                            
                        </div>
                        <!-- 검색설정 end -->
                    </div>                    
                </div>
            
            
            <div id="logging" class="tabcontent">
                <div class="panel panel-default">
                    <div class="panel-heading"><span class="glyphicon glyphicon-list-alt" style="margin-right:10px"></span>Logs</div>
                    <div class="panel-body">
                        <div style="margin-bottom:10px;">
                            로그 레벨 :
                            <html:select name="configBean" property="debugLoggingLevel"> 
                                <html:options name="configBean" property="debugLoggingLevels" labelName="configBean" labelProperty="debugLoggingLevelLabels" /> 
                            </html:select>
                        </div>                      
                        <div style="overflow:auto;">
                            <table class="table table-condensed">
                                <tr>
                                    <th>File Name</th>
                                    <th>Last Modified</th>
                                    <th>Size</th>
                                    <th>Actions</th>
                                </tr>
                                <logic:iterate id="logFiles" name="configBean" property="logFiles" indexId="logFileIndex">
                                <tr>
                                    <td align="left"><c:out value="${logFiles.name}"/></td>
                                    <td align="left"><c:out value="${logFiles.modified}"/></td>
                                    <td align="left"><c:out value="${logFiles.size}"/></td>
                                    <td align="center">
                                        <input type="submit" class="btn btn-default btn-sm" name="submit" onclick="viewLog('${logFiles.name}');return false;" value="<bean:message key="config.log_view"/>">&nbsp;
                                        <input type="submit" class="btn btn-default btn-sm" name="submit.downloadlog.${logFiles.name}" value="<bean:message key="config.log_download"/>">
                                        <!-- confirmDelete() 함수가 없음. 확인요함 -->
                                        <input type="submit" class="btn btn-default btn-sm" name="submit.deletelog.${logFiles.name}" value="<bean:message key="config.log_delete"/>" onClick="return confirmDelete()">
                                    </td>
                                </tr>
                                </logic:iterate>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
            
            <div id="about" class="tabcontent">
                <div class="panel panel-default">
                    <div class="panel-heading"><span class="glyphicon glyphicon-copyright-mark" style="margin-right:10px"></span>About</div>
                    <div class="panel-body">
                        <img src="images/about1.png" class="img-responsive center-block" alt="mailarchiva about">
                        <hr/>
                        <img src="images/about2.png" class="img-responsive center-block" alt="mailarchiva system">
                    </div>
                </div>
            </div>
            
            <!-- 저장버튼 -->            
            <div id="btn" class="text-center">
                <button type="submit" name="submit.save" class="btn btn-info">Save</button>
                <button type="submit" name="submit.cancel" class="btn btn-info">Cancel</button>
            </div>
            
        </div>
    </div>
</div>


<%@include file="../common/bottom.jsp"%>
<script type="text/javascript">
    var configtabs = new ddtabcontent("configtabs") //enter ID of Tab Container
    configtabs.setpersist(true) //toogle persistence of the tabs' state
    configtabs.setselectedClassTarget("link") //"link" or "linkparent"
    configtabs.init()
</script>
</html:form>
</body>
</html>
 