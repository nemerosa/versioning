package net.nemerosa.versioning.core;

import org.apache.commons.lang3.StringUtils;

public class SCMInfo {

    public static final SCMInfo NONE = new SCMInfo();

    private String branch;
    private String commit;
    private String abbreviated;
    private String tag;
    private boolean dirty;

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public String getAbbreviated() {
        return abbreviated;
    }

    public void setAbbreviated(String abbreviated) {
        this.abbreviated = abbreviated;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isEmpty() {
        return StringUtils.isBlank(branch);
    }
}
