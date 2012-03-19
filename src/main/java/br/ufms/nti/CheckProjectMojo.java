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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Check that a Redmine project is created.
 * 
 * @goal check-project
 */
public class CheckProjectMojo extends AbstractRedmineMojo {

	@Override
	public void executeWhileLogged() throws MojoExecutionException,
			MojoFailureException {
		executeRequest("projects/show/" + getProjectIdentifier());
	}

}
