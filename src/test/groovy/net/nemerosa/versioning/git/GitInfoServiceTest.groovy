package net.nemerosa.versioning.git

import org.junit.Test

class GitInfoServiceTest {

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
                // Add a file
                cmd 'touch', 'test.txt'
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
