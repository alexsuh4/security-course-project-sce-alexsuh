package Logic;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import Network.NetServer;


public class programDriver {
	
/**
 * program entry point
 * 
 * @param args
 */
	public static void main(String args[])
	{

		try 
		{
			//default port
			int port=12345;
			
			try
			{
				//check if user provided different port
				if (args.length>1)
					port=Integer.parseInt(args[1]);
			}
			catch(NumberFormatException e)
			{
				//if parsing failed revert to default port
				port=12345;
			}
			NetServer mainServer=new NetServer(port);
			mainServer.run();
		} 
		catch (IOException e) 
		{
			Application.LogEvent(Level.INFO, "could not start server , application will now stop!"+e.getMessage());
		}
	}

}
