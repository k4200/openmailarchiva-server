package com.stimulus.util;

import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.subethamail.smtp.util.Base64;
import com.stimulus.archiva.domain.Config;
import com.stimulus.archiva.exception.MessageStoreException;

public class Crypto {
	
    protected static Log logger = LogFactory.getLog(Crypto.class);
	
	public static String encryptPassword(String password) throws MessageStoreException {
 		try {
 	    		int iterationCount = 17;  
 	    		KeySpec keySpec = new PBEKeySpec(Config.getEncKey().toCharArray(), Config.getConfig().getSalt(), iterationCount);
             	Key key = SecretKeyFactory.getInstance(Config.getConfig().getPBEAlgorithm()).generateSecret(keySpec);
             	AlgorithmParameterSpec  paramSpec = new PBEParameterSpec(Config.getConfig().getSalt(), iterationCount);
             	Cipher cipher = Cipher.getInstance(key.getAlgorithm());
             	cipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
             	byte[] outputBytes = cipher.doFinal(password.getBytes("UTF-8"));
             	return Base64.encodeToString(outputBytes,false);
          } catch (java.security.NoSuchAlgorithmException e)	{
              throw new MessageStoreException("failed to locate desired encryption algorithm {algorithm='"+Config.getConfig().getPBEAlgorithm()+"'",logger);
          } catch (Exception e) {
              throw new MessageStoreException(e.toString(),e,logger);
          }
 	}
 	  public static String decryptPassword(String password)  throws MessageStoreException {
 		 try {
	    	int iterationCount = 17; 
	    	KeySpec keySpec = new PBEKeySpec(Config.getEncKey().toCharArray(), Config.getConfig().getSalt(), iterationCount);
          	Key key = SecretKeyFactory.getInstance(Config.getConfig().getPBEAlgorithm()).generateSecret(keySpec);
          	AlgorithmParameterSpec  paramSpec = new PBEParameterSpec(Config.getConfig().getSalt(), iterationCount);
          	Cipher cipher = Cipher.getInstance(key.getAlgorithm());
          	cipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
          	byte[] base64DecodedData = Base64.decodeFast(password);
          	byte[] outputBytes = cipher.doFinal(base64DecodedData);
          	return new String(outputBytes);
       } catch (java.security.NoSuchAlgorithmException e)	{
           throw new MessageStoreException("failed to locate desired encryption algorithm {algorithm='"+Config.getConfig().getPBEAlgorithm()+"'",logger);
       } catch (Exception e) {
           throw new MessageStoreException(e.toString(),e,logger);
       }  
 	  }
}
