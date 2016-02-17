package net.nemerosa.versioning.core;

public class BranchInfo {

    private final String type;
    private final String base;

    public BranchInfo(String type, String base) {
        this.type = type;
        this.base = base;
    }

    public String getType() {
        return type;
    }

    public String getBase() {
        return base;
    }

}
