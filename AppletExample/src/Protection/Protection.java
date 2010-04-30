package Protection;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.KeySpec;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import sun.security.util.BigInt;
 /**
  * a library of methods for different securety functionality
  * @author Alex Suchman
  *
  */
public class Protection {

  
	 /**
	   * Encryption reraled methods
	   */
	  
	  //constant key used for communication
	  private static String KEY="mySuperseCretKey";
	  //retuns key
	  public static String getKey()
	  {
		  return KEY;
	  }
	  /**
	   * Encrypts a string using AES method,
	   * @param password a string to be converted to key
	   * @param plain plain text to encrypt
	   * @return base64 encoded encrypted data
	   * @throws UnsupportedEncodingException
	   * @throws NoSuchAlgorithmException
	   * @throws InvalidKeyException
	   * @throws IllegalBlockSizeException
	   * @throws BadPaddingException
	   * @throws NoSuchPaddingException
	   */
	  public static String Encrypt(String password,String plain) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException
	  {
		  Cipher cipher=Cipher.getInstance("AES");
		  byte[] rawPassword=getKey(password);
		  SecretKeySpec skeySpec = new SecretKeySpec(rawPassword, "AES");;
		  cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		  byte[] cryptBytes=cipher.doFinal(plain.getBytes());
		  String cryptString=Base64.encodeBytes(cryptBytes);
		  return cryptString;
	  }
	  /**
	   * Decrypt an AES encrypted base64 encoded String
	   * @param password password used to generate key used for encryption
	   * @param crypt encrypted data encoded in BASE64  
	   * @return  plain text
	   * @throws NoSuchAlgorithmException
	   * @throws InvalidKeyException
	   * @throws IllegalBlockSizeException
	   * @throws BadPaddingException
	   * @throws NoSuchPaddingException
	   * @throws IOException
	   */
	  public static String Decrypt(String password,String crypt) throws NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, IOException
	  {
		  Cipher cipher=Cipher.getInstance("AES");
		  byte[] rawPassword=getKey(password);
		  SecretKeySpec skeySpec = new SecretKeySpec(rawPassword, "AES");;
		  cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		  
		  byte[] cryptBytes=Base64.decode(crypt);
		  byte[] plainBytes=cipher.doFinal(cryptBytes);
		  
		  return new String(plainBytes);
	  }
	  /**
	   * generates a byte array size 16 to act as a key for AES algorithm
	   * (8*16=128) as a 128 bit key
	   * @param key a String to turn into a key
	   * @return byte array to be used as a key specification
	   * @throws UnsupportedEncodingException
	   * @throws NoSuchAlgorithmException
	   */
	  public static byte[] getKey(String key) throws UnsupportedEncodingException, NoSuchAlgorithmException
	  {
		  byte bytes[]=key.getBytes("UTF-8");
		  MessageDigest md = MessageDigest.getInstance("SHA");
		  
		  md.update(bytes);
		  byte digested[]=md.digest();
		  
		  int PasswordSize=16;
		  byte rawPassword[]=new byte[PasswordSize];
		  for (int i=0;i<PasswordSize;i++)
			  rawPassword[i]=digested[i];
		  return rawPassword;
	  }
	  
	  /**
		 * break message to lines
		 * encrypt each line separetly
		 * reassemble
		 * @param msg message to encrypt
		 * @return encrypted lines
		 * @throws NoSuchPaddingException 
		 * @throws BadPaddingException 
		 * @throws IllegalBlockSizeException 
		 * @throws NoSuchAlgorithmException 
		 * @throws UnsupportedEncodingException 
		 * @throws InvalidKeyException 
		 */
		public static String Encrypt(String msg) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException
		{
			//break message batch to individual commands
			//seperated by newline (\n = break line + line feed)
			String commands[]=msg.split("\n");

			//encrypt each command
			for(int i=0;i<commands.length;i++)
				commands[i]=Protection.Encrypt(Protection.getKey(), commands[i]);
			
			//Reconstruct message
			msg="";
			for (String cmd:commands)
				msg+=cmd+"\n";
			return msg;
		}
		/**
		 * break message to lines
		 * decrypte each line eparetly
		 * return reassembled plain text
		 * @param msg message to decrypt
		 * @return a reassembled plain text
		 * @throws InvalidKeyException
		 * @throws NoSuchAlgorithmException
		 * @throws IllegalBlockSizeException
		 * @throws BadPaddingException
		 * @throws NoSuchPaddingException
		 * @throws IOException
		 */
		public static String Decrypt(String msg) throws InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, IOException
		{
			
			//split result to lines (each line encrypted separately)
	        String cryptLines[]=msg.split("\r\n|\r|\n");
	        //decrypt each line separately
	        for(int i=0;i<cryptLines.length;i++)
	        {
	        	cryptLines[i]=Protection.Decrypt(Protection.getKey(), cryptLines[i]);
	        	//System.out.println("decrypted line "+i+" out of "+cryptLines.length);
	        	//System.out.println(cryptLines[i]);
	        }
	        //reassemble decrypted lines
	        msg="";
	        for(String res:cryptLines)
	        	msg+=res+"\n";
	        return msg;
		}
		/**
		 * returns message digest of a message an a password
		 * @param msg message
		 * @param password password 
		 * @return
		 * @throws NoSuchAlgorithmException
		 * @throws UnsupportedEncodingException
		 */
		public static String doDigest(String msg,String password) throws NoSuchAlgorithmException, UnsupportedEncodingException
		{
			 MessageDigest md = MessageDigest.getInstance("SHA");
			 //remove white spaces
			 String parts[]=msg.split("\\s+");
			 for(String part:parts)
				 md.update(part.getBytes("UTF-8"));
			 md.update(password.getBytes("UTF-8"));
			 return Base64.encodeBytes(md.digest());
		}
		/**
		 * tests if a given digested hash matches actual message+pasword hash 
		 * @param digested
		 * @param msg message
		 * @param password password
		 * @return
		 * @throws NoSuchAlgorithmException
		 * @throws UnsupportedEncodingException
		 */
		public static boolean Verify(String digested,String msg,String password) throws NoSuchAlgorithmException, UnsupportedEncodingException
		{
			 String testDigested=doDigest(msg, password);
			 return digested.equalsIgnoreCase(testDigested);
		}
}