package net.nemerosa.versioning.core;

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
