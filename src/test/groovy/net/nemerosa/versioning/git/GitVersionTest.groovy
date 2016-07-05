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
        assert info.tag == null
    }

    @Test
    void 'Git master'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                (1..4).each { commit it }
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
            assert info.display == "master-${headAbbreviated}" as String
            assert info.full == "master-${headAbbreviated}" as String
            assert info.scm == 'git'
            assert info.tag == null

        } finally {
            repo.close()
        }
    }

    /**
     * The Git information is accessible from a sub project.
     * @issue #20
     */
    @Test
    void 'Git sub project'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                (1..4).each { commit it }
            }
            def head = repo.commitLookup('Commit 4')
            def headAbbreviated = repo.commitLookup('Commit 4', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            def subdir = new File(repo.dir, 'sub')
            subdir.mkdirs()
            def subproject = ProjectBuilder.builder().withParent(project).withProjectDir(subdir).build()
            new VersioningPlugin().apply(subproject)
            VersionInfo info = subproject.versioning.info as VersionInfo
            assert info != null
            assert info.build == headAbbreviated
            assert info.branch == 'master'
            assert info.base == ''
            assert info.branchId == 'master'
            assert info.branchType == 'master'
            assert info.commit == head
            assert info.display == "master-${headAbbreviated}" as String
            assert info.full == "master-${headAbbreviated}" as String
            assert info.scm == 'git'
            assert info.tag == null

        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git display version'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
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
    void 'Git version file - defaults'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
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
VERSION_BUILD=${headAbbreviated}
VERSION_BRANCH=master
VERSION_BASE=\n\
VERSION_BRANCHID=master
VERSION_BRANCHTYPE=master
VERSION_COMMIT=${head}
VERSION_DISPLAY=master-${headAbbreviated}
VERSION_FULL=master-${headAbbreviated}
VERSION_SCM=git
VERSION_TAG=
""" as String
        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git version file - custom prefix'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
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
CUSTOM_BUILD=${headAbbreviated}
CUSTOM_BRANCH=master
CUSTOM_BASE=\n\
CUSTOM_BRANCHID=master
CUSTOM_BRANCHTYPE=master
CUSTOM_COMMIT=${head}
CUSTOM_DISPLAY=master-${headAbbreviated}
CUSTOM_FULL=master-${headAbbreviated}
CUSTOM_SCM=git
CUSTOM_TAG=
""" as String
        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git version file - custom file'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
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
VERSION_BUILD=${headAbbreviated}
VERSION_BRANCH=master
VERSION_BASE=\n\
VERSION_BRANCHID=master
VERSION_BRANCHTYPE=master
VERSION_COMMIT=${head}
VERSION_DISPLAY=master-${headAbbreviated}
VERSION_FULL=master-${headAbbreviated}
VERSION_SCM=git
VERSION_TAG=
""" as String
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
                (1..4).each { commit it }
                branch 'feature/123-great'
                commit 5
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
            assert info.display == "feature-123-great-${headAbbreviated}" as String
            assert info.full == "feature-123-great-${headAbbreviated}" as String
            assert info.scm == 'git'
            assert info.tag == null

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
                (1..4).each { commit it }
                branch 'feature/123-great'
                commit 5
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
            assert info.display == "feature-123-great-${headAbbreviated}" as String
            assert info.full == "feature-123-great-${headAbbreviated}" as String
            assert info.scm == 'git'
            assert info.tag == null

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
                (1..4).each { commit it }
                branch 'feature/123-great'
                commit 5
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
            assert info.full == "feature-123-great-${headAbbreviated}" as String
            assert info.scm == 'git'
            assert info.tag == null

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
                (1..4).each { commit it }
                branch 'feature/123-great'
                commit 5
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
            assert info.full == "feature-123-great-${headAbbreviated}" as String
            assert info.scm == 'git'
            assert info.tag == null

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
                (1..4).each { commit it }
                branch 'feature/123-great'
                commit 5
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
            assert info.full == "feature-123-great-${headAbbreviated}" as String
            assert info.scm == 'git'
            assert info.tag == null

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
                (1..4).each { commit it }
                branch 'feature/123-great'
                commit 5
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
            assert info.display == "123-great-${headAbbreviated}-SNAPSHOT" as String
            assert info.full == "feature-123-great-${headAbbreviated}" as String
            assert info.scm == 'git'
            assert info.tag == null

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
                (1..4).each { commit it }
                branch 'release/2.0'
                commit 5
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
            assert info.full == "release-2.0-${headAbbreviated}" as String
            assert info.scm == 'git'
            assert info.tag == null
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
                (1..4).each { commit it }
                branch 'release/2.0'
                commit 5
                tag '2.0.2'
                commit 6
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
            assert info.full == "release-2.0-${headAbbreviated}" as String
            assert info.scm == 'git'
            assert info.tag == null

        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git release branch: with previous tag alpha'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                (1..4).each { commit it }
                branch 'release/2.0-alpha'
                commit 5
                tag '2.0-alpha.2'
                commit 6
            }
            def head = repo.commitLookup('Commit 6')
            def headAbbreviated = repo.commitLookup('Commit 6', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            VersionInfo info = project.versioning.info as VersionInfo
            assert info != null
            assert info.build == headAbbreviated
            assert info.branch == 'release/2.0-alpha'
            assert info.base == '2.0-alpha'
            assert info.branchId == 'release-2.0-alpha'
            assert info.branchType == 'release'
            assert info.commit == head
            assert info.display == '2.0-alpha.3'
            assert info.full == "release-2.0-alpha-${headAbbreviated}" as String
            assert info.scm == 'git'
            assert info.tag == null

        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git release branch: with previous tags alpha, older tag must be taken into account'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                (1..4).each { commit it }
                branch 'release/3.0-alpha'
                commit 5
                tag '3.0-alpha.0'
                sleep 1000
                commit 6
                tag '3.0-alpha.1'
                commit 7
            }
            def head = repo.commitLookup('Commit 7')
            def headAbbreviated = repo.commitLookup('Commit 7', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            VersionInfo info = project.versioning.info as VersionInfo

            assert info != null
            assert info.build == headAbbreviated
            assert info.branch == 'release/3.0-alpha'
            assert info.base == '3.0-alpha'
            assert info.branchId == 'release-3.0-alpha'
            assert info.branchType == 'release'
            assert info.commit == head
            assert info.display == '3.0-alpha.2'
            assert info.full == "release-3.0-alpha-${headAbbreviated}" as String
            assert info.scm == 'git'
            assert info.tag == null

        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git release branch: with previous tags alpha, chronological order of tags must be taken into account'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                (1..4).each { commit it }
                branch 'release/3.0-alpha'
                commit 5
                tag '3.0-alpha.9'
                sleep 1000
                commit 6
                tag '3.0-alpha.10'
                commit 7
            }
            def head = repo.commitLookup('Commit 7')
            def headAbbreviated = repo.commitLookup('Commit 7', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            VersionInfo info = project.versioning.info as VersionInfo

            assert info != null
            assert info.build == headAbbreviated
            assert info.branch == 'release/3.0-alpha'
            assert info.base == '3.0-alpha'
            assert info.branchId == 'release-3.0-alpha'
            assert info.branchType == 'release'
            assert info.commit == head
            assert info.display == '3.0-alpha.11'
            assert info.full == "release-3.0-alpha-${headAbbreviated}" as String
            assert info.scm == 'git'
            assert info.tag == null

        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git release branch: with previous tags alpha, chronological order of tags must be taken into account - 2'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                (1..4).each { commit it }
                branch 'release/3.0-alpha'
                commit 5
                tag '3.0-alpha.19'
                sleep 1000
                commit 6
                tag '3.0-alpha.20'
                commit 7
            }
            def head = repo.commitLookup('Commit 7')
            def headAbbreviated = repo.commitLookup('Commit 7', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            VersionInfo info = project.versioning.info as VersionInfo

            assert info != null
            assert info.build == headAbbreviated
            assert info.branch == 'release/3.0-alpha'
            assert info.base == '3.0-alpha'
            assert info.branchId == 'release-3.0-alpha'
            assert info.branchType == 'release'
            assert info.commit == head
            assert info.display == '3.0-alpha.21'
            assert info.full == "release-3.0-alpha-${headAbbreviated}" as String
            assert info.scm == 'git'
            assert info.tag == null

        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git release branch: with previous tag on different branches'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                (1..4).each { commit it }
                branch 'release/2.0'
                commit 5
                tag '2.0.2'
                branch 'release/2.1'
                commit 6
                tag '2.1.0'
                checkout 'release/2.0'
                commit 7
            }
            def head = repo.commitLookup('Commit 7')
            def headAbbreviated = repo.commitLookup('Commit 7', true)

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
            assert info.full == "release-2.0-${headAbbreviated}" as String
            assert info.scm == 'git'
            assert info.tag == null

        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git release branch: with previous tag with two final digits'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                (1..4).each { commit it }
                branch 'release/2.0'
                commit 5
                tag '2.0.10'
                commit 6
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
            assert info.display == '2.0.11'
            assert info.full == "release-2.0-${headAbbreviated}" as String
            assert info.scm == 'git'
            assert info.tag == null

        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git release branch with snapshot: no previous tag'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                (1..4).each { commit it }
                branch 'release/2.0'
                commit 5
            }
            def head = repo.commitLookup('Commit 5')
            def headAbbreviated = repo.commitLookup('Commit 5', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            project.versioning {
                releaseMode = 'snapshot'
            }

            VersionInfo info = project.versioning.info as VersionInfo
            assert info != null
            assert info.build == headAbbreviated
            assert info.branch == 'release/2.0'
            assert info.base == '2.0'
            assert info.branchId == 'release-2.0'
            assert info.branchType == 'release'
            assert info.commit == head
            assert info.display == '2.0.0-SNAPSHOT'
            assert info.full == "release-2.0-${headAbbreviated}" as String
            assert info.scm == 'git'
            assert info.tag == null
        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git release branch with custom snapshot: no previous tag'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                (1..4).each { commit it }
                branch 'release/2.0'
                commit 5
            }
            def head = repo.commitLookup('Commit 5')
            def headAbbreviated = repo.commitLookup('Commit 5', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            project.versioning {
                releaseMode = 'snapshot'
                snapshot = '-DEV'
            }
            VersionInfo info = project.versioning.info as VersionInfo
            assert info != null
            assert info.build == headAbbreviated
            assert info.branch == 'release/2.0'
            assert info.base == '2.0'
            assert info.branchId == 'release-2.0'
            assert info.branchType == 'release'
            assert info.commit == head
            assert info.display == '2.0.0-DEV'
            assert info.full == "release-2.0-${headAbbreviated}" as String
            assert info.scm == 'git'
            assert info.tag == null
        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git release branch with custom display: no previous tag'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                (1..4).each { commit it }
                branch 'release/2.0'
                commit 5
            }
            def head = repo.commitLookup('Commit 5')
            def headAbbreviated = repo.commitLookup('Commit 5', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            project.versioning {
                releaseMode = { nextTag, lastTag, currentTag, extension -> "${nextTag}-PREVIEW"}
            }
            VersionInfo info = project.versioning.info as VersionInfo
            assert info != null
            assert info.build == headAbbreviated
            assert info.branch == 'release/2.0'
            assert info.base == '2.0'
            assert info.branchId == 'release-2.0'
            assert info.branchType == 'release'
            assert info.commit == head
            assert info.display == '2.0.0-PREVIEW'
            assert info.full == "release-2.0-${headAbbreviated}" as String
            assert info.scm == 'git'
            assert info.tag == null
        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git release branch with snapshot: with previous tag'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                (1..4).each { commit it }
                branch 'release/2.0'
                commit 5
                tag '2.0.2'
                commit 6
            }
            def head = repo.commitLookup('Commit 6')
            def headAbbreviated = repo.commitLookup('Commit 6', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            project.versioning {
                releaseMode = 'snapshot'
            }
            VersionInfo info = project.versioning.info as VersionInfo
            assert info != null
            assert info.build == headAbbreviated
            assert info.branch == 'release/2.0'
            assert info.base == '2.0'
            assert info.branchId == 'release-2.0'
            assert info.branchType == 'release'
            assert info.commit == head
            assert info.display == '2.0.3-SNAPSHOT'
            assert info.full == "release-2.0-${headAbbreviated}" as String
            assert info.scm == 'git'
            assert info.tag == null

        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git release branch with custom snapshot: with previous tag'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                (1..4).each { commit it }
                branch 'release/2.0'
                commit 5
                tag '2.0.2'
                commit 6
            }
            def head = repo.commitLookup('Commit 6')
            def headAbbreviated = repo.commitLookup('Commit 6', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            project.versioning {
                releaseMode = 'snapshot'
                snapshot = '-DEV'
            }
            VersionInfo info = project.versioning.info as VersionInfo
            assert info != null
            assert info.build == headAbbreviated
            assert info.branch == 'release/2.0'
            assert info.base == '2.0'
            assert info.branchId == 'release-2.0'
            assert info.branchType == 'release'
            assert info.commit == head
            assert info.display == '2.0.3-DEV'
            assert info.full == "release-2.0-${headAbbreviated}" as String
            assert info.scm == 'git'
            assert info.tag == null

        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git release branch with custom display: with previous tag'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                (1..4).each { commit it }
                branch 'release/2.0'
                commit 5
                tag '2.0.2'
                commit 6
            }
            def head = repo.commitLookup('Commit 6')
            def headAbbreviated = repo.commitLookup('Commit 6', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            project.versioning {
                releaseMode = { nextTag, lastTag, currentTag, extension -> "${nextTag}-PREVIEW"}
            }
            VersionInfo info = project.versioning.info as VersionInfo
            assert info != null
            assert info.build == headAbbreviated
            assert info.branch == 'release/2.0'
            assert info.base == '2.0'
            assert info.branchId == 'release-2.0'
            assert info.branchType == 'release'
            assert info.commit == head
            assert info.display == '2.0.3-PREVIEW'
            assert info.full == "release-2.0-${headAbbreviated}" as String
            assert info.scm == 'git'
            assert info.tag == null

        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git release branch with snapshot: on tag'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                (1..4).each { commit it }
                branch 'release/2.0'
                commit 5
                commit 6
                tag '2.0.2'
            }
            def head = repo.commitLookup('Commit 6')
            def headAbbreviated = repo.commitLookup('Commit 6', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            project.versioning {
                releaseMode = 'snapshot'
            }
            VersionInfo info = project.versioning.info as VersionInfo
            assert info != null
            assert info.build == headAbbreviated
            assert info.branch == 'release/2.0'
            assert info.base == '2.0'
            assert info.branchId == 'release-2.0'
            assert info.branchType == 'release'
            assert info.commit == head
            assert info.display == '2.0.2'
            assert info.full == "release-2.0-${headAbbreviated}" as String
            assert info.scm == 'git'
            assert info.tag == '2.0.2'

        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git release branch with custom snapshot: on tag'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                (1..4).each { commit it }
                branch 'release/2.0'
                commit 5
                commit 6
                tag '2.0.2'
            }
            def head = repo.commitLookup('Commit 6')
            def headAbbreviated = repo.commitLookup('Commit 6', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            project.versioning {
                releaseMode = 'snapshot'
                snapshot = '-DEV'
            }
            VersionInfo info = project.versioning.info as VersionInfo
            assert info != null
            assert info.build == headAbbreviated
            assert info.branch == 'release/2.0'
            assert info.base == '2.0'
            assert info.branchId == 'release-2.0'
            assert info.branchType == 'release'
            assert info.commit == head
            assert info.display == '2.0.2'
            assert info.full == "release-2.0-${headAbbreviated}" as String
            assert info.scm == 'git'
            assert info.tag == '2.0.2'

        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git release branch with custom display: on tag'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                (1..4).each { commit it }
                branch 'release/2.0'
                commit 5
                commit 6
                tag '2.0.2'
            }
            def head = repo.commitLookup('Commit 6')
            def headAbbreviated = repo.commitLookup('Commit 6', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            project.versioning {
                releaseMode = { nextTag, lastTag, currentTag, extension -> "${nextTag}-PREVIEW"}
            }
            VersionInfo info = project.versioning.info as VersionInfo
            assert info != null
            assert info.build == headAbbreviated
            assert info.branch == 'release/2.0'
            assert info.base == '2.0'
            assert info.branchId == 'release-2.0'
            assert info.branchType == 'release'
            assert info.commit == head
            assert info.display == '2.0.3-PREVIEW'
            assert info.full == "release-2.0-${headAbbreviated}" as String
            assert info.scm == 'git'
            assert info.tag == '2.0.2'

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
                (1..4).each { commit it }
                branch 'feature/123-great'
                commit 5
                // Nope, got to mod an existing tracked file
                // cmd 'touch', 'test.txt'
				new File(dir, 'file5') << 'Add some content'
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
            assert info.display == "feature-123-great-${headAbbreviated}-dirty" as String
            assert info.full == "feature-123-great-${headAbbreviated}-dirty" as String
            assert info.scm == 'git'
            assert info.tag == null

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
                (1..4).each { commit it }
                branch 'feature/123-great'
                commit 5
                // Add a file
                new File(repo.dir, 'test.text').text = 'test'
                add 'test.txt'
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
            assert info.display == "feature-123-great-${headAbbreviated}-dirty" as String
            assert info.full == "feature-123-great-${headAbbreviated}-dirty" as String
            assert info.scm == 'git'
            assert info.tag == null

        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git feature branch - ignored files - not dirty'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                (1..4).each { commit it }
                branch 'feature/123-great'
                commit 5
                // Ignore file
                new File(repo.dir, '.gitignore').text = 'test.txt'
                add '.gitignore'
                commit 6
                // Add a file
                new File(repo.dir, 'test.txt').text = 'test'
            }
            def head = repo.commitLookup('Commit 6')
            def headAbbreviated = repo.commitLookup('Commit 6', true)

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
            assert info.display == "feature-123-great-${headAbbreviated}" as String
            assert info.full == "feature-123-great-${headAbbreviated}" as String
            assert info.scm == 'git'
            assert info.tag == null

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
                (1..4).each { commit it }
                branch 'feature/123-great'
                commit 5
                // Nope, need to mod an existing file to make the tree dirty
                //cmd 'touch', 'test.txt'
				new File(dir, 'file5') << 'Add some content'
            }
            def head = repo.commitLookup('Commit 5')
            def headAbbreviated = repo.commitLookup('Commit 5', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            project.versioning {
                dirtySuffix = '-dev'
                noWarningOnDirty = true
            }
            VersionInfo info = project.versioning.info as VersionInfo
            assert info != null
            assert info.build == headAbbreviated
            assert info.branch == 'feature/123-great'
            assert info.base == '123-great'
            assert info.branchId == 'feature-123-great'
            assert info.branchType == 'feature'
            assert info.commit == head
            assert info.display == "feature-123-great-${headAbbreviated}-dev" as String
            assert info.full == "feature-123-great-${headAbbreviated}-dev" as String
            assert info.scm == 'git'
            assert info.tag == null

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
                (1..4).each { commit it }
                branch 'release/2.0'
                commit 5
                tag '2.0.2'
                commit 6
                // Nope, got to mod an existing file
		        new File(dir, 'file5') << 'Add some content'
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
            assert info.full == "release-2.0-${headAbbreviated}-dirty" as String
            assert info.scm == 'git'
            assert info.tag == null

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
                (1..4).each { commit it }
                branch 'release/2.0'
                commit 5
                tag '2.0.2'
                commit 6
                // Nope, got to mod an existing file
                //cmd 'touch', 'test.txt'
				new File(dir, 'file5') << 'Mod the content'
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
            assert info.full == "release-2.0-${headAbbreviated}-DIRTY" as String
            assert info.scm == 'git'
            assert info.tag == null

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
                (1..4).each { commit it }
                branch 'release/2.0'
                commit 5
                tag '2.0.2'
                commit 6
                // Nope, got to mod an existing file
                //cmd 'touch', 'test.txt'
				new File(dir, 'file5') << 'Mod the content'
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
            assert info.full == "release-2.0-${headAbbreviated}-DONOTUSE" as String
            assert info.scm == 'git'
            assert info.tag == null

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
                (1..4).each { commit it }
                branch 'release/2.0'
                commit 5
                tag '2.0.2'
                commit 6
                // Nope, mod an existing file
                //cmd 'touch', 'test.txt'
				new File(dir, 'file5') << 'mod the content'
            }

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


    @Test
    void 'Git branch with env TEST_BRANCH'() {
        // TEST_BRANCH is provided by gradle.build
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                (1..4).each { commit it }
                branch 'feature/123-great'
                commit 5
            }
            // System.setenv('TEST_BRANCH', 'feature/456-cute')
            def head = repo.commitLookup('Commit 5')
            def headAbbreviated = repo.commitLookup('Commit 5', true)

            def project = ProjectBuilder.builder().withProjectDir(repo.dir).build()
            new VersioningPlugin().apply(project)
            project.versioning {
                branchEnv << 'GIT_TEST_BRANCH'
            }
            VersionInfo info = project.versioning.info as VersionInfo
            assert info != null
            assert info.build == headAbbreviated
            assert info.branch == 'feature/456-cute'
            assert info.base == '456-cute'
            assert info.branchId == 'feature-456-cute'
            assert info.branchType == 'feature'
            assert info.commit == head
            assert info.display == "feature-456-cute-${headAbbreviated}" as String
            assert info.full == "feature-456-cute-${headAbbreviated}" as String
            assert info.scm == 'git'
            assert info.tag == null

        } finally {
            repo.close()
        }
    }
}
