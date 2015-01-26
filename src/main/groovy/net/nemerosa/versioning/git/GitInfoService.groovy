package net.nemerosa.versioning.git

import net.nemerosa.versioning.SCMInfo
import net.nemerosa.versioning.SCMInfoService
import net.nemerosa.versioning.VersioningExtension
import org.gradle.api.Project

import static net.nemerosa.versioning.support.Utils.run

class GitInfoService implements SCMInfoService {

    @Override
    SCMInfo getInfo(Project project, VersioningExtension extension) {
        // Is Git enabled?
        boolean hasGit = project.file('.git').exists()
        // No Git information
        if (!hasGit) {
            SCMInfo.NONE
        }
        // Git information available
        else {
            // Gets the branch info
            String branch = run(project.projectDir, 'git', 'rev-parse', '--abbrev-ref', 'HEAD')
            // Gets the commit info (full hash)
            String commit = run(project.projectDir, 'git', 'log', '-1', '--format=%H')
            // Gets the current commit (short hash)
            String abbreviated = run(project.projectDir, 'git', 'log', '-1', '--format=%h')
            // Returns the information
            new SCMInfo(
                    branch: branch,
                    commit: commit,
                    abbreviated: abbreviated,
                    dirty: isGitTreeDirty(project.projectDir)
            )
        }
    }

    static boolean isGitTreeDirty(File dir) {
//        return run(dir, 'git', 'status', '--porcelain').trim() != ''
        return 'git update-index -q --ignore-submodules --refresh'.execute([], dir).waitFor() ||
                'git diff-files --quiet --ignore-submodules --'.execute([], dir).waitFor() ||
                'git diff-index --cached --quiet HEAD --ignore-submodules --'.execute([], dir).waitFor()
    }

    @Override
    List<String> getBaseTags(Project project, VersioningExtension extension, String base) {
        def tags = run(project.projectDir, 'git', 'log', 'HEAD', '--pretty=format:%d').readLines()
        def baseTagPattern = /tag: (${base}\.[\d+])/
        return tags.collect { tag ->
            def m = tag =~ baseTagPattern
            if (m.find()) {
                m.group(1)
            } else {
                ''
            }
        }.findAll { it != '' }
    }

    @Override
    String getBranchTypeSeparator() {
        '/'
    }
}
