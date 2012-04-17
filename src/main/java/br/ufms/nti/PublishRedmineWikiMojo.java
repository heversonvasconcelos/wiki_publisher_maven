package br.ufms.nti;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
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
@SuppressWarnings("unchecked")
public class PublishRedmineWikiMojo extends AbstractMavenReport {

	Logger _log = Logger.getLogger(PublishRedmineWikiMojo.class.getName());

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
	 * @parameter expression="${redmineDatabaseUrl}"
	 * @required
	 */
	private String redmineDatabaseUrl;

	/**
	 * Project design directory
	 * 
	 * @parameter expression="${redmineDesignDir}"
	 * @required
	 */
	private String designDir;

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

	private Long projectId;
	private Long wikiId;

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
				redmineDatabaseDriver, redmineDatabaseUrl, databaseUsername,
				databasePassword);
	}

	@Override
	protected void executeReport(Locale locale) throws MavenReportException {
		initializeRedmineDatabaseConfiguration();
		projectId = getProjectId();
		wikiId = getWikiId();

		File designDir = new File("design");
		Collection<File> files = FileUtils.listFiles(designDir,
				new String[] { "textile" }, true);

		for (File file : files) {
			Long wikiPageId = createWikiPage(file);
			createWikiContent(wikiPageId, file);
		}
	}

	private Long createWikiPage(File file) {
		String wikiPageTitle = file.getName();
		wikiPageTitle = wikiPageTitle.substring(0,
				wikiPageTitle.lastIndexOf("textile") - 1);
		return createWikiPage(wikiPageTitle);
	}

	/**
	 * Gets database project id by project identifier.
	 * 
	 * @return project id or -1 if not found
	 */
	private Long getProjectId() {
		try {
			PreparedStatement statement;
			ResultSet rs = null;
			statement = RedmineDatabaseConnector.getDbConnection()
					.prepareStatement(Constants.SQL_GET_PROJECT_ID);
			statement.setString(1, projectIdentifier);
			rs = statement.executeQuery();
			if (rs.next()) {
				Long projectId = rs.getLong("id");
				return projectId;
			}
			return null;
		} catch (SQLException e) {
			throw new RuntimeException(
					"Error while trying to get project id from db", e);
		}
	}

	/**
	 * Gets database wiki id by project id
	 * 
	 * @return wiki id or null if not found
	 */
	private Long getWikiId() {
		try {
			PreparedStatement statement;
			ResultSet rs = null;
			statement = RedmineDatabaseConnector.getDbConnection()
					.prepareStatement(Constants.SQL_GET_WIKI_ID);
			statement.setLong(1, projectId);
			rs = statement.executeQuery();
			if (rs.next()) {
				Long wikiId = rs.getLong("id");
				return wikiId;
			}
			return null;
		} catch (SQLException e) {
			throw new RuntimeException("Error while trying to get wiki id", e);
		}
	}

	/**
	 * Creates wiki page
	 * 
	 * @param wikiPageTitle
	 * @return wiki page id or null if not created
	 */
	private Long createWikiPage(String wikiPageTitle) {
		try {
			Long wikiPageId = getWikiPageId(wikiPageTitle);
			if (wikiPageId != null) {
				_log.info("WikiPage already exists: " + wikiPageTitle);
				return wikiPageId;
			}
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
			return wikiPageId;
		} catch (SQLException e) {
			throw new RuntimeException(
					"Error while trying to create wiki page", e);
		}
	}

	private Long getWikiPageId(String wikiPageTitle) {
		try {
			PreparedStatement statement = RedmineDatabaseConnector
					.getDbConnection().prepareStatement(
							Constants.SQL_CREATE_WIKI_PAGE);
			statement.setLong(1, wikiId);
			statement.setString(2, wikiPageTitle);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				Long wikiPageId = rs.getLong("id");
				return wikiPageId;
			}
			return null;
		} catch (Exception e) {
			throw new RuntimeException(
					"Error while trying to retrieve the wiki page ID", e);
		}
	}

	/**
	 * Creates wiki content
	 * 
	 * @param wikiPageId
	 * @param reader
	 *            Reader that contains wiki data
	 * @param fileLength
	 *            Length from the reader
	 * @return wiki content id or null if not created
	 */
	private Long createWikiContent(Long wikiPageId, File file) {
		try {
			PreparedStatement statement;
			ResultSet rs = null;
			statement = RedmineDatabaseConnector.getDbConnection()
					.prepareStatement(Constants.SQL_CREATE_WIKI_CONTENT);
			statement.setLong(1, wikiPageId);

			String path = file.getAbsolutePath();
			path = path.substring(path.indexOf(designDir));

			StringBuilder wikiContent = new StringBuilder();
			wikiContent.append("{{repo_include(");
			wikiContent.append(path);
			wikiContent.append(")}}");
			statement.setString(2, wikiContent.toString());

			rs = statement.executeQuery();
			if (rs.next()) {
				Long wikiContentId = rs.getLong("id");
				return wikiContentId;
			}
			return null;
		} catch (SQLException e) {
			throw new RuntimeException(
					"Error while trying to create wiki page", e);
		}

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
