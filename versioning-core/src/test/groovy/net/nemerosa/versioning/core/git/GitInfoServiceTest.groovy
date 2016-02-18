package net.nemerosa.versioning.core.git

import org.junit.Test

class GitInfoServiceTest {

    @Test
    void 'Selection of base tags'() {
        assert GitInfoService.selectBaseTags(
                '2.0',
                [
                        '(tag: 2.0.9)',
                        '(tag: 2.0.10)',
                        '(tag: 2.0.11)',
                        '(tag: 2.1.0)'
                ]
        ) == ['2.0.9', '2.0.10', '2.0.11']
    }

    @Test
    void 'Git - clean'() {
        GitRepo repo = new GitRepo()
        try {
            repo.with {
                git 'init'
                commit 1
            }
            assert !GitInfoService.isGitTreeDirty(repo.dir): "Git tree clean"
        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git - unstaged'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                git 'init'
                commit 1
                // Need to modify a tracked file, not just create a new untracked file
                //cmd 'touch', 'test.txt'
                new File(dir, 'file1') << 'Add some content'
            }
            assert GitInfoService.isGitTreeDirty(repo.dir): "Unstaged changes"
        } finally {
            repo.close()
        }
    }

    @Test
    void 'Git - uncommitted'() {
        GitRepo repo = new GitRepo()
        try {
            // Git initialisation
            repo.with {
                git 'init'
                commit 1
                // Add a file
                cmd 'touch', 'test.txt'
                git 'add', 'test.txt'
            }
            assert GitInfoService.isGitTreeDirty(repo.dir): "Uncommitted changes"
        } finally {
            repo.close()
        }
    }

}
