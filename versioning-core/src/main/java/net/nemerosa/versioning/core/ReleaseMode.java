package net.nemerosa.versioning.core;

/**
 * Release mode
 */
public interface ReleaseMode {

    String getDisplayVersion(String nextTag, String lastTag, String currentTag, VersioningConfig config);

}
