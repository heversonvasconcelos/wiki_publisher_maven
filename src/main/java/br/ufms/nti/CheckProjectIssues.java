package br.ufms.nti;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * 
 * @goal check-issues
 */
public class CheckProjectIssues extends AbstractRedmineMojo {

	public void executeWhileLogged() throws MojoExecutionException,
			MojoFailureException {
		String issuesURL = "projects/" + getProjectIdentifier() + "/issues";
		String pageContent = executeRequest(issuesURL);

		String pattern = "<tr id=\"issue(.*)</tr>";
		Matcher matcher = Pattern.compile(pattern).matcher(pageContent);
		matcher.find();

		System.out.println(matcher.toMatchResult().group());
	}
}
