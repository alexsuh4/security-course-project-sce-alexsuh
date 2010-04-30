import java.io.IOException;


public class ConsoleApplication
{
	public static void main(String Args[])
	{
		Console.run(System.in,System.out);
		
	}

	public static void stopApplication() 
	{	
		Console.stop();
		try 
		{
			System.in.close();
		} catch (IOException e) 
		{
		}
		System.out.println("press any key to stop application");
	}
		
}
