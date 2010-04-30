package Logic;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import Protection.Protection;

/**
 * emulates a simple console
 * @author Alex Suchman
 *
 */
public class MyConsole {
	
	private InputStream in;
	private PrintWriter out;
	MyConsole(InputStream in,OutputStream out)
	{
		this.in=in;
		this.out=new PrintWriter(out,true);
	}
	/**
	 * prints a prompt , and read astring from input stream
	 * returns red string
	 * @param msg prompt for user
	 * @return string read
	 * @throws IOException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	public String read(String msg) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException
	{
		//write prompt
		write(msg);
		//read line
		String line =new BufferedReader(new InputStreamReader(in)).readLine();
		line=Decrypt(line);
		System.out.println("red "+line);
		return line;
	}
	
	/**
	 * writes a string to standard output
	 * @param str a string to output
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws UnsupportedEncodingException 
	 */
	public void write(String str) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException
	{
		//don't encrypt control messages
		if (!str.equalsIgnoreCase("..END_TRANSMISSION.."))
		{
			//encrypt string
			str=Encrypt(str);
		}
		//send string
		out.println(str);
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
	private String Encrypt(String msg) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException
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
	private String Decrypt(String msg) throws InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, IOException
	{
		//split result to lines (each line encrypted separately)
        String cryptLines[]=msg.split("\n");
        //decrypt each line separately
        for(int i=0;i<cryptLines.length;i++)
        	cryptLines[i]=Protection.Decrypt(Protection.getKey(), cryptLines[i]);
        //reassemble decrypted lines
        msg="";
        for(String res:cryptLines)
        	msg+=res+"\n";
        return msg;
	}
}
