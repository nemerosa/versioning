package net.nemerosa.versioning

import net.nemerosa.versioning.git.GitInfoService
import net.nemerosa.versioning.support.DirtyException
import net.nemerosa.versioning.svn.SVNInfoService
import org.gradle.api.GradleException
import org.gradle.api.Project

class VersioningExtension {

    /**
     * Registry of SCM info services
     */
    private static final Map<String, SCMInfoService> INFO_SERVICES = [
            git: new GitInfoService(),
            svn: new SVNInfoService(),
    ]

    /**
     * Registry of display modes
     */
    private static final Map<String, Closure<String>> DISPLAY_MODES = [
            full    : { branchType, branchId, base, build, full, extension ->
                "${branchId}-${build}"
            },
            snapshot: { branchType, branchId, base, build, full, extension ->
                "${base}${extension.snapshot}"
            },
            base    : { branchType, branchId, base, build, full, extension ->
                base
            },
    ]

    /**
     * Registry of release modes
     */
    private static final Map<String, Closure<String>> RELEASE_MODES = [
            tag     : { nextTag, lastTag, currentTag, extension ->
                nextTag
            },
            snapshot: { nextTag, lastTag, currentTag, extension ->
                extension.releaseBuild && currentTag ? currentTag : "${nextTag}${extension.snapshot}"
            },
    ]

    /**
     * Version SCM - git by default
     */
    String scm = 'git'

    /**
     * Allow setting the root git repo directory for non conventional git/gradle setups.
     * This is the path to the root directory that contains the .git folder. This
     * is used to validate if the current project is a git repository.
     *
     */
    String gitRepoRootDir = null

    /**
     * Fetch the branch from environment variable if available.
     *
     * By default, the environment is not taken into account, in order to be backward compatible
     * with existing build systems.
     */
    List<String> branchEnv = []

    /**
     * Getting the version type from a branch. Default: getting the part before the first "/" (or a second
     * optional 'separator' parameter). If no slash is found, takes the branch name as whole.
     *
     * For example:
     *
     * * release/2.0 --> release
     * * feature/2.0 --> feature
     * * master --> master
     */
    Closure<ReleaseInfo> releaseParser = { SCMInfo scmInfo, String separator = '/' ->
        List<String> part = scmInfo.branch.split(separator, 2) + ''
        new ReleaseInfo(type: part[0], base: part[1])
    }

    /**
     * Computes the full version.
     */
    Closure<String> full = { SCMInfo scmInfo -> "${normalise(scmInfo.branch)}-${scmInfo.abbreviated}" }

    /**
     * Set of eligible branch types for computing a display version from the branch base name
     */
    Set<String> releases = ['release'] as Set<String>

    /**
     * Display mode
     */
    def displayMode = 'full'

    /**
     * Release mode
     */
    def releaseMode = 'tag'

    /**
     * True if it's release build. Default is true, and branch should be in releases-set.
     */
    def releaseBuild = true

    /**
     * Default Snapshot extension
     */
    String snapshot = '-SNAPSHOT'

    /**
     * Dirty mode.
     *
     * Closure that takes a version (<i>display</i> or <i>full</i>) and processes it to produce a <i>dirty</i>
     * indicator. By default, it appends the {@link #dirtySuffix} value to the version.
     */
    Closure<String> dirty = { version -> "${version}${dirtySuffix}" }

    /**
     * Default dirty suffix
     */
    String dirtySuffix = '-dirty'

    /**
     * If set to <code>true</code>, the build will fail if working copy is dirty and if the branch type is
     * part of the {@link #releases} list ("release" only by default).
     */
    boolean dirtyFailOnReleases = false

    /**
     * If set to <code>true</code>, no warning will be printed in case the workspace is dirty.
     */
    boolean noWarningOnDirty = false

    /**
     * Credentials (for SVN only)
     */
    String user = ''

    /**
     * Credentials (for SVN only)
     */
    String password = ''

    /**
     * Pattern used to match when looking for the last tag. By default, checks for any
     * tag having a last part being numeric. At least one numeric grouping
     * expression is required. The first one will be used to reverse order
     * the tags in Git.
     */
    String lastTagPattern = /(\d+)$/

    /**
     * Certificate - accept SSL server certificates from unknown certificate authorities (for SVN only)
     */
    @Deprecated
    boolean trustServerCert = false

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

        // No info?
        if (scmInfo == SCMInfo.NONE) {
            return VersionInfo.NONE
        }

        // Branch parsing
        ReleaseInfo releaseInfo = releaseParser(scmInfo, scmInfoService.branchTypeSeparator)
        String versionReleaseType = releaseInfo.type
        String versionBase = releaseInfo.base

        // Branch identifier
        String versionBranchId = normalise(scmInfo.branch)

        // Full version
        String versionFull = full(scmInfo)

        // Display version
        String versionDisplay
        if (versionReleaseType in releases) {
            List<String> baseTags = scmInfoService.getBaseTags(project, this, versionBase)
            versionDisplay = getDisplayVersion(scmInfo, releaseInfo, baseTags)
        } else {
            // Adjusting the base
            def base = versionBase ?: versionBranchId
            // Display mode
            if (displayMode instanceof String) {
                def mode = DISPLAY_MODES[displayMode as String]
                if (mode) {
                    versionDisplay = mode(versionReleaseType, versionBranchId, base, scmInfo.abbreviated, versionFull, this)
                } else {
                    throw new GradleException("${mode} is not a valid display mode.")
                }
            } else if (displayMode instanceof Closure) {
                def mode = displayMode as Closure
                versionDisplay = mode(versionReleaseType, versionBranchId, base, scmInfo.abbreviated, versionFull, this)
            } else {
                throw new GradleException("The `displayMode` must be a registered default mode or a Closure.")
            }
        }

        // Dirty update
        if (scmInfo.dirty) {
            if (dirtyFailOnReleases && versionReleaseType in releases) {
                throw new DirtyException()
            } else {
                if (!noWarningOnDirty) {
                    project.getLogger().warn("[versioning] WARNING - the working copy has unstaged or uncommitted changes.")
                }
                versionDisplay = dirty(versionDisplay)
                versionFull = dirty(versionFull)
            }
        }

        // OK
        new VersionInfo(
                scm: scm,
                branch: scmInfo.branch,
                branchType: versionReleaseType,
                branchId: versionBranchId,
                full: versionFull,
                base: versionBase,
                display: versionDisplay,
                commit: scmInfo.commit,
                build: scmInfo.abbreviated,
                tag: scmInfo.tag,
                lastTag: scmInfo.lastTag,
                dirty: scmInfo.dirty,
                shallow: scmInfo.shallow,
        )
    }

    private String getDisplayVersion(SCMInfo scmInfo, ReleaseInfo releaseInfo, List<String> baseTags) {
        String currentTag = scmInfo.tag
        if (scmInfo.shallow) {
            // In case the repository has no history (shallow clone or check out), the last
            // tags cannot be get and the display version cannot be computed correctly.
            if (releaseBuild && currentTag) {
                // The only special case is when the HEAD commit is exactly on a tag and we can use it
                return currentTag
            } else {
                // In any other case, we can only start from the base information
                // and add a snapshot information
                return "${releaseInfo.base}${snapshot}"
            }
        } else {
            String lastTag
            String nextTag
            if (baseTags.empty) {
                lastTag = ''
                nextTag = "${releaseInfo.base}.0"
            } else {
                lastTag = baseTags[0].trim()
                def lastNumber = (lastTag =~ /${releaseInfo.base}\.(\d+)/)[0][1] as int
                def newNumber = lastNumber + 1
                nextTag = "${releaseInfo.base}.${newNumber}"
            }
            Closure<String> mode
            if (releaseMode instanceof String) {
                mode = RELEASE_MODES[releaseMode]
                if (!mode) {
                    throw new GradleException("${releaseMode} is not a valid release mode.")
                }
            } else if (releaseMode instanceof Closure) {
                mode = releaseMode as Closure
            } else {
                throw new GradleException("The `releaseMode` must be a registered default mode or a Closure.")
            }
            return mode(nextTag, lastTag, currentTag, this)
        }
    }

    static String normalise(String value) {
        value.replaceAll(/[^A-Za-z0-9.\-_]/, '-')
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
