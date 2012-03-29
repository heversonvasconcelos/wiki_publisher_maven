package br.ufms.nti;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.codehaus.doxia.site.renderer.SiteRenderer;

/**
 * Provides basic methods to interact with Redmine.
 */
public abstract class AbstractRedmineMojo extends AbstractMavenReport {

	final private DefaultHttpClient httpClient = new DefaultHttpClient();

	/**
	 * Redmine url.
	 * 
	 * @parameter default-value="${project.issueManagement.url}"
	 *            expression="${redmineUrl}"
	 * @required
	 */
	private String url;

	/**
	 * Username used to log in redmine.
	 * 
	 * @parameter expression="${redmineUsername}"
	 */
	private String username;

	/**
	 * Password used to log in redmine.
	 * 
	 * @parameter expression="${redminePassword}"
	 */
	private String password;

	/**
	 * The maven project
	 * 
	 * @parameter expression="${project}"
	 * @readonly
	 */
	private MavenProject project;

	/**
	 * Project identifier.
	 * 
	 * @parameter default-value="${project.artifactId}"
	 *            expression="${projectIdentifier}"
	 * @required
	 */
	private String projectIdentifier;

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
	 * The server id in settings.xml to use when authenticating with Tomcat
	 * manager, or <code>null</code> to use defaults of username
	 * <code>admin</code> and no password.
	 * 
	 * @parameter expression="${maven.tomcat.server}"
	 * @required
	 */
	private String redmineServer;

	/**
	 * The server id in settings.xml to use when authenticating with Tomcat
	 * manager, or <code>null</code> to use defaults of username
	 * <code>admin</code> and no password.
	 * 
	 * @parameter expression="${maven.tomcat.server}"
	 * @required
	 */
	private String redmineDatabaseServer;

	private String authenticityToken;

	protected final String getProjectIdentifier() {
		return this.projectIdentifier;
	}

	private String getRedmineURL() {
		if (this.url.matches(".*/projects/show/.*")) {
			return this.url.substring(0, this.url.indexOf("/projects/show/"));
		}
		return this.url;
	}

	private String generateURI(final String suffix) {
		final String normalizedSuffix = suffix.startsWith("/") ? suffix
				.substring(1) : suffix;
		if (getRedmineURL().endsWith("/")) {
			return getRedmineURL() + normalizedSuffix;
		} else {
			return getRedmineURL() + "/" + normalizedSuffix;
		}
	}

	/**
	 * Read the full content of an {@link HttpResponse} and returns it as a
	 * {@link String}.
	 * 
	 * @param httpResponse
	 *            the {@link HttpResponse} to consume
	 * @return the full page
	 * @throws IOException
	 *             if an error occurred while reading the page
	 */
	private static String readFully(final HttpResponse httpResponse)
			throws IOException {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(
				httpResponse.getEntity().getContent()));
		try {
			final StringBuilder fullContent = new StringBuilder();
			String line = reader.readLine();
			while (line != null) {
				line = reader.readLine();
				fullContent.append(line);
			}
			return fullContent.toString();
		} finally {
			reader.close();
		}
	}

	/**
	 * Ensures that no errors have been returned by redmine.
	 * 
	 * @param content
	 *            an HTML page
	 * @throws MojoExecutionException
	 *             if redmine returned any errors
	 */
	private void validateResult(final String content)
			throws MojoExecutionException {
		final String errorPattern = ".*<div class=\"errorExplanation\" id=\"errorExplanation\"><span>.*</span><ul><li>(.*)</li></ul></div>.*";
		final Matcher matcher = Pattern.compile(errorPattern).matcher(content);
		if (matcher.matches()) {
			matcher.reset();
			while (matcher.find()) {
				final String errors = matcher.group(1).replaceAll("&[^&]*; ",
						"");
				for (final String error : errors.split("</li><li>")) {
					getLog().warn(error);
				}
			}
			throw new MojoExecutionException("Errors while executing request");
		}

		final String loginFailedPattern = ".*<div class=\"flash error\">([^<]*)</div>.*";
		final Matcher loginMatcher = Pattern.compile(loginFailedPattern)
				.matcher(content);
		if (loginMatcher.matches()) {
			matcher.reset();
			while (loginMatcher.find()) {
				final String errors = loginMatcher.group(1).replaceAll(
						"&[^&]*; ", "");
				for (final String error : errors.split("</li><li>")) {
					getLog().warn(error);
				}
			}
			throw new MojoExecutionException("Errors while executing request");
		}
	}

	/**
	 * Searches in redmine page code, the authenticy token used to identify the
	 * login session.
	 * 
	 * @param content
	 *            an HTML page
	 * @return the authenticity token
	 */
	private String getAuthenticityToken(final String content) {
		String pattern = "<meta name=\"csrf-token\" content=\"(.*.)\"/>";
		Matcher matcher = Pattern.compile(pattern).matcher(content);

		if (matcher.find()) {
			authenticityToken = matcher.toMatchResult().group()
					.substring(33, 77);
		}

		return authenticityToken;
	}

	@Override
	protected void executeReport(Locale locale) throws MavenReportException {
		try {
			login(locale);
			executeWhileLogged();
		} catch (MojoFailureException e) {
			System.out.println(e.getMessage());
		} catch (MojoExecutionException e) {
			System.out.println(e.getMessage());
		}
	}

	protected abstract void executeWhileLogged() throws MojoFailureException,
			MojoExecutionException;

	private String login(Locale locale) throws MojoExecutionException,
			MojoFailureException {
		try {

			AuthenticationInfo info = wagonManager
					.getAuthenticationInfo(redmineServer);

			if (info == null) {
				String msg = getMessage(locale, "redmine-wiki-unknown-server",
						redmineServer);
				getLog().error(msg);
				return null;
			}
			username = info.getUserName();
			password = info.getPassword();

			final Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("username", this.username);
			parameters.put("password", this.password);

			String pageContent = executeRequest("login", parameters);
			authenticityToken = getAuthenticityToken(pageContent);

			return pageContent;

		} catch (MojoExecutionException e) {
			getLog().error(
					"Unable to login to <" + getRedmineURL() + "> with user <"
							+ this.username + ">");
			throw e;
		} catch (MojoFailureException e) {
			getLog().error(
					"Unable to login to <" + getRedmineURL() + "> with user <"
							+ this.username + ">");
			throw e;
		}
	}

	/**
	 * Access a request using GET method
	 * 
	 */
	protected String executeRequest(final String requestURI)
			throws MojoFailureException, MojoExecutionException {

		HttpGet request;
		try {
			request = new HttpGet(generateURI(requestURI));
		} catch (URISyntaxException e) {
			throw new MojoFailureException(e.getMessage());
		}

		return executeRequest(request);
	}

	/**
	 * Access a request using POST method
	 * 
	 */
	protected String executeRequest(final String requestURI,
			final Map<String, String> parameters) throws MojoFailureException,
			MojoExecutionException {

		HttpPost request;
		try {
			request = new HttpPost(generateURI(requestURI));
			if (parameters != null) {
				final List<NameValuePair> valuePairs = new ArrayList<NameValuePair>();
				if (authenticityToken != null) {
					valuePairs.add(new BasicNameValuePair("authenticity_token",
							authenticityToken));
				}
				for (final Map.Entry<String, String> parameter : parameters
						.entrySet()) {
					valuePairs.add(new BasicNameValuePair(parameter.getKey(),
							parameter.getValue()));
				}
				request.setEntity(new UrlEncodedFormEntity(valuePairs,
						HTTP.UTF_8));
			}
		} catch (Exception e) {
			throw new MojoFailureException(e.getMessage());
		}

		return executeRequest(request);
	}

	/**
	 * Access a request, read the content, validate it.
	 * 
	 */
	private String executeRequest(final HttpUriRequest request)
			throws MojoFailureException, MojoExecutionException {
		try {
			final HttpResponse response = this.httpClient.execute(request);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				response.getEntity().consumeContent();
				throw new MojoExecutionException("Got error code <"
						+ response.getStatusLine().getStatusCode() + ":"
						+ response.getStatusLine().getReasonPhrase()
						+ "> while accessing " + request.getRequestLine() + "s");
			} else {
				final String pageContent = readFully(response);
				validateResult(pageContent);
				return pageContent;
			}
		} catch (MojoExecutionException e) {
			throw e;
		} catch (Exception e) {
			throw new MojoFailureException(e.getMessage());
		}
	}

	protected String getMessage(Locale locale, String key, Object... params) {
		String texto = ResourceBundle.getBundle("messages", locale,
				this.getClass().getClassLoader()).getString(key);
		if (params != null) {
			return getParameterizedMessage(texto, locale, params);
		}
		return texto;
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