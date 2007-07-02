package com.stimulus.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;


public class ConvertVolume {

	static String 	 saltStr;
	static String    passPhrase;
    static SecretKey decryptKey;
    static SecretKey encryptKey;
    static AlgorithmParameterSpec decryptParamSpec;
    static AlgorithmParameterSpec encryptParamSpec;
    static String hexits = "0123456789abcdef";

    public static void main(String[] param) {
        if (param.length<3) {
            System.out.println("\n\nUtility to Convert Volume Messages From DES to 3DES");
            System.out.println("Usage: ConvertVolume <salt> <passPhrase> <storeDirec tory>");
            System.out.println("Example: ConvertVolume e7150baa58927558 pas123! c:\\vol1\\store\n");
            System.out.println("WARNING: ORIGINAL FILES WILL BE OVERWRITTEN - USE AT YOUR OWN RISK");
            System.out.println("- Ensure that you have Java security policy files installed");
            System.out.println("- Ensure that your volume is backed up");
            return;
        }
        saltStr = param[0];
        passPhrase = param[1];
        System.out.println("salt:"+saltStr+" passPhrase:"+passPhrase+" storeDir:"+param[2]);
        convertVolume(new File(param[2]));
    }


	public static void initDecryptKey(String algorithm) throws Exception {
	        byte[] salt = fromHex(saltStr);
	        int iterationCount = 17;
	        // Create the key
	   try {
	        KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), salt, iterationCount);
	        decryptKey = SecretKeyFactory.getInstance(algorithm).generateSecret(keySpec);
	        decryptParamSpec = new PBEParameterSpec(salt, iterationCount);

	    } catch (java.security.NoSuchAlgorithmException e)	{
	        throw new Exception("failed to locate desired encryption algorithm:" + e.toString());
	    } catch (Exception e) {
	        throw new Exception(e.toString());
	    }
	}

	public static void initEncryptKey(String algorithm) throws Exception {
		        byte[] salt = fromHex(saltStr);
		        int iterationCount = 17;
		        // Create the key
		   try {
		        KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), salt, iterationCount);
		        encryptKey = SecretKeyFactory.getInstance(algorithm).generateSecret(keySpec);
		        encryptParamSpec = new PBEParameterSpec(salt, iterationCount);

		    } catch (java.security.NoSuchAlgorithmException e)	{
		        System.out.println("Please ensure that you have the Java security policy files installed");
		        throw new Exception("failed to locate desired encryption algorithm:" + e.getMessage());

		    } catch (Exception e) {
		        throw new Exception(e.toString());
		    }
	}

	public static InputStream getRawMessageInputStream(File messageFileName, boolean decompress, boolean decrypt)  throws Exception {


		 InputStream in = new BufferedInputStream(new FileInputStream(messageFileName));
		 Cipher dcipher = null;
		 if(decrypt) {
		     try {

		         dcipher = Cipher.getInstance(decryptKey.getAlgorithm());
		         dcipher.init(Cipher.DECRYPT_MODE, decryptKey, decryptParamSpec);
			   } catch (Exception e) {
				 System.out.println("Please ensure that you have the Java security policy files installed");
		         throw new Exception("failed to initialize cipher:"+e.getMessage());
			   }

		     in = new CipherInputStream(in,dcipher);
		 }
		 if(decompress)
		      in = new GZIPInputStream(in);
		  return in;

	}

   public static OutputStream getRawMessageOutputStream(File messageFileName,boolean compress, boolean encrypt) throws Exception  {

	   OutputStream os = new BufferedOutputStream(new FileOutputStream(messageFileName));
	   Cipher ecipher = null;
	   if (encrypt) {
		   try {
			   ecipher = Cipher.getInstance(encryptKey.getAlgorithm());
			   ecipher.init(Cipher.ENCRYPT_MODE, encryptKey, encryptParamSpec);
		   } catch (Exception e) {
			   throw new Exception("failed to initialize cipher:"+e.toString());
		   }
		   os = new CipherOutputStream(os,ecipher);
	   }

	   if (compress)
		   os = new GZIPOutputStream(os);

	   return os;
   }


	public static byte[] fromHex(String s) {
		s = s.toLowerCase();
		byte[] b = new byte[(s.length() + 1) / 2];
		int j = 0;
		int h;
		int nibble = -1;

		for (int i = 0; i < s.length(); ++i) {
			h = hexits.indexOf(s.charAt(i));
			if (h >= 0) {
				if (nibble < 0) {
					nibble = h;
				} else {
					b[j++] = (byte) ((nibble << 4) + h);
					nibble = -1;
				}
			}
		}

		if (nibble >= 0) {
			b[j++] = (byte) (nibble << 4);
		}

		if (j < b.length) {
			byte[] b2 = new byte[j];
			System.arraycopy(b, 0, b2, 0, j);
			b = b2;
		}

		return b;
	}

	public static void convertMessage(File file) {

	   InputStream in = null;
	   OutputStream out = null;
	   String fileName = "unknown";
	   try
	   {
 		   fileName = file.getCanonicalPath();
 		   if (!fileName.endsWith(".mrc"))
 		   	 return;
		   System.out.println("convert message "+fileName);
		   String strTempFileName = file.getAbsolutePath() + ".tmp";
		   File tempFile = new File(strTempFileName);
		   initDecryptKey("PBEWithMD5AndDES");
		   initEncryptKey("PBEWithMD5AndTripleDES");
		   in  = getRawMessageInputStream(file,true,true);
		   out = getRawMessageOutputStream(tempFile,true,true);
		   byte[] buf = new byte[1024];
		   int numRead = 0;
		   while ((numRead = in.read(buf)) >= 0) {
				out.write(buf, 0, numRead);
		   }
		   out.flush();
		   out.close();
		   in.close();

		   boolean deleted = file.delete();
		   if (deleted) {
			   tempFile.renameTo(new File(fileName));
	   	   } else
	   	     System.out.println("could not delete :"+fileName);
		} catch(Exception e) {
		  System.out.println("failed convert for "+fileName+". error: "+e.getMessage());
		  try {
			  if (in !=null)
					in.close();
			  if (out !=null)
					out.close();
		  } catch (Exception ignore) {};
 	    }
	}


	public static void convertVolume(File file) {
	   if(file.isDirectory()) {
		   File[] files = file.listFiles(new MRCFilter());
		   if(files != null) {
			   for(int i = 0; i < files.length; i++) {
				   convertVolume(files[i]);
			   }
		   }
	   	   return;
	   } else
	      convertMessage(file);
     }


	 public static class MRCFilter implements FilenameFilter {

		 public boolean accept(File dir, String name) {
			 try {
				 return name.endsWith(".mrc") || new File(dir.getCanonicalPath()+File.separator+name).isDirectory();
			 } catch (Exception e) { return false; }
		 }
	 }

}


