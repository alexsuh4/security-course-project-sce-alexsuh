

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class WebFrontEndClass
 */
public class WebFrontEndClass extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public WebFrontEndClass() {
        super();
        // TODO Auto-generated constructor stub
    }
    public void Serve(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
    	try{

    	Socket s=new Socket("localhost",12345);
    	BufferedReader fromClient= new BufferedReader( request.getReader());
    	PrintWriter toAppServer=new PrintWriter(s.getOutputStream(),true);
    	BufferedReader fromAppServer=new BufferedReader( new InputStreamReader (s.getInputStream()));
    	PrintWriter toClient= new PrintWriter (response.getWriter(),true);
    	
    	
    	
    	
    	
    	
    	String line;
    	//read from client
    	//collect commands
    	//client commands
    	Vector <String>commands=new Vector<String>();
    	//application server responses
    	Vector<String> responses=new Vector<String>();
    	while ((line=fromClient.readLine())!=null) 
    	{
    		//write to application server
    		commands.add(line);
    	}
    	
    	
    	
    	String appResponse="";
    	//send commands
    	for (String cmd:commands)
    	{
    		toAppServer.println(cmd);
    		//collect responses
    		//collect one response
    		do
        	{
    			try
    			{
    				//read line from app server
    				line=fromAppServer.readLine();
    				//if stream is dead break
	    			if (line==null)
	    				break;
	    			//ignore end of statement indicator
	        		if (!line.equalsIgnoreCase("..END_TRANSMISSION.."))
	        		{
	        			appResponse+=line+"\n";
	        		}
    			}
    			catch (IOException e)
    			{
    				/*	lost connection to applicatio serevr due to end connection
    				*	request from client side
    				*	cannot detect becouse encrypted
    				*	just set return to "..END_TRANSMISSION.."
    				*	and exit loop
    				*	
    				*/
    				line="..END_TRANSMISSION..";
    			}
        	}
        	while (!line.equalsIgnoreCase("..END_TRANSMISSION.."));
        	
    		
    		/*while ((line=fromAppServer.readLine())!=null) 
        	{
    			appResponse+=line+"\n";
        	}
        	*/
    		responses.add(appResponse);
        	appResponse="";
    	}
    	//almost done
    	//send responses to client
    	System.out.println("writing to client");
    	
    	for(String rspns: responses )
    	{
    		toClient.println(rspns);
    	}
    	System.out.println("done");
    	
    	toAppServer.close();
    	fromAppServer.close();
    	s.close();
    	}
    	catch (NullPointerException e)
    	{
    		e.printStackTrace();
    	}
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		Serve(request,response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		Serve(request,response);
	}
	
	
}
