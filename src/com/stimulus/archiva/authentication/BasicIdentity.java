package com.stimulus.archiva.authentication;
import java.io.File;
import java.io.IOException;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.*;

import com.stimulus.archiva.exception.*;
import com.stimulus.archiva.authentication.ADIdentity.ADRoleMap;
import com.stimulus.archiva.domain.*;

public class BasicIdentity extends Identity {

    protected static final String basicRoleKey  		= "role";
    protected static final String basicUsernameKey 	= "email";
    protected static final String basicPasswordKey 	= "password";
    
	 public void newRoleMap() throws ConfigurationException {
	   	 addRoleMap(new BasicRoleMap(ROLES.get(0),"",""));
	 }
	 
	 public void addRoleMap(String role, String username, String password) throws ConfigurationException {
	    	addRoleMap(new BasicRoleMap(role,username, password));
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
		              if (attr.getNodeName().equalsIgnoreCase(basicUsernameKey))
			  			email = attr.getNodeValue();
			  		  if (attr.getNodeName().equalsIgnoreCase(basicRoleKey))
	 		  			role = attr.getNodeValue();
			  		  if (attr.getNodeName().equalsIgnoreCase(basicPasswordKey))
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
