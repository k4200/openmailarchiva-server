import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;


public class DecryptMessage {

	static String salt;
    static SecretKey key;
    static AlgorithmParameterSpec paramSpec;
    static String hexits = "0123456789abcdef";

    public static void main(String[] param) {

        if (param.length<4) {
            System.out.println("Utility to Decrypt MailArchiva Message");
            System.out.println("Usage: DecryptMessage salt passPhrase sourceFile outputFile");
            System.out.println("Ensure that you have Java security policy files installed");
            return;
        }

        InputStream in = null;
        OutputStream out = null;
        try
        {
	    	initKeys(param[0],param[1]);

            in = getRawMessageInputStream(param[2], true, true);
            out = getRawMessageOutputStream(param[3],true,true);
            byte[] buf = new byte[1024];
            int numRead = 0;
            while ((numRead = in.read(buf)) >= 0) {
                 out.write(buf, 0, numRead);
             }
         } catch(Exception e)
         {
           System.out.println("error occurred:"+e.getMessage());
         } finally {
         	  try
                 {

                 	if (in !=null)
                 		in.close();
                     if (out !=null) {
                     	out.flush();
                         out.close();
                     }
                 }
                 catch(IOException ioe) { }
         }
    }


	public static void initKeys(String saltStr, String passPhrase) throws Exception {
	        byte[] salt = fromHex(saltStr);
	        String algorithm = "PBEWithMD5AndTripleDES";
	        int iterationCount = 17;
	        // Create the key
	   try {
	        KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), salt, iterationCount);
	        key = SecretKeyFactory.getInstance(algorithm).generateSecret(keySpec);

	        paramSpec = new PBEParameterSpec(salt, iterationCount);

	    } catch (java.security.NoSuchAlgorithmException e)	{
	        throw new Exception("failed to locate desired encryption algorithm:" + e.toString());
	    } catch (Exception e) {
	        throw new Exception(e.toString());
	    }
	}

	public static InputStream getRawMessageInputStream(String messageFileName, boolean decompress, boolean decrypt)  throws Exception {


		 InputStream in = new BufferedInputStream(new FileInputStream(messageFileName));
		 Cipher dcipher = null;
		 if(decrypt) {
		     try {

		         dcipher = Cipher.getInstance(key.getAlgorithm());
		         dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
			   } catch (Exception e) {
		         throw new Exception("failed to initialize cipher:"+e.toString());
			   }

		     in = new CipherInputStream(in,dcipher);
		 }
		 if(decompress)
		      in = new GZIPInputStream(in);
		  return in;

	}

	   public static OutputStream getRawMessageOutputStream(String messageFileName,boolean compress, boolean encrypt) throws Exception  {

	       OutputStream os = new BufferedOutputStream(new FileOutputStream(messageFileName));
	       Cipher ecipher = null;
	       if (encrypt) {
	           try {
	               ecipher = Cipher.getInstance(key.getAlgorithm());
	               ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
	           } catch (Exception e) {
	        	   throw new Exception("failed to initialize cipher:"+e.toString());
	           }
	           os = new CipherOutputStream(os,ecipher);
	       }

	       if (compress)
	           os = new GZIPOutputStream(os);

	       return os;
	   }


		private static byte[] fromHex(String s) {
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

}
