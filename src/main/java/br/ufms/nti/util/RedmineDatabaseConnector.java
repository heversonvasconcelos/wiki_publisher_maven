package br.ufms.nti.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class RedmineDatabaseConnector {
	private static Connection dbConnection;
	private static String driverClass;
	private static String url;
	private static String username;
	private static String password;

	private RedmineDatabaseConnector() {
	}

	public static void initializeAccessConfiguration(String driverClass,
			String url, String username, String password) {

		RedmineDatabaseConnector.driverClass = driverClass;
		RedmineDatabaseConnector.url = url;
		RedmineDatabaseConnector.username = username;
		RedmineDatabaseConnector.password = password;

	}

	public static Connection getDbConnection() throws SQLException {
		try {
			Class.forName(driverClass);
			if (dbConnection == null || dbConnection.isClosed()) {
				dbConnection = DriverManager.getConnection(url, username,
						password);
			}
		} catch (ClassNotFoundException e) {
			System.out.println("Error while trying to find db driver");
		} catch (SQLException e) {
			System.out.println("Error while trying to open db connection");
		}
		return dbConnection;
	}

	public static void closeDbConnection() throws SQLException {
		if (dbConnection != null) {
			try {
				dbConnection.close();
			} catch (SQLException e) {
				System.out
						.println("Error while trying to close the db connection");
				throw e;
			}
		}
	}

}