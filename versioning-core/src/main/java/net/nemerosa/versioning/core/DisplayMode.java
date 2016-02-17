package net.nemerosa.versioning.core;

public interface DisplayMode {

    String getDisplayVersion(String versionBranchType, String versionBranchId, String base, String abbreviated, String versionFull, VersioningConfig config);

}
