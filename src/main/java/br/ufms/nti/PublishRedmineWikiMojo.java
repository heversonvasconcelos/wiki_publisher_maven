package br.ufms.nti;

import java.io.File;
import java.io.Reader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.codehaus.doxia.site.renderer.SiteRenderer;

import br.ufms.nti.util.Constants;
import br.ufms.nti.util.RedmineDatabaseConnector;

/**
 * Goal which publish the site files to a redmine wiki database.
 * 
 * @goal publish-redmine-wiki
 */
public class PublishRedmineWikiMojo extends AbstractMavenReport {

	/**
	 * The Maven Wagon manager to use when obtaining server authentication
	 * details.
	 * 
	 * @component role="org.apache.maven.artifact.manager.WagonManager"
	 * @required
	 * @readonly
	 */
	protected WagonManager wagonManager;

	/**
	 * Project identifier.
	 * 
	 * @parameter default-value="${project.artifactId}"
	 *            expression="${projectIdentifier}"
	 * @required
	 */
	private String projectIdentifier;

	/**
	 * The server id in settings.xml to use when authenticating with Redmine
	 * database.
	 * 
	 * @parameter expression="${redmineDatabase}"
	 * @required
	 */
	private String redmineDatabase;

	/**
	 * Redmine database url
	 * 
	 * @parameter expression="${redmineDatabaseURL}"
	 * @required
	 */
	private String redmineDatabaseServerURL;

	/**
	 * Redmine database driver
	 * 
	 * @parameter expression="${redmineDatabaseDriver}"
	 * @required
	 */
	private String redmineDatabaseDriver;

	/**
	 * The maven project
	 * 
	 * @parameter expression="${project}"
	 * @readonly
	 */
	private MavenProject project;

	/**
	 * The filename to use for the report.
	 * 
	 * @parameter expression="hello-report"
	 * @readonly
	 */
	private String outputName;

	/**
	 * Directory containing The generated DashBoard report Datafile
	 * "dashboard-report.xml".
	 * 
	 * @parameter expression="${project.reporting.outputDirectory}"
	 * @required
	 */
	private File outputDirectory;

	/**
	 * Site Renderer
	 * 
	 * @parameter 
	 *            expression="${component.org.codehaus.doxia.site.renderer.SiteRenderer}"
	 * @readonly
	 */
	private SiteRenderer siteRenderer;

	private String databaseUsername;
	private String databasePassword;

	private long projectId;
	private long wikiId;

	/**
	 * Initializes Redmine database access (username, password) configuration
	 * 
	 */
	private void initializeRedmineDatabaseConfiguration() {
		AuthenticationInfo info = wagonManager
				.getAuthenticationInfo(redmineDatabase);

		databaseUsername = info.getUserName();
		databasePassword = info.getPassword();
		RedmineDatabaseConnector.initializeAccessConfiguration(
				redmineDatabaseDriver, redmineDatabaseServerURL,
				databaseUsername, databasePassword);
	}

	@Override
	protected void executeReport(Locale locale) throws MavenReportException {
		initializeRedmineDatabaseConfiguration();

		projectId = getProjectId();
		wikiId = getWikiId();
		// TODO
		// long wikiPageId = createWikiPage("Wiki");
		// File file = new File("src/main/resources/app-config.properties");
		// long fileLength = file.length();
		// Reader fileReader = (Reader) new BufferedReader(new
		// FileReader(file));
		// long wikiContentId = createWikiContent(wikiPageId, fileReader,
		// (int) fileLength);

	}

	/**
	 * Gets database project id by project identifier.
	 * 
	 * @return project id or -1 if not found
	 */
	private long getProjectId() {
		long projectId = -1;

		try {
			PreparedStatement statement;
			ResultSet rs = null;
			statement = RedmineDatabaseConnector.getDbConnection()
					.prepareStatement(Constants.SQL_GET_PROJECT_ID);
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

	/**
	 * Gets database wiki id by project id
	 * 
	 * @return wiki id or -1 if not found
	 */
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

	/**
	 * Creates wiki page
	 * 
	 * @param wikiPageTitle
	 * @return wiki page id or -1 if not created
	 */
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

	/**
	 * Creates wiki content
	 * 
	 * @param wikiPageId
	 * @param reader
	 *            Reader that contains wiki data
	 * @param fileLength
	 *            Length from the reader
	 * @return wiki content id or -1 if not created
	 */
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

	protected String getMessage(Locale locale, String key, Object... params) {
		String text = ResourceBundle.getBundle("messages", locale,
				this.getClass().getClassLoader()).getString(key);
		if (params != null) {
			return getParameterizedMessage(text, locale, params);
		}
		return text;
	}

	protected String getParameterizedMessage(String message, Locale locale,
			Object[] params) {
		MessageFormat messageFormat = new MessageFormat(message, locale);
		message = messageFormat.format(params, new StringBuffer(), null)
				.toString();
		return message;
	}

	@Override
	public String getOutputName() {
		return outputName;
	}

	@Override
	public String getName(Locale locale) {
		return getMessage(locale, "redmine-wiki-name");
	}

	@Override
	public String getDescription(Locale locale) {
		return getMessage(locale, "redmine-wiki-description");
	}

	@Override
	protected SiteRenderer getSiteRenderer() {
		return siteRenderer;
	}

	@Override
	protected String getOutputDirectory() {
		return outputDirectory.getName();
	}

	@Override
	protected MavenProject getProject() {
		return project;
	}

}
