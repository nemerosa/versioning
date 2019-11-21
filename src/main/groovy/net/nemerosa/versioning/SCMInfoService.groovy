package net.nemerosa.versioning

import org.gradle.api.Project

interface SCMInfoService {

    /**
     * Get SCM Info
     * @param project Gradle's project
     * @param extension Plugin data holder
     * @return Information got from scm
     */
    SCMInfo getInfo(Project project, VersioningExtension extension)

    /**
     * Get last tags
     *
     * @param project Gradle's project
     * @param extension Plugin data holder
     * @param tagPattern Tag pattern
     * @return List of tags
     */
    List<String> getLastTags(Project project, VersioningExtension extension, String tagPattern)

    /**
     * Get base tags
     *
     * @param project Gradle's project
     * @param extension Plugin data holder
     * @param base Base of branch in case where name of branch is something/base
     * @return List of base tags
     */
    List<String> getBaseTags(Project project, VersioningExtension extension, String base)

    /**
     *
     * @return Separator used for separating branch type and branch base
     */
    String getBranchTypeSeparator()
}
