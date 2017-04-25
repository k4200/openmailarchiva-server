<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="/WEB-INF/tld/c.tld" %>
<%@ taglib prefix="fmt" uri="/WEB-INF/tld/fmt.tld" %>
<%@ taglib prefix="fn" uri="/WEB-INF/tld/fn.tld" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/taglib186.tld" prefix="t" %>
<html lang="ko">
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="description" content="">
<meta name="author" content="">
<link rel="icon" href="./favicon.ico">
<title><bean:message key="searchresults.title"/></title>
<% int nocheckboxes = 0; %>
<link rel="stylesheet" type="text/css" href="css/bootstrap.min.css">
<link rel="stylesheet" type="text/css"  href="css/mav.css">
<link rel="stylesheet" type="text/css" href="common/mailarchiva.css">
<script type="text/javascript" src="js/jquery.min.js"></script>
<script type="text/javascript" src="js/bootstrap.js"></script>
<script type="text/javascript" src="js/moment-with-locales.js"></script>
<script type="text/javascript" src="js/bootstrap-datetimepicker.js"></script>
<script type="text/javascript" src="common/CalendarPopup.js"></script>
<!--[if lt IE 9]>
<script src="./js/html5shiv.min.js"></script>
<script src="./js/respond.min.js"></script>
<![endif]-->
<script type="text/javascript">
var showFlg = "${searchBean.showflg}";
$(document).ready(function() {
    /**
     * 전체선택
     */
    $("#toggleCheckboxes").live('click',function(){                 
        if ($(this).is(":checked")){
            // 전체선택 체크된경우 
            $(".searchResultCheckbox").attr("checked", true);          
        }
        else{
            // 전체선택 체크 해제된경우
            $(".searchResultCheckbox").attr("checked", false);          
        }
    });

    /**
     * 내보내기 버튼 클릭
     */
    $("#submit\\.export").live('click',function(){  
        var cnt = $("input[name='deleteData']:checkbox:checked").length;
        if(cnt < 1){
            alert('메일을 선택하세요.');
            return false;
        }
        $('#searchForm').attr('action', '/export.do');
        $('#searchForm').submit();
        
        $('#searchForm').attr('action', '/search.do');
    });

    /**
     * 삭제 버튼 클릭
     */
    $("#submit\\.delete").live('click',function(){  
        var cnt = $("input[name='deleteData']:checkbox:checked").length;
        if(cnt < 1){
            alert('삭제할 메일을 선택하세요.');
            return false;
        }
        if(confirm('선택하신 메일을 삭제하시겠습니까?')){
            return true;
        }
        else{
            return false;
        }
    });

    $('#searchForm').keypress(function(e) {
        if(e.keyCode == 13){
            $('#search').trigger('click');
            return false;
        }
    });
    
    if(showFlg == 'Y'){
        $('#options').css('display', 'block');
    }
});
</script>
<!-- # Add End HyunSeok.Shin 2013-05-07 -->

<script type="text/javascript">
    function switchPreview(obj) {
        var el = document.getElementById(obj);
        if ( el.style.display != 'none' ) {
            el.style.display = 'none';
        } else {
            el.style.display = '';
        }
    }
    function gotoPage(newpage) {
        var currentPage = document.getElementById("currentPage");
        currentPage.value=newpage;
        var searchForm = document.getElementById("searchForm");
        searchForm.submit();
        return false;
    }
      
    
    function outss(str,maxLen) {
        adjust = 0;
        for (i = 0; i < str.length; i++) {
            if (str[i]=="#" && i+4<str.length && str[i+4] == ";") {
                adjust += 5;
            }
        }
        if (str.length-adjust>maxLen) {
            document.writeln(str.substring(0,maxLen-1+adjust)+"..");
        } else { 
            document.writeln(str);
        }
    }
        
    function showhide() {
        if (document.getElementById){
            obj = document.getElementById("options");
            if (obj.style.display == "none"){
                obj.style.display = "block";
                
                // Add Start HyunSeok.Shin 2013-05-16 
                $('#showflg').val('Y');
                // Add End HyunSeok.Shin 2013-05-16 
            } else {
                obj.style.display = "none";
          
                // Add Start HyunSeok.Shin 2013-05-16 
                $('#showflg').val('N');
                // Add End HyunSeok.Shin 2013-05-16 
            }
        }
    }
    function setcursoratend(obj) { 
        if (obj.createTextRange) { 
            var r = (obj.createTextRange()); 
            r.moveStart('character', (obj.value.length)); 
            r.collapse(); 
            r.select(); 
        }
    }
    
    function setfocus() 
    {
        obj = document.getElementById("criteriaquery0");
        obj.focus();
        setcursoratend(obj);
    }
    
    var NS = (document.layers) ? 1 : 0;
    var IE = (document.all)    ? 1 : 0;
    if (NS) document.captureEvents(Event.KEYPRESS); // uncomment if you wish to cancel the key
    document.onkeypress = keyhandler;
    function keyhandler(e) {
        if (NS) {
            Key = e.which;
        } else {
            Key = window.event.keyCode;
        }
        if (Key==13) {
            document.forms[0].search.click();
            return false; //swallow it if we processed it
        }
    }
</script>
<script type="text/javascript">
    var cal1 = new CalendarPopup();
    cal1.setMonthNames('<bean:message key="calendar.jan"/>',
                       '<bean:message key="calendar.feb"/>',
                       '<bean:message key="calendar.mar"/>',
                       '<bean:message key="calendar.apr"/>',
                       '<bean:message key="calendar.may"/>',
                       '<bean:message key="calendar.jun"/>',
                       '<bean:message key="calendar.jul"/>',
                       '<bean:message key="calendar.aug"/>',
                       '<bean:message key="calendar.sep"/>',
                       '<bean:message key="calendar.oct"/>',
                       '<bean:message key="calendar.nov"/>',
                       '<bean:message key="calendar.dec"/>');
    
    cal1.setDayHeaders('<bean:message key="calendar.su"/>',
                       '<bean:message key="calendar.mo"/>',
                       '<bean:message key="calendar.tu"/>',
                       '<bean:message key="calendar.we"/>',
                       '<bean:message key="calendar.th"/>',
                       '<bean:message key="calendar.fr"/>',
                       '<bean:message key="calendar.sa"/>');
    cal1.setWeekStartDay(1);
    cal1.setTodayText("<bean:message key="calendar.today"/>");
    
    var cal2 = new CalendarPopup();
    cal2.setMonthNames('<bean:message key="calendar.jan"/>',
                       '<bean:message key="calendar.feb"/>',
                       '<bean:message key="calendar.mar"/>',
                       '<bean:message key="calendar.apr"/>',
                       '<bean:message key="calendar.may"/>',
                       '<bean:message key="calendar.jun"/>',
                       '<bean:message key="calendar.jul"/>',
                       '<bean:message key="calendar.aug"/>',
                       '<bean:message key="calendar.sep"/>',
                       '<bean:message key="calendar.oct"/>',
                       '<bean:message key="calendar.nov"/>',
                       '<bean:message key="calendar.dec"/>');
    
    cal2.setDayHeaders('<bean:message key="calendar.su"/>',
                       '<bean:message key="calendar.mo"/>',
                       '<bean:message key="calendar.tu"/>',
                       '<bean:message key="calendar.we"/>',
                       '<bean:message key="calendar.th"/>',
                       '<bean:message key="calendar.fr"/>',
                       '<bean:message key="calendar.sa"/>');
    cal2.setWeekStartDay(1);
    cal2.setTodayText("<bean:message key="calendar.today"/>");
</script>
</head>

<body onload="setfocus();" onkeydown="if(event.keyCode == 13){document.getElementById('search').click();}">
<html:form  styleId="searchForm" action="/search" method="POST" autocomplete="false">
<!-- # Add Start HyunSeok.Shin 2013-05-16 -->
<input type="hidden" id='showflg' name='showflg' value='${searchBean.showflg}'/>
<!-- # Add End HyunSeok.Shin 2013-05-16 -->
<div class="container">
    <%@include file="../common/menu.jsp"%>

    <div class="row">
        <div class="col-md-12">
            <div class="container">
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <span class="glyphicon glyphicon-search" style="margin-right:10px"></span>Search
                        <a href="#" data-toggle="modal" data-target="#help"><span class="badge">?</span></a>
                        <button type="submit" class="btn btn-info btn-xs pull-right" name="submit.reset" id="submit.reset"><span class="glyphicon glyphicon-refresh" style="margin-right:6px"></span>Reset</button>
                        <button type="submit" class="btn btn-info btn-xs pull-right" name="submit.showOptions" id="showOptions" style="margin-right:6px;" onclick="showhide(); return(false);"><span class="glyphicon glyphicon-option-horizontal" style="margin-right:6px"></span>Option</button>
                        <button type="submit" class="btn btn-info btn-xs pull-right" name="submit.search" id="search" style="margin-right:6px;"><span class="glyphicon glyphicon-search" style="margin-right:6px"></span>Search</button>
                    </div>
                    <div class="panel-body">
                        <!-- 일자검색 -->
                        <div class='col-sm-2' style="margin-bottom:4px;">
                            <html:select name="searchBean" property="dateType" styleClass="form-control">
                                <html:options name="searchBean" property="dateTypes" labelName="searchBean" labelProperty="dateTypeLabels" />
                            </html:select>                       
                        </div>
                        <div class='col-sm-5' style="margin-bottom:0; padding-bottom:0;">
                            <div class="form-group form-inline" style="margin-bottom:4px;">
                                <label for="after">From</label>
                                <div class='input-group date' id='div_after'>
                                    <html:text styleId="after" name="searchBean" property="after" styleClass="form-control"/>
                                    <span class="input-group-addon" onclick="javascript:cal1.select(document.forms[0].after,'anchor1','<c:out value="${searchBean.dateFormat}"/>');"><span class="glyphicon glyphicon-calendar" name="anchor1" id="anchor1"></span></span>
                                </div>
                            </div>
                        </div>
                        <div class='col-sm-5' style="margin-bottom:0; padding-bottom:0;">
                            <div class="form-group form-inline" style="margin-bottom:4px;">
                                <label for="before">To</label>
                                <div class='input-group date' id='div_before'>
                                    <html:text styleId="before" name="searchBean" property="before" styleClass="form-control"/>
                                    <span class="input-group-addon" onclick="javascript:cal2.select(document.forms[0].before,'anchor2','<c:out value="${searchBean.dateFormat}"/>');"><span class="glyphicon glyphicon-calendar" name="anchor2" id="anchor2"></span></span>
                                </div>
                            </div>
                        </div>

                        <!-- 검색조건 -->
                        <div>
                            <table class="table table-condensed" style="margin-bottom:0; padding-bottom:0;">
                                <logic:iterate id="criteria" name="searchBean" property="criteria" indexId="criteriaIndex">
                                <tr>
                                    <td>
                                        <div class="form-inline">
                                            <div class="form-group">
                                                <c:if test="${criteriaIndex>0}"> 
                                                    <html:select indexed="true" name="criteria" property="operator" styleClass="form-control input-sm"> 
                                                        <html:options name="searchBean" property="operators" labelName="searchBean" labelProperty="operatorLabels" /> 
                                                    </html:select> 
                                                </c:if> 
                                                <c:if test="${criteriaIndex==0}">&nbsp;<!-- <bean:message key="searchresults.field"/> --></c:if>
                                            </div>
                                        </div>
                                    </td>
                                    <td>
                                        <div class="form-inline">
                                            <div class="form-group">
                                                <label for="field">검색조건</label>
                                                <html:select indexed="true" name="criteria" property="field" styleClass="form-control input-sm"> 
                                                    <html:options name="searchBean" property="fields" labelName="searchBean" labelProperty="fieldLabels"/> 
                                                </html:select> 
                                            </div>
                                        </div>
                                    </td>
                                    <td>
                                        <div class="form-inline">
                                            <div class="form-group">
                                                <label for="method" style="margin-left:10px;">일치조건</label>
                                                <html:select indexed="true" name="criteria" styleId="criteria" property="method" styleClass="form-control input-sm"> 
                                                    <html:options name="searchBean" property="methods" labelName="searchBean" labelProperty="methodLabels"/> 
                                                </html:select> 
                                            </div>
                                        </div>
                                    </td>
                                    <td style="text-align:left">
                                        <div class="form-inline">
                                            <html:text styleId="criteriaquery${criteriaIndex}" indexed="true" name="criteria" property="query" styleClass="form-control"/>
                                            <c:if test="${fn:length(searchBean.criteria)<25}">
                                                <button type="submit" class="btn btn-default" name="submit.newcriteria.${criteriaIndex}"><span class="glyphicon glyphicon-plus"></span></button>
                                            </c:if>
                                            <c:if test="${fn:length(searchBean.criteria)>1}"> 
                                                <button type="submit" class="btn btn-default" name="submit.deletecriteria.${criteriaIndex}"><span class="glyphicon glyphicon-minus"></span></button>
                                            </c:if>
                                        </div>
                                    </td>
                                </tr>
                                </logic:iterate>
                            </table>
                        </div>

                        <!-- search option -->
                        <div style="display: none;" id="options">
                            <hr style="border:1px solid #5BC0DE; font-weight:bold; margin:4px 0 4px 0;"/>
                            <div id="search_option">
                                <div class="form-inline">
                                    <div class="form-group">
                                        <label for="sopt_file"><bean:message key="searchresults.attachment"/></label>
                                        <html:select name="searchBean" property="attachment" styleClass="form-control input-sm">
                                            <html:options name="searchBean" property="attachments" labelName="searchBean" labelProperty="attachmentLabels" />
                                        </html:select>
                                    </div>
                                    <div class="form-group">
                                        <label for="sopt_imp" style="margin-left:10px;"><bean:message key="searchresults.priority"/></label>
                                        <html:select name="searchBean" property="priority" styleClass="form-control input-sm">
                                            <html:options name="searchBean" property="priorities" labelName="searchBean" labelProperty="priorityLabels" />
                                        </html:select>
                                    </div>
                                    <div class="form-group">
                                        <label for="sopt_flag" style="margin-left:10px;"><bean:message key="searchresults.flag"/></label>
                                        <html:select name="searchBean" property="flag" styleClass="form-control input-sm">
                                            <html:options name="searchBean" property="flags" labelName="searchBean" labelProperty="flagLabels" />
                                        </html:select>
                                    </div>
                                    <div class="form-group">
                                        <label for="sopt_disp" style="margin-left:10px;"><bean:message key="searchresults.result_per_page"/></label>
                                        <html:select name="searchBean" property="pageSize" styleClass="form-control input-sm">
                                            <html:options name="searchBean" property="pageSizes" labelName="searchBean" labelProperty="pageSizeLabels" />
                                        </html:select>
                                    </div>
                                    <div class="form-group">
                                        <label for="sopt_lang" style="margin-left:10px;"><bean:message key="searchresults.language"/></label>
                                        <html:select name="searchBean" property="language" styleClass="form-control input-sm">
                                            <html:options name="searchBean" property="languages" labelName="searchBean" labelProperty="languageLabels" />
                                        </html:select>                                    
                                    </div>
                                </div>
                            </div>
                        </div>
                        <!-- search option -->
                    </div>
                </div>

                <!-- 검색결과 -->
                <div class="">

                    <c:if test="${searchBean.totalHits>0}"> 
                    <table class="table table-condensed">
                        <!-- # Add Start HyunSeok.Shin 2013-05-07 -->
                        <!-- # Admin Role 사용자일 경우 표시 -->
                        <tr class="columnheadings" >
                            <td >&nbsp;</td> 
                            <td colspan='9' nowrap align="left">
                                <input type="button" name="submit.export" id="submit.export" value="<bean:message key="searchresults.export"/>"/>
                                <% if (request.isUserInRole("configure")) {%>
                                <input type="submit" name="submit.delete" id="submit.delete" value="<bean:message key="searchresults.delete"/>"/>
                                <% } %>
                            </td>
                        </tr>
                        <!-- # Add End HyunSeok.Shin 2013-05-07 -->
          
                        <tr class="columnheadings" > 
                            <!-- # Add Start HyunSeok.Shin 2013-05-07 -->
                            <td >&nbsp;</td>
                            <td width="2%" class="columnspacing">
                                <div style="text-align:center; vertical-align:top"><input id="toggleCheckboxes" type="checkbox"/></div>
                            </td>
                            <!-- # Add End HyunSeok.Shin 2013-05-07 -->
                        
                            <logic:iterate id="field" name="searchBean" property="availableFields" indexId="fieldsIndex"> 
                                <c:if test="${field.showConditional==true}">
                                    <c:choose>
                                        <c:when test='${field.name == "archivedate" && searchBean.dateType=="archivedate"}'>
                                            <td width="<c:out value='${field.columnSize}'/>%" class="columnspacing">
                                                <div style="text-align:center; vertical-align:top">
                                                    <a href="searchsort.do?orderBy=<c:out value='${field.name}' escapeXml='false'/>" class="columnheadertext">
                                                        <bean:message key="${field.resource}"/>
                                                        <c:if test="${searchBean.orderBy==field.name}">
                                                            <c:if test="${searchBean.sortOrder=='descending'}">\</c:if>
                                                            <c:if test="${searchBean.sortOrder=='ascending'}">/</c:if>
                                                        </c:if>
                                                    </a>
                                                </div>
                                            </td>
                                        </c:when>    
                                        <c:when test='${field.name == "sentdate"  && searchBean.dateType=="sentdate"}'>
                                            <td width="<c:out value='${field.columnSize}'/>%" class="columnspacing">
                                                <div style="text-align:center; vertical-align:top">
                                                    <a href="searchsort.do?orderBy=<c:out value='${field.name}' escapeXml='false'/>" class="columnheadertext">
                                                        <bean:message key="${field.resource}"/>
                                                        <c:if test="${searchBean.orderBy==field.name}">
                                                            <c:if test="${searchBean.sortOrder=='descending'}">\</c:if>
                                                            <c:if test="${searchBean.sortOrder=='ascending'}">/</c:if>
                                                        </c:if>
                                                    </a>
                                                </div>
                                            </td>
                                        </c:when>    
                                        <c:when test='${field.name == "receiveddate"  && searchBean.dateType=="receiveddate"}'>
                                            <td width="<c:out value='${field.columnSize}'/>%" class="columnspacing">
                                                <div style="text-align:center; vertical-align:top">
                                                    <a href="searchsort.do?orderBy=<c:out value='${field.name}' escapeXml='false'/>" class="columnheadertext">
                                                        <bean:message key="${field.resource}"/>
                                                        <c:if test="${searchBean.orderBy==field.name}">
                                                            <c:if test="${searchBean.sortOrder=='descending'}">\</c:if>
                                                            <c:if test="${searchBean.sortOrder=='ascending'}">/</c:if>
                                                        </c:if>
                                                    </a>
                                                </div>
                                            </td>
                                        </c:when>    
                                    </c:choose>        
                                </c:if>    
                                <c:if test="${field.showResults==true}">
                                    <td width="<c:out value='${field.columnSize}'/>%" class="columnspacing">
                                        <div style="text-align:center; vertical-align:top">
                                            <a href="searchsort.do?orderBy=<c:out value='${field.name}'/>" class="columnheadertext">
                                                <c:choose>
                                                    <c:when test='${field.name == "priority"}'>
                                                        <img style="border: none;" src="images/priority.gif" alt="Priority"/>
                                                    </c:when>
                                                    <c:when test='${field.name == "attach"}'>
                                                        <img style="border: none;" src="images/attach.gif" alt="Attach"/>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <bean:message key="${field.resource}"/>
                                                    </c:otherwise>
                                                </c:choose>
                                                <c:if test="${searchBean.orderBy==field.name}">
                                                    <c:if test="${searchBean.sortOrder=='descending'}">\</c:if>
                                                    <c:if test="${searchBean.sortOrder=='ascending'}">/</c:if>
                                                </c:if>
                                            </a>
                                        </div>
                                    </td>
                                </c:if>
                            </logic:iterate>
                        </tr>   
                    </table>
         
                    <table class="table table-condensed">
                        <logic:iterate id="searchResults" offset="${searchBean.firstHitIndex}" length="${searchBean.pageSize}" name="searchBean" property="searchResults" indexId="resultsIndex" type="com.stimulus.archiva.presentation.SearchResultBean">             
                            <c:if test="${searchResults.display==true}">
                                <tr>  
                                    <!-- # Add Start HyunSeok.Shin 2013-05-07 -->
                                    <td width="2%">
                                        <div style="text-align:center; vertical-align:top">
                                            <html:checkbox styleClass="searchResultCheckbox" property="deleteData" value="${searchResults.volumeID}|${searchResults.uniqueID}" />
                                        </div>
                                    </td>
                                    <!-- # Add End HyunSeok.Shin 2013-05-07 -->
                                    <td>
                                        <logic:iterate id="value" name="searchResults" property="fieldValues" indexId="valueIndex"> 
                                            <c:if test="${value.field.showConditional==true}">
                                                <c:choose>
                                                    <c:when test='${value.field.name == "sentdate" && searchBean.dateType=="sentdate"}'>
                                                        <td width="<c:out value='${value.field.columnSize}'/>%" valign="top">
                                                            <script type="text/javascript">outss("<c:out value='${value.display}'/>",250)</script>
                                                        </td>
                                                    </c:when>
                                                    <c:when test='${value.field.name == "archivedate" && searchBean.dateType=="archivedate"}'>
                                                        <td width="<c:out value='${value.field.columnSize}'/>%" valign="top">
                                                            <script type="text/javascript">outss("<c:out value='${value.display}'/>",250)</script>
                                                        </td>
                                                    </c:when>    
                                                    <c:when test='${value.field.name == "receiveddate" && searchBean.dateType=="receiveddate"}'>
                                                        <td width="<c:out value='${value.field.columnSize}'/>%" valign="top">
                                                            <script type="text/javascript">outss("<c:out value='${value.display}'/>",250)</script>
                                                        </td>
                                                    </c:when>
                                                </c:choose>
                                            </c:if>
                                            <c:if test="${value.field.showResults==true}">
                                                <td width="<c:out value='${value.field.columnSize}'/>%" valign="top">
                                                    <c:choose>
                                                        <c:when test='${value.field.name == "priority"}'>
                                                            <c:if test="${value.value<3}"><img src="images/priority.gif" alt="Priority"/></c:if>
                                                            <c:if test="${value.value>=3}">&nbsp;</c:if>
                                                        </c:when>
                                                        <c:when test='${value.field.name == "attach"}'>
                                                            <c:if test="${value.value=='1'}"><img src="images/attach.gif" alt="Attach"/></c:if>
                                                            <c:if test="${value.value=='0'}">&nbsp;</c:if>
                                                        </c:when>
                                                        <c:when test='${value.field.name == "subject"}'>
                                                            <jsp:useBean id="detailParams" class="java.util.HashMap" type="java.util.HashMap" />
                                                            <c:set target="${detailParams}" property="messageID" value="${searchResults.uniqueID}" />
                                                            <c:set target="${detailParams}" property="volumeID" value="${searchResults.volumeID}" />
                                                            <c:set target="${detailParams}" property="resultsIndex" value="${resultsIndex}" />
                                                            <c:if test="${searchResults.messageExist==true}">
                                                                   <t:tooltip>
                                                                       <t:text>
                                                                           <html:link  name="detailParams" scope="page"  page="/viewmail.do">
                                                                               <script type="text/javascript">outss("<c:out value='${value.display}' escapeXml='false'/>",50)</script>
                                                                           </html:link>
                                                                       </t:text>
                                                                       <t:help style="position:absolute;visibility:hidden;background-color:#d3e3f6;padding:1px;border-style:solid;border-color:black;border-width:1px;margin-top:1px;" width="300">
                                                                           <c:out value='${value.display}' escapeXml='false'/>
                                                                       </t:help>
                                                                   </t:tooltip>
                                                               </c:if>
                                                               <c:if test="${searchResults.messageExist==false}">
                                                                   <t:tooltip>
                                                                       <t:text>
                                                                           <script type="text/javascript">outss("<c:out value='${value.display}' escapeXml='false'/>",50)</script>
                                                                       </t:text>
                                                                       <t:help style="position:absolute;visibility:hidden;background-color:#d3e3f6;padding:1px;border-style:solid;border-color:black;border-width:1px;margin-top:1px;" width="300">
                                                                           <bean:message key="searchresults.volume_not_accessible"/>
                                                                       </t:help>
                                                                   </t:tooltip>
                                                               </c:if>
                                                        </c:when>
                                                        <c:when test='${value.field.name == "to"}'>
                                                            <t:tooltip>
                                                                <t:text>
                                                                    <script type="text/javascript">outss("<c:out value='${value.display}' escapeXml='false'/>",200)</script>
                                                                </t:text>
                                                                <t:help style="position:absolute;visibility:hidden;background-color:#d3e3f6;padding:1px;border-style:solid;border-color:black;border-width:1px;margin-top:1px;" width="300">
                                                                    <script type="text/javascript">outss("<c:out value='${value.tip}' escapeXml='false'/>",1000)</script>
                                                                </t:help>
                                                            </t:tooltip>  
                                                        </c:when>
                                                        <c:otherwise>
                                                            <c:if test="${value.display==null}">&nbsp;</c:if>
                                                            <c:if test="${value.display!=null}">
                                                                <t:tooltip>
                                                                    <t:text>
                                                                        <script type="text/javascript">outss("<c:out value='${value.display}' escapeXml='false'/>",35)</script>
                                                                    </t:text>
                                                                    <t:help style="position:absolute;visibility:hidden;background-color:#d3e3f6;padding:1px;border-style:solid;border-color:black;border-width:1px;margin-top:1px;" width="300">
                                                                        <script type="text/javascript">outss("<c:out value='${value.tip}' escapeXml='false'/>",1000)</script>
                                                                    </t:help>
                                                                </t:tooltip>  
                                                            </c:if>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                            </c:if>
                                        </logic:iterate>
                                    </td>
                                </tr>
                            </c:if>
                        </logic:iterate>
                    </table>
                    
                    <html:hidden styleId="currentPage" name="searchBean" property="page"/>
                    <table class="table table-condensed">
                        <tr> 
                            <td width="100%" height="25">
                                <c:if test="${searchBean.noPages>1}">
                                <table class="table table-condensed">
                                    <tr>
                                        <td width="33%">&nbsp;</td>
                                        <td width="58%">
                                            <strong>
                                                <bean:message key="searchresults.pages"/> 
                                                <html:link href="" onclick="return gotoPage('${searchBean.firstPage}');">
                                                    <bean:message key="searchresults.first"/>
                                                </html:link>&nbsp;
                                                <html:link href="" onclick="return gotoPage('${searchBean.previousPage}');">
                                                    <bean:message key="searchresults.previous"/>
                                                </html:link>[
                                                <c:forEach var="pages" begin="${searchBean.minViewPage}" end="${searchBean.maxViewPage}" step="1">
                                                    <c:if test="${pages==searchBean.page}">
                                                        <c:out value="${pages}"/>
                                                    </c:if>
                                                    <c:if test="${pages!=searchBean.page}">
                                                        <html:link href="" onclick="return gotoPage('${pages}');">
                                                            <c:out value="${pages}"/>
                                                        </html:link>
                                                    </c:if>
                                                    <c:if test="${pages!=searchBean.noPages}">-</c:if>        
                                                </c:forEach>
                                                ]
                                                <html:link href="" onclick="return gotoPage('${searchBean.nextPage}');"><bean:message key="searchresults.next"/></html:link>&nbsp;
                                                <html:link href="" onclick="return gotoPage('${searchBean.lastPage}');"><bean:message key="searchresults.last"/></html:link> 
                                            </strong>
                                        </td>
                                        <td width="29%">&nbsp;</td>
                                    </tr>
                                </table>
                                </c:if>
                                <c:if test="${searchBean.noPages==1}">&nbsp;</c:if>
                            </td>
                        </tr>
                    </table>  
                    </c:if> <!-- if test="${searchBean.totalHits>0} -->

                    <c:if test="${searchBean.totalHits<1 && searchBean.notSearched==false}">
                    <table class="table table-condensed">
                        <tr> 
                            <td width="100%">
                                <br><bean:message key="searchresults.no_emails"/> <c:out value="${searchResults.searchQuery}"/><br>
                                <br><bean:message key="searchresults.no_emails_comment"/>
                            </td>
                        </tr>
                    </table>
                    </c:if>

                    <c:if test="${searchBean.notSearched==true}">
                    <table class="table table-condensed">
                        <tr > 
                            <td width="100%"><br><bean:message key="searchresults.no_search"/><br><br></td>
                        </tr>   
                    </table>
                    </c:if>

                </div>

            </div>
        </div>
    </div>

    <div class="modal fade" id="help" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                    <h4 class="modal-title" id="myModalLabel">검색 도움말</h4>
                </div>
                <div class="modal-body">
                    <div class="panel panel-default">
                        <div class="panel-heading">검색어 입력 규칙</div>
                        <div class="panel-body">
                            <li style="margin-left:4px;">한글:  
                                from 및 to의 경우 full text를 입력(일부 입력 검색불가), subject 및 body의 경우 2자 이상 입력
                                <div>예) from/to를 검색하는 경우 : 홍길(X), 홍길동(O)</div>
                            </li>
                            <li style="margin-left:4px;">영문:
                                                                                     단어별로 인식하여 일부 입력 검색불가. "_" 등으로 연결된 문자열의 경우 한단어로 인식
                                <div>예) tester를 검색하는 경우 : test(X), tester(O)</div>
                            </li>
                        </div>
                    </div>
                    <div class="panel panel-default">
                        <div class="panel-heading">검색 제안</div>
                        <div class="panel-body">
                            <li style="margin-left:4px;">철자가 맞는지 확인</li>
                            <li style="margin-left:4px;">많이 쓰는 단어로 검색</li>
                            <li style="margin-left:4px;">동의어를 이용하여 검색</li>
                            <li style="margin-left:4px;">되도록 적은 단어를 이용하여 검색 범위를 확장</li>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                </div>
            </div>
        </div>
    </div>

</div>
<%@include file="../common/bottom.jsp"%>
</html:form>
</body>
</html>
