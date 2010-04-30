package Protection;

import java.io.*;
import java.math.BigInteger;
import java.security.*;

import java.util.Hashtable;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;


 /**
  * a library of methods for different security functionality
  * @author Alex Suchman
  *
  */
public class Protection {
	private static int MinPasswordLen=5;
	private static int MaxPasswordLen=30;
	/**
	 * returns a system generated password that conforms to policy
	 * no dictionary words,length between MinPasswordLen and  MaxPasswordLen 
	 * @return a legal string 
	 */
	public static String getLegalPassword()
	{
		int length;
		String newPassword="";
		//loop until legal password is found
		do
		{
			length=(int)(Math.random()*(MaxPasswordLen-MinPasswordLen))+MinPasswordLen;
			newPassword=generatePassword(length);
		}
		while (! isPasswordOK(newPassword));
		return newPassword;
		 
	}
	/**
	 * generate a random string of length specified by caller
	 * if length smaller then minPassLen or bigger from MaxpasswprdLen
	 * it weill be cut off to meet limits 
	 * @param length required length of password
	 * @return a random ASCII string
	 */
	private static String generatePassword(int length)
	{
		if(length<MinPasswordLen)
			length=MinPasswordLen;
		if (length>MaxPasswordLen)
			length=MaxPasswordLen;
		Random generator = new Random();
		byte passBytes[]=new byte[length];
		for (int i=0;i<length;i++)
		{
			passBytes[i]=(byte)(generator.nextInt(1000)%91+35);
		}
		String result=new String(passBytes);
		return result;
	}
	public static enum HashingTypes {SHA1,MD5};
	private static String getHashName(HashingTypes hType)
	{
		switch(hType)
		{	
			case SHA1:
				return "SHA";
			case MD5:
				return "MD5";
			default:
				return "SHA";
		}
	}
	/**
	 * returns a hashed string using SHA1 algorithm 
	 * @param str string to hash
	 * @return hashed string
	 * @throws NoSuchAlgorithmException if algorith hashing not supported
	 */
  public static String getHash(String str) throws NoSuchAlgorithmException
  {
	 return getHash(str,HashingTypes.SHA1);
  }
  /**
   * returns hash String using defined hashing function 
   * @param str string to hash
   * @param hType hashing algorithm
   * @return a shashed string husing hash algorithm specified
   * @throws NoSuchAlgorithmException if specified algorithm not supported
   */
  public static String getHash(String str,HashingTypes hType) throws NoSuchAlgorithmException
  {
	  String hashFuncName=getHashName(hType);
	  MessageDigest md = MessageDigest.getInstance(hashFuncName);
	  md.update(str.getBytes());
	  byte resultBytes[]=md.digest();
	  String result=new String(resultBytes);
	  return result;
  }
  
  private static Hashtable<String,Boolean> PassDictionary=Init_dictionary();
  /**
   * initlize passWordDictionary hash table
   * expects to find dictionary file
   * if non found will return null and dictionary cheking will not
   * be performed
   * @return common words hash table (dictionary) or null
   */
  private static Hashtable<String, Boolean> Init_dictionary() 
	{
		Hashtable<String,Boolean> result=new Hashtable<String,Boolean>();
		String dictPath="program_data/DICT.TXT";
		BufferedReader br;
		try 
		{
			br=new BufferedReader(new FileReader(new File(dictPath)));
		} 
		catch (FileNotFoundException e) 
		{
			System.err.println("no dictionary file, no dictionary checking for password will be made\n");			
			return null;
		}
		//if i'm here probably dictionary file do exists
		String word="";
		//fill hash table
		try {
			while( ( word=br.readLine() ) != null)
			{
				result.put(word, new Boolean(true));
			}
			//IO exception handling mess
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
	}
  /**
   * gets a string describing password policy
   * @return password policy in human readble format
   */
  public static String getPasswordPolicy()
  {
	  String result = String.format("Password should be no less then %d letter long and no more then %d letters long .\nIn addition avoid using names and common words.\n",MinPasswordLen,MaxPasswordLen);
	  return result;
  }
  /**
   * returns true if password is OK
   * (not in dictionary if exists and conforms to minimum and maximum legths)
   * @param password password in quistion that needs to be checked
   * @return true if password conforms to password policy or false otherwise
   */
  public static boolean isPasswordOK(String password)
  {
	  		//check if password length isd in limits
	  		if (
	  				password.length()>MaxPasswordLen || 
	  				password.length()< MinPasswordLen)
	  			return false;
	  		
	  		//check if dictionary exists and password in dictionary
			if (
					PassDictionary!=null && 
					PassDictionary.get(password)!=null)
				return false;
			return true;
  }
  
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
	public static boolean verifiy(String msg,String password) throws NoSuchAlgorithmException, UnsupportedEncodingException
	{
		String parts[]=msg.split("\\s+");
		String digested=parts[parts.length-1];
		msg="";
		for (int i=0;i<parts.length-1;i++)
			msg+=parts[i] +" ";
		msg.trim();
		return Verify(digested, msg, password);
	}
  
}