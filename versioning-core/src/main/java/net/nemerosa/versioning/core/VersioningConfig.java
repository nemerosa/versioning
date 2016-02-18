package net.nemerosa.versioning.core;

import java.util.Collections;
import java.util.Set;

/**
 * Configuration for the versioning.
 */
public class VersioningConfig {

    /**
     * SCM to use - `git` by default
     */
    private String scm = "git";

    /**
     * Branch parser
     *
     * @see DefaultBranchParser
     */
    private BranchParser branchParser = DefaultBranchParser.INSTANCE;

    /**
     * Full version builder
     */
    private FullVersionBuilder fullVersionBuilder = DefaultFullVersionBuilder.INSTANCE;

    /**
     * Set of eligible branch types for computing a display version from the branch base name
     */
    private Set<String> releases = Collections.singleton("release");

    /**
     * Display mode
     */
    private DisplayMode displayMode = FullDisplayMode.INSTANCE;

    /**
     * Release mode
     */
    private ReleaseMode releaseMode = TagReleaseMode.INSTANCE;

    /**
     * Default Snapshot extension
     */
    private String snapshot = "-SNAPSHOT";

    /**
     * Dirty mode suffix.
     * <p/>
     * Suffix to append to the version (<i>display</i> or <i>full</i>) when the working copy is dirty.
     */
    private String dirtySuffix = "-dirty";

    /**
     * If set to <code>true</code>, the build will fail if working copy is dirty and if the branch type is
     * part of the {@link #releases} list ("release" only by default).
     */
    private boolean dirtyFailOnReleases = false;

    /**
     * If set to <code>true</code>, no warning will be printed in case the workspace is dirty.
     */
    private boolean noWarningOnDirty = false;

    /**
     * Credentials (for SVN only)
     */
    private String user = "";

    /**
     * Credentials (for SVN only)
     */
    private String password = "";

    /**
     * Certificate - accept SSL server certificates from unknown certificate authorities (for SVN only)
     */
    private boolean trustServerCert = false;

    // Accessors

    public String getScm() {
        return scm;
    }

    public void setScm(String scm) {
        this.scm = scm;
    }

    public BranchParser getBranchParser() {
        return branchParser;
    }

    public void setBranchParser(BranchParser branchParser) {
        this.branchParser = branchParser;
    }

    public FullVersionBuilder getFullVersionBuilder() {
        return fullVersionBuilder;
    }

    public void setFullVersionBuilder(FullVersionBuilder fullVersionBuilder) {
        this.fullVersionBuilder = fullVersionBuilder;
    }

    public Set<String> getReleases() {
        return releases;
    }

    public void setReleases(Set<String> releases) {
        this.releases = releases;
    }

    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(DisplayMode displayMode) {
        this.displayMode = displayMode;
    }

    public ReleaseMode getReleaseMode() {
        return releaseMode;
    }

    public void setReleaseMode(ReleaseMode releaseMode) {
        this.releaseMode = releaseMode;
    }

    public String getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(String snapshot) {
        this.snapshot = snapshot;
    }

    public String getDirtySuffix() {
        return dirtySuffix;
    }

    public void setDirtySuffix(String dirtySuffix) {
        this.dirtySuffix = dirtySuffix;
    }

    public boolean isDirtyFailOnReleases() {
        return dirtyFailOnReleases;
    }

    public void setDirtyFailOnReleases(boolean dirtyFailOnReleases) {
        this.dirtyFailOnReleases = dirtyFailOnReleases;
    }

    public boolean isNoWarningOnDirty() {
        return noWarningOnDirty;
    }

    public void setNoWarningOnDirty(boolean noWarningOnDirty) {
        this.noWarningOnDirty = noWarningOnDirty;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isTrustServerCert() {
        return trustServerCert;
    }

    public void setTrustServerCert(boolean trustServerCert) {
        this.trustServerCert = trustServerCert;
    }
}
