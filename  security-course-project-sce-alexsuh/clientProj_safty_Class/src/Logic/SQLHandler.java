package Logic;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Handler;
import java.util.logging.LogRecord;



public class SQLHandler extends Handler{
	private databaseConnection dbCon;
	public SQLHandler(databaseConnection con) {
		this.dbCon=con;
	}
	public databaseConnection getDbCon() {
		return dbCon;
	}

	public void setDbCon(databaseConnection dbCon) {
		this.dbCon = dbCon;
	}

	@Override
	public void close() throws SecurityException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void publish(LogRecord logRec) {
		System.out.println("SERVER>"+logRec.getMessage());
		Timestamp ts=new Timestamp(logRec.getMillis());
		try {
			Connection con=dbCon.getConnection();
			String sqlCmd="";
			sqlCmd="INSERT INTO LOG(" +
					"EVENT_DATE," +
					"EVENT_LEVEL," +
					"EVENT_DESC) " +
					"VALUES" +
					"(?,?,?)"; 
			PreparedStatement stt=con.prepareStatement(sqlCmd);
			stt.setTimestamp(1, ts);
			stt.setString(2, logRec.getLevel().toString());
			stt.setString(3, logRec.getMessage());
			stt.executeUpdate();
			
		} catch (SQLException e) {
			System.err.println(
					"failed to contact database" +
					"message was"+e.getMessage()+
					"original log message "+
					logRec.getLevel()+
					" - "+
					logRec.getMessage());
		}
	}

}
