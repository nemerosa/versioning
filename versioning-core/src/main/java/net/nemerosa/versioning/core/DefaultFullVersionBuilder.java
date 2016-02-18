package net.nemerosa.versioning.core;

/**
 * Default full version builder
 */
public class DefaultFullVersionBuilder implements FullVersionBuilder {

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
