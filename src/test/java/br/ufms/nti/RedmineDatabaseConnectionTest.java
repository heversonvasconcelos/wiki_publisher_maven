package br.ufms.nti;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.Test;

import br.ufms.nti.util.Constants;
import br.ufms.nti.util.RedmineDatabaseConnector;

public class RedmineDatabaseConnectionTest {
	private String projectIdentifier = "projetoteste";
	private String wikiPageTitle = "Wiki";
	private long projectId;
	private long wikiId;

	@Test
	public void connectionTest() {
		try {
			PreparedStatement statement;
			ResultSet rs = null;
			statement = RedmineDatabaseConnector.getDbConnection()
					.prepareStatement(Constants.SQL_SELECT_TEST);
			rs = statement.executeQuery();
			Assert.assertNotNull(rs.next());

		} catch (SQLException e) {
			System.out.println("Error while trying to access the db: "
					+ e.getMessage());
		}
	}

	@Test
	public void publishWikiContent() throws FileNotFoundException {
		projectId = getProjectId();
		wikiId = getWikiId();
		long wikiPageId = createWikiPage(wikiPageTitle);
		File file = new File("src/main/resources/app-config.properties");
		long fileLength = file.length();
		Reader fileReader = (Reader) new BufferedReader(new FileReader(file));
		long wikiContentId = createWikiContent(wikiPageId, fileReader,
				(int) fileLength);

	}

	@AfterClass
	public static void closeConnection() {
		try {
			RedmineDatabaseConnector.closeDbConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private long getProjectId() {
		String sqlGetProjectIdByProjectIdentifier = "SELECT id FROM projects WHERE identifier = ?";
		long projectId = -1;

		PreparedStatement statement = null;
		try {
			ResultSet rs = null;
			statement = RedmineDatabaseConnector.getDbConnection()
					.prepareStatement(sqlGetProjectIdByProjectIdentifier);
			statement.setString(1, projectIdentifier);
			rs = statement.executeQuery();
			if (rs.next()) {
				projectId = rs.getLong("id");
			}

		} catch (SQLException e) {
			System.out.println("Error while trying to get project id from db: "
					+ e.getMessage());
		}

		return projectId;
	}

	private long getWikiId() {
		String sqlGetWikiByProjectId = "SELECT id FROM wikis WHERE project_id = ?";
		long wikiId = -1;

		try {
			PreparedStatement statement;
			ResultSet rs = null;
			statement = RedmineDatabaseConnector.getDbConnection()
					.prepareStatement(sqlGetWikiByProjectId);
			statement.setLong(1, projectId);
			rs = statement.executeQuery();
			if (rs.next()) {
				wikiId = rs.getLong("id");
			}

		} catch (SQLException e) {
			System.out.println("Error while trying to get wiki id: "
					+ e.getMessage());
		}

		return wikiId;
	}

	private long createWikiPage(String wikiPageTitle) {
		String sqlCreateWikiPage = "INSERT INTO wiki_pages (wiki_id, title, created_on, protected) VALUES (?, ?, now(), false) RETURNING id";
		long wikiPageId = -1;

		try {
			PreparedStatement statement;
			ResultSet rs = null;
			statement = RedmineDatabaseConnector.getDbConnection()
					.prepareStatement(sqlCreateWikiPage);
			statement.setLong(1, wikiId);
			statement.setString(2, wikiPageTitle);
			rs = statement.executeQuery();
			if (rs.next()) {
				wikiPageId = rs.getLong("id");
			}

		} catch (SQLException e) {
			System.out.println("Error while trying to create wiki page: "
					+ e.getMessage());
		}

		return wikiPageId;
	}

	private long createWikiContent(long wikiPageId, Reader reader,
			int fileLength) {
		String sqlCreateWikiContent = "INSERT INTO wiki_contents (page_id, text, updated_on, version) VALUES (?, ?, now(), 1) RETURNING id";
		long wikiContentId = -1;

		try {
			PreparedStatement statement;
			ResultSet rs = null;
			statement = RedmineDatabaseConnector.getDbConnection()
					.prepareStatement(sqlCreateWikiContent);
			statement.setLong(1, wikiPageId);
			statement.setCharacterStream(2, reader, fileLength);
			rs = statement.executeQuery();
			if (rs.next()) {
				wikiContentId = rs.getLong("id");
			}

		} catch (SQLException e) {
			System.out.println("Error while trying to create wiki page: "
					+ e.getMessage());
		}

		return wikiContentId;
	}

}
