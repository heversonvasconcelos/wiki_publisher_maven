package br.ufms.nti.model.dao;

import java.io.File;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

import br.ufms.nti.model.generic.GenericRedmineWikiDao;
import br.ufms.nti.util.RedmineDatabaseConnector;

public class RedmineWikiDao extends GenericRedmineWikiDao {

	protected static final String SQL_CREATE_WIKI_PAGE = "INSERT INTO wiki_pages (wiki_id, title, created_on, protected) VALUES (?, ?, ?, false)";
	protected static final String SQL_CREATE_WIKI_CONTENT = "INSERT INTO wiki_contents (page_id, text, updated_on, version) VALUES (?, ?, ?, 1)";

	@Override
	public Class<?> getDomainClass() {
		return RedmineWikiDao.class;
	}

	private Date getCurrentTime() {
		return new Date(Calendar.getInstance().getTime().getTime());
	}

	@Override
	protected Long createWikiPage(Long wikiId, String wikiPageTitle) {
		try {
			Long wikiPageId = getWikiPageId(wikiPageTitle);
			if (wikiPageId != null) {
				_log.info("WikiPage already exists: " + wikiPageTitle);
				return wikiPageId;
			}

			PreparedStatement statement = RedmineDatabaseConnector
					.getDbConnection().prepareStatement(SQL_CREATE_WIKI_PAGE,
							Statement.RETURN_GENERATED_KEYS);
			statement.setLong(1, wikiId);
			statement.setString(2, wikiPageTitle);
			statement.setDate(3, getCurrentTime());

			int affectedRows = statement.executeUpdate();
			if (affectedRows != 1) {
				throw new SQLException(
						"Failed to execute the insert query, the number of affected rows is different from 1");
			}

			ResultSet rs = statement.getGeneratedKeys();
			if (rs.next()) {
				wikiPageId = rs.getLong(1);
			} else {
				throw new SQLException(
						"Failed to get ID of just-inserted Wiki Page");
			}

			return wikiPageId;
		} catch (SQLException e) {
			throw new RuntimeException(
					"Error while trying to create wiki page", e);
		}
	}

	@Override
	protected Long createWikiContent(Long wikiPageId,
			StringBuilder wikiContentData, File file) {
		try {
			Long wikiContentId = null;
			PreparedStatement statement;
			statement = RedmineDatabaseConnector.getDbConnection()
					.prepareStatement(SQL_CREATE_WIKI_CONTENT,
							Statement.RETURN_GENERATED_KEYS);
			statement.setLong(1, wikiPageId);
			statement.setString(2, wikiContentData.toString());
			statement.setDate(3, getCurrentTime());

			int affectedRows = statement.executeUpdate();
			if (affectedRows != 1) {
				throw new SQLException(
						"Failed to execute the insert query, the number of affected rows is different from 1");
			}

			ResultSet rs = statement.getGeneratedKeys();
			if (rs.next()) {
				wikiContentId = rs.getLong(1);
			} else {
				throw new SQLException(
						"Failed to get ID of just-inserted Wiki Page");
			}

			return wikiContentId;
		} catch (SQLException e) {
			throw new RuntimeException(
					"Error while trying to create wiki page", e);
		}

	}

}
