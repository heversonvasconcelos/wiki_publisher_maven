package br.ufms.nti;

import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.codehaus.doxia.site.renderer.SiteRenderer;

/**
 * Goal which sends the site files to a redmine database.
 * 
 * @goal redmine-wiki
 * 
 */
public class RedmineWikiMojo extends AbstractMavenReport {

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

	/**
	 * The Maven Wagon manager to use when obtaining server authentication
	 * details.
	 * 
	 * @component role="org.apache.maven.artifact.manager.WagonManager"
	 * @required
	 * @readonly
	 */
	private WagonManager wagonManager;

	/**
	 * The tomcat username to use for deployment
	 * 
	 * @parameter expression="${tomcat.username}"
	 * @since 1.0-alpha-2
	 */
	private String username;

	/**
	 * The password to use for deployment
	 * 
	 * @parameter expression="${tomcat.password}"
	 * @since 1.0-alpha-2
	 */
	private String password;

	/**
	 * The full URL of the Tomcat manager instance to use.
	 * 
	 * @parameter expression="${maven.tomcat.url}"
	 *            default-value="http://localhost:8080/manager"
	 * @required
	 */
	private URL url;

	/**
	 * The server id in settings.xml to use when authenticating with Tomcat
	 * manager, or <code>null</code> to use defaults of username
	 * <code>admin</code> and no password.
	 * 
	 * @parameter expression="${maven.tomcat.server}"
	 */
	private String server;

	@Override
	public String getOutputName() {
		return outputName;
	}

	@Override
	public String getName(Locale locale) {
		return this.getMessage(locale, "redmine-wiki-name");
	}

	@Override
	public String getDescription(Locale locale) {
		return this.getMessage(locale, "redmine-wiki-description");
	}

	@Override
	protected SiteRenderer getSiteRenderer() {
		return siteRenderer;
	}

	@Override
	protected String getOutputDirectory() {
		return outputDirectory.getPath();
	}

	@Override
	protected MavenProject getProject() {
		return this.project;
	}

	@Override
	protected void executeReport(Locale locale) throws MavenReportException {
		getLog().info("Executando hello plugin");
		AuthenticationInfo info = wagonManager.getAuthenticationInfo(server);

		if (info == null) {
			String msg = getMessage(locale, "redmine-wiki-unknown-server",
					server);
			getLog().error(msg);
			return;
		}
		username = info.getUserName();
		password = info.getPassword();
	}

	private String getMessage(Locale locale, String key, Object... params) {
		String texto = ResourceBundle.getBundle("messages", locale,
				this.getClass().getClassLoader()).getString(key);
		if (params != null) {
			return getParameterizedMessage(texto, locale, params);
		}
		return texto;
	}

	private String getParameterizedMessage(String message, Locale locale,
			Object[] params) {
		MessageFormat messageFormat = new MessageFormat(message, locale);
		message = messageFormat.format(params, new StringBuffer(), null)
				.toString();
		return message;
	}
}
