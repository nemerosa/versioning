package net.nemerosa.versioning.git

import org.ajoberstar.grgit.Status
import org.junit.Test

class GitInfoServiceTest {

    @Test
    void 'Git - clean'() {
        GitRepo repo = new GitRepo()
        try {
            repo.with {
                commit 1
            }
            Status status = GitInfoService.getStatus(repo.dir)
            assert !GitInfoService.isGitTreeDirty(status): "Git tree clean"
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
                commit 1
                // Need to modify a tracked file, not just create a new untracked file
                //cmd 'touch', 'test.txt'
                new File(dir, 'file1') << 'Add some content'
            }
            Status status = GitInfoService.getStatus(repo.dir)
            assert GitInfoService.isGitTreeDirty(status): "Unstaged changes"
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
                commit 1
                // Add a file, without committing it
                new File(repo.dir, 'test.txt').text = 'Test'
                add 'test.txt'
            }
            Status status = GitInfoService.getStatus(repo.dir)
            assert GitInfoService.isGitTreeDirty(status): "Uncommitted changes"
        } finally {
            repo.close()
        }
    }

}
