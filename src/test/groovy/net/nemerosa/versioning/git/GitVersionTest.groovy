package net.nemerosa.versioning.git

import net.nemerosa.versioning.VersionInfo
import net.nemerosa.versioning.VersioningPlugin
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
        assert info.scm == 'git'
    }

}
