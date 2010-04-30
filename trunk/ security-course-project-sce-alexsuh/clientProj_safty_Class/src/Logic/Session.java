package Logic;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * a simple session tracking class
 * hold detaiols about this session
 * @author Alex Suchman
 *
 */
public class Session extends Thread{
	private User myUser;
	private Socket mySocket;
	private MyConsole myCon;
	private boolean isStop;
	
	public User getMyUser() {
		return myUser;
	}
	public void setMyUser(User myUser) {
		this.myUser = myUser;
	}
	public MyConsole getMyCon() {
		return myCon;
	}
	public void setMyCon(MyConsole myCon) {
		this.myCon = myCon;
	}
	public boolean isStop() {
		return isStop;
	}
	public void setStop(boolean isStop) {
		this.isStop = isStop;
	}
	
	public Session(Socket mySocket) throws IOException
	{
		myUser=null;
		myCon=new MyConsole(mySocket.getInputStream(),mySocket.getOutputStream());
		isStop=false;
		this.mySocket=mySocket;
		log(Level.INFO, "accapted new connection from "+mySocket.getRemoteSocketAddress());
		start();
	}
	/**
	 * Session parsing loop
	 */
	public void run()
	{
		String cmd;
		String prompt;
		MyParser parser=new MyParser (this) ;
		try 
		{
			
			while(!isStop)
			{
				
					if (myUser!=null)
					{
						prompt=myUser.getUserName();
					}
					else
					{
						prompt="not logged in";
					}
					Application.LogEvent(Level.FINE, "waiting requests");
					cmd=myCon.read(prompt+">");
					Application.LogEvent(Level.FINE, "red '"+cmd+"'");
					myCon.write(parser.parse(cmd));
					myCon.write("..END_TRANSMISSION..");
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			Application.LogEvent(Level.SEVERE, e.getMessage());
			e.printStackTrace();
		} catch (InvalidKeyException e) 
		{
			Application.LogEvent(Level.SEVERE, e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (NoSuchAlgorithmException e) 
		{
			Application.LogEvent(Level.SEVERE, e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (NoSuchPaddingException e) 
		{
			Application.LogEvent(Level.SEVERE, e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IllegalBlockSizeException e) 
		{
			Application.LogEvent(Level.SEVERE, e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (BadPaddingException e) 
		{
			Application.LogEvent(Level.SEVERE, e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			try 
			{
				mySocket.close();
			} 
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void log(Level lev , String msg)
	{
		if (myUser!=null)
			Application.LogEvent(lev,myUser.getUserName()+":"+msg);
		else
			Application.LogEvent(lev,"no user :"+msg);
	}
}
