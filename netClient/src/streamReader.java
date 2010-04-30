import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import Protection.Protection;

/**
 * reads from stream , updates console.
 * @author Alex Suchman
 *
 */
public class streamReader extends Thread {
	private BufferedReader myReader;
	private PrintWriter out;
	private boolean isStop;
	streamReader(Socket mySocket,OutputStream outStream) throws IOException
	{
		myReader= new BufferedReader( new InputStreamReader( mySocket.getInputStream()));
		isStop=false;
		this.out=new PrintWriter(outStream ,true);
		start();
	}
	/**
	 * reads from a given socket and writes to a given stream
	 * until stop method is called or an IO exception
	 * is made
	 */
	public void run()
	{
		String msg="";
		try 
		{
			while (!isStop)
			{
				msg=read();
				out.println(msg);
			} 
		}
		catch (IOException e) 
		{
			System.err.println("connection lost "+e.getMessage());
		}
		catch (NullPointerException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.err.println("encrypted channel error "+e.getMessage());
		}
		finally
		{
			//not closing out , only the in stream
			//Because I assume out is stdout and I do not want to close it.
			try 
			{
				myReader.close();
			} 
			catch (IOException e) 
			{
				System.err.println("could not close stream "+e.getMessage());
			}
		}
		ConsoleApplication.stopApplication();
	}
	private String read() throws InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, IOException {
		String line="";
		if ((line=myReader.readLine())!=null)
			return Protection.Decrypt(line);
		else
			throw new IOException();
	}
}
