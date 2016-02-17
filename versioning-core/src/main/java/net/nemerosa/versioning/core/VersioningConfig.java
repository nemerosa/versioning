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
    private FullVersionBuilder fullVersionBuilder = new DefaultFullVersionBuilder();

    /**
     * Set of eligible branch types for computing a display version from the branch base name
     */
    private Set<String> releases = Collections.singleton("release");

    /**
     * Display mode
     * <p/>
     * TODO Accept closure
     */
    private String displayMode = "full";

    /**
     * Release mode
     * <p/>
     * TODO Accept closure
     */
    private String releaseMode = "tag";

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

    /**
     * Full version builder
     */
    public interface FullVersionBuilder {

        /**
         * Given the branch ID and an abbreviated form
         * of revision or commit, returns the full version.
         */
        String build(String branchId, String abbreviated);

    }

    /**
     * Default full version builder
     */
    public static class DefaultFullVersionBuilder implements FullVersionBuilder {

        public static final FullVersionBuilder INSTANCE = new DefaultFullVersionBuilder();

        /**
         * Given the branch ID and an abbreviated form
         * of revision or commit, returns the full version.
         */
        @Override
        public String build(String branchId, String abbreviated) {
            return String.format(
                    "%s-%s",
                    branchId,
                    abbreviated
            );
        }

    }

    /**
     * Branch parser. Getting the version type from a branch.
     */
    public interface BranchParser {

        /**
         * Parses the name of the SCM branch in order to
         * get its type.
         */
        BranchInfo parse(String branch);

    }

    /**
     * Default branch parser.
     * <p/>
     * Getting the version type from a branch. Default: getting the part before the first "/" (or a second
     * optional 'separator' parameter). If no slash is found, takes the branch name as whole.
     * <p/>
     * For example:
     * <p/>
     * * release/2.0 --> release
     * * feature/2.0 --> feature
     * * master --> master
     */
    public static class DefaultBranchParser implements BranchParser {

        public static final BranchParser INSTANCE = new DefaultBranchParser();
        public static final String SEPARATOR = "/";

        private final String separator;

        public DefaultBranchParser() {
            this(SEPARATOR);
        }

        public DefaultBranchParser(String separator) {
            this.separator = separator;
        }

        @Override
        public BranchInfo parse(String branch) {
            int pos = branch.indexOf(separator);
            if (pos > 0) {
                return new BranchInfo(
                        branch.substring(0, pos),
                        branch.substring(pos + 1)
                );
            } else {
                return new BranchInfo(branch, "");
            }
        }
    }

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

    public String getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(String displayMode) {
        this.displayMode = displayMode;
    }

    public String getReleaseMode() {
        return releaseMode;
    }

    public void setReleaseMode(String releaseMode) {
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
