package net.nemerosa.versioning.git

import net.nemerosa.versioning.SCMInfo
import net.nemerosa.versioning.SCMInfoService
import net.nemerosa.versioning.VersioningExtension
import org.ajoberstar.grgit.Commit
import org.ajoberstar.grgit.Grgit
import org.gradle.api.GradleException
import org.gradle.api.Project

import static net.nemerosa.versioning.support.Utils.run

class GitInfoService implements SCMInfoService {

    @Override
    SCMInfo getInfo(Project project, VersioningExtension extension) {
        // Is Git enabled?
        boolean hasGit = project.rootProject.file('.git').exists()
        // No Git information
        if (!hasGit) {
            SCMInfo.NONE
        }
        // Git information available
        else {
            // Open the Git repo
            //noinspection GroovyAssignabilityCheck
            def grgit = Grgit.open(currentDir: project.projectDir)
            // Gets the branch info
            String branch = grgit.branch.current.name
            // Gets the commit info (full hash)
            List<Commit> commits = grgit.log(maxCommits: 1)
            if (commits.empty) {
                throw new GradleException("No commit available in the repository - cannot compute version")
            }
            String commit = commits[0].id
            // Gets the current commit (short hash)
            String abbreviated = commits[0].abbreviatedId

            // Gets the current tag, if any
            String tag
            String described = grgit.repository.jgit.describe().setLong(true).call()
            if (described) {
                // The format returned by the long version of the `describe` command is: <tag>-<number>-<commit>
                def m = described =~ /^(.*)-(\d+)-g([0-9a-f]+)$/
                if (m.matches()) {
                    def count = m.group(2) as int
                    if (count == 0) {
                        // We're on a tag
                        tag = m.group(1)
                    } else {
                        // No tag
                        tag = null
                    }
                } else {
                    throw new GradleException("Cannot get parse description of current commit: ${described}")
                }
            } else {
                // Nothing returned - it means there is no previous tag
                tag = null
            }

            // Returns the information
            new SCMInfo(
                    branch: branch,
                    commit: commit,
                    abbreviated: abbreviated,
                    dirty: isGitTreeDirty(project.projectDir),
                    tag: tag,
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
        def tags = run(project.projectDir, 'git', 'log', 'HEAD', '--pretty=oneline', '--decorate').readLines()
        return selectBaseTags(base, tags)
    }

    protected static ArrayList<String> selectBaseTags(String base, List<String> tags) {
        def baseTagPattern = /tag: (${base}\.(\d+))/
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
