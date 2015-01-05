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
     * Getting the version type from a branch. Default: getting the part before the first "/". If no slash is found,
     * takes the branch name as whole.
     *
     * For example:
     *
     * * release/2.0 --> release
     * * feature/2.0 --> feature
     * * master --> master
     */
    Closure<String> branchType = { String branch ->
        int pos = branch.indexOf('/')
        if (pos > 0) {
            branch.substring(0, pos)
        } else {
            branch
        }
    }

    /**
     * Computes the full version.
     */
    Closure<String> versionFull = { branchId, abbreviated -> "${branchId}-${abbreviated}" }

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
        SCMInfo scmInfo = scmInfoService.getInfo(project, this)

        // Version source
        String versionBranch = scmInfo.branch

        // Source type
        String versionBranchType = branchType(versionBranch)

        // Branch identifier
        String versionBranchId = normalise(versionBranch)

        // Full version
        String versionFull = versionFull(versionBranchId, scmInfo)

        // Display version
//        if (versionSourceType == 'release') {
//            versionDisplay = getDisplayVersion(versionSource.substring(pos + 1))
//        } else {
//            versionDisplay = versionBranch
//        }

        // OK
        new VersionInfo(
                scm: scm,
                branch: versionBranch,
                branchType: versionBranchType,
                branchId: versionBranchId,
                full: versionFull,
        )
    }

    private static def normalise(String value) {
        value.replaceAll(/[^A-Za-z0-9\.\-_]/, '-')
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
