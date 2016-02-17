package net.nemerosa.versioning.core;

public class FullDisplayMode implements DisplayMode {

    public static final DisplayMode INSTANCE = new FullDisplayMode();

    @Override
    public String getDisplayVersion(String versionBranchType, String versionBranchId, String base, String abbreviated, String versionFull, VersioningConfig config) {
        return versionFull;
    }

}
