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

	public static Connection getDbConnection() throws RuntimeException {
		try {
			Class.forName(driverClass);
			if (dbConnection == null || dbConnection.isClosed()) {
				dbConnection = DriverManager.getConnection(url, username,
						password);
			}
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Error while trying to find db driver",
					e);
		} catch (SQLException e) {
			throw new RuntimeException(
					"Error while trying to open db connection", e);
		}
		return dbConnection;
	}

	public static void closeDbConnection() throws RuntimeException {
		if (dbConnection != null) {
			try {
				dbConnection.close();
			} catch (SQLException e) {
				throw new RuntimeException(
						"Error while trying to close the db connection", e);
			}
		}
	}

}