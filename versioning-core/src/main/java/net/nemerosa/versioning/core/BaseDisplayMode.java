package net.nemerosa.versioning.core;

public class BaseDisplayMode implements DisplayMode {

    @Override
    public String getDisplayVersion(String versionBranchType, String versionBranchId, String base, String abbreviated, String versionFull, VersioningConfig config) {
        return base;
    }

}
