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
 * Create a new project in Redmine.
 * 
 * @goal create-project
 */
public class CreateProjectMojo extends AbstractRedmineMojo {

	/**
	 * Project name.
	 * 
	 * @parameter default-value="${project.name}" expression="${projectName}"
	 * @required
	 */
	private String projectName;

	/**
	 * Project name.
	 * 
	 * @parameter default-value="false" expression="${projectIsPublic}"
	 */
	private String projectIsPublic;

	/**
	 * Project description.
	 * 
	 * @parameter default-value="${project.description}"
	 *            expression="${projectDescription}"
	 */
	private String projectDescription;

	/**
	 * Project home page.
	 * 
	 * @parameter default-value="${project.url}" expression="${projectHomePage}"
	 */
	private String projectHomePage;

	/**
	 * Project parent identifier.
	 * 
	 * @parameter expression="${projectParentIdentifier}"
	 */
	private String projectParentIdentifier;

	/**
	 * Project parent identifier.
	 * 
	 * @parameter expression="${projectEnabledModules}"
	 */
	private String[] projectEnabledModules;

	/**
	 * Project tracker identifiers.
	 * 
	 * @parameter expression="${projectTrackerIds}"
	 */
	private String[] projectTrackerIds;

	/**
	 * Project custom field identifiers.
	 * 
	 * @parameter
	 */
	private String[] projectCustomFieldIdentifiers;

	@Override
	public void executeWhileLogged() throws MojoExecutionException,
			MojoFailureException {
		final Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("commit", "Save");
		parameters.put("project[name]", this.projectName);
		parameters.put("project[identifier]", getProjectIdentifier());
		parameters.put("project[is_public]", this.projectIsPublic);
		if (this.projectDescription != null) {
			parameters.put("project[description]", this.projectDescription);
		}
		if (this.projectHomePage != null) {
			parameters.put("project[homepage]", this.projectHomePage);
		}
		if (this.projectParentIdentifier != null) {
			parameters.put("project[parent_id]", this.projectParentIdentifier);
		}
		if (this.projectEnabledModules != null) {
			for (final String projectEnabledModule : this.projectEnabledModules) {
				parameters.put("enabled_modules[]", projectEnabledModule);
			}
		}
		if (this.projectTrackerIds != null) {
			for (final String projectTrackerId : this.projectTrackerIds) {
				System.out.println("projectTrackerId: " + projectTrackerId);
				parameters.put("project[tracker_ids][]", projectTrackerId);
			}
		}
		if (this.projectCustomFieldIdentifiers != null) {
			for (final String projectCustomFieldIdentifier : this.projectCustomFieldIdentifiers) {
				System.out.println("projectCustomFieldIdentifier: "
						+ projectCustomFieldIdentifier);
				parameters.put("project[custom_field_ids][]",
						projectCustomFieldIdentifier);
			}
		}

		executeRequest("projects/add", parameters);
	}

}
