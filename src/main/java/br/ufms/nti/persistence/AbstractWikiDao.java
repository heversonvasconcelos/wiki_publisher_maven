package br.ufms.nti.persistence;

public abstract class AbstractWikiDao {

	public abstract Long getWikiPageId();

	public abstract Long getWikiContentId();

	public abstract Long createWikiPage();

	public abstract Long createWikiPageContent();
}
