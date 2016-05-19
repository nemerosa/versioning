package net.nemerosa.versioning.svn

import org.apache.commons.io.FileUtils
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

    File getDir() {
        repo
    }

    SVNURL getUrl() {
        url
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
        // TODO Configuration file
//        new File(repo, 'conf/authz').bytes = SVNRepo.class.getResourceAsStream('/svn/conf/authz').bytes
//        new File(repo, 'conf/passwd').bytes = SVNRepo.class.getResourceAsStream('/svn/conf/passwd').bytes
//        new File(repo, 'conf/svnserve.conf').bytes = SVNRepo.class.getResourceAsStream('/svn/conf/svnserve.conf').bytes
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

    static def ignore(File dir, String ignore) {
        clientManager.getWCClient().doSetProperty(
                dir,
                'svn:ignore',
                SVNPropertyValue.create(ignore),
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
     * Merges {@code from} into {@code to} using the {@code wd} working directory.
     */
    def merge(File wd, String from, String to, String message) {
        if (wd.exists()) wd.deleteDir()
        wd.mkdirs()
        // Checks the `to` out
        clientManager.updateClient.doCheckout(
                url.appendPath(from, false),
                wd,
                SVNRevision.HEAD,
                SVNRevision.HEAD,
                SVNDepth.INFINITY,
                false
        )
        // Merge the `from`
        clientManager.diffClient.doMerge(
                url.appendPath(from, false),
                SVNRevision.HEAD,
                wd,
                SVNRevision.HEAD,
                wd,
                SVNDepth.INFINITY,
                true,
                false,
                false,
                false
        )
        // Commit
        clientManager.commitClient.doCommit(
                [wd] as File[],
                false,
                message,
                null,
                [] as String[],
                false,
                false,
                SVNDepth.INFINITY
        )
    }

    /**
     * Remote copy of {@code from} into {@code into} using the {@code message} message.
     */
    def copy(String from, String into, String message) {
        clientManager.copyClient.doCopy(
                [new SVNCopySource(SVNRevision.HEAD, SVNRevision.HEAD, url.appendPath(from, false))] as SVNCopySource[],
                url.appendPath(into, false),
                false,
                true,
                true,
                message,
                null
        )
    }
}
