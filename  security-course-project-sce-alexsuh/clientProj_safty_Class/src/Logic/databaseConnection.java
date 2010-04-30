package Logic;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public class databaseConnection {
	private String dbUserName;
	private String password;
	private String driverString;
	private String dbCatalog;
	private String DBURL="localhost";
	private dbTypes databaseType;
	public String getDBURL() 
	{
		return DBURL;
	}
	public void setDBURL(String dburl) 
	{
		DBURL = dburl;
	}
	public static enum dbTypes {MY_SQL,MS_SQL,POSTGRADE_SQL};
	private static Map<dbTypes,String> NameToDriverMapping=InitDBDriverMapping();
	private static Map<dbTypes,String> NameToURLSchemaMapping=InitUrlSchemaMapping();
	private static Map<dbTypes, String> InitDBDriverMapping() 
	{
		Map<dbTypes,String> result=new HashMap<dbTypes,String>();
		result.put(dbTypes.MY_SQL,"com.mysql.jdbc.Driver");
		result.put(dbTypes.MS_SQL,"com.microsoft.jdbc.sqlserver.SQLServerDriver");
		result.put(dbTypes.POSTGRADE_SQL,"org.postgresql.Driver");
		return result;
	}
	private static Map<dbTypes, String> InitUrlSchemaMapping() 
	{
		Map<dbTypes,String> result= new HashMap<dbTypes,String>();
		result.put(dbTypes.MY_SQL,"mysql");
		result.put(dbTypes.MS_SQL,"sqlserver");
		result.put(dbTypes.POSTGRADE_SQL,"postgresql");
		return result;
	}
	/**
	 * creates a new wrapper for a jdbc database connection
	 * @param dbType enum db type found under dbtypes
	 * @param userName user naem for connection
	 * @param initialCatalog database to connect to (casn be changed afterwards)
	 * @throws ClassNotFoundException
	 */
	databaseConnection(dbTypes dbType,String userName,String initialCatalog) throws ClassNotFoundException
	{
		driverString=NameToDriverMapping.get(dbType);
		Class.forName(driverString);
		dbUserName=userName;
		dbCatalog=initialCatalog;
		password="";
		databaseType=dbType;
	}
	/**
	 * 
	 * @param dbType enum db type found under dbtypes
	 * @param userName user naem for connection
	 * @param initialCatalog database to connect to (casn be changed afterwards)
	 * @param password password to connect to database
	 * @throws ClassNotFoundException
	 */
	databaseConnection(dbTypes dbType,String userName,String initialCatalog,String password) throws ClassNotFoundException
	{
		this(dbType,userName,initialCatalog);
		if (!password.equalsIgnoreCase(""))
			this.password=password;
	}
	/**
	 * gets database connection
	 * @return a database connection String 
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException
	{
		String connectString="";
		connectString+="jdbc:";
		connectString+=NameToURLSchemaMapping.get(databaseType)+"://";
		connectString+=DBURL;
		if (dbCatalog!="")
		{
			connectString+="/"+dbCatalog;
		}
		if (dbUserName!="")
		{
			connectString+="?user="+dbUserName;
		}
		if (password!="")
		{
			connectString+="&password="+password;
		}
        return DriverManager.getConnection(connectString);
	}
	
	public String getDbUserName() 
	{
		return dbUserName;
	}
	public void setDbUserName(String dbUserName) 
	{
		this.dbUserName = dbUserName;
	}
	public String getPassword() 
	{
		return password;
	}
	public void setPassword(String password) 
	{
		this.password = password;
	}
	public String getDriverString() 
	{
		return driverString;
	}
	public void setDriverString(String driverString) 
	{
		this.driverString = driverString;
	}
	public String getDbCatalog() 
	{
		return dbCatalog;
	}
	public void setDbCatalog(String dbCatalog) 
	{
		this.dbCatalog = dbCatalog;
	}
	
	
}
