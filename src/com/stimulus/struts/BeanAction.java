/*
 * Subversion Infos:
 * $URL$
 * $Author$
 * $Date$
 * $Rev$
*/

		
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

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.stimulus.archiva.domain.Config;
import com.stimulus.archiva.exception.BeanActionException;

/**
 * BeanAction is an extension to the typical Struts Action class that
 * enables mappings to bean methods.  This allows for a more typical
 * Object Oriented design where each object has behaviour as part of
 * its definition.  Instead of writing separate Actions and Forms,
 * BeanAction allows you to simply have a Bean, which models both
 * the state and the methods that operate on that state.
 * <p/>
 * In addition to the simpler packaging, BeanAction also simplifies the
 * Struts progamming paradigm and reduces dependency on Struts.  Using
 * this pattern could allow easier migration to newer frameworks like JSF.
 * <p/>
 * The method signatures are greatly simplified to the following
 * <pre>
 * public String myActionMethod() {
 *   //..work
 *   return "success";
 * }
 * </pre>
 * The return parameter becomes simply the name of the forward (as defined
 * in the config file as usual).  Form parameters, request, response, session,
 * attributes, and cookies are all accessed via the ActionContext class (see the
 * ActionContext javadocs for more).
 * <p/>
 * The forms that you map to a BaseAction mapping must be a subclass of the
 * BaseBean class.  BaseBean continues to simplify the validation and
 * reset methods by removing the parameters from the signature as was done with
 * the above action method example.
 * <p/>
 * There are 3 ways to map a BeanAction in the struts configuration file.
 * They are as follows.
 * <p/>
 * <B>URL Pattern</B>
 * <p/>
 * This approach uses the end of the action definition to determine which
 * method to call on the Bean.  For example if you request the URL:
 * <p/>
 * http://localhost/jpetstore4/shop/viewOrder.do
 * <p/>
 * Then the method called would be "viewOrder" (of the mapped bean as specified
 * by the name="" parameter in the mapping below).  The mapping used for this
 * approach is as follows.
 * <pre>
 *  &lt;action path="/shop/<b>viewOrder</b>" type="com.stimulus.struts.BeanAction"
 *    name="orderBean" scope="session"
 *    validate="false"&gt;
 *    &lt;forward name="success" path="/order/ViewOrder.jsp"/&gt;
 *  &lt;/action&gt;
 * </pre>
 *
 * <B>Method Parameter</B>
 * <p/>
 * This approach uses the Struts action parameter within the mapping
 * to determine the method to call on the Bean.  For example the
 * following action mapping would cause the "viewOrder" method to
 * be called on the bean ("orderBean").  The mapping used for this
 * approach is as follows.
 * <pre>
 *  &lt;action path="/shop/viewOrder" type="com.stimulus.struts.BeanAction"
 *    <b>name="orderBean" parameter="viewOrder"</b> scope="session"
 *    validate="false"&gt;
 *    &lt;forward name="success" path="/order/ViewOrder.jsp"/&gt;
 *  &lt;/action&gt;
 * </pre>
 * <B>No Method call</B>
 * <p/>
 * BeanAction will ignore any Struts action mappings without beans associated
 * to them (i.e. no name="" attribute in the mapping).  If you do want to associate
 * a bean to the action mapping, but do not want a method to be called, simply
 * set the parameter to an asterisk ("*").  The mapping used for this approach
 * is as follows (no method will be called).
 * <pre>
 *  &lt;action path="/shop/viewOrder" type="com.stimulus.struts.BeanAction"
 *    <b>name="orderBean" parameter="*"</b> scope="session"
 *    validate="false"&gt;
 *    &lt;forward name="success" path="/order/ViewOrder.jsp"/&gt;
 *  &lt;/action&gt;
 * </pre>
 * <p/>
 * <B>A WORK IN PROGRESS</B>
 * <p/>
 * <i>The BeanAction Struts extension is a work in progress.  While it demonstrates
 * good patterns for application development, the framework itself is very new and
 * should not be considered stable.  Your comments and suggestions are welcome.
 * Please visit <a href="http://www.ibatis.com">http://www.ibatis.com</a> for contact information.</i>
 * <p/>
 * Date: Mar 11, 2004 10:03:56 PM
 *
 * @author Clinton Begin
 * @see BaseBean
 * @see ActionContext
 */
public class BeanAction extends Action implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8636344294577044962L;
	protected static Logger logger = Logger.getLogger(Config.class);
	
	public final static String getActionPathMethodName(ActionMapping mapping) {
		String methodName = mapping.getPath();
          if (methodName.length() > 1) {
         
            int slash = methodName.lastIndexOf("/") + 1;
            methodName = methodName.substring(slash);
            if (methodName.length() > 0)
            	return methodName;
          }
          return null;
	}
	
	public final static String callMethod(ActionForm form, String methodName) throws Exception {
		Method method = null;
		String forward = null;
		try {
            method = form.getClass().getMethod(methodName, null);
            forward = (String) method.invoke(form, null);
          } catch (InvocationTargetException ite) {
          	throw new BeanActionException("failed to execute method. Cause:"+ite.getTargetException(),ite,logger);
          }       
          return forward;
	}

  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
    response.setContentType("text/html;charset=UTF-8");
    String forward = "success";

    try {
   
        ActionContext.initialize(request, response);
        
      	if (form != null) {
      	
      		String actionParameter = mapping.getParameter();
      	
	      		if (actionParameter!=null && !actionParameter.equals("*")) {
	      		
					forward = callMethod(form,actionParameter);
				} else if (actionParameter==null)  { 
				    
					String methodName = getActionPathMethodName(mapping);
					forward = callMethod(form,methodName);
				}
      	    }
    
    } catch (Exception e) {
      request.setAttribute("BeanActionException", e);
      throw e;
    }

    return mapping.findForward(forward);
  }

}
