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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.changes.Action;
import org.apache.maven.plugin.changes.Release;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
 * Create a report based on a Redmine roadmap.
 * 
 * @goal create-report
 */
public class CreateReportProjectMojo extends AbstractRedmineMojo {

    /**
     * Project version.
     * @parameter default-value="${project.version}" expression="${projectVersion}"
     * @required
     */
    private String projectVersion;
    
    /**
     * Path of the changes.xml that will be generated.
     * @parameter default-value="${basedir}/src/changes/changes.xml" expression="${xmlPath}"
     * @required
     */
    private File xmlPath;
    
    /**
     * Mapping between changes.xml action types and redmine issue types. All your own Trackers fields should be mapped to one of: add, fix, remove, update.
     * @parameter
     * @required
     */
    private Map<String, String> trackerTypes;
    
    public void executeWhileLogged() throws MojoExecutionException, MojoFailureException {
        final String fullContent = executeRequest("projects/roadmap/"+getProjectIdentifier());   
        final String pattern = "<a href=\"/redmine/versions/show/(.*)\">"+this.projectVersion+"</a>";
        if (getLog().isDebugEnabled()) {
            getLog().debug("Trying to match <"+pattern+">");
        }
        final Matcher matcher = Pattern.compile(pattern).matcher(fullContent);
        
        final Release release = new Release();
        release.setVersion(this.projectVersion);                
        if (matcher.find()) {       
            final String releasePageContent = executeRequest("/versions/show/"+matcher.group(1));
            //required
            release.setDescription(extractDescription(releasePageContent));
            //required
            release.setDateRelease(extractDateRelease(releasePageContent));
            release.setAction(extractActions(releasePageContent));           
        } else {
            getLog().warn("No version <"+this.projectVersion+"> for project <"+getProjectIdentifier()+">");
        }
        generateChangesXML(release);
    }
    
    protected String extractDescription(final String content) {
        final Matcher matcher = Pattern.compile("<p>([^<]*)</p>").matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    protected String extractDateRelease(final String content) {
        final Matcher matcher = Pattern.compile("<p><strong>.* (.*)</strong></p>").matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "Not yet released";
    }
    
    protected List<Action> extractActions(final String content) throws MojoFailureException, MojoExecutionException {
        final List<Action> actions = new LinkedList<Action>();
        final Matcher issueMatcher = Pattern.compile("<li class=\"[^\"]*\"><a href=\"/redmine/([^>]*)\">([^#]*) #([^<]*)</a>: ([^<]*)</li>").matcher(content);
        while (issueMatcher.find()) {
            final Action action = new Action();
            final String issueUrl = issueMatcher.group(1);
            final String pageContent = executeRequest(issueUrl);
            final Matcher devMatcher = Pattern.compile(".*<td><b>Assigned to :</b></td><td><a href=\"[^\"*]*\">([^<]*)</a></td>.*").matcher(pageContent);
            if (devMatcher.matches()) {
                //required
                action.setDev(devMatcher.group(1));
            } else {
                getLog().warn("Cannot find dev in <"+issueUrl+">");
                action.setDev("UNKNOWN");
            }

            //There are four valid values: add, fix, remove, update.
            final String trackerType = issueMatcher.group(2);
            if (this.trackerTypes.containsKey(trackerType)) {
                //required
                action.setType(this.trackerTypes.get(trackerType));
            } else {
                getLog().warn("Type <"+trackerType+"> cannot be translated for action <"+issueMatcher.group(3)+">; skipping");
                continue;
            }
            action.setIssue(issueMatcher.group(3));
            action.setAction(issueMatcher.group(4));
            //TODO
            //action.setDueTo(dueTo);
            //action.setDueToEmail(dueToEmail);
            actions.add(action);
        }
        return actions;
    }
    
    protected Element addReleaseSection(final Element element, final Release release) {
        return element.addElement("release")
            .addAttribute("date", release.getDateRelease())
            .addAttribute("version", release.getVersion())
            .addAttribute("description", release.getDescription());
    }
    
    protected void generateChangesXML(final Release release) throws MojoFailureException {
        final Document document;
        final Element releaseElement;
        if (!this.xmlPath.exists()) {
            getLog().info("Creating file <"+this.xmlPath+">");
            try {
                if (this.xmlPath.getParentFile().mkdirs()) {
                    if (!this.xmlPath.createNewFile()) {
                        throw new MojoFailureException("<"+this.xmlPath+"> already exists");
                    }
                } else {
                    throw new MojoFailureException("Cannot create directory layout for <"+this.xmlPath+">");
                }
            } catch (IOException e) {
                throw new MojoFailureException(e.getMessage());
            }
            document = DocumentHelper.createDocument();
            final Element documentElement = document.addElement("document");
            documentElement.addElement("properties");
            final Element bodyElement = documentElement.addElement("body");
            
            releaseElement = addReleaseSection(bodyElement, release);
        } else {
            getLog().info("Reuse existing file <"+this.xmlPath+">");
            try {
                document = new SAXReader().read(this.xmlPath);
            } catch (DocumentException e) {
                throw new MojoFailureException(e.getMessage());
            }
            if (document.selectSingleNode("/document/body/release[@version='"+this.projectVersion+"']") != null) {   
                getLog().debug("Using existing release node for version <"+this.projectVersion+">");
                releaseElement = Element.class.cast(document.selectSingleNode("/document/body/release[@version='"+this.projectVersion+"']"));
            } else {
                getLog().debug("Creating new release node for version <"+this.projectVersion+">");
                if (document.selectSingleNode("/document/body") == null) {
                    final Element documentElement;
                    if (document.selectSingleNode("/document") == null) {
                        documentElement = document.addElement("document");
                    } else {
                        documentElement = Element.class.cast(document.selectSingleNode("/document"));
                    }
                    documentElement.addElement("body");
                }
                releaseElement = addReleaseSection(Element.class.cast(document.selectSingleNode("/document/body")), release);
            }
        }
        
        if (release.getAction() != null) {
            for (final Object object : release.getAction()) {
                final Action action = Action.class.cast(object);
                if (action.getIssue() == null || document.selectSingleNode("/document/body/release[@version='"+this.projectVersion+"']/action[@issue='"+action.getIssue()+"']") == null) {
                    releaseElement.addElement("action")
                        .addAttribute("dev", action.getDev())
                        .addAttribute("type", action.getType())
                        .addAttribute("due-to", action.getDueTo())
                        .addAttribute("due-to-email", action.getDueToEmail())
                        .addAttribute("issue", action.getIssue())
                        .addText(action.getAction());
                } else {
                    getLog().warn("Action <"+action.getIssue()+"> already exists; skipping");
                }
            }
        }
        
        try {
            final XMLWriter writer = new XMLWriter(new FileWriter(this.xmlPath), OutputFormat.createPrettyPrint());
            writer.write(document);
            writer.close();
        } catch (IOException e) {
            throw new MojoFailureException(e.getMessage());
        }
    }
    
}
