
/* Copyright (C) 2005-2009 Jamie Angus Band
 * MailArchiva Open Source Edition Copyright (c) 2005-2009 Jamie Angus Band
 * This program is free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation; either version
 * 3 of the License, or (at your option) any later version.
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

import com.stimulus.util.*;

import org.apache.xerces.parsers.DOMParser;
import org.subethamail.smtp.util.Base64;
import org.w3c.dom.*;
import com.stimulus.archiva.exception.*;
import com.stimulus.archiva.authentication.ADIdentity.ADRoleMap;
import com.stimulus.archiva.domain.*;
import com.stimulus.archiva.domain.Identity.RoleMap;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.xml.parsers.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import java.io.*;
import java.security.Key;
import java.security.MessageDigest;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;


public class BasicIdentity extends Identity implements Serializable,Props, Cloneable {

    private static final long serialVersionUID = 3166260447557364498L;
	protected static final String basicRoleKey  		= "role";
    protected static final String basicUsernameKey 	= "email";
    protected static final String basicPasswordKey 	= "password";

    public BasicIdentity() {
    }
	 @Override
	public void newRoleMap() throws ConfigurationException {
	   	 addRoleMap(new BasicRoleMap(Roles.ADMINISTRATOR_ROLE.getName(),"",""));
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
        File f = null;
    	try {
        	// if the disk is full we dont want to end up in a situation where we delete
    		// users.conf file
        	f = File.createTempFile("users_conf",".tmp");
            Source source = new DOMSource(doc);
            OutputStream os = new FileOutputStream(f);
            StreamResult result = new StreamResult(os);
            //Result result = new StreamResult(file);
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.setOutputProperty(OutputKeys.INDENT, "yes");
            xformer.transform(source, result);
            os.close();

         } catch (Exception io) {
        	 if (f!=null)
         		f.delete();
        	logger.error("failed to write users.conf:"+io.getMessage(),io);
        	return;
        }
        File newFile = new File(filename);
        newFile.delete();
        f.renameTo(newFile);

    }
    public void saveSettings(String prefix, Settings prop, String suffix) {
    	saveXMLFile();
    }

    public boolean loadSettings(String prefix, Settings prop, String suffix) {
    	loadXMLFile();
    	return true;
    }

    public void saveXMLFile() {
    	logger.debug("saving users.conf");
    	Document doc = createDomDocument();
	    Element users = doc.createElement("Users");
	    users.setAttribute("version","1.0");
	    doc.appendChild(users);
	    for (RoleMap roleMap : getRoleMaps()) {
	    	Element user = doc.createElement("User");
	    	BasicRoleMap brm = (BasicRoleMap)roleMap;
	    	user.setAttribute(basicUsernameKey,brm.getUsername());
	    	user.setAttribute(basicRoleKey,brm.getRole());
	    	try {
	    		String encryptedPassword = Crypto.encryptPassword(brm.getLoginPassword());
	    		user.setAttribute(basicPasswordKey,encryptedPassword);
	    	} catch (Exception e) {
	    		logger.error("failed to encrypt basic authentication password",e);
	    	}
	    	users.appendChild(user);
	    }
	    String filename = Config.getFileSystem().getConfigurationPath() + File.separatorChar + "users.conf";
	    writeXmlFile(doc,filename);
    }

	 public boolean loadXMLFile() {
		   logger.debug("loading users.conf");
		  clearAllRoleMaps();
		  String filename = Config.getFileSystem().getConfigurationPath() + File.separatorChar + "users.conf";
 	  	  String email = null;
		  String role = null;
		  String password = null;
		  DOMParser p = new DOMParser();
		  try {
			  p.parse(filename);
		  } catch (IOException e) {
	       	  logger.info("could not read from users.conf. {fileName='"+filename+"'}");
	       	return false;
	      } catch (Exception e) {
	    	  logger.error("failed to load information in users.conf. file is structured incorrectly. {fileName='"+filename+"'}",e);
	    	 return false;
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
		              if (Compare.equalsIgnoreCase(attr.getNodeName(),basicPasswordKey)) {
		            	if (!attr.getNodeValue().endsWith("=")) {
		      	    		password = attr.getNodeValue();
		      	    	} else {
		      	    	  try {
			      	    		String decryptPassword = Crypto.decryptPassword(attr.getNodeValue());
			      	    		password = decryptPassword;
			      	    	} catch (Exception e) {
			      	    		logger.error("failed to encrypt basic authentication password",e);
			      	    	}
		      	    	}
		              }
		          }
		          if (email==null || password==null || role==null) {
		        	  logger.warn("failed to load basic authentication role mapping {email='"+email+"', role='"+role+"',password='<hidden>'}");
		        	  return false;
		       	  }
		       	  try {
		       		  logger.debug("load basic authentication role mapping {email='"+email+"', role='"+role+"',password='<hidden>'}");
		       		  addRoleMap(role,email,password);
		       	  } catch (ConfigurationException ce) {
		       		  logger.error("could not load users in users.conf {fileName='"+filename+"'}.",ce);
		       		  return false;
		       	  }
				}
	      }
		  return true;
	 }


	 public class BasicRoleMap extends RoleMap implements Props, Cloneable  {

		String loginPassword;
		String username;

		public BasicRoleMap(String role, String username, String loginPassword) {
			setRole(role);
			setUsername(username);
			this.loginPassword = loginPassword;
		}

	    public String getLoginPassword() {
	  	  return loginPassword;
	    }

	    public void setLoginPassword(String loginPassword) {
	  	  	this.loginPassword = loginPassword.trim();
		  	try {
			     MessageDigest sha = MessageDigest.getInstance("SHA-1");
	     		byte[] input = sha.digest(ByteUtil.mergeByteArrays(loginPassword.getBytes("UTF-8"),Config.getConfig().getSalt()));
	     		this.loginPassword = Base64.encodeToString(input,false);

			} catch (Exception e) {
				logger.error("failed to setPassPhrase:"+e.getMessage(),e);
			}
	    }

	    public void setUsername(String username) {
	    	this.username = username.trim();
	    }

	    public String getUsername() {
	    	return username;
	    }

	    public void saveSettings(String prefix, Settings prop, String suffix) {

	    }
	    public boolean loadSettings(String prefix, Settings prop, String suffix) {
	    	return true;
	    }

	    public BasicRoleMap clone() {
	    	return new BasicRoleMap(role,username,loginPassword);
	    }
	}

	 		public BasicIdentity clone() {
	 			BasicIdentity basicIdentity = new BasicIdentity();
	 			for (RoleMap roleMap : getRoleMaps()) {
	 				basicIdentity.addRoleMap(((BasicRoleMap)roleMap).clone());
	 			}
	 			return basicIdentity;
	 		}

}
