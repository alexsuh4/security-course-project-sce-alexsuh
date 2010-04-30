package Network;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;

import Logic.Application;
import Logic.Session;

/**
 * a simple TCP AES encrypted server
 * @author we_limited
 *
 */
public class NetServer {
	ServerSocket listen;
	private boolean isStop;
	public NetServer(int port) throws IOException
	{
		listen=new ServerSocket(port);
		isStop=false;
		Application.LogEvent(Level.INFO, "Server initated on port "+port);
	}
	public void run()
	{
		Socket s;
		Application.LogEvent(Level.INFO, "Server is up and running on port "+listen.getLocalPort());
		try 
		{
			while (!isStop)
			{				
				s=listen.accept();
				new Session(s);
			} 
		}
		catch (IOException e) 
		{
			Application.LogEvent(Level.SEVERE, "IO error on serevr , server will now stop ("+e.getMessage()+")");
		}
		finally
		{
			try 
			{
				listen.close();
			} 
			catch (IOException e) 
			{
				Application.LogEvent(Level.WARNING, "failed to close server socket. "+e.getMessage());
			}
		}
	}
}
