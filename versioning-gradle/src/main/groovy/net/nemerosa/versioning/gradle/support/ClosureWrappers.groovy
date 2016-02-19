package net.nemerosa.versioning.gradle.support

import net.nemerosa.versioning.core.*

class ClosureWrappers {

    static BranchParser branchParserClosure(Closure<BranchInfo> closure) {
        return new BranchParser() {
            @Override
            BranchInfo parse(String branch, String branchTypeSeparator) {
                return closure(branch, branchTypeSeparator)
            }
        }
    }

    static ReleaseMode releaseModeClosure(Closure<String> closure) {
        return new ReleaseMode() {
            @Override
            String getDisplayVersion(String nextTag, String lastTag, String currentTag, VersioningConfig config) {
                return closure(nextTag, lastTag, currentTag, config)
            }
        }
    }

    static DisplayMode displayModeClosure(Closure<String> closure) {
        return new DisplayMode() {
            @Override
            String getDisplayVersion(String versionBranchType, String versionBranchId, String base, String abbreviated, String versionFull, VersioningConfig config) {
                return closure(versionBranchType, versionBranchId, base, abbreviated, versionFull, config)
            }
        }
    }

    static FullVersionBuilder fullVersionBuilderClosure(Closure<String> closure) {
        return new FullVersionBuilder() {
            @Override
            String build(String branchId, String abbreviated) {
                return closure(branchId, abbreviated)
            }
        }
    }

}
