package net.nemerosa.versioning.core;

/**
 * Default branch parser.
 * <p/>
 * Getting the version type from a branch. Default: getting the part before the first "/" (or a second
 * optional 'separator' parameter). If no slash is found, takes the branch name as whole.
 * <p/>
 * For example:
 * <p/>
 * * release/2.0 --> release
 * * feature/2.0 --> feature
 * * master --> master
 */
public class DefaultBranchParser implements BranchParser {

    public static final BranchParser INSTANCE = new DefaultBranchParser();

    @Override
    public BranchInfo parse(String branch, String branchTypeSeparator) {
        int pos = branch.indexOf(branchTypeSeparator);
        if (pos > 0) {
            return new BranchInfo(
                    branch.substring(0, pos),
                    branch.substring(pos + 1)
            );
        } else {
            return new BranchInfo(branch, "");
        }
    }
}