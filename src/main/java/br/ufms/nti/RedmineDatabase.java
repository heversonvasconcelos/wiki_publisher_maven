package br.ufms.nti;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Class to manipulate the database. 
 * Allows connection to a PostgreSQL database and
 * execution, on this database, of SQL commands contained in a text file
 */
public class RedmineDatabase {
	private static final String DB_DRIVER = "org.postgresql.Driver"; //$NON-NLS-1$
	private static final String DB_CONNECTION = "jdbc:postgresql://"; //$NON-NLS-1$
	private static String db_host;
	private static String db_name;
	private static String db_user;
	private static String db_port;
	private static String db_password;
	private static String sql;

	public RedmineDatabase (String host, String name, String user, String port, String password, String sqlRequests) throws Exception{
		db_host = host;
		db_name = name;
		db_user = user;
		db_port = port;
		db_password = password;
		sql = sqlRequests;
	}

	/**
	 * Connect to the database and run the sql script from the given path
	 * @throws Exception if there was an error while connecting to database or running sql
	 */
	public void query() throws Exception {
		Connection dbConnection = null;
		Statement statement = null;

		try {
			try {
				Class.forName(DB_DRIVER);
			} catch (Exception e) {
				throw new Exception("PostgreSQL driver not found");
			}
	
			try {
				dbConnection = DriverManager.getConnection(
					DB_CONNECTION+db_host+":"+db_port+"/"+db_name, db_user,db_password); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (Exception e) {
				throw new Exception("Unable to connect to" + db_host);
			}

			try {
				statement = dbConnection.createStatement();
				statement.execute(sql);
			} catch (Exception e) {
				throw new Exception("The entity you are trying to create already exists");
			}
		} finally {
			if (statement != null)
				statement.close();
 
			if (dbConnection != null)
				dbConnection.close();
		}
	}
	
	public void setSearchPath(String schema) {
		sql = "SET search_path = "+schema+";"+sql;
	}
}