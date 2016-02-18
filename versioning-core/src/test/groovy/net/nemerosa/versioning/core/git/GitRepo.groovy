package net.nemerosa.versioning.core.git

import net.nemerosa.versioning.core.support.Utils

class GitRepo {

    private final File dir

    GitRepo() {
        this(File.createTempDir('git', '') as File)
    }

    GitRepo(File dir) {
        this.dir = dir
    }

    @Override
    String toString() {
        return dir.toString()
    }

    File getDir() {
        return dir
    }

    void close() {
        dir.deleteDir()
    }

    String git(String... args) {
        cmd('git', args)
    }

    String cmd(String executable, String... args) {
        def output = Utils.run(dir, executable, args)
        println output
        return output
    }

    void commit(def no) {
        String fileName = "file${no}"
        cmd 'touch', fileName
        git 'add', fileName
        git 'commit', '-m', "Commit $no"
    }

    String commitLookup(String message, boolean abbreviated = false) {
        def format = abbreviated ? '%h' : '%H'
        def info = git 'log', '--all', '--grep', message, "--pretty=format:${format}"
        if (info) {
            info.trim()
        } else {
            throw new RuntimeException("Cannot find commit for message $message")
        }
    }

}
