package Logic;
/**
 * defines a user in the system
 * @author Alex Suchman
 *
 */
public class User {
	public static enum Permissions {USER,ADMIN}; 
	private Permissions myPerm;
	private boolean needChangePassword=false;
	private String password;
	public boolean isNeedChangePassword() {
		return needChangePassword;
	}
	public void setNeedChangePassword(boolean needChangePassword) {
		this.needChangePassword = needChangePassword;
	}
	public Permissions getMyPerm() {
		return myPerm;
	}
	public void setMyPerm(Permissions myPerm) {
		this.myPerm = myPerm;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	private String userName;
	public User(String userName,Permissions myPerm)
	{
		this.userName=userName;
		this.myPerm=myPerm;
	}
	public void setByString(String desc)
	{
		String tok[]=desc.split(" ");
		userName=tok[0];
		needChangePassword=Boolean.parseBoolean(tok[1]);
		myPerm=Permissions.valueOf(tok[2]);
	}
	//@overrides
	public String toString()
	{
		return userName+" "+" "+needChangePassword+" "+myPerm;
	}
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
}
