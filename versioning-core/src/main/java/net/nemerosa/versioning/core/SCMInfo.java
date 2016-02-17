package net.nemerosa.versioning.core;

import org.apache.commons.lang3.StringUtils;

public class SCMInfo {

    public static final SCMInfo NONE = new SCMInfo(
            "",
            "",
            "",
            "",
            false
    );

    private final String branch;
    private final String commit;
    private final String abbreviated;
    private final String tag;
    private final boolean dirty;

    public SCMInfo(String branch, String commit, String abbreviated, String tag, boolean dirty) {
        this.branch = branch;
        this.commit = commit;
        this.abbreviated = abbreviated;
        this.tag = tag;
        this.dirty = dirty;
    }

    public String getBranch() {
        return branch;
    }

    public String getCommit() {
        return commit;
    }

    public String getAbbreviated() {
        return abbreviated;
    }

    public String getTag() {
        return tag;
    }

    public boolean isDirty() {
        return dirty;
    }

    public boolean isEmpty() {
        return StringUtils.isBlank(branch);
    }
}
