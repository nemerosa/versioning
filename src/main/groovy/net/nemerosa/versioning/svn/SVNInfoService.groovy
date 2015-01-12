package net.nemerosa.versioning.svn

import net.nemerosa.versioning.SCMInfo
import net.nemerosa.versioning.SCMInfoService
import net.nemerosa.versioning.VersioningExtension
import org.gradle.api.Project

class SVNInfoService implements SCMInfoService {

    @Override
    SCMInfo getInfo(Project project, VersioningExtension extension) {
        // Is SVN enabled?
        boolean hasSvn = project.file('.svn').exists()
        // No SVN information
        if (!hasSvn) {
            SCMInfo.NONE
        } else {
            // FIXME Method net.nemerosa.versioning.SCMInfoService.getInfo
            return null
        }
    }

    @Override
    List<String> getBaseTags(Project project, VersioningExtension extension, String base) {
        // FIXME Method net.nemerosa.versioning.SCMInfoService.getBaseTags
        return null
    }
}
