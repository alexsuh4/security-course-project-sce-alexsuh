import java.io.IOException;
import java.net.Socket;


public class Session {
	private Socket mySocket;
	private streamReader reader; 
	public boolean isConected() {
		return (mySocket!=null && mySocket.isConnected());
	}

	/**
	 * @return the mySocket
	 */
	public Socket getMySock() {
		return mySocket;
	}

	/**
	 * @param mySock the mySocket to set
	 * @throws IOException 
	 */
	public void setMySock(Socket mySock) throws IOException {
		this.mySocket = mySock;
		reader=new streamReader( mySock,System.out);
	}

	public void stop() 
	{
		System.err.println("stoppint session...");
		try 
		{
			mySocket.close();
		} 
		catch (IOException e) 
		{
			System.err.println("could not close socket , "+e.getMessage());
		}
		System.err.println("session stopped");
	}

}
