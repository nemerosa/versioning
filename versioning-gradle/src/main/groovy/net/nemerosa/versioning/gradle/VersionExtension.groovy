package net.nemerosa.versioning.gradle

class VersionExtension {


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
            tag : { nextTag, lastTag, currentTag, extension ->
                nextTag
            },
            snapshot: { nextTag, lastTag, currentTag, extension ->
                currentTag ?: "${nextTag}${extension.snapshot}"
            },
    ]

    /**
     * Version SCM - git by default
     */
    String scm = 'git'

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
    Closure<BranchInfo> branchParser = { String branch, String separator = '/' ->
        int pos = branch.indexOf(separator)
        if (pos > 0) {
            new BranchInfo(type: branch.substring(0, pos), base: branch.substring(pos + 1))
        } else {
            new BranchInfo(type: branch, base: '')
        }
    }

    /**
     * Computes the full version.
     */
    Closure<String> full = { branchId, abbreviated -> "${branchId}-${abbreviated}" }

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
    boolean noWarningOnDirty = false;

    /**
     * Credentials (for SVN only)
     */
    String user = ''

    /**
     * Credentials (for SVN only)
     */
    String password = ''

    /**
     * Certificate - accept SSL server certificates from unknown certificate authorities (for SVN only)
     */
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

        // Version source
        String versionBranch = scmInfo.branch

        // Branch parsing
        BranchInfo branchInfo = branchParser(versionBranch, scmInfoService.branchTypeSeparator)
        String versionBranchType = branchInfo.type
        String versionBase = branchInfo.base

        // Branch identifier
        String versionBranchId = normalise(versionBranch)

        // Full version
        String versionFull = full(versionBranchId, scmInfo.abbreviated)

        // Display version
        String versionDisplay
        if (versionBranchType in releases) {
            List<String> baseTags = scmInfoService.getBaseTags(project, this, versionBase)
            versionDisplay = getDisplayVersion(scmInfo, branchInfo, baseTags)
        } else {
            // Adjusting the base
            def base = versionBase ?: versionBranchId
            // Display mode
            if (displayMode instanceof String) {
                def mode = DISPLAY_MODES[displayMode as String]
                if (mode) {
                    versionDisplay = mode(versionBranchType, versionBranchId, base, scmInfo.abbreviated, versionFull, this)
                } else {
                    throw new GradleException("${mode} is not a valid display mode.")
                }
            } else if (displayMode instanceof Closure) {
                def mode = displayMode as Closure
                versionDisplay = mode(versionBranchType, versionBranchId, base, scmInfo.abbreviated, versionFull, this)
            } else {
                throw new GradleException("The `displayMode` must be a registered default mode or a Closure.")
            }
        }

        // Dirty update
        if (scmInfo.dirty) {
            if (dirtyFailOnReleases && versionBranchType in releases) {
                throw new DirtyException()
            } else {
                if (!noWarningOnDirty) {
                    println "[versioning] WARNING - the working copy has unstaged or uncommitted changes."
                }
                versionDisplay = dirty(versionDisplay)
                versionFull = dirty(versionFull)
            }
        }

        // OK
        new VersionInfo(
                scm: scm,
                branch: versionBranch,
                branchType: versionBranchType,
                branchId: versionBranchId,
                full: versionFull,
                base: versionBase,
                display: versionDisplay,
                commit: scmInfo.commit,
                build: scmInfo.abbreviated,
        )
    }

    private String getDisplayVersion(SCMInfo scmInfo, BranchInfo branchInfo, List<String> baseTags) {
        String currentTag = scmInfo.tag
        String lastTag
        String nextTag
        if (baseTags.empty) {
            lastTag = ''
            nextTag = "${branchInfo.base}.0"
        } else {
            lastTag = baseTags[0].trim()
            def lastNumber = (lastTag =~ /${branchInfo.base}\.(\d+)/)[0][1] as int
            def newNumber = lastNumber + 1
            nextTag = "${branchInfo.base}.${newNumber}"
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

    private static String normalise(String value) {
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
