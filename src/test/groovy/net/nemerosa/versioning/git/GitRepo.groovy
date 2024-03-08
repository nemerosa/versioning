package net.nemerosa.versioning.git

import org.ajoberstar.grgit.Commit
import org.ajoberstar.grgit.Grgit

import java.time.ZonedDateTime

class GitRepo {

    private final File dir
    private final Grgit grgit

    GitRepo() {
        this(File.createTempDir('git', '') as File)
    }

    GitRepo(File dir) {
        this.dir = dir
        this.grgit = Grgit.init(dir: dir)
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

    void commit(def no) {
        String fileName = "file${no}"
        new File(dir, fileName).text = "Text for commit ${no}"
        grgit.add patterns: [fileName]
        grgit.commit message: "Commit $no"
    }

    void add(String... paths) {
        grgit.add patterns: paths
    }

    void branch(String name) {
        grgit.checkout(branch: name, createBranch: true)
    }

    void checkout(String name) {
        grgit.checkout(branch: name)
    }

    void tag(String name) {
        grgit.tag.add(name: name)
    }

    String commitLookup(String message, boolean abbreviated = false) {
        List<Commit> history = grgit.log()
        Commit commit = history.find { it.fullMessage.contains(message) }
        if (commit) {
            return abbreviated ? commit.abbreviatedId : commit.id
        } else {
            throw new RuntimeException("Cannot find commit for message $message")
        }
    }

    ZonedDateTime dateTimeLookup(String commitId) {
        List<Commit> history = grgit.log()
        Commit commit = history.find { it.id == commitId }
        if (commit) {
            return commit.dateTime
        } else {
            throw new RuntimeException("Cannot find commit for ID $commitId")
        }
    }
}
