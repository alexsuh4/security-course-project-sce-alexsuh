import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import Protection.Protection;







/**
 * browser front end for connection to 
 * Application server through a web Server
 * over HTTP
 * all communication Encrypted using AES algorithm
 * @author we_limited
 *
 */
@SuppressWarnings("serial")
public class AppletMain extends JApplet implements ActionListener {
	static OuterPanel mainPanel;
	static LoginPanel lp;
	static MainMenuPanel mm;
	static AdminCreateUserPanel acup;
	static ChangePasswordPanel chp;
	static JLabel versionLabel;
	Session currentSession;
	//apply current user policy
	Vector<applyPolicy> controls=new Vector<applyPolicy>();;
    //Called when this applet is loaded into the browser.
    public void init() 
    {
    	mainPanel=new OuterPanel();
        lp=new LoginPanel();
        lp.submitButton.addActionListener(this);
        mm=new MainMenuPanel();
        mm.adminButton.addActionListener(this);
        mm.changePasswordButton.addActionListener(this);
        mm.deleteFileButton.addActionListener(this);
        mm.getFileButton.addActionListener(this);
        mm.insertFileButton.addActionListener(this);
        mm.renameFileButton.addActionListener(this);
        mm.updateFileButton.addActionListener(this);
        acup=new AdminCreateUserPanel();
        acup.cancelButton.addActionListener(this);
        acup.submitButton.addActionListener(this);
        chp=new ChangePasswordPanel();
        chp.cancelButton.addActionListener(this);
        chp.submitButton.addActionListener(this);
        
        versionLabel=new JLabel("ver. 5.6");
        currentSession=new Session();
        
        controls.add(mainPanel);
        controls.add(lp);
        controls.add(mm);
        controls.add(acup);
        controls.add(chp);
        
        
        //Execute a job on the event-dispatching thread; creating this applet's GUI.
        try 
        {
            SwingUtilities.invokeAndWait(new Runnable() 
            {
                public void run() 
                {                	
                    mainPanel.setLayout(new BorderLayout());
                    mainPanel.add(lp,BorderLayout.CENTER);
                    mainPanel.add(versionLabel,BorderLayout.SOUTH);
                    add(mainPanel);
                    mainPanel.validate();
                }
            });
        } 
        catch (Exception e) 
        {
            System.err.println("createGUI didn't complete successfully");
        }
    }
    
    /**
	 * like Ajax , only without Asynchronous mode
	 * sends a string to host as a stream
	 * using POST method over HTTP
	 * @param msg - what to send
	 * @return response from server
	 */
	private String send(String msg) 
	{
		String result="";
		URL docBase=null;
		HttpURLConnection con=null;
		try 
		{
			//get address of server host
			docBase=new URL(getDocumentBase().toString()+"/WebFrontEndClass");//getDocumentBase();
			//setup connection
			con = (HttpURLConnection) docBase.openConnection();
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setReadTimeout(10000);
			
			PrintWriter out= new PrintWriter( con.getOutputStream(),true);
			con.connect();
			//encrypt message
			msg=Protection.Encrypt(msg);
			out.println(msg);
			
			BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
	        String temp="";
	        //reading response
	        while ( (temp=rd.readLine())!=null )
	        {
	        	result+=temp+"\n";
	        }
	        //decrypt message
	        result=Protection.Decrypt(result);
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			result="io error: "+e.getMessage();
		}
		catch (Exception e)
		{
			result=e.getMessage();
			e.printStackTrace();
			//probably encryption related
		}
		finally
		{
			con.disconnect();
		}
        return result;
	}
	
	/**
	 * Various operations supported(or to be supported) by the system)
	 * @author aaa
	 *
	 */
	private static enum Operations	
	{
		LOGIN,
		INSERT,
		DELETE,
		UPDATE,
		RENAME,
		GET,
		CREATEUSER,
		CHANGEPSWD,
		NOVALUE,
		HELP,
		BYE;
	};
		
	/**
	 * describes varius screen sytem can be in
	 * @author Alex Suchman
	 *
	 */
	private static enum modes
	{
		NOT_LOGGED_IN,
		MAIN_MENU,
		CHANGE_PASSWORD,
		CREATE_USER,
		INSERT,
		DELETE,
		UPDATE,
		RENAME,
		GET
	}
	/**
	 * handles Applet display
	 * @author Alex Suchman
	 *
	 */
	public static class AppManager
	{
		public static void changeMode(modes mode,AppletMain app)
		{
			mainPanel.removeAll();
			//in case of disabled by forced password change
			chp.cancelButton.setEnabled(false);
			
			switch(mode)
			{
				case NOT_LOGGED_IN:
					loginMode();
					break;
				case MAIN_MENU:
					mainMenuMode();
					break;
				case CHANGE_PASSWORD:
					changePasswordMode();
					break;
				case CREATE_USER:
					createUserMode();
					break;
				default:
					app.alert("sorry, not implemented yet");
					break;
			}
			mainPanel.add(versionLabel,BorderLayout.SOUTH);	
			//app.repaint();
			mainPanel.setVisible(false);
			mainPanel.setVisible(true);
			mainPanel.revalidate();
			
		}

		private static void createUserMode() 
		{
			
			mainPanel.add(acup,BorderLayout.CENTER);
		}

		private static void changePasswordMode() 
		{
			
			mainPanel.add(chp,BorderLayout.CENTER);
		}

		private static void mainMenuMode() 
		{
			
			mainPanel.add(mm,BorderLayout.CENTER);
		}

		private static void loginMode() 
		{
			
            mainPanel.add(lp,BorderLayout.CENTER);
		}
		
	}
	/**
	 * handles user login
	 */
	private void doLogin()
	{	
		String response =send(
				"LOGIN "+lp.getFields()
				+"\nbye");
		//correct login ,forced password change
		if (response.indexOf("you are currently using a temporary")>=0)
		{
			alert("you are using a temporary password.\nYouneed to change it");
			//open change password dialog
			AppManager.changeMode(modes.CHANGE_PASSWORD, this);
			//turn off cancel button
			chp.cancelButton.setEnabled(false);
			//redirect submit button on change password to login screen
			chp.submitButton.setActionCommand("login screen");
			//no login			
			return;
		}
		Integer index=new Integer(response.toLowerCase().indexOf("welcome,"));
		//incorrect login
		if (index<0)
		{
			alert("Log in unsuccesful \nSesver said\n========\n"+response);
			return;
		}
		
		//normal login succesful
		response=response.substring(index);
		Vector<String>tokens=new Vector<String>();
		StringTokenizer st = new StringTokenizer(response);
		while (st.hasMoreTokens()) 
		{
		  tokens.add(st.nextToken());
		}
		//set user
		currentSession.setUser(new User ( tokens.get(1)+" "+tokens.get(2)+" "+tokens.get(3) ) );
		currentSession.currentUser.setPassword(lp.getFields().split(" ")[1]);
		AppManager.changeMode(modes.MAIN_MENU, this);
		
	}
	/**
	 * dialog box wrapper
	 * @param msg
	 */
	
	public void alert(String msg)
	{
		JOptionPane.showMessageDialog(this, msg);
	}
	
      /**
     * current Session
     * @author Alex Suchman
     *
     */
    public class Session {
    	User currentUser;
    	modes currentMode=modes.NOT_LOGGED_IN;
    	Session()
    	{
    		currentUser=null;
    	}
		
		public void setUser(User user) {
			currentUser=user;
			currentMode=modes.MAIN_MENU;
			//update all controls to user
			for (applyPolicy ap:controls)
				ap.setUser(user);
		}
		
    }
    /**
     * defines a user in the system
     * @author Alex Suchman
     *
     */
    public static class User  {
    	public enum Permissions {USER,ADMIN}; 
    	private Permissions myPerm;
    	private String password;
    	public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
		private boolean needChangePassword=false;
    	
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
    	public User(String desc)
    	{
    		setByString(desc);
    	}
    	public void setByString(String desc)
    	{
    		String tok[]=desc.split(" ");
    		userName=tok[0];
    		needChangePassword=Boolean.parseBoolean(tok[1]);
    		myPerm=Permissions.valueOf(tok[2]);
    	}
    	public String ToString()
    	{
    		return userName+" "+" "+needChangePassword+" "+myPerm;
    	}
    	
    }
   
    /**
     * forces all controls show according to user
     * @author Alex Suchman
     *
     */
    public interface applyPolicy
    {
    	public void setUser(User user);
    	public String getFields();
    }
    /**
     * outer container panel
     * @author Alex Suchman
     *
     */
    
    @SuppressWarnings("serial")
	public class OuterPanel extends javax.swing.JPanel implements applyPolicy{

        /** Creates new form OuterPanel */
        public OuterPanel() {
            initComponents();
        }

        /** This method is called from within the constructor to
         * initialize the form.
         * WARNING: Do NOT modify this code. The content of this method is
         * always regenerated by the Form Editor.
         */
        @SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">
        private void initComponents() {

        	setUserName("guest");

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 384, Short.MAX_VALUE)
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 255, Short.MAX_VALUE)
            );
        }// </editor-fold>

        public void setUserName(String name)
        {
        	setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Welcome ,"+name, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 24))); // NOI18N
        }
        // Variables declaration - do not modify
        // End of variables declaration

		@Override
		public void setUser(User user) 
		{
			setUserName(user.userName);
			if (user.myPerm==User.Permissions.ADMIN)
				setUserName(user.userName+ " (admin) ");
		}

		@Override
		public String getFields() {
			// TODO Auto-generated method stub
			return null;
		}

    }
    /**
     * Login screen
     * @author Alex Suchman
     *
     */
    @SuppressWarnings("serial")
	public static class LoginPanel extends javax.swing.JPanel implements applyPolicy{
    	/**
    	 * redutns user name <blank space> password
    	 * @return
    	 */
    	public String getFields()
    	{
    		return userNameText.getText()+" "+new String(passwordText.getPassword());
    	}
        /** Creates new form LoginPanel */
        public LoginPanel() {
            initComponents();
            myInit();
        }

        private void myInit() 
        {
			submitButton.setActionCommand(Operations.LOGIN.toString());
		}
		/** This method is called from within the constructor to
         * initialize the form.
         * WARNING: Do NOT modify this code. The content of this method is
         * always regenerated by the Form Editor.
         */
        @SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">
        private void initComponents() {

            jLabel2 = new javax.swing.JLabel();
            jLabel3 = new javax.swing.JLabel();
            submitButton = new javax.swing.JButton();
            userNameText = new javax.swing.JTextField(10);
            passwordText = new javax.swing.JPasswordField(10);

            setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Login", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 24))); // NOI18N

            jLabel2.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
            jLabel2.setText("User Name");

            jLabel3.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
            jLabel3.setText("Password");

            submitButton.setText("Submit");

            userNameText.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
   
            passwordText.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
            

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel2)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(userNameText))
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel3)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(passwordText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGroup(layout.createSequentialGroup()
                            .addGap(91, 91, 91)
                            .addComponent(submitButton)))
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(43, 43, 43)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(userNameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(24, 24, 24)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel3)
                        .addComponent(passwordText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addComponent(submitButton)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
        }// </editor-fold>

        


        // Variables declaration - do not modify
        private javax.swing.JLabel jLabel2;
        private javax.swing.JLabel jLabel3;
        private javax.swing.JPasswordField passwordText;
        private javax.swing.JButton submitButton;
        private javax.swing.JTextField userNameText;
        // End of variables declaration
		@Override
		public void setUser(User user) {
			// TODO Auto-generated method stub
			
		}

    }


   /**
   * Main Panel
   * @author Alex Suchman
   */
   @SuppressWarnings("serial")
   public static class MainMenuPanel extends javax.swing.JPanel implements applyPolicy{

      /** Creates new form MainMenuPanel */
      public MainMenuPanel() {
          initComponents();
          myInit();
      }

      private void myInit() {
		adminButton.setVisible(false);
		adminButton.setActionCommand("create user dialog");
		changePasswordButton.setActionCommand("change password dialog");
		deleteFileButton.setActionCommand("delete file dialog");
		getFileButton.setActionCommand("get file dialog");
		insertFileButton.setActionCommand("insert file dialog");
		renameFileButton.setActionCommand("rename file dialog");
		updateFileButton.setActionCommand("update file dialog");
	}

	/** This method is called from within the constructor to
       * initialize the form.
       * WARNING: Do NOT modify this code. The content of this method is
       * always regenerated by the Form Editor.
       */
      @SuppressWarnings("unchecked")
      // <editor-fold defaultstate="collapsed" desc="Generated Code">
      private void initComponents() {

          adminButton = new javax.swing.JButton();
          changePasswordButton = new javax.swing.JButton();
          deleteFileButton = new javax.swing.JButton();
          updateFileButton = new javax.swing.JButton();
          getFileButton = new javax.swing.JButton();
          renameFileButton = new javax.swing.JButton();
          insertFileButton = new javax.swing.JButton();

          setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Welcome ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 24))); // NOI18N

          adminButton.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
          adminButton.setForeground(new java.awt.Color(255, 51, 0));
          adminButton.setText("Admin");

          changePasswordButton.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
          changePasswordButton.setText("Change Password");

          deleteFileButton.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
          deleteFileButton.setText("Delete File");

          updateFileButton.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
          updateFileButton.setText("Update File");

          getFileButton.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
          getFileButton.setText("Get File");

          renameFileButton.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
          renameFileButton.setText("Rename File");

          insertFileButton.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
          insertFileButton.setText("Insert File");

          javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
          this.setLayout(layout);
          layout.setHorizontalGroup(
              layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(layout.createSequentialGroup()
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                      .addComponent(updateFileButton)
                      .addComponent(renameFileButton)
                      .addComponent(changePasswordButton)
                      .addComponent(deleteFileButton)
                      .addComponent(insertFileButton)
                      .addComponent(getFileButton))
                  .addContainerGap(182, Short.MAX_VALUE))
              .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                  .addContainerGap(172, Short.MAX_VALUE)
                  .addComponent(adminButton)
                  .addContainerGap())
          );

          layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {adminButton, changePasswordButton, deleteFileButton, updateFileButton, getFileButton, renameFileButton, insertFileButton});

          layout.setVerticalGroup(
              layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(layout.createSequentialGroup()
                  .addGap(43, 43, 43)
                  .addComponent(changePasswordButton)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(deleteFileButton)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(insertFileButton)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(updateFileButton)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(renameFileButton)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(getFileButton)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(adminButton)
                  .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
          );
      }// </editor-fold>


      // Variables declaration - do not modify
      private javax.swing.JButton adminButton;
      private javax.swing.JButton changePasswordButton;
      private javax.swing.JButton deleteFileButton;
      private javax.swing.JButton updateFileButton;
      private javax.swing.JButton getFileButton;
      private javax.swing.JButton renameFileButton;
      private javax.swing.JButton insertFileButton;
      // End of variables declaration
	@Override
	public void setUser(User user) 
	{
		//show admin button only if user is admin
		adminButton.setVisible(user.myPerm==User.Permissions.ADMIN);
		
	}

	@Override
	public String getFields() {
		// TODO Auto-generated method stub
		return null;
	}

  }

   /**
    * Administarator create new user
    * @author Alex Suchman
    *
    */
   @SuppressWarnings("serial")
   public static class AdminCreateUserPanel extends javax.swing.JPanel implements applyPolicy{

	    /** Creates new form AdminCreateUserPanel */
	    public AdminCreateUserPanel() {
	        initComponents();
	        myInit();
	    }
	    
	    
	    private void myInit() 
	    {
	    	submitButton.setActionCommand(Operations.CREATEUSER.toString());
			cancelButton.setActionCommand(Operations.NOVALUE.toString());
		}

		/** This method is called from within the constructor to
	     * initialize the form.
	     * WARNING: Do NOT modify this code. The content of this method is
	     * always regenerated by the Form Editor.
	     */
	    @SuppressWarnings("unchecked")
	    // <editor-fold defaultstate="collapsed" desc="Generated Code">
	    private void initComponents() {

	        jLabel1 = new javax.swing.JLabel();
	        jLabel2 = new javax.swing.JLabel();
	        userNameText = new javax.swing.JTextField();
	        passwordText = new javax.swing.JTextField();
	        submitButton = new javax.swing.JButton();
	        jLabel3 = new javax.swing.JLabel();
	        cancelButton = new javax.swing.JButton();

	        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "New User", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 24))); // NOI18N

	        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
	        jLabel1.setText("User Name");

	        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
	        jLabel2.setText("Password");

	        userNameText.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N

	        passwordText.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N

	        submitButton.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
	        submitButton.setText("Submit");

	        jLabel3.setText("(optional)");

	        cancelButton.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
	        cancelButton.setText("Cancel");

	        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
	        this.setLayout(layout);
	        layout.setHorizontalGroup(
	            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(layout.createSequentialGroup()
	                .addGap(47, 47, 47)
	                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
	                    .addComponent(jLabel1)
	                    .addGroup(layout.createSequentialGroup()
	                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
	                            .addComponent(submitButton)
	                            .addComponent(jLabel2))
	                        .addGap(4, 4, 4)))
	                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addGroup(layout.createSequentialGroup()
	                        .addGap(18, 18, 18)
	                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                            .addComponent(userNameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                            .addGroup(layout.createSequentialGroup()
	                                .addComponent(passwordText, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
	                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                                .addComponent(jLabel3))))
	                    .addGroup(layout.createSequentialGroup()
	                        .addGap(50, 50, 50)
	                        .addComponent(cancelButton)))
	                .addContainerGap(23, Short.MAX_VALUE))
	        );

	        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {passwordText, userNameText});

	        layout.setVerticalGroup(
	            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(layout.createSequentialGroup()
	                .addGap(34, 34, 34)
	                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	                    .addComponent(jLabel1)
	                    .addComponent(userNameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
	                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	                    .addComponent(jLabel2)
	                    .addComponent(passwordText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addComponent(jLabel3))
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 92, Short.MAX_VALUE)
	                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	                    .addComponent(submitButton)
	                    .addComponent(cancelButton))
	                .addContainerGap())
	        );
	    }// </editor-fold>


	    // Variables declaration - do not modify
	    private javax.swing.JButton cancelButton;
	    private javax.swing.JLabel jLabel1;
	    private javax.swing.JLabel jLabel2;
	    private javax.swing.JLabel jLabel3;
	    private javax.swing.JTextField passwordText;
	    private javax.swing.JButton submitButton;
	    private javax.swing.JTextField userNameText;
	    // End of variables declaration
		@Override
		public void setUser(User user) {
			boolean isAdmin= (user.myPerm==User.Permissions.ADMIN);
			userNameText.setEnabled(isAdmin);
			passwordText.setEnabled(isAdmin);
			submitButton.setEnabled(isAdmin);
		}

		@Override
		public String getFields() {
			return userNameText.getText()+" "+passwordText.getText();
		}

	}

   /**
    * change password panel
    * @author Alex Suchman
    *
    */
   @SuppressWarnings("serial")
public static class ChangePasswordPanel extends javax.swing.JPanel implements applyPolicy{

	    /** Creates new form ChangePassword */
	    public ChangePasswordPanel() {
	        initComponents();
	        myInit();
	    }

	    

		private void myInit() 
		{
			cancelButton.setActionCommand(Operations.NOVALUE.toString());
			submitButton.setActionCommand(Operations.CHANGEPSWD.toString());
		}



		/** This method is called from within the constructor to
	     * initialize the form.
	     * WARNING: Do NOT modify this code. The content of this method is
	     * always regenerated by the Form Editor.
	     */
	    @SuppressWarnings("unchecked")
	    // <editor-fold defaultstate="collapsed" desc="Generated Code">
	    private void initComponents() {

	        setPasswordLabel = new javax.swing.JLabel();
	        newPasswordText = new javax.swing.JTextField();
	        setUserNameLabel = new javax.swing.JLabel();
	        userNameText = new javax.swing.JTextField();
	        submitButton = new javax.swing.JButton();
	        changePasswordDescLabel = new javax.swing.JLabel();
	        selectUserDescLabel = new javax.swing.JLabel();
	        cancelButton = new javax.swing.JButton();

	        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Change Password", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 24))); // NOI18N

	        setPasswordLabel.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
	        setPasswordLabel.setText("New Password");

	        newPasswordText.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N

	        setUserNameLabel.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
	        setUserNameLabel.setText("For User Name");

	        userNameText.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N

	        submitButton.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
	        submitButton.setText("Change Password");

	        changePasswordDescLabel.setText("(leave empty for\n system generated password)");

	        selectUserDescLabel.setText("(leave blank \nfor current user)");

	        cancelButton.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
	        cancelButton.setText("Cancel");

	        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
	        this.setLayout(layout);
	        layout.setHorizontalGroup(
	            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(layout.createSequentialGroup()
	                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addGroup(layout.createSequentialGroup()
	                        .addGap(46, 46, 46)
	                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                            .addComponent(setPasswordLabel)
	                            .addComponent(setUserNameLabel))
	                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                            .addComponent(userNameText, javax.swing.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)
	                            .addComponent(newPasswordText, javax.swing.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE))
	                        .addGap(10, 10, 10))
	                    .addGroup(layout.createSequentialGroup()
	                        .addGap(57, 57, 57)
	                        .addComponent(submitButton)
	                        .addGap(67, 67, 67)))
	                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addComponent(cancelButton)
	                    .addComponent(selectUserDescLabel)
	                    .addComponent(changePasswordDescLabel))
	                .addContainerGap())
	        );
	        layout.setVerticalGroup(
	            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(layout.createSequentialGroup()
	                .addGap(71, 71, 71)
	                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	                    .addComponent(setPasswordLabel)
	                    .addComponent(newPasswordText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addComponent(changePasswordDescLabel))
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	                    .addComponent(setUserNameLabel)
	                    .addComponent(userNameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addComponent(selectUserDescLabel))
	                .addGap(18, 18, 18)
	                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	                    .addComponent(submitButton)
	                    .addComponent(cancelButton))
	                .addContainerGap(53, Short.MAX_VALUE))
	        );
	    }// </editor-fold>


	    // Variables declaration - do not modify
	    private javax.swing.JButton submitButton;
	    private javax.swing.JButton cancelButton;
	    private javax.swing.JLabel setPasswordLabel;
	    private javax.swing.JLabel setUserNameLabel;
	    private javax.swing.JLabel changePasswordDescLabel;
	    private javax.swing.JLabel selectUserDescLabel;
	    private javax.swing.JTextField newPasswordText;
	    private javax.swing.JTextField userNameText;
	    // End of variables declaration
		@Override
		public void setUser(User user) {
			boolean isAdmin=(user.myPerm==User.Permissions.ADMIN);
			setUserNameLabel.setVisible(isAdmin);
			userNameText.setVisible(isAdmin);
			selectUserDescLabel.setVisible(isAdmin);
		}

		@Override
		public String getFields() {
			return userNameText.getText().trim()+" "+newPasswordText.getText().trim();
		}

	}
   	
   	
	@Override
	public void actionPerformed(ActionEvent e) 
	{
		
		if (e.getActionCommand().equalsIgnoreCase(Operations.LOGIN.toString()))
		{
			doLogin();
			return;
		}
		if (e.getActionCommand().equalsIgnoreCase("create user dialog"))
		{
			AppManager.changeMode(modes.CREATE_USER, this);
			return ;
		}
		if (e.getActionCommand().equalsIgnoreCase("change password dialog"))
		{
			AppManager.changeMode(modes.CHANGE_PASSWORD, this);
			return ;
		}
		if(e.getActionCommand().equalsIgnoreCase(Operations.CREATEUSER.toString()))
		{
			doCreateUser();
			return;
		}
		if(e.getActionCommand().equalsIgnoreCase(Operations.CHANGEPSWD.toString()))
		{
			doChangePassword();
			return;
		}
		if(e.getActionCommand().equalsIgnoreCase(Operations.NOVALUE.toString()))
		{
			AppManager.changeMode(modes.MAIN_MENU, this);
			return;
		}
		if(e.getActionCommand().equalsIgnoreCase("login screen"))
		{
			temporaryPasswordChange();
			return;
		}
		alert("not implemented yet, sorry");
		
	}
	/**
	 * handles temporary password change
	 */
	private void temporaryPasswordChange() 
	{
		//normal password change
		doChangePassword();
		//return change password submit button to normal operation
		chp.submitButton.setActionCommand(Operations.CHANGEPSWD.toString());
		chp.cancelButton.setEnabled(true);
		//goto login screen
		AppManager.changeMode(modes.NOT_LOGGED_IN, this);
		//alert("you have succesfult chage your password,\nplease log in with the new password");
	}
	private String verCommand(String cmd)
	{
		String password=lp.getFields().split(" ")[0];
		//no message entered
		if (password.equalsIgnoreCase(""))
			return cmd;
		try 
		{
			//Verifiable command
			return cmd+" "+Protection.doDigest(cmd,password );
		} 
		catch (Exception e )
		{
			alert("cannot created varifible message , sendig without verification!");
			return cmd;
		}
	}
	/**
	 * change password script
	 */
	private void doChangePassword() 
	{
		/*String response=send("login "+lp.getFields()+"\n"+
				"changePswd "+chp.getFields()+"\n"+
				"bye");
				*/
		String response=send("login "+lp.getFields()+"\n"+
				verCommand("changePswd "+chp.getFields())+"\n"+
				"bye");
		if (response.indexOf("could not change")>=0)
		{
			//internal error
			alert("could not change password, internal error\nplease contact support");
			return;
		}
		if (response.indexOf("password for user")>=0)
		{
			//all OK
			alert(response.substring(response.indexOf("password for user")));
			AppManager.changeMode(modes.MAIN_MENU, this);
			return;
		}
		//password not OK
		alert("password no long/short/ in dictionary , try again please.");
		
	}
	/**
	 * handles create user operetion request
	 */
	private void doCreateUser() 
	{	 
		String response=send("login "+lp.getFields()+"\n"+
				verCommand("createUser "+acup.getFields())+"\n"+
				"bye");
		//check operaztion result;
		String msg="";
		if (response.indexOf("created new user")<0)
		{
			//creatrion failed
			msg="User creation Failed \n";
			
			if (response.indexOf("already exists")>=0)
			{
				//due to duplicate user name
				msg+="user with that name exists, try another User name\n";
			}
			return;
		}
		else
		{
			msg="User succesfully created!";
		}
		alert(msg);
		AppManager.changeMode(modes.MAIN_MENU, this);
	}
	


}
