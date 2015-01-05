package net.nemerosa.versioning

import net.nemerosa.versioning.git.GitInfoService
import org.gradle.api.GradleException
import org.gradle.api.Project

class VersioningExtension {

    /**
     * Registry of SCM info services
     */
    private static final Map<String, GitInfoService> INFO_SERVICES = [
            'git': new GitInfoService(),
            // TODO SVN
    ]

    /**
     * Version SCM - git by default
     */
    String scm = 'git'

    /**
     * Computed version information
     */
    private VersionInfo info

    /**
     * Linked project
     */
    private final Project project

    /**
     * Constructor
     * @param project Linked project
     */
    VersioningExtension(Project project) {
        this.project = project
    }
/**
     * Gets the computed version information
     */
    VersionInfo getInfo() {
        if (!info) {
            info = computeInfo()
        }
        info
    }

    /**
     * Computes the version information.
     */
    VersionInfo computeInfo() {
        // Gets the SCM info service
        SCMInfoService scmInfoService = getSCMInfoService(scm)
        // Gets the version source
        SCMInfo scmInfo = scmInfoService.getInfo(project)
        // TODO Parses the version source
    }

    private static SCMInfoService getSCMInfoService(String type) {
        SCMInfoService scmInfoService = INFO_SERVICES[type]
        if (scmInfoService) {
            return scmInfoService
        } else {
            throw new GradleException("Unknown SCM info service: ${type}")
        }
    }
}
