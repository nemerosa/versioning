package net.nemerosa.versioning.svn

import net.nemerosa.versioning.SCMInfo
import net.nemerosa.versioning.SCMInfoService
import net.nemerosa.versioning.VersioningExtension
import net.nemerosa.versioning.support.ProcessExitException
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
                    revision,
                    '',
                    isWorkingCopyDirty(project.projectDir)
            )
        }
    }

    static boolean isWorkingCopyDirty(File dir) {
        // Gets the status as XML
        String xmlStatus = run(dir, 'svn', 'status', '--xml')
        // Parsing
        def status = new XmlSlurper().parseText(xmlStatus)
        // List of entries
        def entries = status.target.entry
        if (entries.size() == 0) return false
        // Checks every entry
        def dirtyEntry = entries.find { entry ->
            def path = entry.@path.text() as String
            if (path != 'userHome') {
                def wcStatus = entry['wc-status']
                def item = wcStatus.@item.text()
                def props = wcStatus.@props.text()
                return (item != 'none' && item != 'external') || (props != 'none')
            } else {
                return false
            }
        }
        return dirtyEntry.size() > 0
    }

    static String parseBranch(String url) {
        if (url ==~ /.*\/trunk$/) {
            'trunk'
        } else {
            def m = url =~ /.*\/branches\/([^\/]+)$/
            if (m.matches()) {
                m.group(1)
            } else {
                throw new SVNInfoURLException(url)
            }
        }
    }

    @Override
    List<String> getBaseTags(Project project, VersioningExtension extension, String base) {
        // Gets the SVN raw info as XML
        String xmlInfo = run(project.projectDir, 'svn', 'info', '--xml')
        // Parsing
        def info = new XmlSlurper().parseText(xmlInfo)
        // URL
        String url = info.entry.url as String
        // Branch parsing
        String branch = parseBranch(url)
        // Gets the base URL by removing the branch
        String baseUrl
        if (branch == 'trunk') {
            baseUrl = url - branch
        } else {
            baseUrl = url - "branches/${branch}"
        }
        // Gets the list of tags
        try {
            def tags = run(project.projectDir, 'svn', 'list', "${baseUrl}/tags").readLines()
            def baseTagPattern = /(${base}\.[\d+])/
            return tags.collect { tag ->
                def m = tag =~ baseTagPattern
                if (m.find()) {
                    m.group(1)
                } else {
                    ''
                }
            }.findAll { it != '' }
        } catch (ProcessExitException ex) {
            if (ex.exit == 1) {
                // No tag yet
                []
            } else {
                throw ex
            }
        }
    }

    @Override
    String getBranchTypeSeparator() {
        '-'
    }
}
