package net.nemerosa.versioning

import org.gradle.api.Project

interface SCMInfoService {

    SCMInfo getInfo(Project project, VersioningExtension extension)

    List<String> getLastTags(Project project, VersioningExtension extension, String tagPattern)

    List<String> getBaseTags(Project project, VersioningExtension extension, String base)

    String getBranchTypeSeparator()
}
