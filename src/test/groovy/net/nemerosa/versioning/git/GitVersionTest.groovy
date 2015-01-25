package net.nemerosa.versioning.git

import net.nemerosa.versioning.VersionInfo
import net.nemerosa.versioning.VersioningPlugin
import net.nemerosa.versioning.support.DirtyException
import net.nemerosa.versioning.tasks.VersionDisplayTask
import org.gradle.api.DefaultTask
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class GitVersionTest {

    @Test
    void 'Git not present'() {
        def wd = File.createTempDir('git', '')
        def project = ProjectBuilder.builder().withProjectDir(wd).build()
        new VersioningPlugin().apply(project)
        VersionInfo info = project.versioning.info as VersionInfo
        assert info != null
        assert info == VersionInfo.NONE
        assert info.build == ''
        assert info.branch == ''
        assert info.base == ''
        assert info.branchId == ''
        assert info.branchType == ''
        assert info.branch == ''
        assert info.commit == ''
        assert info.display == ''
        assert info.full == ''
        assert info.scm == 'n/a'
    }

    @Test
    void 'Git master'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                git 'init'
                (1..4).each { commit it }
                git 'log', '--oneline', '--graph', '--decorate', '--all'
            }
            def head = repo.commitLookup('Commit 4')
            def headAbbreviated = repo.commitLookup('Commit 4', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            VersionInfo info = project.versioning.info as VersionInfo
            assert info != null
            assert info.build == headAbbreviated
            assert info.branch == 'master'
            assert info.base == ''
            assert info.branchId == 'master'
            assert info.branchType == 'master'
            assert info.commit == head
            assert info.display == "master-${headAbbreviated}"
            assert info.full == "master-${headAbbreviated}"
            assert info.scm == 'git'

        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git: display version'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                git 'init'
                (1..4).each { commit it }
            }
            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            def task = project.tasks.getByName('versionDisplay') as VersionDisplayTask
            task.execute()

        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git: version file - defaults'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                git 'init'
                (1..4).each { commit it }
            }
            def head = repo.commitLookup('Commit 4')
            def headAbbreviated = repo.commitLookup('Commit 4', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            def task = project.tasks.getByName('versionFile') as DefaultTask
            task.execute()

            // Checks the file
            def file = new File(project.buildDir, 'version.properties')
            assert file.exists(): "File ${file} must exist."
            assert file.text == """\
VERSION_BUILD = ${headAbbreviated}
VERSION_BRANCH = master
VERSION_BASE = \n\
VERSION_BRANCHID = master
VERSION_BRANCHTYPE = master
VERSION_COMMIT = ${head}
VERSION_DISPLAY = master-${headAbbreviated}
VERSION_FULL = master-${headAbbreviated}
VERSION_SCM = git
"""
        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git: version file - custom prefix'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                git 'init'
                (1..4).each { commit it }
            }
            def head = repo.commitLookup('Commit 4')
            def headAbbreviated = repo.commitLookup('Commit 4', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            project.versionFile {
                prefix = 'CUSTOM_'
            }
            def task = project.tasks.getByName('versionFile') as DefaultTask
            task.execute()

            // Checks the file
            def file = new File(project.buildDir, 'version.properties')
            assert file.exists(): "File ${file} must exist."
            assert file.text == """\
CUSTOM_BUILD = ${headAbbreviated}
CUSTOM_BRANCH = master
CUSTOM_BASE = \n\
CUSTOM_BRANCHID = master
CUSTOM_BRANCHTYPE = master
CUSTOM_COMMIT = ${head}
CUSTOM_DISPLAY = master-${headAbbreviated}
CUSTOM_FULL = master-${headAbbreviated}
CUSTOM_SCM = git
"""
        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git: version file - custom file'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                git 'init'
                (1..4).each { commit it }
            }
            def head = repo.commitLookup('Commit 4')
            def headAbbreviated = repo.commitLookup('Commit 4', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            project.versionFile {
                file = new File(repo.dir, '.version')
            }
            def task = project.tasks.getByName('versionFile') as DefaultTask
            task.execute()

            // Checks the file
            def file = new File(project.projectDir, '.version')
            assert file.exists(): "File ${file} must exist."
            assert file.text == """\
VERSION_BUILD = ${headAbbreviated}
VERSION_BRANCH = master
VERSION_BASE = \n\
VERSION_BRANCHID = master
VERSION_BRANCHTYPE = master
VERSION_COMMIT = ${head}
VERSION_DISPLAY = master-${headAbbreviated}
VERSION_FULL = master-${headAbbreviated}
VERSION_SCM = git
"""
        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git feature branch'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                git 'init'
                (1..4).each { commit it }
                git 'checkout', '-b', 'feature/123-great'
                commit 5
                git 'log', '--oneline', '--graph', '--decorate', '--all'
            }
            def head = repo.commitLookup('Commit 5')
            def headAbbreviated = repo.commitLookup('Commit 5', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            VersionInfo info = project.versioning.info as VersionInfo
            assert info != null
            assert info.build == headAbbreviated
            assert info.branch == 'feature/123-great'
            assert info.base == '123-great'
            assert info.branchId == 'feature-123-great'
            assert info.branchType == 'feature'
            assert info.commit == head
            assert info.display == "feature-123-great-${headAbbreviated}"
            assert info.full == "feature-123-great-${headAbbreviated}"
            assert info.scm == 'git'

        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git feature branch with full display mode'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                git 'init'
                (1..4).each { commit it }
                git 'checkout', '-b', 'feature/123-great'
                commit 5
                git 'log', '--oneline', '--graph', '--decorate', '--all'
            }
            def head = repo.commitLookup('Commit 5')
            def headAbbreviated = repo.commitLookup('Commit 5', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            project.versioning {
                displayMode = 'full'
            }
            VersionInfo info = project.versioning.info as VersionInfo
            assert info != null
            assert info.build == headAbbreviated
            assert info.branch == 'feature/123-great'
            assert info.base == '123-great'
            assert info.branchId == 'feature-123-great'
            assert info.branchType == 'feature'
            assert info.commit == head
            assert info.display == "feature-123-great-${headAbbreviated}"
            assert info.full == "feature-123-great-${headAbbreviated}"
            assert info.scm == 'git'

        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git feature branch with snapshot mode'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                git 'init'
                (1..4).each { commit it }
                git 'checkout', '-b', 'feature/123-great'
                commit 5
                git 'log', '--oneline', '--graph', '--decorate', '--all'
            }
            def head = repo.commitLookup('Commit 5')
            def headAbbreviated = repo.commitLookup('Commit 5', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            project.versioning {
                displayMode = 'snapshot'
            }
            VersionInfo info = project.versioning.info as VersionInfo
            assert info != null
            assert info.build == headAbbreviated
            assert info.branch == 'feature/123-great'
            assert info.base == '123-great'
            assert info.branchId == 'feature-123-great'
            assert info.branchType == 'feature'
            assert info.commit == head
            assert info.display == "123-great-SNAPSHOT"
            assert info.full == "feature-123-great-${headAbbreviated}"
            assert info.scm == 'git'

        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git feature branch with custom snapshot mode'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                git 'init'
                (1..4).each { commit it }
                git 'checkout', '-b', 'feature/123-great'
                commit 5
                git 'log', '--oneline', '--graph', '--decorate', '--all'
            }
            def head = repo.commitLookup('Commit 5')
            def headAbbreviated = repo.commitLookup('Commit 5', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            project.versioning {
                displayMode = 'snapshot'
                snapshot = '.DEV'
            }
            VersionInfo info = project.versioning.info as VersionInfo
            assert info != null
            assert info.build == headAbbreviated
            assert info.branch == 'feature/123-great'
            assert info.base == '123-great'
            assert info.branchId == 'feature-123-great'
            assert info.branchType == 'feature'
            assert info.commit == head
            assert info.display == "123-great.DEV"
            assert info.full == "feature-123-great-${headAbbreviated}"
            assert info.scm == 'git'

        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git feature branch with base mode'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                git 'init'
                (1..4).each { commit it }
                git 'checkout', '-b', 'feature/123-great'
                commit 5
                git 'log', '--oneline', '--graph', '--decorate', '--all'
            }
            def head = repo.commitLookup('Commit 5')
            def headAbbreviated = repo.commitLookup('Commit 5', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            project.versioning {
                displayMode = 'base'
            }
            VersionInfo info = project.versioning.info as VersionInfo
            assert info != null
            assert info.build == headAbbreviated
            assert info.branch == 'feature/123-great'
            assert info.base == '123-great'
            assert info.branchId == 'feature-123-great'
            assert info.branchType == 'feature'
            assert info.commit == head
            assert info.display == "123-great"
            assert info.full == "feature-123-great-${headAbbreviated}"
            assert info.scm == 'git'

        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git feature branch with custom mode'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                git 'init'
                (1..4).each { commit it }
                git 'checkout', '-b', 'feature/123-great'
                commit 5
                git 'log', '--oneline', '--graph', '--decorate', '--all'
            }
            def head = repo.commitLookup('Commit 5')
            def headAbbreviated = repo.commitLookup('Commit 5', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            project.versioning {
                displayMode = { branchType, branchId, base, build, full, extension ->
                    "${base}-${build}-SNAPSHOT"
                }
            }
            VersionInfo info = project.versioning.info as VersionInfo
            assert info != null
            assert info.build == headAbbreviated
            assert info.branch == 'feature/123-great'
            assert info.base == '123-great'
            assert info.branchId == 'feature-123-great'
            assert info.branchType == 'feature'
            assert info.commit == head
            assert info.display == "123-great-${headAbbreviated}-SNAPSHOT"
            assert info.full == "feature-123-great-${headAbbreviated}"
            assert info.scm == 'git'

        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git release branch: no previous tag'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                git 'init'
                (1..4).each { commit it }
                git 'checkout', '-b', 'release/2.0'
                commit 5
                git 'log', '--oneline', '--graph', '--decorate', '--all'
            }
            def head = repo.commitLookup('Commit 5')
            def headAbbreviated = repo.commitLookup('Commit 5', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            VersionInfo info = project.versioning.info as VersionInfo
            assert info != null
            assert info.build == headAbbreviated
            assert info.branch == 'release/2.0'
            assert info.base == '2.0'
            assert info.branchId == 'release-2.0'
            assert info.branchType == 'release'
            assert info.commit == head
            assert info.display == '2.0.0'
            assert info.full == "release-2.0-${headAbbreviated}"
            assert info.scm == 'git'
        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git release branch: with previous tag'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                git 'init'
                (1..4).each { commit it }
                git 'checkout', '-b', 'release/2.0'
                commit 5
                git 'tag', '2.0.2'
                commit 6
                git 'log', '--oneline', '--graph', '--decorate', '--all'
            }
            def head = repo.commitLookup('Commit 6')
            def headAbbreviated = repo.commitLookup('Commit 6', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            VersionInfo info = project.versioning.info as VersionInfo
            assert info != null
            assert info.build == headAbbreviated
            assert info.branch == 'release/2.0'
            assert info.base == '2.0'
            assert info.branchId == 'release-2.0'
            assert info.branchType == 'release'
            assert info.commit == head
            assert info.display == '2.0.3'
            assert info.full == "release-2.0-${headAbbreviated}"
            assert info.scm == 'git'

        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git feature branch - dirty working copy - default suffix'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                git 'init'
                (1..4).each { commit it }
                git 'checkout', '-b', 'feature/123-great'
                commit 5
                git 'log', '--oneline', '--graph', '--decorate', '--all'
                // Add a file
                cmd 'touch', 'test.txt'
            }
            def head = repo.commitLookup('Commit 5')
            def headAbbreviated = repo.commitLookup('Commit 5', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            VersionInfo info = project.versioning.info as VersionInfo
            assert info != null
            assert info.build == headAbbreviated
            assert info.branch == 'feature/123-great'
            assert info.base == '123-great'
            assert info.branchId == 'feature-123-great'
            assert info.branchType == 'feature'
            assert info.commit == head
            assert info.display == "feature-123-great-${headAbbreviated}-dirty"
            assert info.full == "feature-123-great-${headAbbreviated}-dirty"
            assert info.scm == 'git'

        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git feature branch - dirty index - default suffix'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                git 'init'
                (1..4).each { commit it }
                git 'checkout', '-b', 'feature/123-great'
                commit 5
                git 'log', '--oneline', '--graph', '--decorate', '--all'
                // Add a file
                cmd 'touch', 'test.txt'
                git 'add', 'test.txt'
            }
            def head = repo.commitLookup('Commit 5')
            def headAbbreviated = repo.commitLookup('Commit 5', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            VersionInfo info = project.versioning.info as VersionInfo
            assert info != null
            assert info.build == headAbbreviated
            assert info.branch == 'feature/123-great'
            assert info.base == '123-great'
            assert info.branchId == 'feature-123-great'
            assert info.branchType == 'feature'
            assert info.commit == head
            assert info.display == "feature-123-great-${headAbbreviated}-dirty"
            assert info.full == "feature-123-great-${headAbbreviated}-dirty"
            assert info.scm == 'git'

        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git feature branch - dirty working copy - custom suffix'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                git 'init'
                (1..4).each { commit it }
                git 'checkout', '-b', 'feature/123-great'
                commit 5
                git 'log', '--oneline', '--graph', '--decorate', '--all'
                // Add a file
                cmd 'touch', 'test.txt'
            }
            def head = repo.commitLookup('Commit 5')
            def headAbbreviated = repo.commitLookup('Commit 5', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            project.versioning {
                dirtySuffix = '-dev'
            }
            VersionInfo info = project.versioning.info as VersionInfo
            assert info != null
            assert info.build == headAbbreviated
            assert info.branch == 'feature/123-great'
            assert info.base == '123-great'
            assert info.branchId == 'feature-123-great'
            assert info.branchType == 'feature'
            assert info.commit == head
            assert info.display == "feature-123-great-${headAbbreviated}-dev"
            assert info.full == "feature-123-great-${headAbbreviated}-dev"
            assert info.scm == 'git'

        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git release branch - dirty working copy - default'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                git 'init'
                (1..4).each { commit it }
                git 'checkout', '-b', 'release/2.0'
                commit 5
                git 'tag', '2.0.2'
                commit 6
                git 'log', '--oneline', '--graph', '--decorate', '--all'
                // Add a file
                cmd 'touch', 'test.txt'
            }
            def head = repo.commitLookup('Commit 6')
            def headAbbreviated = repo.commitLookup('Commit 6', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            VersionInfo info = project.versioning.info as VersionInfo
            assert info != null
            assert info.build == headAbbreviated
            assert info.branch == 'release/2.0'
            assert info.base == '2.0'
            assert info.branchId == 'release-2.0'
            assert info.branchType == 'release'
            assert info.commit == head
            assert info.display == '2.0.3-dirty'
            assert info.full == "release-2.0-${headAbbreviated}-dirty"
            assert info.scm == 'git'

        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git release branch - dirty working copy - custom suffix'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                git 'init'
                (1..4).each { commit it }
                git 'checkout', '-b', 'release/2.0'
                commit 5
                git 'tag', '2.0.2'
                commit 6
                git 'log', '--oneline', '--graph', '--decorate', '--all'
                // Add a file
                cmd 'touch', 'test.txt'
            }
            def head = repo.commitLookup('Commit 6')
            def headAbbreviated = repo.commitLookup('Commit 6', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            project.versioning {
                dirtySuffix = '-DIRTY'
            }
            VersionInfo info = project.versioning.info as VersionInfo
            assert info != null
            assert info.build == headAbbreviated
            assert info.branch == 'release/2.0'
            assert info.base == '2.0'
            assert info.branchId == 'release-2.0'
            assert info.branchType == 'release'
            assert info.commit == head
            assert info.display == '2.0.3-DIRTY'
            assert info.full == "release-2.0-${headAbbreviated}-DIRTY"
            assert info.scm == 'git'

        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git release branch - dirty working copy - custom code'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                git 'init'
                (1..4).each { commit it }
                git 'checkout', '-b', 'release/2.0'
                commit 5
                git 'tag', '2.0.2'
                commit 6
                git 'log', '--oneline', '--graph', '--decorate', '--all'
                // Add a file
                cmd 'touch', 'test.txt'
            }
            def head = repo.commitLookup('Commit 6')
            def headAbbreviated = repo.commitLookup('Commit 6', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            project.versioning {
                dirty = { version -> "${version}-DONOTUSE" }
            }
            VersionInfo info = project.versioning.info as VersionInfo
            assert info != null
            assert info.build == headAbbreviated
            assert info.branch == 'release/2.0'
            assert info.base == '2.0'
            assert info.branchId == 'release-2.0'
            assert info.branchType == 'release'
            assert info.commit == head
            assert info.display == '2.0.3-DONOTUSE'
            assert info.full == "release-2.0-${headAbbreviated}-DONOTUSE"
            assert info.scm == 'git'

        } finally {
            repo.close()
        }
    }

    @Test(expected = DirtyException)
    void 'Git release branch - dirty working copy - fail'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                git 'init'
                (1..4).each { commit it }
                git 'checkout', '-b', 'release/2.0'
                commit 5
                git 'tag', '2.0.2'
                commit 6
                git 'log', '--oneline', '--graph', '--decorate', '--all'
                // Add a file
                cmd 'touch', 'test.txt'
            }
            def head = repo.commitLookup('Commit 6')
            def headAbbreviated = repo.commitLookup('Commit 6', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            project.versioning {
                dirtyFailOnReleases = true
            }
            project.versioning.info

        } finally {
            repo.close()
        }
    }

}
