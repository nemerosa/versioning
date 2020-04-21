package net.nemerosa.versioning.svn

import net.nemerosa.versioning.VersionInfo
import net.nemerosa.versioning.VersioningPlugin
import net.nemerosa.versioning.support.DirtyException
import net.nemerosa.versioning.tasks.VersionDisplayTask
import org.gradle.api.DefaultTask
import org.gradle.testfixtures.ProjectBuilder
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import java.util.concurrent.atomic.AtomicInteger

import static net.nemerosa.versioning.svn.SVNRepo.ignore

class SVNVersionTest {

    private static AtomicInteger count = new AtomicInteger()
    private SVNRepo repo

    @Before
    void 'SVN start'() {
        repo = new SVNRepo("test${count.incrementAndGet()}")
        repo.start()
    }

    @After
    void 'SVN stop'() {
        repo.stop()
    }

    @Test
    void 'SVN not present'() {
        def wd = File.createTempDir('git', '')
        def project = ProjectBuilder.builder().withProjectDir(wd).build()
        new VersioningPlugin().apply(project)
        project.versioning {
            scm = 'svn'
        }
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
        assert !info.dirty
    }

    @Test
    void 'SVN trunk'() {
        // SVN
        repo.mkdir 'project/trunk', 'Trunk'
        repo.mkdir 'project/trunk/1', 'Commit for TEST-1'
        repo.mkdir 'project/trunk/2', 'Commit for TEST-2'
        // Project
        def project = ProjectBuilder.builder().withProjectDir(repo.checkout('project/trunk')).build()
        new VersioningPlugin().apply(project)
        project.versioning {
            scm = 'svn'
        }
        // Gets the version info
        VersionInfo info = project.versioning.info as VersionInfo
        assert info != null
        assert info.build == '3'
        assert info.branch == 'trunk'
        assert info.base == ''
        assert info.branchId == 'trunk'
        assert info.branchType == 'trunk'
        assert info.commit == '3'
        assert info.display == "trunk-3"
        assert info.full == "trunk-3"
        assert info.scm == 'svn'
        assert info.tag == null
        assert !info.dirty
    }

    @Test
    void 'SVN display version'() {
        // SVN
        repo.mkdir 'project/trunk', 'Trunk'
        repo.mkdir 'project/trunk/1', 'Commit for TEST-1'
        repo.mkdir 'project/trunk/2', 'Commit for TEST-2'
        // Project
        def project = ProjectBuilder.builder().withProjectDir(repo.checkout('project/trunk')).build()
        new VersioningPlugin().apply(project)
        project.versioning {
            scm = 'svn'
        }
        // Checks the versionDisplay exists and runs it
        def task = project.tasks.getByName('versionDisplay') as VersionDisplayTask
        task.execute()
    }

    @Test
    void 'SVN version file - defaults'() {
        // SVN
        repo.mkdir 'project/trunk', 'Trunk'
        repo.mkdir 'project/trunk/1', 'Commit for TEST-1'
        repo.mkdir 'project/trunk/2', 'Commit for TEST-2'
        def dir = repo.checkout('project/trunk')
        ignore dir, '.gradle'
        // Project
        def project = ProjectBuilder.builder().withProjectDir(dir).build()
        new VersioningPlugin().apply(project)
        project.versioning {
            scm = 'svn'
        }
        // version file task
        def task = project.tasks.getByName('versionFile') as DefaultTask
        task.execute()

        // Checks the file
        def file = new File(project.buildDir, 'version.properties')
        assert file.exists(): "File ${file} must exist."
        assert file.text == """\
VERSION_BUILD=4
VERSION_BRANCH=trunk
VERSION_BASE=\n\
VERSION_BRANCHID=trunk
VERSION_BRANCHTYPE=trunk
VERSION_COMMIT=4
VERSION_GRADLE=
VERSION_DISPLAY=trunk-4
VERSION_FULL=trunk-4
VERSION_SCM=svn
VERSION_TAG=
VERSION_LAST_TAG=
VERSION_DIRTY=false
VERSION_VERSIONCODE=0
VERSION_MAJOR=0
VERSION_MINOR=0
VERSION_PATCH=0
VERSION_QUALIFIER=
"""
    }

    @Test
    void 'SVN version file - project version'() {
        // SVN
        repo.mkdir 'project/trunk', 'Trunk'
        repo.mkdir 'project/trunk/1', 'Commit for TEST-1'
        repo.mkdir 'project/trunk/2', 'Commit for TEST-2'
        def dir = repo.checkout('project/trunk')
        ignore dir, '.gradle'
        // Project
        def project = ProjectBuilder.builder().withProjectDir(dir).build()
        project.version = '0.0.1'
        new VersioningPlugin().apply(project)
        project.versioning {
            scm = 'svn'
        }
        // version file task
        def task = project.tasks.getByName('versionFile') as DefaultTask
        task.execute()

        // Checks the file
        def file = new File(project.buildDir, 'version.properties')
        assert file.exists(): "File ${file} must exist."
        assert file.text == """\
VERSION_BUILD=4
VERSION_BRANCH=trunk
VERSION_BASE=\n\
VERSION_BRANCHID=trunk
VERSION_BRANCHTYPE=trunk
VERSION_COMMIT=4
VERSION_GRADLE=0.0.1
VERSION_DISPLAY=trunk-4
VERSION_FULL=trunk-4
VERSION_SCM=svn
VERSION_TAG=
VERSION_LAST_TAG=
VERSION_DIRTY=false
VERSION_VERSIONCODE=0
VERSION_MAJOR=0
VERSION_MINOR=0
VERSION_PATCH=0
VERSION_QUALIFIER=
"""
    }

    @Test
    void 'SVN version file - custom prefix'() {
        // SVN
        repo.mkdir 'project/trunk', 'Trunk'
        repo.mkdir 'project/trunk/1', 'Commit for TEST-1'
        repo.mkdir 'project/trunk/2', 'Commit for TEST-2'
        // Project
        def dir = repo.checkout('project/trunk')
        ignore dir, '.gradle'
        def project = ProjectBuilder.builder().withProjectDir(dir).build()
        new VersioningPlugin().apply(project)
        project.versioning {
            scm = 'svn'
        }
        project.versionFile {
            prefix = 'CUSTOM_'
        }
        // version file task
        def task = project.tasks.getByName('versionFile') as DefaultTask
        task.execute()

        // Checks the file
        def file = new File(project.buildDir, 'version.properties')
        assert file.exists(): "File ${file} must exist."
        assert file.text == """\
CUSTOM_BUILD=4
CUSTOM_BRANCH=trunk
CUSTOM_BASE=\n\
CUSTOM_BRANCHID=trunk
CUSTOM_BRANCHTYPE=trunk
CUSTOM_COMMIT=4
CUSTOM_GRADLE=
CUSTOM_DISPLAY=trunk-4
CUSTOM_FULL=trunk-4
CUSTOM_SCM=svn
CUSTOM_TAG=
CUSTOM_LAST_TAG=
CUSTOM_DIRTY=false
CUSTOM_VERSIONCODE=0
CUSTOM_MAJOR=0
CUSTOM_MINOR=0
CUSTOM_PATCH=0
CUSTOM_QUALIFIER=
"""
    }

    @Test
    void 'SVN version file - custom file'() {
        // SVN
        repo.mkdir 'project/trunk', 'Trunk'
        repo.mkdir 'project/trunk/1', 'Commit for TEST-1'
        repo.mkdir 'project/trunk/2', 'Commit for TEST-2'
        // Project
        def dir = repo.checkout('project/trunk')
        ignore dir, '.gradle'
        def project = ProjectBuilder.builder().withProjectDir(dir).build()
        new VersioningPlugin().apply(project)
        project.versioning {
            scm = 'svn'
        }
        project.versionFile {
            file = new File(project.projectDir, '.version')
        }
        // version file task
        def task = project.tasks.getByName('versionFile') as DefaultTask
        task.execute()

        // Checks the file
        def file = new File(project.projectDir, '.version')
        assert file.exists(): "File ${file} must exist."
        assert file.text == """\
VERSION_BUILD=4
VERSION_BRANCH=trunk
VERSION_BASE=\n\
VERSION_BRANCHID=trunk
VERSION_BRANCHTYPE=trunk
VERSION_COMMIT=4
VERSION_GRADLE=
VERSION_DISPLAY=trunk-4
VERSION_FULL=trunk-4
VERSION_SCM=svn
VERSION_TAG=
VERSION_LAST_TAG=
VERSION_DIRTY=false
VERSION_VERSIONCODE=0
VERSION_MAJOR=0
VERSION_MINOR=0
VERSION_PATCH=0
VERSION_QUALIFIER=
"""
    }

    @Test
    void 'SVN feature branch'() {

        // SVN
        repo.mkdir 'project/branches/feature-test-1-my-feature', 'Feature branch'
        repo.mkdir 'project/branches/feature-test-1-my-feature/1', 'Commit for TEST-1'
        repo.mkdir 'project/branches/feature-test-1-my-feature/2', 'Commit for TEST-1'

        // Project
        def project = ProjectBuilder.builder().withProjectDir(repo.checkout('project/branches/feature-test-1-my-feature')).build()
        new VersioningPlugin().apply(project)
        project.versioning {
            scm = 'svn'
        }

        // Gets the info and checks it
        VersionInfo info = project.versioning.info as VersionInfo
        assert info != null
        assert info.build == '3'
        assert info.branch == 'feature-test-1-my-feature'
        assert info.base == 'test-1-my-feature'
        assert info.branchId == 'feature-test-1-my-feature'
        assert info.branchType == 'feature'
        assert info.commit == '3'
        assert info.display == "feature-test-1-my-feature-3"
        assert info.full == "feature-test-1-my-feature-3"
        assert info.scm == 'svn'
        assert info.tag == null
        assert !info.dirty
    }

    @Test
    void 'SVN feature branch with full display mode'() {

        // SVN
        repo.mkdir 'project/branches/feature-test-1-my-feature', 'Feature branch'
        repo.mkdir 'project/branches/feature-test-1-my-feature/1', 'Commit for TEST-1'
        repo.mkdir 'project/branches/feature-test-1-my-feature/2', 'Commit for TEST-1'

        // Project
        def project = ProjectBuilder.builder().withProjectDir(repo.checkout('project/branches/feature-test-1-my-feature')).build()
        new VersioningPlugin().apply(project)
        project.versioning {
            scm = 'svn'
            displayMode = 'full'
        }

        // Gets the info and checks it
        VersionInfo info = project.versioning.info as VersionInfo
        assert info != null
        assert info.build == '3'
        assert info.branch == 'feature-test-1-my-feature'
        assert info.base == 'test-1-my-feature'
        assert info.branchId == 'feature-test-1-my-feature'
        assert info.branchType == 'feature'
        assert info.commit == '3'
        assert info.display == "feature-test-1-my-feature-3"
        assert info.full == "feature-test-1-my-feature-3"
        assert info.scm == 'svn'
        assert info.tag == null
        assert !info.dirty
    }

    @Test
    void 'SVN feature branch with snapshot display mode'() {

        // SVN
        repo.mkdir 'project/branches/feature-test-1-my-feature', 'Feature branch'
        repo.mkdir 'project/branches/feature-test-1-my-feature/1', 'Commit for TEST-1'
        repo.mkdir 'project/branches/feature-test-1-my-feature/2', 'Commit for TEST-1'

        // Project
        def project = ProjectBuilder.builder().withProjectDir(repo.checkout('project/branches/feature-test-1-my-feature')).build()
        new VersioningPlugin().apply(project)
        project.versioning {
            scm = 'svn'
            displayMode = 'snapshot'
        }

        // Gets the info and checks it
        VersionInfo info = project.versioning.info as VersionInfo
        assert info != null
        assert info.build == '3'
        assert info.branch == 'feature-test-1-my-feature'
        assert info.base == 'test-1-my-feature'
        assert info.branchId == 'feature-test-1-my-feature'
        assert info.branchType == 'feature'
        assert info.commit == '3'
        assert info.display == "test-1-my-feature-SNAPSHOT"
        assert info.full == "feature-test-1-my-feature-3"
        assert info.scm == 'svn'
        assert info.tag == null
        assert !info.dirty
    }

    @Test
    void 'SVN feature branch with custom snapshot display mode'() {

        // SVN
        repo.mkdir 'project/branches/feature-test-1-my-feature', 'Feature branch'
        repo.mkdir 'project/branches/feature-test-1-my-feature/1', 'Commit for TEST-1'
        repo.mkdir 'project/branches/feature-test-1-my-feature/2', 'Commit for TEST-1'

        // Project
        def project = ProjectBuilder.builder().withProjectDir(repo.checkout('project/branches/feature-test-1-my-feature')).build()
        new VersioningPlugin().apply(project)
        project.versioning {
            scm = 'svn'
            displayMode = 'snapshot'
            snapshot = '.DEV'
        }

        // Gets the info and checks it
        VersionInfo info = project.versioning.info as VersionInfo
        assert info != null
        assert info.build == '3'
        assert info.branch == 'feature-test-1-my-feature'
        assert info.base == 'test-1-my-feature'
        assert info.branchId == 'feature-test-1-my-feature'
        assert info.branchType == 'feature'
        assert info.commit == '3'
        assert info.display == "test-1-my-feature.DEV"
        assert info.full == "feature-test-1-my-feature-3"
        assert info.scm == 'svn'
        assert info.tag == null
        assert !info.dirty
    }

    @Test
    void 'SVN feature branch with custom base display mode'() {

        // SVN
        repo.mkdir 'project/branches/feature-test-1-my-feature', 'Feature branch'
        repo.mkdir 'project/branches/feature-test-1-my-feature/1', 'Commit for TEST-1'
        repo.mkdir 'project/branches/feature-test-1-my-feature/2', 'Commit for TEST-1'

        // Project
        def project = ProjectBuilder.builder().withProjectDir(repo.checkout('project/branches/feature-test-1-my-feature')).build()
        new VersioningPlugin().apply(project)
        project.versioning {
            scm = 'svn'
            displayMode = 'base'
        }

        // Gets the info and checks it
        VersionInfo info = project.versioning.info as VersionInfo
        assert info != null
        assert info.build == '3'
        assert info.branch == 'feature-test-1-my-feature'
        assert info.base == 'test-1-my-feature'
        assert info.branchId == 'feature-test-1-my-feature'
        assert info.branchType == 'feature'
        assert info.commit == '3'
        assert info.display == "test-1-my-feature"
        assert info.full == "feature-test-1-my-feature-3"
        assert info.scm == 'svn'
        assert info.tag == null
        assert !info.dirty
    }

    @Test
    void 'SVN feature branch with custom display mode'() {

        // SVN
        repo.mkdir 'project/branches/feature-test-1-my-feature', 'Feature branch'
        repo.mkdir 'project/branches/feature-test-1-my-feature/1', 'Commit for TEST-1'
        repo.mkdir 'project/branches/feature-test-1-my-feature/2', 'Commit for TEST-1'

        // Project
        def project = ProjectBuilder.builder().withProjectDir(repo.checkout('project/branches/feature-test-1-my-feature')).build()
        new VersioningPlugin().apply(project)
        project.versioning {
            scm = 'svn'
            displayMode = { branchType, branchId, base, build, full, extension ->
                "${base}-${build}-SNAPSHOT"
            }
        }

        // Gets the info and checks it
        VersionInfo info = project.versioning.info as VersionInfo
        assert info != null
        assert info.build == '3'
        assert info.branch == 'feature-test-1-my-feature'
        assert info.base == 'test-1-my-feature'
        assert info.branchId == 'feature-test-1-my-feature'
        assert info.branchType == 'feature'
        assert info.commit == '3'
        assert info.display == "test-1-my-feature-3-SNAPSHOT"
        assert info.full == "feature-test-1-my-feature-3"
        assert info.scm == 'svn'
        assert info.tag == null
        assert !info.dirty
    }

    @Test
    void 'SVN release branch no previous tag'() {

        // SVN
        repo.mkdir 'project/branches/release-2.0', 'Feature branch'
        repo.mkdir 'project/branches/release-2.0/1', 'Commit for TEST-1'
        repo.mkdir 'project/branches/release-2.0/2', 'Commit for TEST-1'

        // Project
        def project = ProjectBuilder.builder().withProjectDir(repo.checkout('project/branches/release-2.0')).build()
        new VersioningPlugin().apply(project)
        project.versioning {
            scm = 'svn'
        }

        // Gets the info and checks it
        VersionInfo info = project.versioning.info as VersionInfo
        assert info != null
        assert info.build == '3'
        assert info.branch == 'release-2.0'
        assert info.base == '2.0'
        assert info.branchId == 'release-2.0'
        assert info.branchType == 'release'
        assert info.commit == '3'
        assert info.display == "2.0.0"
        assert info.full == "release-2.0-3"
        assert info.scm == 'svn'
        assert info.tag == null
        assert !info.dirty

    }

    @Test
    void 'SVN release branch with previous tag'() {

        // SVN
        repo.mkdir 'project/branches/release-2.0', 'Feature branch'
        repo.mkdir 'project/branches/release-2.0/1', 'Commit for TEST-1' // Tag
        repo.mkdir 'project/branches/release-2.0/2', 'Commit for TEST-1'
        // Tagging
        repo.copy 'project/branches/release-2.0@2', 'project/tags/2.0.0', 'v2.0.0'
        // Logging
        repo.log()

        // Project
        def project = ProjectBuilder.builder().withProjectDir(repo.checkout('project/branches/release-2.0')).build()
        new VersioningPlugin().apply(project)
        project.versioning {
            scm = 'svn'
        }

        // Gets the info and checks it
        VersionInfo info = project.versioning.info as VersionInfo
        assert info != null
        assert info.build == '3'
        assert info.branch == 'release-2.0'
        assert info.base == '2.0'
        assert info.branchId == 'release-2.0'
        assert info.branchType == 'release'
        assert info.commit == '3'
        assert info.display == "2.0.1"
        assert info.full == "release-2.0-3"
        assert info.scm == 'svn'
        assert info.tag == null
        assert !info.dirty

    }

    @Test
    void 'SVN release branch with previous tag with two digits'() {

        // SVN
        repo.mkdir 'project/branches/release-2.0', 'Feature branch'
        repo.mkdir 'project/branches/release-2.0/1', 'Commit for TEST-1' // Tag
        repo.mkdir 'project/branches/release-2.0/2', 'Commit for TEST-1'

        // Tagging
        repo.copy 'project/branches/release-2.0@2', 'project/tags/2.0.10', 'v2.0.10'

        // Project
        def project = ProjectBuilder.builder().withProjectDir(repo.checkout('project/branches/release-2.0')).build()
        new VersioningPlugin().apply(project)
        project.versioning {
            scm = 'svn'
        }

        // Gets the info and checks it
        VersionInfo info = project.versioning.info as VersionInfo
        assert info != null
        assert info.build == '3'
        assert info.branch == 'release-2.0'
        assert info.base == '2.0'
        assert info.branchId == 'release-2.0'
        assert info.branchType == 'release'
        assert info.commit == '3'
        assert info.display == "2.0.11"
        assert info.full == "release-2.0-3"
        assert info.scm == 'svn'
        assert info.tag == null
        assert !info.dirty

    }

    @Test
    void 'SVN release branch with two previous tags'() {

        // SVN
        repo.mkdir 'project/branches/release-2.0', 'Feature branch'
        repo.mkdir 'project/branches/release-2.0/1', 'Commit for TEST-1' // Tag
        repo.mkdir 'project/branches/release-2.0/2', 'Commit for TEST-1' // Tag
        repo.mkdir 'project/branches/release-2.0/3', 'Commit for TEST-1'

        // Tagging
        repo.copy 'project/branches/release-2.0@2', 'project/tags/2.0.0', 'v2.0.0'
        repo.copy 'project/branches/release-2.0@3', 'project/tags/2.0.1', 'v2.0.1'

        // Project
        def project = ProjectBuilder.builder().withProjectDir(repo.checkout('project/branches/release-2.0')).build()
        new VersioningPlugin().apply(project)
        project.versioning {
            scm = 'svn'
        }

        // Gets the info and checks it
        VersionInfo info = project.versioning.info as VersionInfo
        assert info != null
        assert info.build == '4'
        assert info.branch == 'release-2.0'
        assert info.base == '2.0'
        assert info.branchId == 'release-2.0'
        assert info.branchType == 'release'
        assert info.commit == '4'
        assert info.display == "2.0.2"
        assert info.full == "release-2.0-4"
        assert info.scm == 'svn'
        assert info.tag == null
        assert !info.dirty

    }

    @Test
    void 'SVN feature branch - dirty working copy - default suffix'() {
        repo.mkdir 'project/branches/feature-test-1-my-feature', 'Feature branch'
        repo.mkdir 'project/branches/feature-test-1-my-feature/1', 'Commit for TEST-1'
        def dir = repo.checkout('project/branches/feature-test-1-my-feature')
        new File(dir, 'test.txt').text = 'Test'

        def project = ProjectBuilder.builder().withProjectDir(dir).build()
        new VersioningPlugin().apply(project)
        project.versioning {
            scm = 'svn'
        }

        VersionInfo info = project.versioning.info as VersionInfo
        assert info != null
        assert info.build == '2'
        assert info.branch == 'feature-test-1-my-feature'
        assert info.base == 'test-1-my-feature'
        assert info.branchId == 'feature-test-1-my-feature'
        assert info.branchType == 'feature'
        assert info.commit == '2'
        assert info.display == "feature-test-1-my-feature-2-dirty"
        assert info.full == "feature-test-1-my-feature-2-dirty"
        assert info.scm == 'svn'
        assert info.tag == null
        assert info.dirty
    }

    @Test
    void 'SVN feature branch - dirty index - default suffix'() {

        repo.mkdir 'project/branches/feature-test-1-my-feature', 'Feature branch'
        repo.mkdir 'project/branches/feature-test-1-my-feature/1', 'Commit for TEST-1'
        def dir = repo.checkout('project/branches/feature-test-1-my-feature')
        new File(dir, 'test.txt').text = 'test'
        SVNRepo.add dir, 'test.txt'

        def project = ProjectBuilder.builder().withProjectDir(dir).build()
        new VersioningPlugin().apply(project)
        project.versioning {
            scm = 'svn'
        }

        VersionInfo info = project.versioning.info as VersionInfo
        assert info != null
        assert info.build == '2'
        assert info.branch == 'feature-test-1-my-feature'
        assert info.base == 'test-1-my-feature'
        assert info.branchId == 'feature-test-1-my-feature'
        assert info.branchType == 'feature'
        assert info.commit == '2'
        assert info.display == "feature-test-1-my-feature-2-dirty"
        assert info.full == "feature-test-1-my-feature-2-dirty"
        assert info.scm == 'svn'
        assert info.tag == null
        assert info.dirty
    }

    @Test
    void 'SVN feature branch - dirty working copy - custom suffix'() {
        repo.mkdir 'project/branches/feature-test-1-my-feature', 'Feature branch'
        repo.mkdir 'project/branches/feature-test-1-my-feature/1', 'Commit for TEST-1'
        def dir = repo.checkout('project/branches/feature-test-1-my-feature')
        new File(dir, 'test.txt').text = 'test'

        def project = ProjectBuilder.builder().withProjectDir(dir).build()
        new VersioningPlugin().apply(project)
        project.versioning {
            scm = 'svn'
            dirtySuffix = '-dev'
        }

        VersionInfo info = project.versioning.info as VersionInfo
        assert info != null
        assert info.build == '2'
        assert info.branch == 'feature-test-1-my-feature'
        assert info.base == 'test-1-my-feature'
        assert info.branchId == 'feature-test-1-my-feature'
        assert info.branchType == 'feature'
        assert info.commit == '2'
        assert info.display == "feature-test-1-my-feature-2-dev"
        assert info.full == "feature-test-1-my-feature-2-dev"
        assert info.scm == 'svn'
        assert info.tag == null
        assert info.dirty
    }

    @Test
    void 'SVN release branch - dirty working copy - default'() {
        // SVN
        repo.mkdir 'project/branches/release-2.0', 'Feature branch'
        repo.mkdir 'project/branches/release-2.0/1', 'Commit for TEST-1'
        repo.mkdir 'project/branches/release-2.0/2', 'Commit for TEST-1'
        def dir = repo.checkout('project/branches/release-2.0')
        new File(dir, 'test.txt').text = 'test'

        def project = ProjectBuilder.builder().withProjectDir(dir).build()
        new VersioningPlugin().apply(project)
        project.versioning {
            scm = 'svn'
        }

        VersionInfo info = project.versioning.info as VersionInfo
        assert info != null
        assert info.build == '3'
        assert info.branch == 'release-2.0'
        assert info.base == '2.0'
        assert info.branchId == 'release-2.0'
        assert info.branchType == 'release'
        assert info.commit == '3'
        assert info.display == "2.0.0-dirty"
        assert info.full == "release-2.0-3-dirty"
        assert info.scm == 'svn'
        assert info.tag == null
        assert info.dirty
    }

    @Test
    void 'SVN release branch - dirty working copy - custom suffix'() {
        // SVN
        repo.mkdir 'project/branches/release-2.0', 'Feature branch'
        repo.mkdir 'project/branches/release-2.0/1', 'Commit for TEST-1'
        repo.mkdir 'project/branches/release-2.0/2', 'Commit for TEST-1'
        def dir = repo.checkout('project/branches/release-2.0')
        new File(dir, 'test.txt').text = 'test'

        def project = ProjectBuilder.builder().withProjectDir(dir).build()
        new VersioningPlugin().apply(project)
        project.versioning {
            scm = 'svn'
            dirtySuffix = '-DEV'
        }

        VersionInfo info = project.versioning.info as VersionInfo
        assert info != null
        assert info.build == '3'
        assert info.branch == 'release-2.0'
        assert info.base == '2.0'
        assert info.branchId == 'release-2.0'
        assert info.branchType == 'release'
        assert info.commit == '3'
        assert info.display == "2.0.0-DEV"
        assert info.full == "release-2.0-3-DEV"
        assert info.scm == 'svn'
        assert info.tag == null
        assert info.dirty
    }

    @Test
    void 'SVN release branch - dirty working copy - custom code'() {
        // SVN
        repo.mkdir 'project/branches/release-2.0', 'Feature branch'
        repo.mkdir 'project/branches/release-2.0/1', 'Commit for TEST-1'
        repo.mkdir 'project/branches/release-2.0/2', 'Commit for TEST-1'
        def dir = repo.checkout('project/branches/release-2.0')
        new File(dir, 'test.txt').text = 'test'

        def project = ProjectBuilder.builder().withProjectDir(dir).build()
        new VersioningPlugin().apply(project)
        project.versioning {
            scm = 'svn'
            dirty = { version -> "${version}-DONOTUSE" }
        }

        VersionInfo info = project.versioning.info as VersionInfo
        assert info != null
        assert info.build == '3'
        assert info.branch == 'release-2.0'
        assert info.base == '2.0'
        assert info.branchId == 'release-2.0'
        assert info.branchType == 'release'
        assert info.commit == '3'
        assert info.display == "2.0.0-DONOTUSE"
        assert info.full == "release-2.0-3-DONOTUSE"
        assert info.scm == 'svn'
        assert info.tag == null
        assert info.dirty
    }

    @Test(expected = DirtyException)
    void 'SVN release branch - dirty working copy - fail'() {
        // SVN
        repo.mkdir 'project/branches/release-2.0', 'Feature branch'
        repo.mkdir 'project/branches/release-2.0/1', 'Commit for TEST-1'
        repo.mkdir 'project/branches/release-2.0/2', 'Commit for TEST-1'
        def dir = repo.checkout('project/branches/release-2.0')
        new File(dir, 'test.txt').text = 'test'

        def project = ProjectBuilder.builder().withProjectDir(dir).build()
        new VersioningPlugin().apply(project)
        project.versioning {
            scm = 'svn'
            dirtyFailOnReleases = true
        }

        project.versioning.info as VersionInfo
    }

    @Test
    void 'SVN branch with env TEST_BRANCH'() {

        // SVN
        repo.mkdir 'project/branches/feature-test-1-my-feature', 'Feature branch'
        repo.mkdir 'project/branches/feature-test-1-my-feature/1', 'Commit for TEST-1'
        repo.mkdir 'project/branches/feature-test-1-my-feature/2', 'Commit for TEST-1'

        // Project
        def project = ProjectBuilder.builder().withProjectDir(repo.checkout('project/branches/feature-test-1-my-feature')).build()
        new VersioningPlugin().apply(project)
        project.versioning {
            scm = 'svn'
            branchEnv << 'SVN_TEST_BRANCH'
        }

        // Gets the info and checks it
        VersionInfo info = project.versioning.info as VersionInfo
        assert info != null
        assert info.build == '3'
        assert info.branch == 'feature-456-cute'
        assert info.base == '456-cute'
        assert info.branchId == 'feature-456-cute'
        assert info.branchType == 'feature'
        assert info.commit == '3'
        assert info.display == "feature-456-cute-3"
        assert info.full == "feature-456-cute-3"
        assert info.scm == 'svn'
        assert info.tag == null
        assert !info.dirty
    }
}
