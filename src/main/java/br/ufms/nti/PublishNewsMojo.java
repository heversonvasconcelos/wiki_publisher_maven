/**
 * Copyright 2008 fastConnect.
 *
 * This file is part of maven-redmine-plugin Mojo.
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package br.ufms.nti;

import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Publish a news in Redmine.
 * 
 * @goal publish-news
 */
public class PublishNewsMojo extends AbstractRedmineMojo {

	/**
	 * News title.
	 * 
	 * @parameter expression="${newsTitle}"
	 * @required
	 */
	private String newsTitle;

	/**
	 * News description.
	 * 
	 * @parameter expression="${newsDescription}"
	 * @required
	 */
	private String newsDescription;

	/**
	 * News summary.
	 * 
	 * @parameter expression="${newsSummary}"
	 */
	private String newsSummary;

	@Override
	public void executeWhileLogged() throws MojoExecutionException,
			MojoFailureException {
		final Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("commit", "Create");
		parameters.put("news[title]", this.newsTitle);
		parameters.put("news[description]", this.newsDescription);
		if (this.newsSummary != null) {
			parameters.put("news[summary]", this.newsSummary);
		}

		executeRequest("projects/" + getProjectIdentifier() + "/news/new",
				parameters);
	}

}
