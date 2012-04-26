package br.ufms.nti.model.generic;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import br.ufms.nti.util.RedmineDatabaseConnector;

public abstract class GenericRedmineWikiDao implements RedmineWikiDaoInterface {

	protected Logger _log;

	public abstract Class<?> getDomainClass();

	protected static final String SQL_GET_PROJECT_ID = "SELECT id FROM projects WHERE identifier = ?";
	protected static final String SQL_GET_WIKI_ID = "SELECT id FROM wikis WHERE project_id = ?";
	protected static final String SQL_GET_WIKI_PAGE_ID = "SELECT id FROM wiki_pages WHERE title = ?";

	public GenericRedmineWikiDao() {
		_log = Logger.getLogger(getDomainClass().getName());
	}

	/**
	 * Construtor wich initializes Redmine database access (username, password)
	 * configuration
	 * 
	 */
	public void initializeAccessConfiguration(String redmineDatabaseDriver,
			String redmineDatabaseUrl, String redmineDatabaseUsername,
			String redmineDatabasePassword) {

		RedmineDatabaseConnector.initializeAccessConfiguration(
				redmineDatabaseDriver, redmineDatabaseUrl,
				redmineDatabaseUsername, redmineDatabasePassword);
	}

	/**
	 * Finalizes Redmine database access
	 * 
	 */
	public void finalizeRedmineDatabaseAccess() throws RuntimeException {
		RedmineDatabaseConnector.closeDbConnection();
	}

	@Override
	public Long getProjectId(String projectIdentifier) {
		try {
			PreparedStatement statement = RedmineDatabaseConnector
					.getDbConnection().prepareStatement(SQL_GET_PROJECT_ID);
			statement.setString(1, projectIdentifier);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				Long projectId = rs.getLong("id");
				return projectId;
			}
			return null;
		} catch (SQLException e) {
			throw new RuntimeException("Error while trying to get project id",
					e);
		}
	}

	@Override
	public Long getWikiId(Long projectId) {
		try {
			PreparedStatement statement = RedmineDatabaseConnector
					.getDbConnection().prepareStatement(SQL_GET_WIKI_ID);
			statement.setLong(1, projectId);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				Long wikiId = rs.getLong("id");
				return wikiId;
			}
			return null;
		} catch (SQLException e) {
			throw new RuntimeException("Error while trying to get wiki id", e);
		}
	}

	@Override
	public Long getWikiPageId(String wikiPageTitle) {
		try {
			PreparedStatement statement = RedmineDatabaseConnector
					.getDbConnection().prepareStatement(SQL_GET_WIKI_PAGE_ID);
			statement.setString(1, wikiPageTitle);
			ResultSet rs = statement.executeQuery();

			if (rs.next()) {
				Long wikiPageId = rs.getLong("id");
				return wikiPageId;
			}
			return null;
		} catch (Exception e) {
			throw new RuntimeException(
					"Error while trying to retrieve the wiki page id", e);
		}
	}

	@Override
	public Long createWikiPage(Long wikiId, File file,
			String redmineWikiTextFormat) {
		String wikiPageTitle = file.getName();
		wikiPageTitle = wikiPageTitle.substring(0,
				wikiPageTitle.lastIndexOf(redmineWikiTextFormat) - 1);

		return createWikiPage(wikiId, wikiPageTitle);
	}

	protected abstract Long createWikiPage(Long wikiId, String wikiPageTitle);

	@Override
	public Long createWikiContent(String designDir, Long wikiPageId, File file) {
		String path = file.getAbsolutePath();
		path = path.substring(path.indexOf(designDir));

		StringBuilder wikiContentData = new StringBuilder();
		wikiContentData.append("{{repo_include(");
		wikiContentData.append(path);
		wikiContentData.append(")}}");

		return createWikiContent(wikiPageId, wikiContentData, file);
	}

	/**
	 * Creates wiki page content
	 * 
	 * @param wikiPageId
	 *            Wiki page id wich
	 * @param file
	 *            File that contains wiki data content
	 * @return wiki content id or null if not created
	 */
	protected abstract Long createWikiContent(Long wikiPageId,
			StringBuilder wikiContentData, File file);

}
