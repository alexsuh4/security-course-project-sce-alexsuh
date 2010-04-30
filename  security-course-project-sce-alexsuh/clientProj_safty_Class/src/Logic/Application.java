package Logic;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * holds application shared resources
 * @author aaa
 *
 */
public class Application {
	private static Logger applicationLogger;
	/**
	 * initalize application logger
	 * @param _dbCon database connection to write logs to 
	 * @return initazlized application logger
	 */
	private static Logger initApplicationLogger(databaseConnection _dbCon) {
		Logger result=Logger.getLogger("com.AppSecureServer.log");
		SQLHandler handler=new SQLHandler(_dbCon);
		result.addHandler(handler);
		result.setUseParentHandlers(false);
		
		return result;
	}
	private static databaseConnection dbCon=initDBConnection();
	/**
	 * init connection to database
	 * @return a working databse connection
	 */
	private static databaseConnection initDBConnection()
	{
		
		
		databaseConnection result=null;
		
			
		HashMap<String,String> config=loadConfig();
		//set defaults
		String dbUserName="root";
		String dbCatalog="testDB";
		String dbPassword="";
		if (config!=null)
		{	
			//config exists
			//set values if exist
			//if not use defaults
			dbUserName=(config.get("database_username")!=null? config.get("database_username"):dbUserName);
			dbCatalog=(config.get("database_catalog")!=null? config.get("database_catalog"):dbCatalog);
			dbPassword=(config.get("database_password")!=null? config.get("database_password"):dbPassword);
		}
		try 
		{
			result = new databaseConnection(
					databaseConnection.dbTypes.MY_SQL, 
					dbUserName, 
					dbCatalog,
					dbPassword
					);
		} 
		catch (ClassNotFoundException e) 
		{
			result=null;
			e.printStackTrace();
		}
		applicationLogger=initApplicationLogger(result);
		applicationLogger.setLevel(Level.FINEST);
		
		return result;
	}
	/**
	 * loads application configuration
	 * @return returns configurations key-value pairs
	 */
	private static HashMap<String,String> loadConfig() 
	{
		HashMap<String,String> result=new HashMap <String,String>();
		BufferedReader reader=null;
		try 
		{
			reader=new BufferedReader(new FileReader("program_data/config.txt"));
			String line="";
			String tokens[];
			while((line=reader.readLine())!=null)
			{
				tokens=line.split("\\s+");
				result.put(
						tokens[0].trim().toLowerCase(), 
						tokens[1].trim().toLowerCase());
			}
			
		} 
		catch (FileNotFoundException e) 
		{
			Application.LogEvent(Level.SEVERE, "cannot load config file ,using defaults");
		} 
		catch (IOException e) 
		{			
			Application.LogEvent(Level.SEVERE, "error reading from config file, using defaults("+e.getMessage()+")");
		}
		finally
		{
			try 
			{
				reader.close();
			} 
			catch (IOException e) 
			{
				Application.LogEvent(Level.SEVERE, "error closing config file("+e.getMessage()+")");
			}
		}
		return result;
	}
	public static Logger getLogger()
	{
		return applicationLogger;
	}
	public static void LogEvent(Level lvl,String msg)
	{
		applicationLogger.log(lvl, msg);
	}
	public static Connection getDBConnection() throws SQLException
	{
		return dbCon.getConnection();
	}
	
}
