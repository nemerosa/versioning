package net.nemerosa.versioning.core;

/**
 * Tag release mode
 */
public class TagReleaseMode implements ReleaseMode {

    public static final ReleaseMode INSTANCE = new TagReleaseMode();

    @Override
    public String getDisplayVersion(String nextTag, String lastTag, String currentTag, VersioningConfig config) {
        return nextTag;
    }

}
