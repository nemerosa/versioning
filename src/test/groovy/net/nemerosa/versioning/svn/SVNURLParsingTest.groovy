package net.nemerosa.versioning.svn

import org.junit.Test

class SVNURLParsingTest {

    @Test
    void 'Trunk'() {
        assert SVNInfoService.parseBranch('svn://localhost/project/trunk') == 'trunk'
    }

    @Test
    void 'Branch'() {
        assert SVNInfoService.parseBranch('svn://localhost/project/branches/xxx-yyy') == 'xxx-yyy'
    }

    @Test(expected = SVNInfoURLException)
    void 'Directory under trunk'() {
        SVNInfoService.parseBranch('svn://localhost/project/trunk/zzz')
    }

    @Test(expected = SVNInfoURLException)
    void 'Directory under branch'() {
        SVNInfoService.parseBranch('svn://localhost/project/branches/xxx-yyy/zzz')
    }

    @Test
    void 'Tag'() {
        assert SVNInfoService.parseBranch('svn://localhost/project/tags/1.0') == '1.0'
    }

    @Test(expected = SVNInfoURLException)
    void 'Root'() {
        SVNInfoService.parseBranch('svn://localhost/project')
    }

}
