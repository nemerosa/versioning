package net.nemerosa.versioning.svn

import net.nemerosa.versioning.SCMInfo
import net.nemerosa.versioning.SCMInfoService
import net.nemerosa.versioning.VersioningExtension
import net.nemerosa.versioning.support.ProcessExitException
import org.gradle.api.Project
import org.tmatesoft.svn.core.SVNDepth
import org.tmatesoft.svn.core.wc.*

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
            // Gets the client manager
            def clientManager = getClientManager(extension)
            // Gets the SVN information
            SVNInfo info = clientManager.getWCClient().doInfo(
                    project.projectDir,
                    SVNRevision.HEAD
            )
            // URL
            String url = info.URL as String
            // Branch parsing
            String branch = parseBranch(url)
            // Revision
            String revision = info.committedRevision.number as String
            // OK
            new SCMInfo(
                    branch,
                    revision,
                    revision,
                    '',
                    isWorkingCopyDirty(project.projectDir, clientManager)
            )
        }
    }

    static boolean isWorkingCopyDirty(File dir, SVNClientManager clientManager) {
        // Gets the status
        List<SVNStatus> statuses = []
        clientManager.statusClient.doStatus(
                dir,
                SVNRevision.WORKING,
                SVNDepth.INFINITY,
                false,
                false,
                false,
                false,
                { SVNStatus status -> statuses.add(status) },
                null
        )
        // List of entries
        if (statuses.empty) return false
        // Checks every entry
        def dirtyEntry = statuses.find { entry ->
            def path = entry.repositoryRelativePath
            if (path && !path.startsWith('userHome')) {
                return (entry.nodeStatus != SVNStatusType.UNCHANGED && entry.nodeStatus != SVNStatusType.STATUS_EXTERNAL) || (entry.propertiesStatus != SVNStatusType.UNCHANGED)
            } else {
                return false
            }
        }
        return dirtyEntry != null
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
            String tagsUrl = "${baseUrl}/tags"
            println "[version] Getting list of tags from ${tagsUrl}..."
            // Command arguments
            List<String> args = ['list', '--xml', '--non-interactive']
            // Credentials
            if (extension.user) {
                println "[version] Authenticating with ${extension.user}"
                args.addAll([
                        '--no-auth-cache',
                        '--username', extension.user,
                        '--password', extension.password,
                ])
            }
            // Certificate
            if (extension.trustServerCert) {
                println "[version] Trusting certificate by default"
                args << '--trust-server-cert'
            }
            // Tags folder
            args << tagsUrl
            // Command
            def xmlTags = run(project.projectDir, 'svn', args as String[])
            // Parsing
            def lists = new XmlSlurper().parseText(xmlTags)
            // Lists of tags, order from the most recent to the oldest
            List<String> tags = lists.list.entry.sort { -(it.commit.@revision.text() as long) }.collect { it.name }
            // Keeping only tags which fit the release pattern
            def baseTagPattern = /(${base}\.(\d+))/
            return tags.collect { tag ->
                def m = tag =~ baseTagPattern
                if (m.find()) {
                    m.group(1)
                } else {
                    ''
                }
            }.findAll { it != '' }
        } catch (ProcessExitException ex) {
            if (ex.exit == 1 && ex.message.contains('E200009')) {
                println "[version] The tags/ folder does not exist yet"
                return []
            } else {
                throw ex
            }
        }
    }

    @Override
    String getBranchTypeSeparator() {
        '-'
    }

    /**
     * Creates the client manager
     */
    protected static SVNClientManager getClientManager(VersioningExtension extension) {
        return SVNClientManager.newInstance()
        // TODO Authentication
    }

}
