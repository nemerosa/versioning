package net.nemerosa.versioning.core;

/**
 * Branch parser. Getting the version type from a branch.
 */
public interface BranchParser {

    /**
     * Parses the name of the SCM branch in order to
     * get its type.
     */
    BranchInfo parse(String branch, String branchTypeSeparator);

}