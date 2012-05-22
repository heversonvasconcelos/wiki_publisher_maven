package br.ufms.nti;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;

import br.ufms.nti.model.dao.RedmineWikiDao;
import br.ufms.nti.model.generic.GenericRedmineWikiDao;

/**
 * Goal which publish the wiki files to a redmine database.
 * 
 * @goal publish-redmine-wiki
 */
@SuppressWarnings("unchecked")
public class PublishRedmineWikiMojo extends AbstractMojo {

	/**
	 * Project identifier.
	 * 
	 * @parameter default-value="${project.artifactId}"
	 *            expression="${projectIdentifier}"
	 * @required
	 */
	private String projectIdentifier;

	/**
	 * Project design directory wich store the documentation. Example: design
	 * 
	 * @parameter expression="${designDir}" default-value="design"
	 */
	private String designDir;

	/**
	 * Project SCM (Source Control Management) directory where designDir is.
	 * Example: trunk
	 * 
	 * @parameter expression="${designSCMDir}" default-value="trunk"
	 */
	private String designSCMDir;

	/**
	 * Redmine wiki text format. Example: Textile, MediaWiki
	 * 
	 * @parameter expression="${redmineWikiTextFormat}" default-value="textile"
	 */
	private String redmineWikiTextFormat;

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
	 * Redmine database driver
	 * 
	 * @parameter expression="${redmineDatabaseDriver}"
	 * @required
	 */
	private String redmineDatabaseDriver;

	private Long projectId;
	private Long wikiId;
	private GenericRedmineWikiDao redmineWikiDao;

	public void intializePublishRedmineWikiMojo() {
		AuthenticationInfo info = wagonManager
				.getAuthenticationInfo(redmineDatabase);

		String redmineDatabaseUsername = info.getUserName();
		String redmineDatabasePassword = info.getPassword();

		redmineWikiDao = new RedmineWikiDao();
		redmineWikiDao.initializeAccessConfiguration(redmineDatabaseDriver,
				redmineDatabaseUrl, redmineDatabaseUsername,
				redmineDatabasePassword);
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		intializePublishRedmineWikiMojo();

		projectId = redmineWikiDao.getProjectId(projectIdentifier);
		wikiId = redmineWikiDao.getWikiId(projectId);

		File designDirFile = new File(designDir);
		Collection<File> files = FileUtils.listFiles(designDirFile,
				new String[] { redmineWikiTextFormat }, true);

		for (File file : files) {
			Long wikiPageId = redmineWikiDao.createWikiPage(wikiId, file,
					redmineWikiTextFormat);
			if (wikiPageId != null) {
				redmineWikiDao.createWikiContent(designDir, designSCMDir,
						wikiPageId, file);
			}
		}

		redmineWikiDao.finalizeRedmineDatabaseAccess();
	}

}
