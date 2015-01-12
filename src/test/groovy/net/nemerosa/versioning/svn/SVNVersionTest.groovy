package net.nemerosa.versioning.svn

import net.nemerosa.versioning.VersionInfo
import net.nemerosa.versioning.VersioningPlugin
import net.nemerosa.versioning.tasks.VersionDisplayTask
import org.gradle.api.DefaultTask
import org.gradle.testfixtures.ProjectBuilder
import org.junit.After
import org.junit.Before
import org.junit.Test

import java.util.concurrent.atomic.AtomicInteger

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
    void 'SVN: not present'() {
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
    }

    @Test
    void 'SVN: trunk'() {
        // SVN
        repo.mkdir 'project/trunk', 'Trunk'
        repo.mkdir 'project/trunk/1', 'Commit for TEST-1'
        repo.mkdir 'project/trunk/2', 'Commit for TEST-2'
        // Project
        def project = ProjectBuilder.builder().withProjectDir(SVNRepo.checkout('project/trunk')).build()
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
    }

    @Test
    void 'SVN: display version'() {
        // SVN
        repo.mkdir 'project/trunk', 'Trunk'
        repo.mkdir 'project/trunk/1', 'Commit for TEST-1'
        repo.mkdir 'project/trunk/2', 'Commit for TEST-2'
        // Project
        def project = ProjectBuilder.builder().withProjectDir(SVNRepo.checkout('project/trunk')).build()
        new VersioningPlugin().apply(project)
        project.versioning {
            scm = 'svn'
        }
        // Checks the versionDisplay exists and runs it
        def task = project.tasks.getByName('versionDisplay') as VersionDisplayTask
        task.execute()
    }

    @Test
    void 'SVN: version file - defaults'() {
        // SVN
        repo.mkdir 'project/trunk', 'Trunk'
        repo.mkdir 'project/trunk/1', 'Commit for TEST-1'
        repo.mkdir 'project/trunk/2', 'Commit for TEST-2'
        // Project
        def project = ProjectBuilder.builder().withProjectDir(SVNRepo.checkout('project/trunk')).build()
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
VERSION_BUILD = 3
VERSION_BRANCH = trunk
VERSION_BASE = \n\
VERSION_BRANCHID = trunk
VERSION_BRANCHTYPE = trunk
VERSION_COMMIT = 3
VERSION_DISPLAY = trunk-3
VERSION_FULL = trunk-3
VERSION_SCM = svn
"""
    }

    @Test
    void 'SVN: version file - custom prefix'() {
        // SVN
        repo.mkdir 'project/trunk', 'Trunk'
        repo.mkdir 'project/trunk/1', 'Commit for TEST-1'
        repo.mkdir 'project/trunk/2', 'Commit for TEST-2'
        // Project
        def project = ProjectBuilder.builder().withProjectDir(SVNRepo.checkout('project/trunk')).build()
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
CUSTOM_BUILD = 3
CUSTOM_BRANCH = trunk
CUSTOM_BASE = \n\
CUSTOM_BRANCHID = trunk
CUSTOM_BRANCHTYPE = trunk
CUSTOM_COMMIT = 3
CUSTOM_DISPLAY = trunk-3
CUSTOM_FULL = trunk-3
CUSTOM_SCM = svn
"""
    }

    @Test
    void 'SVN: version file - custom file'() {
        // SVN
        repo.mkdir 'project/trunk', 'Trunk'
        repo.mkdir 'project/trunk/1', 'Commit for TEST-1'
        repo.mkdir 'project/trunk/2', 'Commit for TEST-2'
        // Project
        def project = ProjectBuilder.builder().withProjectDir(SVNRepo.checkout('project/trunk')).build()
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
VERSION_BUILD = 3
VERSION_BRANCH = trunk
VERSION_BASE = \n\
VERSION_BRANCHID = trunk
VERSION_BRANCHTYPE = trunk
VERSION_COMMIT = 3
VERSION_DISPLAY = trunk-3
VERSION_FULL = trunk-3
VERSION_SCM = svn
"""
    }

    @Test
    void 'SVN: feature branch'() {

        // SVN
        repo.mkdir 'project/branches/feature-test-1-my-feature', 'Feature branch'
        repo.mkdir 'project/branches/feature-test-1-my-feature/1', 'Commit for TEST-1'
        repo.mkdir 'project/branches/feature-test-1-my-feature/2', 'Commit for TEST-1'

        // Project
        def project = ProjectBuilder.builder().withProjectDir(SVNRepo.checkout('project/branches/feature-test-1-my-feature')).build()
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
    }

}
