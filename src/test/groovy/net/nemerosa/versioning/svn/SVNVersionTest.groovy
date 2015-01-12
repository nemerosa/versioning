package net.nemerosa.versioning.svn

import net.nemerosa.versioning.VersionInfo
import net.nemerosa.versioning.VersioningPlugin
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
    }

    @Test
    void 'SVN trunk'() {
    }

}
