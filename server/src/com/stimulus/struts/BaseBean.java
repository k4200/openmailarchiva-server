package com.stimulus.struts;


import org.apache.struts.action.*;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * All actions mapped through the BeanAction class should be mapped
 * to a subclass of BaseBean (or have no form bean mapping at all).
 * <p/>
 * The BaseBean class simplifies the validate() and reset() methods
 * by allowing them to be managed without Struts dependencies. Quite
 * simply, subclasses can override the parameterless validate()
 * and reset() methods and set errors and messages using the ActionContext
 * class.
 * <p/>
 * <i>Note:  Full error, message and internastionalization support is not complete.</i>
 * <p/>
 * Date: Mar 12, 2004 9:20:39 PM
 *
 * @author Clinton Begin
 */
public abstract class BaseBean extends ActionForm {

  public void reset(ActionMapping mapping, ServletRequest request) {
    ActionContext.initialize((HttpServletRequest)request, null);
    reset();
  }

  public void reset(ActionMapping mapping, HttpServletRequest request) {
    ActionContext.initialize((HttpServletRequest)request, null);
    reset();
  }

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
  

  protected class SubmitButton {
  	public String action = null;
  	public String value = null;
  	public SubmitButton() {};
  }
  // hack to get around struts poor handling of multiple button selections
  protected final SubmitButton getSubmitButton() {
  	HttpServletRequest request = ActionContext.getActionContext().getRequest();
    Enumeration enum = request.getParameterNames();
    SubmitButton button = new SubmitButton();
    String parameterName = null;
    while(enum.hasMoreElements()) {
    	
      parameterName = (String)enum.nextElement();
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

}
