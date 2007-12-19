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

package com.stimulus.archiva.authentication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import com.stimulus.archiva.domain.Config;
import com.stimulus.archiva.domain.Identity;
import com.stimulus.archiva.exception.ConfigurationException;
import com.stimulus.util.Compare;


public class BasicIdentity extends Identity implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 3166260447557364498L;
	protected static final String basicRoleKey  		= "role";
    protected static final String basicUsernameKey 	= "email";
    protected static final String basicPasswordKey 	= "password";
    
	 public void newRoleMap() throws ConfigurationException {
	   	 addRoleMap(new BasicRoleMap(ROLES.get(0),"",""));
	 }
	 
	 public void addRoleMap(String role, String username, String password) throws ConfigurationException {
	    	addRoleMap(new BasicRoleMap(role,username, password));
	 }
	 
    public Document createDomDocument() {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.newDocument();
            return doc;
        } catch (ParserConfigurationException e) {
        }
        return null;
    }
    
    public void writeXmlFile(Document doc, String filename) {
        try {
            Source source = new DOMSource(doc);
            OutputStream os = new FileOutputStream(filename);
            StreamResult result = new StreamResult(os);
            //Result result = new StreamResult(file);
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.setOutputProperty(OutputKeys.INDENT, "yes");
            xformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
        	logger.error("failed to write users.conf",e);
        } catch (TransformerException e) {
        	logger.error("failed to write users.conf",e);
        } catch (FileNotFoundException e) {
        	logger.error("failed to write users.conf",e);
        }
    }

	 public void save() {
		 	Document doc = createDomDocument();
		    Element users = doc.createElement("Users");
		    users.setAttribute("version","1.0");
		    doc.appendChild(users);
		    for (RoleMap roleMap : getRoleMaps()) {
		    	Element user = doc.createElement("User");
		    	BasicRoleMap brm = (BasicRoleMap)roleMap;
		    	user.setAttribute(basicUsernameKey,brm.getUsername());
		    	user.setAttribute(basicRoleKey,brm.getRole());
		    	user.setAttribute(basicPasswordKey,brm.getLoginPassword());
		    	users.appendChild(user);
		    }
		    String filename = Config.getApplicationPath() + File.separatorChar + "WEB-INF"+File.separatorChar + "conf"+ File.separatorChar + "users.conf";
		    writeXmlFile(doc,filename); 
	 }
	 
	 public void load() {
		  clearAllRoleMaps();
  	  	  String fileName = Config.getApplicationPath() + File.separatorChar + "WEB-INF"+File.separatorChar + "conf"+ File.separatorChar + "users.conf";
  	  	  String email = null;
		  String role = null;
		  String password = null;
		  DOMParser p = new DOMParser();
		  try {
			  p.parse(fileName);
		  } catch (IOException e) {
	       	  logger.info("could not read from users.conf. {fileName='"+fileName+"'}");
	       	  return;
	      } catch (Exception e) {
	    	  logger.error("failed to load information in users.conf. file is structured incorrectly. {fileName='"+fileName+"'}",e);
	    	  return;
	      }
	      Document doc = p.getDocument();
	      Element docEle = doc.getDocumentElement();
	      NodeList nl = docEle.getElementsByTagName("User");
		 if(nl != null && nl.getLength() > 0) {
				for(int i = 0 ; i < nl.getLength();i++) {
				  Element el = (Element)nl.item(i);
				  NamedNodeMap attrs = el.getAttributes();
			      int len = attrs.getLength();
		          for (int j=0; j<len; j++) {
		              Attr attr = (Attr)attrs.item(j);
		              
		              if (Compare.equalsIgnoreCase(attr.getNodeName(),basicUsernameKey))
		            	  email = attr.getNodeValue();
		              if (Compare.equalsIgnoreCase(attr.getNodeName(),basicRoleKey))
		            	  role = attr.getNodeValue();
		              if (Compare.equalsIgnoreCase(attr.getNodeName(),basicPasswordKey))
		            	  password = attr.getNodeValue();
		          }
		          if (email==null || password==null || role==null) {
		        	  logger.warn("failed to load basic authentication role mapping {email='"+email+"', role='"+role+"',password='<hidden>'}");
		       		  return;
		       	  }
		       	  try {
		       		  logger.debug("load basic authentication role mapping {email='"+email+"', role='"+role+"',password='<hidden>'}");
		       		  addRoleMap(role,email,password);
		       	  } catch (ConfigurationException ce) {
		       		  logger.error("could not load users in users.conf {fileName='"+fileName+"'}.",ce);
		       		  return;
		       	  }  
				}
	      }
     }
	 
	 public class BasicRoleMap extends RoleMap {

		String loginPassword;
		String username;
		
		public BasicRoleMap(String role, String username, String loginPassword) throws ConfigurationException {
			setRole(role);
			setUsername(username);
			setLoginPassword(loginPassword);
		}
		
	    public String getLoginPassword() {
	  	  return loginPassword;
	    }
	    
	    public void setLoginPassword(String loginPassword) {
	  	  this.loginPassword = loginPassword.trim();
	    }
	    
	    public void setUsername(String username) {
	    	this.username = username.trim();
	    }
	    
	    public String getUsername() {
	    	return username;
	    }
	}
    
}
