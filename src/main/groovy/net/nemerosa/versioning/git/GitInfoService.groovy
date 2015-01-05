package net.nemerosa.versioning.git

import net.nemerosa.versioning.SCMInfo
import net.nemerosa.versioning.SCMInfoService
import net.nemerosa.versioning.VersioningExtension
import org.gradle.api.Project

class GitInfoService implements SCMInfoService {

    @Override
    SCMInfo getInfo(Project project, VersioningExtension extension) {
        // Is Git enabled?
        boolean hasGit = project.file('.git').exists()
        // No Git information
        if (!hasGit) {
            SCMInfo.NONE
        }
        // Git information available
        else {
            // Gets the branch info
            String branch = 'git rev-parse --abbrev-ref HEAD'.execute().text.trim()
            // Gets the commit info (full hash)
            String commit = 'git log -1 --format=%H'.execute().text.trim()
            // Gets the current commit (short hash)
            String abbreviated = 'git log -1 --format=%h'.execute().text.trim()
            // Returns the information
            new SCMInfo(
                    branch: branch,
                    commit: commit,
                    abbreviated: abbreviated
            )
        }
    }

}
