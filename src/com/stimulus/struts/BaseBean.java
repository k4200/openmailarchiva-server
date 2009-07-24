
/* Copyright (C) 2005-2007 Jamie Angus Band 
 * MailArchiva Open Source Edition Copyright (c) 2005-2007 Jamie Angus Band
 * This program is free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation; either version
 * 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see http://www.gnu.org/licenses or write to the Free Software Foundation,Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */
package com.stimulus.struts;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import javax.servlet.ServletRequest;
import org.apache.commons.logging.*;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;

import com.stimulus.archiva.domain.MailArchivaPrincipal;

public abstract class BaseBean extends ActionForm implements Serializable {

  private static final long serialVersionUID = 1324834450703716122L;
  protected static Log logger = LogFactory.getLog(BaseBean.class.getName());	
	
  @Override
public void reset(ActionMapping mapping, ServletRequest request) {
    ActionContext.initialize((HttpServletRequest)request, null);
    reset();
  }

  @Override
public void reset(ActionMapping mapping, HttpServletRequest request) {
    ActionContext.initialize(request, null);
    reset();
  }

  @Override
public ActionErrors validate(ActionMapping mapping, ServletRequest request) {
    ActionContext.initialize((HttpServletRequest)request, null);
    ActionContext ctx = ActionContext.getActionContext();
    Map requestMap = ctx.getRequestMap();

    List errorList = null;
    requestMap.put("errors",errorList);
    validate();
    errorList = (List) requestMap.get("errors");
    ActionErrors actionErrors = null;
    if (errorList != null && !errorList.isEmpty()) {
      actionErrors = new ActionErrors();
      actionErrors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("global.error"));
    }
    return actionErrors;
  }

  @Override
public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
    ActionContext.initialize(request, null);
    ActionContext ctx = ActionContext.getActionContext();
    Map requestMap = ctx.getRequestMap();

    List errorList = null;
    requestMap.put("errors",errorList);
    validate();
    errorList = (List) requestMap.get("errors");
    ActionErrors actionErrors = null;
    if (errorList != null && !errorList.isEmpty()) {
      actionErrors = new ActionErrors();
      actionErrors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("global.error"));
    }
    return actionErrors;
  }
  
  public void setSimpleMessage(String message) {
	  ActionContext.getActionContext().setSimpleMessage(message);
  }

  protected class SubmitButton {
  	public String action = null;
  	public String value = null;
  	public SubmitButton() {};
  }
  // hack to get around struts poor handling of multiple button selections
  protected final SubmitButton getSubmitButton() {
  	HttpServletRequest request = ActionContext.getActionContext().getRequest();
    Enumeration enumo = request.getParameterNames();
    SubmitButton button = new SubmitButton();
    String parameterName = null;
    while(enumo.hasMoreElements()) {
    	
      parameterName = (String)enumo.nextElement();
      if(parameterName.startsWith("submit.")) {
        String temp = parameterName.substring(parameterName.indexOf("submit.")+7,parameterName.length());
        if (temp.indexOf('.')<1)
        	button.action = temp;
        else {
        	button.action = temp.substring(0,temp.indexOf('.'));
        	button.value = temp.substring(button.action.length()+1,temp.length());
        }
      }
    }
    return button;
  }

  public void validate() {
  }

  public void reset() {
  }

  public void clear() {
  }

  protected void validateRequiredField(String value, String errorMessage) {
    if (value == null || value.trim().length() < 1) {
      ActionContext.getActionContext().addSimpleError(errorMessage);
    }
  }
  
  protected String getMessage(String key) {
      Locale locale = getLocale();
      Object servlet = getServlet();
      if (servlet!=null) {
	      MessageResources mr = (MessageResources) getServlet().getServletContext().getAttribute(Globals.MESSAGES_KEY);
	      return mr.getMessage(locale,key);
      } else {
    	  logger.warn("getMessage(): servlet is null");
    	  return "";
      }
  }
  
  protected Locale getLocale() {
	  Locale locale = Locale.getDefault();
	  String country = locale.getCountry();
	  ActionContext context = ActionContext.getActionContext();
	  if (context!=null) {
		  HttpServletRequest request = context.getRequest();
	      if (request!=null) 
	  	  	locale = request.getLocale();
	  }
	  if (locale.getCountry().length()<1 && locale.getLanguage().length()>0) {
		  locale = new Locale(locale.getLanguage(),Locale.getDefault().getCountry());
	  }
	  String country2 = locale.getCountry();
	  //if (locale.getCountry().equalsIgnoreCase("ZA"))
	  //		locale = Locale.UK; // bug in JVM concerning date formatting in South Africa
      return locale;
  }
  
  protected List<String> translateList(List<String> srcList, boolean toLowerCase)
  {
	  List<String> translatedList = new ArrayList<String>();
	  for (int i = 0; i < srcList.size(); i++)
	  {
		  String toTranslate = srcList.get(i);
		  String translated = getMessage(toTranslate);
		  if (toLowerCase) {
			  toTranslate = toTranslate.toLowerCase();
			  translated = translated.toLowerCase();
		  }
		  if (translated != null)
			  translatedList.add(translated);
		  else
			  translatedList.add(toTranslate);
	  }
	  return translatedList;
  }

  protected List<String> translateList(List<String> srcList) {
	  return translateList(srcList,false);
  }
  
  public MailArchivaPrincipal getMailArchivaPrincipal() {
	 	String remoteHost = ActionContext.getActionContext().getRequest().getRemoteHost();
	  	MailArchivaPrincipal cp = (MailArchivaPrincipal)ActionContext.getActionContext().getRequest().getUserPrincipal();  	
	  	if (cp!=null)
	  		cp.setIpAddress(remoteHost);
	  	return cp;
  }
  
}
