import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import Protection.Protection;

/**
 * a imple console that reads from InputStream and writes output to outputStream
 * @author Alex Suchman
 *
 */
public class Console {
	
	private static boolean isStop; 
	private static Parser p; 
	public static void run(InputStream in, OutputStream out)
	{
		String msg="";
		Session s=new Session();
		isStop=false;
		p=new Parser(s);
		
		try 
		{
			write(out, "Client Shell has started , type Connect <host> <port> to connect to server ");
			while (!isStop)
			{
				msg=p.Parse(read(in));
				msg=verCommand(msg);
				out.write(msg.getBytes());
			}
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static String verCommand(String cmd,String password)
	{
		//no message entered
		if (password.equalsIgnoreCase(""))
			return cmd;
		try 
		{
			//Verifiable command
			return cmd+" "+Protection.doDigest(cmd,password );
		} 
		catch (Exception e )
		{
			System.err.println("cannot created varifible message , sendig without verification!");
			return cmd;
		}
	}
	public static void write (OutputStream out,String msg) throws IOException
	{
		out.write(msg.getBytes());
	}
	/**
	 * convinivance method to read a line from Input Stream
	 * @param in an input stream
	 * @return a string red from stream
	 * @throws IOException if IO exception (error) accures during reading
	 */
	public static String read(InputStream in) throws IOException
	{
		return new BufferedReader(new InputStreamReader(in)).readLine();
	}
	public static void stop()
	{
		isStop=true;
		p.stop();
	}
}
