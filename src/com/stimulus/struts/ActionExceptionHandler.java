
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
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
//import org.apache.commons.logging.*.Log;
//import org.apache.commons.logging.*.LogFactory;
import org.apache.commons.logging.*;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.ExceptionHandler;
import org.apache.struts.config.ExceptionConfig;



/**	
* Implementation of <strong>ExceptionHandler</strong> that	
* handles any Exceptions that are bubbled up to the Action	
* layer.  This allows us to remove generic try/catch statements	
* from our Action Classes.	
*	
* <p>	
* <a href="ActionExceptionHandler.java.html"><i>View Source</i></a>	
* </p>	
*	
* @author <a href="mailto:matt@raibledesigns.com">Matt Raible</a>	
* @version $Revision$ $Date$	
*/

public final class ActionExceptionHandler extends ExceptionHandler implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -4219810091670377408L;
	protected static final Log logger = LogFactory.getLog(ActionExceptionHandler.class);
//~ Instance fields ========================================================


//private Log log = LogFactory.getLog(ActionExceptionHandler.class);


//~ Methods ================================================================


/**
 * This method handles any java.lang.Exceptions that are not
 * caught in previous classes.  It will loop through and get
 * all the causes (exception chain), create ActionErrors,
 * add them to the request and then forward to the input.
 *
 * @see org.apache.struts.action.ExceptionHandler#execute
 *      (
 *          java.lang.Exception,
 *          org.apache.struts.config.ExceptionConfig,
 *          org.apache.struts.action.ActionMapping,
 *          org.apache.struts.action.ActionForm,
 *          javax.servlet.http.HttpServletRequest,
 *          javax.servlet.http.HttpServletResponse
 *      )
 */

@Override
public ActionForward execute(Exception ex, ExceptionConfig ae,ActionMapping mapping,
							 ActionForm formInstance,HttpServletRequest request,
                             HttpServletResponse response) throws ServletException {
	
	if (ex instanceof org.apache.struts.chain.commands.InvalidPathException) {
		  ActionForward forward = new ActionForward();
		  forward.setRedirect(true);
		  forward.setPath("/");
		  return forward;
		
	}
    // if there's already errors in the request, don't process
    ActionErrors errors = (ActionErrors) request.getAttribute(Globals.ERROR_KEY);
    if (errors != null) {
        return null;
    }
    ActionForward forward = super.execute(ex, ae, mapping, formInstance, request, response);
    ActionMessage error = null;
    String property = null;
    // log the exception to the default logger
    logException(ex);
    // Get the chained exceptions (causes) and add them to the
    // list of errors as well
    while (ex != null) {
        String msg = ex.getMessage();
        error = new ActionMessage("errors.detail", msg);
        property = error.getKey();
        ex = (Exception) ex.getCause();

        if ((ex != null) && (ex.getMessage() != null)) {
            // check to see if the child message is the same
            // if so, don't store it
            //if (msg.indexOf(ex.getMessage()) == -1) {
                storeException(request, property, error, forward);
            //}
        } else {
            storeException(request, property, error, forward);

        }
    }

    return forward;
}


/**

 * This method overrides the the ExceptionHandler's storeException
 * method in order to create more than one error message.
 *
 * @param request - The request we are handling
 * @param property  - The property name to use for this error
 * @param error - The error generated from the exception mapping
 * @param forward - The forward generated from the input path
 *                  (from the form or exception mapping)
 */

protected void storeException(HttpServletRequest request, String property,

                              ActionMessage error, ActionForward forward) {

    ActionMessages errors = (ActionMessages) request.getAttribute(Globals.ERROR_KEY);
    if (errors == null) {
        errors = new ActionMessages();
    }
    errors.add(property, error);
    request.setAttribute(Globals.ERROR_KEY, errors);

}


/**

 * Overrides logException method in ExceptionHandler to print the stackTrace

 * @see org.apache.struts.action.ExceptionHandler#logException(java.lang.Exception)

 */

@Override
protected void logException(Exception ex) {
    StringWriter sw = new StringWriter();
    ex.printStackTrace(new PrintWriter(sw));
    logger.error(sw.toString());

}

}

