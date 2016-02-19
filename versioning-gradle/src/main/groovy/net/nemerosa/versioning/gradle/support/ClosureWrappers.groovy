package net.nemerosa.versioning.gradle.support

import net.nemerosa.versioning.core.*

class ClosureWrappers {

    static Closure<BranchInfo> branchParserClosure(BranchParser branchParser) {
        { String branch, String separator = '/' ->
            branchParser.parse(branch, separator)
        }
    }

    static Closure<String> fullVersionBuilderClosure(FullVersionBuilder fullVersionBuilder) {
        { String branchId, String abbreviated ->
            fullVersionBuilder.build(branchId, abbreviated)
        }
    }

    static Closure<String> displayModeClosure(DisplayMode displayMode) {
        { String versionBranchType, String versionBranchId, String base, String abbreviated, String versionFull, VersioningConfig config ->
            displayMode.getDisplayVersion(versionBranchType, versionBranchId, base, abbreviated, versionFull, config)
        }
    }

}
