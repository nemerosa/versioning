package net.nemerosa.versioning.svn

import net.nemerosa.versioning.SCMInfo
import net.nemerosa.versioning.SCMInfoService
import net.nemerosa.versioning.VersioningExtension
import org.gradle.api.Project

import static net.nemerosa.versioning.support.Utils.run

class SVNInfoService implements SCMInfoService {

    @Override
    SCMInfo getInfo(Project project, VersioningExtension extension) {
        // Is SVN enabled?
        boolean hasSvn = project.file('.svn').exists()
        // No SVN information
        if (!hasSvn) {
            SCMInfo.NONE
        } else {
            // Gets the SVN raw info as XML
            String xmlInfo = run(project.projectDir, 'svn', 'info', '--xml')
            // Parsing
            def info = new XmlSlurper().parseText(xmlInfo)
            // URL
            String url = info.entry.url as String
            // Branch parsing
            String branch = parseBranch(url)
            // Revision
            String revision = info.entry.commit.@revision as String
            // OK
            new SCMInfo(
                    branch,
                    revision,
                    revision
            )
        }
    }

    static String parseBranch(String url) {
        if (url ==~ /.*\/trunk$/) {
            'trunk'
        } else {
            def m = url =~ /.*\/branches\/([^\/])$/
            if (m.matches()) {
                m.group(1)
            } else {
                throw new SVNInfoURLException(url)
            }
        }
    }

    @Override
    List<String> getBaseTags(Project project, VersioningExtension extension, String base) {
        // FIXME Method net.nemerosa.versioning.SCMInfoService.getBaseTags
        return null
    }
}
