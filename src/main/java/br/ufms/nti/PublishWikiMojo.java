package br.ufms.nti;

import java.io.Reader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

import org.apache.maven.reporting.MavenReportException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;

import br.ufms.nti.util.Constants;
import br.ufms.nti.util.RedmineDatabaseConnector;

/**
 * Goal which publish the site files to a redmine wiki database.
 * 
 * @goal publish-wiki
 */
public class PublishWikiMojo {
	/**
	 * 
	 * @parameter expression="${redmineDatabaseServer}"
	 * @required
	 */
	private String redmineDatabaseServer;

	/**
	 * 
	 * @parameter expression="${redmineDatabaseServerURL}"
	 * @required
	 */
	private String redmineDatabaseServerURL;

	/**
	 * 
	 * @parameter expression="${redmineDatabaseServerDriverClassNamed}"
	 * @required
	 */
	private String redmineDatabaseServerDriverClassNamed;

	private String databaseUsername;
	private String databasePassword;

	private long projectId;
	private String wikiPageTitle = "Wiki";
	private long wikiId;

	private void initializeRedmineDatabaseConfiguration() {
		AuthenticationInfo info = wagonManager
				.getAuthenticationInfo(redmineDatabaseServer);

		databaseUsername = info.getUserName();
		databasePassword = info.getPassword();
		RedmineDatabaseConnector.initializeAccessConfiguration(
				redmineDatabaseServerDriverClassNamed,
				redmineDatabaseServerURL, databaseUsername, databasePassword);
	}

	protected void executeReport(Locale locale) throws MavenReportException {
		initializeRedmineDatabaseConfiguration();

		projectId = getProjectId();
		wikiId = getWikiId();
		// TODO
		// long wikiPageId = createWikiPage(wikiPageTitle);
		// File file = new File("src/main/resources/app-config.properties");
		// long fileLength = file.length();
		// Reader fileReader = (Reader) new BufferedReader(new
		// FileReader(file));
		// long wikiContentId = createWikiContent(wikiPageId, fileReader,
		// (int) fileLength);

	}

	private long getProjectId() {
		long projectId = -1;

		try {
			PreparedStatement statement;
			ResultSet rs = null;
			statement = RedmineDatabaseConnector.getDbConnection()
					.prepareStatement(Constants.SQL_GET_PROJECT_ID);
			statement.setString(1, getProjectIdentifier());
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
		long wikiId = -1;

		try {
			PreparedStatement statement;
			ResultSet rs = null;
			statement = RedmineDatabaseConnector.getDbConnection()
					.prepareStatement(Constants.SQL_GET_WIKI_BY_PROJECT_ID);
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
		long wikiPageId = -1;

		try {
			PreparedStatement statement;
			ResultSet rs = null;
			statement = RedmineDatabaseConnector.getDbConnection()
					.prepareStatement(Constants.SQL_CREATE_WIKI_PAGE);
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
		long wikiContentId = -1;

		try {
			PreparedStatement statement;
			ResultSet rs = null;
			statement = RedmineDatabaseConnector.getDbConnection()
					.prepareStatement(Constants.SQL_CREATE_WIKI_CONTENT);
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
