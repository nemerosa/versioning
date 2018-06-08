package net.nemerosa.versioning.git

import net.nemerosa.versioning.SCMInfo
import net.nemerosa.versioning.SCMInfoService
import net.nemerosa.versioning.VersioningExtension
import net.nemerosa.versioning.support.TagSupport
import org.ajoberstar.grgit.Commit
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.Status
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Ref
import org.gradle.api.GradleException
import org.gradle.api.Project

import static org.eclipse.jgit.lib.Constants.R_TAGS

class GitInfoService implements SCMInfoService {

    @Override
    SCMInfo getInfo(Project project, VersioningExtension extension) {
        // Is Git enabled?
        boolean hasGit = project.rootProject.file('.git').exists() ||
                project.file('.git').exists() ||
                (extension.gitRepoRootDir != null &&
                        new File(extension.gitRepoRootDir, '.git').exists())
        // No Git information
        if (!hasGit) {
            SCMInfo.NONE
        }
        // Git information available
        else {
            // Git directory
            File gitDir = getGitDirectory(extension, project)
            // Open the Git repo
            //noinspection GroovyAssignabilityCheck
            def grgit = Grgit.open(currentDir: gitDir)

            // Check passed in environment variable list
            String branch = null
            for (ev in extension.branchEnv) {
                if (System.env[ev] != null) {
                    branch = System.env[ev]
                    break
                }
            }
            // Gets the branch info from git
            if (branch == null) {
                branch = grgit.branch.current.name
            }

            // Gets the commit info (full hash)
            List<Commit> commits = grgit.log(maxCommits: 1)
            if (commits.empty) {
                throw new GradleException("No commit available in the repository - cannot compute version")
            }

            def lastCommit = commits[0]
            // Full commit hash
            String commit = lastCommit.id
            // Gets the current commit (short hash)
            String abbreviated = lastCommit.abbreviatedId
            // Is the repository shallow?
            boolean shallow = lastCommit.parentIds.empty

            // Gets the current tag, if any
            String tag
            // Cannot use the `describe` command if the repository is shallow
            if (shallow) {
                // Map of tags
                Map<ObjectId, Ref> tags = new HashMap<ObjectId, Ref>();

                def gitRepository = grgit.repository.jgit.repository

                for (Ref r : gitRepository.refDatabase.getRefs(R_TAGS).values()) {
                    ObjectId key = gitRepository.peel(r).getPeeledObjectId();
                    if (key == null)
                        key = r.getObjectId();
                    tags.put(key, r);
                }
                // If we're on a tag, we can use it directly
                Ref lucky = tags.get(gitRepository.resolve(Constants.HEAD))
                if (lucky != null) {
                    tag = lucky.name.substring(R_TAGS.length());
                }
                // If not, we do not go further
                else {
                    tag = null
                }
            } else {
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
            }

            // Last tag
            List<String> lastTags = getLastTags(project, extension, extension.lastTagPattern)
            String lastTag = lastTags.empty ? null : lastTags.first()

            // Returns the information
            new SCMInfo(
                    branch: branch,
                    commit: commit,
                    abbreviated: abbreviated,
                    dirty: isGitTreeDirty(gitDir),
                    tag: tag,
                    lastTag: lastTag,
                    shallow: shallow,
            )
        }
    }

    /**
     * Gets the actual Git working directory to use.
     * @param extension Extension of the plugin
     * @param project Project
     * @return Directory to use
     */
    protected static File getGitDirectory(VersioningExtension extension, Project project) {
        return extension.gitRepoRootDir ?
                new File(extension.gitRepoRootDir) :
                project.projectDir
    }

    static boolean isGitTreeDirty(File dir) {// Open the Git repo
        //noinspection GroovyAssignabilityCheck
        Status status = Grgit.open(currentDir: dir).status()
        return !isClean(status)
    }

    private static boolean isClean(Status status) {
        return status.staged.allChanges.empty &&
                status.unstaged.allChanges.findAll { !it.startsWith('userHome/') }.empty
    }

    @Override
    List<String> getBaseTags(Project project, VersioningExtension extension, String base) {
        return getLastTags(
                project,
                extension,
                /^${base}\.(\d+)$/
        )
    }

    @Override
    List<String> getLastTags(Project project, VersioningExtension extension, String tagPattern) {
        // Git access
        //noinspection GroovyAssignabilityCheck
        def grgit = Grgit.open(currentDir: getGitDirectory(extension, project))
        // List all tags
        return grgit.tag.list()
        // ... filters using the pattern
                .findAll { (it.name =~ tagPattern).find() }
        // ... sort by desc commit time
                .sort { -it.commit.time }
        // ... (#36) commit time is not enough. We have also to consider the case where several pattern compliant tags
        // ...       are on the same commit, and we must sort them by desc version
                .sort { -TagSupport.tagOrder(tagPattern, it.name) }
        // ... gets their name only
                .collect { it.name }
    }

    @Override
    String getBranchTypeSeparator() {
        '/'
    }
}
