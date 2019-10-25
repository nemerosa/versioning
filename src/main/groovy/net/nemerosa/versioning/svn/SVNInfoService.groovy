package net.nemerosa.versioning.svn

import net.nemerosa.versioning.SCMInfo
import net.nemerosa.versioning.SCMInfoService
import net.nemerosa.versioning.VersioningExtension
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.tmatesoft.svn.core.SVNDepth
import org.tmatesoft.svn.core.SVNDirEntry
import org.tmatesoft.svn.core.SVNException
import org.tmatesoft.svn.core.SVNURL
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager
import org.tmatesoft.svn.core.auth.SVNAuthentication
import org.tmatesoft.svn.core.wc.*

class SVNInfoService implements SCMInfoService {

    private static final Logger LOGGER = Logging.getLogger(this.getClass())

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

            // Check passed in environment variable list
            String branch = null
            for (ev in extension.branchEnv) {
                if (System.env[ev] != null) {
                    branch = System.env[ev]
                    break
                }
            }
            // Branch parsing from URL
            if (branch == null) {
                String url = info.URL as String
                branch = parseBranch(url)
            }

            // Revision
            String revision = info.committedRevision.number as String
            // OK
            new SCMInfo(
                    branch,
                    revision,
                    revision,
                    null,
                    null,
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
            def m = url =~ /.*\/(?:branches|tags)\/([^\/]+)$/
            if (m.matches()) {
                m.group(1)
            } else {
                throw new SVNInfoURLException(url)
            }
        }
    }

    @Override
    List<String> getBaseTags(Project project, VersioningExtension extension, String base) {
        return getLastTags(
                project,
                extension,
                /(${base}\.(\d+))/
        )
    }

    @Override
    List<String> getLastTags(Project project, VersioningExtension extension, String tagPattern) {
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
        LOGGER.info("[version] Getting list of tags from ${tagsUrl}...")
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
        return tags.collect { tag ->
            def m = tag =~ tagPattern
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
        ISVNAuthenticationManager authenticationManager
        if (extension.user && extension.password) {
            LOGGER.info("[version] Authenticating with ${extension.user}")
            authenticationManager = configureProxy(BasicAuthenticationManager.newInstance(extension.user, extension.password.toCharArray()))
            // The BasicAuthenticationManager trusts the certificates by default
        } else if (extension.trustServerCert) {
            LOGGER.info("[version] Trusting certificate by default")
            LOGGER.warn("[version] WARNING The `trustServerCert` is now deprecated - and should not be used any longer.")
            authenticationManager = configureProxy(BasicAuthenticationManager.newInstance(new SVNAuthentication[0]))
        } else {
            LOGGER.info("[version] Using default SVN configuration")
            authenticationManager = SVNWCUtil.createDefaultAuthenticationManager()
        }
        clientManager.setAuthenticationManager(authenticationManager)
        return clientManager
    }

    private static BasicAuthenticationManager configureProxy(BasicAuthenticationManager authenticationManager) {
        def properties= System.properties
        def proxyHost = properties.getProperty('http.proxyHost')
        def proxyPort = properties.getProperty('http.proxyPort')
        def proxyUser = properties.getProperty('http.proxyUser')
        def proxyPassword = properties.getProperty('http.proxyPassword')
        if (proxyHost != null && proxyPort != null) {
            if (proxyUser != null && proxyPassword != null) {
                authenticationManager.setProxy(proxyHost, proxyPort as int, proxyUser, proxyPassword.toCharArray())
            } else {
                authenticationManager.setProxy(proxyHost, proxyPort as int, null, [] as char[])
            }
        }

        return authenticationManager
    }

}
