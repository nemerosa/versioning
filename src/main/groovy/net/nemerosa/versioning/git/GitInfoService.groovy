package net.nemerosa.versioning.git

import net.nemerosa.versioning.SCMInfo
import net.nemerosa.versioning.SCMInfoService
import org.gradle.api.Project

class GitInfoService implements SCMInfoService {

    @Override
    SCMInfo getInfo(Project project) {
        // Is Git enabled?
        boolean hasGit = project.file('.git').exists()
        // No Git information
        if (!hasGit) {
            return SCMInfo.NONE
        }
        // FIXME Method net.nemerosa.versioning.SCMInfoService.getInfo
        return null
    }

}
