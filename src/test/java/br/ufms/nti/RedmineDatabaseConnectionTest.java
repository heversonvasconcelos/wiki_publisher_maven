package br.ufms.nti;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import br.ufms.nti.util.RedmineDatabaseConnector;

public class RedmineDatabaseConnectionTest {
	private final String SQL_SELECT_TEST = "SELECT 1 FROM wikis";

	@BeforeClass
	public static void openConnection() {
		RedmineDatabaseConnector
				.initializeAccessConfiguration("org.postgresql.Driver",
						"jdbc:postgresql://debianvm:5432/redmine", "redmine",
						"redmine");
	}

	@AfterClass
	public static void closeConnection() throws SQLException {
		RedmineDatabaseConnector.closeDbConnection();
	}

	@Test
	public void connectionTest() {
		try {
			PreparedStatement statement = RedmineDatabaseConnector
					.getDbConnection().prepareStatement(SQL_SELECT_TEST);
			ResultSet rs = statement.executeQuery();
			Assert.assertNotNull(rs.next());

		} catch (SQLException e) {
			System.out.println("Error while trying to access the db: "
					+ e.getMessage());
		}
	}
}
