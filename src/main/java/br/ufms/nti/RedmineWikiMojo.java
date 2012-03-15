package br.ufms.nti;

import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
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

	@Override
	public String getOutputName() {
		return outputName;
	}

	@Override
	public String getName(Locale locale) {
		return this.getBundle(locale).getString("redmine-wiki-name");
	}

	@Override
	public String getDescription(Locale locale) {
		return this.getBundle(locale).getString("redmine-wiki-description");
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
	}

	private ResourceBundle getBundle(Locale locale) {
		return ResourceBundle.getBundle("messages", locale, this.getClass()
				.getClassLoader());
	}
}
