import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.util.Vector;

import AppletMain.User;
import Protection.Protection;





/**
 * parse and execuire input
 * actualy involes only sending it to server 
 * and informing user if something goes wrong
 * @author Alex Suchman
 *
 */
public class Parser 
{
	private Session ssn;
	Parser (Session ssn)
	{
		this.ssn=ssn;
	}
	/**
	 * actions that client parser understands
	 * @author aaa
	 *
	 */
	public static enum actions
	{
		CONNECT,NOVALUE;
		public static actions toOperation(String str)
	    {
	        try
	        {
	            return valueOf(str.toUpperCase());
	        } 
	        catch (Exception ex) 
	        {
	            return NOVALUE;
	        }
	    }   
	};
	/**
	 * execute user request or transfer it to server
	 * @param cmd command to parse
	 * @return echo ot action status
	 */
	public String Parse(String cmd)
	{
		if (ssn.isConected())
		{
			
			System.out.println("connected , writing to socket");
			
			String response=write(cmd);
			//check for login information
			if (
					cmd.split("\\s+")[0].toLowerCase().equalsIgnoreCase("login") &&
					response.toLowerCase().indexOf("welcome,")>0
					)
			{
				ssn.setPassword(cmd.split("\\s+")[2]);
			}
			return response;
		}
		//retrieve the first word of command - the 
		//requested operation
		String commands[]=cmd.split(" ");
		String fcnName= commands[0].trim();
		
		switch	(actions.toOperation(fcnName))
		{
			case CONNECT:
				return connectToHost(cmd);
			default:
				return "you are not connected , try\nCONNECT <host> <port number>"; 
		}
	}
	/**
	 * writes string to server 
	 * echos user command
	 * @param cmd command to send to server 
	 * @return echoed string
	 * @throws IOException if transmit goes bad
	 */
	private String write(String cmd)  {
		PrintWriter pw;
		try 
		{
			System.out.println("writing to socket "+cmd);
			pw = new PrintWriter( ssn.getMySock().getOutputStream(), true);
			pw.println(Protection.Encrypt(cmd));
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			return "connection lost : "+e.getMessage();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "write to server failed : "+e.getMessage();
		}
		return cmd;
	}
/**
 * connect to host as requested by command (user)
 * @param cmd connection parameters sepewrated by spaces <host> <port>
 * @return status message
 */
	private String connectToHost(String cmd) 
	{
		String commands[]=cmd.split(" ");
		if (commands.length<3)
			return "usage CONNECT <host> <port number>";
		String host = commands[1];
		String port = commands[2];
		Socket sock;
		try 
		{
			sock = new Socket(host,Integer.parseInt(port));
			ssn.setMySock(sock);
			write("hello");
			return "connected to server at "+host+":"+port;
		} 
		catch (NumberFormatException e) 
		{
			return e.getMessage();
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (UnknownHostException e) 
		{
			return e.getMessage();
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (IOException e) 
		{
			return e.getMessage();
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
	}
	public void stop() 
	{
		ssn.stop();
	}
}
