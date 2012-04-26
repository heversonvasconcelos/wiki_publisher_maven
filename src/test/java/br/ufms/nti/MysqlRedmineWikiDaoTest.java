package br.ufms.nti;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import br.ufms.nti.util.RedmineDatabaseConnector;

public class MysqlRedmineWikiDaoTest {
	private final String SQL_CREATE_WIKI_PAGE = "INSERT INTO wiki_pages (wiki_id, title, created_on, protected) VALUES (?, ?, now(), false)";
	private final Long wikiId = new Long(1L);
	private final String wikiPageTitle = new String("index");

	@BeforeClass
	public static void openConnection() {
		RedmineDatabaseConnector.initializeAccessConfiguration(
				"com.mysql.jdbc.Driver", "jdbc:mysql://debianvm:3306/redmine",
				"redmine", "redmine");
	}

	@AfterClass
	public static void closeConnection() throws SQLException {
		RedmineDatabaseConnector.closeDbConnection();
	}

	@Test
	public void createWikiPageTest() {
		Long id;
		try {
			PreparedStatement statement = RedmineDatabaseConnector
					.getDbConnection().prepareStatement(SQL_CREATE_WIKI_PAGE,
							Statement.RETURN_GENERATED_KEYS);
			statement.setLong(1, wikiId);
			statement.setString(2, wikiPageTitle);
			statement.executeUpdate();
			ResultSet rs = statement.getGeneratedKeys();

			if (rs.next()) {
				id = rs.getLong(1);
				Assert.assertNotNull(id);
			}
		} catch (SQLException e) {
			throw new RuntimeException(
					"Error while trying to create wiki page", e);
		}
	}
}
