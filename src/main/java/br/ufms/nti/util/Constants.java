package br.ufms.nti.util;

/**
 * Constants definition
 * 
 */
public class Constants {

	/*
	 * 
	 * 
	 * Queries definitions
	 */
	public static final String SQL_SELECT_TEST = "SELECT 1 FROM wikis";
	public static final String SQL_GET_PROJECT_ID = "SELECT id FROM projects WHERE identifier = ?";
	public static final String SQL_GET_WIKI_ID = "SELECT id FROM wikis WHERE project_id = ?";
	public static final String SQL_CREATE_WIKI_PAGE = "INSERT INTO wiki_pages (wiki_id, title, created_on, protected) VALUES (?, ?, now(), false) RETURNING id";
	public static final String SQL_CREATE_WIKI_CONTENT = "INSERT INTO wiki_contents (page_id, text, updated_on, version) VALUES (?, ?, now(), 1) RETURNING id";
	public static final String SQL_GET_WIKI_PAGE_ID = "SELECT id FROM wiki_pages WHERE title = ?";
}
