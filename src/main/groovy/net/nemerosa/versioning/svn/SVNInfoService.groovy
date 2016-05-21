package net.nemerosa.versioning.svn

import net.nemerosa.versioning.SCMInfo
import net.nemerosa.versioning.SCMInfoService
import net.nemerosa.versioning.VersioningExtension
import org.gradle.api.Project
import org.tmatesoft.svn.core.SVNDepth
import org.tmatesoft.svn.core.SVNDirEntry
import org.tmatesoft.svn.core.SVNException
import org.tmatesoft.svn.core.SVNURL
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager
import org.tmatesoft.svn.core.auth.SVNAuthentication
import org.tmatesoft.svn.core.wc.*

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
            def path = (entry.file.absolutePath - dir.absolutePath)
            if (path && !path.startsWith('/userHome')) {
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
        // Gets the base URL by removing the branch
        String baseUrl
        if (branch == 'trunk') {
            baseUrl = url - branch
        } else {
            baseUrl = url - "branches/${branch}"
        }
        // Gets the list of tags
        String tagsUrl = "${baseUrl}/tags"
        println "[version] Getting list of tags from ${tagsUrl}..."
        // Gets the list
        List<SVNDirEntry> entries = []
        try {
            clientManager.logClient.doList(
                    SVNURL.parseURIEncoded(tagsUrl),
                    SVNRevision.HEAD,
                    SVNRevision.HEAD,
                    false,
                    false,
                    { dirEntry -> entries.add(dirEntry) }
            )
        } catch (SVNException ex) {
            if (ex.message.contains('E160013')) {
                // No tag
                return []
            } else {
                // Actual problem
                throw ex
            }
        }
        // Lists of tags, order from the most recent to the oldest
        List<String> tags = entries.sort {
            -it.revision
        }.collect {
            it.name
        }
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
    }

    @Override
    String getBranchTypeSeparator() {
        '-'
    }

    /**
     * Creates the client manager
     */
    protected static SVNClientManager getClientManager(VersioningExtension extension) {
        def clientManager = SVNClientManager.newInstance()
        if (extension.user && extension.password) {
            println "[version] Authenticating with ${extension.user}"
            clientManager.setAuthenticationManager(BasicAuthenticationManager.newInstance(extension.user, extension.password.toCharArray()));
            // The BasicAuthenticationManager trusts the certificates by default
        } else if (extension.trustServerCert) {
            println "[version] Trusting certificate by default"
            clientManager.setAuthenticationManager(BasicAuthenticationManager.newInstance(new SVNAuthentication[0]));
        }
        return clientManager
    }

}
