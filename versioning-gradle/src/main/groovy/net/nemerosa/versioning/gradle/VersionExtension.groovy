package net.nemerosa.versioning.gradle

import net.nemerosa.versioning.core.*
import org.gradle.api.Project

class VersionExtension {

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
    def branchParser = 'default'

    /**
     * Computes the full version.
     */
    def full = 'default'

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
    VersionExtension(Project project) {
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
        // Configuration
        VersioningConfig config = createConfig()
        // Project interface
        ProjectIntf projectIntf = new GradleProjectIntf(project)
        // Calling the service
        return new DefaultVersionService().computeVersionInfo(projectIntf, config)
    }

    VersioningConfig createConfig() {
        def config = new VersioningConfig()

        // SCM
        config.scm = scm

        // Branch parser
        config.branchParser = branchParser instanceof Closure ? branchParser as BranchParser : BranchParsers.get(branchParser as String)

        // Full version builder
        config.fullVersionBuilder = full instanceof Closure ? full as FullVersionBuilder : FullVersionBuilders.get(full as String)

        // Releases
        config.releases = releases

        // Display mode
        config.displayMode = displayMode instanceof Closure ? displayMode as DisplayMode : DisplayModes.get(displayMode as String)

        // Release mode
        config.releaseMode = releaseMode instanceof Closure ? releaseMode as ReleaseMode : ReleaseModes.get(releaseMode as String)

        // Snapshot suffix
        config.snapshot = snapshot

        // Dirty properties
        config.dirtySuffix = dirtySuffix
        config.dirtyFailOnReleases = dirtyFailOnReleases
        config.noWarningOnDirty = noWarningOnDirty

        // SVN properties
        config.user = user
        config.password = password
        config.trustServerCert = trustServerCert

        // OK
        return config
    }
}
