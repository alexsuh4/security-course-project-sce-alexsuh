package Logic;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;

import Protection.Protection;






/**
 * the parser hadnles parsing and executing user commands
 * @author Alex Suchman
 *
 */
public class MyParser {
	
	private Session currentSession;
	public MyParser(Session mySession)
	{
		this.currentSession=mySession;
	}
	
	/**
	 * describes availble System operations
	 * @author Alex Suchman
	 *
	 */
	private static enum operation	{
		INSERT,DELETE,UPDATE,RENAME,GET,CREATEUSER,CHANGEPSWD,NOVALUE,HELP,BYE;
		public static operation toOperation(String str)
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
	 * parses user input and performs desired operation
	 * @param cmd requested operation
	 * @return operation result description
	 */
	public String parse(String cmd)
	{
		
		String commands[]=cmd.split("\\s+");
		//if no commands do nothing
		if (commands.length<1)
			return "";
		//test if user requested disconnection
		if (commands[0].trim().toUpperCase().equalsIgnoreCase(operation.BYE.toString()))
		{
			return new String(doLogout(cmd));
		}
		//test if user is logged in
		if (currentSession.getMyUser()==null)
		{
			//if not logged in
			try {
				String msg=doLogin(cmd);
				currentSession.log(Level.INFO, ",not logged in ,user typed "+cmd);
				return  msg;
			} catch (NoSuchAlgorithmException e) {				
				currentSession.log(Level.SEVERE,e.getMessage()+",user input "+cmd);
				return e.getMessage();
			} catch (SQLException e) {
				currentSession.log(Level.SEVERE,e.getMessage()+",user input "+cmd);
				return e.getMessage();
			}
			
		}
		//test if users is forced to change password
		if (currentSession.getMyUser().isNeedChangePassword())
		{
			return hadnleForcedPasswordChange(cmd);
		}
		//user is logged in , parse normal mode
		String result;
		//retrieve the first word of command - the 
	
		String fcnName= commands[0].trim();
		
		
		switch	(operation.toOperation(fcnName))
		{
			case CREATEUSER:
				//handle creatye user request
				result=new String(handleCreateUser(cmd));
				break;
			case CHANGEPSWD:
				result=handleChangePassword(cmd);
				break;
			case HELP:
				result=new String(showHelp(cmd));
				break;
			default:
				result=new String("you requested: "+cmd +" , not implemented yet");
		}
		return result;
	}
	/**
	 * handle stopping session
	 * @param cmd command initiated logggin out 
	 * (not realy needed , maybe for future implementaions)
	 * @return status message
	 */
	private String doLogout(String cmd) {
		if (currentSession.getMyUser()!=null)
			currentSession.log(Level.INFO, currentSession.getMyUser().getUserName()+" logged out");
		else
			currentSession.log(Level.INFO, "unregistered user disconnected");
		currentSession.setStop(true);
		currentSession.setMyUser(null);
		return "byebye";
	}
	/**
	 * hanldes user force of password change (especialy for new users
	 * @param cmd command parameters
	 * @return status String
	 */
	private String hadnleForcedPasswordChange( String cmd) 
	{
		String commands[]=cmd.split("\\s+");
		String fcnName= commands[0].trim();
		if (operation.toOperation(fcnName)==operation.CHANGEPSWD)
		{
			return handleChangePassword(cmd);
		}
		return new String("you are currently using a temporary password.\n" +
				"You must change it now." +
				"\nuse " +
				"\nCHANGEPSWD <new_password> to select a new password" +
				"\nor" +
				"\nCHANGEPSWD (with no parameters) to let the system select a password for you" +
				"\n Password Policy :\n==========" +
				"\n"+Protection.getPasswordPolicy());
	}

	/**
	 * will list availble (even not yet implemented ) commands
	 * @param cmd commad string 
	 * @return
	 */
	private String showHelp( String cmd) {
		String result="";
		result+="simple securety project (2010 , Alex Suchman , SCE collage)\n";
		result+="\navailbe commands\n";
		
		operation operations[] = operation .values(); 
		for(operation opr : operations) 
			result+="\n"+opr.toString();
		result+="\nprint command name for a short description(carefull , some commands will be activated without parameters at all)\n";
		return result;
	}
	/**
	 * handles changeing password 
	 * @param cmd requested command
	 * @return 
	 */
	private String handleChangePassword(String cmd)
	{
		String commands[]=cmd.split("\\s+");
		int parmNum=commands.length;
		boolean isVerified=false;
		try 
		{
			isVerified=Protection.verifiy( cmd,currentSession.getMyUser().getPassword() ) ;
			if (!isVerified)
				throw new Exception("could not verify message!");
			Application.LogEvent(Level.FINE, "User "+currentSession.getMyUser().getUserName()+" succesfuly verified");
			switch(parmNum)
			{
				case 4:
					//is admin ?
					if (currentSession.getMyUser().getMyPerm()!=User.Permissions.ADMIN)
					{
						currentSession.log(Level.WARNING, currentSession.getMyUser().getUserName()+" (not admin) has attempted to chasnge other users password ");
						return new String("sorry , you have no permission to do that.\nplease contact system admin");
					}
					return doChangePswd(commands[1], commands[2]);
				case 3:
					return doChangePswd(commands[1]);
				case 2:
					return doChangePswd();
				default:
					return new String("\nchange password:\nusage:\nchangePswd <user_name> <password>\nnchangePswd <user_name>\nchangePswd");
			}
		}
		catch(SQLException e)
		{
			return "failed to change password.\n"+e.getMessage();
		}
		catch (Exception e)
		{
			Application.LogEvent(Level.SEVERE, "could not verify message , request rejected.\noriginal cmd="+cmd+"\nfrom user="+currentSession.getMyUser().getUserName()+"\n"+e.getMessage());
			return e.getMessage();
		}
	}
	/**
	 * handles loggin in
	 * @param cmd
	 * @return operation status String
	 * @throws NoSuchAlgorithmException 
	 * @throws SQLException 
	 */
	private String doLogin(String cmd) throws NoSuchAlgorithmException, SQLException {
		
		String commands[]=cmd.split("\\s+");
		String fcnName= commands[0];
		if (!fcnName.toUpperCase().equalsIgnoreCase("LOGIN")||commands.length<3)
		{
			return new String("you are not logged in, try:\n\nlogin <your-user-name> <your-password>\n");
		}
		
		
				
		String userName=commands[1];
		String password=commands[2];
		//validate user
		Connection con = Application.getDBConnection();
		String sqlQry="SELECT * FROM Users WHERE USER_NAME=? AND PASSWORD=SHA1(?)";
		PreparedStatement authoUsr=con.prepareStatement(sqlQry);
		authoUsr.setString(1, userName);
		authoUsr.setString(2, password);
		ResultSet rs=authoUsr.executeQuery();
		//no such user with password
		if (!rs.next())
		{
			currentSession.log(Level.WARNING, "failed login attempt to username "+userName);
			return new String("wrong user or password!!");
		}
		
		boolean is_admin=rs.getBoolean("IS_ADMIN");
		boolean is_forced_change_password=rs.getBoolean("IS_NEED_CHANGE_PASSWORD");
		con.close();//close database connection
		User.Permissions usrPerm;
		if (is_admin)
			usrPerm=User.Permissions.ADMIN;
		else
			usrPerm=User.Permissions.USER;
		
		User newUser=new User(userName, usrPerm);
		newUser.setNeedChangePassword(is_forced_change_password);
		newUser.setPassword(password);
		currentSession.setMyUser(newUser);
		
		currentSession.log(Level.INFO, userName+" succesfully logged in");
		//if password needs to be changed
		if(is_forced_change_password)
			return hadnleForcedPasswordChange(cmd);
		return "Welcome, "+newUser.toString();
	}
	/**
	 * accapts request to create user and command and redirects to approptiate
	 * function
	
	 * @param cmd command string
	 * @return operation result string
	 */
	private String handleCreateUser(String cmd)
	{
		boolean isVerified;
		try
		{
			isVerified=Protection.verifiy( cmd,currentSession.getMyUser().getPassword() ) ;
			if (!isVerified)
				throw new Exception("could not verify message!");
		}
		catch (Exception e)
		{
			Application.LogEvent(Level.SEVERE, "could not verify message , request rejected.\noriginal cmd="+cmd+"\nfrom user="+currentSession.getMyUser().getUserName()+"\n"+e.getMessage());
			return e.getMessage();
		}
		
		Application.LogEvent(Level.FINE, "User "+currentSession.getMyUser().getUserName()+" succesfuly verified");
		
		//is admin ?
		if (currentSession.getMyUser().getMyPerm()!=User.Permissions.ADMIN)
		{
			currentSession.log(Level.WARNING,currentSession.getMyUser().getUserName()+" (not admin) has attempted to create a new user ");
			return "sorry , you have no permission to do that.\nplease ontact system administarator";
		}
		String commands[]=cmd.split("\\s+");
		int parmNum=commands.length;
		String result="";
		
		switch(parmNum)
		{
			case 3:
				result=doCreateUser(commands[1]);
				break;
			case 4:
				result=doCreateUser(commands[1],commands[2]);
				break;
			default:
				result="create user .\nusage createUser user_name [password]\nusername - user name of new user \npassword(optional) - password for new user";
		}
		
		return result;
	}
	/**
	 * create a new user 
	 * @param userName user name
	 * @param password password
	 * @return operation result string 
	 */
	private String doCreateUser(String userName,String password) 
	{
		try {
			if (!Protection.isPasswordOK(password))
			{
				return "you password is invalid\n"+Protection.getPasswordPolicy();
			}
			Connection con=Application.getDBConnection();
			int isAdmin=0;
			String sqlCmd="INSERT INTO users(USER_NAME,PASSWORD,IS_ADMIN,IS_NEED_CHANGE_PASSWORD) VALUES (?,SHA(?),?,1)";
			
			PreparedStatement insertUser=con.prepareStatement(sqlCmd);
			insertUser.setString(1, userName);
			InputStream is = new ByteArrayInputStream( password.getBytes() );
			insertUser.setBinaryStream(2, is, password.getBytes().length);
			insertUser.setInt(3, isAdmin);
			insertUser.execute();
			con.close();
		} catch (SQLException e) {
			currentSession.log(Level.SEVERE,e.getMessage()+",in createUser <userName , Password>");
			return "user with name "+userName+" already exists please try another ";
		}
		currentSession.log(Level.INFO," new user("+userName+") has been created ");
	    return "created new user\nusername = "+userName+"\npassword = "+password;
	}
	/**
	 * create a new user with system defined password
	 * @param userName usewr name 
	 * @return operation result string
	 */
	private String doCreateUser(String userName) 
	{
		String newPass=Protection.getLegalPassword();
		return doCreateUser(userName,newPass);
	}
	/**
	 * change password for for another user
	 * access to admin level only 
	 * @param userName user name whose password we wish to change
	 * @param new_password new password
	 * @return operation result string
	 * @throws SQLException if database error has accured
	 */
	private String doChangePswd ( String userName,String new_password) throws SQLException
	{
		if (!Protection.isPasswordOK(new_password))
		{
			//password not good , inform user , no changes are made
			return Protection.getPasswordPolicy();
		}
		//update password
		
		Connection con=Application.getDBConnection();
		String sqlCmd="UPDATE users SET PASSWORD=SHA(?) ,IS_NEED_CHANGE_PASSWORD=? WHERE USER_NAME=?";
		
		PreparedStatement prstmnt=con.prepareStatement(sqlCmd);
		prstmnt.setString(1, new_password);
		prstmnt.setBoolean(2, false);
		prstmnt.setString(3, userName);
		
		
		if (prstmnt.executeUpdate()>0)
		{
			currentSession.log(Level.INFO,"password for user "+userName+" succesfult changed to "+new_password);
			return "password for user "+userName+" succesfult changed to "+new_password;
		}
		else
		{
			currentSession.log(Level.INFO,"could not change password .please check datyabase and logs");
			return "could not change password .please check datyabase and logs ";
		}		
	}
	/**
	 *	change password of currently logged-in user
	 * @param new_password new password
	 * @return operation result string
	 * @throws SQLException if database errors accure
	 */
	private String doChangePswd (String new_password) throws SQLException
	{
		return doChangePswd(currentSession.getMyUser().getUserName(), new_password);
	}
	/**
	 * change password of current user to system generated password
	 * @return operation status String
	 * @throws SQLException if update went wrong
	 */
	private String doChangePswd () throws SQLException
	{
		return doChangePswd (Protection.getLegalPassword()); 
	}
	
	/**
	 * handles insert
	 * does not implemented yet
	 * @param path
	 * @param filename
	 * @param key
	 */
	private void doInsert(String path, String filename,String key)
	{
		
	}
	/**
	 * handles delere
	 * @param filename
	 */
	private void doDelete(String filename)
	{
		
	}
	/**
	 * handles update
	 * @param filename
	 * @param path
	 * @param key
	 */
	private void doUpdate(String filename,String path,String key)
	{
		
	}
	/**
	 * handles rename
	 * @param oldFilename
	 * @param newFilename
	 */
	private void doRename(String oldFilename,String newFilename)
	{
		
	}
	/**
	 * handles get
	 * @param filename
	 * @param path
	 * @param key
	 */
	private void doGet(String filename,String path,String key)
	{
		
	}
}
