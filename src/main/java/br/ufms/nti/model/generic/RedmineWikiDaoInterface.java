package br.ufms.nti.model.generic;

import java.io.File;

public interface RedmineWikiDaoInterface {

	/**
	 * Gets database project id by project identifier.
	 * 
	 * @return project id or null if not found
	 */
	public Long getProjectId(String projectIdentifier);

	/**
	 * Gets database wiki id by project id
	 * 
	 * @return wiki id or null if not found
	 */
	public Long getWikiId(Long projectId);

	/**
	 * Gets database wiki page id by wiki page title
	 * 
	 * @param wikiPageTitle
	 * @return wiki page id or null if not found
	 */
	public Long getWikiPageId(String wikiPageTitle);

	/**
	 * Creates wiki page with title from a file name
	 * 
	 * @param file
	 * @return wiki page id or null if not created
	 */
	public Long createWikiPage(Long wikiId, File file,
			String redmineWikiTextFormat);

	/**
	 * Creates wiki page
	 * 
	 * @param wikiPageTitle
	 *            Wiki page title
	 * @return wiki page id or null if not created
	 */
	public Long createWikiContent(String designDir, Long wikiPageId, File file);

}
