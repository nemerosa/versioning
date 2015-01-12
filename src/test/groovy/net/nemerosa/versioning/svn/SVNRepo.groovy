package net.nemerosa.versioning.svn

import net.nemerosa.versioning.support.Utils
import org.apache.commons.io.FileUtils
import org.junit.Assert

class SVNRepo {

    private final String repoName
    private File repo
    private long pid

    SVNRepo(String repoName) {
        this.repoName = repoName
    }

    static SVNRepo get(String repoName) {
        SVNRepo repo = new SVNRepo(repoName)
        repo.start()
        repo
    }

    long start() {
        repo = new File("build/repo/$repoName").absoluteFile
        FileUtils.deleteQuietly(repo)
        repo.mkdirs()
        println "SVN Test repo at ${repo.absolutePath}"
        // Creates the repository
        run repo, "svnadmin", "create", repo.absolutePath
        // Configuration file
        new File(repo, 'conf/authz').bytes = SVNRepo.class.getResourceAsStream('/svn/conf/authz').bytes
        new File(repo, 'conf/passwd').bytes = SVNRepo.class.getResourceAsStream('/svn/conf/passwd').bytes
        new File(repo, 'conf/svnserve.conf').bytes = SVNRepo.class.getResourceAsStream('/svn/conf/svnserve.conf').bytes
        // Starts serving the repository
        def pidFile = new File(repo, 'pid')
        Utils.run repo, "svnserve", "--daemon", "--root", repo.absolutePath, "--pid-file", pidFile.absolutePath
        // Waits until the PID is created
        boolean pidExists = pidFile.exists()
        int tries = 0
        while (!pidExists && tries < 5) {
            println "Waiting for SVN repo at ${repo.absolutePath} to start..."
            sleep 1000
            pidExists = pidFile.exists()
            tries++
        }
        if (!pidExists) {
            Assert.fail "The SVN repo at ${repo.absolutePath} could not start in 5 seconds."
        } else {
            this.pid = pidFile.text as long
            println "SVN server started in $repo with pid=$pid"
            // OK
            pid
        }
    }

    void stop() {
        if (pid != 0) {
            println "Stopping server with pid=$pid"
            run repo, "kill", "-KILL", "$pid"
        }
    }

    def mkdir(String path, String message) {
        run repo, 'svn', 'mkdir', '--message', message, '--parents', '--username', 'user', '--password', 'test', '--no-auth-cache', "svn://localhost/${path}"
    }

    /**
     * Merges {@code from} into {@code to} using the {@code wd} working directory.
     */
    static def merge(File wd, String from, String to, String message) {
        FileUtils.cleanDirectory wd
        // Checks the from out
        run wd, 'svn', 'checkout', "svn://localhost/${to}", wd.absolutePath
        // Merge the to
        run wd, 'svn', 'merge', '--accept', 'postpone', "svn://localhost/${from}", wd.absolutePath
        // Commit
        run wd, 'svn', 'commit', '--message', message, wd.absolutePath, '--username', 'user', '--password', 'test', '--no-auth-cache'
    }

    /**
     * Remote copy of {@code from} into {@code into} using the {@code message} message.
     */
    def copy(String from, String into, String message) {
        run repo, 'svn', 'copy', '--parents', "svn://localhost/${from}", "svn://localhost/${into}", '--message', message, '--username', 'user', '--password', 'test', '--no-auth-cache'
    }
}
