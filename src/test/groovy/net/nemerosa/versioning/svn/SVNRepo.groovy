package net.nemerosa.versioning.svn

import org.apache.commons.io.FileUtils
import org.apache.commons.lang.StringUtils
import org.tmatesoft.svn.core.SVNDepth
import org.tmatesoft.svn.core.SVNPropertyValue
import org.tmatesoft.svn.core.SVNURL
import org.tmatesoft.svn.core.io.SVNRepositoryFactory
import org.tmatesoft.svn.core.wc.SVNClientManager
import org.tmatesoft.svn.core.wc.SVNCopySource
import org.tmatesoft.svn.core.wc.SVNRevision

class SVNRepo {

    private final String repoName
    private File repo
    private SVNURL url

    SVNRepo(String repoName) {
        this.repoName = repoName
    }

    void start() {
        repo = new File("build/repo/$repoName").absoluteFile
        if (repo.exists()) repo.deleteDir()
        repo.mkdirs()
        println "SVN Test repo at ${repo.absolutePath}"
        // Creates the repository
        url = SVNRepositoryFactory.createLocalRepository(
                repo, null, true, false,
                false,
                false,
                false,
                false,
                true
        )
    }

    void stop() {
        FileUtils.forceDelete(repo)
    }

    protected static SVNClientManager getClientManager() {
        return SVNClientManager.newInstance();
    }

    def mkdir(String path, String message) {
        clientManager.commitClient.doMkDir(
                [url.appendPath(path, false)] as SVNURL[],
                message,
                null,
                true
        )
    }

    static def add(File dir, String path) {
        clientManager.getWCClient().doAdd(
                new File(dir, path),
                false,
                false,
                false,
                SVNDepth.INFINITY,
                false,
                true
        )
    }

    static def ignore(File dir, String... ignore) {
        clientManager.getWCClient().doSetProperty(
                dir,
                'svn:ignore',
                SVNPropertyValue.create(ignore.join('\n')),
                false,
                SVNDepth.EMPTY,
                null,
                []
        )
        clientManager.commitClient.doCommit(
                [dir] as File[],
                false,
                "Ignoring",
                null,
                [] as String[],
                false,
                false,
                SVNDepth.INFINITY
        )
    }

    /**
     * Checks the code into a temporary directory and returns it
     */
    File checkout(String path) {
        def wc = File.createTempDir('svn', '.wd')
        clientManager.updateClient.doCheckout(
                url.appendPath(path, false),
                wc,
                SVNRevision.HEAD,
                SVNRevision.HEAD,
                SVNDepth.INFINITY,
                false
        )
        wc
    }

    /**
     * Remote copy of {@code from} into {@code into} using the {@code message} message.
     */
    def copy(String from, String into, String message) {
        // Parsing (from)
        String fromPath
        SVNRevision fromRevision
        if (from.contains('@')) {
            fromPath = StringUtils.substringBefore(from, '@')
            fromRevision = SVNRevision.parse(StringUtils.substringAfter(from, '@'))
        } else {
            fromPath = from
            fromRevision = SVNRevision.HEAD
        }
        // Copy
        clientManager.copyClient.doCopy(
                [new SVNCopySource(SVNRevision.HEAD, fromRevision, url.appendPath(fromPath, false))] as SVNCopySource[],
                url.appendPath(into, false),
                false, // move
                true,  // make parents
                true,  // fail when exists
                message,
                null
        )
    }

    /**
     * Logs the repository history
     */
    def log() {
        println "Log for ${url}..."
        clientManager.logClient.doLog(
                url,
                null,
                SVNRevision.HEAD,
                SVNRevision.create(1),
                SVNRevision.HEAD,
                false,
                false,
                1000,
                { logEntry -> println "${logEntry.revision} ${logEntry.message}" }
        )
    }
}
